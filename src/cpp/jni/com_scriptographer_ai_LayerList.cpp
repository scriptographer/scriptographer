/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile: com_scriptographer_ai_LayerList.cpp,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/03/07 13:42:29 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "Plugin.h"
#include "com_scriptographer_ai_LayerList.h"

/*
 * com.scriptographer.ai.LayerList
 */

// LAYERLIST_BEGIN and LAYERLIST_END are necessary because only the layerse of the 
// active document can be accessed throught sAILayer. it seems like adobe forgot
// tu use the AIDocumentHandle parameter there...

#define LAYERLIST_BEGIN \
	AIDocumentHandle activeDoc = NULL; \
	AIDocumentHandle prevDoc = NULL; \
	try { \
		sAIDocument->GetDocument(&activeDoc); \
		if (activeDoc != (AIDocumentHandle) docHandle) { \
			prevDoc = activeDoc; \
			sAIDocumentList->Activate((AIDocumentHandle) docHandle, false); \
		} \

#define LAYERLIST_END \
	} EXCEPTION_CONVERT(env) \
	if (prevDoc != NULL) \
		sAIDocumentList->Activate(prevDoc, false);

/*
 * int nativeGetLength(int docHandle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_LayerList_nativeGetLength(JNIEnv *env, jclass cls, jint docHandle) {
	long count = 0;
	
	LAYERLIST_BEGIN
		
	sAILayer->CountLayers(&count);

	LAYERLIST_END
	
	return count;
}

/*
 * com.scriptographer.ai.Art nativeGet(int docHandle, int index)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_LayerList_nativeGet__II(JNIEnv *env, jclass cls, jint docHandle, jint index) {
	jobject layerObj = NULL;

	LAYERLIST_BEGIN

	AILayerHandle layer = NULL;
	sAILayer->GetNthLayer(index, &layer);
	if (layer != NULL)
		layerObj = gEngine->wrapLayerHandle(env, layer);

	LAYERLIST_END
	
	return layerObj;
}

/*
 * com.scriptographer.ai.Art nativeGet(int docHandle, java.lang.String name)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_LayerList_nativeGet__ILjava_lang_String_2(JNIEnv *env, jclass cls, jint docHandle, jstring name) {
	jobject layerObj = NULL;

	LAYERLIST_BEGIN

	AILayerHandle layer = NULL;
	char *str = gEngine->createCString(env, name);
	sAILayer->GetLayerByTitle(&layer, gPlugin->toPascal(str, (unsigned char *) str));
	delete str;
	if (layer != NULL)
		layerObj = gEngine->wrapLayerHandle(env, layer);
	
	LAYERLIST_END
	
	return layerObj;
}
