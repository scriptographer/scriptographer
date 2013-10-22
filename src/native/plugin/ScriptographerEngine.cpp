/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 */

#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "ScriptographerPlugin.h"
#include "AppContext.h"
#include "com_scriptographer_ScriptographerEngine.h"
// for com_scriptographer_ai_Item_TYPE_LAYER:
#include "com_scriptographer_ai_Item.h"
#include "uiGlobals.h"
#include "aiGlobals.h"

#ifdef WIN_ENV
#include "loadJava.h"
#endif

#if defined(MAC_ENV)
#define MAC_THREAD
#endif

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
	// Now tell the caller that the engine can be deleted, before DestroyJavaVM
	// is called, which may block the current thread until the end of the app.
	MPNotifyQueue(m_responseQueue, NULL, NULL, NULL);
	// Now exit, and destroy the JavaVM. 
	// exit();
}

#endif // MAC_THREAD

ScriptographerEngine::ScriptographerEngine(const char *pluginPath) {
	m_initialized = false;
	m_javaVM = NULL;
	m_pluginPath = new char[strlen(pluginPath) + 1];
	strcpy(m_pluginPath, pluginPath);
	ScriptographerException *exc = NULL;

#ifdef MAC_THREAD
	if(MPLibraryIsLoaded()) {
		MPCreateQueue(&m_requestQueue);
		MPCreateQueue(&m_responseQueue);
		MPCreateTask((TaskProc)::javaThread, this, 0, NULL, NULL, NULL, 0, NULL); 
		// Now wait for the javaThread to finish initialization
		// exceptions that happen in the javaThread are passed through to this
		// thread in order to display the error code:
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
		gPlugin->log("Unable to create ScriptographerEngine: %s", exc->toString(env));
		exc->report(env);
		delete exc;
		throw new StringException("Unable to create ScriptographerEngine.");
	}
	m_rulerOrigin.h = m_rulerOrigin.v = 0;
	m_artboardOrigin.h = m_artboardOrigin.v = 0;
	m_topDownCoordinates = true;
	gEngine = this;
}

ScriptographerEngine::~ScriptographerEngine() {
	callStaticVoidMethodReport(NULL,
			cls_ScriptographerEngine, mid_ScriptographerEngine_destroy);
	gEngine = NULL;
	delete m_pluginPath;
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
	JavaVMOption m_options[128];
	int m_numOptions;
	bool m_checkSize;
	int m_stackSize;
	int m_maxPermSize;
	int m_minHeapSize;
	int m_maxHeapSize;

public:
	JVMOptions(bool checkSize, int maxPermSize = 0, int maxHeapSize = 0) {
		m_numOptions = 0;
		memset(m_options, 0, sizeof(m_options));
		m_checkSize = checkSize;
		// Set default sizes 
		m_stackSize = 0;
		m_maxPermSize = maxPermSize * 1024 * 1024;
		m_minHeapSize = 0;
		m_maxHeapSize = maxHeapSize * 1024 * 1024;
	}

	~JVMOptions() {
		for (int i = 0; i < m_numOptions; i++) {
			delete[] m_options[i].optionString;
		}
	}

	void add(const char *str, ...) {
		char *text = new char[2048];
		va_list args;
		va_start(args, str);
		vsprintf(text, str, args);
		va_end(args);
		if (strlen(text) > 0) {
			bool isSize = false;
			if (strncmp(text, "-Xss", 4) == 0) {
				m_stackSize = parseSize(&text[4]);
				isSize = true;
			} else if (strncmp(text, "-Xms", 4) == 0) {
				m_minHeapSize = parseSize(&text[4]);
				isSize = true;
			} else if (strncmp(text, "-Xmx", 4) == 0) {
				m_maxHeapSize = parseSize(&text[4]);
				isSize = true;
			} else if (strncmp(text, "-XX:MaxPermSize=", 16) == 0) {
				m_maxPermSize = parseSize(&text[16]);
				isSize = true;
			}
			if (!m_checkSize || !isSize) {
				gPlugin->log("JVM Option: %s", text);
				m_options[m_numOptions++].optionString = text;
			}
		}
	}

	int parseSize(char* str) {
		char* end;
		int size = strtol(str, &end, 0);
		switch (end[0]) {
		case 'b':
		case 'B':
			break;
		case 'k':
		case 'K':
			size *= 1024;
			break;
		case 'm':
		case 'M':
			size *= 1024 * 1024;
			break;
		default:
			break;
		}
		return size;
	}

	void fillArgs(JavaVMInitArgs *args) {
#ifdef WIN_ENV
		if (m_checkSize) {
			// Depending on the situation, the JVM seems to require some extra
			// amount of space. 16mb seems to be a good guess ????
			m_maxHeapSize = getMaxHeapAvailable(m_maxPermSize, m_maxHeapSize,
					16 * 1024 * 1024);
			// Turn off now so the add-calls below actually add the sizes
			m_checkSize = false;
		}
#endif // WIN_ENV
		if (m_stackSize > 0)
			add("-Xss%im", m_stackSize / (1024 * 1024));
		if (m_minHeapSize > 0)
			add("-Xms%im", m_minHeapSize / (1024 * 1024));
		if (m_maxHeapSize > 0)
			add("-Xmx%im", m_maxHeapSize / (1024 * 1024));
		if (m_maxPermSize > 0)
			add("-XX:MaxPermSize=%im", m_maxPermSize / (1024 * 1024));
		args->options = m_options;
		args->nOptions = m_numOptions;
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

	char javaPath[512];
	sprintf(javaPath, "%s" PATH_SEP_STR "Core" PATH_SEP_STR "Java" PATH_SEP_STR,
			m_pluginPath);

	// Initialize args
	JavaVMInitArgs args;
	args.version = JNI_VERSION_1_4;

	// Define options
#ifdef WIN_ENV
	JVMOptions options(true, 64, 128);
#else // !WIN_ENV
	JVMOptions options(false);
#endif // !WIN_ENV
	// Only add the loader to the classpath, the rest is done in java:
	options.add("-Djava.class.path=%sloader.jar", javaPath);
	options.add("-Djava.library.path=%slib", javaPath);

#ifdef MAC_ENV
#ifdef MAC_THREAD
	// Start headless, in order to avoid conflicts with AWT and Illustrator
	options.add("-Djava.awt.headless=true");
#else // !MAC_THREAD
	options.add("-XstartOnFirstThread");
#endif // !MAC_THREAD
	// Use the carbon line separator instead of the unix one on mac:
	options.add("-Dline.separator=\r");
#endif // MAC_ENV
	// Read ini file and add the options here
	char buffer[512];
	sprintf(buffer, "%sjvm.ini", javaPath);
	FILE *file = fopen(buffer, "rt");
	if (file != NULL) {
		while (fgets(buffer, sizeof(buffer), file)) {
			char *str = buffer;
			// Trim new line and white space at beginning and the end of the
			// line:
			while (isspace(*str) && str != '\0') str++;
			if (*str == '#') continue; // skip comments
			int pos = strlen(str) - 1;
			while (pos >= 0 && isspace(str[pos])) str[pos--] = '\0';
			options.add(str);
		}
	}

	options.fillArgs(&args);
	args.ignoreUnrecognized = true;
	JNIEnv *env = NULL;
	// Create the JVM
	jint res = createJavaVM(&m_javaVM, (void **) &env, (void *) &args);
	if (res < 0) {
		gPlugin->log("Error creataing Java VM: %i", res);
		throw new StringException("Unable to create Java VM.");
	}
	cls_Loader = env->FindClass("com/scriptographer/loader/Loader");
	if (cls_Loader == NULL)
		throw new StringException("Unable to load loader.jar. Make sure that the java folder was copied together with the Scriptographer plugin.");
	mid_Loader_init = getStaticMethodID(env, cls_Loader, "init", "(Ljava/lang/String;)V");
	mid_Loader_reload = getStaticMethodID(env, cls_Loader, "reload", "()Ljava/lang/String;");
	mid_Loader_loadClass = getStaticMethodID(env, cls_Loader, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
	callStaticObjectMethodReport(env, cls_Loader, mid_Loader_init, env->NewStringUTF(m_pluginPath));
	
	// Initialize reflection. This retrieves references to all the classes,
	// fields and methods that are accessed from native code. Since JSE 1.6,
	// this cannot be called after registerNatives, since this would somehow
	// clear the internal native link tables. Also, registerNatives needs yet
	// another workaround to get 1.6 to work: By calling a reflection method,
	// e.g. getConstructors, the classes are pulled in fully, avoiding yet
	// another clearing of the link tables at a later point. (e.g. for classes
	// that are not loaded in initReflection)
	initReflection(env);
	// Link the native functions to the java functions. The code for this is in
	// registerNatives.cpp, which is automatically generated from the JNI header
	// files by jni.js
	registerNatives(env);
}

void ScriptographerEngine::exit() {
	m_javaVM->DetachCurrentThread();
	m_javaVM->DestroyJavaVM();
}

/**
 * Initializes the engine, e.g. activates console redirection and similar things.
 * To be called when all the underlying native structures are initiialized.
 */
void ScriptographerEngine::initEngine() {
	JNIEnv *env = getEnv();
	try {
		// create the art handle key in which the art object's original handle
		// value is stored as this value is used to lookup wrappers on the java
		// side, this needs to be passed in onSelectionChanged, in case the
		// art's handle was changed due to manipulations the java wrappers can
		// then be updated accordingly
		
		// According Adobe: note that entries whose keys are prefixed with the
		// character '-' are considered to be temporary entries which are not
		// saved to file.
		m_artHandleKey = sAIDictionary->Key("-scriptographer-art-handle");
		m_docReflowKey = sAIDictionary->Key("-scriptographer-doc-reflow-suspended");
		
		callStaticVoidMethod(env, cls_ScriptographerEngine,
				mid_ScriptographerEngine_init, env->NewStringUTF(m_pluginPath));
		m_initialized = true;
		onStartup();
	} EXCEPTION_CATCH_REPORT(env);
}

/**
 * Reloads the engine through Loader.reload, which does the half of the work.
 * The other half is to remap the reflections and register the natives again.
 */
jstring ScriptographerEngine::reloadEngine() {
	JNIEnv *env = getEnv();
	jstring errors = (jstring) callStaticObjectMethodReport(env, cls_Loader,
			mid_Loader_reload);
	m_initialized = false;
	initReflection(env);
	registerNatives(env);
	initEngine();
	return errors;
}

ASErr ScriptographerEngine::onStartup() {
	return callOnHandleEvent(com_scriptographer_ScriptographerEngine_EVENT_APP_STARTUP);
}

ASErr ScriptographerEngine::onShutdown() {
	return callOnHandleEvent(com_scriptographer_ScriptographerEngine_EVENT_APP_SHUTDOWN);
}

/**
 * Loads the Java classes, and the method and field descriptors required for
 * Java reflection. Returns true on success, false on failure.
 */
void ScriptographerEngine::initReflection(JNIEnv *env) {
// JSE:
	cls_Object = loadClass(env, "java/lang/Object");
	mid_Object_toString = getMethodID(env, cls_Object, "toString", "()Ljava/lang/String;");
	mid_Object_equals = getMethodID(env, cls_Object, "equals", "(Ljava/lang/Object;)Z");

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
	mid_Number_doubleValue = getMethodID(env, cls_Number, "doubleValue", "()D");

	cls_Integer = loadClass(env, "java/lang/Integer");
	cid_Integer = getConstructorID(env, cls_Integer, "(I)V");

	cls_Float = loadClass(env, "java/lang/Float");
	cid_Float = getConstructorID(env, cls_Float, "(F)V");

	cls_Double = loadClass(env, "java/lang/Double");
	cid_Double = getConstructorID(env, cls_Double, "(D)V");

	cls_Boolean = loadClass(env, "java/lang/Boolean");
	cid_Boolean = getConstructorID(env, cls_Boolean, "(Z)V");
	mid_Boolean_booleanValue = getMethodID(env, cls_Boolean, "booleanValue", "()Z");

	cls_File = loadClass(env, "java/io/File");
	cid_File = getConstructorID(env, cls_File, "(Ljava/lang/String;)V");
	mid_File_getPath = getMethodID(env, cls_File, "getPath", "()Ljava/lang/String;");

	cls_Collection = loadClass(env, "java/util/Collection");
	mid_Collection_add = getMethodID(env, cls_Collection, "add", "(Ljava/lang/Object;)Z");

	cls_Map = loadClass(env, "java/util/Map");

	cls_ArrayList = loadClass(env, "java/util/ArrayList");
	cid_ArrayList = getConstructorID(env, cls_ArrayList, "()V");

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

// Scriptographer:
	cls_ScriptographerEngine = loadClass(env, "com/scriptographer/ScriptographerEngine");
	mid_ScriptographerEngine_init = getStaticMethodID(env, cls_ScriptographerEngine, "init", "(Ljava/lang/String;)V");
	mid_ScriptographerEngine_destroy = getStaticMethodID(env, cls_ScriptographerEngine, "destroy", "()V");
	mid_ScriptographerEngine_reportError = getStaticMethodID(env, cls_ScriptographerEngine, "reportError", "(Ljava/lang/Throwable;)V");
	mid_ScriptographerEngine_onHandleEvent = getStaticMethodID(env, cls_ScriptographerEngine, "onHandleEvent", "(I)V");
	mid_ScriptographerEngine_onHandleKeyEvent = getStaticMethodID(env, cls_ScriptographerEngine, "onHandleKeyEvent", "(IICI)Z");

	cls_ScriptographerException = loadClass(env, "com/scriptographer/ScriptographerException");

	cls_CommitManager = loadClass(env, "com/scriptographer/CommitManager");
	mid_CommitManager_commit = getStaticMethodID(env, cls_CommitManager, "commit", "(Ljava/lang/Object;)Z");

// AI:
	cls_ai_NativeObject = loadClass(env, "com/scriptographer/ai/NativeObject");
	fid_ai_NativeObject_handle = getFieldID(env, cls_ai_NativeObject, "handle", "I");

	cls_ai_DocumentObject = loadClass(env, "com/scriptographer/ai/DocumentObject");
	fid_ai_DocumentObject_document = getFieldID(env, cls_ai_DocumentObject, "document", "Lcom/scriptographer/ai/Document;");
	
	cls_ai_Document = loadClass(env, "com/scriptographer/ai/Document");
	mid_ai_Document_wrapHandle = getStaticMethodID(env, cls_ai_Document, "wrapHandle", "(I)Lcom/scriptographer/ai/Document;");
	mid_ai_Document_onClosed = getMethodID(env, cls_ai_Document, "onClosed", "()V");
	mid_ai_Document_onSelectionChanged = getMethodID(env, cls_ai_Document, "onSelectionChanged", "([III)V");
	mid_ai_Document_onUndo = getMethodID(env, cls_ai_Document, "onUndo", "(II)V");
	mid_ai_Document_onRedo = getMethodID(env, cls_ai_Document, "onRedo", "(II)V");
	mid_ai_Document_onClear = getMethodID(env, cls_ai_Document, "onClear", "([I)V");
	mid_ai_Document_onRevert = getMethodID(env, cls_ai_Document, "onRevert", "()V");

	cls_ai_Dictionary = loadClass(env, "com/scriptographer/ai/Dictionary");
	cid_ai_Dictionary = getConstructorID(env, cls_ai_Dictionary, "(Ljava/util/Map;)V");
	fid_ai_Dictionary_handle = getFieldID(env, cls_ai_Dictionary, "handle", "I");
	mid_ai_Dictionary_wrapHandle = getStaticMethodID(env, cls_ai_Dictionary, "wrapHandle", "(IILcom/scriptographer/ai/ValidationObject;)Lcom/scriptographer/ai/Dictionary;");
	mid_ai_Dictionary_setValidation = getMethodID(env, cls_ai_Dictionary, "setValidation", "(Lcom/scriptographer/ai/ValidationObject;)V");

	cls_ai_Tool = loadClass(env, "com/scriptographer/ai/Tool");
	cid_ai_Tool = getConstructorID(env, cls_ai_Tool, "(ILjava/lang/String;)V");
	mid_ai_Tool_onHandleEvent = getStaticMethodID(env, cls_ai_Tool, "onHandleEvent", "(ILjava/lang/String;DDII)I");

	cls_ai_Point = loadClass(env, "com/scriptographer/ai/Point");
	cid_ai_Point = getConstructorID(env, cls_ai_Point, "(DD)V");
	fid_ai_Point_x = getFieldID(env, cls_ai_Point, "x", "D");
	fid_ai_Point_y = getFieldID(env, cls_ai_Point, "y", "D");
	mid_ai_Point_set = getMethodID(env, cls_ai_Point, "set", "(DD)V");

	cls_ai_Rectangle = loadClass(env, "com/scriptographer/ai/Rectangle");
	cid_ai_Rectangle = getConstructorID(env, cls_ai_Rectangle, "(DDDD)V");
	fid_ai_Rectangle_x = getFieldID(env, cls_ai_Rectangle, "x", "D");
	fid_ai_Rectangle_y = getFieldID(env, cls_ai_Rectangle, "y", "D");
	fid_ai_Rectangle_width = getFieldID(env, cls_ai_Rectangle, "width", "D");
	fid_ai_Rectangle_height = getFieldID(env, cls_ai_Rectangle, "height", "D");
	mid_ai_Rectangle_set = getMethodID(env, cls_ai_Rectangle, "set", "(DDDD)V");

	cls_ai_Size = loadClass(env, "com/scriptographer/ai/Size");
	cid_ai_Size = getConstructorID(env, cls_ai_Size, "(DD)V");
	fid_ai_Size_width = getFieldID(env, cls_ai_Size, "width", "D");
	fid_ai_Size_height = getFieldID(env, cls_ai_Size, "height", "D");
	mid_ai_Size_set = getMethodID(env, cls_ai_Size, "set", "(DD)V");
	
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
	cid_ai_GradientColor = getConstructorID(env, cls_ai_GradientColor, "(ILcom/scriptographer/ai/Point;DDLcom/scriptographer/ai/Matrix;DD)V");
	mid_ai_GradientColor_set = getMethodID(env, cls_ai_GradientColor, "set", "(I)V");

	cls_ai_PatternColor = loadClass(env, "com/scriptographer/ai/PatternColor");
	cid_ai_PatternColor = getConstructorID(env, cls_ai_PatternColor, "(ILcom/scriptographer/ai/Matrix;)V");
	mid_ai_PatternColor_set = getMethodID(env, cls_ai_PatternColor, "set", "(I)V");
	
	cls_ai_Item = loadClass(env, "com/scriptographer/ai/Item");
	fid_ai_Item_version = getFieldID(env, cls_ai_Item, "version", "I");
	fid_ai_Item_dictionaryHandle = getFieldID(env, cls_ai_Item, "dictionaryHandle", "I");
	fid_ai_Item_dictionaryKey = getFieldID(env, cls_ai_Item, "dictionaryKey", "I");
	mid_ai_Item_wrapHandle = getStaticMethodID(env, cls_ai_Item, "wrapHandle", "(ISIIZZ)Lcom/scriptographer/ai/Item;");
	mid_ai_Item_getIfWrapped = getStaticMethodID(env, cls_ai_Item, "getIfWrapped", "(I)Lcom/scriptographer/ai/Item;");
	mid_ai_Item_changeHandle = getMethodID(env, cls_ai_Item, "changeHandle", "(IIZ)V");
	mid_ai_Item_commitIfWrapped = getStaticMethodID(env, cls_ai_Item, "commitIfWrapped", "(IZ)Z");
	mid_ai_Item_isValid = getMethodID(env, cls_ai_Item, "isValid", "()Z");

	cls_ai_ItemList = loadClass(env, "com/scriptographer/ai/ItemList");
	cid_ItemList = getConstructorID(env, cls_ai_ItemList, "()V");
	mid_ai_ItemList_add = getMethodID(env, cls_ai_ItemList, "add", "(Ljava/lang/Object;)Ljava/lang/Object;");

	cls_ai_Path = loadClass(env, "com/scriptographer/ai/Path");
	cls_ai_CompoundPath = loadClass(env, "com/scriptographer/ai/CompoundPath");
	cls_ai_TextItem = loadClass(env, "com/scriptographer/ai/TextItem");

	cls_ai_TextRange = loadClass(env, "com/scriptographer/ai/TextRange");
	cid_ai_TextRange = getConstructorID(env, cls_ai_TextRange, "(II)V");
	fid_ai_TextRange_glyphRuns = getFieldID(env, cls_ai_TextRange, "glyphRuns", "I");
	
	cls_ai_TextStory = loadClass(env, "com/scriptographer/ai/TextStory");

	cls_ai_PathStyle = loadClass(env, "com/scriptographer/ai/PathStyle");
	
	mid_PathStyle_init = getMethodID(env, cls_ai_PathStyle, "init", "(Lcom/scriptographer/ai/Color;ZSLcom/scriptographer/ai/Color;ZSFSSFF[FSSIF)V");

	cls_ai_FillStyle = loadClass(env, "com/scriptographer/ai/FillStyle");

	cid_ai_FillStyle = getConstructorID(env, cls_ai_FillStyle, "(Lcom/scriptographer/ai/Color;ZS)V");
	mid_ai_FillStyle_init = getMethodID(env, cls_ai_FillStyle, "init", "(Lcom/scriptographer/ai/Color;ZS)V");
	mid_ai_FillStyle_initNative = getMethodID(env, cls_ai_FillStyle, "initNative", "(I)V");
	
	cls_ai_StrokeStyle = loadClass(env, "com/scriptographer/ai/StrokeStyle");
	cid_ai_StrokeStyle = getConstructorID(env, cls_ai_StrokeStyle, "(Lcom/scriptographer/ai/Color;ZSFIIFF[F)V");
	mid_ai_StrokeStyle_init = getMethodID(env, cls_ai_StrokeStyle, "init", "(Lcom/scriptographer/ai/Color;ZSFIIFF[F)V");
	mid_ai_StrokeStyle_initNative = getMethodID(env, cls_ai_StrokeStyle, "initNative", "(I)V");
	
	cls_ai_CharacterStyle = loadClass(env, "com/scriptographer/ai/CharacterStyle");
	mid_ai_CharacterStyle_markSetStyle = getMethodID(env, cls_ai_CharacterStyle, "markSetStyle", "()V");

	cls_ai_ParagraphStyle = loadClass(env, "com/scriptographer/ai/ParagraphStyle");
	mid_ai_ParagraphStyle_markSetStyle = getMethodID(env, cls_ai_ParagraphStyle, "markSetStyle", "()V");
	
	cls_ai_Group = loadClass(env, "com/scriptographer/ai/Group");
	
	cls_ai_Raster = loadClass(env, "com/scriptographer/ai/Raster");
	fid_ai_Raster_data = getFieldID(env, cls_ai_Raster, "data", "I");
	
	cls_ai_PlacedFile = loadClass(env, "com/scriptographer/ai/PlacedFile");
	
	cls_ai_PlacedSymbol = loadClass(env, "com/scriptographer/ai/PlacedSymbol");
	
	cls_ai_Tracing = loadClass(env, "com/scriptographer/ai/Tracing");
	mid_ai_Tracing_markDirty = getMethodID(env, cls_ai_Tracing, "markDirty", "()V");
	
	cls_ai_Layer = loadClass(env, "com/scriptographer/ai/Layer");

	cls_ai_Segment = loadClass(env, "com/scriptographer/ai/Segment");
	cls_ai_Curve = loadClass(env, "com/scriptographer/ai/Curve");
	
	cls_ai_GradientStop = loadClass(env, "com/scriptographer/ai/GradientStop");
	mid_ai_GradientStop_set = getMethodID(env, cls_ai_GradientStop, "set", "(DDLcom/scriptographer/ai/Color;)V");
	
	cls_ai_Artboard = loadClass(env, "com/scriptographer/ai/Artboard");
	mid_ai_Artboard_set = getMethodID(env, cls_ai_Artboard, "set", "(Lcom/scriptographer/ai/Rectangle;ZZZD)V");

	cls_ai_LiveEffect = loadClass(env, "com/scriptographer/ai/LiveEffect");
	cid_ai_LiveEffect = getConstructorID(env, cls_ai_LiveEffect, "(ILjava/lang/String;Ljava/lang/String;IIIII)V");
	mid_ai_LiveEffect_onEditParameters = getStaticMethodID(env, cls_ai_LiveEffect, "onEditParameters", "(II)V");
	mid_ai_LiveEffect_onCalculate = getStaticMethodID(env, cls_ai_LiveEffect, "onCalculate", "(ILcom/scriptographer/ai/Item;I)I");
	mid_ai_LiveEffect_onGetInputType = getStaticMethodID(env, cls_ai_LiveEffect, "onGetInputType", "(III)I");
	
	cls_ai_LiveEffectParameters = loadClass(env, "com/scriptographer/ai/LiveEffectParameters");
	mid_ai_LiveEffectParameters_wrapHandle = getStaticMethodID(env, cls_ai_LiveEffectParameters, "wrapHandle", "(II)Lcom/scriptographer/ai/LiveEffectParameters;");

	cls_ai_Annotator = loadClass(env, "com/scriptographer/ai/Annotator");
	cid_ai_Annotator = getConstructorID(env, cls_ai_Annotator, "(I)V");
	mid_ai_Annotator_onDraw = getStaticMethodID(env, cls_ai_Annotator, "onDraw", "(IIII)V");
	mid_ai_Annotator_onInvalidate = getStaticMethodID(env, cls_ai_Annotator, "onInvalidate", "(I)V");
	
	cls_ai_HitResult = loadClass(env, "com/scriptographer/ai/HitResult");
	cid_ai_HitResult = getConstructorID(env, cls_ai_HitResult, "(IILcom/scriptographer/ai/Item;IDLcom/scriptographer/ai/Point;I)V");

	cls_ai_FileFormat = loadClass(env, "com/scriptographer/ai/FileFormat");
	cid_ai_FileFormat = getConstructorID(env, cls_ai_FileFormat, "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;J)V");

	cls_ai_Timer = loadClass(env, "com/scriptographer/ai/Timer");
	mid_ai_Timer_onExecute = getStaticMethodID(env, cls_ai_Timer, "onExecute", "(I)Z");

// UI:
	cls_ui_NativeObject = loadClass(env, "com/scriptographer/ui/NativeObject");
	fid_ui_NativeObject_handle = getFieldID(env, cls_ui_NativeObject, "handle", "I");

	cls_ui_MenuGroup = loadClass(env, "com/scriptographer/ui/MenuGroup");
	mid_ui_MenuGroup_wrapHandle = getStaticMethodID(env, cls_ui_MenuGroup, "wrapHandle", "(ILjava/lang/String;)Lcom/scriptographer/ui/MenuGroup;");

	cls_ui_MenuItem = loadClass(env, "com/scriptographer/ui/MenuItem");
	mid_ui_MenuItem_wrapHandle = getStaticMethodID(env, cls_ui_MenuItem, "wrapHandle", "(ILjava/lang/String;ILjava/lang/String;)Lcom/scriptographer/ui/MenuItem;");
	mid_ui_MenuItem_onSelect = getStaticMethodID(env, cls_ui_MenuItem, "onSelect", "(I)V");
	mid_ui_MenuItem_onUpdate = getStaticMethodID(env, cls_ui_MenuItem, "onUpdate", "(IIII)V");

#ifndef ADM_FREE
// ADM:
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
	mid_adm_Dialog_onSizeChanged = getMethodID(env, cls_adm_Dialog, "onSizeChanged", "(IIZ)V");
	mid_adm_Dialog_deactivateActiveDialog = getStaticMethodID(env, cls_adm_Dialog, "deactivateActiveDialog", "()V");

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
	
	cls_adm_HierarchyListBox = loadClass(env, "com/scriptographer/adm/HierarchyListBox");

	cls_adm_ListEntry = loadClass(env, "com/scriptographer/adm/ListEntry");

	cls_adm_HierarchyListEntry = loadClass(env, "com/scriptographer/adm/HierarchyListEntry");

	cls_adm_NotificationHandler = loadClass(env, "com/scriptographer/adm/NotificationHandler");
	fid_adm_NotificationHandler_tracker = getFieldID(env, cls_adm_NotificationHandler, "tracker", "Lcom/scriptographer/adm/Tracker;");
	fid_adm_NotificationHandler_drawer = getFieldID(env, cls_adm_NotificationHandler, "drawer", "Lcom/scriptographer/adm/Drawer;");
	mid_adm_NotificationHandler_onNotify = getMethodID(env, cls_adm_NotificationHandler, "onNotify", "(Ljava/lang/String;)V");
	mid_adm_NotificationHandler_onDraw = getMethodID(env, cls_adm_NotificationHandler, "onDraw", "(Lcom/scriptographer/adm/Drawer;)Z");

	cls_adm_Tracker = loadClass(env, "com/scriptographer/adm/Tracker");
	mid_adm_Tracker_onTrack = getMethodID(env, cls_adm_Tracker, "onTrack", "(Lcom/scriptographer/adm/NotificationHandler;IIIIIIICJ)Z");

#if defined(MAC_ENV) && kPluginInterfaceVersion >= kAI14
	cls_adm_TextEditItem = loadClass(env, "com/scriptographer/adm/TextEditItem");
	fid_adm_TextEditItem_setSelectionTimer = getFieldID(env, cls_adm_TextEditItem, "setSelectionTimer", "I");
#endif

#endif //#ifndef ADM_FREE
}

void ScriptographerEngine::println(JNIEnv *env, const char *str, ...) {
	JNI_CHECK_ENV
	int size = 8192;
	char *text = new char[size];
	va_list args;
	va_start(args, str);
	vsnprintf(text, size, str, args);
	va_end(args);
	callVoidMethodReport(env,
			env->GetStaticObjectField(cls_System, fid_System_out),
			mid_PrintStream_println,
			env->NewStringUTF(text));
	delete text;
}

void ScriptographerEngine::reportError(JNIEnv *env) {
	JNI_CHECK_ENV
	if (isInitialized()) {
		jthrowable throwable = env->ExceptionOccurred();
		if (throwable != NULL) {
			env->ExceptionClear();
			callStaticVoidMethod(env, cls_ScriptographerEngine,
					mid_ScriptographerEngine_reportError, throwable);
		}
	} else {
		// TODO: ????
		env->ExceptionDescribe();
	}
}

/**
 * Creates a Java String from a given C-String.
 * TODO: The non depreceated version that takes an encoding parameter should be
 * used in the future.
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
 * TODO: The non depreceated version that takes an encoding parameter should be
 * used in the future.
 *
 * throws exceptions
 */
char *ScriptographerEngine::convertString(JNIEnv *env,
		jstring jstr, int minLength) {
	if (!jstr)
		return NULL;
	JNI_CHECK_ENV
	jbyteArray bytes = (jbyteArray) callObjectMethod(env, jstr,
			mid_String_getBytes);
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

jstring ScriptographerEngine::convertString(JNIEnv *env,
		const ASUnicode *str, int length) {
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

ASUnicode *ScriptographerEngine::convertString_ASUnicode(JNIEnv *env,
		jstring jstr) {
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
unsigned char *ScriptographerEngine::convertString_Pascal(JNIEnv *env,
		jstring jstr, int minLength) {
	char *str = convertString(env, jstr, minLength);
	return gPlugin->toPascal(str, (unsigned char*) str);
}

#else

/**
 * Creates a Java String from a given UTF-16-String.
 * Only supported in CS2 and above
 */
jstring ScriptographerEngine::convertString(JNIEnv *env,
		ai::UnicodeString &str) {
	JNI_CHECK_ENV
	const ASUnicode *buffer;
	int len = str.utf_16(buffer);
	jstring res = env->NewString(buffer, len);
	if (res == NULL) EXCEPTION_CHECK(env);
	return res;
}

ai::UnicodeString ScriptographerEngine::convertString_UnicodeString(JNIEnv *env,
		jstring jstr) {
	JNI_CHECK_ENV
	const jchar *chars = env->GetStringCritical(jstr, NULL);
	ai::UnicodeString str(chars, env->GetStringLength(jstr));
	env->ReleaseStringCritical(jstr, chars); 
	return str;
}

#endif

#ifdef MAC_ENV
CFStringRef ScriptographerEngine::convertString_CFString(JNIEnv *env,
		jstring jstr) {
	if (jstr == NULL)
		return NULL;
	JNI_CHECK_ENV
	const jchar *chars = env->GetStringCritical(jstr, NULL);
	CFStringRef str = CFStringCreateWithCharacters(kCFAllocatorDefault,
			chars, env->GetStringLength(jstr));
	env->ReleaseStringCritical(jstr, chars); 
	return str;
}		
#endif

// java.lang.Boolean <-> jboolean
jobject ScriptographerEngine::convertBoolean(JNIEnv *env, jboolean value) {
	return newObject(env, cls_Boolean, cid_Boolean, value);
}

jboolean ScriptographerEngine::convertBoolean(JNIEnv *env, jobject value) {
	return callBooleanMethod(env, value, mid_Boolean_booleanValue);
}

// java.lang.Integer <-> jint
jobject ScriptographerEngine::convertInteger(JNIEnv *env, jint value) {
	return newObject(env, cls_Integer, cid_Integer, value);
}

jint ScriptographerEngine::convertInteger(JNIEnv *env, jobject value) {
	return callIntMethod(env, value, mid_Number_intValue);
}

// java.lang.Float <-> jfloat
jobject ScriptographerEngine::convertFloat(JNIEnv *env, jfloat value) {
	return newObject(env, cls_Float, cid_Float, value);
}

jfloat ScriptographerEngine::convertFloat(JNIEnv *env, jobject value) {
	return callFloatMethod(env, value, mid_Number_floatValue);
}

// java.lang.Double <-> jdouble
jobject ScriptographerEngine::convertDouble(JNIEnv *env, jdouble value) {
	return newObject(env, cls_Double, cid_Double, value);
}

jdouble ScriptographerEngine::convertDouble(JNIEnv *env, jobject value) {
	return callDoubleMethod(env, value, mid_Number_doubleValue);
}

void ScriptographerEngine::updateCoordinateSystem() {
	// First make sure there's an open document at all
	if (gWorkingDoc != NULL) {
		// Get the global document origin
		sAIDocument->GetDocumentRulerOrigin(&m_rulerOrigin);
#if kPluginInterfaceVersion >= kAI15
		using namespace ai;
		ArtboardList artboards;
		if (artboards.IsValid()) {
			ArtboardID index;
			if (!artboards.GetActive(index)) {
				ArtboardProperties artboard = artboards.GetArtboardProperties(index);
				// Artboard Origin is returned relative to the Artboard Bounds, so
				// Add it up
				AIRealRect rect;
				artboard.GetPosition(rect);
				AIRealPoint origin;
				artboard.GetRulerOrigin(origin);
				// If we are in bottom up coordinates and the origin is set to the
				// top left corner (default setting), fake a new origin at the
				// bottom left corner.
				if (m_topDownCoordinates || origin.v != 0 || origin.v != 0) {
					m_artboardOrigin.h = rect.left + origin.h;
					m_artboardOrigin.v = rect.top - origin.v;
				} else {
					m_artboardOrigin.h = rect.left;
					m_artboardOrigin.v = rect.bottom;
				}
				// Document Origin on CS5 is returned relative to the active
				// artboard, so add its origin to it for the absolute value.
				m_rulerOrigin.h += m_artboardOrigin.h;
				m_rulerOrigin.v += m_artboardOrigin.v;
			}
		}
#elif kPluginInterfaceVersion >= kAI14
		ASInt32 index = 0;
		AICropAreaPtr area = NULL;
		if (!sAICropArea->GetActive(&index) && index != -1
				&& !sAICropArea->Get(index, &area)) {
			m_artboardOrigin.h = area->m_CropAreaRect.left;
			m_artboardOrigin.v = m_topDownCoordinates
				? area->m_CropAreaRect.top : area->m_CropAreaRect.bottom;
		} else {
			// In case of error
			m_artboardOrigin.h = 0;
			m_artboardOrigin.v = 0;
		}
#else // kPluginInterfaceVersion < kAI14
		// This is similar to ArtboardList.nativeGet()
		m_artboardOrigin.h = -m_rulerOrigin.h;
		m_artboardOrigin.v = -m_rulerOrigin.v;
		if (m_topDownCoordinates) {
			AIDocumentSetup setup;
			sAIDocument->GetDocumentSetup(&setup);
			m_artboardOrigin.v += setup.height;
		}
#endif // kPluginInterfaceVersion < kAI14
	}
}

// com.scriptographer.ai.Point <-> AIRealPoint
jobject ScriptographerEngine::convertPoint(JNIEnv *env, CoordinateSystem system,
		AIReal x, AIReal y, jobject res) {
	if (system == kArtboardCoordinates) {
		x -= m_artboardOrigin.h;
		y -= m_artboardOrigin.v;
	}
	if (m_topDownCoordinates && system != kCurrentCoordinates)
		y = -y;
	if (res == NULL) {
		return newObject(env, cls_ai_Point, cid_ai_Point,
				(jdouble) x, (jdouble) y);
	} else {
		callVoidMethod(env, res, mid_ai_Point_set,
				(jdouble) x, (jdouble) y);
		return res;
	}
}

// This handles 2 types of points: com.scriptographer.ai.Point,
// com.scriptographer.adm.Point
void ScriptographerEngine::convertPoint(JNIEnv *env, CoordinateSystem system,
		jdouble x, jdouble y, AIRealPoint *res) {
	// Illustrator crashes when it receives nan coordinates. So Set them 
	// to 0 before passing them to the native code.
	if (!VALID_COORDINATE(x) || !VALID_COORDINATE(y))
		THROW_INVALID_COORDINATES(env,
				convertPoint(env, kCurrentCoordinates, x, y));
	if (m_topDownCoordinates && system != kCurrentCoordinates)
		y = -y;
	if (system == kArtboardCoordinates) {
		x += m_artboardOrigin.h;
		y += m_artboardOrigin.v;
	}
	res->h = x;
	res->v = y;
	EXCEPTION_CHECK(env);
}

void ScriptographerEngine::convertPoint(JNIEnv *env, CoordinateSystem system,
		jobject point, AIRealPoint *res) {
	jdouble x, y;
	if (env->IsInstanceOf(point, cls_ai_Point)) {
		x = env->GetDoubleField(point, fid_ai_Point_x);
		y = env->GetDoubleField(point, fid_ai_Point_y);
	}
#ifndef ADM_FREE	
	else if (env->IsInstanceOf(point, cls_adm_Point)) {
		x = env->GetIntField(point, fid_adm_Point_x);
		y = env->GetIntField(point, fid_adm_Point_y);
	}
#endif //#ifndef ADM_FREE
	convertPoint(env, system, x, y, res);
}

/**
 * Coordinate System Conversion for raw segment point coordinates. Directly
 * modifies the point values in the float array.
 */
void ScriptographerEngine::convertSegments(JNIEnv *env, AIReal *data, int count,
		CoordinateSystem system, bool from) {
	AIReal xOrigin, yOrigin;
	if (system == kArtboardCoordinates) {
		xOrigin = m_artboardOrigin.h;
		yOrigin = m_artboardOrigin.v;
	}
	// Check if a conversion is needed at all?
	if (xOrigin != 0 || yOrigin != 0 || m_topDownCoordinates) {
		AIReal *ptr = data;
		if (m_topDownCoordinates) {
			// We need to flip coordinates both ways. When reading from Ai, we
			// first subtract the yOrigin from the value and flip after.
			// When writing to Ai, we first negate the value and then add
			// yOrigin.
			if (from) {
				for (int i = 0; i < count; i++) {
					// Convert point values for each segment
					for (int j = 0; j < 3; j++) {
						*(ptr++) = *ptr - xOrigin;
						*(ptr++) = -(*ptr - yOrigin);
					}
					// Skip boolean value
					ptr++;
				}
			} else {
				for (int i = 0; i < count; i++) {
					// Convert point values for each segment
					for (int j = 0; j < 3; j++) {
						AIReal x = *ptr;
						AIReal y = *(ptr + 1);
						if (!VALID_COORDINATE(x) || !VALID_COORDINATE(y))
							THROW_INVALID_COORDINATES(env, gEngine->convertPoint(
									env, kCurrentCoordinates, x, y));
						*(ptr++) = x + xOrigin;
						*(ptr++) = -y + yOrigin;
					}
					// Skip boolean value
					ptr++;
				}
			}
		} else {
			if (from) {
				for (int i = 0; i < count; i++) {
					// Convert point values for each segment
					for (int j = 0; j < 3; j++) {
						*(ptr++) = *ptr - xOrigin;
						*(ptr++) = *ptr - yOrigin;
					}
					// Skip boolean value
					ptr++;
				}
			} else {
				for (int i = 0; i < count; i++) {
					// Convert point values for each segment
					for (int j = 0; j < 3; j++) {
						AIReal x = *ptr;
						AIReal y = *(ptr + 1);
						if (!VALID_COORDINATE(x) || !VALID_COORDINATE(y))
							THROW_INVALID_COORDINATES(env, gEngine->convertPoint(
									env, kCurrentCoordinates, x, y));
						*(ptr++) = x + xOrigin;
						*(ptr++) = y + yOrigin;
					}
					// Skip boolean value
					ptr++;
				}
			}
		}
	}
}

// com.scriptographer.ai.Rectangle <-> AIRealRect

jobject ScriptographerEngine::convertRectangle(JNIEnv *env,
		CoordinateSystem system, AIReal left, AIReal top,
		AIReal right, AIReal bottom, jobject res) {
	AIReal x = left;
	AIReal y = m_topDownCoordinates ? top : bottom;
	AIReal width = right - left;
	AIReal height = top - bottom;
	if (system == kArtboardCoordinates) {
		x -= m_artboardOrigin.h;
		y -= m_artboardOrigin.v;
	}
	if (m_topDownCoordinates)
		y = -y;
	// AIRealRects are upside down, top and bottom are switched!
	if (res == NULL) {
		return newObject(env, cls_ai_Rectangle, cid_ai_Rectangle,
				(jdouble) x, (jdouble) y, (jdouble) width, (jdouble) height);
	} else {
		callVoidMethod(env, res, mid_ai_Rectangle_set,
				(jdouble) x, (jdouble) y, (jdouble) width, (jdouble) height);
		return res;
	}
}

void ScriptographerEngine::convertRectangle(JNIEnv *env,
		CoordinateSystem system, jobject rect, AIRealRect *res) {
	// AIRealRects are upside down, top and bottom are switched!
	jdouble x = env->GetDoubleField(rect, fid_ai_Rectangle_x);
	jdouble y =  env->GetDoubleField(rect, fid_ai_Rectangle_y);
	jdouble width = env->GetDoubleField(rect, fid_ai_Rectangle_width);
	jdouble height = env->GetDoubleField(rect, fid_ai_Rectangle_height);
	if (!VALID_COORDINATE(x) || !VALID_COORDINATE(y)
			|| !VALID_COORDINATE(width) || !VALID_COORDINATE(height))
		THROW_INVALID_COORDINATES(env, rect);
	if (m_topDownCoordinates)
		y = -y;
	if (system == kArtboardCoordinates) {
		x += m_artboardOrigin.h;
		y += m_artboardOrigin.v;
	}
	res->left = x;
	res->right = x + width;

	if (m_topDownCoordinates) {
		res->top = y;
		res->bottom = y - height;
	} else {
		res->bottom = y;
		res->top = y + height;
	}
	EXCEPTION_CHECK(env);
}

// com.scriptographer.ai.Size <-> AIRealPoint
jobject ScriptographerEngine::convertSize(JNIEnv *env, float width,
		float height, jobject res) {
	if (res == NULL) {
		return newObject(env, cls_ai_Size, cid_ai_Size,
				(jdouble) width, (jdouble) height);
	} else {
		callVoidMethod(env, res, mid_ai_Size_set,
				(jdouble) width, (jdouble) height);
		return res;
	}
}

void ScriptographerEngine::convertSize(JNIEnv *env, jobject size,
		AIRealPoint *res) {
	jdouble width = env->GetDoubleField(size, fid_ai_Size_width);
	jdouble height = env->GetDoubleField(size, fid_ai_Size_height);
	if (!VALID_COORDINATE(width) || !VALID_COORDINATE(height))
		THROW_INVALID_COORDINATES(env, size);
	res->h = width;
	res->v = height;
	EXCEPTION_CHECK(env);
}

// com.scriptoggrapher.ai.Matrix <-> AIRealMatrix
jobject ScriptographerEngine::convertMatrix(JNIEnv *env,
		CoordinateSystem from, CoordinateSystem to, AIRealMatrix *mt,
		jobject res) {

	AIRealMatrix matrix = *mt;

	if (to == kArtboardCoordinates) {
		sAIRealMath->AIRealMatrixConcatTranslate(&matrix,
				-m_artboardOrigin.h, -m_artboardOrigin.v);
		if (m_topDownCoordinates)
			sAIRealMath->AIRealMatrixConcatScale(&matrix, 1, -1);
	}

	if (from == kArtboardCoordinates) {
		sAIRealMath->AIRealMatrixConcatTranslate(&matrix,
				m_artboardOrigin.h, m_artboardOrigin.v);
		if (m_topDownCoordinates)
			sAIRealMath->AIRealMatrixConcatScale(&matrix, 1, -1);
	}
	return newObject(env, cls_ai_Matrix, cid_ai_Matrix,
		(jdouble) matrix.a, (jdouble) matrix.b,
		(jdouble) matrix.c, (jdouble) matrix.d,
		(jdouble) matrix.tx, (jdouble) matrix.ty);
}

AIRealMatrix *ScriptographerEngine::convertMatrix(JNIEnv *env,
		CoordinateSystem from, CoordinateSystem to, jobject mt,
		AIRealMatrix *res) {
	// TODO: use same conversion as Gradient where the native side calls a java
	// function on Matrix to call back into native side with values.
	AIRealMatrix matrix = {
		env->CallDoubleMethod(mt, mid_ai_Matrix_getScaleX),
		env->CallDoubleMethod(mt, mid_ai_Matrix_getShearY),
		env->CallDoubleMethod(mt, mid_ai_Matrix_getShearX),
		env->CallDoubleMethod(mt, mid_ai_Matrix_getScaleY),
		env->CallDoubleMethod(mt, mid_ai_Matrix_getTranslateX),
		env->CallDoubleMethod(mt, mid_ai_Matrix_getTranslateY)
	};
	if (!VALID_COORDINATE(matrix.a) || !VALID_COORDINATE(matrix.b)
			|| !VALID_COORDINATE(matrix.c) || !VALID_COORDINATE(matrix.d)
			|| !VALID_COORDINATE(matrix.tx) || !VALID_COORDINATE(matrix.ty))
		THROW_INVALID_COORDINATES(env, mt);
	EXCEPTION_CHECK(env);

	if (to == kArtboardCoordinates) {
		sAIRealMath->AIRealMatrixSetTranslate(res,
				-m_artboardOrigin.h, -m_artboardOrigin.v);
		if (m_topDownCoordinates)
			sAIRealMath->AIRealMatrixConcatScale(res, 1, -1);
	} else {
		sAIRealMath->AIRealMatrixSetIdentity(res);
	}

	sAIRealMath->AIRealMatrixConcat(res, &matrix, res);

	if (from == kArtboardCoordinates) {
		if (m_topDownCoordinates)
			sAIRealMath->AIRealMatrixConcatScale(res, 1, -1);
		sAIRealMath->AIRealMatrixConcatTranslate(res,
				m_artboardOrigin.h, m_artboardOrigin.v);
	}
	return res;
}

#ifndef ADM_FREE

// com.scriptographer.adm.Point <-> ADMPoint
jobject ScriptographerEngine::convertPoint(JNIEnv *env,
		ADMPoint *point, jobject res) {
	if (res == NULL) {
		return newObject(env, cls_adm_Point, cid_adm_Point,
				(jint) point->h, (jint) point->v);
	} else {
		callVoidMethod(env, res, mid_adm_Point_set,
				(jint) point->h, (jint) point->v);
		return res;
	}
}

// This handles 2 types of points: com.scriptographer.adm.Point,
// com.scriptographer.ai.Point
void ScriptographerEngine::convertPoint(JNIEnv *env,
		jobject point, ADMPoint *res) {
	if (env->IsInstanceOf(point, cls_adm_Point)) {
		res->h = env->GetIntField(point, fid_adm_Point_x);
		res->v = env->GetIntField(point, fid_adm_Point_y);
	} else if (env->IsInstanceOf(point, cls_ai_Point)) {
		res->h = (short) env->GetDoubleField(point, fid_ai_Point_x);
		res->v = (short) env->GetDoubleField(point, fid_ai_Point_y);
	}
	EXCEPTION_CHECK(env);
}

// com.scriptographer.adm.Rectangle <-> ADMRect
jobject ScriptographerEngine::convertRectangle(JNIEnv *env,
		ADMRect *rect, jobject res) {
	if (res == NULL) {
		return newObject(env, cls_adm_Rectangle, cid_adm_Rectangle,
				(jint) rect->left, (jint) rect->top,
				(jint) (rect->right - rect->left),
				(jint) (rect->bottom - rect->top));
	} else {
		callVoidMethod(env, res, mid_adm_Rectangle_set,
				(jint) rect->left, (jint) rect->top,
				(jint) (rect->right - rect->left),
				(jint) (rect->bottom - rect->top));
		return res;
	}
}

void ScriptographerEngine::convertRectangle(JNIEnv *env,
		jobject rect, ADMRect *res) {
	res->left = env->GetIntField(rect, fid_adm_Rectangle_x);
	res->top = env->GetIntField(rect, fid_adm_Rectangle_y);
	res->right = res->left + env->GetIntField(rect, fid_adm_Rectangle_width);
	res->bottom = res->top + env->GetIntField(rect, fid_adm_Rectangle_height);
	EXCEPTION_CHECK(env);
}

// com.scriptographer.adm.Size <-> ADMPoint
jobject ScriptographerEngine::convertSize(JNIEnv *env,
		ADMPoint *size, jobject res) {
	if (res == NULL) {
		return newObject(env, cls_adm_Size, cid_adm_Size,
				(jint) size->h, (jint) size->v);
	} else {
		callVoidMethod(env, res, mid_adm_Size_set,
				(jint) size->h, (jint) size->v);
		return res;
	}
}

void ScriptographerEngine::convertSize(JNIEnv *env,
		jobject size, ADMPoint *res) {
	res->h = env->GetIntField(size, fid_adm_Size_width);
	res->v = env->GetIntField(size, fid_adm_Size_height);
	EXCEPTION_CHECK(env);
}

#endif //#ifndef ADM_FREE

// java.awt.Color <-> ADMRGBColor
jobject ScriptographerEngine::convertColor(JNIEnv *env, ADMRGBColor *srcCol) {
	return newObject(env, cls_awt_Color, cid_awt_Color,
			(jfloat) srcCol->red / 65535.0,
			(jfloat) srcCol->green / 65535.0,
			(jfloat) srcCol->blue / 65535.0);
}

void ScriptographerEngine::convertColor(JNIEnv *env,
		jobject srcCol, ADMRGBColor *dstCol) {
	jfloatArray array = (jfloatArray) env->CallObjectMethod(srcCol,
			mid_awt_Color_getColorComponents, NULL);
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
}

// com.scriptoggrapher.ai.Color <-> AIColor
jobject ScriptographerEngine::convertColor(JNIEnv *env,
		AIColor *srcCol, AIReal alpha) {
	switch (srcCol->kind) {
		case kGrayColor: {
			return newObject(env, cls_ai_GrayColor, cid_ai_GrayColor,
				(jfloat) srcCol->c.g.gray, (jfloat) alpha);
		}
		case kThreeColor: {
			AIThreeColorStyle *rgb = &srcCol->c.rgb;
			return newObject(env, cls_ai_RGBColor, cid_ai_RGBColor,
					(jfloat) rgb->red, (jfloat) rgb->green, (jfloat) rgb->blue,
					(jfloat) alpha);
		}
		case kFourColor: {
			AIFourColorStyle *f = &srcCol->c.f;
			return newObject(env, cls_ai_CMYKColor, cid_ai_CMYKColor,
					(jfloat) f->cyan, (jfloat) f->magenta, (jfloat) f->yellow,
					(jfloat) f->black, (jfloat) alpha);
		}
		case kGradient: {
			AIGradientStyle *b = &srcCol->c.b;
			return newObject(env, cls_ai_GradientColor, cid_ai_GradientColor,
				(jint) b->gradient,
				gEngine->convertPoint(env, kArtboardCoordinates, &b->gradientOrigin),
				(jfloat) b->gradientAngle, (jfloat) b->gradientLength,
				// TODO: Test if conersion is correct and sync with 
				// Color.nativeSetGradient
				gEngine->convertMatrix(env, kCurrentCoordinates,
						kArtboardCoordinates, &b->matrix),
				(jfloat) b->hiliteAngle, (jfloat) b->hiliteLength);
		}
		case kPattern: {
			AIPatternStyle *p = &srcCol->c.p;
			// TODO: These values always seem to be the same, so we do not need
			// to expose them and can only use the Matrix instead. This check
			// here is just there to see if it can happen that they contain
			// something else, in which case they would need to be added to the
			// matrix.
			if (p->shiftDist != 0 || p->shiftAngle != 0 || p->scale.h != 100
					|| p->scale.v != 100 || p->rotate != 0 || p->reflect
					|| p->reflectAngle != 0 || p->shearAngle != 0 
					|| p->shearAxis != 0) {
				throw new StringException(
						"Pattern contains old-style transform definition.\n"
						"Please store file and send to team@scriptographer.org"
						"along with these values:\n%f %f %f %f %f %i %f %f %f",
						p->shiftDist, p->shiftAngle, p->scale.h, p->scale.v,
						p->rotate, p->reflect, p->reflectAngle, p->shearAngle,
						p->shearAxis);
			}
			return newObject(env, cls_ai_PatternColor, cid_ai_PatternColor,
					(jint) p->pattern,
					// TODO: Test if conersion is correct and sync with 
					// Color.nativeSetPattern
					gEngine->convertMatrix(env, kCurrentCoordinates,
							kArtboardCoordinates, &p->transform));
		}
		case kNoneColor: {
			return obj_ai_Color_NONE;
		}
		// TODO: add kCustomColor
	}
	return NULL;
}

bool ScriptographerEngine::convertColor(JNIEnv *env,
		jobject srcCol, AIColor *dstCol, AIReal *alpha) {
	// TODO: add kCustomColor
	if (env->IsInstanceOf(srcCol, cls_ai_GradientColor)) {
		dstCol->kind = kGradient;
		// call mid_ai_GradientColor_set, which sets the AIGradientStyle by
		// calling Java_com_scriptographer_ai_Color_nativeSetGradient with the
		// right arguments
		callVoidMethod(env, srcCol, mid_ai_GradientColor_set,
				(jint) &dstCol->c.b);
		return true;
	} else if (env->IsInstanceOf(srcCol, cls_ai_PatternColor)) {
		dstCol->kind = kPattern;
		// call mid_ai_PatternColor_set, which sets the AIPatternStyle by
		// calling Java_com_scriptographer_ai_Color_nativeSetPattern with the
		// right arguments
		callVoidMethod(env, srcCol, mid_ai_PatternColor_set,
				(jint) &dstCol->c.p);
		return true;
	} else {
		return convertColor(env, (jfloatArray) env->CallObjectMethod(srcCol,
				mid_ai_Color_getComponents), dstCol, alpha);
	}
	return false;
}

bool ScriptographerEngine::convertColor(JNIEnv *env,
		jfloatArray srcCol, AIColor *dstCol, AIReal *alpha) {
	int length = env->GetArrayLength(srcCol);
	jfloat *values = new jfloat[length];
	env->GetFloatArrayRegion(srcCol, 0, length, values);
	bool ok = true;
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
	default:
		ok = false;
	}
	delete values;
	return ok;
}

// AIColor <-> ADMRGBColor

bool ScriptographerEngine::convertColor(ADMRGBColor *srcCol, AIColor *dstCol) {
	dstCol->kind = kThreeColor;
	dstCol->c.rgb.red = (float) srcCol->red / 65535.0;
	dstCol->c.rgb.green = (float) srcCol->green / 65535.0;
	dstCol->c.rgb.blue = (float) srcCol->blue / 65535.0;
	return true;
}

bool ScriptographerEngine::convertColor(AIColor *srcCol, ADMRGBColor *dstCol) {
	// convert to RGB if it isn't already:
	if (srcCol->kind != kThreeColor && !convertColor(srcCol,
			kAIRGBColorSpace, srcCol))
		return false;
	dstCol->red = (short) (srcCol->c.rgb.red * 65535.0 + 0.5);
	dstCol->green = (short) (srcCol->c.rgb.green * 65535.0 + 0.5);
	dstCol->blue = (short) (srcCol->c.rgb.blue * 65535.0 + 0.5);
	return true;
}

// AIColor <-> AIColor
bool ScriptographerEngine::convertColor(AIColor *srcCol,
		AIColorConversionSpaceValue dstSpace, AIColor *dstCol, AIReal srcAlpha,
		AIReal *dstAlpha) {
	// Determine srcCol's space and sample size:
	AIColorConversionSpaceValue srcSpace;
	int srcSize;

	// ConvertSampleColor seems to have problems converting alpha color spaces,
	// so always use the non alpha versions and copy over alpha seperately.
	switch (srcCol->kind) {
		case kGrayColor:
			srcSize = 1;
			srcSpace = kAIGrayColorSpace;
			break;
		case kThreeColor:
			srcSize = 3;
			srcSpace = kAIRGBColorSpace;
			break;
		case kFourColor:
			srcSize = 4;
			srcSpace = kAICMYKColorSpace;
			break;
		default:
			return NULL;
	}

	bool dstHasAlpha = dstSpace == kAIACMYKColorSpace ||
		dstSpace == kAIARGBColorSpace ||
		dstSpace == kAIAGrayColorSpace;

	// Change to non alpha version. See explanation above.
	switch (dstSpace) {
		case kAIAGrayColorSpace:
			dstSpace = kAIGrayColorSpace;
			break;
		case kAIARGBColorSpace:
			dstSpace = kAIRGBColorSpace;
			break;
		case kAIACMYKColorSpace:
			dstSpace = kAICMYKColorSpace;
			break;
	}

	if (srcSpace >= 0 && dstSpace >= 0) {
		AISampleComponent src[4];
		AISampleComponent dst[4];
		memcpy(src, &srcCol->c, srcSize * sizeof(AISampleComponent));
		// TODO: why swapping kGrayColor???
		if (srcCol->kind == kGrayColor)
			src[0] = 1.0 - src[0];
		ASBoolean inGamut;
#if kPluginInterfaceVersion < kAI12
		if (!sAIColorConversion->ConvertSampleColor(srcSpace, src, dstSpace,
				dst, &inGamut)) {
#elif kPluginInterfaceVersion < kAI17
		AIColorConvertOptions options;
		if (!sAIColorConversion->ConvertSampleColor(srcSpace, src, dstSpace,
				dst, options, &inGamut)) {
#else
		{ //todo
#endif
			// Init the destCol with 0
			// memset(dstCol, 0, sizeof(AIColor));
			// determine dstCol's kind and sampleSize:
			int dstSize;
			switch (dstSpace) {
				case kAIMonoColorSpace:
					dstCol->kind = kGrayColor;
					dstSize = 1;
					break;
				case kAIGrayColorSpace:
					dstCol->kind = kGrayColor;
					dstSize = 1;
					break;
				case kAIRGBColorSpace:
					dstCol->kind = kThreeColor;
					dstSize = 3;
					break;
				case kAICMYKColorSpace:
					dstCol->kind = kFourColor;
					dstSize = 4;
					break;
				default:
					return NULL;
			}
			// TODO: why swapping kGrayColor???
			if (dstCol->kind == kGrayColor)
				dst[0] = 1.0 - dst[0];
			memcpy(&dstCol->c, dst, dstSize * sizeof(AISampleComponent));
			// get back alpha:
			if (dstAlpha != NULL)
				*dstAlpha = dstHasAlpha ? srcAlpha : -1;
			return true;
		}
	}
	return false;
}

// AIFillStyle <-> com.scriptoggrapher.ai.FillStyle

jobject ScriptographerEngine::convertFillStyle(JNIEnv *env, AIFillStyle *style,
		jobject res) {
	// TODO: add additional AIFillStyleMap
	jobject color = convertColor(env, &style->color);
	if (res == NULL) {
		res = newObject(env, cls_ai_FillStyle, cid_ai_FillStyle, color, true,
				style->overprint);
	} else {
		callVoidMethod(env, res, mid_ai_FillStyle_init, color, true,
				style->overprint);
	}
	return res;
}

void ScriptographerEngine::convertFillStyle(JNIEnv *env,
		jobject style, AIFillStyle *res) {
	callVoidMethod(env, style, mid_ai_FillStyle_initNative, res);
}

// AIStrokeStyle <-> com.scriptoggrapher.ai.StrokeStyle

jobject ScriptographerEngine::convertStrokeStyle(JNIEnv *env,
		AIStrokeStyle *style, jobject res) {
	// TODO: add additional AIStrokeStyleMap
	jobject color = convertColor(env, &style->color);
	int count = style->dash.length;
	jfloatArray dashArray = env->NewFloatArray(count);
	env->SetFloatArrayRegion(dashArray, 0, count, style->dash.array);
	if (res == NULL) {
		res = newObject(env, cls_ai_StrokeStyle, cid_ai_StrokeStyle, color, true,
			style->overprint, style->width,
			style->cap, style->join, style->miterLimit,
			style->dash.offset, dashArray);
	} else {
		callVoidMethod(env, res, mid_ai_StrokeStyle_init, color, true,
			style->overprint, style->width,
			style->cap, style->join, style->miterLimit,
			style->dash.offset, dashArray);
	}
	return res;
}

void ScriptographerEngine::convertStrokeStyle(JNIEnv *env,
		jobject style, AIStrokeStyle *res) {
	callVoidMethod(env, style, mid_ai_StrokeStyle_initNative, res);
}


// AIArtSet <-> ItemList

jobject ScriptographerEngine::convertItemSet(JNIEnv *env,
		AIArtSet set, bool layerOnly) {
	Item_filter(set, layerOnly);
	sizet count = 0;
	sAIArtSet->CountArtSet(set, &count);
	jobject itemSet = newObject(env, cls_ai_ItemList, cid_ItemList); 
	for (long i = 0; i < count; i++) {
		jobject obj;
		AIArtHandle art;
		if (!sAIArtSet->IndexArtSet(set, i, &art)) {
			if (art != NULL) {
				obj = wrapArtHandle(env, art);
				if (obj != NULL)
					callBooleanMethod(env, itemSet, mid_ai_ItemList_add, obj);
			}
		}
	}
	EXCEPTION_CHECK(env);
	return itemSet;
}

AIArtSet ScriptographerEngine::convertItemSet(JNIEnv *env,
		jobject itemSet, bool activate) {
	AIArtSet set = NULL;
	if (!sAIArtSet->NewArtSet(&set)) {
		// Use a for loop with size instead of hasNext, because that saves us
		// many calls...
		jint size = callIntMethod(env, itemSet, mid_List_size);
		for (int i = 0; i < size; i++) {
			jobject obj = callObjectMethod(env, itemSet, mid_List_get, i);
			if (obj != NULL)
				sAIArtSet->AddArtToArtSet(set, getArtHandle(env, obj));
		}
	}
	if (activate)
		Item_activateDocument(env, set);
	EXCEPTION_CHECK(env);
	return set;
}

AIArtSet ScriptographerEngine::convertItemSet(JNIEnv *env,
		jobjectArray items, bool activate) {
	AIArtSet set = NULL;
	if (!sAIArtSet->NewArtSet(&set)) {
		jint length = env->GetArrayLength(items);
		for (int i = 0; i < length; i++) {
			jobject item = env->GetObjectArrayElement(items, i);
			if (item != NULL)
				sAIArtSet->AddArtToArtSet(set, getArtHandle(env, item));
		}
	}
	if (activate)
		Item_activateDocument(env, set);
	EXCEPTION_CHECK(env);
	return set;
}

// java.io.File <-> SPPlatformFileSpecification

char *ScriptographerEngine::getFilePath(JNIEnv *env, jobject file) {
	char *path = convertString(env,
			(jstring) callObjectMethod(env, file, mid_File_getPath));
	EXCEPTION_CHECK(env);
	return path;
}

jobject ScriptographerEngine::convertFile(JNIEnv *env, const char *path) {
	if (path == NULL)
		return NULL;
	return newObject(env, cls_File, cid_File, convertString(env, path));
}

#ifdef MAC_ENV
jobject ScriptographerEngine::convertFile(JNIEnv *env, CFStringRef path) {
	if (path == NULL)
		return NULL;
	char chars[1024];
	CFStringGetCString(path, chars, 1024, kCFStringEncodingUTF8);
	return convertFile(env, chars);
}
#endif

jobject ScriptographerEngine::convertFile(JNIEnv *env,
		SPPlatformFileSpecification *fileSpec) {
	if (fileSpec != NULL) {
		char path[kMaxPathLength];
		if (gPlugin->fileSpecToPath(fileSpec, path))
			return convertFile(env, path);
	}
	return NULL;
}

SPPlatformFileSpecification *ScriptographerEngine::convertFile(JNIEnv *env,
		jobject file, SPPlatformFileSpecification *res) {
	if (file == NULL)
		return NULL;
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

void ScriptographerEngine::commit(JNIEnv *env) {
	// TODO: Add a commmit command that only commits all items of the document
	callStaticVoidMethod(env, cls_CommitManager, mid_CommitManager_commit, NULL);
}

void ScriptographerEngine::resumeSuspendedDocuments() {
	for (int i = m_suspendedDocuments.size() - 1; i >= 0; i--) {
		AIDocumentHandle doc = m_suspendedDocuments.get(i);
		if (doc != gWorkingDoc)
			Document_activate(doc, true);
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

int ScriptographerEngine::getAIObjectHandle(JNIEnv *env,
		jobject obj, const char *name) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	int handle = getIntField(env, obj, fid_ai_NativeObject_handle);
	if (!handle)
		throw new StringException("The %i is no longer valid. Use isValid()"
				"checks to avoid this error.", name);
	return handle;
}

int ScriptographerEngine::getDocumentObjectHandle(JNIEnv *env,
		jobject obj, bool activateDoc, const char *name) {
	int handle = getAIObjectHandle(env, obj, name);
	if (activateDoc && handle)
		getDocumentHandle(env, obj, true);
	return handle;
}

AIDictionaryRef ScriptographerEngine::getDictionaryHandle(JNIEnv *env,
		jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	int handle = getIntField(env, obj, fid_ai_Dictionary_handle);
	if (!handle)
		throw new StringException("The dictionary is no longer valid."
				"Use isValid() checks to avoid this error.");
	return (AIDictionaryRef) handle;
}
/**
 * Returns the wrapped AIDocumentHandle of an object by assuming that it is a
 * Document and accessing its field 'handle', or an Item, in which case it's
 * document field is fetched first. 
 *
 * throws exceptions
 */
AIDocumentHandle ScriptographerEngine::getDocumentHandle(JNIEnv *env,
		jobject obj, bool activate) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	if (env->IsInstanceOf(obj, cls_ai_DocumentObject))
		// Fetch document field and switch if necessary
		obj = getObjectField(env, obj, fid_ai_DocumentObject_document);
	AIDocumentHandle doc =
			(AIDocumentHandle) getAIObjectHandle(env, obj, "document");
	// Switch to this document if necessary
	if (activate && doc && doc != gWorkingDoc)
		Document_activate(doc, true);
	return doc;
}

/**
 * Returns the wrapped AIArtHandle of an object by assuming that it is an
 * anchestor of Class Item and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
AIArtHandle ScriptographerEngine::getArtHandle(JNIEnv *env,
		jobject obj, bool activateDoc, AIDocumentHandle *doc) {
	if (obj == NULL)
		return NULL;
	AIArtHandle art = (AIArtHandle) getAIObjectHandle(env, obj, "art");
	if (art == (AIArtHandle) com_scriptographer_ai_Item_HANDLE_CURRENT_STYLE)
		return NULL;
	// Make sure the object is valid
	if (!callBooleanMethod(env, obj, mid_ai_Item_isValid))
		throw new StringException("The item is no longer valid, either due to"
				"deletion or undoing. Use isValid() checks to avoid this error.");
	if (activateDoc || doc != NULL) {
		// Fetch docHandle and switch if necessary
		jobject docObj = getObjectField(env, obj,
				fid_ai_DocumentObject_document);
		if (docObj != NULL) {
			AIDocumentHandle docHandle = (AIDocumentHandle) getIntField(env,
					docObj, fid_ai_NativeObject_handle);
			if (doc != NULL)
				*doc = docHandle;
			// Switch to this document if necessary
			if (activateDoc) {
				if (docHandle != gWorkingDoc)
					Document_activate(docHandle, true);
				// If it's a text object, suspend the text flow now
				// text flow of all suspended documents is resumed at the end
				// by gEngine->resumeSuspendedDocuments()
				AIDictionaryRef dict = NULL;
				// Use a dictionary entry to see if it was suspsended before
				// already
				if (Item_getType(art) == kTextFrameArt
						&& !sAIDocument->GetDictionary(&dict)) {
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

/**
 * Returns the wrapped AILayerHandle of an object by assuming that it is an
 * anchestor of Class Item and accessing its field 'handle':
 *
 * throws exceptions
 */
AILayerHandle ScriptographerEngine::getLayerHandle(JNIEnv *env,
		jobject obj, bool activateDoc) {
	AIArtHandle art = getArtHandle(env, obj, activateDoc);
	AILayerHandle layer = NULL;
	sAIArt->GetLayerOfArt(art, &layer);
	return layer;
}

/**
 * Returns the AIDictionaryRef that contains the wrapped AIArtHandle, if any
 *
 * throws exceptions
 */
AIDictionaryRef ScriptographerEngine::getArtDictionaryHandle(JNIEnv *env,
		jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	return (AIDictionaryRef) gEngine->getIntField(env, obj,
			fid_ai_Item_dictionaryHandle);
}

AIDictKey ScriptographerEngine::getArtDictionaryKey(JNIEnv *env, jobject obj) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	return (AIDictKey) gEngine->getIntField(env, obj, fid_ai_Item_dictionaryKey);
}

/**
 * Returns the wrapped TextFrameRef of an object by assuming that it is an
 * anchestor of Class TextFrame and
 * accessing its field 'handle':
 *
 * throws exceptions
 */
ATE::TextFrameRef ScriptographerEngine::getTextFrameHandle(JNIEnv *env,
		jobject obj, bool activateDoc) {
	AIArtHandle art = getArtHandle(env, obj, activateDoc);
	ATE::TextFrameRef ref = NULL;
	sAITextFrame->GetATETextFrame(art, &ref);
	return ref;
}

jobject ScriptographerEngine::wrapTextRangeRef(JNIEnv *env,
		ATE::TextRangeRef range) {
	// we need to increase the ref count here. this is decreased again in
	// TextRange.finalize
	ATE::sTextRange->AddRef(range);
	return newObject(env, cls_ai_TextRange, cid_ai_TextRange,
			(jint) range, (jint) gWorkingDoc);
}

/**
 * Wraps the handle in a java object. see the Java function Item.wrapArtHandle
 * to see how the cashing of already wrapped objects is handled.
 *
 * throws exceptions
 */
jobject ScriptographerEngine::wrapArtHandle(JNIEnv *env, AIArtHandle art,
		AIDocumentHandle doc, bool created, short type, bool checkWrapped) {
	JNI_CHECK_ENV
	if (art == NULL)
		return NULL;
	AITextFrameType textType = kUnknownTextType;
	// Do we need to determine type or is it passed?
	// Currently this is only used by wrapLayerHandle.
	if (type == -1) {
		ASBoolean isLayer = false;
		if (sAIArt->GetArtType(art, &type)
				|| sAIArt->IsArtLayerGroup(art, &isLayer))
			throw new StringException("Cannot determine the item's type");
		if (isLayer) {
			// Self defined type for layer groups
			type = com_scriptographer_ai_Item_TYPE_LAYER;
		} else if (type == kTextFrameArt) {
			// Determine text type as well
			sAITextFrame->GetType(art, &textType);
		} else if (type == kPluginArt) {
			// TODO: Add handling of special types
#if kPluginInterfaceVersion >= kAI12 && kPluginInterfaceVersion <= kAI15
			if (sAITracing->IsTracing(art))
				type = com_scriptographer_ai_Item_TYPE_TRACING;
#endif
		}
	}

	// Store the art object's initial handle value in its own dictionary.
	// See onSelectionChanged for more explanations
	jboolean wrapped = false;
	AIDictionaryRef artDict;
	if (checkWrapped && !sAIArt->GetDictionary(art, &artDict)) {
		wrapped = sAIDictionary->IsKnown(artDict, m_artHandleKey);
		sAIDictionary->SetIntegerEntry(artDict, m_artHandleKey, (ASInt32) art);
		sAIDictionary->Release(artDict);
	}
#if kPluginInterfaceVersion >= kAI15
	// Turn off pixel perfect flag on all newly created items on CS5 and above,
	// as it aligns points strangly. To use this feature one then has to 
	// explicitely set it through the UI for now
	// TODO: later maybe add a property to Item
	if (created)
		sAIArt->SetPixelPerfect(art, false);
#endif
	return callStaticObjectMethod(env, cls_ai_Item, mid_ai_Item_wrapHandle,
			(jint) art, (jshort) type, (jint) textType,
			(jint) (doc ? doc : gWorkingDoc), wrapped, created);
}

void ScriptographerEngine::changeArtHandle(JNIEnv *env, jobject item,
		AIArtHandle art, AIDocumentHandle doc, bool clearDictionary) {
	callVoidMethod(env, item, mid_ai_Item_changeHandle, (jint) art, (jint) doc,
			(jboolean) clearDictionary);
}

void ScriptographerEngine::setItemDictionary(JNIEnv *env, jobject item,
		AIDictionaryRef dictionary, AIDictKey key) {
	// Release the previous dictionary if there was one. Do this even if it is
	// the same since we are adding a reference again after.
	AIDictionaryRef prevDict = getArtDictionaryHandle(env, item);
	if (prevDict != NULL)
		sAIDictionary->Release(prevDict);
	// Let the art object know it's part of a dictionary now:
	setIntField(env, item, fid_ai_Item_dictionaryHandle, (jint) dictionary);
	setIntField(env, item, fid_ai_Item_dictionaryKey, (jint) key);
	// Increase reference counter for the item dictionary.
	// It's decreased in Item#finalize
	if (dictionary != NULL)
		sAIDictionary->AddRef(dictionary);
}

jobject ScriptographerEngine::wrapLayerHandle(JNIEnv *env, AILayerHandle layer,
		AIDocumentHandle doc) {
	// Layer handles are not used in java as Layer is derived from Item. Allways
	// use the first invisible Item group in the layer that contains everything
	// (even in AI, layer seems only be a wrapper around an art group:
	AIArtHandle art;
	if (sAIArt->GetFirstArtOfLayer(layer, &art))
		throw new StringException("Cannot get layer art");
	return wrapArtHandle(env, art, doc, false,
			com_scriptographer_ai_Item_TYPE_LAYER);
}

jobject ScriptographerEngine::wrapDocumentHandle(JNIEnv *env,
		AIDocumentHandle doc) {
	JNI_CHECK_ENV
	return callStaticObjectMethod(env, cls_ai_Document,
			mid_ai_Document_wrapHandle, (jint) doc);
}

jobject ScriptographerEngine::wrapDictionaryHandle(JNIEnv *env,
		AIDictionaryRef dictionary, AIDocumentHandle doc, jobject validation) {
	JNI_CHECK_ENV
	return callStaticObjectMethod(env, cls_ai_Dictionary,
			mid_ai_Dictionary_wrapHandle, (jint) dictionary,
			(jint) (doc ? doc : gWorkingDoc), validation);
}

jobject ScriptographerEngine::wrapLiveEffectParameters(JNIEnv *env,
		AILiveEffectParameters parameters, AIDocumentHandle doc) {
	JNI_CHECK_ENV
	return callStaticObjectMethod(env, cls_ai_LiveEffectParameters,
			mid_ai_LiveEffectParameters_wrapHandle, (jint) parameters,
			(jint) (doc ? doc : gWorkingDoc));
}

/**
 * Wraps the handle in a java object. see the Java function MenuGroup.wrapHandle
 * to see how he cashing of already wrapped objects is handled.
 *
 * throws exceptions
 */
jobject ScriptographerEngine::wrapMenuGroupHandle(JNIEnv *env,
		AIMenuGroup group) {
	JNI_CHECK_ENV
#if kPluginInterfaceVersion < kAI12
	char *groupName;
#else // kPluginInterfaceVersion >= kAI12
	const char *groupName;
#endif // kPluginInterfaceVersion >= kAI12
	if (!sAIMenu->GetMenuGroupName(group, &groupName)) {
		return callStaticObjectMethod(env, cls_ui_MenuGroup,
				mid_ui_MenuGroup_wrapHandle, (jint) group,
				convertString(env, groupName)
		);
	}
	return NULL;
}

/**
 * Wraps the handle in a java object. see the Java function MenuItem.wrapHandle
 * to see how 
 * the cashing of already wrapped objects is handled.
 *
 * throws exceptions
 */
jobject ScriptographerEngine::wrapMenuItemHandle(JNIEnv *env,
		AIMenuItemHandle item) {
	JNI_CHECK_ENV
	AIMenuGroup group;
#if kPluginInterfaceVersion < kAI12
	char *name, *groupName;
	if (!sAIMenu->GetMenuItemName(item, &name) &&
#else // kPluginInterfaceVersion >= kAI12
	const char *name, *groupName;
	if (!sAIMenu->GetMenuItemKeyboardShortcutDictionaryKey(item, &name) &&
#endif // kPluginInterfaceVersion >= kAI12
		!sAIMenu->GetItemMenuGroup(item, &group) &&
		!sAIMenu->GetMenuGroupName(group, &groupName)) {
		return callStaticObjectMethod(env, cls_ui_MenuItem,
			mid_ui_MenuItem_wrapHandle,
			(jint) item, convertString(env, name),
			(jint) group, convertString(env, groupName)
		);
	}
	return NULL;
}
		
/**
 * onSelectionChanged is fired in the following situations:
 * when either a change in the selected art objects occurs or an artwork
 * modification such as moving a point on a path occurs. In other words EITHER
 * something was selected or deselected or targeted or untargeted, OR some
 * aspect of the current selected  object(s) changed. 
 * It calls Item.onSelectionChanged with an array containing all the affected
 * artHandles, which then increases the version variable of already wrapped
 * objects
 */
ASErr ScriptographerEngine::onSelectionChanged() {
	JNIEnv *env = getEnv();
	try {
		ASErr error;
//		long t = getNanoTime();
		AIArtHandle **matches;
		ai::int32 numMatches;
		RETURN_ERROR(sAIMatchingArt->GetSelectedArt(&matches, &numMatches));
		jintArray artHandles;
		if (numMatches > 0) {
			// pass an array of twice the size to the java side:
			// i + 0 contains the current handle
			// i + 1 contains the previous handle, in case the object was
			// wrapped earlier and its handle was changed  in the meantime.
			// Like this, the java side can update its wrappers. The previous
			// handle's value is stored in the art's dictionary which is copied
			// over to the knew instance by illustrator automatically.
			
			// New instances are created by illustrator when objects are moved,
			// rotated with the rotate tool, etc. why it's doing this i don't
			// know...

			// The maximum possible amount of handles is the amount of
			// matches x 2, as each wrapped art object needs two handles:
			jint *handles = new jint[numMatches * 2];
			int count = 0;
			for (int i = 0; i < numMatches; i++) {
				AIArtHandle art = (*matches)[i];
				AIArtHandle prevArt = NULL;
				// See if the object already was wrapped earlier and its handle
				// was changed due to manipulations by the user if that's the
				// case, let the java side know about the handle change.
				AIDictionaryRef artDict;
				if (!sAIArt->GetDictionary(art, &artDict)) {
					if (sAIDictionary->IsKnown(artDict, m_artHandleKey)) {
						bool changed = true;
						if (!sAIDictionary->GetIntegerEntry(artDict,
								m_artHandleKey, (ASInt32 *) &prevArt)
								&& prevArt == art) {
							prevArt = NULL;
							changed = false;
						}
						// Set the new handle:
						if (changed)
							sAIDictionary->SetIntegerEntry(artDict,
									m_artHandleKey, (ASInt32) art);
						// Only if the m_artHandleKey was set before, this
						// object was wrapped
						handles[count++] = (jint) art;
						handles[count++] = (jint) prevArt;
					}
					sAIDictionary->Release(artDict);
				}
			}
			// TODO: Does this need disposing even if numMatches == 0?
			RETURN_ERROR(sAIMDMemory->MdMemoryDisposeHandle((void **) matches));
			artHandles = env->NewIntArray(count);
			env->SetIntArrayRegion(artHandles, 0, count, (jint *) handles);
			delete handles;
		} else {
			artHandles = NULL;
		}

		// Pass undoLevel and redoLevel to onSelectionChanged as well.
		ai::int32 undoLevel, redoLevel;
		RETURN_ERROR(sAIUndo->CountTransactions(&undoLevel, &redoLevel));
		AIDocumentHandle doc;
		RETURN_ERROR(sAIDocument->GetDocument(&doc));
		jobject document = gEngine->wrapDocumentHandle(env, doc);
		callVoidMethod(env, document, mid_ai_Document_onSelectionChanged,
				artHandles, undoLevel, redoLevel);
//		println(env, "%i", (getNanoTime() - t) / 1000000);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

		
ASErr ScriptographerEngine::onDocumentClosed(AIDocumentHandle handle) {
	JNIEnv *env = getEnv();
	try {
		jobject document = gEngine->wrapDocumentHandle(env, handle);
		callVoidMethod(env, document, mid_ai_Document_onClosed);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}
		
ASErr ScriptographerEngine::onUndo() {
	JNIEnv *env = getEnv();
	try {
		ASErr error;
		ai::int32 undoLevel, redoLevel;
		RETURN_ERROR(sAIUndo->CountTransactions(&undoLevel, &redoLevel));
		AIDocumentHandle doc;
		RETURN_ERROR(sAIDocument->GetDocument(&doc));
		jobject document = gEngine->wrapDocumentHandle(env, doc);
		callVoidMethod(env, document, mid_ai_Document_onUndo, undoLevel, redoLevel);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

ASErr ScriptographerEngine::onRedo() {
	JNIEnv *env = getEnv();
	try {
		ASErr error;
		ai::int32 undoLevel, redoLevel;
		RETURN_ERROR(sAIUndo->CountTransactions(&undoLevel, &redoLevel));
		AIDocumentHandle doc;
		RETURN_ERROR(sAIDocument->GetDocument(&doc));
		jobject document = gEngine->wrapDocumentHandle(env, doc);
		callVoidMethod(env, document, mid_ai_Document_onRedo, undoLevel, redoLevel);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

ASErr ScriptographerEngine::onClear() {
	JNIEnv *env = getEnv();
	try {
		ASErr error;
		AIDocumentHandle doc;
		RETURN_ERROR(sAIDocument->GetDocument(&doc));
		if (doc != NULL) {
			AIArtHandle **matches;
			ai::int32 numMatches;
			RETURN_ERROR(sAIMatchingArt->GetSelectedArt(&matches, &numMatches));
			jintArray artHandles;
			if (numMatches > 0) {
				jint *handles = new jint[numMatches];
				for (int i = 0; i < numMatches; i++)
					handles[i] = (jint) (*matches)[i];
				// TODO: Does this need disposing even if m_numClearItems == 0?
				RETURN_ERROR(sAIMDMemory->MdMemoryDisposeHandle((void **) matches));
				artHandles = env->NewIntArray(numMatches);
				env->SetIntArrayRegion(artHandles, 0, numMatches, (jint *) handles);
				delete handles;
			} else {
				artHandles = NULL;
			}
			AIDocumentHandle doc;
			RETURN_ERROR(sAIDocument->GetDocument(&doc));
			jobject document = gEngine->wrapDocumentHandle(env, doc);
			callVoidMethod(env, document, mid_ai_Document_onClear, artHandles);
		}
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

ASErr ScriptographerEngine::onRevert() {
	JNIEnv *env = getEnv();
	try {
		ASErr error;
		// Filter out canceling the revert command by detecting still
		// modified document
		ASBoolean modified;
		RETURN_ERROR(sAIDocument->GetDocumentModified(&modified));
		if (!modified) {
			AIDocumentHandle doc;
			RETURN_ERROR(sAIDocument->GetDocument(&doc));
			jobject document = gEngine->wrapDocumentHandle(env, doc);
			callVoidMethod(env, document, mid_ai_Document_onRevert);
		}
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

#ifdef WIN_ENV

ASErr ScriptographerEngine::deactivateActiveDialog() {
	JNIEnv *env = getEnv();
#ifndef ADM_FREE
	try {
		callStaticVoidMethod(env, cls_adm_Dialog, mid_adm_Dialog_deactivateActiveDialog);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
#endif //#ifndef ADM_FREE
	return kExceptionErr;
}

#endif // WIN_ENV

/**
 * AI Tool
 *
 */

ASErr ScriptographerEngine::Tool_onHandleEvent(const char * selector,
		AIToolMessage *message) {
	JNIEnv *env = getEnv();
	try {
		// TODO: Wrap additional mesage->event properties too and add support
		// for these:
		/*
		// For graphic tablets, tangential pressure on the finger wheel of the
		// airbrush tool.
		AIToolPressure stylusWheel;
		// How the tool is angled, also called altitude or elevation.
		AIToolAngle tilt;
		// The direction of tilt, measured clockwise in degrees around the
		// Z axis, also called azimuth,
		AIToolAngle bearing;
		// Rotation of the tool, measured clockwise in degrees around the tool's
		// barrel.
		AIToolAngle rotation;
		*/
		jint cursorId = callStaticIntMethod(env, cls_ai_Tool,
				mid_ai_Tool_onHandleEvent,
				(jint) message->tool, convertString(env, selector),
				(jdouble) message->cursor.h, (jdouble) message->cursor.v,
				(jint) message->pressure,
				(jint) (message->event != NULL ? message->event->modifiers : 0));
		if (cursorId)
			gPlugin->setCursor(cursorId);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

/**
 * AI LiveEffect
 */

ASErr ScriptographerEngine::LiveEffect_onEditParameters(
		AILiveEffectEditParamMessage *message) {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_ai_LiveEffect,
				mid_ai_LiveEffect_onEditParameters,
				(jint) message->effect, (jint) message->parameters);
		if (message->allowPreview)
			sAILiveEffect->UpdateParameters(message->context);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

ASErr ScriptographerEngine::LiveEffect_onCalculate(
		AILiveEffectGoMessage *message) {
	JNIEnv *env = getEnv();
	try {
		message->art = (AIArtHandle) callStaticIntMethod(env, cls_ai_LiveEffect,
				mid_ai_LiveEffect_onCalculate,
				(jint) message->effect,
				// Do not check wrappers as the art items in live effects are
				// duplicated and therefore seem to contain the m_artHandleKey,
				// causing wrapped to be set to true when Item#wrapHandle is
				// called. And sometimes their handles are reused, causing reuse
				// of wrong wrappers.
				// We could call Item_clearArtHandles but that's slow. Passing
				// false for checkWrapped should do it.
				wrapArtHandle(env, message->art, NULL, true, -1, false),
				(jint) message->parameters);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

ASErr ScriptographerEngine::LiveEffect_onInterpolate(
		AILiveEffectInterpParamMessage *message) {
	// TODO: define
	return kNoErr;
}

ASErr ScriptographerEngine::LiveEffect_onGetInputType(
		AILiveEffectInputTypeMessage *message) {
	JNIEnv *env = getEnv();
	try {
		message->typeMask = callStaticIntMethod(env, cls_ai_LiveEffect,
				mid_ai_LiveEffect_onGetInputType,
				(jint) message->effect,
				(jint) message->inputArt,
				(jint) message->parameters);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

/**
 * AI MenuItem
 *
 */

ASErr ScriptographerEngine::MenuItem_onSelect(AIMenuMessage *message) {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_ui_MenuItem, mid_ui_MenuItem_onSelect,
				(jint) message->menuItem);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

ASErr ScriptographerEngine::MenuItem_onUpdate(AIMenuMessage *message,
		long inArtwork, long isSelected, long isTrue) {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_ui_MenuItem, mid_ui_MenuItem_onUpdate,
				(jint) message->menuItem, (jint) inArtwork, (jint) isSelected,
				(jint) isTrue);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

/**
 * AI Annotator
 *
 */

ASErr ScriptographerEngine::Annotator_onDraw(AIAnnotatorMessage *message) {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_ai_Annotator, mid_ai_Annotator_onDraw,
				(jint) message->annotator, (jint) message->port,
				(jint) message->view, (jint) gWorkingDoc);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

ASErr ScriptographerEngine::Annotator_onInvalidate(AIAnnotatorMessage *message) {
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_ai_Annotator,
				mid_ai_Annotator_onInvalidate,
				(jint) message->annotator);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

#ifndef ADM_FREE

/**
 * ADM CallbackListener
 *
 */
void ScriptographerEngine::callOnNotify(jobject handler,
		ADMNotifierRef notifier) {
	char type[64];
	sADMNotifier->GetNotifierType(notifier, type, 64);
	callOnNotify(handler, type);
}

void ScriptographerEngine::callOnNotify(jobject handler, char *notifier) {
	JNIEnv *env = getEnv();
	AppContext context;
	callVoidMethodReport(env, handler, mid_adm_NotificationHandler_onNotify,
			env->NewStringUTF(notifier));
}

void ScriptographerEngine::callOnDestroy(jobject handler) {
	callOnNotify(handler, kADMDestroyNotifier);
}

bool ScriptographerEngine::callOnTrack(jobject handler, ADMTrackerRef tracker) {
	JNIEnv *env = getEnv();
	try {
		AppContext context;
		jobject trackerObj = getObjectField(env, handler,
				fid_adm_NotificationHandler_tracker);
		ADMPoint pt;
		sADMTracker->GetPoint(tracker, &pt);
		return callBooleanMethod(env, trackerObj,
				mid_adm_Tracker_onTrack, handler,
				(jint) tracker, (jint) sADMTracker->GetAction(tracker),
				(jint) sADMTracker->GetModifiers(tracker), pt.h, pt.v,
				(jint) sADMTracker->GetMouseState(tracker),
				(jint) sADMTracker->GetVirtualKey(tracker),
				(jchar) sADMTracker->GetCharacter(tracker),
				(jlong) sADMTracker->GetTime(tracker));
	} EXCEPTION_CATCH_REPORT(env);
	return true;
}

bool ScriptographerEngine::callOnDraw(jobject handler, ADMDrawerRef drawer) {
	JNIEnv *env = getEnv();
	try {
		AppContext context;
		jobject drawerObj = getObjectField(env, handler,
				fid_adm_NotificationHandler_drawer);
		setIntField(env, drawerObj, fid_ui_NativeObject_handle, (jint) drawer);
		return callBooleanMethod(env, handler,
				mid_adm_NotificationHandler_onDraw, drawerObj);
	} EXCEPTION_CATCH_REPORT(env);
	return true;
}

int ScriptographerEngine::getADMObjectHandle(JNIEnv *env, jobject obj,
		const char *name) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	int handle = getIntField(env, obj, fid_ui_NativeObject_handle);
	if (!handle) {
		// For HierarchyListBoxes it could be that the user wants to call item
		// functions on a child list. report that this can only be called on the
		// root list:
		if (env->IsInstanceOf(obj, cls_adm_HierarchyListBox)) {
			throw new StringException("This function can only be called on the root hierarchy list.");
		} else {
			throw new StringException("The %s is no longer valid. Use isValid() checks to avoid this error.", name);
		}
	}
	return handle;
}

int ScriptographerEngine::getADMListHandle(JNIEnv *env, jobject obj,
		const char *name) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	int handle = getIntField(env, obj, fid_adm_ListItem_listHandle);
	if (!handle)
		throw new StringException("The %s is no longer valid. Use isValid() checks to avoid this error.", name);
	return handle;
}

jobject ScriptographerEngine::getDialogObject(ADMDialogRef dlg) {
	jobject obj = NULL;
	if (dlg != NULL && sADMDialog != NULL) {
		obj = (jobject) sADMDialog->GetUserData(dlg);
		if (obj == NULL) throw new StringException("The dialog is not linked to a scripting object.");
	}
	return obj;
}

jobject ScriptographerEngine::getItemObject(ADMItemRef item) {
	jobject obj = NULL;
	if (item != NULL && sADMItem != NULL) {
		obj = (jobject) sADMItem->GetUserData(item);
		if (obj == NULL) throw new StringException("The item is not linked to a scripting object.");
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
		if (obj == NULL) throw new StringException("The list is not linked to a scripting object.");
	}
	return obj;
}

jobject ScriptographerEngine::getListObject(ADMHierarchyListRef list) {
	jobject obj = NULL;
	if (list != NULL && sADMHierarchyList != NULL) {
		obj = (jobject) sADMHierarchyList->GetUserData(list);
		if (obj == NULL) throw new StringException("The list is not linked to a scripting object.");
	}
	return obj;
}

jobject ScriptographerEngine::getListEntryObject(ADMEntryRef entry) {
	jobject obj = NULL;
	if (entry != NULL && sADMEntry != NULL) {
		obj = (jobject) sADMEntry->GetUserData(entry);
		if (obj == NULL) throw new StringException("The entry is not linked to a scripting object.");
	}
	return obj;
}

jobject ScriptographerEngine::getListEntryObject(ADMListEntryRef entry) {
	jobject obj = NULL;
	if (entry != NULL && sADMListEntry != NULL) {
		obj = (jobject) sADMListEntry->GetUserData(entry);
		if (obj == NULL) throw new StringException("The entry is not linked to a scripting object.");
	}
	return obj;
}
#endif //#ifndef ADM_FREE

ASErr ScriptographerEngine::callOnHandleEvent(int event) {
	AppContext context;
	JNIEnv *env = getEnv();
	try {
		callStaticVoidMethod(env, cls_ScriptographerEngine,
				mid_ScriptographerEngine_onHandleEvent, event);
		return kNoErr;
	} EXCEPTION_CATCH_REPORT(env);
	return kExceptionErr;
}

bool ScriptographerEngine::callOnHandleKeyEvent(int type, ASUInt32 keyCode,
		ASUnicode character, ASUInt32 modifiers) {
	AppContext context;
	JNIEnv *env = getEnv();
	try {
		return callStaticBooleanMethodReport(NULL, cls_ScriptographerEngine,
				mid_ScriptographerEngine_onHandleKeyEvent,
				type, keyCode, character, modifiers);
	} EXCEPTION_CATCH_REPORT(env);
	return false;
}

int ScriptographerEngine::getMenuObjectHandle(JNIEnv *env, jobject obj,
		const char *name) {
	if (obj == NULL)
		return NULL;
	JNI_CHECK_ENV
	int handle = getIntField(env, obj, fid_ui_NativeObject_handle);
	if (!handle) {
		// For HierarchyListBoxes it could be that the user wants to call item
		// functions on a child list. report that this can only be called on the
		// root list:
#ifndef ADM_FREE
		if (env->IsInstanceOf(obj, cls_adm_HierarchyListBox)) {
			throw new StringException("This function can only be called on the root hierarchy list.");
		} else 
#endif //#ifndef ADM_FREE		
		{
			throw new StringException("The %s is no longer valid. Use isValid() checks to avoid this error.", name);
		}
	}
	return handle;
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
 * Throws an exception with the given name (like
 * "com/scriptographer/ScriptographerException") and message.
 *
 * throws exceptions
 */
void ScriptographerEngine::throwException(JNIEnv *env, const char* name,
		const char* msg) {
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
 * Uses the Scriptographer loader instead of the default JNI one, because
 * otherwise classes for reflection and registering of natives won't be found!
 * The JNI loader can only be used for loading the Loader, because that's the
 * only thing initially on the classpath.
 */
jclass ScriptographerEngine::findClass(JNIEnv *env, const char *name) {
	JNI_CHECK_ENV
	jclass cls = NULL;
	if (cls_Loader != NULL) {
		// Try loading using the URL classloader first:
		cls = (jclass) callStaticObjectMethod(env, cls_Loader,
				mid_Loader_loadClass, env->NewStringUTF(name));
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

jmethodID ScriptographerEngine::getMethodID(JNIEnv *env, jclass cls,
		const char *name, const char *signature) {
	JNI_CHECK_ENV
	jmethodID res = env->GetMethodID(cls, name, signature);
	if (res == NULL) EXCEPTION_CHECK(env);
	return res;
}

jmethodID ScriptographerEngine::getStaticMethodID(JNIEnv *env, jclass cls,
		const char *name, const char *signature) {
	JNI_CHECK_ENV
	jmethodID res = env->GetStaticMethodID(cls, name, signature);
	if (res == NULL) EXCEPTION_CHECK(env);
	return res;
}

jmethodID ScriptographerEngine::getConstructorID(JNIEnv *env, jclass cls,
		const char *signature) {
	return getMethodID(env, cls, "<init>", signature);
}

jfieldID ScriptographerEngine::getFieldID(JNIEnv *env, jclass cls,
		const char *name, const char *signature) {
	JNI_CHECK_ENV
	jfieldID res = env->GetFieldID(cls, name, signature);
	if (res == NULL) EXCEPTION_CHECK(env);
	return res;
}

jfieldID ScriptographerEngine::getStaticFieldID(JNIEnv *env, jclass cls,
		const char *name, const char *signature) {
	JNI_CHECK_ENV
	jfieldID res = env->GetStaticFieldID(cls, name, signature);
	if (res == NULL) EXCEPTION_CHECK(env);
	return res;
}

jobject ScriptographerEngine::newObject(JNIEnv *env, jclass cls,
		jmethodID ctor, ...) {
	JNI_CHECK_ENV
	JNI_ARGS_BEGIN(ctor)
	jobject res = env->NewObjectV(cls, ctor, args);
	JNI_ARGS_END
	if (res == NULL) EXCEPTION_CHECK(env);
	return res;
}

jboolean ScriptographerEngine::isEqual(JNIEnv *env, jobject obj1, jobject obj2) {
	if (obj1 == NULL)
		return obj2 == NULL;
	return callBooleanMethod(env, obj1, gEngine->mid_Object_equals, obj2);
}

// declare macro functions now:

JNI_DEFINE_GETFIELD_FUNCTIONS
JNI_DEFINE_SETFIELD_FUNCTIONS
JNI_DEFINE_GETSTATICFIELD_FUNCTIONS
JNI_DEFINE_CALLMETHOD_FUNCTIONS