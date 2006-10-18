#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_Gradient.h"

/*
 * com.scriptographer.ai.Gradient
 */

/*
 * int nativeCreate()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Gradient_nativeCreate(JNIEnv *env, jclass cls) {
	try {
		AIGradientHandle gradient = NULL;
		sAIGradient->NewGradient(&gradient);
		return (jint) gradient;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * java.lang.String getName()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_Gradient_getName(JNIEnv *env, jobject obj) {
	try {
		AIGradientHandle gradient = gEngine->getGradientHandle(env, obj);
#if kPluginInterfaceVersion < kAI12
		char name[256];
		if (!sAIGradient->GetGradientName(gradient, name, 256)) {
#else
		ai::UnicodeString name;
		if (!sAIGradient->GetGradientName(gradient, name)) {
#endif
			return gEngine->convertString(env, name);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setName(java.lang.String name)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Gradient_setName(JNIEnv *env, jobject obj, jstring name) {
	try {
		AIGradientHandle gradient = gEngine->getGradientHandle(env, obj, true);
#if kPluginInterfaceVersion < kAI12
		char *str = gEngine->convertString(env, name);
		sAIGradient->SetGradientName(gradient, str);
		delete str;
#else
		ai::UnicodeString str = gEngine->convertString_UnicodeString(env, name);
		sAIGradient->SetGradientName(gradient, str);
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * short getType()
 */
JNIEXPORT jshort JNICALL Java_com_scriptographer_ai_Gradient_getType(JNIEnv *env, jobject obj) {
	try {
		AIGradientHandle gradient = gEngine->getGradientHandle(env, obj);
		short type = 0;
		sAIGradient->GetGradientType(gradient, &type);
		return type;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void setType(short type)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Gradient_setType(JNIEnv *env, jobject obj, jshort type) {
	try {
		AIGradientHandle gradient = gEngine->getGradientHandle(env, obj, true);
		sAIGradient->SetGradientType(gradient, type);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isValid()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Gradient_isValid(JNIEnv *env, jobject obj) {
	try {
		AIGradientHandle gradient = gEngine->getGradientHandle(env, obj, true);
		return sAIGradient->ValidateGradient(gradient);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean nativeRemove()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Gradient_nativeRemove(JNIEnv *env, jobject obj) {
	try {
		AIGradientHandle gradient = gEngine->getGradientHandle(env, obj, true);
		return !sAIGradient->DeleteGradient(gradient);
	} EXCEPTION_CONVERT(env);
	return false;
}
