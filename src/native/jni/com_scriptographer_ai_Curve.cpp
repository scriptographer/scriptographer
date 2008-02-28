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
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Curve.h"

/*
 * com.scriptographer.ai.Bezier
 */

/*
 * float nativeSize(float p1x, float p1y, float h1x, float h1y, float h2x, float h2y, float p2x, float p2y, float flatness)
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_Curve_nativeSize(JNIEnv *env, jclass cls, jfloat p1x, jfloat p1y, jfloat h1x, jfloat h1y, jfloat h2x, jfloat h2y, jfloat p2x, jfloat p2y, jfloat flatness) {
	try {
		DEFINE_BEZIER(bezier, p1x, p1y, h1x, h1y, h2x, h2y, p2x, p2y);
		return sAIRealBezier->Length(&bezier, flatness);
	} EXCEPTION_CONVERT(env);
	return 0.0;
}

/*
 * void nativeAdjustThroughPoint(float[] values, float x, float y, float parameter)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Curve_nativeAdjustThroughPoint(JNIEnv *env, jclass cls, jfloatArray values, jfloat x, jfloat y, jfloat parameter) {
	try {
		AIPathSegment *segments = (AIPathSegment *) env->GetFloatArrayElements(values, NULL);
		DEFINE_POINT(pt, x, y);
		AIRealBezier bezier;
		bezier.p0 = segments[0].p;
		bezier.p1 = segments[0].out;
		bezier.p2 = segments[1].in;
		bezier.p3 = segments[1].p;
		sAIRealBezier->AdjustThroughPoint(&bezier, &pt, parameter);
		segments[0].p = bezier.p0;
		segments[0].out = bezier.p1;
		segments[1].in = bezier.p2;
		segments[1].p = bezier.p3;
		env->ReleaseFloatArrayElements(values, (jfloat *) segments, 0);
	} EXCEPTION_CONVERT(env);
}
