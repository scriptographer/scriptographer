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
#include "aiGlobals.h"
#include "com_scriptographer_ai_DocumentViewList.h"

/*
 * com.scriptographer.ai.DocumentViewList
 */

/*
 * int nativeSize(int docHandle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_DocumentViewList_nativeSize(JNIEnv *env, jclass cls, jint docHandle) {
	long count = 0;
	try {
		Document_activate((AIDocumentHandle) docHandle);
		sAIDocumentView->CountDocumentViews(&count);
	} EXCEPTION_CONVERT(env);
	return (jint) count;
}

/*
 * int nativeGetView(int docHandle, int index)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_DocumentViewList_nativeGet(JNIEnv *env, jclass cls, jint docHandle, jint index) {
	AIDocumentViewHandle view = NULL;
	try {
		Document_activate((AIDocumentHandle) docHandle);
		// according to the documentation, the views start at index 1:
		sAIDocumentView->GetNthDocumentView(index + 1, &view);
	} EXCEPTION_CONVERT(env);
	return (jint) view;
}
