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
#include "ScriptographerPlugin.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_DocumentList.h"

/*
 * com.scriptographer.ai.DocumentList
 */

/*
 * int size()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_DocumentList_size(JNIEnv *env, jobject obj) {
	try {
		ai::int32 count;
		sAIDocumentList->Count(&count);
		return count;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeGet(int index)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_DocumentList_nativeGet(JNIEnv *env, jclass cls, jint index) {
	try {
		AIDocumentHandle doc = NULL;
		sAIDocumentList->GetNthDocument(&doc, index);
		return (jint) doc;
	} EXCEPTION_CONVERT(env);
	return 0;
}
