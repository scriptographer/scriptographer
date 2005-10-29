#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_TextRanges.h"

/*
 * com.scriptographer.ai.TextRanges
 */

/*
 * void removeAll()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRanges_removeAll(JNIEnv *env, jobject obj) {
	try {
		ATE::TextRangesRef ranges = gEngine->getTextRangesRef(env, obj);
		ATE::sTextRanges->RemoveAll(ranges);
	} EXCEPTION_CONVERT(env)
}

/*
 * int getLength()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRanges_getLength(JNIEnv *env, jobject obj) {
	try {
		TextRangesRef ranges = gEngine->getTextRangesRef(env, obj);
		ASInt32 size;
		if (!ATE::sTextRanges->GetSize(ranges, &size))
			return size;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * java.lang.Object get(int index)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_TextRanges_get(JNIEnv *env, jobject obj, jint index) {
	try {
		ATE::TextRangesRef ranges = gEngine->getTextRangesRef(env, obj);
		ATE::TextRangeRef range;
		if (!ATE::sTextRanges->Item(ranges, index, &range)) {
			return gEngine->wrapTextRangeRef(env, range);
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void finalize()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRanges_finalize(JNIEnv *env, jobject obj) {
	try {
		ATE::sTextRanges->Release(gEngine->getTextRangesRef(env, obj));
	} EXCEPTION_CONVERT(env)
}
