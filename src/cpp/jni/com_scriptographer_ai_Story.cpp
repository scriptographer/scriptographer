#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Story.h"

/*
 * com.scriptographer.ai.Story
 */

using namespace ATE;

/*
 * void finailze()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Story_finailze(JNIEnv *env, jobject obj) {
	try {
		sStory->Release(gEngine->getStoryRef(env, obj));
	} EXCEPTION_CONVERT(env)
}

/*
 * com.scriptographer.ai.TextRange getRange()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Story_getRange(JNIEnv *env, jobject obj) {
	try {
		StoryRef story = gEngine->getStoryRef(env, obj);
		TextRangeRef range;
		if (!sStory->GetTextRange_ForThisStory(story, &range))
			return gEngine->wrapTextRangeRef(env, range);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.ai.TextRange getSelection()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Story_getSelection(JNIEnv *env, jobject obj) {
	try {
		StoryRef story = gEngine->getStoryRef(env, obj);
		TextRangesRef ranges;
		if (!sStory->GetTextSelection(story, &ranges))
			return textRangeConvertTextRanges(env, ranges);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * boolean equals(java.lang.Object story)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Story_equals(JNIEnv *env, jobject obj, jobject story) {
	try {
		if (env->IsInstanceOf(story, gEngine->cls_Story)) {
			StoryRef story1 = gEngine->getStoryRef(env, obj);
			StoryRef story2 = gEngine->getStoryRef(env, story);
			if (story2 != NULL) {
				bool ret;
				if (!sStory->IsEqual(story1, story2, &ret))
					return ret;
			}
		}
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * int nativeGetTexListLength(int handle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Story_nativeGetTexListLength(JNIEnv *env, jobject obj, jint handle) {
	try {
		// determine amount of TextFrames by looping through the iterator
		TextFramesIteratorRef framesRef;
		if (!sStory->GetTextFramesIterator((StoryRef) handle, &framesRef)) {
			ITextFramesIterator frames(framesRef);
			int length = 0;
			while (frames.IsNotDone()) {
				frames.Next();
				length++;
			}
			return length;
		}
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * com.scriptographer.ai.Text nativeGetText(int handle, int index, com.scriptographer.ai.Text curText)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Story_nativeGetText(JNIEnv *env, jobject obj, jint handle, jint index, jobject curText) {
	try {
		TextFrameRef textRef;
		if (!sStory->GetFrame((StoryRef) handle, index, &textRef)) {
			TextFrameRef curTextRef = gEngine->getTextFrameRef(env, curText);
			bool equal;
			// check if it's the same story as before, in that case return the old wrapped text
			// this is needed as in ATE, reference handles allways change their values
			if (curTextRef != NULL && !sTextFrame->IsEqual(textRef, curTextRef, &equal) && equal) {
				return curText;
			}
			// if we're still here, we need to wrap the text:
			// determine AIArtHandle:
			AIArtHandle textHandle;
			if (!sAITextFrame->GetAITextFrame(textRef, &textHandle))
				return gEngine->wrapArtHandle(env, textHandle);
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}
