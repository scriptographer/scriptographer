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
		ASUnicode unicodeName[256];
		if (!sAIFont->IndexTypefaceStyleList((AITypefaceKey) handle, 0, &font) &&
				!sAIFont->GetFontFamilyUINameUnicode(font, unicodeName, 256)) {
			// In some cases GetFontFamilyUINameUnicode does seem to return an empty
			// string, e.g on CS2 Windows. If so, fall back to non-unicode version.
			if (unicodeName[0] == 0) {
				char name[256];
				if (!sAIFont->GetFontFamilyUIName(font, name, 256))
					return gEngine->convertString(env, name);
			}
			return gEngine->convertString(env, unicodeName);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * int nativeSize(int handle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_FontFamily_nativeSize(JNIEnv *env, jobject obj, jint handle) {
	try {
		long length;
		if (!sAIFont->CountTypefaceStyles((AITypefaceKey) handle, &length))
			return length;
	} EXCEPTION_CONVERT(env);
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
	} EXCEPTION_CONVERT(env);
	return 0;
}
