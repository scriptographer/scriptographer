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
 * $RCSfile: com_scriptographer_ai_ArtSet.cpp,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:58 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Art.h"
#include "com_scriptographer_ai_ArtSet.h"

/*
 * com.scriptographer.ai.ArtSet
 */

void artSetFilter(AIArtSet set, bool layerOnly = false) {
	// takes out all kUnknownArt, kTextRunArt, ... objs
	// removes layergroups as well
	long count;
	sAIArtSet->CountArtSet(set, &count);
	for (long i = count - 1; i >= 0; i--) {
		AIArtHandle art = NULL;
		if (!sAIArtSet->IndexArtSet(set, i, &art)) {
			short type = artGetType(art);
			bool isLayer = artIsLayer(art);
			if (type == kUnknownArt ||
#ifdef OLD_TEXT_SUITES
				type == kTextRunArt ||
#endif
				(layerOnly && !isLayer || !layerOnly && isLayer)) {
					sAIArtSet->RemoveArtFromArtSet(set, art);
			}
		}
	}
}

jobjectArray artSetToArray(JNIEnv *env, AIArtSet set, bool layerOnly = false) {
	artSetFilter(set, layerOnly);
	long count;
	sAIArtSet->CountArtSet(set, &count);
	jobjectArray array = env->NewObjectArray(count, gEngine->cls_Art, NULL); 
	for (long i = 0; i < count; i++) {
		jobject obj;
		AIArtHandle art;
		if (!sAIArtSet->IndexArtSet(set, i, &art)) {
			obj = gEngine->wrapArtHandle(env, art);
		} else {
			obj = NULL;	
		}
		env->SetObjectArrayElement(array, i, obj);
	}
	return array;
}

AIArtSet artSetFromArray(JNIEnv *env, jobjectArray array) {
	int length = env->GetArrayLength(array);
	AIArtHandle *handles = new AIArtHandle[length];
	int count = 0;
	for (int i = 0; i < length; i++) {
		jobject obj = env->GetObjectArrayElement(array, i);
		if (env->IsInstanceOf(obj, gEngine->cls_Art)) {
			handles[count++] = gEngine->getArtHandle(env, obj);
		}
	}
	AIArtSet set = NULL;
	if (!sAIArtSet->NewArtSet(&set))
		sAIArtSet->ArrayArtSet(set, handles, count);
	delete handles;
	return set;
}

/*
 * java.lang.Object[] nativeGetSelected()
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_ai_ArtSet_nativeGetSelected(JNIEnv *env, jclass cls) {
	try {
		// TODO: consider using Matching Art Suite instead!!! (faster, direct array access)
		AIArtSet set;
		if (!sAIArtSet->NewArtSet(&set)) {
			if (!sAIArtSet->SelectedArtSet(set)) {
				jobjectArray array = artSetToArray(env, set);
				sAIArtSet->DisposeArtSet(&set);
				return array;
			}
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * java.lang.Object[] nativeGetMatching(Class typeClass, java.util.Map attributes)
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_ai_ArtSet_nativeGetMatching(JNIEnv *env, jclass cls, jclass typeClass, jobject attributes) {
	try {
		AIArtSet set;
		if (!sAIArtSet->NewArtSet(&set)) {
			bool layerOnly = false;
			short type = artGetType(env, typeClass);
			if (type == com_scriptographer_ai_Art_TYPE_LAYER) {
				type = kGroupArt;
				layerOnly = true;
			}
			AIArtSpec spec;
			spec.type = type;
			spec.whichAttr = 0;
			spec.attr = 0;
			// use the env's version of the callers for speed reasons. check for exceptions only once at the end:		
			jobject entrySet = env->CallObjectMethod(attributes, gEngine->mid_Map_entrySet);
			jobject iterator = env->CallObjectMethod(entrySet, gEngine->mid_Set_iterator);
			while (env->CallBooleanMethod(iterator, gEngine->mid_Iterator_hasNext)) {
				jobject entry = env->CallObjectMethod(iterator, gEngine->mid_Iterator_next);
				jobject key = env->CallObjectMethod(entry, gEngine->mid_Map_Entry_getKey);
				jobject value = env->CallObjectMethod(entry, gEngine->mid_Map_Entry_getValue);
				if (env->IsInstanceOf(key, gEngine->cls_Number) && env->IsInstanceOf(value, gEngine->cls_Boolean)) {
					jint flag = env->CallIntMethod(key, gEngine->mid_Number_intValue);
					jboolean set = env->CallBooleanMethod(value, gEngine->mid_Boolean_booleanValue);
					spec.whichAttr |= flag;
					if (set) spec.attr |=  flag;
				}
			}
			EXCEPTION_CHECK(env)
			if (!sAIArtSet->MatchingArtSet(&spec, 1, set)) {
				jobjectArray array = artSetToArray(env, set, layerOnly);
				sAIArtSet->DisposeArtSet(&set);
				return array;
			}
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * java.lang.Object[] nativeGetLayer(int artHandle)
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_ai_ArtSet_nativeGetLayer(JNIEnv *env, jclass cls, jint artHandle) {
	try {
		AIArtSet set;
		if (!sAIArtSet->NewArtSet(&set)) {
			if (artIsLayer((AIArtHandle) artHandle)) {
				AILayerHandle layer;
				if (!sAIArt->GetLayerOfArt((AIArtHandle) artHandle, &layer) &&
					!sAIArtSet->LayerArtSet(layer, set)) {
				jobjectArray array = artSetToArray(env, set);
				sAIArtSet->DisposeArtSet(&set);
				return array;
				}
			}
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * java.lang.Object[] nativeInvert(java.lang.Object[] artObjects)
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_ai_ArtSet_nativeInvert(JNIEnv *env, jclass cls, jobjectArray artObjects) {
	try {
		AIArtSet setFrom = artSetFromArray(env, artObjects), setTo;
		if (setFrom != NULL && !sAIArtSet->NewArtSet(&setTo) && !sAIArtSet->NotArtSet(setFrom, setTo)) {
				jobjectArray array = artSetToArray(env, setTo);
				sAIArtSet->DisposeArtSet(&setFrom);
				sAIArtSet->DisposeArtSet(&setTo);
				return array;
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}
