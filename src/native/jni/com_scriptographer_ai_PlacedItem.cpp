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
#include "ScriptographerPlugin.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_PlacedItem.h"

/*
 * com.scriptographer.ai.PlacedItem
 */

AIArtHandle JNICALL PlacedItem_place(JNIEnv *env, AIDocumentHandle doc, jobject file, jboolean linked) {
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
			throw new StringException("Cannot create placed item.");
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
		throw new StringException("Cannot create placed item.");
	
	return request.m_hNewArt;
}

/*
 * com.scriptographer.ai.Matrix getMatrix()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_PlacedItem_getMatrix(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIRealMatrix m;
		sAIPlaced->GetPlacedMatrix(art, &m);
		return gEngine->convertMatrix(env, &m);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setMatrix(com.scriptographer.ai.Matrix matrix)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_PlacedItem_setMatrix(JNIEnv *env, jobject obj, jobject matrix) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		AIRealMatrix mx;
		gEngine->convertMatrix(env, matrix, &mx);
		sAIPlaced->SetPlacedMatrix(art, &mx);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.Item embed(boolean askParams)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_PlacedItem_embed(JNIEnv *env, jobject obj, jboolean askParams) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		AIArtHandle res = NULL;
		sAIPlaced->MakePlacedObjectNative(art, &res, askParams);
		if (res != NULL)
			return gEngine->wrapArtHandle(env, res);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Rectangle getBoundingBox()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_PlacedItem_getBoundingBox(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		AIRealRect rect;
		sAIPlaced->GetPlacedBoundingBox(art, &rect);
		return gEngine->convertRectangle(env, &rect);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * java.io.File getFile()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_PlacedItem_getFile(JNIEnv *env, jobject obj) {
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
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_PlacedItem_isEps(JNIEnv *env, jobject obj) {
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
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_PlacedItem_nativeCreate(JNIEnv *env, jclass cls, jobject file) {
	try {
		return (jint) PlacedItem_place(env, NULL, file, true);
	} EXCEPTION_CONVERT(env);
	return 0;
}
