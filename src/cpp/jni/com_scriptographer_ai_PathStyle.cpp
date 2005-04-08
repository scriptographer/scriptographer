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
 * $RCSfile: com_scriptographer_ai_PathStyle.cpp,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/04/08 21:56:40 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_PathStyle.h"

/*
 * com.scriptographer.ai.PathStyle
 */

/*
 * void nativeFetch(int artHandle)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_PathStyle_nativeFetch(JNIEnv *env, jobject obj, jint artHandle) {
	try {
		AIPathStyle style;

		jobject fillColor, strokeColor;
		jfloatArray dashArray;

		sAIPathStyle->GetPathStyle((AIArtHandle) artHandle, &style);

		// Fill
		if (style.fillPaint) {
			fillColor = gEngine->convertColor(env, &style.fill.color);
		} else {
			fillColor = NULL;
		}
		// Stroke
		if (style.strokePaint) {
			strokeColor = gEngine->convertColor(env, &style.stroke.color);
			// Dash
			int count = style.stroke.dash.length;
			dashArray = env->NewFloatArray(count);
			env->SetFloatArrayRegion(dashArray, 0, count, style.stroke.dash.array);
		} else {
			strokeColor = NULL;
			dashArray = NULL;
		}
		// call init:
		gEngine->callVoidMethod(env, obj, gEngine->mid_PathStyle_init,
				fillColor, style.fill.overprint,
				strokeColor, style.stroke.overprint, style.stroke.width, style.stroke.dash.offset, dashArray, style.stroke.cap, style.stroke.join, style.stroke.miterLimit,
				style.clip, style.lockClip, style.evenodd, style.resolution);
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeCommit(int artHandle, float[] fillColor, boolean fillOverprint,
			float[] strokeColor, boolean strokeOverprint, float strokeWidth, float dashOffset, float[] dashArray, short cap, short join, float miterLimit,
			boolean clip, boolean lockClip, boolean evenOdd, float resolution)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_PathStyle_nativeCommit(JNIEnv *env, jobject obj, jint artHandle, jfloatArray fillColor, jboolean fillOverprint, jfloatArray strokeColor, jboolean strokeOverprint, jfloat strokeWidth, jfloat dashOffset, jfloatArray dashArray, jshort cap, jshort join, jfloat miterLimit, jboolean clip, jboolean lockClip, jboolean evenOdd, jfloat resolution) {
	try {
		AIPathStyle style;
		
		// Fill
		style.fillPaint = fillColor != NULL;
		if (style.fillPaint) {
			gEngine->convertColor(env, fillColor, &style.fill.color);
			style.fill.overprint = fillOverprint;
		}

		// Stroke
		style.strokePaint = strokeColor != NULL;
		if (style.strokePaint) {
			gEngine->convertColor(env, strokeColor, &style.stroke.color);
			style.stroke.overprint = strokeOverprint;
			style.stroke.width = strokeWidth;
			style.stroke.cap = (AILineCap) cap;
			style.stroke.join = (AILineJoin) join;
			style.stroke.miterLimit = miterLimit;
			
			// Dash
			style.stroke.dash.offset = dashOffset;
			int count = env->GetArrayLength(dashArray);
			style.stroke.dash.length = count;
			if (count > 0) {
				env->GetFloatArrayRegion(dashArray, 0, count, style.stroke.dash.array);
			}
		}
		
		// Path
		style.clip = clip;
		style.lockClip = lockClip;
		style.evenodd = evenOdd;
		style.resolution = resolution;
		
		sAIPathStyle->SetPathStyle((AIArtHandle) artHandle, &style);
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeInitStrokeStyle(int handle, float[] strokeColor, boolean strokeOverprint, float strokeWidth, float dashOffset, float[] dashArray, short cap, short join, float miterLimit,)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_PathStyle_nativeInitStrokeStyle(JNIEnv *env, jclass cls, jint handle, jfloatArray strokeColor, jboolean strokeOverprint, jfloat strokeWidth, jfloat dashOffset, jfloatArray dashArray, jshort cap, jshort join, jfloat miterLimit) {
	try {
		AIStrokeStyle *style = (AIStrokeStyle *) handle;
		gEngine->convertColor(env, strokeColor, &style->color);
		style->overprint = strokeOverprint;
		style->width = strokeWidth;
		style->cap = (AILineCap) cap;
		style->join = (AILineJoin) join;
		style->miterLimit = miterLimit;
		
		// Dash
		style->dash.offset = dashOffset;
		int count = env->GetArrayLength(dashArray);
		style->dash.length = count;
		if (count > 0) {
			env->GetFloatArrayRegion(dashArray, 0, count, style->dash.array);
		}
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeInitFillStyle(int handle, float[] fillColor, boolean fillOverprint)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_PathStyle_nativeInitFillStyle(JNIEnv *env, jclass cls, jint handle, jfloatArray fillColor, jboolean fillOverprint) {
	try {
		AIFillStyle *style = (AIFillStyle *) handle;
		gEngine->convertColor(env, fillColor, &style->color);
		style->overprint = fillOverprint;
	} EXCEPTION_CONVERT(env)
}
