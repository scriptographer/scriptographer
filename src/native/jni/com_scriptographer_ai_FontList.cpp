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

#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_FontList.h"

/*
 * com.scriptographer.ai.FontList
 */

/*
 * int size()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_FontList_size(JNIEnv *env, jobject obj) {
	try {
		long length;
		if (!sAIFont->CountTypefaces(&length))
			return length;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeGet(int index)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_FontList_nativeGet(JNIEnv *env, jclass cls, jint index) {
	try {
		AITypefaceKey key;
		if (!sAIFont->IndexTypefaceList(index, &key))
			return (jint) key;
	} EXCEPTION_CONVERT(env);
	return 0;
}
