/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.com/ for updates and contact.
 *
 * -- GPL LICENSE NOTICE --
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -- GPL LICENSE NOTICE --
 *
 * $RCSfile: ScriptographerEngine.cpp,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/03/25 17:09:14 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_Art.h" // for com_scriptographer_ai_Art_TYPE_LAYER
#include "com_scriptographer_adm_Notifier.h"
#ifdef MAC_ENV 
#include "macUtils.h"
#else
#include "loadJava.h"
#endif
#include "aiGlobals.h"

ScriptographerEngine *gEngine = NULL;

// Workaround for MacOS: The JVM needs to be created on a thread other than
// the main thread. only like this, java.awt.* becomes available, even in
// headless mode! otherwise, the following error would be printed out:

// "Can't start the AWT because Java was started on the first thread.
// Make sure StartOnFirstThread is not specified in your application's
// Info.plist or on the command line."

// headless is still needed due to a bug with concurencing UI loops between 
// carbon and java's cocoa in JDK >= 1.4.

#ifdef MAC_ENV

OSStatus javaThread(ScriptographerEngine *engine) {
	// the returned JavaVM needs to be destroyed. this may hang until the end of the app.
	// do not call this in ScriptographerEngine::javaThread because ScriptographerEngine
	// will be destroyed after the fResponseQueue notification.
	JavaVM *jvm = engine->javaThread();
	if (jvm != NULL)
		jvm->DestroyJavaVM();
	return noErr;
}

/**
 * returns the JavaVM to be destroyed after execution.
 */
JavaVM *ScriptographerEngine::javaThread() {
	try {
		init();
		// tell the constructor that the initialization is done:
		MPNotifyQueue(fResponseQueue, NULL, NULL, NULL);
	} catch (Exception *e) {
		// let the user know about this error:
		MPNotifyQueue(fResponseQueue, e, NULL, NULL);
		return NULL;
	}
	// keep this thread alive until the JVM is to be destroyed. This needs
	// to happen from the creation thread as well, otherwise JNI hangs endlessly:
	MPWaitOnQueue(fRequestQueue, NULL, NULL, NULL, kDurationForever);
	// now exit, and destroy the JavaVM. 
	JavaVM *jvm = exit();
	// now tell the caller that the engine can be deleted, before DestroyJavaVM is called,
	// which may block the current thread until the end of the app.
	MPNotifyQueue(fResponseQueue, NULL, NULL, NULL);
	return jvm; 
}

#endif

ScriptographerEngine::ScriptographerEngine(const char *homeDir) {
	fInitialized = false;
	fHomeDir = new char[strlen(homeDir) + 1];
	strcpy(fHomeDir, homeDir);
	Exception *exc = NULL;
#ifdef MAC_ENV
	if(MPLibraryIsLoaded()) {
		MPCreateQueue(&fRequestQueue);
		MPCreateQueue(&fResponseQueue);
		MPCreateTask((TaskProc)::javaThread, this, 0, NULL, NULL, NULL, 0, NULL); 
		// now wait for the javaThread to finish initialization
		// exceptions that happen in the javaThread are passed through to this thread in order to display the error code:
		MPWaitOnQueue(fResponseQueue, (void **) &exc, NULL, NULL, kDurationForever);
	} else 
#endif
	{	// on windows, we can directly call the initialize function:
		try {
			init();
		} catch (Exception *e) {
			exc = e;
		}
	}
	if (exc != NULL) {
		exc->report(getEnv());
		delete exc;
		throw new StringException("Cannot create ScriptographerEngine.");
	}
}

ScriptographerEngine::~ScriptographerEngine() {
	delete fHomeDir;
	callStaticVoidMethodReport(NULL, cls_ScriptographerEngine, mid_ScriptographerEngine_destroy);
#ifdef MAC_ENV
	if(MPLibraryIsLoaded()) {
		// notify the JVM thread to end, then clean up:
		MPNotifyQueue(fRequestQueue, NULL, NULL, NULL);
		// now wait for the javaThread to finish before destroying the engine
		MPWaitOnQueue(fResponseQueue, NULL, NULL, NULL, kDurationForever);
		// clean up...
		MPDeleteQueue(fRequestQueue);
		MPDeleteQueue(fResponseQueue);
	} else 
#endif
	{	// call exit directly, as the machine was created in the main thread:
		JavaVM *jvm = exit();
		jvm->DestroyJavaVM();
	}
}

void ScriptographerEngine::init() {
	// The VM Invokation functions need to be loaded dynamically on Windows,
	// On Mac, the static ones can be used without problems:
    CreateJavaVMProc createJavaVM = NULL;
    GetDefaultJavaVMInitArgsProc getDefaultJavaVMInitArgs = NULL;
#ifdef WIN_ENV
	loadJavaVM("client", &createJavaVM, &getDefaultJavaVMInitArgs);
#else
	createJavaVM = &JNI_CreateJavaVM;
	getDefaultJavaVMInitArgs = &JNI_GetDefaultJavaVMInitArgs;
#endif
	
	// init args
	JavaVMInitArgs args;
	args.version = JNI_VERSION_1_4;
	if (args.version < JNI_VERSION_1_4) getDefaultJavaVMInitArgs(&args);
	JavaVMOption options[7];
	
	char classpath[512];
	// only add the loader to the classpath, the rest is done in java:
	sprintf(classpath, "-Djava.class.path=%s" PATH_SEP_STR "loader.jar", fHomeDir);
	int numOptions = 0;
	options[numOptions++].optionString = classpath;
#ifdef MAC_ENV
	// start headless, in order to avoid conflicts with AWT and Illustrator on Mac
	// TODO: see wether this works on windows
	options[numOptions++].optionString = "-Djava.awt.headless=true";
	// use the carbon line separator instead of the unix one on mac:
	options[numOptions++].optionString = "-Dline.separator=\r";
#else
//	options[numOptions++].optionString = "-Djava.awt.headless=true";
#endif

	int noDebug = numOptions;

#ifdef _DEBUG
	// start JVM in debug mode, for remote debuggin on port 4000
	options[numOptions++].optionString = "-Xdebug";
	options[numOptions++].optionString = "-Xnoagent";
	options[numOptions++].optionString = "-Djava.compiler=NONE";
	options[numOptions++].optionString = "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=4000";
#endif
//	numOptions = noDebug;

	args.options = options;
	args.nOptions = numOptions;
	
	args.ignoreUnrecognized = JNI_TRUE;

	// create the JVM
	JNIEnv *env;
	jint res = createJavaVM(&fJavaVM, (void **)&env, (void *)&args);
    if (res < 0)
        throw new StringException("Cannot create Java VM.");

	fJavaEngine = NULL;
	
	// link the native functions to the java functions. The code for this is in registerNatives.cpp,
	// which is automatically generated from the JNI header files by jni.js
	cls_Loader = env->FindClass("com/scriptographer/loader/Loader");
	mid_Loader_init = getStaticMethodID(env, cls_Loader, "init", "(Ljava/lang/String;)V");
	mid_Loader_reload = getStaticMethodID(env, cls_Loader, "reload", "()Ljava/lang/String;");
	mid_Loader_loadClass = getStaticMethodID(env, cls_Loader, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
	callStaticObjectMethodReport(env, cls_Loader, mid_Loader_init, env->NewStringUTF(fHomeDir));
	registerNatives(env);
	initReflection(env);
}

/**
 * Initializes the engine, e.g. activates console redirection and similar things. To be called when
 * all the underlying native structures are initiialized.
 */
void ScriptographerEngine::initEngine() {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_ScriptographerEngine, mid_ScriptographerEngine_init);
		fInitialized = true;
	} EXCEPTION_CATCH_REPORT(env)
}

/**
 * Reloads the engine through Loader.reload, which does the half of the work. The other half is to
 * remap the reflections and register the natives again.
 */
jstring ScriptographerEngine::reloadEngine() {
	JNIEnv *env = getEnv();
	jstring errors = (jstring) callStaticObjectMethodReport(env, cls_Loader, mid_Loader_reload);
	fInitialized = false;
	registerNatives(env);
	initReflection(env);
	fJavaEngine = NULL;
	initEngine();
	return errors;
}

/**
 * Cleans up everything except destroying the fJavaVM, which has some problems
 * with the thread workaround on mac: It blocks until the main thread is done, which
 * is at the shutdown of the application.
 * The caller of exit() needs to call DestroyJavaVM on the returned JavaVM object after
 * having executed all other important functions, such as MPNotifyQueue...
 */ 
JavaVM *ScriptographerEngine::exit() {
	fJavaVM->DetachCurrentThread();
	
	return fJavaVM;
}

/**
 * Loads the Java classes, and the method and field descriptors required for Java reflection.
 * Returns true on success, false on failure.
 */
void ScriptographerEngine::initReflection(JNIEnv *env) {
	cls_System = env->FindClass("java/lang/System");
	fid_System_out = env->GetStaticFieldID(cls_System, "out", "Ljava/io/PrintStream;");

	cls_PrintStream = env->FindClass("java/io/PrintStream");
	mid_PrintStream_println = env->GetMethodID(cls_PrintStream, "println", "(Ljava/lang/String;)V");

	cls_Class = loadClass(env, "java/lang/Class");
	mid_Class_getName = getMethodID(env, cls_Class, "getName", "()Ljava/lang/String;");

	cls_String = loadClass(env, "java/lang/String");
	cid_String = getConstructorID(env, cls_String, "([B)V");
	mid_String_getBytes = getMethodID(env, cls_String, "getBytes", "()[B");

	cls_Number = loadClass(env, "java/lang/Number");
	mid_Number_intValue = getMethodID(env, cls_Number, "intValue", "()I");

	cls_Integer = loadClass(env, "java/lang/Integer");
	cid_Integer = getConstructorID(env, cls_Integer, "(I)V");

	cls_Float = loadClass(env, "java/lang/Float");
	cid_Float = getConstructorID(env, cls_Float, "(F)V");

	cls_Boolean = loadClass(env, "java/lang/Boolean");
	cid_Boolean = getConstructorID(env, cls_Boolean, "(Z)V");
	mid_Boolean_booleanValue = getMethodID(env, cls_Boolean, "booleanValue", "()Z");
	
	cls_ObjectArray = loadClass(env, "[Ljava/lang/Object;");
	
	cls_File = loadClass(env, "java/io/File");
	cid_File = getConstructorID(env, cls_File, "(Ljava/lang/String;)V");
	mid_File_getPath = getMethodID(env, cls_File, "getPath", "()Ljava/lang/String;");

	cls_Collection = loadClass(env, "java/util/Collection");
	mid_Collection_add = getMethodID(env, cls_Collection, "add", "(Ljava/lang/Object;)Z");
	mid_Collection_iterator = getMethodID(env, cls_Collection, "iterator", "()Ljava/util/Iterator;");
	mid_Collection_size = getMethodID(env, cls_Collection, "size", "()I");
	
	cls_Map = loadClass(env, "java/util/Map");
	mid_Map_entrySet = getMethodID(env, cls_Map, "entrySet", "()Ljava/util/Set;");
	mid_Map_put = getMethodID(env, cls_Map, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
	mid_Map_get = getMethodID(env, cls_Map, "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
	
	cls_Map_Entry = loadClass(env, "java/util/Map$Entry");
	mid_Map_Entry_getKey = getMethodID(env, cls_Map_Entry, "getKey", "()Ljava/lang/Object;");
	mid_Map_Entry_getValue = getMethodID(env, cls_Map_Entry, "getValue", "()Ljava/lang/Object;");
	
	cls_HashMap = loadClass(env, "java/util/HashMap");
	cid_HashMap = getConstructorID(env, cls_HashMap, "()V");
	
	cls_Set = loadClass(env, "java/util/Set");
	mid_Set_iterator = getMethodID(env, cls_Set, "iterator", "()Ljava/util/Iterator;");

	cls_Iterator = loadClass(env, "java/util/Iterator");
	mid_Iterator_hasNext = getMethodID(env, cls_Iterator, "hasNext", "()Z");
	mid_Iterator_next = getMethodID(env, cls_Iterator, "next", "()Ljava/lang/Object;");

	cls_OutOfMemoryError = loadClass(env, "java/lang/OutOfMemoryError");

	cls_awt_Color = loadClass(env, "java/awt/Color");
	cid_awt_Color = getConstructorID(env, cls_awt_Color, "(FFF)V");
	mid_awt_Color_getColorComponents = getMethodID(env, cls_awt_Color, "getColorComponents", "([F)[F");
	
	cls_awt_Rectangle = loadClass(env, "java/awt/Rectangle");
	cid_awt_Rectangle = getConstructorID(env, cls_awt_Rectangle, "(IIII)V");
	fid_awt_Rectangle_x = getFieldID(env, cls_awt_Rectangle, "x", "I");
	fid_awt_Rectangle_y = getFieldID(env, cls_awt_Rectangle, "y", "I");
	fid_awt_Rectangle_width = getFieldID(env, cls_awt_Rectangle, "width", "I");
	fid_awt_Rectangle_height = getFieldID(env, cls_awt_Rectangle, "height", "I");
	mid_awt_Rectangle_setBounds = getMethodID(env, cls_awt_Rectangle, "setBounds", "(IIII)V");

	cls_awt_Point = loadClass(env, "java/awt/Point");
	cid_awt_Point = getConstructorID(env, cls_awt_Point, "(II)V");
	fid_awt_Point_x = getFieldID(env, cls_awt_Point, "x", "I");
	fid_awt_Point_y = getFieldID(env, cls_awt_Point, "y", "I");
	mid_awt_Point_setLocation = getMethodID(env, cls_awt_Point, "setLocation", "(II)V");
	
	cls_awt_Dimension = loadClass(env, "java/awt/Dimension");
	cid_awt_Dimension = getConstructorID(env, cls_awt_Dimension, "(II)V");
	fid_awt_Dimension_width = getFieldID(env, cls_awt_Dimension, "width", "I");
	fid_awt_Dimension_height = getFieldID(env, cls_awt_Dimension, "height", "I");
	mid_awt_Dimension_setSize = getMethodID(env, cls_awt_Dimension, "setSize", "(II)V");

	cls_awt_AffineTransform = loadClass(env, "java/awt/geom/AffineTransform");
	cid_awt_AffineTransform = getConstructorID(env, cls_awt_AffineTransform, "(DDDDDD)V");;
	mid_awt_AffineTransform_getScaleX = getMethodID(env, cls_awt_AffineTransform, "getScaleX", "()D");
	mid_awt_AffineTransform_getShearY = getMethodID(env, cls_awt_AffineTransform, "getShearY", "()D");
	mid_awt_AffineTransform_getShearX = getMethodID(env, cls_awt_AffineTransform, "getShearX", "()D");
	mid_awt_AffineTransform_getScaleY = getMethodID(env, cls_awt_AffineTransform, "getScaleY", "()D");
	mid_awt_AffineTransform_getTranslateX = getMethodID(env, cls_awt_AffineTransform, "getTranslateX", "()D");
	mid_awt_AffineTransform_getTranslateY = getMethodID(env, cls_awt_AffineTransform, "getTranslateY", "()D");

	cls_awt_ICC_Profile = loadClass(env, "java/awt/color/ICC_Profile");
	mid_awt_ICC_Profile_getInstance = getStaticMethodID(env, cls_awt_ICC_Profile, "getInstance", "([B)Ljava/awt/color/ICC_Profile;");

// Scriptographer:

	cls_ScriptographerEngine = loadClass(env, "com/scriptographer/ScriptographerEngine");
	mid_ScriptographerEngine_getInstance = getStaticMethodID(env, cls_ScriptographerEngine, "getInstance", "()Lcom/scriptographer/ScriptographerEngine;");
	mid_ScriptographerEngine_executeString = getMethodID(env, cls_ScriptographerEngine, "executeString", "(Ljava/lang/String;Lorg/mozilla/javascript/Scriptable;)Lorg/mozilla/javascript/Scriptable;");
	mid_ScriptographerEngine_executeFile = getMethodID(env, cls_ScriptographerEngine, "executeFile", "(Ljava/lang/String;Lorg/mozilla/javascript/Scriptable;)Lorg/mozilla/javascript/Scriptable;");
	mid_ScriptographerEngine_init = getStaticMethodID(env, cls_ScriptographerEngine, "init", "()V");
	mid_ScriptographerEngine_destroy = getStaticMethodID(env, cls_ScriptographerEngine, "destroy", "()V");
	mid_ScriptographerEngine_onAbout = getStaticMethodID(env, cls_ScriptographerEngine, "onAbout", "()V");

	cls_ScriptographerException = loadClass(env, "com/scriptographer/ScriptographerException");

	cls_Handle = loadClass(env, "com/scriptographer/util/Handle");
	cid_Handle = getConstructorID(env, cls_Handle, "(I)V");
	fid_Handle_handle = getFieldID(env, cls_Handle, "handle", "I");

// AI:

	cls_AIObject = loadClass(env, "com/scriptographer/ai/AIObject");
	fid_AIObject_handle = getFieldID(env, cls_AIObject, "handle", "I");

	cls_Tool = loadClass(env, "com/scriptographer/ai/Tool");
	cid_Tool = getConstructorID(env, cls_Tool, "(II)V");
	mid_Tool_onEditOptions = getStaticMethodID(env, cls_Tool, "onEditOptions", "(I)V");
	mid_Tool_onSelect = getStaticMethodID(env, cls_Tool, "onSelect", "(I)V");
	mid_Tool_onDeselect = getStaticMethodID(env, cls_Tool, "onDeselect", "(I)V");
	mid_Tool_onReselect = getStaticMethodID(env, cls_Tool, "onReselect", "(I)V");
	mid_Tool_onMouseDrag = getStaticMethodID(env, cls_Tool, "onMouseDrag", "(IFFI)V");
	mid_Tool_onMouseDown = getStaticMethodID(env, cls_Tool, "onMouseDown", "(IFFI)V");
	mid_Tool_onMouseUp = getStaticMethodID(env, cls_Tool, "onMouseUp", "(IFFI)V");

	cls_Point = loadClass(env, "com/scriptographer/ai/Point");
	cid_Point = getConstructorID(env, cls_Point, "(FF)V");
	fid_Point_x = getFieldID(env, cls_Point, "x", "F");
	fid_Point_y = getFieldID(env, cls_Point, "y", "F");
	mid_Point_setPoint = getMethodID(env, cls_Point, "setPoint", "(FF)V");

	cls_Rectangle = loadClass(env, "com/scriptographer/ai/Rectangle");
	cid_Rectangle = getConstructorID(env, cls_Rectangle, "(FFFF)V");
	fid_Rectangle_x = getFieldID(env, cls_Rectangle, "x", "F");
	fid_Rectangle_y = getFieldID(env, cls_Rectangle, "y", "F");
	fid_Rectangle_width = getFieldID(env, cls_Rectangle, "width", "F");
	fid_Rectangle_height = getFieldID(env, cls_Rectangle, "height", "F");
	mid_Rectangle_setRect = getMethodID(env, cls_Rectangle, "setRect", "(FFFF)V");
	
	cls_Color = loadClass(env, "com/scriptographer/ai/Color");
	mid_Color_getComponents = getMethodID(env, cls_Color, "getComponents", "()[F");

	cls_Grayscale = loadClass(env, "com/scriptographer/ai/Grayscale");
	cid_Grayscale = getConstructorID(env, cls_Grayscale, "(FF)V");

	cls_RGBColor = loadClass(env, "com/scriptographer/ai/RGBColor");
	cid_RGBColor = getConstructorID(env, cls_RGBColor, "(FFFF)V");

	cls_CMYKColor = loadClass(env, "com/scriptographer/ai/CMYKColor");
	cid_CMYKColor = getConstructorID(env, cls_CMYKColor, "(FFFFF)V");

	cls_Art = loadClass(env, "com/scriptographer/ai/Art");
	mid_Art_wrapHandle = getStaticMethodID(env, cls_Art, "wrapHandle", "(II)Lcom/scriptographer/ai/Art;");
	mid_Art_updateIfWrapped = getStaticMethodID(env, cls_Art, "updateIfWrapped", "(I)Z");
	mid_Art_onSelectionChanged = getStaticMethodID(env, cls_Art, "onSelectionChanged", "([I)V");

	cls_ArtSet = loadClass(env, "com/scriptographer/ai/ArtSet");
	cid_ArtSet = getConstructorID(env, cls_ArtSet, "()V");

	cls_Path = loadClass(env, "com/scriptographer/ai/Path");
	
	cls_PathStyle = loadClass(env, "com/scriptographer/ai/PathStyle");
	mid_PathStyle_init = getMethodID(env, cls_PathStyle, "init", "(Lcom/scriptographer/ai/Color;ZLcom/scriptographer/ai/Color;ZFF[FSSFZZZF)V");

	cls_FillStyle = loadClass(env, "com/scriptographer/ai/FillStyle");
	cid_FillStyle = getConstructorID(env, cls_FillStyle, "(Lcom/scriptographer/ai/Color;Z)V");

	cls_StrokeStyle = loadClass(env, "com/scriptographer/ai/StrokeStyle");
	cid_StrokeStyle = getConstructorID(env, cls_StrokeStyle, "(Lcom/scriptographer/ai/Color;ZFF[FSSF)V");
	
	cls_Group = loadClass(env, "com/scriptographer/ai/Group");
	
	cls_Raster = loadClass(env, "com/scriptographer/ai/Raster");
	fid_Raster_rasterData = getFieldID(env, cls_Raster, "rasterData", "I");
	
	cls_Layer = loadClass(env, "com/scriptographer/ai/Layer");

	cls_Segment = loadClass(env, "com/scriptographer/ai/Segment");
	cls_Curve = loadClass(env, "com/scriptographer/ai/Curve");

	cls_SegmentPosition = loadClass(env, "com/scriptographer/ai/SegmentPosition");
	cid_SegmentPosition = getConstructorID(env, cls_SegmentPosition, "(IF)V");

	cls_TabletValue = loadClass(env, "com/scriptographer/ai/TabletValue");
	cid_TabletValue = getConstructorID(env, cls_TabletValue, "(FF)V");
	fid_TabletValue_offset = getFieldID(env, cls_TabletValue, "offset", "F");
	fid_TabletValue_value = getFieldID(env, cls_TabletValue, "value", "F");
	
	cls_Document = loadClass(env, "com/scriptographer/ai/Document");

	cls_LiveEffect = loadClass(env, "com/scriptographer/ai/LiveEffect");
	cid_LiveEffect = getConstructorID(env, cls_LiveEffect, "(ILjava/lang/String;Ljava/lang/String;IIIII)V");
	mid_LiveEffect_onEditParameters = getStaticMethodID(env, cls_LiveEffect, "onEditParameters", "(ILjava/util/Map;IZ)V");
	mid_LiveEffect_onCalculate = getStaticMethodID(env, cls_LiveEffect, "onCalculate", "(ILjava/util/Map;Lcom/scriptographer/ai/Art;)I");
	mid_LiveEffect_onGetInputType = getStaticMethodID(env, cls_LiveEffect, "onGetInputType", "(ILjava/util/Map;Lcom/scriptographer/ai/Art;)I");
	
	cls_MenuItem = loadClass(env, "com/scriptographer/ai/MenuItem");
	mid_MenuItem_wrapItemHandle =getStaticMethodID(env, cls_MenuItem, "wrapItemHandle", "(ILjava/lang/String;Ljava/lang/String;ILjava/lang/String;)Lcom/scriptographer/ai/MenuItem;");
	mid_MenuItem_onClick = getStaticMethodID(env, cls_MenuItem, "onClick", "(I)V");
	mid_MenuItem_onUpdate = getStaticMethodID(env, cls_MenuItem, "onUpdate", "(IIII)V");

	cls_MenuGroup = loadClass(env, "com/scriptographer/ai/MenuGroup");
	
// ADM:

	cls_ADMObject = loadClass(env, "com/scriptographer/adm/ADMObject");
	fid_ADMObject_handle = getFieldID(env, cls_ADMObject, "handle", "I");

	cls_Dialog = loadClass(env, "com/scriptographer/adm/Dialog");
	fid_Dialog_size = getFieldID(env, cls_Dialog, "size", "Ljava/awt/Dimension;");
	fid_Dialog_bounds = getFieldID(env, cls_Dialog, "bounds", "Ljava/awt/Rectangle;");

	cls_ModalDialog = loadClass(env, "com/scriptographer/adm/ModalDialog");
	fid_ModalDialog_doesModal = getFieldID(env, cls_ModalDialog, "doesModal", "Z");

	cls_PopupDialog = loadClass(env, "com/scriptographer/adm/PopupDialog");

	cls_DialogGroupInfo = loadClass(env, "com/scriptographer/adm/DialogGroupInfo");
	cid_DialogGroupInfo = getConstructorID(env, cls_DialogGroupInfo, "(Ljava/lang/String;I)V");

	cls_Drawer = loadClass(env, "com/scriptographer/adm/Drawer");

	cls_Image = loadClass(env, "com/scriptographer/adm/Image");
	fid_Image_byteWidth = getFieldID(env, cls_Image, "byteWidth", "I");
	fid_Image_bitsPerPixel = getFieldID(env, cls_Image, "bitsPerPixel", "I");
	mid_Image_getIconHandle = getMethodID(env, cls_Image, "getIconHandle", "()I");

	cls_Item = loadClass(env, "com/scriptographer/adm/Item");
	fid_Item_size = getFieldID(env, cls_Item, "size", "Ljava/awt/Dimension;");
	fid_Item_bounds = getFieldID(env, cls_Item, "bounds", "Ljava/awt/Rectangle;");
	
	cls_ListItem = loadClass(env, "com/scriptographer/adm/ListItem");
	fid_ListItem_listHandle = getFieldID(env, cls_ListItem, "listHandle", "I");	
	
	cls_HierarchyList = loadClass(env, "com/scriptographer/adm/HierarchyList");

	cls_ListEntry = loadClass(env, "com/scriptographer/adm/ListEntry");

	cls_HierarchyListEntry = loadClass(env, "com/scriptographer/adm/HierarchyListEntry");

	cls_NotificationHandler = loadClass(env, "com/scriptographer/adm/NotificationHandler");
	fid_NotificationHandler_tracker = getFieldID(env, cls_NotificationHandler, "tracker", "Lcom/scriptographer/adm/Tracker;");
	fid_NotificationHandler_drawer = getFieldID(env, cls_NotificationHandler, "drawer", "Lcom/scriptographer/adm/Drawer;");
	mid_NotificationHandler_onNotify_String = getMethodID(env, cls_NotificationHandler, "onNotify", "(Ljava/lang/String;)V");
	mid_NotificationHandler_onNotify_int = getMethodID(env, cls_NotificationHandler, "onNotify", "(I)V");
	mid_NotificationHandler_onDraw = getMethodID(env, cls_NotificationHandler, "onDraw", "(Lcom/scriptographer/adm/Drawer;)V");

	cls_CallbackHandler = loadClass(env, "com/scriptographer/adm/CallbackHandler");
	mid_CallbackHandler_onResize = getMethodID(env, cls_CallbackHandler, "onResize", "(II)V");
	
	cls_Tracker = loadClass(env, "com/scriptographer/adm/Tracker");
	mid_Tracker_onTrack = getMethodID(env, cls_Tracker, "onTrack", "(Lcom/scriptographer/adm/NotificationHandler;IIIIIICCJ)Z");
}

/**
 * Cashes the javaScriptEngine, returned by ScriptographerEngine.getInstance()
 *
 * throws exceptions
 */
jobject ScriptographerEngine::getJavaEngine() {
	if (fJavaEngine == NULL) {
		JNIEnv *env = getEnv();
		fJavaEngine = env->NewWeakGlobalRef(callStaticObjectMethod(env, cls_ScriptographerEngine, mid_ScriptographerEngine_getInstance));
	}
	return fJavaEngine;
}

/**
 * Evaluates the given string as a script, catches occuring exceptions and prints them out on the console.
 * TODO: Add more finegrained exception parsing instead of just printing the whole stacktrace that goes deep down
 * in Rhino
 *
 * catches exceptions
 */
void ScriptographerEngine::executeString(const char* string) {
	JNIEnv *env = getEnv();
	try {
		jobject engine = getJavaEngine();
		callVoidMethod(env, engine, mid_ScriptographerEngine_executeString, createJString(env, string), NULL);
	} EXCEPTION_CATCH_REPORT(env)
}

/**
 * Evaluates the given file as a script, catches occuring exceptions and prints them out on the console.
 * TODO: Add more finegrained exception parsing instead of just printing the whole stacktrace that goes deep down
 * in Rhino
 *
 * catches exceptions
 */
void ScriptographerEngine::executeFile(const char* filename) {
#ifdef MAC_ENV
	char path[512];
	carbonPathToPosixPath(filename, path);
#else
	const char *path = filename;
#endif
	JNIEnv *env = getEnv();
	try {
		jobject engine = getJavaEngine();
		callVoidMethod(env, engine, mid_ScriptographerEngine_executeFile, createJString(env, path), NULL);
	} EXCEPTION_CATCH_REPORT(env)
}

void ScriptographerEngine::println(JNIEnv *env, const char *str, ...) {
	char text[2048];
	va_list args;
	va_start(args, str);
	vsprintf(text, str, args);
	va_end(args);
	callVoidMethodReport(env, env->GetStaticObjectField(cls_System, fid_System_out), mid_PrintStream_println, env->NewStringUTF(text));
}

// com.scriptographer.awt.Point <-> AIRealPoint
jobject ScriptographerEngine::convertPoint(JNIEnv *env, AIRealPoint *pt, jobject res) {
	if (res == NULL) {
		return newObject(env, cls_Point, cid_Point, (jfloat)pt->h, (jfloat)pt->v);
	} else {
		callVoidMethod(env, res, mid_Point_setPoint, (jfloat)pt->h, (jfloat)pt->v);
		return res;
	}
}

AIRealPoint *ScriptographerEngine::convertPoint(JNIEnv *env, jobject pt, AIRealPoint *res) {
	if (res == NULL) res = new AIRealPoint;
	res->h = env->GetFloatField(pt, fid_Point_x);
	res->v = env->GetFloatField(pt, fid_Point_y);
	EXCEPTION_CHECK(env)
	return res;
}

// java.awt.Point <-> ADMPoint
jobject ScriptographerEngine::convertPoint(JNIEnv *env, ADMPoint *pt, jobject res) {
	if (res == NULL) {
		return newObject(env, cls_awt_Point, cid_Point, (jint)pt->h, (jint)pt->v);
	} else {
		callVoidMethod(env, res, mid_awt_Point_setLocation, (jint)pt->h, (jint)pt->v);
		return res;
	}
}

ADMPoint *ScriptographerEngine::convertPoint(JNIEnv *env, jobject pt, ADMPoint *res) {
	if (res == NULL) res = new ADMPoint;
	res->h = env->GetIntField(pt, fid_awt_Point_x);
	res->v = env->GetIntField(pt, fid_awt_Point_y);
	EXCEPTION_CHECK(env)
	return res;
}

// com.scriptographer.awt.Rectangle <-> AIRealRect
jobject ScriptographerEngine::convertRectangle(JNIEnv *env, AIRealRect *rt, jobject res) {
	// AIRealRects are upside down, top and bottom are switched!
	if (res == NULL) {
		return newObject(env, cls_Rectangle, cid_Rectangle, (jfloat)rt->left, (jfloat)rt->bottom, (jfloat)(rt->right - rt->left), (jfloat)(rt->top - rt->bottom));
	} else {
		callVoidMethod(env, res, mid_Rectangle_setRect, (jfloat)rt->left, (jfloat)rt->bottom, (jfloat)(rt->right - rt->left), (jfloat)(rt->top - rt->bottom));
		return res;
	}
}

AIRealRect *ScriptographerEngine::convertRectangle(JNIEnv *env, jobject rt, AIRealRect *res) {
	// AIRealRects are upside down, top and bottom are switched!
	if (res == NULL) res = new AIRealRect;
	res->left =  env->GetFloatField(rt, fid_Rectangle_x);
	res->bottom =  env->GetFloatField(rt, fid_Rectangle_y);
	res->right = res->left + env->GetFloatField(rt, fid_Rectangle_width);
	res->top = res->bottom + env->GetFloatField(rt, fid_Rectangle_height);
	EXCEPTION_CHECK(env)
	return res;
}

// java.awt.Rectangle <-> ADMRect
jobject ScriptographerEngine::convertRectangle(JNIEnv *env, ADMRect *rt, jobject res) {
	if (res == NULL) {
		return newObject(env, cls_awt_Rectangle, cid_awt_Rectangle, (jint)rt->left, (jint)rt->top, (jint)(rt->right - rt->left), (jint)(rt->bottom - rt->top));
	} else {
		callVoidMethod(env, res, mid_awt_Rectangle_setBounds, (jint)rt->left, (jint)rt->top, (jint)(rt->right - rt->left), (jint)(rt->bottom - rt->top));
		return res;
	}
}

ADMRect *ScriptographerEngine::convertRectangle(JNIEnv *env, jobject rt, ADMRect *res) {
	if (res == NULL) res = new ADMRect;
	res->left = env->GetIntField(rt, fid_awt_Rectangle_x);
	res->top = env->GetIntField(rt, fid_awt_Rectangle_y);
	res->right = res->left + env->GetIntField(rt, fid_awt_Rectangle_width);
	res->bottom = res->top + env->GetIntField(rt, fid_awt_Rectangle_height);
	EXCEPTION_CHECK(env)
	return res;
}

// java.awt.Dimension <-> ADMPoint
jobject ScriptographerEngine::convertDimension(JNIEnv *env, ADMPoint *pt, jobject res) {
	if (res == NULL) {
		return newObject(env, cls_awt_Dimension, cid_awt_Dimension, (jint)pt->h, (jint)pt->v);
	} else {
		callVoidMethod(env, res, mid_awt_Dimension_setSize, (jint)pt->h, (jint)pt->v);
		return res;
	}
}

ADMPoint *ScriptographerEngine::convertDimension(JNIEnv *env, jobject dim, ADMPoint *res) {
	if (res == NULL) res = new ADMPoint;
	res->h = env->GetIntField(dim, fid_awt_Dimension_width);
	res->v = env->GetIntField(dim, fid_awt_Dimension_height);
	EXCEPTION_CHECK(env)
	return res;
}

// java.awt.Color <-> ADMRGBColor
jobject ScriptographerEngine::convertColor(JNIEnv *env, ADMRGBColor *srcCol) {
	return newObject(env, cls_awt_Color, cid_awt_Color, (jfloat)srcCol->red / 65535.0, (jfloat)srcCol->green / 65535.0, (jfloat)srcCol->blue / 65535.0);
}

ADMRGBColor *ScriptographerEngine::convertColor(JNIEnv *env, jobject srcCol, ADMRGBColor *dstCol) {
	if (dstCol == NULL) dstCol = new ADMRGBColor;
	jfloatArray array = (jfloatArray) env->CallObjectMethod(srcCol, mid_awt_Color_getColorComponents, NULL);
	int length = env->GetArrayLength(array);
	// TODO: add handling for different values of length!!!
	jfloat *values = new jfloat[length];
	env->GetFloatArrayRegion(array, 0, length, values);
	dstCol->red = (values[0] * 65535.0 + 0.5);
	dstCol->green = (values[1] * 65535.0 + 0.5);
	dstCol->blue = (values[2] * 65535.0 + 0.5);
	delete values;
	EXCEPTION_CHECK(env)
	return dstCol;
}

// com.scriptoggrapher.ai.Color <-> AIColor
jobject ScriptographerEngine::convertColor(JNIEnv *env, AIColor *srcCol, AIReal alpha) {
	switch (srcCol->kind) {
		case kGrayColor:
			return newObject(env, cls_Grayscale, cid_Grayscale, (jfloat) srcCol->c.g.gray, (jfloat) alpha);
		case kThreeColor:
			return newObject(env, cls_RGBColor, cid_RGBColor, (jfloat) srcCol->c.rgb.red, (jfloat) srcCol->c.rgb.green, (jfloat) srcCol->c.rgb.blue, (jfloat) alpha);
		case kFourColor:
			return newObject(env, cls_CMYKColor, cid_CMYKColor, (jfloat) srcCol->c.f.cyan, (jfloat) srcCol->c.f.magenta, (jfloat) srcCol->c.f.yellow, (jfloat) srcCol->c.f.black, (jfloat) alpha);
	}
	return NULL;
}

AIColor *ScriptographerEngine::convertColor(JNIEnv *env, jobject srcCol, AIColor *dstCol, AIReal *alpha) {
	return convertColor(env, (jfloatArray) env->CallObjectMethod(srcCol, mid_Color_getComponents), dstCol, alpha);
}

AIColor *ScriptographerEngine::convertColor(JNIEnv *env, jfloatArray srcCol, AIColor *dstCol, AIReal *alpha) {
	if (dstCol == NULL) dstCol = new AIColor;
	int length = env->GetArrayLength(srcCol);
	jfloat *values = new jfloat[length];
	env->GetFloatArrayRegion(srcCol, 0, length, values);
	switch (length) {
	case 2:
		dstCol->kind = kGrayColor;
		dstCol->c.g.gray = values[0];
		if (alpha != NULL)
			*alpha = values[1];
		break;
	case 4:
		dstCol->kind = kThreeColor;
		dstCol->c.rgb.red = values[0];
		dstCol->c.rgb.green = values[1];
		dstCol->c.rgb.blue = values[2];
		if (alpha != NULL)
			*alpha = values[3];
		break;
	case 5:
		dstCol->kind = kFourColor;
		dstCol->c.f.cyan = values[0];
		dstCol->c.f.magenta = values[1];
		dstCol->c.f.yellow = values[2];
		dstCol->c.f.black = values[3];
		if (alpha != NULL)
			*alpha = values[4];
		break;
	}
	delete values;
	return dstCol;
}

// AIColor <-> ADMRGBColor
AIColor *ScriptographerEngine::convertColor(ADMRGBColor *srcCol, AIColor *dstCol) {
	if (dstCol == NULL) dstCol = new AIColor;
	dstCol->kind = kThreeColor;
	dstCol->c.rgb.red = (float) srcCol->red / 65535.0;
	dstCol->c.rgb.green = (float) srcCol->green / 65535.0;
	dstCol->c.rgb.blue = (float) srcCol->blue / 65535.0;
	return dstCol;
}

ADMRGBColor *ScriptographerEngine::convertColor(AIColor *srcCol, ADMRGBColor *dstCol) {
	if (dstCol == NULL) dstCol = new ADMRGBColor;
	// convert to RGB if it isn't already:
	if (srcCol->kind != kThreeColor && !convertColor(srcCol, kAIRGBColorSpace, srcCol))
		return NULL;
	dstCol->red = srcCol->c.rgb.red * 65535.0 + 0.5;
	dstCol->green = srcCol->c.rgb.green * 65535.0 + 0.5;
	dstCol->blue = srcCol->c.rgb.blue * 65535.0 + 0.5;
	return dstCol;
}

// AIColor <-> AIColor
AIColor *ScriptographerEngine::convertColor(AIColor *srcCol, AIColorConversionSpaceValue dstSpace, AIColor *dstCol, AIReal srcAlpha, AIReal *dstAlpha) {
	// determine srcCol's space and sample size:
	AIColorConversionSpaceValue srcSpace;
	int srcSize;
	bool srcHasAlpha = srcAlpha < 1;
	switch (srcCol->kind) {
		case kGrayColor:
			srcSize = 1;
			if (srcHasAlpha) srcSpace = kAIAGrayColorSpace;
			else srcSpace = kAIGrayColorSpace;
			break;
		case kThreeColor:
			srcSize = 3;
			if (srcHasAlpha) srcSpace = kAIARGBColorSpace;
			else srcSpace = kAIRGBColorSpace;
			break;
		case kFourColor: 
			srcSize = 4;
			if (srcHasAlpha) srcSpace = kAIACMYKColorSpace;
			else srcSpace = kAICMYKColorSpace;
			break;
		default:
			return NULL;
	}

	bool dstHasAlpha = dstSpace == kAIACMYKColorSpace || dstSpace == kAIARGBColorSpace || dstSpace == kAIAGrayColorSpace;

	if (srcSpace >= 0 && dstSpace >= 0) {
		AISampleComponent src[5];
		AISampleComponent dst[5];
		memcpy(src, &srcCol->c, srcSize * sizeof(AISampleComponent));
		if (srcHasAlpha) src[srcSize] = srcAlpha;
		// TODO: why swapping kGrayColor???
		if (srcCol->kind == kGrayColor) src[0] = 1.0 - src[0];
		ASBoolean inGamut;
		sAIColorConversion->ConvertSampleColor(srcSpace, src, dstSpace, dst, &inGamut);
		if (dstCol == NULL)
			dstCol = new AIColor;
		// init the destCol with 0
		// memset(dstCol, 0, sizeof(AIColor));
		// determine dstCol's kind and sampleSize:
		int dstSize;
		switch (dstSpace) {
			case kAIMonoColorSpace:
			case kAIGrayColorSpace:
			case kAIAGrayColorSpace:
				dstCol->kind = kGrayColor;
				dstSize = 1;
				break;
			case kAIRGBColorSpace:
			case kAIARGBColorSpace:
				dstCol->kind = kThreeColor;
				dstSize = 3;
				break;
			case kAICMYKColorSpace:
			case kAIACMYKColorSpace:
				dstCol->kind = kFourColor;
				dstSize = 4;
				break;
			default:
				return NULL;
		}
		// TODO: why swapping kGrayColor???
		if (dstCol->kind == kGrayColor) dst[0] = 1.0 - dst[0];
		memcpy(&dstCol->c, dst, dstSize * sizeof(AISampleComponent));
		// get back alpha:
		if (dstAlpha != NULL)
			*dstAlpha = dstHasAlpha ? dst[dstSize] : 1;
		
		return dstCol;
	}
	return NULL;
}

// java.awt.AffineTransform <-> AIRealMatrix
jobject ScriptographerEngine::convertMatrix(JNIEnv *env, AIRealMatrix *mt, jobject res) {
	return newObject(env, cls_awt_AffineTransform, cid_awt_AffineTransform, (jdouble)mt->a, (jdouble)mt->b, (jdouble)mt->c, (jdouble)mt->d, (jdouble)mt->tx, (jdouble)mt->ty);
}

AIRealMatrix *ScriptographerEngine::convertMatrix(JNIEnv *env, jobject mt, AIRealMatrix *res) {
	if (res == NULL) res = new AIRealMatrix;
	res->a = env->CallDoubleMethod(mt, mid_awt_AffineTransform_getScaleX);
	res->b = env->CallDoubleMethod(mt, mid_awt_AffineTransform_getShearY);
	res->c = env->CallDoubleMethod(mt, mid_awt_AffineTransform_getShearX);
	res->d = env->CallDoubleMethod(mt, mid_awt_AffineTransform_getScaleY);
	res->tx = env->CallDoubleMethod(mt, mid_awt_AffineTransform_getTranslateX);
	res->ty = env->CallDoubleMethod(mt, mid_awt_AffineTransform_getTranslateY);
	EXCEPTION_CHECK(env)
	return res;
}


// AIArtSet <-> ArtSet

jobject ScriptographerEngine::convertArtSet(JNIEnv *env, AIArtSet set, bool layerOnly) {
	artSetFilter(set, layerOnly);
	long count;
	sAIArtSet->CountArtSet(set, &count);
	jobject artSet = newObject(env, cls_ArtSet, cid_ArtSet); 
	for (long i = 0; i < count; i++) {
		jobject obj;
		AIArtHandle art;
		if (!sAIArtSet->IndexArtSet(set, i, &art)) {
			obj = wrapArtHandle(env, art);
			if (obj != NULL)
				callBooleanMethod(env, artSet, mid_Collection_add, obj);
		}
	}
	EXCEPTION_CHECK(env)
	return artSet;
}

AIArtSet ScriptographerEngine::convertArtSet(JNIEnv *env, jobject artSet) {
	AIArtSet set = NULL;
	if (!sAIArtSet->NewArtSet(&set)) {
		// use a for loop with size instead of hasNext, because that saves us many calls...
		jint length = callIntMethod(env, artSet, mid_Collection_size);
		jobject iterator = callObjectMethod(env, artSet, mid_Collection_iterator);
		for (int i = 0; i < length; i++) {
			jobject obj = callObjectMethod(env, iterator, mid_Iterator_next);
			if (obj != NULL)
				sAIArtSet->AddArtToArtSet(set, getArtHandle(env, obj));
		}
	}
	EXCEPTION_CHECK(env)
	return set;
}
// java.util.Map <-> AIDictionary
jobject ScriptographerEngine::convertDictionary(JNIEnv *env, AIDictionaryRef dictionary, jobject map, bool onlyNew) {
	JNI_CHECK_ENV
	AIDictionaryIterator iterator = NULL;
	Exception *exc = NULL;
	try {
		if (map == NULL)
			map = newObject(env, cls_HashMap, cid_HashMap);
		// walk through the dictionary and see what we can add:
		if (!sAIDictionary->Begin(dictionary, &iterator)) {
			while (!sAIDictionaryIterator->AtEnd(iterator)) {
				AIDictKey key = sAIDictionaryIterator->GetKey(iterator);
				const char *name = sAIDictionary->GetKeyString(key);
				jobject nameObj = createJString(env, name); // consider newStringUTF!
				// if onlyNew is set, check first wether that key already exists in the map and if so, skip it:
				if (onlyNew) {
					jobject obj = callObjectMethod(env, map, mid_Map_get, nameObj, obj);
					if (obj != NULL) { // skip this entry
						sAIDictionaryIterator->Next(iterator);
						continue;
					}
				}
				AIEntryRef entry = sAIDictionary->Get(dictionary, key);
				jobject obj = NULL;
				if (entry != NULL) {
					try {
						AIEntryType type = sAIEntry->GetType(entry);
						switch (type) {
							/*
							TODO: implement these:
							UnknownType,
							// array
							ArrayType,
							// Binary data. if the data is stored to file it is the clients responsibility to
								deal with the endianess of the data.
							BinaryType,
							// a reference to a pattern
							PatternRefType,
							// a reference to a brush pattern
							BrushPatternRefType,
							// a reference to a custom color (either spot or global process)
							CustomColorRefType,
							// a reference to a gradient
							GradientRefType,
							// a reference to a plugin global object
							PluginObjectRefType,
							// an unique id
							UIDType,
							// an unique id reference
							UIDREFType,
							// an XML node
							XMLNodeType,
							// a SVG filter
							SVGFilterType,
							// an art style
							ArtStyleType,
							// a symbol definition reference
							SymbolPatternRefType,
							// a graph design reference
							GraphDesignRefType,
							// a blend style (transpareny attributes)
							BlendStyleType,
							// a graphical object
							GraphicObjectType
							*/
							case IntegerType: {
								ASInt32 value;
								if (!sAIEntry->ToInteger(entry, &value)) {
									obj = newObject(env, cls_Integer, cid_Integer, (jint) value);
								}
							} break;
							case BooleanType: {
								ASBoolean value;
								if (!sAIEntry->ToBoolean(entry, &value)) {
									obj = newObject(env, cls_Boolean, cid_Boolean, (jboolean) value);
								}
							} break;
							case RealType: {
								ASReal value;
								if (!sAIEntry->ToReal(entry, &value)) {
									obj = newObject(env, cls_Float, cid_Float, (jfloat) value);
								}
							} break;
							case StringType: {
								const char *value;
								if (!sAIEntry->ToString(entry, &value)) {
									obj = createJString(env, value);
								}
							} break;
							case DictType: {
								AIDictionaryRef dict;
								if (!sAIEntry->ToDict(entry, &dict)) {
									obj = convertDictionary(env, dict);
								}
							} break;
							case PointType: {
								AIRealPoint point;
								if (!sAIEntry->ToRealPoint(entry, &point)) {
									obj = convertPoint(env, &point);
								}
							} break;
							case MatrixType: {
								AIRealMatrix matrix;
								if (!sAIEntry->ToRealMatrix(entry, &matrix)) {
									obj = convertMatrix(env, &matrix);
								}
							} break;
							case FillStyleType: {
								AIFillStyle fill;
								if (!sAIEntry->ToFillStyle(entry, &fill)) {
									jobject color = convertColor(env, &fill.color);
									obj = newObject(env, cls_FillStyle, cid_FillStyle, color, fill.overprint);
								}
							}
							break;
							case StrokeStyleType: {
								AIStrokeStyle stroke;
								if (!sAIEntry->ToStrokeStyle(entry, &stroke)) {
									jobject color = convertColor(env, &stroke.color);
									int count = stroke.dash.length;
									jfloatArray dashArray = env->NewFloatArray(count);
									env->SetFloatArrayRegion(dashArray, 0, count, stroke.dash.array);
									obj = newObject(env, cls_StrokeStyle, cid_StrokeStyle, color, stroke.overprint, stroke.width, stroke.dash.offset, dashArray, stroke.cap, stroke.join, stroke.miterLimit);
								}
							}
						}
					} catch(Exception *e) {
						exc = e;
					}
					sAIEntry->Release(entry);
					if (exc != NULL)
						throw exc;
				}
				if (obj != NULL) {
					callObjectMethod(env, map, mid_Map_put, nameObj, obj);
				}
				sAIDictionaryIterator->Next(iterator);
			}
		}
	} catch(Exception *e) {
		exc = e;
	}
	if (iterator != NULL)
		sAIDictionaryIterator->Release(iterator);
	
	if (exc != NULL)
		throw exc;
	
	return map;
}

AIDictionaryRef ScriptographerEngine::convertDictionary(JNIEnv *env, jobject map, AIDictionaryRef dictionary) {
	// TODO: define!
	// Syncing:  not only needs the map be filled back into res, also already
	// existing fields in res that do not exist in map should be deleted!
	return dictionary;
}

/**
 * Returns the wrapped AIArtHandle of an object by assuming that it is an anchestor of Class Art and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
AIArtHandle ScriptographerEngine::getArtHandle(JNIEnv *env, jobject obj) {
	JNI_CHECK_ENV
	AIArtHandle art = (AIArtHandle) getIntField(env, obj, fid_AIObject_handle);
	if (art == NULL) throw new StringException("Object is not wrapped around an art handle.");
	return art;
}

/**
 * Returns the wrapped AILayerHandle of an object by assuming that it is an anchestor of Class Art and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
AILayerHandle ScriptographerEngine::getLayerHandle(JNIEnv *env, jobject obj) {
	JNI_CHECK_ENV
	AIArtHandle art = (AIArtHandle) getIntField(env, obj, fid_AIObject_handle);
	if (art == NULL) throw new StringException("Object is not wrapped around an layer handle.");
	AILayerHandle layer;
	sAIArt->GetLayerOfArt(art, &layer);
	return layer;
}

/**
 * Returns the wrapped AIDocumentHandle of an object by assuming that it is an anchestor of Class Document and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
AIDocumentHandle ScriptographerEngine::getDocumentHandle(JNIEnv *env, jobject obj) {
	JNI_CHECK_ENV
	AIDocumentHandle document = (AIDocumentHandle) getIntField(env, obj, fid_AIObject_handle);
	if (document == NULL) throw new StringException("Object is not wrapped around a document handle.");
	return document;
}

/**
 * Returns the wrapped AILiveEffectHandle of an object by assuming that it is an anchestor of Class LiveEffect and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
AILiveEffectHandle ScriptographerEngine::getLiveEffectHandle(JNIEnv *env, jobject obj) {
	JNI_CHECK_ENV
	AILiveEffectHandle effect = (AILiveEffectHandle) getIntField(env, obj, fid_AIObject_handle);
	if (effect == NULL) throw new StringException("Object is not wrapped around a effect handle.");
	return effect;
}

/**
 * Returns the wrapped AIMenuItemHandle of an object by assuming that it is an anchestor of Class LiveEffect and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
AIMenuItemHandle ScriptographerEngine::getMenuItemHandle(JNIEnv *env, jobject obj) {
	JNI_CHECK_ENV
	AIMenuItemHandle item = (AIMenuItemHandle) getIntField(env, obj, fid_AIObject_handle);
	if (item == NULL) throw new StringException("Object is not wrapped around a menu item handle.");
	return item;
}

/**
 * Returns the wrapped AIMenuGroup of an object by assuming that it is an anchestor of Class LiveEffect and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
AIMenuGroup ScriptographerEngine::getMenuGroupHandle(JNIEnv *env, jobject obj) {
	JNI_CHECK_ENV
	AIMenuGroup group = (AIMenuGroup) getIntField(env, obj, fid_AIObject_handle);
	if (group == NULL) throw new StringException("Object is not wrapped around a menu group handle.");
	return group;
}
/**
 * Wraps the handle in a java object. see the Java function Art.wrapArtHandle to see how 
 * the cashing of already wrapped objects is handled.
 *
 * throws exceptions
 */
jobject ScriptographerEngine::wrapArtHandle(JNIEnv *env, AIArtHandle art) {
	JNI_CHECK_ENV
	short type = -1;
	ASBoolean isLayer;
	if (sAIArt->GetArtType(art, &type) || sAIArt->IsArtLayerGroup(art, &isLayer)) throw new StringException("Cannot determine the art object's type");
	// self defined type for layer groups:
	if (isLayer) type = com_scriptographer_ai_Art_TYPE_LAYER;
	return callStaticObjectMethod(env, cls_Art, mid_Art_wrapHandle, (jint) art, (jint) type);
}

jobject ScriptographerEngine::wrapLayerHandle(JNIEnv *env, AILayerHandle layer) {
	// layer handles are not used in java as Layer is derived from Art. Allways use the first invisible
	// Art group in the layer that contains everything (even in AI, layer seems only be a wrapper around
	// an art group:
	AIArtHandle art;
	sAIArt->GetFirstArtOfLayer(layer, &art);
	return callStaticObjectMethod(env, cls_Art, mid_Art_wrapHandle, (jint) art, (jint) com_scriptographer_ai_Art_TYPE_LAYER);
}

/**
 * Wraps the handle in a java object. see the Java function MenuItem.wrapItemHandle to see how 
 * the cashing of already wrapped objects is handled.
 *
 * throws exceptions
 */
jobject ScriptographerEngine::wrapMenuItemHandle(JNIEnv *env, AIMenuItemHandle item) {
	JNI_CHECK_ENV
	char *name, *groupName;
	char text[256];
	AIMenuGroup group;
	if (!sAIMenu->GetMenuItemName(item, &name) &&
		!sAIMenu->GetItemText(item, text) &&
		!sAIMenu->GetItemMenuGroup(item, &group) &&
		!sAIMenu->GetMenuGroupName(group, &groupName)) {
		return callStaticObjectMethod(env, cls_MenuItem, mid_MenuItem_wrapItemHandle,
			(jint) item, createJString(env, name), createJString(env, text),
			(jint) group, createJString(env, groupName)
		);
	}
	return NULL;
}

/**
 * selectionChanged is fired in the following situations:
 * when either a change in the selected art objects occurs or an artwork modification
 * such as moving a point on a path occurs. In other words EITHER something was selected
 * or deselected or targeted or untargeted, OR some aspect of the current selected
 * object(s) changed. 
 * It calls Art.onSelectionChanged with an array containing all the affected artHandles,
 * which then increases the version variable of already wrapped objects
 */
ASErr ScriptographerEngine::selectionChanged() {
	JNIEnv *env = getEnv();
	try {
		AIArtHandle **matches;
		long i, numMatches;
		ASErr error = sAIMatchingArt->GetSelectedArt(&matches, &numMatches);
		if (error) return error;
		if (numMatches == 0) return kNoErr;

		jintArray artHandles = env->NewIntArray(numMatches);
		env->SetIntArrayRegion(artHandles, 0, numMatches, (jint *) *matches);
		callStaticVoidMethod(env, cls_Art, mid_Art_onSelectionChanged, artHandles);

		sAIMDMemory->MdMemoryDisposeHandle((void **) matches);

		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env)
	return kExceptionErr;
}

/**
 * AI Tool
 *
 */

ASErr ScriptographerEngine::toolEditOptions(AIToolMessage *message) {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_Tool, mid_Tool_onEditOptions, (jint) message->tool);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env)
	return kExceptionErr;
}

ASErr ScriptographerEngine::toolTrackCursor(AIToolMessage *message) {
	return kNoErr;
}

ASErr ScriptographerEngine::toolSelect(AIToolMessage *message) {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_Tool, mid_Tool_onSelect, (jint) message->tool);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env)
	return kExceptionErr;
}

ASErr ScriptographerEngine::toolDeselect(AIToolMessage *message) {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_Tool, mid_Tool_onDeselect, (jint) message->tool);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env)
	return kExceptionErr;
}

ASErr ScriptographerEngine::toolReselect(AIToolMessage *message) {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_Tool, mid_Tool_onReselect, (jint) message->tool);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env)
	return kExceptionErr;
}

ASErr ScriptographerEngine::toolMouseDrag(AIToolMessage *message) {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_Tool, mid_Tool_onMouseDrag, (jint) message->tool, (jfloat) message->cursor.h, (jfloat) message->cursor.v, (jint) message->pressure);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env)
	return kExceptionErr;
}

ASErr ScriptographerEngine::toolMouseDown(AIToolMessage *message) {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_Tool, mid_Tool_onMouseDown, (jint) message->tool, (jfloat) message->cursor.h, (jfloat) message->cursor.v, (jint) message->pressure);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env)
	return kExceptionErr;
}

ASErr ScriptographerEngine::toolMouseUp(AIToolMessage *message) {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_Tool, mid_Tool_onMouseUp, (jint) message->tool, (jfloat) message->cursor.h, (jfloat) message->cursor.v, (jint) message->pressure);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env)
	return kExceptionErr;
}

/**
 * AI LiveEffect
 *
 */

jobject ScriptographerEngine::getLiveEffectParameters(JNIEnv *env, AILiveEffectParameters parameters) {
	// Create a java.util.Map with the needed fields from the parameters list.
	// Add the result list under the key "java.util.Map" in the dictionary so it does not need to be created each time
	// and can also be used to store the effect's parameters
	
	// see wether there is already a map:
	AIDictKey key = sAIDictionary->Key("java.util.Map");
	jobject map = NULL;
	long size;
	sAIDictionary->GetBinaryEntry(parameters, key, &map, &size);
	// if it doesn't exist, create it:
	if (map == NULL) {
		map = convertDictionary(env, parameters);
		// TODO: shouldn't map be protected by a GlobalRef? But how to remove it again then???
		sAIDictionary->SetBinaryEntry(parameters, key, &map, sizeof(jobject));
	} else {
		// else add possible new entry to it:
		convertDictionary(env, parameters, map, true);
	}
	return map;
}

AILiveEffectParamContext ScriptographerEngine::getLiveEffectContext(JNIEnv *env, jobject parameters) {
	// gets the value for key "context" from the map and converts it to a AILiveEffectParamContext
	jobject contextObj = callObjectMethod(env, parameters, mid_Map_get, env->NewStringUTF("context"));
	if (contextObj != NULL && env->IsInstanceOf(contextObj, cls_Handle)) {
		return (AILiveEffectParamContext) getIntField(env, contextObj, fid_Handle_handle);
	}
	return NULL;
}

ASErr ScriptographerEngine::liveEffectEditParameters(AILiveEffectEditParamMessage *message) {
	JNIEnv *env = getEnv();
	try {
		jobject map = getLiveEffectParameters(env, message->parameters);
		callStaticVoidMethod(env, cls_LiveEffect, mid_LiveEffect_onEditParameters, (jint) message->effect, map, (jint) message->context, (jboolean) message->allowPreview);
		sAILiveEffect->UpdateParameters(message->context);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env)
	return kExceptionErr;
}

ASErr ScriptographerEngine::liveEffectCalculate(AILiveEffectGoMessage *message) {
	JNIEnv *env = getEnv();
	try {
		jobject map = getLiveEffectParameters(env, message->parameters);
		// TODO: setting art to something else seems to crash!
		message->art = (AIArtHandle) callStaticIntMethod(env, cls_LiveEffect, mid_LiveEffect_onCalculate, (jint) message->effect, map, wrapArtHandle(env, message->art));
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env)
	return kExceptionErr;
}

ASErr ScriptographerEngine::liveEffectInterpolate(AILiveEffectInterpParamMessage *message) {
	// TODO: define
	return kNoErr;
}

ASErr ScriptographerEngine::liveEffectGetInputType(AILiveEffectInputTypeMessage *message) {
	JNIEnv *env = getEnv();
	try {
		jobject map = getLiveEffectParameters(env, message->parameters);
		message->typeMask = callStaticIntMethod(env, cls_LiveEffect, mid_LiveEffect_onGetInputType, (jint) message->effect, map, wrapArtHandle(env, message->inputArt));
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env)
	return kExceptionErr;
}

	
/**
 * AI MenuItem
 *
 */

ASErr ScriptographerEngine::menuItemExecute(AIMenuMessage *message) {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_MenuItem, mid_MenuItem_onClick, (jint) message->menuItem);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env)
	return kExceptionErr;
}

ASErr ScriptographerEngine::menuItemUpdate(AIMenuMessage *message, long inArtwork, long isSelected, long isTrue) {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_MenuItem, mid_MenuItem_onUpdate, (jint) message->menuItem, (jint) inArtwork, (jint) isSelected, (jint) isTrue);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env)
	return kExceptionErr;
}


/**
 * ADM CallbackListener
 *
 */
void ScriptographerEngine::callOnNotify(jobject handler, ADMNotifierRef notifier) {
	char type[64];
	sADMNotifier->GetNotifierType(notifier, type, 64);
	JNIEnv *env = getEnv();
	callVoidMethodReport(env, handler, mid_NotificationHandler_onNotify_String, env->NewStringUTF(type));
}

void ScriptographerEngine::callOnDestroy(jobject handler) {
	callVoidMethodReport(NULL, handler, mid_NotificationHandler_onNotify_int, (jint) com_scriptographer_adm_Notifier_NOTIFIER_DESTROY);
}

/**
 *
 *
 */
bool ScriptographerEngine::callOnTrack(jobject handler, ADMTrackerRef tracker) {
	JNIEnv *env = getEnv();
	try {
		jobject trackerObj = getObjectField(env, handler, fid_NotificationHandler_tracker);
		ADMPoint pt;
		sADMTracker->GetPoint(tracker, &pt);
		return callBooleanMethod(env, trackerObj, mid_Tracker_onTrack, handler, (jint)tracker, (jint)sADMTracker->GetAction(tracker),
			(jint)sADMTracker->GetModifiers(tracker), pt.h, pt.v, (int)sADMTracker->GetMouseState(tracker),
			(jchar)sADMTracker->GetVirtualKey(tracker), (int)sADMTracker->GetCharacter(tracker), (long)sADMTracker->GetTime(tracker));
	} EXCEPTION_CATCH_REPORT(env)
	return true;
}

/**
 *
 *
 */
void ScriptographerEngine::callOnDraw(jobject handler, ADMDrawerRef drawer) {
	JNIEnv *env = getEnv();
	try {
		jobject drawerObj = getObjectField(env, handler, fid_NotificationHandler_drawer);
		setIntField(env, drawerObj, fid_ADMObject_handle, (jint)drawer);
		callVoidMethod(env, handler, mid_NotificationHandler_onDraw, drawerObj);
	} EXCEPTION_CATCH_REPORT(env)
}

ASErr ScriptographerEngine::about() {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_ScriptographerEngine, mid_ScriptographerEngine_onAbout);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env)
	return kExceptionErr;
}

/**
 * Returns the wrapped ADMDialogRef of an object by assuming that it is an anchestor of Class Dialog and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ADMDialogRef ScriptographerEngine::getDialogRef(JNIEnv *env, jobject obj) {
	JNI_CHECK_ENV
	ADMDialogRef dlg = (ADMDialogRef) getIntField(env, obj, fid_ADMObject_handle);
	if (dlg == NULL) throw new StringException("Object is not wrapped around a dialog ref.");
	return dlg;
}

/**
 * Returns the wrapped ADMDrawerRef of an object by assuming that it is an anchestor of Class Drawer and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ADMDrawerRef ScriptographerEngine::getDrawerRef(JNIEnv *env, jobject obj) {
	ADMDrawerRef drawer = (ADMDrawerRef) getIntField(env, obj, fid_ADMObject_handle);
	if (drawer == NULL) throw new StringException("Object is not wrapped around a drawer ref.");
	return drawer;
}

/**
 * Returns the wrapped ADMTrackerRef of an object by assuming that it is an anchestor of Class Tracker and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ADMTrackerRef ScriptographerEngine::getTrackerRef(JNIEnv *env, jobject obj) {
	ADMTrackerRef tracker = (ADMTrackerRef) getIntField(env, obj, fid_ADMObject_handle);
	if (tracker == NULL) throw new StringException("Object is not wrapped around a tracker ref.");
	return tracker;
}

/**
 * Returns the wrapped ADMImageRef of an object by assuming that it is an anchestor of Class Image and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ADMImageRef ScriptographerEngine::getImageRef(JNIEnv *env, jobject obj) {
	ADMImageRef image = (ADMImageRef) getIntField(env, obj, fid_ADMObject_handle);
	if (image == NULL) throw new StringException("Object is not wrapped around a image ref.");
	return image;
}

/**
 * Returns the wrapped ADMItemRef of an object by assuming that it is an anchestor of Class Item and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ADMItemRef ScriptographerEngine::getItemRef(JNIEnv *env, jobject obj) {
	JNI_CHECK_ENV
	ADMItemRef item = (ADMItemRef) getIntField(env, obj, fid_ADMObject_handle);
	if (item == NULL) {
		// for HierarchyLists it could be that the user wants to call item functions
		// on a child list. report that this can only be called on the root list:
		if (env->IsInstanceOf(obj, cls_HierarchyList)) {
			throw new StringException("This function can only be called on the root hierarchy list.");
		} else {
			throw new StringException("Object is not wrapped around an item ref.");
		}
	}
	return item;
}

/**
 * Returns the wrapped ADMListRef of an object by assuming that it is an anchestor of Class ListItem and
 * accessing its field 'listHandle':
 *
 * throws exceptions
 */
ADMListRef ScriptographerEngine::getListRef(JNIEnv *env, jobject obj) {
	JNI_CHECK_ENV
	ADMListRef list = (ADMListRef) getIntField(env, obj, fid_ListItem_listHandle);
	if (list == NULL) throw new StringException("Object is not wrapped around a list ref.");
	return list;
}

/**
 * Returns the wrapped ADMHierarchyListRef of an object by assuming that it is an anchestor of Class ListItem and
 * accessing its field 'listHandle':
 *
 * throws exceptions
 */
ADMHierarchyListRef ScriptographerEngine::getHierarchyListRef(JNIEnv *env, jobject obj) {
	JNI_CHECK_ENV
	ADMHierarchyListRef list = (ADMHierarchyListRef) getIntField(env, obj, fid_ListItem_listHandle);
	if (list == NULL) throw new StringException("Object is not wrapped around a hierarchy list ref.");
	return list;
}

/**
 * Returns the wrapped ADMEntryRef of an object by assuming that it is an anchestor of Class Entry and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ADMEntryRef ScriptographerEngine::getListEntryRef(JNIEnv *env, jobject obj) {
	JNI_CHECK_ENV
	ADMEntryRef entry = (ADMEntryRef) getIntField(env, obj, fid_ADMObject_handle);
	if (entry == NULL) throw new StringException("Object is not wrapped around a list entry ref.");
	return entry;
}

/**
 * Returns the wrapped ADMListEntryRef of an object by assuming that it is an anchestor of Class Entry and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ADMListEntryRef ScriptographerEngine::getHierarchyListEntryRef(JNIEnv *env, jobject obj) {
	JNI_CHECK_ENV
	ADMListEntryRef entry = (ADMListEntryRef) getIntField(env, obj, fid_ADMObject_handle);
	if (entry == NULL) throw new StringException("Object is not wrapped around a hierarchy list entry ref.");
	return entry;
}

jobject ScriptographerEngine::getDialogObject(ADMDialogRef dlg) {
	jobject obj = NULL;
	if (sADMDialog != NULL) {
		obj = (jobject) sADMDialog->GetUserData(dlg);
		if (obj == NULL) throw new StringException("Dialog does not have a wrapper object.");
	}
	return obj;
}

jobject ScriptographerEngine::getItemObject(ADMItemRef item) {
	jobject obj = NULL;
	if (sADMItem != NULL) {
		obj = (jobject) sADMItem->GetUserData(item);
		if (obj == NULL) throw new StringException("Item does not have a wrapper object.");
	}
	return obj;
}

/*
 * The list related functions wrap the lists in Java objects if not done already!
 */

jobject ScriptographerEngine::getListObject(ADMListRef list) {
	jobject obj = NULL;
	if (sADMList != NULL) {
		obj = (jobject) sADMList->GetUserData(list);
		if (obj == NULL) throw new StringException("List does not have a wrapper object.");
	}
	return obj;
}

jobject ScriptographerEngine::getListObject(ADMHierarchyListRef list) {
	jobject obj = NULL;
	if (sADMHierarchyList != NULL) {
		obj = (jobject) sADMHierarchyList->GetUserData(list);
		if (obj == NULL) throw new StringException("Hierarchy list does not have a wrapper object.");
	}
	return obj;
}

jobject ScriptographerEngine::getListEntryObject(ADMEntryRef entry) {
	jobject obj = NULL;
	if (sADMEntry != NULL) {
		obj = (jobject) sADMEntry->GetUserData(entry);
		if (obj == NULL) throw new StringException("Entry does not have a wrapper object.");
	}
	return obj;
}

jobject ScriptographerEngine::getListEntryObject(ADMListEntryRef entry) {
	jobject obj = NULL;
	if (sADMListEntry != NULL) {
		obj = (jobject) sADMListEntry->GetUserData(entry);
		if (obj == NULL) throw new StringException("Hierarchy entry does not have a wrapper object.");
	}
	return obj;
}

/*
 *
 * JNI
 *
 */

JNIEnv *ScriptographerEngine::getEnv() {
	JNIEnv *env;
	fJavaVM->AttachCurrentThread((void **)&env, NULL);
	return env;
}

/**
 * Creates a Java String from a given C-String.
 * TODO: The non depreceated version that takes an encoding parameter should be used in the future.
 *
 * throws exceptions
 */
jstring ScriptographerEngine::createJString(JNIEnv *env, const char *str) {
	JNI_CHECK_ENV
	int len = strlen(str);
	jbyteArray bytes = env->NewByteArray(len);
	if (bytes == NULL) throw new JThrowableClassException(cls_OutOfMemoryError);
	env->SetByteArrayRegion(bytes, 0, len, (jbyte *)str);
	jstring result = (jstring)env->functions->NewObject(env, cls_String, cid_String, bytes);
	env->DeleteLocalRef(bytes);
	return result;
}

/**
 * Creates a C-String from a given Java String. 
 * TODO: The non depreceated version that takes an encoding parameter should be used in the future.
 *
 * throws exceptions
 */
char *ScriptographerEngine::createCString(JNIEnv *env, jstring jstr) {
	JNI_CHECK_ENV
	jbyteArray bytes = (jbyteArray)callObjectMethod(env, jstr, mid_String_getBytes);
	jint len = env->GetArrayLength(bytes);
	char *result = new char[len + 1];
	if (result == NULL) {
		env->DeleteLocalRef(bytes);
		throw new JThrowableClassException(cls_OutOfMemoryError);
	}
	env->GetByteArrayRegion(bytes, 0, len, (jbyte *)result);
	result[len] = 0; // NULL-terminate
	env->DeleteLocalRef(bytes);
	return result;
}

/**
 * Throws an exception with the given name (like "com/scriptographer/ScriptographerException") and message.
 *
 * throws exceptions
 */
void ScriptographerEngine::throwException(JNIEnv *env, const char* name, const char* msg) {
	JNI_CHECK_ENV
	jclass cls = findClass(env, name);
	// if cls is NULL, an exception has already been thrown
	if (cls != NULL) {
		env->ThrowNew(cls, msg);
		env->DeleteLocalRef(cls); 
	}
}

/**
 * Throws a ScriptographerException with the given message.
 *
 * throws exceptions
 */
void ScriptographerEngine::throwException(JNIEnv *env, const char* msg) {
	JNI_CHECK_ENV
	env->ThrowNew(cls_ScriptographerException, msg);
}

/**
 * Uses the Scriptographer loader instead of the default JNI one, because otherwise
 * classes for reflection and registering of natives won't be found! The JNI loader
 * can only be used for loading the Loader, because that's the only thing initially
 * on the classpath.
 */
jclass ScriptographerEngine::findClass(JNIEnv *env, const char *name) {
	JNI_CHECK_ENV

	// Loading with JNI would be like this:
	// jclass cls = env->FindClass(name);
	
	jclass cls = (jclass)callStaticObjectMethod(env, cls_Loader, mid_Loader_loadClass, env->NewStringUTF(name));
	if (cls == NULL) EXCEPTION_CHECK(env)
	return cls;
}

jclass ScriptographerEngine::loadClass(JNIEnv *env, const char *name) {
	JNI_CHECK_ENV
	jclass cls = findClass(env, name);
	jclass res = (jclass)env->NewGlobalRef(cls);
	env->DeleteLocalRef(cls);
	return res;
}

jmethodID ScriptographerEngine::getMethodID(JNIEnv *env, jclass cls, const char *name, const char *signature) {
	JNI_CHECK_ENV
	jmethodID res = env->GetMethodID(cls, name, signature);
	if (res == NULL) EXCEPTION_CHECK(env)
	return res;
}

jmethodID ScriptographerEngine::getStaticMethodID(JNIEnv *env, jclass cls, const char *name, const char *signature) {
	JNI_CHECK_ENV
	jmethodID res = env->GetStaticMethodID(cls, name, signature);
	if (res == NULL) EXCEPTION_CHECK(env)
	return res;
}

jmethodID ScriptographerEngine::getConstructorID(JNIEnv *env, jclass cls, const char *signature) {
	return getMethodID(env, cls, "<init>", signature);
}

jfieldID ScriptographerEngine::getFieldID(JNIEnv *env, jclass cls, const char *name, const char *signature) {
	JNI_CHECK_ENV
	jfieldID res = env->GetFieldID(cls, name, signature);
	if (res == NULL) EXCEPTION_CHECK(env)
	return res;
}

jfieldID ScriptographerEngine::getStaticFieldID(JNIEnv *env, jclass cls, const char *name, const char *signature) {
	JNI_CHECK_ENV
	jfieldID res = env->GetStaticFieldID(cls, name, signature);
	if (res == NULL) EXCEPTION_CHECK(env)
	return res;
}

jobject ScriptographerEngine::newObject(JNIEnv *env, jclass cls, jmethodID ctor, ...) {
	JNI_CHECK_ENV
	JNI_ARGS_BEGIN(ctor)
	jobject res = env->functions->NewObjectV(env, cls, ctor, args);
	JNI_ARGS_END
	if (res == NULL) EXCEPTION_CHECK(env)
	return res;
}

// declare macro functions now:

JNI_DEFINE_GETFIELD_FUNCTIONS
JNI_DEFINE_SETFIELD_FUNCTIONS
JNI_DEFINE_GETSTATICFIELD_FUNCTIONS
JNI_DEFINE_CALLMETHOD_FUNCTIONS