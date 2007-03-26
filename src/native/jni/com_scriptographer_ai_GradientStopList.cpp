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
#include "com_scriptographer_ai_GradientStopList.h"

/*
 * com.scriptographer.ai.GradientStopList
 */

/*
 * void nativeGet(int handle, int index, com.scriptographer.ai.GradientStop stop)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_GradientStopList_nativeGet(JNIEnv *env, jclass cls, jint handle, jint index, jobject stop) {
	try {
		AIGradientStop s;
		if (sAIGradient->GetNthGradientStop((AIGradientHandle) handle, index, &s))
			throw new StringException("Cannot get gradient stop");
		jobject color = gEngine->convertColor(env, &s.color);
		gEngine->callVoidMethod(env, stop, gEngine->mid_GradientStop_init, s.midPoint, s.rampPoint, color);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSet(int handle, int docHandle, int index, float midPoint, float rampPoint, float[] color)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_GradientStopList_nativeSet(JNIEnv *env, jclass cls, jint handle, jint docHandle, jint index, jfloat midPoint, jfloat rampPoint, jfloatArray color) {
	try {
		AIGradientStop s;
		s.midPoint = midPoint;
		s.rampPoint = rampPoint;
		gEngine->convertColor(env, color, &s.color);
		if (sAIGradient->SetNthGradientStop((AIGradientHandle) handle, index, &s))
			throw new StringException("Cannot set gradient stop");
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeInsert(int handle, int docHandle, int index, float midPoint, float rampPoint, float[] color)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_GradientStopList_nativeInsert(JNIEnv *env, jclass cls, jint handle, jint docHandle, jint index, jfloat midPoint, jfloat rampPoint, jfloatArray color) {
	try {
		AIGradientStop s;
		s.midPoint = midPoint;
		s.rampPoint = rampPoint;
		gEngine->convertColor(env, color, &s.color);
		if (sAIGradient->InsertGradientStop((AIGradientHandle) handle, index, &s))
			throw new StringException("Cannot insert gradient stop");
	} EXCEPTION_CONVERT(env);
}

/*
 * int nativeGetSize(int handle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_GradientStopList_nativeGetSize(JNIEnv *env, jclass cls, jint handle) {
	try {
		short count = 0;
		sAIGradient->GetGradientStopCount((AIGradientHandle) handle, &count);
		return count;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeRemove(int handle, int docHandle, int fromIndex, int toIndex)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_GradientStopList_nativeRemove(JNIEnv *env, jclass cls, jint handle, jint docHandle, jint fromIndex, jint toIndex) {
	try {
		for (int i = toIndex - 1; i >= fromIndex; i--) {
			// TODO: we might pass NULL instead of &s (verify)
			AIGradientStop s;
			sAIGradient->DeleteGradientStop((AIGradientHandle) handle, i, &s);
		}
		short count = 0;
		sAIGradient->GetGradientStopCount((AIGradientHandle) handle, &count);
		return count;
	} EXCEPTION_CONVERT(env);
	return 0;
}
