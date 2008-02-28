/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id$
 */

#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "ScriptographerPlugin.h"
#include "com_scriptographer_ai_Art.h" // for com_scriptographer_ai_Art_TYPE_LAYER
#include "com_scriptographer_adm_Notifier.h"

#ifdef WIN_ENV
#include "loadJava.h"
#endif

#ifdef _CLASSPATH_JNI_H
#define GCJ 1
#endif

#if defined(MAC_ENV) && !defined(GCJ)
#define MAC_THREAD
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

#ifdef MAC_THREAD

OSStatus javaThread(ScriptographerEngine *engine) {
	engine->javaThread();
	return noErr;
}

/**
 * returns the JavaVM to be destroyed after execution.
 */
void ScriptographerEngine::javaThread() {
	try {
		init();
		// Tell the constructor that the initialization is done:
		MPNotifyQueue(m_responseQueue, NULL, NULL, NULL);
	} catch (ScriptographerException *e) {
		// Let the user know about this error:
		MPNotifyQueue(m_responseQueue, e, NULL, NULL);
	}
	// Keep this thread alive until the JVM is to be destroyed. This needs
	// to happen from the creation thread as well, otherwise JNI hangs endlessly:
	MPWaitOnQueue(m_requestQueue, NULL, NULL, NULL, kDurationForever);
	// Now tell the caller that the engine can be deleted, before DestroyJavaVM is called,
	// which may block the current thread until the end of the app.
	MPNotifyQueue(m_responseQueue, NULL, NULL, NULL);
	// Now exit, and destroy the JavaVM. 
	exit();
}

#endif // MAC_THREAD

ScriptographerEngine::ScriptographerEngine(const char *homeDir) {
	m_initialized = false;
	m_javaVM = NULL;
	m_homeDir = new char[strlen(homeDir) + 1];
	strcpy(m_homeDir, homeDir);
	ScriptographerException *exc = NULL;

#ifdef MAC_THREAD
	if(MPLibraryIsLoaded()) {
		MPCreateQueue(&m_requestQueue);
		MPCreateQueue(&m_responseQueue);
		MPCreateTask((TaskProc)::javaThread, this, 0, NULL, NULL, NULL, 0, NULL); 
		// Now wait for the javaThread to finish initialization
		// exceptions that happen in the javaThread are passed through to this thread in order to display the error code:
		MPWaitOnQueue(m_responseQueue, (void **) &exc, NULL, NULL, kDurationForever);
	} else 
#endif // MAC_THREAD
	{	// On windows, we can directly call the initialize function:
		try {
			init();
		} catch (ScriptographerException *e) {
			exc = e;
		}
	}
	if (exc != NULL) {
		JNIEnv *env = getEnv();
		gPlugin->log("Cannot create ScriptographerEngine: %s", exc->toString(env));
		exc->report(env);
		delete exc;
		throw new StringException("Cannot create ScriptographerEngine.");
	}
	gEngine = this;
}

ScriptographerEngine::~ScriptographerEngine() {
	gEngine = NULL;
	delete m_homeDir;
	callStaticVoidMethodReport(NULL, cls_ScriptographerEngine, mid_ScriptographerEngine_destroy);
#ifdef MAC_THREAD
	if(MPLibraryIsLoaded()) {
		// notify the JVM thread to end, then clean up:
		MPNotifyQueue(m_requestQueue, NULL, NULL, NULL);
		// now wait for the javaThread to finish before destroying the engine
		MPWaitOnQueue(m_responseQueue, NULL, NULL, NULL, kDurationForever);
		// clean up...
		MPDeleteQueue(m_requestQueue);
		MPDeleteQueue(m_responseQueue);
	} else 
#endif // MAC_THREAD
	{ // Clean up:
//		exit();
	}
}

class JVMOptions {
	// Make sure we have plenty...
	JavaVMOption fOptions[128];
	int fNumOptions;
	
public:
	JVMOptions() {
		fNumOptions = 0;
		memset(fOptions, 0, sizeof(fOptions));
	}
	
	~JVMOptions() {
		for (int i = 0; i < fNumOptions; i++) {
			delete[] fOptions[i].optionString;
		}
	}
	
	void add(const char *str, ...) {
		char *text = new char[2048];
		va_list args;
		va_start(args, str);
		vsprintf(text, str, args);
		va_end(args);
		fOptions[fNumOptions++].optionString = text;
		if (strlen(text) > 0)
			gPlugin->log("JVM Option: %s", text);
	}
	
	void fillArgs(JavaVMInitArgs *args) {
		args->options = fOptions;
		args->nOptions = fNumOptions;
	}
};

void ScriptographerEngine::init() {
	// The VM invocation functions need to be loaded dynamically on Windows,
	// On Mac, the static ones can be used without problems:
    CreateJavaVMProc createJavaVM = NULL;
    GetDefaultJavaVMInitArgsProc getDefaultJavaVMInitArgs = NULL;
#ifdef WIN_ENV
	loadJavaVM("client", &createJavaVM, &getDefaultJavaVMInitArgs);
#else // !WIN_ENV
	createJavaVM = JNI_CreateJavaVM;
	getDefaultJavaVMInitArgs = JNI_GetDefaultJavaVMInitArgs;
#endif // !WIN_ENV

	// Initialize args
	JavaVMInitArgs args;
	args.version = JNI_VERSION_1_4;
	if (args.version < JNI_VERSION_1_4)
		getDefaultJavaVMInitArgs(&args);

	// Define options
	JVMOptions options;
#ifndef GCJ
	// Only add the loader to the classpath, the rest is done in java:
	options.add("-Djava.class.path=%s" PATH_SEP_STR "loader.jar", m_homeDir);
#endif // GCJ
	options.add("-Djava.library.path=%s" PATH_SEP_STR "lib", m_homeDir);

#ifdef MAC_ENV
#ifdef MAC_THREAD
	// Start headless, in order to avoid conflicts with AWT and Illustrator
	options.add("-Djava.awt.headless=true");
#else // !MAC_THREAD
	options.add("-Dapple.awt.usingSWT=true");
#endif // !MAC_THREAD
	// Use the carbon line separator instead of the unix one on mac:
	options.add("-Dline.separator=\r");
#endif // MAC_ENV
	// Read ini file and add the options here
	char buffer[512];
	sprintf(buffer, "%s" PATH_SEP_STR "jvm.ini", m_homeDir);
	FILE *file = fopen(buffer, "rt");
	if (file != NULL) {
		while (fgets(buffer, sizeof(buffer), file)) {
			char *str = buffer;
			// Trim new line and white space at beginning and the end of the line:
			while (isspace(*str) && str != '\0') str++;
			if (*str == '#') continue; // skip comments
			int pos = strlen(str) - 1;
			while (pos >= 0 && isspace(str[pos])) str[pos--] = '\0';
			options.add(str);
		}
	}

#if defined(_DEBUG) && !defined(GCJ)
	// Start JVM in debug mode, for remote debuggin on port 8000
	options.add("-Xdebug");
	options.add("-Xnoagent");
	options.add("-Djava.compiler=NONE");
	options.add("-Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n");
#endif // _DEBUG && !GCJ

	options.fillArgs(&args);
	args.ignoreUnrecognized = true;
	JNIEnv *env = NULL;
	// Create the JVM
	jint res = createJavaVM(&m_javaVM, (void **) &env, (void *) &args);
	if (res < 0) {
		gPlugin->log("Error creataing Java VM: %i", res);
		throw new StringException("Cannot create Java VM.");
	}
#ifndef GCJ
	cls_Loader = env->FindClass("com/scriptographer/loader/Loader");
	if (cls_Loader == NULL)
		throw new StringException("Cannot load loader.jar. Please make sure that the java folder was copied together with the Scriptographer plugin.");
	mid_Loader_init = getStaticMethodID(env, cls_Loader, "init", "(Ljava/lang/String;)V");
	mid_Loader_reload = getStaticMethodID(env, cls_Loader, "reload", "()Ljava/lang/String;");
	mid_Loader_loadClass = getStaticMethodID(env, cls_Loader, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
	callStaticObjectMethodReport(env, cls_Loader, mid_Loader_init, env->NewStringUTF(m_homeDir));
#else // !GCJ
	cls_Loader = NULL;
#endif // !GCJ
	
	// Initialize reflection. This retrieves references to all the classes, fields and methods
	// that are accessed from native code. Since JSE 1.6, this cannot be called after 
	// registerNatives, since this would somehow clear the internal native link tables.
	// Also, registerNatives needs yet another workaround to get 1.6 to work: By calling
	// a reflection method, e.g. getConstructors, the classes are pulled in fully,
	// avoiding yet another clearing of the link tables at a later point.
	// (e.g. for classes that are not loaded in initReflection)
	initReflection(env);
	// Link the native functions to the java functions. The code for this is in registerNatives.cpp,
	// which is automatically generated from the JNI header files by jni.js
	registerNatives(env);
}

void ScriptographerEngine::exit() {
	m_javaVM->DetachCurrentThread();
	m_javaVM->DestroyJavaVM();
}

/**
 * Initializes the engine, e.g. activates console redirection and similar things. To be called when
 * all the underlying native structures are initiialized.
 */
void ScriptographerEngine::initEngine() {
	JNIEnv *env = getEnv();
	try {
		// create the art handle key in which the art object's original handle value is stored
		// as this value is used to lookup wrappers on the java side, this needs to be passed
		// in selectionChanged, in case the art's handle was changed due to manipulations
		// the java wrappers can then be updated accordingly
		
		// According Adobe: note that entries whose keys are prefixed with
		// the character '-' are considered to be temporary entries which are not saved to file.
		m_artHandleKey = sAIDictionary->Key("-scriptographer-art-handle");
		m_docReflowKey = sAIDictionary->Key("-scriptographer-doc-reflow-suspended");
		
		callStaticVoidMethod(env, cls_ScriptographerEngine, mid_ScriptographerEngine_init, env->NewStringUTF(m_homeDir));
		m_initialized = true;
	} EXCEPTION_CATCH_REPORT(env);
}

/**
 * Reloads the engine through Loader.reload, which does the half of the work. The other half is to
 * remap the reflections and register the natives again.
 */
jstring ScriptographerEngine::reloadEngine() {
	JNIEnv *env = getEnv();
	jstring errors = (jstring) callStaticObjectMethodReport(env, cls_Loader, mid_Loader_reload);
	m_initialized = false;
	initReflection(env);
	registerNatives(env);
	initEngine();
	return errors;
}

/**
 * Loads the Java classes, and the method and field descriptors required for Java reflection.
 * Returns true on success, false on failure.
 */
void ScriptographerEngine::initReflection(JNIEnv *env) {
// JSE:
	cls_Object = loadClass(env, "java/lang/Object");
	mid_Object_toString = getMethodID(env, cls_Object, "toString", "()Ljava/lang/String;");

	cls_System = loadClass(env, "java/lang/System");
	fid_System_out = getStaticFieldID(env, cls_System, "out", "Ljava/io/PrintStream;");

	cls_PrintStream = loadClass(env, "java/io/PrintStream");
	mid_PrintStream_println = getMethodID(env, cls_PrintStream, "println", "(Ljava/lang/String;)V");

	cls_Class = loadClass(env, "java/lang/Class");
	mid_Class_getName = getMethodID(env, cls_Class, "getName", "()Ljava/lang/String;");
	mid_Class_getConstructors = getMethodID(env, cls_Class, "getConstructors", "()[Ljava/lang/reflect/Constructor;");

	cls_String = loadClass(env, "java/lang/String");
	cid_String = getConstructorID(env, cls_String, "([B)V");
	mid_String_getBytes = getMethodID(env, cls_String, "getBytes", "()[B");

	cls_Number = loadClass(env, "java/lang/Number");
	mid_Number_intValue = getMethodID(env, cls_Number, "intValue", "()I");
	mid_Number_floatValue = getMethodID(env, cls_Number, "floatValue", "()F");

	cls_Integer = loadClass(env, "java/lang/Integer");
	cid_Integer = getConstructorID(env, cls_Integer, "(I)V");

	cls_Float = loadClass(env, "java/lang/Float");
	cid_Float = getConstructorID(env, cls_Float, "(F)V");

	cls_Boolean = loadClass(env, "java/lang/Boolean");
	cid_Boolean = getConstructorID(env, cls_Boolean, "(Z)V");
	mid_Boolean_booleanValue = getMethodID(env, cls_Boolean, "booleanValue", "()Z");
	
	cls_File = loadClass(env, "java/io/File");
	cid_File = getConstructorID(env, cls_File, "(Ljava/lang/String;)V");
	mid_File_getPath = getMethodID(env, cls_File, "getPath", "()Ljava/lang/String;");

	cls_Collection = loadClass(env, "java/util/Collection");
	mid_Collection_add = getMethodID(env, cls_Collection, "add", "(Ljava/lang/Object;)Z");
	
	cls_Map = loadClass(env, "java/util/Map");
	mid_Map_keySet = getMethodID(env, cls_Map, "keySet", "()Ljava/util/Set;");
	mid_Map_put = getMethodID(env, cls_Map, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
	mid_Map_get = getMethodID(env, cls_Map, "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
	
	cls_HashMap = loadClass(env, "java/util/HashMap");
	cid_HashMap = getConstructorID(env, cls_HashMap, "()V");
	
	cls_ArrayList = loadClass(env, "java/util/ArrayList");
	cid_ArrayList = getConstructorID(env, cls_ArrayList, "()V");
	
	cls_Set = loadClass(env, "java/util/Set");
	mid_Set_iterator = getMethodID(env, cls_Set, "iterator", "()Ljava/util/Iterator;");

	cls_Iterator = loadClass(env, "java/util/Iterator");
	mid_Iterator_hasNext = getMethodID(env, cls_Iterator, "hasNext", "()Z");
	mid_Iterator_next = getMethodID(env, cls_Iterator, "next", "()Ljava/lang/Object;");
	mid_Iterator_remove = getMethodID(env, cls_Iterator, "remove", "()V");

	cls_OutOfMemoryError = loadClass(env, "java/lang/OutOfMemoryError");

	cls_awt_Color = loadClass(env, "java/awt/Color");
	cid_awt_Color = getConstructorID(env, cls_awt_Color, "(FFF)V");
	mid_awt_Color_getColorComponents = getMethodID(env, cls_awt_Color, "getColorComponents", "([F)[F");

	cls_awt_ICC_Profile = loadClass(env, "java/awt/color/ICC_Profile");
	mid_awt_ICC_Profile_getInstance = getStaticMethodID(env, cls_awt_ICC_Profile, "getInstance", "([B)Ljava/awt/color/ICC_Profile;");

// Scratchdisk:
	cls_List = loadClass(env, "com/scratchdisk/list/List");
	mid_List_size = getMethodID(env, cls_List, "size", "()I");
	mid_List_get = getMethodID(env, cls_List, "get", "(I)Ljava/lang/Object;");

	cls_IntMap = loadClass(env, "com/scratchdisk/util/IntMap");
	cid_IntMap = getConstructorID(env, cls_IntMap, "()V");
	mid_IntMap_put = getMethodID(env, cls_IntMap, "put", "(ILjava/lang/Object;)Ljava/lang/Object;");

// Scriptographer:
	cls_ScriptographerEngine = loadClass(env, "com/scriptographer/ScriptographerEngine");
	mid_ScriptographerEngine_init = getStaticMethodID(env, cls_ScriptographerEngine, "init", "(Ljava/lang/String;)V");
	mid_ScriptographerEngine_destroy = getStaticMethodID(env, cls_ScriptographerEngine, "destroy", "()V");
	mid_ScriptographerEngine_reportError = getStaticMethodID(env, cls_ScriptographerEngine, "reportError", "(Ljava/lang/Throwable;)V");
	mid_ScriptographerEngine_onAbout = getStaticMethodID(env, cls_ScriptographerEngine, "onAbout", "()V");

	cls_ScriptographerException = loadClass(env, "com/scriptographer/ScriptographerException");

	cls_CommitManager = loadClass(env, "com/scriptographer/CommitManager");
	mid_CommitManager_commit = getStaticMethodID(env, cls_CommitManager, "commit", "()V");

// AI:
	cls_ai_NativeObject = loadClass(env, "com/scriptographer/ai/NativeObject");
	fid_ai_NativeObject_handle = getFieldID(env, cls_ai_NativeObject, "handle", "I");
	
	cls_ai_NativeWrapper = loadClass(env, "com/scriptographer/ai/NativeWrapper");
	fid_ai_NativeWrapper_document = getFieldID(env, cls_ai_NativeWrapper, "document", "Lcom/scriptographer/ai/Document;");
	
	cls_ai_Tool = loadClass(env, "com/scriptographer/ai/Tool");
	cid_ai_Tool = getConstructorID(env, cls_ai_Tool, "(II)V");
	mid_ai_Tool_onHandleEvent = getStaticMethodID(env, cls_ai_Tool, "onHandleEvent", "(ILjava/lang/String;FFI)I");

	cls_ai_Point = loadClass(env, "com/scriptographer/ai/Point");
	cid_ai_Point = getConstructorID(env, cls_ai_Point, "(FF)V");
	fid_ai_Point_x = getFieldID(env, cls_ai_Point, "x", "F");
	fid_ai_Point_y = getFieldID(env, cls_ai_Point, "y", "F");
	mid_ai_Point_set = getMethodID(env, cls_ai_Point, "set", "(FF)V");

	cls_ai_Rectangle = loadClass(env, "com/scriptographer/ai/Rectangle");
	cid_ai_Rectangle = getConstructorID(env, cls_ai_Rectangle, "(FFFF)V");
	fid_ai_Rectangle_x = getFieldID(env, cls_ai_Rectangle, "x", "F");
	fid_ai_Rectangle_y = getFieldID(env, cls_ai_Rectangle, "y", "F");
	fid_ai_Rectangle_width = getFieldID(env, cls_ai_Rectangle, "width", "F");
	fid_ai_Rectangle_height = getFieldID(env, cls_ai_Rectangle, "height", "F");
	mid_ai_Rectangle_set = getMethodID(env, cls_ai_Rectangle, "set", "(FFFF)V");

	cls_ai_Size = loadClass(env, "com/scriptographer/ai/Size");
	cid_ai_Size = getConstructorID(env, cls_ai_Size, "(FF)V");
	fid_ai_Size_width = getFieldID(env, cls_ai_Size, "width", "F");
	fid_ai_Size_height = getFieldID(env, cls_ai_Size, "height", "F");
	mid_ai_Size_set = getMethodID(env, cls_ai_Size, "set", "(FF)V");
	
	cls_ai_Matrix = loadClass(env, "com/scriptographer/ai/Matrix");
	cid_ai_Matrix = getConstructorID(env, cls_ai_Matrix, "(DDDDDD)V");
	mid_ai_Matrix_getScaleX = getMethodID(env, cls_ai_Matrix, "getScaleX", "()D");
	mid_ai_Matrix_getShearY = getMethodID(env, cls_ai_Matrix, "getShearY", "()D");
	mid_ai_Matrix_getShearX = getMethodID(env, cls_ai_Matrix, "getShearX", "()D");
	mid_ai_Matrix_getScaleY = getMethodID(env, cls_ai_Matrix, "getScaleY", "()D");
	mid_ai_Matrix_getTranslateX = getMethodID(env, cls_ai_Matrix, "getTranslateX", "()D");
	mid_ai_Matrix_getTranslateY = getMethodID(env, cls_ai_Matrix, "getTranslateY", "()D");

	cls_ai_Color = loadClass(env, "com/scriptographer/ai/Color");
	mid_ai_Color_getComponents = getMethodID(env, cls_ai_Color, "getComponents", "()[F");
	obj_ai_Color_NONE = getStaticObjectField(env, cls_ai_Color, "NONE", "Lcom/scriptographer/ai/Color;");

	cls_ai_GrayColor = loadClass(env, "com/scriptographer/ai/GrayColor");
	cid_ai_GrayColor = getConstructorID(env, cls_ai_GrayColor, "(FF)V");

	cls_ai_RGBColor = loadClass(env, "com/scriptographer/ai/RGBColor");
	cid_ai_RGBColor = getConstructorID(env, cls_ai_RGBColor, "(FFFF)V");

	cls_ai_CMYKColor = loadClass(env, "com/scriptographer/ai/CMYKColor");
	cid_ai_CMYKColor = getConstructorID(env, cls_ai_CMYKColor, "(FFFFF)V");
	
	cls_ai_GradientColor = loadClass(env, "com/scriptographer/ai/GradientColor");
	cid_ai_GradientColor = getConstructorID(env, cls_ai_GradientColor, "(ILcom/scriptographer/ai/Point;FFLcom/scriptographer/ai/Matrix;FF)V");
	mid_ai_GradientColor_set = getMethodID(env, cls_ai_GradientColor, "set", "(I)V");

	cls_ai_PatternColor = loadClass(env, "com/scriptographer/ai/PatternColor");
	cid_ai_PatternColor = getConstructorID(env, cls_ai_PatternColor, "(IFFLcom/scriptographer/ai/Point;FZFFFLcom/scriptographer/ai/Matrix;)V");
	mid_ai_PatternColor_set = getMethodID(env, cls_ai_PatternColor, "set", "(I)V");
	
	cls_ai_Art = loadClass(env, "com/scriptographer/ai/Art");
	fid_ai_Art_version = getFieldID(env, cls_ai_Art, "version", "I");
	fid_ai_Art_document = getFieldID(env, cls_ai_Art, "document", "Lcom/scriptographer/ai/Document;");
	fid_ai_Art_dictionaryRef = getFieldID(env, cls_ai_Art, "dictionaryRef", "I");
	mid_ai_Art_wrapHandle = getStaticMethodID(env, cls_ai_Art, "wrapHandle", "(ISIIIZ)Lcom/scriptographer/ai/Art;");
	mid_ai_Art_getIfWrapped = getStaticMethodID(env, cls_ai_Art, "getIfWrapped", "(I)Lcom/scriptographer/ai/Art;");
	mid_ai_Art_updateIfWrapped = getStaticMethodID(env, cls_ai_Art, "updateIfWrapped", "([I)V");
	mid_ai_Art_changeHandle = getMethodID(env, cls_ai_Art, "changeHandle", "(III)V");
	mid_ai_Art_commit = getMethodID(env, cls_ai_Art, "commit", "(Z)V");

	cls_ai_ArtSet = loadClass(env, "com/scriptographer/ai/ArtSet");
	cid_ArtSet = getConstructorID(env, cls_ai_ArtSet, "()V");
	mid_ai_ArtSet_add = getMethodID(env, cls_ai_ArtSet, "add", "(Ljava/lang/Object;)Ljava/lang/Object;");

	cls_ai_Path = loadClass(env, "com/scriptographer/ai/Path");
	cls_ai_CompoundPath = loadClass(env, "com/scriptographer/ai/CompoundPath");
	cls_ai_TextFrame = loadClass(env, "com/scriptographer/ai/TextFrame");

	cls_ai_TextRange = loadClass(env, "com/scriptographer/ai/TextRange");
	cid_ai_TextRange = getConstructorID(env, cls_ai_TextRange, "(II)V");
	fid_ai_TextRange_glyphRuns = getFieldID(env, cls_ai_TextRange, "glyphRuns", "I");
	
	cls_ai_TextStory = loadClass(env, "com/scriptographer/ai/TextStory");

	cls_ai_PathStyle = loadClass(env, "com/scriptographer/ai/PathStyle");
	
	mid_PathStyle_init = getMethodID(env, cls_ai_PathStyle, "init", "(Lcom/scriptographer/ai/Color;ZSLcom/scriptographer/ai/Color;ZSFF[FSSFSSSF)V");

	cls_ai_FillStyle = loadClass(env, "com/scriptographer/ai/FillStyle");

	cid_ai_FillStyle = getConstructorID(env, cls_ai_FillStyle, "(Lcom/scriptographer/ai/Color;ZS)V");
	mid_ai_FillStyle_init = getMethodID(env, cls_ai_FillStyle, "init", "(Lcom/scriptographer/ai/Color;ZS)V");
	mid_ai_FillStyle_initNative = getMethodID(env, cls_ai_FillStyle, "initNative", "(I)V");
	
	cls_ai_StrokeStyle = loadClass(env, "com/scriptographer/ai/StrokeStyle");
	cid_ai_StrokeStyle = getConstructorID(env, cls_ai_StrokeStyle, "(Lcom/scriptographer/ai/Color;ZSFF[FSSF)V");
	mid_ai_StrokeStyle_init = getMethodID(env, cls_ai_StrokeStyle, "init", "(Lcom/scriptographer/ai/Color;ZSFF[FSSF)V");
	mid_ai_StrokeStyle_initNative = getMethodID(env, cls_ai_StrokeStyle, "initNative", "(I)V");
	
	cls_ai_CharacterStyle = loadClass(env, "com/scriptographer/ai/CharacterStyle");
	mid_ai_CharacterStyle_markSetStyle = getMethodID(env, cls_ai_CharacterStyle, "markSetStyle", "()V");

	cls_ai_ParagraphStyle = loadClass(env, "com/scriptographer/ai/ParagraphStyle");
	mid_ai_ParagraphStyle_markSetStyle = getMethodID(env, cls_ai_ParagraphStyle, "markSetStyle", "()V");
	
	cls_ai_Group = loadClass(env, "com/scriptographer/ai/Group");
	
	cls_ai_Raster = loadClass(env, "com/scriptographer/ai/Raster");
	fid_ai_Raster_data = getFieldID(env, cls_ai_Raster, "data", "I");
	
	cls_ai_PlacedItem = loadClass(env, "com/scriptographer/ai/PlacedItem");
	
	cls_ai_Tracing = loadClass(env, "com/scriptographer/ai/Tracing");
	mid_ai_Tracing_markDirty = getMethodID(env, cls_ai_Tracing, "markDirty", "()V");
	
	cls_ai_Layer = loadClass(env, "com/scriptographer/ai/Layer");

	cls_ai_Segment = loadClass(env, "com/scriptographer/ai/Segment");
	cls_ai_Curve = loadClass(env, "com/scriptographer/ai/Curve");

	cls_ai_TabletValue = loadClass(env, "com/scriptographer/ai/TabletValue");
	cid_ai_TabletValue = getConstructorID(env, cls_ai_TabletValue, "(FF)V");
	fid_ai_TabletValue_offset = getFieldID(env, cls_ai_TabletValue, "offset", "F");
	fid_ai_TabletValue_value = getFieldID(env, cls_ai_TabletValue, "value", "F");
	
	cls_ai_GradientStop = loadClass(env, "com/scriptographer/ai/GradientStop");
	mid_ai_GradientStop_init = getMethodID(env, cls_ai_GradientStop, "init", "(FFLcom/scriptographer/ai/Color;)V");
	
	cls_ai_Document = loadClass(env, "com/scriptographer/ai/Document");

	cls_ai_LiveEffect = loadClass(env, "com/scriptographer/ai/LiveEffect");
	cid_ai_LiveEffect = getConstructorID(env, cls_ai_LiveEffect, "(ILjava/lang/String;Ljava/lang/String;IIIII)V");
	mid_ai_LiveEffect_onEditParameters = getStaticMethodID(env, cls_ai_LiveEffect, "onEditParameters", "(ILjava/util/Map;IZ)V");
	mid_ai_LiveEffect_onCalculate = getStaticMethodID(env, cls_ai_LiveEffect, "onCalculate", "(ILjava/util/Map;Lcom/scriptographer/ai/Art;)I");
	mid_ai_LiveEffect_onGetInputType = getStaticMethodID(env, cls_ai_LiveEffect, "onGetInputType", "(ILjava/util/Map;Lcom/scriptographer/ai/Art;)I");
	
	cls_ai_Timer = loadClass(env, "com/scriptographer/ai/Timer");
	cid_ai_Timer = getConstructorID(env, cls_ai_Timer, "(I)V");
	mid_ai_Timer_onExecute = getStaticMethodID(env, cls_ai_Timer, "onExecute", "(I)V");

	cls_ai_Annotator = loadClass(env, "com/scriptographer/ai/Annotator");
	cid_ai_Annotator = getConstructorID(env, cls_ai_Annotator, "(I)V");
	mid_ai_Annotator_onDraw = getStaticMethodID(env, cls_ai_Annotator, "onDraw", "(III)V");
	mid_ai_Annotator_onInvalidate = getStaticMethodID(env, cls_ai_Annotator, "onInvalidate", "(I)V");
	
	cls_ai_HitTest = loadClass(env, "com/scriptographer/ai/HitTest");
	cid_ai_HitTest = getConstructorID(env, cls_ai_HitTest, "(ILcom/scriptographer/ai/Art;IFLcom/scriptographer/ai/Point;)V");

// ADM:
	cls_adm_NativeObject = loadClass(env, "com/scriptographer/adm/NativeObject");
	fid_adm_NativeObject_handle = getFieldID(env, cls_adm_NativeObject, "handle", "I");

	cls_adm_Rectangle = loadClass(env, "com/scriptographer/adm/Rectangle");
	cid_adm_Rectangle = getConstructorID(env, cls_adm_Rectangle, "(IIII)V");
	fid_adm_Rectangle_x = getFieldID(env, cls_adm_Rectangle, "x", "I");
	fid_adm_Rectangle_y = getFieldID(env, cls_adm_Rectangle, "y", "I");
	fid_adm_Rectangle_width = getFieldID(env, cls_adm_Rectangle, "width", "I");
	fid_adm_Rectangle_height = getFieldID(env, cls_adm_Rectangle, "height", "I");
	mid_adm_Rectangle_set = getMethodID(env, cls_adm_Rectangle, "set", "(IIII)V");
	
	cls_adm_Point = loadClass(env, "com/scriptographer/adm/Point");
	cid_adm_Point = getConstructorID(env, cls_adm_Point, "(II)V");
	fid_adm_Point_x = getFieldID(env, cls_adm_Point, "x", "I");
	fid_adm_Point_y = getFieldID(env, cls_adm_Point, "y", "I");
	mid_adm_Point_set = getMethodID(env, cls_adm_Point, "set", "(II)V");
	
	cls_adm_Size = loadClass(env, "com/scriptographer/adm/Size");
	cid_adm_Size = getConstructorID(env, cls_adm_Size, "(II)V");
	fid_adm_Size_width = getFieldID(env, cls_adm_Size, "width", "I");
	fid_adm_Size_height = getFieldID(env, cls_adm_Size, "height", "I");
	mid_adm_Size_set = getMethodID(env, cls_adm_Size, "set", "(II)V");

	cls_adm_Dialog = loadClass(env, "com/scriptographer/adm/Dialog");
	mid_adm_Dialog_onSizeChanged = getMethodID(env, cls_adm_Dialog, "onSizeChanged", "(II)V");

	cls_adm_PopupDialog = loadClass(env, "com/scriptographer/adm/PopupDialog");

	cls_adm_DialogGroupInfo = loadClass(env, "com/scriptographer/adm/DialogGroupInfo");
	cid_adm_DialogGroupInfo = getConstructorID(env, cls_adm_DialogGroupInfo, "(Ljava/lang/String;I)V");

	cls_adm_Drawer = loadClass(env, "com/scriptographer/adm/Drawer");
	cid_adm_Drawer = getConstructorID(env, cls_adm_Drawer, "(I)V");

	cls_adm_FontInfo = loadClass(env, "com/scriptographer/adm/FontInfo");
	cid_adm_FontInfo = getConstructorID(env, cls_adm_FontInfo, "(IIIII)V");

	cls_adm_Image = loadClass(env, "com/scriptographer/adm/Image");
	fid_adm_Image_byteWidth = getFieldID(env, cls_adm_Image, "byteWidth", "I");
	fid_adm_Image_bitsPerPixel = getFieldID(env, cls_adm_Image, "bitsPerPixel", "I");
	mid_adm_Image_getIconHandle = getMethodID(env, cls_adm_Image, "getIconHandle", "()I");

	cls_adm_ListItem = loadClass(env, "com/scriptographer/adm/ListItem");
	fid_adm_ListItem_listHandle = getFieldID(env, cls_adm_ListItem, "listHandle", "I");	
	
	cls_adm_HierarchyList = loadClass(env, "com/scriptographer/adm/HierarchyList");

	cls_adm_ListEntry = loadClass(env, "com/scriptographer/adm/ListEntry");

	cls_adm_HierarchyListEntry = loadClass(env, "com/scriptographer/adm/HierarchyListEntry");

	cls_adm_NotificationHandler = loadClass(env, "com/scriptographer/adm/NotificationHandler");
	fid_adm_NotificationHandler_tracker = getFieldID(env, cls_adm_NotificationHandler, "tracker", "Lcom/scriptographer/adm/Tracker;");
	fid_adm_NotificationHandler_drawer = getFieldID(env, cls_adm_NotificationHandler, "drawer", "Lcom/scriptographer/adm/Drawer;");
	mid_adm_NotificationHandler_onNotify_String = getMethodID(env, cls_adm_NotificationHandler, "onNotify", "(Ljava/lang/String;)V");
	mid_adm_NotificationHandler_onNotify_int = getMethodID(env, cls_adm_NotificationHandler, "onNotify", "(I)V");
	mid_adm_NotificationHandler_onDraw = getMethodID(env, cls_adm_NotificationHandler, "onDraw", "(Lcom/scriptographer/adm/Drawer;)V");
	
	cls_adm_Tracker = loadClass(env, "com/scriptographer/adm/Tracker");
	mid_adm_Tracker_onTrack = getMethodID(env, cls_adm_Tracker, "onTrack", "(Lcom/scriptographer/adm/NotificationHandler;IIIIIICCJ)Z");
	
	cls_adm_MenuItem = loadClass(env, "com/scriptographer/adm/MenuItem");
	mid_adm_MenuItem_wrapHandle = getStaticMethodID(env, cls_adm_MenuItem, "wrapHandle", "(ILjava/lang/String;ILjava/lang/String;)Lcom/scriptographer/adm/MenuItem;");
	mid_adm_MenuItem_onSelect = getStaticMethodID(env, cls_adm_MenuItem, "onSelect", "(I)V");
	mid_adm_MenuItem_onUpdate = getStaticMethodID(env, cls_adm_MenuItem, "onUpdate", "(IIII)V");
	
	cls_adm_MenuGroup = loadClass(env, "com/scriptographer/adm/MenuGroup");
}

long ScriptographerEngine::getNanoTime() {
#ifdef MAC_ENV
		Nanoseconds nano = AbsoluteToNanoseconds(UpTime());
		return UnsignedWideToUInt64(nano);
#endif
#ifdef WIN_ENV
		static int scaleFactor = 0;
		if (scaleFactor == 0) {
			LARGE_INTEGER frequency;
			QueryPerformanceFrequency (&frequency);
			scaleFactor = frequency.QuadPart;
		}
		LARGE_INTEGER counter;
		QueryPerformanceCounter (& counter);
		return counter.QuadPart * 1000000 / scaleFactor;
#endif
}

bool ScriptographerEngine::isKeyDown(short keycode) {
#ifdef MAC_ENV
	// table that converts java keycodes to mac keycodes:
	static unsigned char keycodeToMac[256] = {
		0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0x33,0x30,0x4c,0xff,0xff,0x24,0xff,0xff,0x38,
		0x3b,0x36,0xff,0x39,0xff,0xff,0xff,0xff,0xff,0xff,0x35,0xff,0xff,0xff,0xff,0x31,0x74,
		0x79,0x77,0x73,0x7b,0x7e,0x7c,0x7d,0xff,0xff,0xff,0x2b,0xff,0x2f,0x2c,0x1d,0x12,0x13,
		0x14,0x15,0x17,0x16,0x1a,0x1c,0x19,0xff,0x29,0xff,0x18,0xff,0xff,0xff,0x00,0x0b,0x08,
		0x02,0x0e,0x03,0x05,0x04,0x22,0x26,0x28,0x25,0x2e,0x2d,0x1f,0x23,0x0c,0x0f,0x01,0x11,
		0x20,0x09,0x0d,0x07,0x10,0x06,0x21,0x2a,0x1e,0xff,0xff,0x52,0x53,0x54,0x55,0x56,0x57,
		0x58,0x59,0x5b,0x5c,0x43,0x45,0xff,0x1b,0x41,0x4b,0x7a,0x78,0x63,0x76,0x60,0x61,0x62,
		0x64,0x65,0x6d,0x67,0x6f,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,
		0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,
		0xff,0xff,0xff,0xff,0x3a,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,
		0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,
		0xff,0xff,0xff,0xff,0xff,0x27,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,
		0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,
		0xff,0x32,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,
		0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,
	};
	if (keycode >= 0 && keycode <= 255) {
		keycode = keycodeToMac[keycode];
		if (keycode != 0xff) {
			KeyMap keys;
			GetKeys(keys);
			// return BitTst(&keys, keycode) != 0;
			return (((unsigned char *) keys)[keycode >> 3] & (1 << (keycode & 7))) != 0;
		}
	}
	return false;
#endif
#ifdef WIN_ENV
	return (GetAsyncKeyState(keycode) & 0x8000) ? 1 : 0;
#endif
}

void ScriptographerEngine::println(JNIEnv *env, const char *str, ...) {
	int size = 8192;
	char *text = new char[size];
	va_list args;
	va_start(args, str);
	vsnprintf(text, size, str, args);
	va_end(args);
	callVoidMethodReport(env, env->GetStaticObjectField(cls_System, fid_System_out), mid_PrintStream_println, env->NewStringUTF(text));
	delete text;
}

void ScriptographerEngine::reportError(JNIEnv *env) {
	JNI_CHECK_ENV
	if (isInitialized()) {
		jthrowable throwable = env->ExceptionOccurred();
		if (throwable != NULL) {
			env->ExceptionClear();
			callStaticVoidMethod(env, cls_ScriptographerEngine, mid_ScriptographerEngine_reportError, throwable);
		}
	} else {
		// TODO: ????
		env->ExceptionDescribe();
	}
}

// com.scriptographer.ai.Point <-> AIRealPoint

jobject ScriptographerEngine::convertPoint(JNIEnv *env, AIReal x, AIReal y, jobject res) {
	if (res == NULL) {
		return newObject(env, cls_ai_Point, cid_ai_Point, (jfloat) x, (jfloat) y);
	} else {
		callVoidMethod(env, res, mid_ai_Point_set, (jfloat) x, (jfloat) y);
		return res;
	}
}

// This handles 3 types of points: com.scriptographer.ai.Point, java.awt.geom.Point2D, com.scriptographer.adm.Point
AIRealPoint *ScriptographerEngine::convertPoint(JNIEnv *env, jobject pt, AIRealPoint *res) {
	if (res == NULL)
		res = new AIRealPoint;
	if (env->IsInstanceOf(pt, cls_ai_Point)) {
		res->h = env->GetFloatField(pt, fid_ai_Point_x);
		res->v = env->GetFloatField(pt, fid_ai_Point_y);
	} else if (env->IsInstanceOf(pt, cls_adm_Point)) {
		res->h = env->GetIntField(pt, fid_adm_Point_x);
		res->v = env->GetIntField(pt, fid_adm_Point_y);
	}
	EXCEPTION_CHECK(env);
	return res;
}

// com.scriptographer.adm.Point <-> ADMPoint
jobject ScriptographerEngine::convertPoint(JNIEnv *env, int x, int y, jobject res) {
	if (res == NULL) {
		return newObject(env, cls_adm_Point, cid_adm_Point, x, y);
	} else {
		callVoidMethod(env, res, mid_adm_Point_set, x, y);
		return res;
	}
}

// This handles 3 types of points: com.scriptographer.adm.Point,com.scriptographer.ai.Point, java.awt.geom.Point2D
ADMPoint *ScriptographerEngine::convertPoint(JNIEnv *env, jobject pt, ADMPoint *res) {
	if (res == NULL)
		res = new ADMPoint;
	if (env->IsInstanceOf(pt, cls_adm_Point)) {
		res->h = env->GetIntField(pt, fid_adm_Point_x);
		res->v = env->GetIntField(pt, fid_adm_Point_y);
	} else if (env->IsInstanceOf(pt, cls_ai_Point)) {
		res->h = (short) env->GetFloatField(pt, fid_ai_Point_x);
		res->v = (short) env->GetFloatField(pt, fid_ai_Point_y);
	}
	EXCEPTION_CHECK(env);
	return res;
}

// com.scriptographer.ai.Rectangle <-> AIRealRect

jobject ScriptographerEngine::convertRectangle(JNIEnv *env, AIReal left, AIReal top, AIReal right, AIReal bottom, jobject res) {
	// AIRealRects are upside down, top and bottom are switched!
	if (res == NULL) {
		return newObject(env, cls_ai_Rectangle, cid_ai_Rectangle, (jfloat) left, (jfloat) bottom, (jfloat) (right - left), (jfloat) (top - bottom));
	} else {
		callVoidMethod(env, res, mid_ai_Rectangle_set, (jfloat) left, (jfloat) bottom, (jfloat) (right - left), (jfloat) (top - bottom));
		return res;
	}
}

AIRealRect *ScriptographerEngine::convertRectangle(JNIEnv *env, jobject rt, AIRealRect *res) {
	// AIRealRects are upside down, top and bottom are switched!
	if (res == NULL)
		res = new AIRealRect;
	res->left =  env->GetFloatField(rt, fid_ai_Rectangle_x);
	res->bottom =  env->GetFloatField(rt, fid_ai_Rectangle_y);
	res->right = res->left + env->GetFloatField(rt, fid_ai_Rectangle_width);
	res->top = res->bottom + env->GetFloatField(rt, fid_ai_Rectangle_height);
	EXCEPTION_CHECK(env);
	return res;
}

// java.awt.Rectangle <-> ADMRect

jobject ScriptographerEngine::convertRectangle(JNIEnv *env, int left, int top, int right, int bottom, jobject res) {
	if (res == NULL) {
		return newObject(env, cls_adm_Rectangle, cid_adm_Rectangle, left, top, right - left, bottom - top);
	} else {
		callVoidMethod(env, res, mid_adm_Rectangle_set, left, top, right - left, bottom - top);
		return res;
	}
}

ADMRect *ScriptographerEngine::convertRectangle(JNIEnv *env, jobject rt, ADMRect *res) {
	if (res == NULL)
		res = new ADMRect;
	res->left = env->GetIntField(rt, fid_adm_Rectangle_x);
	res->top = env->GetIntField(rt, fid_adm_Rectangle_y);
	res->right = res->left + env->GetIntField(rt, fid_adm_Rectangle_width);
	res->bottom = res->top + env->GetIntField(rt, fid_adm_Rectangle_height);
	EXCEPTION_CHECK(env);
	return res;
}

// com.scriptographer.ai.Size <-> AIRealPoint
jobject ScriptographerEngine::convertSize(JNIEnv *env, float width, float height, jobject res) {
	if (res == NULL) {
		return newObject(env, cls_ai_Size, cid_ai_Size, (jfloat) width, (jfloat) height);
	} else {
		callVoidMethod(env, res, mid_ai_Size_set, (jfloat) width, (jfloat) height);
		return res;
	}
}

AIRealPoint *ScriptographerEngine::convertSize(JNIEnv *env, jobject size, AIRealPoint *res) {
	if (res == NULL)
		res = new AIRealPoint;
	res->h = env->GetFloatField(size, fid_ai_Size_width);
	res->v = env->GetFloatField(size, fid_ai_Size_height);
	EXCEPTION_CHECK(env);
	return res;
}

// com.scriptographer.adm.Size <-> ADMPoint
jobject ScriptographerEngine::convertSize(JNIEnv *env, int width, int height, jobject res) {
	if (res == NULL) {
		return newObject(env, cls_adm_Size, cid_adm_Size, (jint) width, (jint) height);
	} else {
		callVoidMethod(env, res, mid_adm_Size_set, (jint) width, (jint) height);
		return res;
	}
}

ADMPoint *ScriptographerEngine::convertSize(JNIEnv *env, jobject size, ADMPoint *res) {
	if (res == NULL)
		res = new ADMPoint;
	res->h = env->GetIntField(size, fid_adm_Size_width);
	res->v = env->GetIntField(size, fid_adm_Size_height);
	EXCEPTION_CHECK(env);
	return res;
}

// java.awt.Color <-> ADMRGBColor
jobject ScriptographerEngine::convertColor(JNIEnv *env, ADMRGBColor *srcCol) {
	return newObject(env, cls_awt_Color, cid_awt_Color, (jfloat) srcCol->red / 65535.0, (jfloat) srcCol->green / 65535.0, (jfloat) srcCol->blue / 65535.0);
}

ADMRGBColor *ScriptographerEngine::convertColor(JNIEnv *env, jobject srcCol, ADMRGBColor *dstCol) {
	if (dstCol == NULL)
		dstCol = new ADMRGBColor;
	jfloatArray array = (jfloatArray) env->CallObjectMethod(srcCol, mid_awt_Color_getColorComponents, NULL);
	int length = env->GetArrayLength(array);
	// TODO: add handling for different values of length!!!
	jfloat *values = new jfloat[length];
	env->GetFloatArrayRegion(array, 0, length, values);
	// fast rounding:
	dstCol->red =  (unsigned short) (values[0] * 65535.0 + 0.5);
	dstCol->green = (unsigned short) (values[1] * 65535.0 + 0.5);
	dstCol->blue = (unsigned short) (values[2] * 65535.0 + 0.5);
	delete values;
	EXCEPTION_CHECK(env);
	return dstCol;
}

// com.scriptoggrapher.ai.Color <-> AIColor
jobject ScriptographerEngine::convertColor(JNIEnv *env, AIColor *srcCol, AIReal alpha) {
	switch (srcCol->kind) {
		case kGrayColor: {
			return newObject(env, cls_ai_GrayColor, cid_ai_GrayColor,
				(jfloat) srcCol->c.g.gray, (jfloat) alpha);
		}
		case kThreeColor: {
			AIThreeColorStyle *rgb = &srcCol->c.rgb;
			return newObject(env, cls_ai_RGBColor, cid_ai_RGBColor, (jfloat) rgb->red,
				(jfloat) rgb->green, (jfloat) rgb->blue, (jfloat) alpha);
		}
		case kFourColor: {
			AIFourColorStyle *f = &srcCol->c.f;
			return newObject(env, cls_ai_CMYKColor, cid_ai_CMYKColor, (jfloat) f->cyan,
				(jfloat) f->magenta, (jfloat) f->yellow, (jfloat) f->black, (jfloat) alpha);
		}
		case kGradient: {
			AIGradientStyle *b = &srcCol->c.b;
			return newObject(env, cls_ai_GradientColor, cid_ai_GradientColor,
				(jint) b->gradient, gEngine->convertPoint(env, &b->gradientOrigin),
				(jfloat) b->gradientAngle, (jfloat) b->gradientLength,
				gEngine->convertMatrix(env, &b->matrix),
				(jfloat) b->hiliteAngle, (jfloat) b->hiliteLength);
		}
		case kPattern: {
			AIPatternStyle *p = &srcCol->c.p;
			return newObject(env, cls_ai_PatternColor, cid_ai_PatternColor,
				 (jint) p->pattern, (jfloat) p->shiftDist, (jfloat) p->shiftAngle,
				 gEngine->convertPoint(env, &p->scale), (jfloat) p->rotate,
				 (jboolean) p->reflect, (jfloat) p->reflectAngle,
				 (jfloat) p->shearAngle, (jfloat) p->shearAxis, 
				 gEngine->convertMatrix(env, &p->transform));
		}
		case kNoneColor: {
			return obj_ai_Color_NONE;
		}
		// TODO: add kCustomColor
	}
	return NULL;
}

AIColor *ScriptographerEngine::convertColor(JNIEnv *env, jobject srcCol, AIColor *dstCol, AIReal *alpha) {
	if (dstCol == NULL)
		dstCol = new AIColor;
	// TODO: add kCustomColor
	if (env->IsInstanceOf(srcCol, cls_ai_GradientColor)) {
		dstCol->kind = kGradient;
		// call mid_ai_GradientColor_set, which sets the AIGradientStyle by calling 
		// Java_com_scriptographer_ai_Color_nativeSetGradient with the right arguments
		callVoidMethod(env, srcCol, mid_ai_GradientColor_set, (jint) &dstCol->c.b);
	} else if (env->IsInstanceOf(srcCol, cls_ai_PatternColor)) {
		dstCol->kind = kPattern;
		// call mid_ai_PatternColor_set, which sets the AIPatternStyle by calling 
		// Java_com_scriptographer_ai_Color_nativeSetPattern with the right arguments
		callVoidMethod(env, srcCol, mid_ai_PatternColor_set, (jint) &dstCol->c.p);
	} else {
		convertColor(env, (jfloatArray) env->CallObjectMethod(srcCol, mid_ai_Color_getComponents), dstCol, alpha);
	}
	return dstCol;
}

AIColor *ScriptographerEngine::convertColor(JNIEnv *env, jfloatArray srcCol, AIColor *dstCol, AIReal *alpha) {
	if (dstCol == NULL)
		dstCol = new AIColor;
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
	if (dstCol == NULL)
		dstCol = new AIColor;
	dstCol->kind = kThreeColor;
	dstCol->c.rgb.red = (float) srcCol->red / 65535.0;
	dstCol->c.rgb.green = (float) srcCol->green / 65535.0;
	dstCol->c.rgb.blue = (float) srcCol->blue / 65535.0;
	return dstCol;
}

ADMRGBColor *ScriptographerEngine::convertColor(AIColor *srcCol, ADMRGBColor *dstCol) {
	if (dstCol == NULL)
		dstCol = new ADMRGBColor;
	// convert to RGB if it isn't already:
	if (srcCol->kind != kThreeColor && !convertColor(srcCol, kAIRGBColorSpace, srcCol))
		return NULL;
	dstCol->red = (short) (srcCol->c.rgb.red * 65535.0 + 0.5);
	dstCol->green = (short) (srcCol->c.rgb.green * 65535.0 + 0.5);
	dstCol->blue = (short) (srcCol->c.rgb.blue * 65535.0 + 0.5);
	return dstCol;
}

// AIColor <-> AIColor
AIColor *ScriptographerEngine::convertColor(AIColor *srcCol, AIColorConversionSpaceValue dstSpace, AIColor *dstCol, AIReal srcAlpha, AIReal *dstAlpha) {
	// determine srcCol's space and sample size:
	AIColorConversionSpaceValue srcSpace;
	int srcSize;
	bool srcHasAlpha = srcAlpha >= 0;
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

	bool dstHasAlpha = dstSpace == kAIACMYKColorSpace ||
		dstSpace == kAIARGBColorSpace ||
		dstSpace == kAIAGrayColorSpace;

	if (srcSpace >= 0 && dstSpace >= 0) {
		AISampleComponent src[5];
		AISampleComponent dst[5];
		memcpy(src, &srcCol->c, srcSize * sizeof(AISampleComponent));
		if (srcHasAlpha) src[srcSize] = srcAlpha;
		// TODO: why swapping kGrayColor???
		if (srcCol->kind == kGrayColor) src[0] = 1.0 - src[0];
		ASBoolean inGamut;
#if kPluginInterfaceVersion < kAI12
		sAIColorConversion->ConvertSampleColor(srcSpace, src, dstSpace, dst, &inGamut);
#else
		AIColorConvertOptions options;
		sAIColorConversion->ConvertSampleColor(srcSpace, src, dstSpace, dst, options, &inGamut);
#endif
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

// AIRealMatrix <-> com.scriptoggrapher.ai.Matrix
jobject ScriptographerEngine::convertMatrix(JNIEnv *env, AIRealMatrix *mt, jobject res) {
	return newObject(env, cls_ai_Matrix, cid_ai_Matrix,
		(jdouble) mt->a, (jdouble) mt->b,
		(jdouble) mt->c, (jdouble) mt->d,
		(jdouble) mt->tx, (jdouble) mt->ty);
}

AIRealMatrix *ScriptographerEngine::convertMatrix(JNIEnv *env, jobject mt, AIRealMatrix *res) {
	if (res == NULL)
		res = new AIRealMatrix;
	res->a = env->CallDoubleMethod(mt, mid_ai_Matrix_getScaleX);
	res->b = env->CallDoubleMethod(mt, mid_ai_Matrix_getShearY);
	res->c = env->CallDoubleMethod(mt, mid_ai_Matrix_getShearX);
	res->d = env->CallDoubleMethod(mt, mid_ai_Matrix_getScaleY);
	res->tx = env->CallDoubleMethod(mt, mid_ai_Matrix_getTranslateX);
	res->ty = env->CallDoubleMethod(mt, mid_ai_Matrix_getTranslateY);
	EXCEPTION_CHECK(env);
	return res;
}

// AIFillStyle <-> com.scriptoggrapher.ai.FillStyle

jobject ScriptographerEngine::convertFillStyle(JNIEnv *env, AIFillStyle *style, jobject res) {
	// TODO: add additional AIFillStyleMap
	jobject color = convertColor(env, &style->color);
	if (res == NULL) {
		res = newObject(env, cls_ai_FillStyle, cid_ai_FillStyle, color, true, style->overprint);
	} else {
		callVoidMethod(env, res, mid_ai_FillStyle_init, color, true, style->overprint);
	}
	return res;
}

AIFillStyle *ScriptographerEngine::convertFillStyle(JNIEnv *env, jobject style, AIFillStyle *res) {
	if (res == NULL)
		res = new AIFillStyle;
	callVoidMethod(env, style, mid_ai_FillStyle_initNative, res);
	return res;
}

// AIStrokeStyle <-> com.scriptoggrapher.ai.StrokeStyle

jobject ScriptographerEngine::convertStrokeStyle(JNIEnv *env, AIStrokeStyle *style, jobject res) {
	// TODO: add additional AIStrokeStyleMap
	jobject color = convertColor(env, &style->color);
	int count = style->dash.length;
	jfloatArray dashArray = env->NewFloatArray(count);
	env->SetFloatArrayRegion(dashArray, 0, count, style->dash.array);
	if (res == NULL) {
		res = newObject(env, cls_ai_StrokeStyle, cid_ai_StrokeStyle, color, true,
			style->overprint, style->width, style->dash.offset, dashArray,
			style->cap, style->join, style->miterLimit);
	} else {
		callVoidMethod(env, res, mid_ai_StrokeStyle_init, color, true,
			style->overprint, style->width, style->dash.offset, dashArray,
			style->cap, style->join, style->miterLimit);
	}
	return res;
}

AIStrokeStyle *ScriptographerEngine::convertStrokeStyle(JNIEnv *env, jobject style, AIStrokeStyle *res) {
	if (res == NULL)
		res = new AIStrokeStyle;
	callVoidMethod(env, style, mid_ai_StrokeStyle_initNative, res);
	return res;
}


// AIArtSet <-> ArtSet

jobject ScriptographerEngine::convertArtSet(JNIEnv *env, AIArtSet set, bool layerOnly) {
	ArtSet_filter(set, layerOnly);
	long count;
	sAIArtSet->CountArtSet(set, &count);
	jobject artSet = newObject(env, cls_ai_ArtSet, cid_ArtSet); 
	for (long i = 0; i < count; i++) {
		jobject obj;
		AIArtHandle art;
		if (!sAIArtSet->IndexArtSet(set, i, &art)) {
			if (art != NULL) {
				obj = wrapArtHandle(env, art);
				if (obj != NULL)
					callBooleanMethod(env, artSet, mid_ai_ArtSet_add, obj);
			}
		}
	}
	EXCEPTION_CHECK(env);
	return artSet;
}

AIArtSet ScriptographerEngine::convertArtSet(JNIEnv *env, jobject artSet) {
	AIArtSet set = NULL;
	if (!sAIArtSet->NewArtSet(&set)) {
		// use a for loop with size instead of hasNext, because that saves us many calls...
		jint size = callIntMethod(env, artSet, mid_List_size);
		for (int i = 0; i < size; i++) {
			jobject obj = callObjectMethod(env, artSet, mid_List_get, i);
			if (obj != NULL)
				sAIArtSet->AddArtToArtSet(set, getArtHandle(env, obj));
		}
	}
	EXCEPTION_CHECK(env);
	return set;
}
// java.util.Map <-> AIDictionaryRef
jobject ScriptographerEngine::convertDictionary(JNIEnv *env, AIDictionaryRef dictionary, jobject map, bool dontOverwrite, bool removeOld) {
	JNI_CHECK_ENV
	AIDictionaryIterator iterator = NULL;
	ScriptographerException *exc = NULL;
	try {
		if (map == NULL) {
			map = newObject(env, cls_HashMap, cid_HashMap);
		} else if (removeOld) {
			// scan through the map and remove the entries that are not contained
			// in the dictionary any longer (just tell by name):

			// use the env's version of the callers for speed reasons. check for exceptions only once for every entry:
			jobject keySet = env->CallObjectMethod(map, mid_Map_keySet);
			jobject iterator = env->CallObjectMethod(keySet, mid_Set_iterator);
			while (env->CallBooleanMethod(iterator, mid_Iterator_hasNext)) {
				jobject key = env->CallObjectMethod(iterator, mid_Iterator_next);
				char *name = convertString(env, (jstring) env->CallObjectMethod(key, mid_Object_toString));
				// now see if there's an entry called like this, and if not, removed it
				// from the map as well:
				if (!sAIDictionary->IsKnown(dictionary, sAIDictionary->Key(name))) {
					env->CallVoidMethod(iterator, mid_Iterator_remove);
				}
				delete name;
				EXCEPTION_CHECK(env);
			}
		}
		// walk through the dictionary and see what we can add:
		if (!sAIDictionary->Begin(dictionary, &iterator)) {
			while (!sAIDictionaryIterator->AtEnd(iterator)) {
				AIDictKey key = sAIDictionaryIterator->GetKey(iterator);
				const char *name = sAIDictionary->GetKeyString(key);
				jobject keyObj = convertString(env, name); // consider newStringUTF!
				// if dontOverwrite is set, check first wether that key already exists in the map and if so, skip it:
				if (dontOverwrite && callObjectMethod(env, map, mid_Map_get, keyObj) != NULL) {
					// skip this entry
					sAIDictionaryIterator->Next(iterator);
					continue;
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
									obj = convertString(env, value);
								}
							} break;
							case DictType: {
								AIDictionaryRef dict;
								// this could be an art object:
								AIArtHandle art;
								if (!sAIEntry->ToArt(entry, &art)) {
									obj = wrapArtHandle(env, art, dictionary);
								} else if (!sAIEntry->ToDict(entry, &dict)) {
									// add to the existing object, if there's any
									jobject subMap = callObjectMethod(env, map, mid_Map_get, keyObj);
									if (subMap != NULL && !env->IsInstanceOf(subMap, cls_Map))
										subMap = NULL;
									obj = convertDictionary(env, dict, subMap, dontOverwrite, removeOld);
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
									obj = convertFillStyle(env, &fill);
								}
							}
							break;
							case StrokeStyleType: {
								AIStrokeStyle stroke;
								if (!sAIEntry->ToStrokeStyle(entry, &stroke)) {
									obj = convertStrokeStyle(env,&stroke);
								}
							}
						}
					} catch(ScriptographerException *e) {
						exc = e;
					}
					sAIEntry->Release(entry);
					if (exc != NULL)
						throw exc;
				}
				if (obj != NULL) {
					callObjectMethod(env, map, mid_Map_put, keyObj, obj);
				}
				sAIDictionaryIterator->Next(iterator);
			}
		}
	} catch(ScriptographerException *e) {
		exc = e;
	}
	if (iterator != NULL)
		sAIDictionaryIterator->Release(iterator);
	
	if (exc != NULL)
		throw exc;
	
	return map;
}

AIDictionaryRef ScriptographerEngine::convertDictionary(JNIEnv *env, jobject map, AIDictionaryRef dictionary, bool dontOverwrite, bool removeOld) {
	JNI_CHECK_ENV
	if (dictionary == NULL) {
		sAIDictionary->CreateDictionary(&dictionary);
		if (dictionary == NULL)
			throw new StringException("Cannot create the dictionary");
	} else if (removeOld) {
		// scan through the dictionary and remove the entries that are not contained
		// in the map any longer (just tell by name):
		AIDictionaryIterator iterator = NULL;
		ScriptographerException *exc = NULL;
		try {
			if (!sAIDictionary->Begin(dictionary, &iterator)) {
				while (!sAIDictionaryIterator->AtEnd(iterator)) {
					AIDictKey key = sAIDictionaryIterator->GetKey(iterator);
					jobject keyObj = convertString(env, sAIDictionary->GetKeyString(key)); // consider newStringUTF!
					// now see if there's an entry called like this, and if not, removed it
					// from the dictionary as well:
					jobject obj = callObjectMethod(env, map, mid_Map_get, keyObj);
					if (obj == NULL) {
						// be carefull: only delete objects that can actually be wrapped
						// in java. the others are not from us because they could not be wrapped
						// when creating the map, so don't remove them now.
						// TODO: Make sure this list is allways the opposite of what is handled bellow:
						AIEntryType type = UnknownType;
						sAIDictionary->GetEntryType(dictionary, key, &type);
						switch (type) {
							UnknownType:
							ArrayType:
							BinaryType:
							PatternRefType:
							BrushPatternRefType:
							CustomColorRefType:
							GradientRefType:
							PluginObjectRefType:
							UIDType:
							UIDREFType:
							XMLNodeType:
							SVGFilterType:
							ArtStyleType:
							SymbolPatternRefType:
							GraphDesignRefType:
							BlendStyleType:
							GraphicObjectType:
							// do nothing
							break;
						default:
							sAIDictionary->DeleteEntry(dictionary, key);
						}
						
					}
					sAIDictionaryIterator->Next(iterator);
				}
			}
		} catch(ScriptographerException *e) {
			exc = e;
		}
		if (iterator != NULL)
			sAIDictionaryIterator->Release(iterator);
		
		if (exc != NULL)
			throw exc;
	}
	// walk through the map and see what we can add:
	// use the env's version of the callers for speed reasons. check for exceptions only once for every entry:
	jobject keySet = env->CallObjectMethod(map, mid_Map_keySet);
	jobject iterator = env->CallObjectMethod(keySet, mid_Set_iterator);
	while (env->CallBooleanMethod(iterator, mid_Iterator_hasNext)) {
		jobject keyObj = env->CallObjectMethod(iterator, mid_Iterator_next);
		jobject value = env->CallObjectMethod(map, mid_Map_get, keyObj);
		char *name = convertString(env, (jstring) env->CallObjectMethod(keyObj, mid_Object_toString));
		EXCEPTION_CHECK(env);
		// if dontOverwrite is set, check first wether that key already exists in the dictionary and if so, skip it:
		AIDictKey key = sAIDictionary->Key(name);
		delete name;
		if (dontOverwrite && sAIDictionary->IsKnown(dictionary, key)) {
			// skip this entry
			continue;
		}

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
		AIEntryRef entry = NULL;

		if (value == NULL) {
			sAIDictionary->SetBinaryEntry(dictionary, key, NULL, 0); 
		} else {
			if (env->IsInstanceOf(value, cls_Integer)) {
				entry = sAIEntry->FromInteger(callIntMethod(env, value, mid_Number_intValue));
			} else if (env->IsInstanceOf(value, cls_Boolean)) {
				entry = sAIEntry->FromBoolean(callBooleanMethod(env, value, mid_Boolean_booleanValue));
			} else if (env->IsInstanceOf(value, cls_Float)) {
				entry = sAIEntry->FromInteger((ASInt32) callFloatMethod(env, value, mid_Number_floatValue));
			} else if (env->IsInstanceOf(value, cls_String)) {
				char *string = convertString(env, (jstring) value);
				entry = sAIEntry->FromString(string);
				delete string;
			} else if (env->IsInstanceOf(value, cls_ai_Art)) {
				AIArtHandle art = getArtHandle(env, value);
				sAIDictionary->MoveArtToEntry(dictionary, key, art);
				// let the art object know it's part of a dictionary now:
				setIntField(env, value, fid_ai_Art_dictionaryRef, (jint) dictionary);
			} else if (env->IsInstanceOf(value, cls_Map)) {
				// add to the existing object, if there's any
				AIDictionaryRef subDictionary = NULL;
				sAIDictionary->GetDictEntry(dictionary, key, &subDictionary);
				subDictionary = convertDictionary(env, value, subDictionary, dontOverwrite, removeOld);
				entry = sAIEntry->FromDict(subDictionary);
			} else if (env->IsInstanceOf(value, cls_adm_Point)) {
				AIRealPoint point;
				convertPoint(env, value, &point);
				entry = sAIEntry->FromRealPoint(&point);
			} else if (env->IsInstanceOf(value, cls_ai_Matrix)) {
				AIRealMatrix matrix;
				convertMatrix(env, value, &matrix);
				entry = sAIEntry->FromRealMatrix(&matrix);
			} else if (env->IsInstanceOf(value, cls_ai_FillStyle)) {
				AIFillStyle style;
				convertFillStyle(env, value, &style);
				entry = sAIEntry->FromFillStyle(&style);
			} else if (env->IsInstanceOf(value, cls_ai_StrokeStyle)) {
				AIStrokeStyle style;
				convertStrokeStyle(env, value, &style);
				entry = sAIEntry->FromStrokeStyle(&style);
			}
			if (entry != NULL)
				sAIDictionary->Set(dictionary, key, entry);
		}
	}
	
	return dictionary;
}

// java.io.File <-> SPPlatformFileSpecification

char *ScriptographerEngine::getFilePath(JNIEnv *env, jobject file) {
	char *path = convertString(env, (jstring) callObjectMethod(env, file, mid_File_getPath));
	EXCEPTION_CHECK(env);
	return path;
}

jobject ScriptographerEngine::convertFile(JNIEnv *env, SPPlatformFileSpecification *fileSpec) {
	char path[kMaxPathLength];
	gPlugin->fileSpecToPath(fileSpec, path);
	return newObject(env, cls_File, cid_File, convertString(env, path));
}

SPPlatformFileSpecification *ScriptographerEngine::convertFile(JNIEnv *env, jobject file, SPPlatformFileSpecification *res) {
	char *path = getFilePath(env, file);
	bool create = res == NULL;
	if (create)
		res = new SPPlatformFileSpecification;
	if (!gPlugin->pathToFileSpec(path, res)) {
		if (create)
			delete res;
		res = NULL;
	}
	delete path;
	return res;
}

/**
 * Returns the wrapped AIArtHandle of an object by assuming that it is an anchestor of Class Art and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
AIArtHandle ScriptographerEngine::getArtHandle(JNIEnv *env, jobject obj, bool activateDoc, AIDocumentHandle *doc) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	AIArtHandle art = (AIArtHandle) getIntField(env, obj, fid_ai_NativeObject_handle);
	if (art == NULL)
		throw new StringException("Object is not wrapping an art handle.");
	if (activateDoc || doc != NULL) {
		// fetch docHandle and switch if necessary
		jobject docObj = getObjectField(env, obj, fid_ai_Art_document);
		if (docObj != NULL) {
			AIDocumentHandle docHandle = (AIDocumentHandle) getIntField(env, docObj, fid_ai_NativeObject_handle);
			if (doc != NULL)
				*doc = docHandle;
			// switch to this document if necessary
			if (activateDoc) {
				if (docHandle != gActiveDoc) {
					sAIDocumentList->Activate(docHandle, false);
					gActiveDoc = docHandle;
				}
				// if it's a text object, suspend the text flow now
				// text flow of all suspended documents is resumed at the end
				// by gEngine->resumeSuspendedDocuments()
				short type = kAnyArt;
				AIDictionaryRef dict = NULL;
				// use a dictionary entry to see if it was suspsended before already
				if (!sAIArt->GetArtType(art, &type) && type == kTextFrameArt && !sAIDocument->GetDictionary(&dict)) {
					if (!sAIDictionary->IsKnown(dict, m_docReflowKey)) {
						sAIDocument->SuspendTextReflow();
						sAIDictionary->SetBooleanEntry(dict, m_docReflowKey, true);
						m_suspendedDocuments.add(docHandle);
					}
					sAIDictionary->Release(dict);
				}
			}
		}
	}
	return art;
}

void ScriptographerEngine::resumeSuspendedDocuments() {
	for (int i = m_suspendedDocuments.size() - 1; i >= 0; i--) {
		AIDocumentHandle doc = m_suspendedDocuments.get(i);
		if (doc != gActiveDoc) {
			sAIDocumentList->Activate(doc, false);
			gActiveDoc = doc;
		}
		sAIDocument->ResumeTextReflow();
		AIDictionaryRef dict = NULL;
		// remove dictionary entry
		if (!sAIDocument->GetDictionary(&dict)) {
			sAIDictionary->DeleteEntry(dict, m_docReflowKey);
			sAIDictionary->Release(dict);
		}
	}
	m_suspendedDocuments.reset();
}

/**
 * Returns the wrapped AILayerHandle of an object by assuming that it is an anchestor of Class Art and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
AILayerHandle ScriptographerEngine::getLayerHandle(JNIEnv *env, jobject obj, bool activateDoc) {
	AIArtHandle art = getArtHandle(env, obj, activateDoc);
	AILayerHandle layer = NULL;
	sAIArt->GetLayerOfArt(art, &layer);
	return layer;
}

void *ScriptographerEngine::getWrapperHandle(JNIEnv *env, jobject obj, bool activateDoc, const char *name) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	void *wrapper = (void *) getIntField(env, obj, fid_ai_NativeObject_handle);
	if (wrapper == NULL)
		throw new StringException("Object is not wrapping a %s handle.", name);
	if (activateDoc) {
		// fetch docHandle and switch if necessary
		jobject docObj = getObjectField(env, obj, fid_ai_NativeWrapper_document);
		if (docObj != NULL) {
			AIDocumentHandle doc = (AIDocumentHandle) getIntField(env, docObj, fid_ai_NativeObject_handle);
			// switch to this document if necessary
			if (doc != gActiveDoc) {
				sAIDocumentList->Activate(doc, false);
				gActiveDoc = doc;
			}
		}
	}
	return wrapper;
}

AIPatternHandle ScriptographerEngine::getPatternHandle(JNIEnv *env, jobject obj, bool activateDoc) {
	return (AIPatternHandle) getWrapperHandle(env, obj, activateDoc, "pattern");
}

AISwatchRef ScriptographerEngine::getSwatchHandle(JNIEnv *env, jobject obj, bool activateDoc) {
	return (AISwatchRef) getWrapperHandle(env, obj, activateDoc, "swatch");
}

AIGradientHandle ScriptographerEngine::getGradientHandle(JNIEnv *env, jobject obj, bool activateDoc) {
	return (AIGradientHandle) ScriptographerEngine::getWrapperHandle(env, obj, activateDoc, "gradient");
}

/**
 * Returns the wrapped AIFontKey of an object by assuming that it is an anchestor of Class Textfeatures and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
AIFontKey ScriptographerEngine::getFontHandle(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	AIFontKey font = (AIFontKey) getIntField(env, obj, fid_ai_NativeObject_handle);
	if (font == NULL)
		throw new StringException("Object is not wrapping a font key.");
	return font;
}

/**
 * Returns the AIDictionaryRef that contains the wrapped AIArtHandle, if any
 *
 * throws exceptions
 */
AIDictionaryRef ScriptographerEngine::getArtDictionaryHandle(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	return (AIDictionaryRef) gEngine->getIntField(env, obj, gEngine->fid_ai_Art_dictionaryRef);
}

/**
 * Returns the wrapped AIDocumentHandle of an object by assuming that it is an anchestor of Class Document and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
AIDocumentHandle ScriptographerEngine::getDocumentHandle(JNIEnv *env, jobject obj, bool activate) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	AIDocumentHandle document = (AIDocumentHandle) getIntField(env, obj, fid_ai_NativeObject_handle);
	if (document == NULL)
		throw new StringException("Object is not wrapping a document handle.");
	if (activate && document != gActiveDoc) {
		sAIDocumentList->Activate(document, false);
		gActiveDoc = document;
	}
	return document;
}

/**
 * Returns the wrapped AIDocumentViewHandle of an object by assuming that it is an anchestor of Class View and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
AIDocumentViewHandle ScriptographerEngine::getDocumentViewHandle(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	AIDocumentViewHandle view = (AIDocumentViewHandle) getIntField(env, obj, fid_ai_NativeObject_handle);
	if (view == NULL)
		throw new StringException("Object is not wrapping a view handle.");
	return view;
}

/**
 * Returns the wrapped AIToolHandle of an object by assuming that it is an anchestor of Class Tool and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
AIToolHandle ScriptographerEngine::getToolHandle(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	AIToolHandle tool = (AIToolHandle) getIntField(env, obj, fid_ai_NativeObject_handle);
	if (tool == NULL)
		throw new StringException("Object is not wrapping an tool handle.");
	return tool;
}

/**
 * Returns the wrapped AILiveEffectHandle of an object by assuming that it is an anchestor of Class LiveEffect and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
AILiveEffectHandle ScriptographerEngine::getLiveEffectHandle(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	AILiveEffectHandle effect = (AILiveEffectHandle) getIntField(env, obj, fid_ai_NativeObject_handle);
	if (effect == NULL)
		throw new StringException("Object is not wrapping an effect handle.");
	return effect;
}

/**
 * Returns the wrapped AIMenuItemHandle of an object by assuming that it is an anchestor of Class LiveEffect and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
AIMenuItemHandle ScriptographerEngine::getMenuItemHandle(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	AIMenuItemHandle item = (AIMenuItemHandle) getIntField(env, obj, fid_adm_NativeObject_handle);
	if (item == NULL)
		throw new StringException("Object is not wrapping a menu item handle.");
	return item;
}

/**
 * Returns the wrapped AIMenuGroup of an object by assuming that it is an anchestor of Class LiveEffect and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
AIMenuGroup ScriptographerEngine::getMenuGroupHandle(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	AIMenuGroup group = (AIMenuGroup) getIntField(env, obj, fid_adm_NativeObject_handle);
	if (group == NULL)
		throw new StringException("Object is not wrapping a menu group handle.");
	return group;
}

/**
 * Returns the wrapped TextFrameRef of an object by assuming that it is an anchestor of Class TextFrame and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ATE::TextFrameRef ScriptographerEngine::getTextFrameRef(JNIEnv *env, jobject obj, bool activateDoc) {
	AIArtHandle art = getArtHandle(env, obj, activateDoc);
	ATE::TextFrameRef frame = NULL;
	sAITextFrame->GetATETextFrame(art, &frame);
	return frame;
}

/**
 * Returns the wrapped TextRangeRef of an object by assuming that it is an anchestor of Class TextRange and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ATE::TextRangeRef ScriptographerEngine::getTextRangeRef(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	ATE::TextRangeRef range = (ATE::TextRangeRef) getIntField(env, obj, fid_ai_NativeObject_handle);
	if (range == NULL)
		throw new StringException("Object is not wrapping a text range handle.");
	return range;
}

/**
 * Returns the wrapped StoryRef of an object by assuming that it is an anchestor of Class Textstory and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ATE::StoryRef ScriptographerEngine::getStoryRef(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	ATE::StoryRef story = (ATE::StoryRef) getIntField(env, obj, fid_ai_NativeObject_handle);
	if (story == NULL)
		throw new StringException("Object is not wrapping a text story handle.");
	return story;
}

/**
 * Returns the wrapped CharFeaturesRef of an object by assuming that it is an anchestor of Class CharacterStyle and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ATE::CharFeaturesRef ScriptographerEngine::getCharFeaturesRef(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	ATE::CharFeaturesRef features = (ATE::CharFeaturesRef) getIntField(env, obj, fid_ai_NativeObject_handle);
	if (features == NULL)
		throw new StringException("Object is not wrapping a character style handle.");
	return features;
}

/**
 * Returns the wrapped ParaFeaturesRef of an object by assuming that it is an anchestor of Class ParagraphStyle and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ATE::ParaFeaturesRef ScriptographerEngine::getParaFeaturesRef(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	ATE::ParaFeaturesRef features = (ATE::ParaFeaturesRef) getIntField(env, obj, fid_ai_NativeObject_handle);
	if (features == NULL)
		throw new StringException("Object is not wrapping a paragraph style handle.");
	return features;
}

jobject ScriptographerEngine::wrapTextRangeRef(JNIEnv *env, ATE::TextRangeRef range) {
	// we need to increase the ref count here. this is decreased again in TextRange.finalize
	ATE::sTextRange->AddRef(range);
	return newObject(env, cls_ai_TextRange, cid_ai_TextRange, (jint) range, (jint) gActiveDoc);
}

/**
 * Wraps the handle in a java object. see the Java function Art.wrapArtHandle to see how 
 * the cashing of already wrapped objects is handled.
 *
 * throws exceptions
 */
jobject ScriptographerEngine::wrapArtHandle(JNIEnv *env, AIArtHandle art, AIDictionaryRef dictionary) {
	JNI_CHECK_ENV
	if (art == NULL)
		return NULL;
	short type = -1;
	AITextFrameType textType = kUnknownTextType;
	ASBoolean isLayer;
	if (sAIArt->GetArtType(art, &type) || sAIArt->IsArtLayerGroup(art, &isLayer))
		throw new StringException("Cannot determine the art object's type");
	if (isLayer) {
		// self defined type for layer groups
		type = com_scriptographer_ai_Art_TYPE_LAYER;
	} else if (type == kTextFrameArt) {
		// determine text type as well
		sAITextFrame->GetType(art, &textType);
	} else if (type == kPluginArt) {
		// TODO: add handle of special types
#if kPluginInterfaceVersion >= kAI12
		if (sAITracing->IsTracing(art))
			type = com_scriptographer_ai_Art_TYPE_TRACING;
#endif
	}
	
	if (dictionary != NULL) // increase reference counter. It's decreased in finalize
		sAIDictionary->AddRef(dictionary);
	
	// store the art object's initial handle value in its own dictionary. see selectionChanged for more explanations
	bool wrapped = false;
	AIDictionaryRef artDict;
	if (!sAIArt->GetDictionary(art, &artDict)) {
		wrapped = sAIDictionary->IsKnown(artDict, m_artHandleKey);
		sAIDictionary->SetIntegerEntry(artDict, m_artHandleKey, (ASInt32) art);
		sAIDictionary->Release(artDict);
	}
	return callStaticObjectMethod(env, cls_ai_Art, mid_ai_Art_wrapHandle, (jint) art, (jshort) type, (jint) textType, (jint) gActiveDoc, (jint) dictionary, (jboolean) wrapped);
}

void ScriptographerEngine::changeArtHandle(JNIEnv *env, jobject artObject, AIArtHandle art, AIDictionaryRef dictionary, AIDocumentHandle doc) {
	callVoidMethod(env, artObject, mid_ai_Art_changeHandle, (jint) art, (jint) dictionary, (jint) doc);
}

jobject ScriptographerEngine::getIfWrapped(JNIEnv *env, AIArtHandle art) {
	return callStaticObjectMethod(env, cls_ai_Art, mid_ai_Art_getIfWrapped, (jint) art);
}

jobject ScriptographerEngine::wrapLayerHandle(JNIEnv *env, AILayerHandle layer) {
	// layer handles are not used in java as Layer is derived from Art. Allways use the first invisible
	// Art group in the layer that contains everything (even in AI, layer seems only be a wrapper around
	// an art group:
	AIArtHandle art;
	if (sAIArt->GetFirstArtOfLayer(layer, &art))
		throw new StringException("Cannot get layer art");
	return callStaticObjectMethod(env, cls_ai_Art, mid_ai_Art_wrapHandle, (jint) art, (jint) com_scriptographer_ai_Art_TYPE_LAYER, (jint) kUnknownTextType, (jint) gActiveDoc, 0);
}

/**
 * Wraps the handle in a java object. see the Java function MenuItem.wrapHandle to see how 
 * the cashing of already wrapped objects is handled.
 *
 * throws exceptions
 */
jobject ScriptographerEngine::wrapMenuItemHandle(JNIEnv *env, AIMenuItemHandle item) {
	JNI_CHECK_ENV
	AIMenuGroup group;
#if kPluginInterfaceVersion < kAI12
	char *name, *groupName;
	if (!sAIMenu->GetMenuItemName(item, &name) &&
#else
	const char *name, *groupName;
	if (!sAIMenu->GetMenuItemKeyboardShortcutDictionaryKey(item, &name) &&
#endif
		!sAIMenu->GetItemMenuGroup(item, &group) &&
		!sAIMenu->GetMenuGroupName(group, &groupName)) {
		return callStaticObjectMethod(env, cls_adm_MenuItem, mid_adm_MenuItem_wrapHandle,
			(jint) item, convertString(env, name),
			(jint) group, convertString(env, groupName)
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
//		long t = getNanoTime();

		AIArtHandle **matches;
		long numMatches;
		ASErr error = sAIMatchingArt->GetSelectedArt(&matches, &numMatches);
		if (error) return error;
		if (numMatches > 0) {
			// pass an array of twice the size to the java side:
			// i + 0 contains the current handle
			// i + 1 contains the previous handle, in case the object was wrapped earlier and its handle was changed 
			// in the meantime. like this, the java side can update its wrappers. the previous handle's value is stored
			// in the art's dictionary which is copied over to the knew instance by illustrator automatically.
			
			// new instances are created by illustrator when objects are moved, rotated with the rotate tool, etc.
			// why it's doing this i don't know...

			// the maximum possible amount of handles is the amount of matches x 2, as each wrapped art object needs two handles:
			jint *handles = new jint[numMatches * 2];
			int count = 0;
			for (int i = 0; i < numMatches; i++) {
				AIArtHandle art = (*matches)[i];
				AIArtHandle prevArt = NULL;
				// see if the object already was wrapped earlier and its handle was changed due to manipulations by the user
				// if that's the case, let the java side know about the handle change
				AIDictionaryRef artDict;
				if (!sAIArt->GetDictionary(art, &artDict)) {
					if (sAIDictionary->IsKnown(artDict, m_artHandleKey)) {
						bool changed = true;
						if (!sAIDictionary->GetIntegerEntry(artDict, m_artHandleKey, (ASInt32 *) &prevArt) && prevArt == art) {
							prevArt = NULL;
							changed = false;
						}
						// set the new handle:
						if (changed)
							sAIDictionary->SetIntegerEntry(artDict, m_artHandleKey, (ASInt32) art);
						// only if the m_artHandleKey was set before, this object was wrapped
						handles[count++] = (jint) art;
						handles[count++] = (jint) prevArt;
					}
					sAIDictionary->Release(artDict);
				}
			}

			sAIMDMemory->MdMemoryDisposeHandle((void **) matches);

			jintArray artHandles = env->NewIntArray(count);
			env->SetIntArrayRegion(artHandles, 0, count, (jint *) handles);
			delete handles;
			
			callStaticVoidMethod(env, cls_ai_Art, mid_ai_Art_updateIfWrapped, artHandles);
		}
//		println(env, "%i", (getNanoTime() - t) / 1000000);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

/**
 * AI Tool
 *
 */

ASErr ScriptographerEngine::toolHandleEvent(const char * selector, AIToolMessage *message) {
	JNIEnv *env = getEnv();
	try {
		jint cursorId = callStaticIntMethod(env, cls_ai_Tool, mid_ai_Tool_onHandleEvent, (jint) message->tool, convertString(env, selector), (jfloat) message->cursor.h, (jfloat) message->cursor.v, (jint) message->pressure);
		if (cursorId)
			gPlugin->setCursor(cursorId);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
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
	if (contextObj != NULL && env->IsInstanceOf(contextObj, cls_Number)) {
		return (AILiveEffectParamContext) callIntMethod(env, contextObj, mid_Number_intValue);
	}
	return NULL;
}

ASErr ScriptographerEngine::liveEffectEditParameters(AILiveEffectEditParamMessage *message) {
	JNIEnv *env = getEnv();
	try {
		jobject map = getLiveEffectParameters(env, message->parameters);
		callStaticVoidMethod(env, cls_ai_LiveEffect, mid_ai_LiveEffect_onEditParameters,
				(jint) message->effect, map, (jint) message->context, (jboolean) message->allowPreview);
		sAILiveEffect->UpdateParameters(message->context);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

ASErr ScriptographerEngine::liveEffectCalculate(AILiveEffectGoMessage *message) {
	JNIEnv *env = getEnv();
	try {
		jobject map = getLiveEffectParameters(env, message->parameters);
		// TODO: setting art to something else seems to crash!
		message->art = (AIArtHandle) callStaticIntMethod(env, cls_ai_LiveEffect, mid_ai_LiveEffect_onCalculate,
				(jint) message->effect, map, wrapArtHandle(env, message->art));
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
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
		message->typeMask = callStaticIntMethod(env, cls_ai_LiveEffect, mid_ai_LiveEffect_onGetInputType,
				(jint) message->effect, map, wrapArtHandle(env, message->inputArt));
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

	
/**
 * AI MenuItem
 *
 */

ASErr ScriptographerEngine::menuItemExecute(AIMenuMessage *message) {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_adm_MenuItem, mid_adm_MenuItem_onSelect, (jint) message->menuItem);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

ASErr ScriptographerEngine::menuItemUpdate(AIMenuMessage *message, long inArtwork, long isSelected, long isTrue) {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_adm_MenuItem, mid_adm_MenuItem_onUpdate,
				(jint) message->menuItem, (jint) inArtwork, (jint) isSelected, (jint) isTrue);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

/**
 * AI Timer
 *
 */

ASErr ScriptographerEngine::timerExecute(AITimerMessage *message) {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_ai_Timer, mid_ai_Timer_onExecute, (jint) message->timer);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

/**
 * AI Annotator
 *
 */

ASErr ScriptographerEngine::annotatorDraw(AIAnnotatorMessage *message) {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_ai_Annotator, mid_ai_Annotator_onDraw,
				(jint) message->annotator, (jint) message->port, (jint) message->view);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

ASErr ScriptographerEngine::annotatorInvalidate(AIAnnotatorMessage *message) {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_ai_Annotator, mid_ai_Annotator_onInvalidate, (jint) message->annotator);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
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
	callVoidMethodReport(env, handler, mid_adm_NotificationHandler_onNotify_String, env->NewStringUTF(type));
}

void ScriptographerEngine::callOnDestroy(jobject handler) {
	callVoidMethodReport(NULL, handler, mid_adm_NotificationHandler_onNotify_int,
				(jint) com_scriptographer_adm_Notifier_NOTIFIER_DESTROY);
}

/**
 *
 *
 */
bool ScriptographerEngine::callOnTrack(jobject handler, ADMTrackerRef tracker) {
	JNIEnv *env = getEnv();
	try {
		jobject trackerObj = getObjectField(env, handler, fid_adm_NotificationHandler_tracker);
		ADMPoint pt;
		sADMTracker->GetPoint(tracker, &pt);
		return callBooleanMethod(env, trackerObj, mid_adm_Tracker_onTrack, handler,
				(jint) tracker, (jint) sADMTracker->GetAction(tracker),
				(jint) sADMTracker->GetModifiers(tracker), pt.h, pt.v,
				(jint) sADMTracker->GetMouseState(tracker),
				(jchar) sADMTracker->GetVirtualKey(tracker),
				(jint) sADMTracker->GetCharacter(tracker),
				(jlong) sADMTracker->GetTime(tracker));
	} EXCEPTION_CATCH_REPORT(env);
	return true;
}

/**
 *
 *
 */
void ScriptographerEngine::callOnDraw(jobject handler, ADMDrawerRef drawer) {
	JNIEnv *env = getEnv();
	try {
		jobject drawerObj = getObjectField(env, handler, fid_adm_NotificationHandler_drawer);
		setIntField(env, drawerObj, fid_adm_NativeObject_handle, (jint)drawer);
		callVoidMethod(env, handler, mid_adm_NotificationHandler_onDraw, drawerObj);
	} EXCEPTION_CATCH_REPORT(env);
}

ASErr ScriptographerEngine::displayAbout() {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_ScriptographerEngine, mid_ScriptographerEngine_onAbout);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

/**
 * Returns the wrapped ADMDialogRef of an object by assuming that it is an anchestor of Class Dialog and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ADMDialogRef ScriptographerEngine::getDialogRef(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	ADMDialogRef dlg = (ADMDialogRef) getIntField(env, obj, fid_adm_NativeObject_handle);
	if (dlg == NULL)
		throw new StringException("Object is not wrapping a dialog ref.");
	return dlg;
}

/**
 * Returns the wrapped ADMDrawerRef of an object by assuming that it is an anchestor of Class Drawer and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ADMDrawerRef ScriptographerEngine::getDrawerRef(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	ADMDrawerRef drawer = (ADMDrawerRef) getIntField(env, obj, fid_adm_NativeObject_handle);
	if (drawer == NULL)
		throw new StringException("Object is not wrapping a drawer ref.");
	return drawer;
}

/**
 * Returns the wrapped ADMTrackerRef of an object by assuming that it is an anchestor of Class Tracker and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ADMTrackerRef ScriptographerEngine::getTrackerRef(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	ADMTrackerRef tracker = (ADMTrackerRef) getIntField(env, obj, fid_adm_NativeObject_handle);
	if (tracker == NULL)
		throw new StringException("Object is not wrapping a tracker ref.");
	return tracker;
}

/**
 * Returns the wrapped ADMImageRef of an object by assuming that it is an anchestor of Class Image and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ADMImageRef ScriptographerEngine::getImageRef(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	ADMImageRef image = (ADMImageRef) getIntField(env, obj, fid_adm_NativeObject_handle);
	if (image == NULL)
		throw new StringException("Object is not wrapping an image ref.");
	return image;
}

/**
 * Returns the wrapped ADMItemRef of an object by assuming that it is an anchestor of Class Item and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ADMItemRef ScriptographerEngine::getItemRef(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	ADMItemRef item = (ADMItemRef) getIntField(env, obj, fid_adm_NativeObject_handle);
	if (item == NULL) {
		// for HierarchyLists it could be that the user wants to call item functions
		// on a child list. report that this can only be called on the root list:
		if (env->IsInstanceOf(obj, cls_adm_HierarchyList)) {
			throw new StringException("This function can only be called on the root hierarchy list.");
		} else {
			throw new StringException("Object is not wrapping an item ref.");
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
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	ADMListRef list = (ADMListRef) getIntField(env, obj, fid_adm_ListItem_listHandle);
	if (list == NULL) throw new StringException("Object is not wrapping a list ref.");
	return list;
}

/**
 * Returns the wrapped ADMHierarchyListRef of an object by assuming that it is an anchestor of Class ListItem and
 * accessing its field 'listHandle':
 *
 * throws exceptions
 */
ADMHierarchyListRef ScriptographerEngine::getHierarchyListRef(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	ADMHierarchyListRef list = (ADMHierarchyListRef) getIntField(env, obj, fid_adm_ListItem_listHandle);
	if (list == NULL) throw new StringException("Object is not wrapping a hierarchy list ref.");
	return list;
}

/**
 * Returns the wrapped ADMEntryRef of an object by assuming that it is an anchestor of Class Entry and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ADMEntryRef ScriptographerEngine::getListEntryRef(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	ADMEntryRef entry = (ADMEntryRef) getIntField(env, obj, fid_adm_NativeObject_handle);
	if (entry == NULL) throw new StringException("Object is not wrapping a list entry ref.");
	return entry;
}

/**
 * Returns the wrapped ADMListEntryRef of an object by assuming that it is an anchestor of Class Entry and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ADMListEntryRef ScriptographerEngine::getHierarchyListEntryRef(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	ADMListEntryRef entry = (ADMListEntryRef) getIntField(env, obj, fid_adm_NativeObject_handle);
	if (entry == NULL) throw new StringException("Object is not wrapping a hierarchy list entry ref.");
	return entry;
}

jobject ScriptographerEngine::getDialogObject(ADMDialogRef dlg) {
	jobject obj = NULL;
	if (dlg != NULL && sADMDialog != NULL) {
		obj = (jobject) sADMDialog->GetUserData(dlg);
		if (obj == NULL) throw new StringException("Dialog does not have a wrapper object.");
	}
	return obj;
}

jobject ScriptographerEngine::getItemObject(ADMItemRef item) {
	jobject obj = NULL;
	if (item != NULL && sADMItem != NULL) {
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
	if (list != NULL && sADMList != NULL) {
		obj = (jobject) sADMList->GetUserData(list);
		if (obj == NULL) throw new StringException("List does not have a wrapper object.");
	}
	return obj;
}

jobject ScriptographerEngine::getListObject(ADMHierarchyListRef list) {
	jobject obj = NULL;
	if (list != NULL && sADMHierarchyList != NULL) {
		obj = (jobject) sADMHierarchyList->GetUserData(list);
		if (obj == NULL) throw new StringException("Hierarchy list does not have a wrapper object.");
	}
	return obj;
}

jobject ScriptographerEngine::getListEntryObject(ADMEntryRef entry) {
	jobject obj = NULL;
	if (entry != NULL && sADMEntry != NULL) {
		obj = (jobject) sADMEntry->GetUserData(entry);
		if (obj == NULL) throw new StringException("Entry does not have a wrapper object.");
	}
	return obj;
}

jobject ScriptographerEngine::getListEntryObject(ADMListEntryRef entry) {
	jobject obj = NULL;
	if (entry != NULL && sADMListEntry != NULL) {
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
	if (m_javaVM != NULL) {
		JNIEnv *env;
		m_javaVM->AttachCurrentThread((void **)&env, NULL);
		return env;
	} else {
		return NULL;
	}
}

/**
 * Creates a Java String from a given C-String.
 * TODO: The non depreceated version that takes an encoding parameter should be used in the future.
 *
 * throws exceptions
 */
jstring ScriptographerEngine::convertString(JNIEnv *env, const char *str) {
	if (!str)
		return NULL;
	JNI_CHECK_ENV
	int len = strlen(str);
	jbyteArray bytes = env->NewByteArray(len);
	if (bytes == NULL) throw new JThrowableClassException(cls_OutOfMemoryError);
	env->SetByteArrayRegion(bytes, 0, len, (jbyte *) str);
//	jstring result = (jstring) env->functions->NewObject(env, cls_String, cid_String, bytes);
	jstring res = (jstring) env->NewObject(cls_String, cid_String, bytes);
	env->DeleteLocalRef(bytes);
	if (res == NULL) EXCEPTION_CHECK(env);
	return res;
}

/**
 * Creates a Java String from a given Pascal-String.
 * Caution: modifies str!
 *
 * throws exceptions
 */
jstring ScriptographerEngine::convertString(JNIEnv *env, unsigned char *str) {
	return convertString(env, gPlugin->fromPascal(str, (char *) str));
}

/**
 * Creates a C-String from a given Java String. 
 * TODO: The non depreceated version that takes an encoding parameter should be used in the future.
 *
 * throws exceptions
 */
char *ScriptographerEngine::convertString(JNIEnv *env, jstring jstr, int minLength) {
	if (!jstr)
		return NULL;
	JNI_CHECK_ENV
	jbyteArray bytes = (jbyteArray)callObjectMethod(env, jstr, mid_String_getBytes);
	jint len = env->GetArrayLength(bytes);
	jint length = len + 1;
	if (length < minLength)
		length = minLength;
	char *result = new char[length];
	if (result == NULL) {
		env->DeleteLocalRef(bytes);
		throw new JThrowableClassException(cls_OutOfMemoryError);
	}
	env->GetByteArrayRegion(bytes, 0, len, (jbyte *) result);
	result[len] = 0; // NULL-terminate
	env->DeleteLocalRef(bytes);
	return result;
}

jstring ScriptographerEngine::convertString(JNIEnv *env, const ASUnicode *str, int length) {
	JNI_CHECK_ENV
	if (length < 0) {
		// find length
		length = 0;
		while (str[length] != 0)
			length++;
	}
	jstring res = env->NewString((jchar *) str, length);
	if (res == NULL) EXCEPTION_CHECK(env);
	return res;
}

ASUnicode *ScriptographerEngine::convertString_ASUnicode(JNIEnv *env, jstring jstr) {
	JNI_CHECK_ENV
	int length = env->GetStringLength(jstr);
	ASUnicode *chars = new ASUnicode[length + 1];
	env->GetStringRegion(jstr, 0, length, (jchar *) chars);
	// make it null-determined:
	chars[length] = 0;
	return chars;
}

#if kPluginInterfaceVersion < kAI12

/**
 * Creates a Java String from a given Pascal-String.
 * Only supported in CS and bellow
 */
unsigned char *ScriptographerEngine::convertString_Pascal(JNIEnv *env, jstring jstr, int minLength) {
	char *str = convertString(env, jstr, minLength);
	return gPlugin->toPascal(str, (unsigned char*) str);
}

#else

/**
 * Creates a Java String from a given UTF-16-String.
 * Only supported in CS2 and above
 */
jstring ScriptographerEngine::convertString(JNIEnv *env, ai::UnicodeString &str) {
	JNI_CHECK_ENV
	const ASUnicode *buffer;
	int len = str.utf_16(buffer);
	jstring res = env->NewString(buffer, len);
	if (res == NULL) EXCEPTION_CHECK(env);
	return res;
}

ai::UnicodeString ScriptographerEngine::convertString_UnicodeString(JNIEnv *env, jstring jstr) {
	JNI_CHECK_ENV
	const jchar *chars = env->GetStringCritical(jstr, NULL);
	ai::UnicodeString str(chars, env->GetStringLength(jstr));
	env->ReleaseStringCritical(jstr, chars); 
	return str;
}

#endif

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
	jclass cls = NULL;
	if (cls_Loader != NULL) {
		// Try loading using the URL classloader first:
		cls = (jclass) callStaticObjectMethod(env, cls_Loader, mid_Loader_loadClass, env->NewStringUTF(name));
		if (cls == NULL) {
			env->ExceptionClear();
			// Fallback to loading using JNI throught the default classloader:
			cls = env->FindClass(name);
		}
	} else {
		// Load using JNI throught the default classloader:
		cls = env->FindClass(name);
	}
	if (cls == NULL) EXCEPTION_CHECK(env);
	return cls;
}

jclass ScriptographerEngine::loadClass(JNIEnv *env, const char *name) {
	JNI_CHECK_ENV
	jclass cls = findClass(env, name);
	jclass res = (jclass) env->NewGlobalRef(cls);
	env->DeleteLocalRef(cls);
	return res;
}

jmethodID ScriptographerEngine::getMethodID(JNIEnv *env, jclass cls, const char *name, const char *signature) {
	JNI_CHECK_ENV
	jmethodID res = env->GetMethodID(cls, name, signature);
	if (res == NULL) EXCEPTION_CHECK(env);
	return res;
}

jmethodID ScriptographerEngine::getStaticMethodID(JNIEnv *env, jclass cls, const char *name, const char *signature) {
	JNI_CHECK_ENV
	jmethodID res = env->GetStaticMethodID(cls, name, signature);
	if (res == NULL) EXCEPTION_CHECK(env);
	return res;
}

jmethodID ScriptographerEngine::getConstructorID(JNIEnv *env, jclass cls, const char *signature) {
	return getMethodID(env, cls, "<init>", signature);
}

jfieldID ScriptographerEngine::getFieldID(JNIEnv *env, jclass cls, const char *name, const char *signature) {
	JNI_CHECK_ENV
	jfieldID res = env->GetFieldID(cls, name, signature);
	if (res == NULL) EXCEPTION_CHECK(env);
	return res;
}

jfieldID ScriptographerEngine::getStaticFieldID(JNIEnv *env, jclass cls, const char *name, const char *signature) {
	JNI_CHECK_ENV
	jfieldID res = env->GetStaticFieldID(cls, name, signature);
	if (res == NULL) EXCEPTION_CHECK(env);
	return res;
}

jobject ScriptographerEngine::newObject(JNIEnv *env, jclass cls, jmethodID ctor, ...) {
	JNI_CHECK_ENV
	JNI_ARGS_BEGIN(ctor)
//	jobject res = env->functions->NewObjectV(env, cls, ctor, args);
	jobject res = env->NewObjectV(cls, ctor, args);
	JNI_ARGS_END
	if (res == NULL) EXCEPTION_CHECK(env);
	return res;
}

// declare macro functions now:

JNI_DEFINE_GETFIELD_FUNCTIONS
JNI_DEFINE_SETFIELD_FUNCTIONS
JNI_DEFINE_GETSTATICFIELD_FUNCTIONS
JNI_DEFINE_CALLMETHOD_FUNCTIONS