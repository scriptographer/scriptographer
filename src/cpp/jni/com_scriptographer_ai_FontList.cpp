#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_FontList.h"

/*
 * com.scriptographer.ai.FontList
 */

/*
 * int getLength()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_FontList_getLength(JNIEnv *env, jobject obj) {
	try {
		long length;
		if (!sAIFont->CountTypefaces(&length))
			return length;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * int nativeGet(int index)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_FontList_nativeGet(JNIEnv *env, jclass cls, jint index) {
	try {
		AITypefaceKey key;
		if (!sAIFont->IndexTypefaceList(index, &key))
			return (jint) key;
	} EXCEPTION_CONVERT(env)
	return 0;
}
