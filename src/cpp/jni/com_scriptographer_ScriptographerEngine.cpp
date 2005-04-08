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
#else
	Nanoseconds nano = AbsoluteToNanoseconds(UpTime());
	return UnsignedWideToUInt64(nano);
#endif
}
