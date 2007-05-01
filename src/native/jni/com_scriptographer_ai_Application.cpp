#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "ScriptographerPlugin.h"
#include "com_scriptographer_ai_Application.h"
#include "com_scriptographer_adm_Key.h"

/*
 * com.scriptographer.ai.Application
 */

/*
 * int getVersion()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Application_getVersion(JNIEnv *env, jclass cls) {
	// TODO: define in more detail!
#if kPluginInterfaceVersion < kAI11
	return 10;
#elseif kPluginInterfaceVersion < kAI12
	return 11;
#elseif kPluginInterfaceVersion < kAI13
	return 12;
#else
	return 13;
#endif
}

/*
 * boolean launch(java.lang.String filename)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Application_launch(JNIEnv *env, jclass cls, jstring filename) {
	if (filename == NULL)
		return false;
	
	char *path = NULL;
	bool result = false;
	
	try {
		path = gEngine->convertString(env, filename);
		if (strlen(path) >= 10 &&
			strncmp(path, "file://", 7) == 0 ||
			strncmp(path, "http://", 7) == 0 ||
			strncmp(path, "https://", 8) == 0 ||
			strncmp(path, "mailto://", 9) == 0) {
			result = !sAIURL->OpenURL(path);
		} else {
			SPPlatformFileSpecification fileSpec;
			if (gPlugin->pathToFileSpec(path, &fileSpec)) {
#if kPluginInterfaceVersion < kAI12
				result = !sAIUser->LaunchApp(&fileSpec, true);
#else
				ai::FilePath filePath(fileSpec);
				result = !sAIUser->LaunchApp(filePath, true);
#endif
			}
		}
	} EXCEPTION_CONVERT(env);
	if (path != NULL)
		delete path;
	return result;
}

/*
 * long getNanoTime()
 */
JNIEXPORT jlong JNICALL Java_com_scriptographer_ai_Application_getNanoTime(JNIEnv *env, jclass cls) {
	return gEngine->getNanoTime();
}

/*
 * void nativeShowProgress(java.lang.String text)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Application_nativeShowProgress(JNIEnv *env, jclass cls, jstring text) {
	try {
#if kPluginInterfaceVersion < kAI12
		char *str = gEngine->convertString(env, text);
		sAIUser->SetProgressText(str);
		delete str;
#else
		ai::UnicodeString str = gEngine->convertString_UnicodeString(env, text);
		sAIUser->SetProgressText(str);
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean nativeUpdateProgress(long current, long max)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Application_nativeUpdateProgress(JNIEnv *env, jclass cls, jlong current, jlong max) {
	try {
		if (gEngine->isKeyDown(com_scriptographer_adm_Key_VK_ESCAPE))
			return false;
		sAIUser->UpdateProgress(current, max);
		return !sAIUser->Cancel();
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean closeProgress()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Application_closeProgress(JNIEnv *env, jclass cls) {
	try {
		sAIUser->CloseProgress();
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void dispatchNextEvent()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Application_dispatchNextEvent(JNIEnv *env, jclass cls) {
	try {
		// Manually process event loop events:
#ifdef MAC_ENV
		// http://developer.apple.com/documentation/Carbon/Conceptual/Carbon_Event_Manager/Tasks/chapter_3_section_12.html
		EventRef event;
		if (ReceiveNextEvent(0, NULL, kEventDurationForever, true, &event) == noErr) {
			SendEventToEventTarget (event, GetEventDispatcherTarget());
            ReleaseEvent(event);
		}
#endif
#ifdef WIN_ENV
		// http://msdn2.microsoft.com/en-US/library/aa452701.aspx
		MSG message;
		if (GetMessage(&message, NULL, 0, 0)) {
			TranslateMessage(&message);
			DispatchMessage(&message);
		}
#endif
	} EXCEPTION_CONVERT(env);
}
