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
 * $RCSfile: com_scriptographer_ai_Curve.cpp,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:58 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Curve.h"

/*
 * com.scriptographer.ai.Bezier
 */

double curveGetPartLength(AIRealBezier *bezier, AIReal fromPosition, AIReal toPosition, AIReal flatness) {
	AIRealBezier b1, b2;
	if (fromPosition > toPosition) {
		AIReal temp = fromPosition;
		fromPosition = toPosition;
		toPosition = temp;
	}
	sAIRealBezier->Divide(bezier, fromPosition, &b1, &b2);
	sAIRealBezier->Divide(&b2, toPosition, &b2, &b1);
	return sAIRealBezier->Length(&b2, flatness);	
}

double curveGetPositionWithLength(AIRealBezier *bezier, AIReal length, AIReal flatness) {
	double bezierLength = sAIRealBezier->Length(bezier, flatness);
	double pos = length / bezierLength, oldPos = 0, oldF = 1;
	for (int i = 0; i < 100; i++) { // prevent too many iterations...
		double stepLength = curveGetPartLength(bezier, oldPos, pos, flatness);
		double f = fabs(stepLength - length) / length; // f: value for exactness
		if (f < 0.01 || f >= oldF) break; // if it's exact enough or even getting worse with iteration, break the loop...
		pos += (length - stepLength) / bezierLength;
		// if pos < 0 then pos = 0
		oldF = f;
	}
	return pos;
}

/*
 * float nativeGetLength(float p1x, float p1y, float h1x, float h1y, float h2x, float h2y, float p2x, float p2y, float flatness)
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_Curve_nativeGetLength(JNIEnv *env, jobject obj, jfloat p1x, jfloat p1y, jfloat h1x, jfloat h1y, jfloat h2x, jfloat h2y, jfloat p2x, jfloat p2y, jfloat flatness) {
	try {
		DEFINE_BEZIER(bezier, p1x, p1y, h1x, h1y, h2x, h2y, p2x, p2y);
		return sAIRealBezier->Length(&bezier, flatness);
	} EXCEPTION_CONVERT(env)
	return 0.0;
}

/*
 * float nativeGetPartLength(float p1x, float p1y, float h1x, float h1y, float h2x, float h2y, float p2x, float p2y, float fromPosition, float toPosition, float flatness)
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_Curve_nativeGetPartLength(JNIEnv *env, jobject obj, jfloat p1x, jfloat p1y, jfloat h1x, jfloat h1y, jfloat h2x, jfloat h2y, jfloat p2x, jfloat p2y, jfloat fromPosition, jfloat toPosition, jfloat flatness) {
	try {
		DEFINE_BEZIER(bezier, p1x, p1y, h1x, h1y, h2x, h2y, p2x, p2y);
		return curveGetPartLength(&bezier, fromPosition, toPosition, flatness);
	} EXCEPTION_CONVERT(env)
	return 0.0;
}

/*
 * float nativeGetPositionWithLength(float p1x, float p1y, float h1x, float h1y, float h2x, float h2y, float p2x, float p2y, float length, float flatness)
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_Curve_nativeGetPositionWithLength(JNIEnv *env, jobject obj, jfloat p1x, jfloat p1y, jfloat h1x, jfloat h1y, jfloat h2x, jfloat h2y, jfloat p2x, jfloat p2y, jfloat length, jfloat flatness) {
	try {
		DEFINE_BEZIER(bezier, p1x, p1y, h1x, h1y, h2x, h2y, p2x, p2y);
		return curveGetPositionWithLength(&bezier, length, flatness);
	} EXCEPTION_CONVERT(env)
	return 0.0;
}

/*
 * void nativeAdjustThroughPoint(float[] values, float x, float y, float position)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Curve_nativeAdjustThroughPoint(JNIEnv *env, jobject obj, jfloatArray values, jfloat x, jfloat y, jfloat position) {
	try {
		AIPathSegment *segments = (AIPathSegment *) env->GetFloatArrayElements(values, NULL);
		DEFINE_POINT(pt, x, y);
		AIRealBezier bezier;
		bezier.p0 = segments[0].p;
		bezier.p1 = segments[0].out;
		bezier.p2 = segments[1].in;
		bezier.p3 = segments[2].p;
		sAIRealBezier->AdjustThroughPoint(&bezier, &pt, position);
		segments[0].p = bezier.p0;
		segments[0].out = bezier.p1;
		segments[1].in = bezier.p2;
		segments[2].p = bezier.p3;
		env->ReleaseFloatArrayElements(values, (jfloat *) segments, 0);
	} EXCEPTION_CONVERT(env)
}
