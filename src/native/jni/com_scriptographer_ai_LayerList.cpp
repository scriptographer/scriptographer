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

#include "stdHeaders.h"
#include "ScriptographerPlugin.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_LayerList.h"

/*
 * com.scriptographer.ai.LayerList
 */

// LAYERLIST_BEGIN and LAYERLIST_END are necessary because only the layerse of the 
// active document can be accessed throught sAILayer. it seems like adobe forgot
// tu use the AIDocumentHandle parameter there...

/*
 * int nativeSize(int docHandle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_LayerList_nativeSize(JNIEnv *env, jclass cls, jint docHandle) {
	long count = 0;
	try {
		Document_activate((AIDocumentHandle) docHandle);
		sAILayer->CountLayers(&count);
	} EXCEPTION_CONVERT(env);
	return count;
}

/*
 * java.lang.Object nativeGet(int docHandle, int index)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_LayerList_nativeGet__II(JNIEnv *env, jclass cls, jint docHandle, jint index) {
	jobject layerObj = NULL;
	try {
		Document_activate((AIDocumentHandle) docHandle);
		AILayerHandle layer = NULL;
		sAILayer->GetNthLayer(index, &layer);
		if (layer != NULL)
			layerObj = gEngine->wrapLayerHandle(env, layer, (AIDocumentHandle) docHandle);
	} EXCEPTION_CONVERT(env);
	return layerObj;
}

/*
 * java.lang.Object nativeGet(int docHandle, java.lang.String name)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_LayerList_nativeGet__ILjava_lang_String_2(JNIEnv *env, jclass cls, jint docHandle, jstring name) {
	jobject layerObj = NULL;
	try {
		Document_activate((AIDocumentHandle) docHandle);
		AILayerHandle layer = NULL;
#if kPluginInterfaceVersion < kAI12
		char *str = gEngine->convertString(env, name);
		sAILayer->GetLayerByTitle(&layer, gPlugin->toPascal(str, (unsigned char *) str));
		delete str;
#else
		ai::UnicodeString str = gEngine->convertString_UnicodeString(env, name);
		sAILayer->GetLayerByTitle(&layer, str);
#endif
		if (layer != NULL)
			layerObj = gEngine->wrapLayerHandle(env, layer, (AIDocumentHandle) docHandle);
	} EXCEPTION_CONVERT(env);
	return layerObj;
}
