#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "ScriptographerPlugin.h"
#include "com_scriptographer_ai_Pattern.h"

/*
 * com.scriptographer.ai.Pattern
 */

/*
 * int nativeCreate()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Pattern_nativeCreate(JNIEnv *env, jclass cls) {
	try {
		AIPatternHandle pattern = NULL;
		sAIPattern->NewPattern(&pattern);
		return (jint) pattern;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * java.lang.String getName()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_Pattern_getName(JNIEnv *env, jobject obj) {
	try {
		AIPatternHandle pattern = gEngine->getPatternHandle(env, obj);
#if kPluginInterfaceVersion < kAI12
		unsigned char name[256];
		if (!sAIPattern->GetPatternName(pattern, name)) {
#else
			ai::UnicodeString name;
			if (!sAIPattern->GetPatternName(pattern, name)) {
#endif
				return gEngine->convertString(env, name);
			}
		} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setName(java.lang.String name)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Pattern_setName(JNIEnv *env, jobject obj, jstring name) {
	try {
		AIPatternHandle pattern = gEngine->getPatternHandle(env, obj, true);
#if kPluginInterfaceVersion < kAI12
		char *str = gEngine->convertString(env, name, 256);
		sAIPattern->NewPatternName(str, 256);
		sAIPattern->SetPatternName(pattern, gPlugin->toPascal(str, (unsigned char *) str));
		delete str;
#else
		ai::UnicodeString str = gEngine->convertString_UnicodeString(env, name);
		sAIPattern->NewPatternName(str);
		sAIPattern->SetPatternName(pattern, str);
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.Art getDefinition()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Pattern_getDefinition(JNIEnv *env, jobject obj) {
	try {
		AIPatternHandle pattern = gEngine->getPatternHandle(env, obj);
		AIArtHandle art = NULL;
		sAIPattern->GetPatternArt(pattern, &art);
		return gEngine->wrapArtHandle(env, art);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setDefinition(com.scriptographer.ai.Art item)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Pattern_setDefinition(JNIEnv *env, jobject obj, jobject item) {
	try {
		AIPatternHandle pattern = gEngine->getPatternHandle(env, obj, true);
		AIArtHandle art = gEngine->getArtHandle(env, item);
		// TODO: see what happens if pattern and art are not from the same document!
		// consider adding a special case where this could work if it does not already (Using Art_copyTo?)
		sAIPattern->SetPatternArt(pattern, art);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isValid()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Pattern_isValid(JNIEnv *env, jobject obj) {
	try {
		AIPatternHandle pattern = gEngine->getPatternHandle(env, obj);
		return sAIPattern->ValidatePattern(pattern);
	} EXCEPTION_CONVERT(env);
	return false;
}
