#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_TextRange.h"

/*
 * com.scriptographer.ai.TextRange
 */

/*
 * int getStart()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getStart(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ASInt32 start;
		if (!ATE::sTextRange->GetStart(range, &start))
			return start;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void setStart(int start)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_setStart(JNIEnv *env, jobject obj, jint start) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ATE::sTextRange->SetStart(range, start);
	} EXCEPTION_CONVERT(env)
}

/*
 * int getEnd()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getEnd(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ASInt32 end;
		if (!ATE::sTextRange->GetEnd(range, &end))
			return end;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void setEnd(int end)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_setEnd(JNIEnv *env, jobject obj, jint end) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ATE::sTextRange->SetEnd(range, end);
	} EXCEPTION_CONVERT(env)
}

/*
 * int getSize()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getSize(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ASInt32 size;
		if (!ATE::sTextRange->GetSize(range, &size))
			return size;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * java.lang.String getContent()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_TextRange_getContent(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ASInt32 size;
		if (!ATE::sTextRange->GetSize(range, &size)) {
			ASUnicode *text = new ASUnicode[size];
			if (!ATE::sTextRange->GetContents_AsUnicode(range, text, size, &size)) {
	 			return env->NewString(text, size);
			}
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void insertBefore(java.lang.String text)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_insertBefore__Ljava_lang_String_2(JNIEnv *env, jobject obj, jstring text) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int size = env->GetStringLength(text);
		const jchar *chars = env->GetStringCritical(text, NULL);
		ATE::sTextRange->InsertBefore_AsUnicode(range, chars, size);
		env->ReleaseStringCritical(text, chars); 
	} EXCEPTION_CONVERT(env)
}

/*
 * void insertAfter(java.lang.String text)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_insertAfter__Ljava_lang_String_2(JNIEnv *env, jobject obj, jstring text) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int size = env->GetStringLength(text);
		const jchar *chars = env->GetStringCritical(text, NULL);
		ATE::sTextRange->InsertAfter_AsUnicode(range, chars, size);
		env->ReleaseStringCritical(text, chars); 
	} EXCEPTION_CONVERT(env)
}

/*
 * void insertBefore(com.scriptographer.ai.TextRange range)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_insertBefore__Lcom_scriptographer_ai_TextRange_2(JNIEnv *env, jobject obj, jobject range) {
	try {
		TextRangeRef range1 = gEngine->getTextRangeRef(env, obj);
		TextRangeRef range2 = gEngine->getTextRangeRef(env, range);
		if (range2 != NULL)
			ATE::sTextRange->InsertBefore_AsTextRange(range1, range2);
	} EXCEPTION_CONVERT(env)
}

/*
 * void insertAfter(com.scriptographer.ai.TextRange range)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_insertAfter__Lcom_scriptographer_ai_TextRange_2(JNIEnv *env, jobject obj, jobject range) {
	try {
		TextRangeRef range1 = gEngine->getTextRangeRef(env, obj);
		TextRangeRef range2 = gEngine->getTextRangeRef(env, range);
		if (range2 != NULL)
			ATE::sTextRange->InsertAfter_AsTextRange(range1, range2);
	} EXCEPTION_CONVERT(env)
}

/*
 * void remove()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_remove(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ATE::sTextRange->Remove(range);
	} EXCEPTION_CONVERT(env)
}

/*
 * java.lang.Object clone()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_TextRange_clone(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		TextRangeRef clone;
		if (!ATE::sTextRange->Clone(range, &clone))
			return gEngine->wrapTextRangeRef(env, clone);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * int getSingleGlyph()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getSingleGlyph(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ATE::ATEGlyphID id;
		bool ret;
		if (!ATE::sTextRange->GetSingleGlyphInRange(range, &id, &ret) && ret) {
			return id;
		}
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void select(boolean addToSelection)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_select(JNIEnv *env, jobject obj, jboolean addToSelection) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ATE::sTextRange->Select(range, addToSelection);
	} EXCEPTION_CONVERT(env)
}

/*
 * void deselect()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_deselect(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ATE::sTextRange->DeSelect(range);
	} EXCEPTION_CONVERT(env)
}

/*
 * void changeCase(int type)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_changeCase(JNIEnv *env, jobject obj, jint type) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ATE::sTextRange->ChangeCase(range, (ATE::CaseChangeType) type);
	} EXCEPTION_CONVERT(env)
}

/*
 * void fitHeadlines()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_fitHeadlines(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ATE::sTextRange->FitHeadlines(range);
	} EXCEPTION_CONVERT(env)
}

/*
 * int getCharacterType()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getCharacterType(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ATE::ASCharType type;
		if (!ATE::sTextRange->GetCharacterType(range, &type)) {
			return (jint) type;
		}
	} EXCEPTION_CONVERT(env)
	return -1;
}

/*
 * void finalize()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_finalize(JNIEnv *env, jobject obj) {
	try {
		ATE::sTextRange->Release(gEngine->getTextRangeRef(env, obj));
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean equals(java.lang.Object range)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_TextRange_equals(JNIEnv *env, jobject obj, jobject range) {
	try {
		if (env->IsInstanceOf(range, gEngine->cls_TextRange)) {
			TextRangeRef range1 = gEngine->getTextRangeRef(env, obj);
			TextRangeRef range2 = gEngine->getTextRangeRef(env, range);
			if (range2 != NULL) {
				bool ret;
				if (!ATE::sTextRange->IsEqual(range1, range2, &ret))
					return ret;
			}
		}
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}
