#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "AppContext.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Swatch.h"

/*
 * com.scriptographer.ai.Swatch
 */

/*
 * int nativeCreate()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Swatch_nativeCreate(JNIEnv *env, jclass cls) {
	try {
		Document_activate();
		AISwatchListRef list = NULL;
		if (!sAISwatchList->GetSwatchList(NULL, &list))
			return (jint) sAISwatchList->InsertNthSwatch(list, -1);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * java.lang.String getName()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_Swatch_getName(JNIEnv *env, jobject obj) {
	try {
		AISwatchRef swatch = gEngine->getSwatchHandle(env, obj);
#if kPluginInterfaceVersion < kAI12
		char name[256];
		if (!sAISwatchList->GetSwatchName(swatch, name, 256)) {
#else
		ai::UnicodeString name;
		if (!sAISwatchList->GetSwatchName(swatch, name)) {
#endif
			return gEngine->convertString(env, name);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setName(java.lang.String name)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Swatch_setName(JNIEnv *env, jobject obj, jstring name) {
	try {
		AISwatchRef swatch = gEngine->getSwatchHandle(env, obj, true);
#if kPluginInterfaceVersion < kAI12
		char *str = gEngine->convertString(env, name);
		sAISwatchList->SetSwatchName(swatch, str);
		delete str;
#else
		ai::UnicodeString str = gEngine->convertString_UnicodeString(env, name);
		sAISwatchList->SetSwatchName(swatch, str);
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.Color getColor()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Swatch_getColor(JNIEnv *env, jobject obj) {
	try {
		AISwatchRef swatch = gEngine->getSwatchHandle(env, obj);
		AIColor col;
		sAISwatchList->GetAIColor(swatch, &col);
		return gEngine->convertColor(env, &col);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setColor(com.scriptographer.ai.Color color)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Swatch_setColor(JNIEnv *env, jobject obj, jobject color) {
	try {
		AISwatchRef swatch = gEngine->getSwatchHandle(env, obj, true);
		AIColor col;
		gEngine->convertColor(env, color, &col);
		sAISwatchList->SetAIColor(swatch, &col);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean nativeRemove()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Swatch_nativeRemove(JNIEnv *env, jobject obj) {
	try {
		AISwatchRef swatch = gEngine->getSwatchHandle(env, obj, true);
		AISwatchListRef list = NULL;
		return !sAISwatchList->GetSwatchList(NULL, &list) &&
			!sAISwatchList->RemoveSwatch(list, swatch, true);
	} EXCEPTION_CONVERT(env);
	return false;
}
