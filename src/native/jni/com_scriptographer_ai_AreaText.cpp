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
#include "com_scriptographer_ai_AreaText.h"

/*
 * com.scriptographer.ai.AreaText
 */

using namespace ATE;

/*
 * int nativeCreate(int orient, int artHandle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_AreaText_nativeCreate(JNIEnv *env, jclass cls, jint orientation, jint artHandle) {
	AIArtHandle art = NULL;
	short paintOrder;
	AIArtHandle artInsert = Item_getInsertionPoint(&paintOrder);
	sAITextFrame->NewInPathText(paintOrder, artInsert, (AITextOrientation) orientation, (AIArtHandle) artHandle, NULL, false, &art);
	if (art == NULL)
		throw new StringException("Unable to create text object. Please make sure there is an open document.");

	return (jint) art;
}

/*
 * int getRowCount()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_AreaText_getRowCount(JNIEnv *env, jobject obj) {
	ASInt32 count = 0;
	try {
		TextFrameRef frame = gEngine->getTextFrameHandle(env, obj);
		sTextFrame->GetRowCount(frame, &count);
		sTextFrame->Release(frame);
	} EXCEPTION_CONVERT(env);
	return count;
}

/*
 * void setRowCount(int count)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_AreaText_setRowCount(JNIEnv *env, jobject obj, jint count) {
	try {
		TextFrameRef frame = gEngine->getTextFrameHandle(env, obj, true);
		sTextFrame->SetRowCount(frame, count);
		sTextFrame->Release(frame);
	} EXCEPTION_CONVERT(env);
}

/*
 * int getColumnCount()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_AreaText_getColumnCount(JNIEnv *env, jobject obj) {
	ASInt32 count = 0;
	try {
		TextFrameRef frame = gEngine->getTextFrameHandle(env, obj);
		sTextFrame->GetColumnCount(frame, &count);
		sTextFrame->Release(frame);
	} EXCEPTION_CONVERT(env);
	return count;
}

/*
 * void setColumnCount(int count)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_AreaText_setColumnCount(JNIEnv *env, jobject obj, jint count) {
	try {
		TextFrameRef frame = gEngine->getTextFrameHandle(env, obj, true);
		sTextFrame->SetColumnCount(frame, count);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean getRowMajorOrder()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_AreaText_getRowMajorOrder(JNIEnv *env, jobject obj) {
	ATEBool8 rowMajorOrder = false;
	try {
		TextFrameRef frame = gEngine->getTextFrameHandle(env, obj);
		sTextFrame->GetRowMajorOrder(frame, &rowMajorOrder);
		sTextFrame->Release(frame);
	} EXCEPTION_CONVERT(env);
	return rowMajorOrder;
}

/*
 * void setRowMajorOrder(boolean rowMajorOrder)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_AreaText_setRowMajorOrder(JNIEnv *env, jobject obj, jboolean rowMajorOrder) {
	try {
		TextFrameRef frame = gEngine->getTextFrameHandle(env, obj, true);
		sTextFrame->SetRowMajorOrder(frame, rowMajorOrder);
		sTextFrame->Release(frame);
	} EXCEPTION_CONVERT(env);
}

/*
 * float getRowGutter()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_AreaText_getRowGutter(JNIEnv *env, jobject obj) {
	try {
		TextFrameRef frame = gEngine->getTextFrameHandle(env, obj);
		ASReal gutter;
		if (!sTextFrame->GetRowGutter(frame, &gutter))
			return gutter;
	} EXCEPTION_CONVERT(env);
	return 0.0;
}

/*
 * void setRowGutter(float gutter)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_AreaText_setRowGutter(JNIEnv *env, jobject obj, jfloat gutter) {
	try {
		TextFrameRef frame = gEngine->getTextFrameHandle(env, obj, true);
		sTextFrame->SetRowGutter(frame, gutter);
	} EXCEPTION_CONVERT(env);
}

/*
 * float getColumnGutter()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_AreaText_getColumnGutter(JNIEnv *env, jobject obj) {
	ASReal gutter = 0;
	try {
		TextFrameRef frame = gEngine->getTextFrameHandle(env, obj);
		sTextFrame->GetColumnGutter(frame, &gutter);
		sTextFrame->Release(frame);
	} EXCEPTION_CONVERT(env);
	return gutter;
}

/*
 * void setColumnGutter(float gutter)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_AreaText_setColumnGutter(JNIEnv *env, jobject obj, jfloat gutter) {
	try {
		TextFrameRef frame = gEngine->getTextFrameHandle(env, obj, true);
		sTextFrame->SetColumnGutter(frame, gutter);
		sTextFrame->Release(frame);
	} EXCEPTION_CONVERT(env);
}
