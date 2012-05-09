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
#include "com_scriptographer_ai_SwatchList.h"

/*
 * com.scriptographer.ai.SwatchList
 */

// Skip the first two standard swatches from the list exposed to scripting
#define SWATCHLIST_OFFSET 2

/*
 * int nativeSize(int docHandle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_SwatchList_nativeSize(
		JNIEnv *env, jclass cls, jint docHandle) {
	try {
		AISwatchListRef list = NULL;
		if (!sAISwatchList->GetSwatchList((AIDocumentHandle) docHandle, &list))
			return sAISwatchList->CountSwatches(list) - SWATCHLIST_OFFSET;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeGet(int docHandle, int index)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_SwatchList_nativeGet__II(
		JNIEnv *env, jclass cls, jint docHandle, jint index) {
	try {
		AISwatchListRef list = NULL;
		if (!sAISwatchList->GetSwatchList((AIDocumentHandle) docHandle, &list))
			return (jint) sAISwatchList->GetNthSwatch(list,
					index + SWATCHLIST_OFFSET);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeGet(int docHandle, java.lang.String name)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_SwatchList_nativeGet__ILjava_lang_String_2(
		JNIEnv *env, jclass cls, jint docHandle, jstring name) {
	AISwatchRef ret = NULL;
	try {
		AISwatchListRef list = NULL;
		if (!sAISwatchList->GetSwatchList((AIDocumentHandle) docHandle, &list)) {
#if kPluginInterfaceVersion < kAI12
			char *str = gEngine->convertString(env, name);
			ret = sAISwatchList->GetSwatchByName(list, str);
			delete str;
#else
			ai::UnicodeString str = gEngine->convertString_UnicodeString(env, name);
			ret = sAISwatchList->GetSwatchByName(list, str);
#endif
		}
	} EXCEPTION_CONVERT(env);
	return (jint) ret;
}
