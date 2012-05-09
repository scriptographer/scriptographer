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
#include "com_scriptographer_ai_Symbol.h"

/*
 * com.scriptographer.ai.Symbol
 */

/*
 * int nativeCreate(int artHandle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Symbol_nativeCreate(JNIEnv *env, jclass cls, jint artHandle) {
	try {
		// Make sure we're switching to the right doc (gCreationDoc)
		Document_activate();
		AIPatternHandle symbol = NULL;
		// Commit pending changes first
		Item_commit(env, (AIArtHandle) artHandle);
#if kPluginInterfaceVersion >= kAI15
		// TODO: Test these parameters registrationPoint, transformDefinitionArt, decide wether to pass
		// them and compare with behavior in CS4...
		sAISymbol->NewSymbolPattern(&symbol, (AIArtHandle) artHandle, kSymbolCenterPoint, true, false);
#else // kPluginInterfaceVersion < kAI15
		sAISymbol->NewSymbolPattern(&symbol, (AIArtHandle) artHandle, false);
#endif // kPluginInterfaceVersion < kAI15
		return (jint) symbol;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * java.lang.String getName()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_Symbol_getName(JNIEnv *env, jobject obj) {
	try {
		AIPatternHandle symbol = gEngine->getPatternHandle(env, obj);
#if kPluginInterfaceVersion >= kAI12
		ai::UnicodeString name;
		if (!sAISymbol->GetSymbolPatternName(symbol, name)) {
#else // kPluginInterfaceVersion < kAI12
		char name[kMaxSymbolNameLength];
		if (!sAISymbol->GetSymbolPatternName(symbol, name, kMaxSymbolNameLength)) {
#endif //  kPluginInterfaceVersion < kAI12
			return gEngine->convertString(env, name);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setName(java.lang.String name)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Symbol_setName(JNIEnv *env, jobject obj, jstring name) {
	try {
		AIPatternHandle symbol = gEngine->getPatternHandle(env, obj, true);
		if (name != NULL) {
#if kPluginInterfaceVersion >= kAI12
			ai::UnicodeString str = gEngine->convertString_UnicodeString(env, name);
			sAISymbol->GetSymbolPatternDisplayName(str);
			sAISymbol->SetSymbolPatternBaseName(symbol, str);
#else // kPluginInterfaceVersion < kAI12
			char *str = gEngine->convertString(env, name, kMaxSymbolNameLength);
			sAISymbol->GetSymbolPatternDisplayName(str);
			sAISymbol->SetSymbolPatternBaseName(symbol, str);
			delete str;
#endif // kPluginInterfaceVersion < kAI12
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.Item getDefinition()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Symbol_getDefinition(JNIEnv *env, jobject obj) {
	try {
		AIPatternHandle symbol = gEngine->getPatternHandle(env, obj);
		AIArtHandle art = NULL;
		sAISymbol->GetSymbolPatternArt(symbol, &art);
		return gEngine->wrapArtHandle(env, art, gEngine->getDocumentHandle(env, obj));
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setDefinition(com.scriptographer.ai.Item item)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Symbol_setDefinition(JNIEnv *env, jobject obj, jobject item) {
	try {
		AIPatternHandle symbol = gEngine->getPatternHandle(env, obj, true);
		AIArtHandle art = gEngine->getArtHandle(env, item);
		// Commit pending changes first
		Item_commit(env, art);
		// TODO: see what happens if symbol and art are not from the same document!
		// consider adding a special case where this could work if it does not already (Using Item_copyTo?)
#if kPluginInterfaceVersion >= kAI15
		// TODO: See if transformDefinationArt needs to be set to true to behave the same as CS4
		sAISymbol->SetSymbolPatternArt(symbol, art, true);
#else // kPluginInterfaceVersion < kAI15
		sAISymbol->SetSymbolPatternArt(symbol, art);
#endif // kPluginInterfaceVersion < kAI15
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isValid()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Symbol_isValid(JNIEnv *env, jobject obj) {
	try {
		AIPatternHandle symbol = gEngine->getPatternHandle(env, obj);
		return sAISymbol->ValidateSymbolPattern(symbol);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean nativeRemove()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Symbol_nativeRemove(JNIEnv *env, jobject obj) {
	try {
		AIPatternHandle symbol = gEngine->getPatternHandle(env, obj, true);
		return !sAISymbol->DeleteSymbolPattern(symbol);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean isListed()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Symbol_isListed(JNIEnv *env, jobject obj) {
	try {
		AIPatternHandle symbol = gEngine->getPatternHandle(env, obj);
		return sAISymbol->IsSymbolPatternListed(symbol);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setListed(boolean listed)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Symbol_setListed(JNIEnv *env, jobject obj, jboolean listed) {
	try {
		AIPatternHandle symbol = gEngine->getPatternHandle(env, obj, true);
		ASBoolean isListed = sAISymbol->IsSymbolPatternListed(symbol);
		if (isListed && !listed) {
			sAISymbol->UnlistSymbolPattern(symbol);
		} else if (!isListed && listed) {
			sAISymbol->MakeSymbolPatternListed(symbol);
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isSelected()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Symbol_isSelected(JNIEnv *env, jobject obj) {
	try {
		AIPatternHandle symbol = gEngine->getPatternHandle(env, obj);
		return sAISymbolPalette->IsSymbolSelected(symbol);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void activate()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Symbol_activate(JNIEnv *env, jobject obj) {
	try {
		AIPatternHandle symbol = gEngine->getPatternHandle(env, obj, true);
		sAISymbolPalette->SetCurrentSymbol(symbol);
	} EXCEPTION_CONVERT(env);
}

/*
 * void setIndex(int index)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Symbol_setIndex(JNIEnv *env, jobject obj, jint index) {
	try {
		AIPatternHandle symbol = gEngine->getPatternHandle(env, obj, true);
		sAISymbol->MoveSymbolPatternInList(symbol, index);
	} EXCEPTION_CONVERT(env);
}
