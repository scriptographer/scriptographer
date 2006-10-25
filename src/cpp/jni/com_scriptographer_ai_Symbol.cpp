#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Symbol.h"

/*
 * com.scriptographer.ai.Symbol
 */

/*
 * int nativeCreate(int artHandle, boolean listed)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Symbol_nativeCreate(JNIEnv *env, jclass cls, jint artHandle, jboolean listed) {
	try {
		Document_activate();
		AIPatternHandle symbol = NULL;
		sAISymbol->NewSymbolPattern(&symbol, (AIArtHandle) artHandle, !listed);
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
#if kPluginInterfaceVersion < kAI12
		char name[kMaxSymbolNameLength];
		if (!sAISymbol->GetSymbolPatternName(symbol, name, kMaxSymbolNameLength)) {
#else
		ai::UnicodeString name;
		if (!sAISymbol->GetSymbolPatternName(symbol, name)) {
#endif
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
#if kPluginInterfaceVersion < kAI12
		char *str = gEngine->convertString(env, name, kMaxSymbolNameLength);
		sAISymbol->GetSymbolPatternDisplayName(str);
		sAISymbol->SetSymbolPatternName(symbol, str);
		delete str;
#else
		ai::UnicodeString str = gEngine->convertString_UnicodeString(env, name);
		sAISymbol->GetSymbolPatternDisplayName(str);
		sAISymbol->SetSymbolPatternName(symbol, str);
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.Art getDefinition()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Symbol_getDefinition(JNIEnv *env, jobject obj) {
	try {
		AIPatternHandle symbol = gEngine->getPatternHandle(env, obj);
		AIArtHandle art = NULL;
		sAISymbol->GetSymbolPatternArt(symbol, &art);
		return gEngine->wrapArtHandle(env, art);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setDefinition(com.scriptographer.ai.Art item)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Symbol_setDefinition(JNIEnv *env, jobject obj, jobject item) {
	try {
		AIPatternHandle symbol = gEngine->getPatternHandle(env, obj, true);
		AIArtHandle art = gEngine->getArtHandle(env, item);
		// TODO: see what happens if symbol and art are not from the same document!
		// consider adding a special case where this could work if it does not already (Using Art_copyTo?)
		sAISymbol->SetSymbolPatternArt(symbol, art);
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
