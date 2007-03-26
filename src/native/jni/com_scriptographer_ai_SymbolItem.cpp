/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
#include "com_scriptographer_ai_SymbolItem.h"

/*
 * com.scriptographer.ai.SymbolItem
 */

/*
 * int nativeCreate(int symbolHandle, java.awt.geom.AffineTransform at)
 */

JNIEXPORT jint JNICALL Java_com_scriptographer_ai_SymbolItem_nativeCreate(JNIEnv *env, jclass cls, jint symbolHandle, jobject at) {
	try {
		short paintOrder;
		AIArtHandle artInsert = Art_getInsertionPoint(&paintOrder);
		AIRealMatrix m;
		gEngine->convertMatrix(env, at, &m);
		// harden the matrix as symbols use hard matrixes internaly
		sAIHardSoft->AIRealMatrixHarden(&m);
		AIArtHandle res = NULL;
		sAISymbol->NewInstanceWithTransform((AIPatternHandle) symbolHandle, &m, paintOrder, artInsert, &res);
		return (jint) res;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeGetSymbol()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_SymbolItem_nativeGetSymbol(JNIEnv *env, jobject obj) {
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
JNIEXPORT void JNICALL Java_com_scriptographer_ai_SymbolItem_setSymbol(JNIEnv *env, jobject obj, jobject symbol) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		gEngine->getPatternHandle(env, symbol);
		sAISymbol->SetSymbolPatternOfSymbolArt(art, symbol);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.Matrix getMatrix()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_SymbolItem_getMatrix(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIRealMatrix m;
		sAISymbol->GetSoftTransformOfSymbolArt(art, &m);
		return gEngine->convertMatrix(env, &m);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setMatrix(java.awt.geom.AffineTransform at)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_SymbolItem_setMatrix(JNIEnv *env, jobject obj, jobject at) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		AIRealMatrix m;
		gEngine->convertMatrix(env, at, &m);
		sAISymbol->SetSoftTransformOfSymbolArt(art, &m);
	} EXCEPTION_CONVERT(env);
}
