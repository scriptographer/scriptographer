#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_FontWeight.h"

/*
 * com.scriptographer.ai.FontWeight
 */

/*
 * java.lang.String getName()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_FontWeight_getName(JNIEnv *env, jobject obj) {
	try {
		AIFontKey font = gEngine->getFontKey(env, obj);
		ASUnicode name[256];
		if (!sAIFont->GetFontStyleUINameUnicode(font, name, 256))
			return gEngine->convertString(env, name);		
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * int nativeGetFamily(int handle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_FontWeight_nativeGetFamily(JNIEnv *env, jobject obj, jint handle) {
	try {
		AITypefaceKey family;
		short style;
		if (!sAIFont->TypefaceAndStyleFromFontKey((AIFontKey) handle, &family, &style))
			return (jint) family;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * int getIndex()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_FontWeight_getIndex(JNIEnv *env, jobject obj) {
	try {
		AIFontKey font = gEngine->getFontKey(env, obj);
		AITypefaceKey family;
		short style;
		if (!sAIFont->TypefaceAndStyleFromFontKey(font, &family, &style))
			return (jint) style;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * boolean isValid()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_FontWeight_isValid(JNIEnv *env, jobject obj) {
	try {
		AIFontKey font = gEngine->getFontKey(env, obj);
		FontRef ref;
		if (!sAIFont->FontFromFontKey(font, &ref) && ref != NULL)
			return true;
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}
