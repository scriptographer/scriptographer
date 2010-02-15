/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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
		// Make sure we're switching to the right doc (gCreationDoc)
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
		// TODO: handle proberly. check is passing NULL for name returns the buffersize
		// to allocate the buffer first...?
		char name[256];
		short bufferSize = 256;
		if (!sAISwatchList->GetSwatchName(swatch, name, &bufferSize)) {
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
