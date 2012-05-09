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
#include "com_scriptographer_ai_PlacedSymbol.h"

/*
 * com.scriptographer.ai.PlacedSymbol
 */

/*
 * int nativeCreate(int symbolHandle, com.scriptographer.ai.Matrix matrix)
 */

JNIEXPORT jint JNICALL Java_com_scriptographer_ai_PlacedSymbol_nativeCreate(
		JNIEnv *env, jclass cls, jint symbolHandle, jobject matrix) {
	try {
		short paintOrder;
		AIArtHandle artInsert = Item_getInsertionPoint(&paintOrder);
		AIRealMatrix mx;
		gEngine->convertMatrix(env, kArtboardCoordinates, kCurrentCoordinates,
				matrix, &mx);
		// harden the matrix as symbols use hard matrixes internaly
		sAIHardSoft->AIRealMatrixHarden(&mx);
		AIArtHandle res = NULL;
		sAISymbol->NewInstanceWithTransform((AIPatternHandle) symbolHandle, &mx,
				paintOrder, artInsert, &res);
		return (jint) res;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeGetSymbol()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_PlacedSymbol_nativeGetSymbol(
		JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIPatternHandle symbol = NULL;
		sAISymbol->GetSymbolPatternOfSymbolArt(art, &symbol);
		return (jint) symbol;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void setSymbol(com.scriptographer.ai.Symbol symbol)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_PlacedSymbol_setSymbol(
		JNIEnv *env, jobject obj, jobject symbol) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		gEngine->getPatternHandle(env, symbol);
		sAISymbol->SetSymbolPatternOfSymbolArt(art, symbol);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.Matrix getMatrix()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_PlacedSymbol_getMatrix(
		JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIRealMatrix mx;
		sAISymbol->GetSoftTransformOfSymbolArt(art, &mx);
		// Flip the scaleY value to reflect orientation of PlacedFile
		mx.d = -mx.d;
		return gEngine->convertMatrix(env, kCurrentCoordinates,
				kArtboardCoordinates, &mx);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setMatrix(com.scriptographer.ai.Matrix matrix)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_PlacedSymbol_setMatrix(
		JNIEnv *env, jobject obj, jobject matrix) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		AIRealMatrix mx;
		gEngine->convertMatrix(env, kArtboardCoordinates, kCurrentCoordinates,
				matrix, &mx);
		// Flip the scaleY value to reflect orientation of PlacedFile
		mx.d = -mx.d;
		sAISymbol->SetSoftTransformOfSymbolArt(art, &mx);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.Item embed()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_PlacedSymbol_embed(
		JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIArtHandle embedded = NULL;
		if (sAISymbol->BreakLinkToSymbol(art, kPlaceAbove, art, &embedded))
			throw new StringException("Unable to embed symbol item.");
		return gEngine->wrapArtHandle(env, embedded, NULL, true);
	} EXCEPTION_CONVERT(env);
	return NULL;
}
