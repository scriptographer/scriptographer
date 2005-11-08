#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_TextFrame.h"

/*
 * com.scriptographer.ai.TextFrame
 */
 
using namespace ATE;

/*
 * int getOrientation()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextFrame_getOrientation(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle text = gEngine->getArtHandle(env, obj);
		AITextOrientation orient;
		if (!sAITextFrame->GetOrientation(text, &orient))
			return (jint) orient;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void setOrientation(int orient)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextFrame_setOrientation(JNIEnv *env, jobject obj, jint orient) {
	try {
		AIArtHandle text = gEngine->getArtHandle(env, obj);
		sAITextFrame->SetOrientation(text, (AITextOrientation) orient);
	} EXCEPTION_CONVERT(env)
}

/*
 * com.scriptographer.ai.Art createOutline()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_TextFrame_createOutline(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle text = gEngine->getArtHandle(env, obj);
		AIArtHandle outline;
		if (!sAITextFrame-> CreateOutline(text, &outline))
			return gEngine->wrapArtHandle(env, outline);
	} EXCEPTION_CONVERT(env)
	return NULL;
}


/*
 * boolean link(com.scriptographer.ai.TextFrame text)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_TextFrame_link(JNIEnv *env, jobject obj, jobject text) {
	try {
		AIArtHandle text1 = gEngine->getArtHandle(env, obj);
		AIArtHandle text2 = gEngine->getArtHandle(env, text);
		if (text2 != NULL && !sAITextFrame->Link(text1, text2))
			return true;
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * boolean unlinkBefore()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_TextFrame_unlinkBefore(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle text = gEngine->getArtHandle(env, obj);
		if (!sAITextFrame->Unlink(text, true, false))
			return true;
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * boolean unlinkAfter()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_TextFrame_unlinkAfter(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle text = gEngine->getArtHandle(env, obj);
		if (!sAITextFrame->Unlink(text, false, true))
			return true;
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * boolean isLinked()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_TextFrame_isLinked(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle text = gEngine->getArtHandle(env, obj);
		bool linked;
		if (!sAITextFrame->PartOfLinkedText(text, &linked))
			return linked;
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * int getStoryIndex()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextFrame_getStoryIndex(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle text = gEngine->getArtHandle(env, obj);
		long index;
		if (!sAITextFrame->GetStoryIndex(text, &index))
			return index;
	} EXCEPTION_CONVERT(env)
	return -1;
}

/*
 * int getIndex()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextFrame_getIndex(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle text = gEngine->getArtHandle(env, obj);
		long index;
		if (!sAITextFrame->GetFrameIndex(text, &index))
			return index;
	} EXCEPTION_CONVERT(env)
	return -1;
}

/*
 * com.scriptographer.ai.TextRange getSelection()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_TextFrame_getSelection(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle text = gEngine->getArtHandle(env, obj);
		TextRangesRef ranges;
		if (!sAITextFrame->GetATETextSelection(text, &ranges))
			return TextRange_convertTextRanges(env, ranges);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.ai.TextRange nativeGetRange(boolean bIncludeOverflow)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_TextFrame_nativeGetRange(JNIEnv *env, jobject obj, jboolean bIncludeOverflow) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		TextRangeRef range;
		if (!sTextFrame->GetTextRange(frame, bIncludeOverflow, &range))
			return gEngine->wrapTextRangeRef(env, range);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * float getSpacing()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_TextFrame_getSpacing(JNIEnv *env, jobject obj) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		ASReal spacing;
		if (!sTextFrame->GetSpacing(frame, &spacing))
			return spacing;
	} EXCEPTION_CONVERT(env)
	return 0.0;
}

/*
 * void setSpacing(float spacing)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextFrame_setSpacing(JNIEnv *env, jobject obj, jfloat spacing) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		sTextFrame->SetSpacing(frame, spacing);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean getOpticalAlignment()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_TextFrame_getOpticalAlignment(JNIEnv *env, jobject obj) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		bool active;
		if (!sTextFrame->GetOpticalAlignment(frame, &active))
			return active;
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setOpticalAlignment(boolean active)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextFrame_setOpticalAlignment(JNIEnv *env, jobject obj, jboolean active) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		sTextFrame->SetOpticalAlignment(frame, active);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean equals(java.lang.Object text)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_TextFrame_equals(JNIEnv *env, jobject obj, jobject text) {
	try {
		if (env->IsInstanceOf(text, gEngine->cls_TextFrame)) {
			TextFrameRef frame1 = gEngine->getTextFrameRef(env, obj);
			TextFrameRef frame2 = gEngine->getTextFrameRef(env, text);
			if (frame2 != NULL) {
				bool ret;
				if (!sTextFrame->IsEqual(frame1, frame2, &ret))
					return ret;
			}
		}
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}
