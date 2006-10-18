#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_GradientList.h"

/*
 * com.scriptographer.ai.GradientList
 */

/*
 * int nativeGetLength(int docHandle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_GradientList_nativeGetLength(JNIEnv *env, jclass cls, jint docHandle) {
	try {
		Document_activate((AIDocumentHandle) docHandle);
		long count = 0;
		sAIGradient->CountGradients(&count);
		return count;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeGet(int docHandle, int index)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_GradientList_nativeGet__II(JNIEnv *env, jclass cls, jint docHandle, jint index) {
	try {
		Document_activate((AIDocumentHandle) docHandle);
		AIGradientHandle gradient = NULL;
		sAIGradient->GetNthGradient(index, &gradient);
		return (jint) gradient;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeGet(int docHandle, java.lang.String name)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_GradientList_nativeGet__ILjava_lang_String_2(JNIEnv *env, jclass cls, jint docHandle, jstring name) {
	AIGradientHandle ret = NULL;
	try {
		Document_activate((AIDocumentHandle) docHandle);
#if kPluginInterfaceVersion < kAI12
		char *str = gEngine->convertString(env, name);
		sAIGradient->GetGradientByName(str, &ret);
		delete str;
#else
		ai::UnicodeString str = gEngine->convertString_UnicodeString(env, name);
		sAIGradient->GetGradientByName(str, &ret);
#endif
	} EXCEPTION_CONVERT(env);
	return (jint) ret;
}
