#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_PatternList.h"

/*
 * com.scriptographer.ai.PatternList
 */

/*
 * int nativeGetLength(int docHandle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_PatternList_nativeGetLength(JNIEnv *env, jclass cls, jint docHandle) {
	long count = 0;
	try {
		Document_activate((AIDocumentHandle) docHandle);
		sAIPattern->CountPatterns(&count);
	} EXCEPTION_CONVERT(env);
	return count;
}

/*
 * int nativeGet(int docHandle, int index)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_PatternList_nativeGet__II(JNIEnv *env, jclass cls, jint docHandle, jint index) {
	AIPatternHandle pattern = NULL;
	try {
		Document_activate((AIDocumentHandle) docHandle);
		sAIPattern->GetNthPattern(index, &pattern);
	} EXCEPTION_CONVERT(env);
	return (jint) pattern;
}

/*
 * int nativeGet(int docHandle, java.lang.String name)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_PatternList_nativeGet__ILjava_lang_String_2(JNIEnv *env, jclass cls, jint docHandle, jstring name) {
	AIPatternHandle pattern = NULL;
	try {
		Document_activate((AIDocumentHandle) docHandle);
#if kPluginInterfaceVersion < kAI12
		unsigned char *str = gEngine->convertString_Pascal(env, name);
		sAIPattern->GetPatternByName(str, &pattern);
		delete str;
#else
		ai::UnicodeString str = gEngine->convertString_UnicodeString(env, name);
		sAIPattern->GetPatternByName(str, &pattern);
#endif
	} EXCEPTION_CONVERT(env);
	return (jint) pattern;
}
