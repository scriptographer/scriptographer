/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile$
 * $Author$
 * $Revision$
 * $Date$
 */

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
		AIFontKey font = gEngine->getFontHandle(env, obj);
		ASUnicode name[256];
		if (!sAIFont->GetFontStyleUINameUnicode(font, name, 256))
			return gEngine->convertString(env, name);		
	} EXCEPTION_CONVERT(env);
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
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int getIndex()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_FontWeight_getIndex(JNIEnv *env, jobject obj) {
	try {
		AIFontKey font = gEngine->getFontHandle(env, obj);
		AITypefaceKey family;
		short style;
		if (!sAIFont->TypefaceAndStyleFromFontKey(font, &family, &style))
			return (jint) style;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * boolean isValid()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_FontWeight_isValid(JNIEnv *env, jobject obj) {
	try {
		AIFontKey font = gEngine->getFontHandle(env, obj);
		FontRef ref;
		if (!sAIFont->FontFromFontKey(font, &ref) && ref != NULL)
			return true;
	} EXCEPTION_CONVERT(env);
	return false;
}
