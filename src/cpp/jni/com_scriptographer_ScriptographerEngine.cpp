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
			strncmp(path, "http://", 7) == 0 ||
			strncmp(path, "https://", 8) == 0) {
			sAIURL->OpenURL(path);
		} else {
#ifdef WIN_ENV

			SHELLEXECUTEINFO info;
			memset(&info, 0, sizeof(SHELLEXECUTEINFO));
			info.cbSize = sizeof(SHELLEXECUTEINFO);
			info.lpFile = path;
			info.nShow = SW_SHOW;
			
			result = ShellExecuteEx(&info);

#elif MAC_ENV

			// SEE http://developer.apple.com/technotes/tn/tn1002.html
			SPPlatformFileSpecification fileSpec;
			gPlugin->pathToFileSpec(path, &fileSpec);
			FSSpec spec;
			FSMakeFSSpec(fileSpec.vRefNum, fileSpec.parID, fileSpec.name, &spec);

			AppleEvent theAEvent, theReply;
			AEAddressDesc fndrAddress;
			AEDescList targetListDesc;
			OSType fndrCreator;
			AliasHandle targetAlias;
			
			// set up locals 
			AECreateDesc(typeNull, NULL, 0, &theAEvent);
			AECreateDesc(typeNull, NULL, 0, &fndrAddress);
			AECreateDesc(typeNull, NULL, 0, &theReply);
			AECreateDesc(typeNull, NULL, 0, &targetListDesc);
			targetAlias = NULL;
			fndrCreator = 'MACS';
			
			// create an open documents event targeting the finder
			result = AECreateDesc(typeApplSignature, (Ptr) &fndrCreator,
				sizeof(fndrCreator), &fndrAddress) == noErr;
			if (!result) goto error;
			result = AECreateAppleEvent(kCoreEventClass, kAEOpenDocuments,
				&fndrAddress, kAutoGenerateReturnID,
				kAnyTransactionID, &theAEvent) == noErr;
			if (!result) goto error;
			
			// create the list of files to open
			result = AECreateList(NULL, 0, false, &targetListDesc) == noErr;
			if (!result) goto error;
			result = NewAlias(NULL, &spec, &targetAlias) == noErr;
			if (!result) goto error;
			HLock((Handle) targetAlias);
			result = AEPutPtr(&targetListDesc, 1, typeAlias, *targetAlias, GetHandleSize((Handle) targetAlias)) == noErr;
			HUnlock((Handle) targetAlias);
			if (!result) goto error;
			
			// add the file list to the apple event
			result = AEPutParamDesc(&theAEvent, keyDirectObject, &targetListDesc) == noErr;
			if (!result) goto error;

			// send the event to the Finder
			result = AESend(&theAEvent, &theReply, kAENoReply,
				kAENormalPriority, kAEDefaultTimeout, NULL, NULL) == noErr;

			// clean up and leave
error:
			if (targetAlias != NULL)
				DisposeHandle((Handle) targetAlias);
			AEDisposeDesc(&targetListDesc);
			AEDisposeDesc(&theAEvent);
			AEDisposeDesc(&fndrAddress);
			AEDisposeDesc(&theReply);

#endif

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

#ifdef MAC_ENV
// table that converts java keycodes to mac keycodes:
unsigned char keycodeToMac[256] = {
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
#endif

/*
 * boolean isKeyDown(short keycode)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ScriptographerEngine_isKeyDown(JNIEnv *env, jclass cls, jshort keycode) {
	try {
#ifdef MAC_ENV
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
#elif WIN_ENV
		return GetAsyncKeyState(keycode) & 0x8000) == 0x8000;
#endif
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
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
