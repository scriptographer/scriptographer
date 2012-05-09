/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 */

#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_TextStoryList.h"

/*
 * com.scriptographer.ai.TextStoryList
 */

using namespace ATE;

/*
 * int nativeSize(int handle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextStoryList_nativeSize(JNIEnv *env, jobject obj, jint handle) {
	try {
		ASInt32 size;
		if (!sStories->GetSize((StoriesRef) handle, &size))
			return size;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeGet(int handle, int index, int curStoryHandle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextStoryList_nativeGet(JNIEnv *env, jobject obj, jint handle, jint index, jint curStoryHandle) {
	try {
		StoryRef storyRef, ret = 0;
		if (!sStories->Item((StoriesRef) handle, index, &storyRef)) {
			ATEBool8 equal;
			// Check if it's the same story as before, in that case return the old wrapped story
			// this is needed as in ATE, reference handles allways change their values
			if (curStoryHandle && !sStory->IsEqual(storyRef, (StoryRef) curStoryHandle, &equal) && equal) {
				ret = (StoryRef) curStoryHandle;
				sStory->Release(storyRef);
			} else {
				ret = storyRef;
			}
			return (jint) ret;
		}
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void nativeRelease(int handle)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextStoryList_nativeRelease(JNIEnv *env, jobject obj, jint handle) {
	try {
		if (handle)
			sStories->Release((StoriesRef) handle);
	} EXCEPTION_CONVERT(env);
}
