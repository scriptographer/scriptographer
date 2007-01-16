/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.com/ for updates and contact.
 *
 * -- GPL LICENSE NOTICE --
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -- GPL LICENSE NOTICE --
 *
 * $Id$
 */

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
	} EXCEPTION_CONVERT(env);
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
