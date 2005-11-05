#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_FontFamily.h"

/*
 * com.scriptographer.ai.FontFamily
 */

/*
 * java.lang.String nativeGetName(int handle)
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_FontFamily_nativeGetName(JNIEnv *env, jobject obj, jint handle) {
	try {
		AIFontKey font;
		ASUnicode name[256];
		if (!sAIFont->IndexTypefaceStyleList((AITypefaceKey) handle, 0, &font) &&
			!sAIFont->GetFontFamilyUINameUnicode(font, name, 256)) {
			return gEngine->convertString(env, name);
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * int nativeGetLength(int handle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_FontFamily_nativeGetLength(JNIEnv *env, jobject obj, jint handle) {
	try {
		long length;
		if (!sAIFont->CountTypefaceStyles((AITypefaceKey) handle, &length))
			return length;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * int nativeGet(int handle, int index)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_FontFamily_nativeGet(JNIEnv *env, jclass cls, jint handle, jint index) {
	try {
		AIFontKey font;
		if (!sAIFont->IndexTypefaceStyleList((AITypefaceKey) handle, index, &font))
			return (jint) font;
	} EXCEPTION_CONVERT(env)
	return 0;
}
