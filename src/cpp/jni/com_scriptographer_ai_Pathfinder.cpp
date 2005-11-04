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
 * $RCSfile: com_scriptographer_ai_Pathfinder.cpp,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/11/04 01:34:14 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_Pathfinder.h"

/*
 * com.scriptographer.ai.Pathfinder
 */

ASBoolean Pathfinder_begin(JNIEnv *env, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted, AIPathfinderData *data, AIArtSet *prevSelected) {
	memset(data, 0, sizeof(AIPathfinderData));
	data->options.ipmPrecision = precision;
	data->options.removeRedundantPoints = removePoints;
	
#if (kPluginInterfaceVersion == kPluginInterfaceVersion9001)
	data->options.extractUnpaintedArtwork = extractUnpainted;
#else
	data->options.flags = kExtractUnpaintedArtwork;
#endif

	int length = env->GetArrayLength(artObjects);
	AIArtHandle *handles = new AIArtHandle[length];
	long count = 0, i;
	for (i = 0; i < length; i++) {
		jobject obj = env->GetObjectArrayElement(artObjects, i);
		if (env->IsInstanceOf(obj, gEngine->cls_Art)) {
			handles[count++] = gEngine->getArtHandle(env, obj);
		}
	}
	data->fSelectedArtCount = count;
	data->fSelectedArt = handles;

	// get the previously selected objects:
	// because the result is selected after execution of pathfinder...
	AIArtSet selected = NULL;
	if (!sAIArtSet->NewArtSet(&selected))
		sAIArtSet->SelectedArtSet(selected);
	*prevSelected = selected;
	// and deselect it:
	sAIArtSet->CountArtSet(selected, &count);
	AIArtHandle art;
	for (i = 0; i < count; i++) {
		if (!sAIArtSet->IndexArtSet(selected, i, &art)) sAIArt->SetArtUserAttr(art, kArtSelected, 0);
	}
	return true;
}

void Pathfinder_end(JNIEnv *env, AIPathfinderData *data, AIArtSet *prevSelected) {
	// get the now selected objects (= result) and change the artset:
	AIArtSet selected = NULL;
	long count;
	AIArtHandle art;
	if (!sAIArtSet->NewArtSet(&selected)) {
		// get the result set:
		sAIArtSet->SelectedArtSet(selected);
		// and deselect it:
		sAIArtSet->CountArtSet(selected, &count);
		for (long i = 0; i < count; i++) {
			if (!sAIArtSet->IndexArtSet(selected, i, &art))
				sAIArt->SetArtUserAttr(art, kArtSelected, 0);
		}
	}
	// select the previously selected objects:
	sAIArtSet->CountArtSet(*prevSelected, &count);
	for (long i = 0; i < count; i++) {
		if (!sAIArtSet->IndexArtSet(*prevSelected, i, &art) &&
#if kPluginInterfaceVersion < kAI12
			sAIArt->ValidArt(art)
#else
			sAIArt->ValidArt(art, false)
#endif
			)
			sAIArt->SetArtUserAttr(art, kArtSelected, kArtSelected);
	}

	// clean up
	sAIArtSet->DisposeArtSet(prevSelected);
	delete data->fSelectedArt;
}

/*
 * void unite(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Pathfinder_unite(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoUniteEffect(&data, NULL);
			Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env)
}

/*
 * void intersect(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Pathfinder_intersect(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoIntersectEffect(&data, NULL);
			Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env)
}

/*
 * void exclude(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Pathfinder_exclude(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoExcludeEffect(&data, NULL);
			Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env)
}

/*
 * void backMinusFront(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Pathfinder_backMinusFront(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoBackMinusFrontEffect(&data, NULL);
			Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env)
}

/*
 * void frontMinusBack(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Pathfinder_frontMinusBack(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoFrontMinusBackEffect(&data, NULL);
			Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env)
}

/*
 * void divide(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Pathfinder_divide(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoDivideEffect(&data, NULL);
			Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env)
}

/*
 * void outline(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Pathfinder_outline(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoOutlineEffect(&data, NULL);
			Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env)
}

/*
 * void trim(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Pathfinder_trim(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoTrimEffect(&data, NULL);
			Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env)
}

/*
 * void merge(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Pathfinder_merge(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoMergeEffect(&data, NULL);
			Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env)
}

/*
 * void crop(java.lang.Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Pathfinder_crop(JNIEnv *env, jclass cls, jobjectArray artObjects, jfloat precision, jboolean removePoints, jboolean extractUnpainted) {
	try {
		AIPathfinderData data;
		AIArtSet prevSelected;
		if (Pathfinder_begin(env, artObjects, precision, removePoints, extractUnpainted, &data, &prevSelected)) {
			sAIPathfinder->DoCropEffect(&data, NULL);
			Pathfinder_end(env, &data, &prevSelected);
		}
	} EXCEPTION_CONVERT(env)
}
