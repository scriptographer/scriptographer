/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
#include "com_scriptographer_ai_SwatchList.h"

/*
 * com.scriptographer.ai.SwatchList
 */

/*
 * int nativeSize(int docHandle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_SwatchList_nativeSize(JNIEnv *env, jclass cls, jint docHandle) {
	try {
		AISwatchListRef list = NULL;
		if (!sAISwatchList->GetSwatchList((AIDocumentHandle) docHandle, &list))
			return sAISwatchList->CountSwatches(list);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeGet(int docHandle, int index)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_SwatchList_nativeGet__II(JNIEnv *env, jclass cls, jint docHandle, jint index) {
	try {
		AISwatchListRef list = NULL;
		if (!sAISwatchList->GetSwatchList((AIDocumentHandle) docHandle, &list))
			return (jint) sAISwatchList->GetNthSwatch(list, index);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeGet(int docHandle, java.lang.String name)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_SwatchList_nativeGet__ILjava_lang_String_2(JNIEnv *env, jclass cls, jint docHandle, jstring name) {
	AISwatchRef ret = NULL;
	try {
		AISwatchListRef list = NULL;
		if (!sAISwatchList->GetSwatchList((AIDocumentHandle) docHandle, &list)) {
#if kPluginInterfaceVersion < kAI12
			char *str = gEngine->convertString(env, name);
			ret = sAISwatchList->GetSwatchByName(list, str);
			delete str;
#else
			ai::UnicodeString str = gEngine->convertString_UnicodeString(env, name);
			ret = sAISwatchList->GetSwatchByName(list, str);
#endif
		}
	} EXCEPTION_CONVERT(env);
	return (jint) ret;
}
