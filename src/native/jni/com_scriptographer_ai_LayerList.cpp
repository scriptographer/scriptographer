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
	ai::int32 count = 0;
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
