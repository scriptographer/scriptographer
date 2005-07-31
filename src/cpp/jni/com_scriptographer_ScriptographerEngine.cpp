#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "Plugin.h"
#include "com_scriptographer_ScriptographerEngine.h"

/*
 * com.scriptographer.ScriptographerEngine
 */

/*
 * java.lang.String nativeReload()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ScriptographerEngine_nativeReload(JNIEnv *env, jclass cls) {
	try {
		return gEngine->reloadEngine();
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * boolean launch(java.lang.String filename)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ScriptographerEngine_launch(JNIEnv *env, jclass cls, jstring filename) {
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
	} EXCEPTION_CONVERT(env)
	if (path != NULL)
		delete path;
	return result;
}

/*
 * double getNanoTime()
 */

JNIEXPORT jlong JNICALL Java_com_scriptographer_ScriptographerEngine_getNanoTime(JNIEnv *env, jclass cls) {
#ifdef MAC_ENV
	Nanoseconds nano = AbsoluteToNanoseconds(UpTime());
	return UnsignedWideToUInt64(nano);
#elif WIN_ENV
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

/*
 * com.scriptographer.Point getMousePoint()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ScriptographerEngine_getMousePoint(JNIEnv *env, jclass cls) {
	try {
		// get the mousepoint in screencoordinates from the ADMTracker:
		ADMPoint pt;
		sADMTracker->GetPoint(NULL, &pt);
		// now we need to convert the point to a artworkPoint.
		// this is tricky: first we need to find the point in the window
		AIDocumentViewHandle view;
		if (!sAIDocumentView->GetNthDocumentView(0, &view)) {
#ifdef MAC_ENV
			GrafPtr port;
			GetPort(&port);
			SetPort(GetWindowPort(ActiveNonFloatingWindow()));
			GlobalToLocal((Point *)&pt);
			SetPort(port);
#elif WIN_ENV
			// on windows, the current document window is the app windows first child-child window...
			// i didn't find a better way for the conversion of the mouse position...
			HWND wnd = GetTopWindow(GetTopWindow((HWND)sADMWinHost->GetPlatformAppWindow()));
			ScreenToClient(wnd, (LPPOINT)&pt);
#endif					// the rest of the conversion is easy: 
			AIRealPoint point;
			sAIDocumentView->ViewPointToArtworkPoint(view, &pt, &point);
			return gEngine->convertPoint(env, &point);
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}
