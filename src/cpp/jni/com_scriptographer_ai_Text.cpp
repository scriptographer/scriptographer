#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Text.h"

/*
 * com.scriptographer.ai.Text
 */

/*
 * int getOrientation()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Text_getOrientation(JNIEnv *env, jobject obj) {
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
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Text_setOrientation(JNIEnv *env, jobject obj, jint orient) {
	try {
	    AIArtHandle text = gEngine->getArtHandle(env, obj);
		sAITextFrame->SetOrientation(text, (AITextOrientation) orient);
	} EXCEPTION_CONVERT(env)
}

/*
 * com.scriptographer.ai.Art createOutline()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Text_createOutline(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle text = gEngine->getArtHandle(env, obj);
	    AIArtHandle outline;
		if (!sAITextFrame-> CreateOutline(text, &outline))
			return gEngine->wrapArtHandle(env, outline);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.ai.TextRanges getSelection()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Text_getSelection(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle text = gEngine->getArtHandle(env, obj);
	    TextRangesRef ranges;
		if (!sAITextFrame->GetATETextSelection(text, &ranges)) {
			return gEngine->wrapTextRangesRef(env, ranges);
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.ai.TextRange getRange(boolean bIncludeOverflow)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Text_getRange(JNIEnv *env, jobject obj, jboolean bIncludeOverflow) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		TextRangeRef range;
		if (!ATE::sTextFrame->GetTextRange(frame, bIncludeOverflow, &range)) {
			return gEngine->wrapTextRangeRef(env, range);
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * float getSpacing()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_Text_getSpacing(JNIEnv *env, jobject obj) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		ASReal spacing;
		if (!ATE::sTextFrame->GetSpacing(frame, &spacing))
			return spacing;
	} EXCEPTION_CONVERT(env)
	return 0.0;
}

/*
 * void setSpacing(float spacing)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Text_setSpacing(JNIEnv *env, jobject obj, jfloat spacing) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		ATE::sTextFrame->SetSpacing(frame, spacing);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean getOpticalAlignment()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Text_getOpticalAlignment(JNIEnv *env, jobject obj) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		bool active;
		if (!ATE::sTextFrame->GetOpticalAlignment(frame, &active))
			return active;
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setOpticalAlignment(boolean active)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Text_setOpticalAlignment(JNIEnv *env, jobject obj, jboolean active) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		ATE::sTextFrame->SetOpticalAlignment(frame, active);
	} EXCEPTION_CONVERT(env)
}
