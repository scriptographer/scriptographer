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
