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
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_ArtboardList.h"

/*
 * com.scriptographer.ai.ArtboardList
 */

/*
 * int nativeGetSize(int handle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_ArtboardList_nativeGetSize(
		JNIEnv *env, jclass cls, jint handle) {
	// On cropping areas aresomething else than artboards, so use CS4 and above
#if kPluginInterfaceVersion >= kAI14
	try {
		Document_activate((AIDocumentHandle) handle);
		ASInt32 count = 0;
		sAICropArea->GetCount(&count);
		return count;
	} EXCEPTION_CONVERT(env);
	return 0;
#else // kPluginInterfaceVersion < kAI14
	return 1;
#endif // kPluginInterfaceVersion < kAI14
}

/*
 * int nativeRemove(int handle, int fromIndex, int toIndex)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_ArtboardList_nativeRemove(
		JNIEnv *env, jclass cls, jint handle, jint fromIndex, jint toIndex) {
#if kPluginInterfaceVersion >= kAI14
	try {
		Document_activate((AIDocumentHandle) handle);
		AICropAreaPtr area = NULL;
		for (int i = toIndex - 1; i >= fromIndex; i--)
			sAICropArea->Delete(i, &area);
		ASInt32 count = 0;
		sAICropArea->GetCount(&count);
		return count;
	} EXCEPTION_CONVERT(env);
#else // kPluginInterfaceVersion < kAI14
	// We can't remove any artboards on CS3 and bellow
#endif // kPluginInterfaceVersion < kAI14
	return 0;
}

/*
 * boolean nativeGet(int handle, int index, com.scriptographer.ai.Artboard 
 *     artboard)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_ArtboardList_nativeGet(
		JNIEnv *env, jclass cls, jint handle, jint index, jobject artboard) {
	try {
		Document_activate((AIDocumentHandle) handle);
#if kPluginInterfaceVersion >= kAI14
		AICropAreaPtr area = NULL;
		if (sAICropArea->Get(index, &area))
			throw new StringException("Cannot get artboard");
		gEngine->callVoidMethod(env, artboard, gEngine->mid_ai_Artboard_set,
				gEngine->convertRectangle(env, kCurrentCoordinates,
				&area->m_CropAreaRect),
				area->m_bShowCenter, area->m_bShowCrossHairs,
				area->m_bShowSafeAreas, area->m_fPAR
		);
		return true;
#else // kPluginInterfaceVersion < kAI14
		AIRealPoint origin;
		sAIDocument->GetDocumentRulerOrigin(&origin);
		AIDocumentSetup setup;
		sAIDocument->GetDocumentSetup(&setup);
		AIRealRect rect;
		rect.left = -origin.h;
		rect.bottom  = -origin.v;
		rect.right =  rect.left + setup.width;
		rect.top = rect.bottom + setup.height;
		gEngine->callVoidMethod(env, artboard, gEngine->mid_ai_Artboard_set,
			gEngine->convertRectangle(env, kCurrentCoordinates, &rect),
			// TODO: Find out if these can be simulated somehow too?
			false, false, false, 1.0f
		);
		return true;
#endif // kPluginInterfaceVersion < kAI14 
	} EXCEPTION_CONVERT(env);
	return false;
}

// memset to 0 since it might have more fields than we support here (e.g.
// additional fields in CS3)
#define DEFINE_CROPAREA() \
	AICropArea area; \
	memset(&area, 0, sizeof(AICropArea)); \
	gEngine->convertRectangle(env, kCurrentCoordinates, bounds, &area.m_CropAreaRect); \
	area.m_fPAR = pixelAspectRatio; \
	area.m_bShowCenter = showCenter; \
	area.m_bShowCrossHairs = showCrossHairs; \
	area.m_bShowSafeAreas = showSafeAreas; \

/*
 * boolean nativeInsert(int handle, int index, com.scriptographer.ai.Rectangle
 *     bounds, boolean showCenter, boolean showCrossHairs,
 *     boolean showSafeAreas, double pixelAspectRatio)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_ArtboardList_nativeInsert(
		JNIEnv *env, jclass cls, jint handle, jint index, jobject bounds,
		jboolean showCenter, jboolean showCrossHairs, jboolean showSafeAreas,
		jdouble pixelAspectRatio) {
#if kPluginInterfaceVersion >= kAI14
	try {
		Document_activate((AIDocumentHandle) handle);
		DEFINE_CROPAREA();
		ASInt32 count = 0, pos = 0;
		sAICropArea->GetCount(&count);
		if (index >= count) {
			if (index > count) {
				// Add empty areas
				AICropArea empty;
				memset(&empty, 0, sizeof(AICropArea));
				while (pos < index)
					if (sAICropArea->AddNew(&empty, &pos))
						throw new StringException("Cannot add artboard");
			}
			// Simply add at the end
			if (sAICropArea->AddNew(&area, &pos))
				throw new StringException("Cannot add artboard");
		} else {
			// Get last one and add it again at the end, to start moving the
			// others by one, down to index.
			AICropAreaPtr prev = NULL;
			if (sAICropArea->Get(count - 1, &prev))
				throw new StringException("Cannot get artboard");
			if (sAICropArea->AddNew(prev, &pos))
				throw new StringException("Cannot add artboard");
			// Move the ones above index one up now, excluding the last one
			// which was already handled above.
			for (int i = count - 2; i >= index; i--) {
				if (sAICropArea->Get(i, &prev))
					throw new StringException("Cannot get artboard");
				if (sAICropArea->Update(i + 1, prev))
					throw new StringException("Cannot update artboard");
			}
			if (sAICropArea->Update(index, &area))
				throw new StringException("Cannot update artboard");
		}
		return true;
	} EXCEPTION_CONVERT(env);
#else // kPluginInterfaceVersion < kAI14
	// Do nothing. We can't insert artboards on CS3 and below.
#endif // kPluginInterfaceVersion < kAI14 
	return false;
}

/*
 * boolean nativeSet(int handle, int index, com.scriptographer.ai.Rectangle
 *     bounds, boolean showCenter, boolean showCrossHairs,
 *     boolean showSafeAreas, double pixelAspectRatio)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_ArtboardList_nativeSet(
		JNIEnv *env, jclass cls, jint handle, jint index, jobject bounds,
		jboolean showCenter, jboolean showCrossHairs, jboolean showSafeAreas,
		jdouble pixelAspectRatio) {
	try {
		Document_activate((AIDocumentHandle) handle);
#if kPluginInterfaceVersion >= kAI14
		DEFINE_CROPAREA();
		// If index is bigger than array size, add empty artboards until we
		// reach the needed size, then update that field.
		ASInt32 count = 0, pos = 0;
		sAICropArea->GetCount(&count);
		if (index >= count) {
			AICropArea empty;
			memset(&empty, 0, sizeof(AICropArea));
			while (pos < index)
				if (sAICropArea->AddNew(&empty, &pos))
					throw new StringException("Cannot add artboard");
		}
		if (sAICropArea->Update(index, &area))
			throw new StringException("Cannot update artboard");
		return true;
#else // kPluginInterfaceVersion < kAI14
	// Do nothing. We can't change artboards on CS3 and below.
#endif // kPluginInterfaceVersion < kAI14 
	} EXCEPTION_CONVERT(env);
	return false;
}
