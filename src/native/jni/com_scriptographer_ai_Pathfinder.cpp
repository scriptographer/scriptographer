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
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Pathfinder.h"

/*
 * com.scriptographer.ai.Pathfinder
 */

ASBoolean Pathfinder_begin(JNIEnv *env, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted, AIPathfinderData *data, AIArtSet *prevSelected) {
	memset(data, 0, sizeof(AIPathfinderData));
	data->options.ipmPrecision = precision;
	data->options.removeRedundantPoints = removePoints;
	data->options.flags = kExtractUnpaintedArtwork;

	int length = env->GetArrayLength(artObjects);
	AIArtHandle *handles = new AIArtHandle[length];
	long count = 0, i;
	bool first = true;
	for (i = 0; i < length; i++) {
		jobject obj = env->GetObjectArrayElement(artObjects, i);
		if (env->IsInstanceOf(obj, gEngine->cls_ai_Item)) {
			// Only activate document of the first object,
			// then use IsValid to see if the others are valid (= in the same doc)
			AIArtHandle art = gEngine->getArtHandle(env, obj, first);
#if kPluginInterfaceVersion < kAI12
			if (sAIArt->ValidArt(art))
#else // kPluginInterfaceVersion >= kAI12
			if (sAIArt->ValidArt(art, false))
#endif // kPluginInterfaceVersion >= kAI12
				handles[count++] = art;
			first = false;
		}
	}
	data->fSelectedArtCount = count;
	data->fSelectedArt = handles;

	// Get the previously selected objects:
	// Because the result is selected after execution of pathfinder...
	*prevSelected = Item_getSelected(false);
	Document_deselectAll();
	return true;
}

jobject Pathfinder_end(JNIEnv *env, AIPathfinderData *data, AIArtSet *prevSelected) {
	// Get the now selected objects (= result) and change the artset:
	long count;
	AIArtHandle art;
	// Get the selected results in a set:
	jobject result = NULL;
	AIArtSet selected = Item_getSelected();
	if (selected != NULL) {
		long count = 0;
		sAIArtSet->CountArtSet(selected, &count);
		// We're expecting exactly one result from Pathfinder. Either a simple object, a Group or a CompoundPath
		if (count != 1)
			throw new StringException("Received more than one object back from Pathfinder.");
		AIArtHandle art = NULL;
		sAIArtSet->IndexArtSet(selected, 0, &art);
		if (art != NULL)
			result = gEngine->wrapArtHandle(env, art, NULL, true);
		sAIArtSet->DisposeArtSet(&selected);
	}
	Item_restoreSelected(*prevSelected);
	delete data->fSelectedArt;
	
	return result;
}

/*
 * com.scriptographer.ai.ItemList unite(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Pathfinder_unite(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoUniteEffect(&data, NULL);
			return Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.ItemList intersect(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Pathfinder_intersect(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoIntersectEffect(&data, NULL);
			return Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.ItemList exclude(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Pathfinder_exclude(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoExcludeEffect(&data, NULL);
			return Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.ItemList backMinusFront(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Pathfinder_backMinusFront(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoBackMinusFrontEffect(&data, NULL);
			return Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.ItemList frontMinusBack(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Pathfinder_frontMinusBack(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoFrontMinusBackEffect(&data, NULL);
			return Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.ItemList divide(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Pathfinder_divide(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoDivideEffect(&data, NULL);
			return Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.ItemList outline(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Pathfinder_outline(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoOutlineEffect(&data, NULL);
			return Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.ItemList trim(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Pathfinder_trim(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoTrimEffect(&data, NULL);
			return Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.ItemList merge(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Pathfinder_merge(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoMergeEffect(&data, NULL);
			return Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.ItemList crop(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Pathfinder_crop(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoCropEffect(&data, NULL);
			return Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}
