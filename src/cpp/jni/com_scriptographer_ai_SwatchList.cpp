#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_SwatchList.h"

/*
 * com.scriptographer.ai.SwatchList
 */

/*
 * int nativeGetLength(int docHandle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_SwatchList_nativeGetLength(JNIEnv *env, jclass cls, jint docHandle) {
	try {
		AISwatchListRef list = NULL;
		if (!sAISwatchList->GetSwatchList((AIDocumentHandle) docHandle, &list))
			return sAISwatchList->CountSwatches(list);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeGet(int docHandle, int index)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_SwatchList_nativeGet__II(JNIEnv *env, jclass cls, jint docHandle, jint index) {
	try {
		AISwatchListRef list = NULL;
		if (!sAISwatchList->GetSwatchList((AIDocumentHandle) docHandle, &list))
			return (jint) sAISwatchList->GetNthSwatch(list, index);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeGet(int docHandle, java.lang.String name)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_SwatchList_nativeGet__ILjava_lang_String_2(JNIEnv *env, jclass cls, jint docHandle, jstring name) {
	AISwatchRef ret = NULL;
	try {
		AISwatchListRef list = NULL;
		if (!sAISwatchList->GetSwatchList((AIDocumentHandle) docHandle, &list)) {
#if kPluginInterfaceVersion < kAI12
			char *str = gEngine->convertString(env, name);
			ret = sAISwatchList->GetSwatchByName(list, str);
			delete str;
#else
			ai::UnicodeString str = gEngine->convertString_UnicodeString(env, name);
			ret = sAISwatchList->GetSwatchByName(list, str);
#endif
		}
	} EXCEPTION_CONVERT(env);
	return (jint) ret;
}
