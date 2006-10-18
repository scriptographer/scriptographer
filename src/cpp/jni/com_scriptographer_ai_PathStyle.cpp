/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Revision: 1.6 $
 * $Date: 2006/10/18 14:17:17 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_PathStyle.h"

/*
 * com.scriptographer.ai.PathStyle
 */
 
#define UNDEFINED -1

/**
 * map can be NULL
 */
void PathStyle_init(JNIEnv *env, jobject obj, AIPathStyle *style, AIPathStyleMap *map) {
	jobject fillColor, strokeColor;
	jfloatArray dashArray;

	// Fill
	// if map is set, don't check every component. just assume that if kind is set, the rest is as well. that should
	// be enough for ATE... (TODO: is it???)
	if (style->fillPaint && (map == NULL || map->fillPaint && map->fill.color.kind)) {
		fillColor = gEngine->convertColor(env, &style->fill.color);
	} else {
		fillColor = NULL;
	}
	// Stroke
	// if map is set, don't check every component. just assume that if kind is set, the rest is as well. that should
	// be enough for ATE... (TODO: is it???)
	if (style->strokePaint && (map == NULL || map->strokePaint && map->stroke.color.kind)) {
		strokeColor = gEngine->convertColor(env, &style->stroke.color);
	} else {
		strokeColor = NULL;
	}

	// Dash
	if (style->strokePaint && (map == NULL || map->strokePaint && map->stroke.dash.length)) {
		int count = style->stroke.dash.length;
		dashArray = env->NewFloatArray(count);
		env->SetFloatArrayRegion(dashArray, 0, count, style->stroke.dash.array);
	} else {
		dashArray = NULL;
	}

	// call init:
	if (map == NULL) {
		gEngine->callVoidMethod(env, obj, gEngine->mid_PathStyle_init,
				fillColor, true, style->fill.overprint,
				strokeColor, true, style->stroke.overprint, style->stroke.width,
				style->stroke.dash.offset, dashArray,
				style->stroke.cap, style->stroke.join, style->stroke.miterLimit,
				style->clip, style->lockClip, style->evenodd, style->resolution
		);
	} else {
		gEngine->callVoidMethod(env, obj, gEngine->mid_PathStyle_init,
				fillColor, map->fillPaint, map->fill.overprint ? (style->fill.overprint ? 1 : 0) : - 1,
				strokeColor, map->strokePaint, map->stroke.overprint ? (style->stroke.overprint ? 1 : 0) : - 1, map->stroke.width ? style->stroke.width : - 1,
				map->stroke.dash.offset ? style->stroke.dash.offset : - 1, dashArray,
				map->stroke.cap ? style->stroke.cap : - 1, map->stroke.join ? style->stroke.join : - 1, map->stroke.miterLimit ? style->stroke.miterLimit : - 1,
				map->clip ? (style->clip ? 1 : 0) : - 1, map->lockClip ? (style->lockClip ? 1 : 0) : - 1, map->evenodd ? (style->evenodd ? 1 : 0) : - 1, map->resolution ? style->resolution : - 1
		); 
	}
}

/**
 * functions that take all the passed parameters to the various init functions and fills a style and a map structure.
 */
void PathStyle_convertPathStyle(JNIEnv *env, AIPathStyle *style, AIPathStyleMap *map, jfloatArray fillColor, jboolean hasFillColor, jshort fillOverprint, jfloatArray strokeColor, jboolean hasStrokeColor, jshort strokeOverprint, jfloat strokeWidth, jfloat dashOffset, jfloatArray dashArray, jshort cap, jshort join, jfloat miterLimit, jshort clip, jshort lockClip, jshort evenOdd, jfloat resolution) {
	// Fill
	int fillPaint = PathStyle_convertFillStyle(env, &style->fill, &map->fill, fillColor, hasFillColor, fillOverprint);
	if (fillPaint != UNDEFINED) {
		style->fillPaint = fillPaint;
		map->fillPaint = true;
	}

	// Stroke
	int strokePaint = PathStyle_convertStrokeStyle(env, &style->stroke, &map->stroke, strokeColor, hasStrokeColor, strokeOverprint, strokeWidth, dashOffset, dashArray, cap, join, miterLimit);
	if (strokePaint != UNDEFINED) {
		style->strokePaint = strokePaint;
		map->strokePaint = true;
	}
	
	// Path
	if (clip >= 0) {
		style->clip = clip != 0;
		map->clip = true;
	}
	if (lockClip >= 0) {
		style->lockClip = lockClip != 0;
		map->lockClip = true;
	}
	if (evenOdd >= 0) {
		style->evenodd = evenOdd != 0;
		map->evenodd = true;
	}
	if (resolution >= 0) {
		style->resolution = resolution;
		map->resolution = true;
	}
}

int PathStyle_convertFillStyle(JNIEnv *env, AIFillStyle *style, AIFillStyleMap *map, jfloatArray fillColor, jboolean hasFillColor, jshort fillOverprint) {
	int fillPaint = UNDEFINED;
	// set all to false:
	memset(map, 0, sizeof(AIFillStyleMap));
	// Fill
	if (hasFillColor) {
		if (fillColor != NULL) {
			gEngine->convertColor(env, fillColor, &style->color);
			map->color.kind = true;
			// turn on every component:
			memset(&map->color.c, 0xff, sizeof(AIColorUnionMap));
			fillPaint = true;
		} else {
			fillPaint = false;
		}
	}
	
	if (fillOverprint >= 0) {
		style->overprint = fillOverprint != 0;
		map->overprint = true;
	}
	return fillPaint;
}
 
int PathStyle_convertStrokeStyle(JNIEnv *env, AIStrokeStyle *style, AIStrokeStyleMap *map, jfloatArray strokeColor, jboolean hasStrokeColor, jshort strokeOverprint, jfloat strokeWidth, jfloat dashOffset, jfloatArray dashArray, jshort cap, jshort join, jfloat miterLimit) {
	int strokePaint = UNDEFINED;
	// set all to false:
	memset(map, 0, sizeof(AIStrokeStyleMap));
	// Stroke
	if (hasStrokeColor) {
		if (strokeColor != NULL) {
			gEngine->convertColor(env, strokeColor, &style->color);
			map->color.kind = true;
			// turn on every component:
			memset(&map->color.c, 0xff, sizeof(AIColorUnionMap));
			strokePaint = true;
		} else {
			strokePaint = false;
		}
	}
	if (strokeOverprint >= 0) {
		style->overprint = strokeOverprint;
		map->overprint = true;
	}
	if (strokeWidth >= 0) {
		style->width = strokeWidth;
		map->width = true;
	}
	if (cap >= 0) {
		style->cap = (AILineCap) cap;
		map->cap = true;
	}
	if (join >= 0) {
		style->join = (AILineJoin) join;
		map->join = true;
	}
	if (miterLimit >= 0) {
		style->miterLimit = miterLimit;
		map->miterLimit = true;
	}
		
	// Dash
	if (dashOffset >= 0) {
		style->dash.offset = dashOffset;
		map->dash.offset = true;
	}
	
	if (dashArray != NULL) {
		int count = env->GetArrayLength(dashArray);
		style->dash.length = count;
		map->dash.length = true;
		if (count > 0) {
			env->GetFloatArrayRegion(dashArray, 0, count, style->dash.array);
			for (int i = 0; i < count; i++)
				map->dash.array[i] = true;	
		}
	}
	return strokePaint;
}

/*
 * void nativeFetch(int handle)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_PathStyle_nativeFetch(JNIEnv *env, jobject obj, jint handle) {
	try {
		// don't use pathStyleInitPathStyle here as there's no map:
		
		AIPathStyle style;
		sAIPathStyle->GetPathStyle((AIArtHandle) handle, &style);
		
		PathStyle_init(env, obj, &style, NULL);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeCommit(int docHandle, int handle,
			float[] fillColor, boolean hasFillColor, short fillOverprint,
			float[] strokeColor, boolean hasStrokeColor, short strokeOverprint, float strokeWidth,
			float dashOffset, float[] dashArray,
			short cap, short join, float miterLimit,
			short clip, short lockClip, short evenOdd, float resolution)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_PathStyle_nativeCommit(JNIEnv *env, jobject obj, jint docHandle, jint handle, jfloatArray fillColor, jboolean hasFillColor, jshort fillOverprint, jfloatArray strokeColor, jboolean hasStrokeColor, jshort strokeOverprint, jfloat strokeWidth, jfloat dashOffset, jfloatArray dashArray, jshort cap, jshort join, jfloat miterLimit, jshort clip, jshort lockClip, jshort evenOdd, jfloat resolution) {
	try {
		Document_activate((AIDocumentHandle) docHandle);
		AIPathStyle style;
		AIPathStyleMap map; // is not needed here but we need to pass it
		// fill with current values as not everything might be set
		// TODO: instead of the path's style, this should be the current default style?
		// because if the user sets a value to undefined, this should fall back to the default value...
		sAIPathStyle->GetPathStyle((AIArtHandle) handle, &style);
		PathStyle_convertPathStyle(env, &style, &map, fillColor, hasFillColor, fillOverprint, strokeColor, hasStrokeColor, strokeOverprint, strokeWidth, dashOffset, dashArray, cap, join, miterLimit, clip, lockClip, evenOdd, resolution);
		sAIPathStyle->SetPathStyle((AIArtHandle) handle, &style);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeInitStrokeStyle(int handle, float[] strokeColor, boolean hasStrokeColor, short strokeOverprint, float strokeWidth, float dashOffset, float[] dashArray, short cap, short join, float miterLimit)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_PathStyle_nativeInitStrokeStyle(JNIEnv *env, jclass cls, jint handle, jfloatArray strokeColor, jboolean hasStrokeColor, jshort strokeOverprint, jfloat strokeWidth, jfloat dashOffset, jfloatArray dashArray, jshort cap, jshort join, jfloat miterLimit) {
	try {
		AIStrokeStyleMap map; // unused
		PathStyle_convertStrokeStyle(env, (AIStrokeStyle *) handle, &map, strokeColor, hasStrokeColor, strokeOverprint, strokeWidth, dashOffset, dashArray, cap, join, miterLimit);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeInitFillStyle(int handle, float[] fillColor, boolean hasFillColor, short fillOverprint)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_PathStyle_nativeInitFillStyle(JNIEnv *env, jclass cls, jint handle, jfloatArray fillColor, jboolean hasFillColor, jshort fillOverprint) {
	try {
		AIFillStyleMap map; // unused
		PathStyle_convertFillStyle(env, (AIFillStyle *) handle, &map, fillColor, hasFillColor, fillOverprint);
	} EXCEPTION_CONVERT(env);
}
