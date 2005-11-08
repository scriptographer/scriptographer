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
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * com.scriptographer.ai.Story nativeGet(int handle, int index, com.scriptographer.ai.Story curStory)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_TextStoryList_nativeGet(JNIEnv *env, jobject obj, jint handle, jint index, jobject curStory) {
	try {
		StoryRef storyRef;
		if (!sStories->Item((StoriesRef) handle, index, &storyRef)) {
			StoryRef curStoryRef = gEngine->getStoryRef(env, curStory);
			bool equal;
			// check if it's the same story as before, in that case return the old wrapped story
			// this is needed as in ATE, reference handles allways change their values
			if (curStoryRef != NULL && !sStory->IsEqual(storyRef, curStoryRef, &equal) && equal) {
				return curStory;
			}
			// if we're still here, we need to wrap the story:
			return gEngine->wrapStoryRef(env, storyRef);
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}
