/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.com/ for updates and contact.
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
#include "com_scriptographer_ai_TextStoryList.h"

/*
 * com.scriptographer.ai.TextStoryList
 */

using namespace ATE;

/*
 * int nativeGetLength(int handle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextStoryList_nativeGetLength(JNIEnv *env, jobject obj, jint handle) {
	try {
		ASInt32 size;
		if (!sStories->GetSize((StoriesRef) handle, &size))
			return size;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * com.scriptographer.ai.Story nativeGet(int handle, int index, com.scriptographer.ai.Story curStory)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_TextStoryList_nativeGet(JNIEnv *env, jobject obj, jint handle, jint index, jobject curStory) {
	try {
		StoryRef storyRef;
		if (!sStories->Item((StoriesRef) handle, index, &storyRef)) {
			StoryRef curStoryRef = gEngine->getStoryHandle(env, curStory);
			bool equal;
			// check if it's the same story as before, in that case return the old wrapped story
			// this is needed as in ATE, reference handles allways change their values
			if (curStoryRef != NULL && !sStory->IsEqual(storyRef, curStoryRef, &equal) && equal) {
				return curStory;
			}
			// if we're still here, we need to wrap the story:
			return gEngine->wrapStoryHandle(env, storyRef);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}
