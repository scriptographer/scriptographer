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
 */

#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "ScriptographerPlugin.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Pattern.h"

/*
 * com.scriptographer.ai.Pattern
 */

/*
 * int nativeCreate()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Pattern_nativeCreate(JNIEnv *env, jclass cls) {
	try {
		// Make sure we're switching to the right doc (gCreationDoc)
		Document_activate();
		AIPatternHandle pattern = NULL;
		sAIPattern->NewPattern(&pattern);
		return (jint) pattern;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * com.scriptographer.ai.Item getDefinition()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Pattern_getDefinition(JNIEnv *env, jobject obj) {
	try {
		AIPatternHandle pattern = gEngine->getPatternHandle(env, obj);
		AIArtHandle art = NULL;
		sAIPattern->GetPatternArt(pattern, &art);
		return gEngine->wrapArtHandle(env, art, gEngine->getDocumentHandle(env, obj));
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setDefinition(com.scriptographer.ai.Item item)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Pattern_setDefinition(JNIEnv *env, jobject obj, jobject item) {
	try {
		AIPatternHandle pattern = gEngine->getPatternHandle(env, obj, true);
		AIArtHandle art = gEngine->getArtHandle(env, item, true);
		// TODO: see what happens if pattern and art are not from the same document!
		// consider adding a special case where this could work if it does not already (Using Item_copyTo?)
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
