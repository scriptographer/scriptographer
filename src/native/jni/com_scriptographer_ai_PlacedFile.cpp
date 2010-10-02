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
#include "com_scriptographer_ai_PlacedFile.h"

/*
 * com.scriptographer.ai.PlacedFile
 */

AIArtHandle JNICALL PlacedFile_place(JNIEnv *env, AIDocumentHandle doc, jobject file, jboolean linked) {
	Document_activate(doc);
	AIPlaceRequestData request;
	SPPlatformFileSpecification fileSpec;
#if kPluginInterfaceVersion >= kAI12
	ai::FilePath filePath;
#endif
	memset(&request, 0, sizeof(AIPlaceRequestData));
	request.m_lPlaceMode = kVanillaPlace;
	if (file != NULL) {
		if (!gEngine->convertFile(env, file, &fileSpec))
			throw new StringException("Unable to create placed item.");
#if kPluginInterfaceVersion < kAI12
		request.m_pSPFSSpec = &fileSpec;
#else
		filePath = ai::FilePath(fileSpec);
		request.m_pFilePath = &filePath;
#endif
	}
	request.m_filemethod = linked ? 1 : 0;
	if (
#if kPluginInterfaceVersion < kAI12
		sAIPlaced->ExecPlaceRequest(&request)
#else
		sAIPlaced->ExecPlaceRequest(request)
#endif
		|| request.m_hNewArt == NULL)
		throw new StringException("Unable to create placed item.");
	
	return request.m_hNewArt;
}

/*
 * com.scriptographer.ai.Matrix getMatrix()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_PlacedFile_getMatrix(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIRealMatrix mx;
		sAIPlaced->GetPlacedMatrix(art, &mx);
		sAIHardSoft->AIRealMatrixHarden(&mx);
		sAIRealMath->AIRealMatrixConcatScale(&mx, 1, -1);
		return gEngine->convertMatrix(env, kCurrentCoordinates, kArtboardCoordinates, &mx);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setMatrix(com.scriptographer.ai.Matrix matrix)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_PlacedFile_setMatrix(JNIEnv *env, jobject obj, jobject matrix) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		AIRealMatrix mx;
		gEngine->convertMatrix(env, kArtboardCoordinates, kCurrentCoordinates, matrix, &mx);
		// Similar issue as in GlyphRun#getMatrix() (see TextRange),
		// again not clear why this solves it.
		sAIRealMath->AIRealMatrixConcatScale(&mx, 1, -1);
		sAIHardSoft->AIRealMatrixSoften(&mx);
		sAIPlaced->SetPlacedMatrix(art, &mx);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.Item embed(boolean askParams)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_PlacedFile_embed(JNIEnv *env, jobject obj, jboolean askParams) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		AIArtHandle res = NULL;
		AIArtSet set = Item_getSelected(false);
		long values;
		bool selected = !sAIArt->GetArtUserAttr(art, kArtSelected, &values) && values;
		sAIPlaced->MakePlacedObjectNative(art, &res, askParams);
		Item_deselectAll();
		Item_setSelected(set);
		sAIArtSet->DisposeArtSet(&set);
		if (res != NULL) {
			sAIArt->SetArtUserAttr(res, kArtSelected, selected ? kArtSelected : 0);
			// No need to pass document since we're activating document in getArtHandle
			return gEngine->wrapArtHandle(env, res, NULL, true);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Size getSize()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_PlacedFile_getSize(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		AIRealRect rect;
		if (!sAIPlaced->GetPlacedBoundingBox(art, &rect)) {
			DEFINE_POINT(point, rect.right - rect.left, rect.top - rect.bottom);
			return gEngine->convertSize(env, &point);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * java.io.File getFile()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_PlacedFile_getFile(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		SPPlatformFileSpecification fileSpec;
		// convert to SPPlatformFileSpecification to convert back to string through fileSpecToPath
		// like this, we're sure to get a proper java path on mac...
		// TODO: maybe move this to gPlugin->pathToPath() ?
#if kPluginInterfaceVersion < kAI12
		char path[kMaxPathLength];
		if (!sAIPlaced->GetPlacedFilePathFromArt(art, path, kMaxPathLength) &&
			!sAIUser->Path2SPPlatformFileSpecification(path, &fileSpec)) {
#else
		ai::UnicodeString path;
		if (!sAIPlaced->GetPlacedFilePathFromArt(art, path)) {
			ai::FilePath(path).GetAsSPPlatformFileSpec(fileSpec);
#endif
			return gEngine->convertFile(env, &fileSpec);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * boolean isEps()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_PlacedFile_isEps(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		short placedType = kOtherType;
		sAIPlaced->GetPlacedType(art, &placedType);
		return placedType == kEPSType;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * int nativeCreate(java.io.File file)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_PlacedFile_nativeCreate(JNIEnv *env, jclass cls, jobject file) {
	try {
		return (jint) PlacedFile_place(env, NULL, file, true);
	} EXCEPTION_CONVERT(env);
	return 0;
}
