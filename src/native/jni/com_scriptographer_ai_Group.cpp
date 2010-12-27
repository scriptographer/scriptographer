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
#include "com_scriptographer_ai_Group.h"

/*
 * com.scriptographer.ai.Group
 */

/*
 * boolean isClipped()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Group_isClipped(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		AIBoolean clipped;
		if (!sAIGroup->GetGroupClipped(handle, &clipped))
			return clipped;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void nativeSetClipped(boolean clipped)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Group_nativeSetClipped(JNIEnv *env, jobject obj, jboolean clipped) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj, true);
		sAIGroup->SetGroupClipped(handle, clipped);
	} EXCEPTION_CONVERT(env);
}
