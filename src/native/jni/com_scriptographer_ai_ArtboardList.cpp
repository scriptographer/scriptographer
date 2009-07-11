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

#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_ArtboardList.h"

/*
 * com.scriptographer.ai.ArtboardList
 */

/*
 * int nativeGetSize(int handle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_ArtboardList_nativeGetSize(JNIEnv *env, jclass cls, jint handle) {
	try {
		Document_activate((AIDocumentHandle) handle);
		ASInt32 count = 0;
		sAICropArea->GetCount(&count);
		return count;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeRemove(int handle, int fromIndex, int toIndex)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_ArtboardList_nativeRemove(JNIEnv *env, jclass cls, jint handle, jint fromIndex, jint toIndex) {
	try {
		Document_activate((AIDocumentHandle) handle);
		AICropAreaPtr area = NULL;
		for (int i = toIndex - 1; i >= fromIndex; i--)
			sAICropArea->Delete(i, &area);
		ASInt32 count = 0;
		sAICropArea->GetCount(&count);
		return count;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void nativeGet(int handle, int index, com.scriptographer.ai.Artboard artboard)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_ArtboardList_nativeGet(JNIEnv *env, jclass cls, jint handle, jint index, jobject artboard) {
	try {
		Document_activate((AIDocumentHandle) handle);
		AICropAreaPtr area = NULL;
		if (sAICropArea->Get(index, &area))
			throw new StringException("Cannot get artboard");
		gEngine->callVoidMethod(env, artboard, gEngine->mid_ai_Artboard_set,
				gEngine->convertRectangle(env, &area->m_CropAreaRect),
				area->m_bShowCenter, area->m_bShowCrossHairs,
				area->m_bShowSafeAreas, area->m_fPAR
		);
	} EXCEPTION_CONVERT(env);
}

// memset to 0 since it might have more fields than we support here (e.g. additional fields in CS3)
#define DEFINE_CROPAREA() \
	AICropArea area; \
	memset(&area, 0, sizeof(AICropArea)); \
	gEngine->convertRectangle(env, bounds, &area.m_CropAreaRect); \
	area.m_fPAR = pixelAspectRatio; \
	area.m_bShowCenter = showCenter; \
	area.m_bShowCrossHairs = showCrossHairs; \
	area.m_bShowSafeAreas = showSafeAreas; \

/*
 * void nativeInsert(int handle, int index, com.scriptographer.ai.Rectangle bounds, boolean showCenter, boolean showCrossHairs, boolean showSafeAreas, double pixelAspectRatio)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_ArtboardList_nativeInsert(JNIEnv *env, jclass cls, jint handle, jint index, jobject bounds, jboolean showCenter, jboolean showCrossHairs, jboolean showSafeAreas, jdouble pixelAspectRatio) {
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
			// Get last one and add it again at the end, to start moving the others by one, down to index.
			AICropAreaPtr prev = NULL;
			if (sAICropArea->Get(count - 1, &prev))
				throw new StringException("Cannot get artboard");
			if (sAICropArea->AddNew(prev, &pos))
				throw new StringException("Cannot add artboard");
			// Move the ones above index one up now, excluding the last one which was already handled above.
			for (int i = count - 2; i >= index; i--) {
				if (sAICropArea->Get(i, &prev))
					throw new StringException("Cannot get artboard");
				if (sAICropArea->Update(i + 1, prev))
					throw new StringException("Cannot update artboard");
			}
			if (sAICropArea->Update(index, &area))
				throw new StringException("Cannot update artboard");
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSet(int handle, int index, com.scriptographer.ai.Rectangle bounds, boolean showCenter, boolean showCrossHairs, boolean showSafeAreas, double pixelAspectRatio)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_ArtboardList_nativeSet(JNIEnv *env, jclass cls, jint handle, jint index, jobject bounds, jboolean showCenter, jboolean showCrossHairs, jboolean showSafeAreas, jdouble pixelAspectRatio) {
	try {
		Document_activate((AIDocumentHandle) handle);
		DEFINE_CROPAREA();
		// If index is bigger than array size, add empty artboards until we reach the needed size, then update that field.
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
	} EXCEPTION_CONVERT(env);
}
