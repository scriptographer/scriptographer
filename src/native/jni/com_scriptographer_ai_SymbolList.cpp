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
#include "com_scriptographer_ai_SymbolList.h"

/*
 * com.scriptographer.ai.SymbolList
 */

/*
 * int nativeSize(int docHandle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_SymbolList_nativeSize(JNIEnv *env, jclass cls, jint docHandle) {
	long count = 0;
	try {
		sAISymbol->CountSymbolPatternsFromDocument(&count, (AIDocumentHandle) docHandle);
	} EXCEPTION_CONVERT(env);
	return count;
}

/*
 * int nativeGet(int docHandle, int index)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_SymbolList_nativeGet__II(JNIEnv *env, jclass cls, jint docHandle, jint index) {
	AIPatternHandle symbol = NULL;
	try {
		sAISymbol->GetNthSymbolPatternFromDocument(index, &symbol, (AIDocumentHandle) docHandle);
	} EXCEPTION_CONVERT(env);
	return (jint) symbol;
}

/*
 * int nativeGet(int docHandle, java.lang.String name)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_SymbolList_nativeGet__ILjava_lang_String_2(JNIEnv *env, jclass cls, jint docHandle, jstring name) {
	AIPatternHandle symbol = NULL;
	try {
#if kPluginInterfaceVersion < kAI12
		char *str = gEngine->convertString(env, name);
		sAISymbol->GetSymbolByNameFromDocument(str, &symbol, (AIDocumentHandle) docHandle);
		delete str;
#else
		ai::UnicodeString str = gEngine->convertString_UnicodeString(env, name);
		sAISymbol->GetSymbolByNameFromDocument(str, &symbol, (AIDocumentHandle) docHandle);
#endif
	} EXCEPTION_CONVERT(env);
	return (jint) symbol;
}
