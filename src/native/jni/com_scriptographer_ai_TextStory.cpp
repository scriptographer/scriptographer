/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
 *
 * -- GPL LICENSE NOTICE --
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -- GPL LICENSE NOTICE --
 *
 * $Id$
 */

#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_TextStory.h"

/*
 * com.scriptographer.ai.TextStory
 */

using namespace ATE;

/*
 * void nativeRelease()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextStory_nativeRelease(JNIEnv *env, jobject obj, jint handle) {
	try {
		if (handle)
			sStory->Release((StoryRef) handle);
	} EXCEPTION_CONVERT(env);
}

/*
 * int getIndex()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextStory_getIndex(JNIEnv *env, jobject obj) {
	try {
		StoryRef story = gEngine->getStoryHandle(env, obj);
		ASInt32 index;
		if (!sStory->GetIndex(story, &index))
			return index;
	} EXCEPTION_CONVERT(env);
	return -1;
}

/*
 * int nativeGetRange()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextStory_nativeGetRange(JNIEnv *env, jobject obj) {
	try {
		StoryRef story = gEngine->getStoryHandle(env, obj);
		TextRangeRef range;
		if (!sStory->GetTextRange_ForThisStory(story, &range))
			return (jint) range;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * com.scriptographer.ai.TextRange getRange(int start, int end)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_TextStory_getRange(JNIEnv *env, jobject obj, jint start, jint end) {
	try {
		StoryRef story = gEngine->getStoryHandle(env, obj);
		TextRangeRef range;
		if (!sStory->GetTextRange(story, start, end, &range))
			return gEngine->wrapTextRangeRef(env, range);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.TextRange getSelection()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_TextStory_getSelection(JNIEnv *env, jobject obj) {
	try {
		StoryRef story = gEngine->getStoryHandle(env, obj);
		TextRangesRef ranges;
		if (!sStory->GetTextSelection(story, &ranges))
			return TextRange_convertTextRanges(env, ranges);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * boolean equals(java.lang.Object story)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_TextStory_equals(JNIEnv *env, jobject obj, jobject story) {
	try {
		if (env->IsInstanceOf(story, gEngine->cls_ai_TextStory)) {
			StoryRef story1 = gEngine->getStoryHandle(env, obj);
			StoryRef story2 = gEngine->getStoryHandle(env, story);
			if (story2 != NULL) {
				ATEBool8 ret;
				if (!sStory->IsEqual(story1, story2, &ret))
					return ret;
			}
		}
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * int nativeGetTexListLength(int handle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextStory_nativeGetTexListLength(JNIEnv *env, jobject obj, jint handle) {
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
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * com.scriptographer.ai.Text nativeGetTextItem(int storyHandle, int docHandle, int index)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_TextStory_nativeGetTextItem(JNIEnv *env, jobject obj, jint storyHandle, jint docHandle, jint index) {
	try {
		TextFrameRef textRef;
		AIArtHandle textHandle;
		if (!sStory->GetFrame((StoryRef) storyHandle, index, &textRef) &&
			!sAITextFrame->GetAITextFrame(textRef, &textHandle))
			return gEngine->wrapArtHandle(env, textHandle, (AIDocumentHandle) docHandle);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * int getLength()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextStory_getLength(JNIEnv *env, jobject obj) {
	try {
		StoryRef story = gEngine->getStoryHandle(env, obj);
		ASInt32 length;
		if (!sStory->GetSize(story, &length))
			return length;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void reflow()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextStory_reflow(JNIEnv *env, jobject obj) {
	try {
	// TODO: Do we need to activate doc?
		StoryRef story = gEngine->getStoryHandle(env, obj);
		sStory->SuspendReflow(story);
		sStory->ResumeReflow(story);
	} EXCEPTION_CONVERT(env);
}
