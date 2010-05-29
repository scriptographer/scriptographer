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

#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Item.h"
#include "com_scriptographer_ai_PathStyle.h"

/*
 * com.scriptographer.ai.PathStyle
 */
 
#define UNDEFINED -1

bool PathStyle_hasColor(AIColor* color, AIColorMap* map) {
	return map->kind && (
		color->kind == kGrayColor
			&& map->c.g.gray
		|| color->kind == kThreeColor
			&& map->c.rgb.red && map->c.rgb.green && map->c.rgb.blue
		|| color->kind == kFourColor
			&& map->c.f.cyan && map->c.f.magenta && map->c.f.yellow && map->c.f.black);
}

/**
 * map can be NULL
 */
void PathStyle_init(JNIEnv *env, jobject obj, AIPathStyle *style, AIPathStyleMap *map) {
	jobject fillColor, strokeColor;
	jfloatArray dashArray;

	// Fill
	// if map is set, check every component through PathStyle_hasColor
	if (style->fillPaint && (map == NULL || map->fillPaint
			&& PathStyle_hasColor(&style->fill.color, &map->fill.color))) {
		fillColor = gEngine->convertColor(env, &style->fill.color);
	} else {
		fillColor = NULL;
	}
	// Stroke
	// if map is set, don't check every component through PathStyle_hasColor
	if (style->strokePaint && (map == NULL || map->strokePaint
			&& PathStyle_hasColor(&style->stroke.color, &map->stroke.color))) {
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

	// Call init:
	if (map == NULL) {
		gEngine->callVoidMethod(env, obj, gEngine->mid_PathStyle_init,
				fillColor, true, style->fill.overprint,
				strokeColor, true, style->stroke.overprint, style->stroke.width,
				style->stroke.cap, style->stroke.join, style->stroke.miterLimit,
				style->stroke.dash.offset, dashArray,
				style->clip, style->lockClip, style->evenodd, style->resolution
		);
	} else {
		gEngine->callVoidMethod(env, obj, gEngine->mid_PathStyle_init,
				fillColor, map->fillPaint, map->fill.overprint ? (style->fill.overprint ? 1 : 0) : - 1,
				strokeColor, map->strokePaint, map->stroke.overprint ? (style->stroke.overprint ? 1 : 0) : - 1, map->stroke.width ? style->stroke.width : - 1,
				map->stroke.cap ? style->stroke.cap : - 1, map->stroke.join ? style->stroke.join : - 1, map->stroke.miterLimit ? style->stroke.miterLimit : - 1,
				map->stroke.dash.offset ? style->stroke.dash.offset : - 1, dashArray,
				map->clip ? (style->clip ? 1 : 0) : - 1, map->lockClip ? (style->lockClip ? 1 : 0) : - 1, map->evenodd ? (style->evenodd ? 1 : 0) : - 1, map->resolution ? style->resolution : - 1
		); 
	}
}

/**
 * Functions that take all the passed parameters to the various init functions and fills a style and a map structure.
 */
void PathStyle_convertPathStyle(JNIEnv *env, AIPathStyle *style, AIPathStyleMap *map,
		jobject fillColor, jboolean hasFillColor, jshort fillOverprint,
		jobject strokeColor, jboolean hasStrokeColor, jshort strokeOverprint, jfloat strokeWidth,
		jshort strokeCap, jshort strokeJoin, jfloat miterLimit,
		jfloat dashOffset, jfloatArray dashArray,
		jshort clip, jshort lockClip, jint windingRule, jfloat resolution) {
	// Fill
	int fillPaint = PathStyle_convertFillStyle(env, &style->fill, map != NULL ? &map->fill : NULL,
			fillColor, hasFillColor, fillOverprint);
	if (fillPaint != UNDEFINED) {
		style->fillPaint = fillPaint;
		map->fillPaint = true;
	}

	// Stroke
	int strokePaint = PathStyle_convertStrokeStyle(env, &style->stroke, map != NULL ? &map->stroke : NULL,
			strokeColor, hasStrokeColor, strokeOverprint, strokeWidth,
			strokeCap, strokeJoin, miterLimit,
			dashOffset, dashArray);
	if (strokePaint != UNDEFINED) {
		style->strokePaint = strokePaint;
		if (map != NULL)
			map->strokePaint = true;
	}
	
	// Path
	if (clip >= 0) {
		style->clip = clip != 0;
		if (map != NULL)
			map->clip = true;
	}
	if (lockClip >= 0) {
		style->lockClip = lockClip != 0;
		if (map != NULL)
			map->lockClip = true;
	}
	if (windingRule >= 0) {
		style->evenodd = windingRule != 0;
		if (map != NULL)
			map->evenodd = true;
	}
	if (resolution >= 0) {
		style->resolution = resolution;
		if (map != NULL)
			map->resolution = true;
	}
}

int PathStyle_convertFillStyle(JNIEnv *env, AIFillStyle *style, AIFillStyleMap *map,
		jobject fillColor, jboolean hasFillColor, jshort fillOverprint) {
	int fillPaint = UNDEFINED;
	// set all to false:
	if (map != NULL)
		memset(map, 0, sizeof(AIFillStyleMap));
	// Fill
	if (hasFillColor) {
		if (fillColor != NULL) {
			gEngine->convertColor(env, fillColor, &style->color);
			fillPaint = true;
			if (map != NULL) {
				map->color.kind = true;
				// turn on every component:
				memset(&map->color.c, 0xff, sizeof(AIColorUnionMap));
			}
		} else {
			fillPaint = false;
		}
	}
	
	if (fillOverprint >= 0) {
		style->overprint = fillOverprint != 0;
		if (map != NULL)
			map->overprint = true;
	}
	return fillPaint;
}
 
int PathStyle_convertStrokeStyle(JNIEnv *env, AIStrokeStyle *style, AIStrokeStyleMap *map,
		jobject strokeColor, jboolean hasStrokeColor, jshort strokeOverprint, jfloat strokeWidth,
		jshort strokeCap, jshort strokeJoin, jfloat miterLimit,
		jfloat dashOffset, jfloatArray dashArray) {
	int strokePaint = UNDEFINED;
	// Set all to false:
	if (map != NULL)
		memset(map, 0, sizeof(AIStrokeStyleMap));
	// Stroke
	if (hasStrokeColor) {
		if (strokeColor != NULL) {
			gEngine->convertColor(env, strokeColor, &style->color);
			strokePaint = true;
			if (map != NULL) {
				map->color.kind = true;
				// Turn on every component:
				memset(&map->color.c, 0xff, sizeof(AIColorUnionMap));
			}
		} else {
			strokePaint = false;
		}
	}
	if (strokeOverprint >= 0) {
		style->overprint = strokeOverprint;
		if (map != NULL)
			map->overprint = true;
	}
	if (strokeWidth >= 0) {
		style->width = strokeWidth;
		if (map != NULL)
			map->width = true;
	}
	if (strokeCap >= 0) {
		style->cap = (AILineCap) strokeCap;
		if (map != NULL)
			map->cap = true;
	}
	if (strokeJoin >= 0) {
		style->join = (AILineJoin) strokeJoin;
		if (map != NULL)
			map->join = true;
	}
	if (miterLimit >= 0) {
		style->miterLimit = miterLimit;
		if (map != NULL)
			map->miterLimit = true;
	}
		
	// Dash
	if (dashOffset >= 0) {
		style->dash.offset = dashOffset;
		if (map != NULL)
			map->dash.offset = true;
	}
	
	if (dashArray != NULL) {
		int count = env->GetArrayLength(dashArray);
		style->dash.length = count;
		if (count > 0)
			env->GetFloatArrayRegion(dashArray, 0, count, style->dash.array);
		if (map != NULL) {
			map->dash.length = true;
			for (int i = 0; i < count; i++)
				map->dash.array[i] = true;	
		}
	}
	return strokePaint;
}

/*
 * void nativeGet(int handle, int docHandle)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_PathStyle_nativeGet(JNIEnv *env, jobject obj, jint handle, jint docHandle) {
	AIArtSet prevSelected = NULL;
	try {
		if (docHandle)
			Document_activate((AIDocumentHandle) docHandle);
		// Don't use PathStyle_init here as there's no map:
		AIPathStyle style;
		bool useCurrent = handle == com_scriptographer_ai_Item_HANDLE_CURRENT_STYLE;
		if (!useCurrent && Item_getType((AIArtHandle) handle) == kGroupArt) {
			// Groups act differently than other objects, so handle seperately,
			// by using Illustrator's capability to merge styles of selected
			// items to one. We need to backup current selection state, select
			// the group, then get current path style, then restore selction...
			// TODO: This is slow, is there no better way?
			prevSelected = Item_getSelected(false);
			Item_deselectAll();
			Item_setSelected((AIArtHandle) handle, true);
			useCurrent = true;
		}
		if (useCurrent) {
			AIPathStyleMap map;
#if kPluginInterfaceVersion >= kAI15
			// TODO: See what advanced stroke parameters are doing in CS5 and
			// decide how to deal with them
			if (sAIPathStyle->GetCurrentPathStyle(&style, &map, NULL))
#else // kPluginInterfaceVersion < kAI15
			if (sAIPathStyle->GetCurrentPathStyle(&style, &map))
#endif // kPluginInterfaceVersion < kAI15
				throw new StringException("Unable to get item style.");
			PathStyle_init(env, obj, &style, &map);
		} else {
			if (sAIPathStyle->GetPathStyle((AIArtHandle) handle, &style))
				throw new StringException("Unable to get item style.");
			PathStyle_init(env, obj, &style, NULL);
		}
	} EXCEPTION_CONVERT(env);
	if (prevSelected != NULL) {
		// Restore previous selection and clean up before bailing out in
		// case of an error.
		Item_deselectAll();
		Item_setSelected(prevSelected);
		sAIArtSet->DisposeArtSet(&prevSelected);
	}
}

/*
 * void nativeSet(int handle, int docHandle,
			com.scriptographer.ai.Color fillColor, boolean hasFillColor, short fillOverprint,
			com.scriptographer.ai.Color strokeColor, boolean hasStrokeColor, short strokeOverprint, float strokeWidth,
			short strokeCap, short strokeJoin, float miterLimit,
			float dashOffset, float[] dashArray,
			short clip, short lockClip, int windingRule, float resolution)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_PathStyle_nativeSet(JNIEnv *env, jobject obj, jint handle, jint docHandle, jobject fillColor, jboolean hasFillColor, jshort fillOverprint, jobject strokeColor, jboolean hasStrokeColor, jshort strokeOverprint, jfloat strokeWidth, jint strokeCap, jint strokeJoin, jfloat miterLimit, jfloat dashOffset, jfloatArray dashArray, jshort clip, jshort lockClip, jint windingRule, jfloat resolution) {
	AIArtSet prevSelected = NULL;
	try {
		if (docHandle)
			Document_activate((AIDocumentHandle) docHandle);
		AIPathStyle style;
		AIPathStyleMap map;
		// Fill with current values as not everything might be set
		// TODO: instead of the path's style, this should be the current default style?
		// because if the user sets a value to undefined, this should fall back to the default value...
		bool useCurrent = handle == com_scriptographer_ai_Item_HANDLE_CURRENT_STYLE;
		if (!useCurrent && Item_getType((AIArtHandle) handle) == kGroupArt) {
			// Groups act differently than other objects, so handle seperately,
			// by using Illustrator's capability to merge styles of selected
			// items to one. We need to backup current selection state, select
			// the group, then get current path style, then restore selction...
			// TODO: This is slow, is there no better way?
			prevSelected = Item_getSelected(false);
			Item_deselectAll();
			Item_setSelected((AIArtHandle) handle, true);
			useCurrent = true;
		}
		if (useCurrent) {
#if kPluginInterfaceVersion >= kAI15
			// TODO: See what advanced stroke parameters are doing in CS5 and
			// decide how to deal with them
			if (sAIPathStyle->GetCurrentPathStyle(&style, &map, NULL))
#else // kPluginInterfaceVersion < kAI15
			if (sAIPathStyle->GetCurrentPathStyle(&style, &map))
#endif // kPluginInterfaceVersion < kAI15
				throw new StringException("Unable to get item style.");
		} else {
			if (sAIPathStyle->GetPathStyle((AIArtHandle) handle, &style))
				throw new StringException("Unable to get item style.");
		}
		PathStyle_convertPathStyle(env, &style, &map,
				fillColor, hasFillColor, fillOverprint,
				strokeColor, hasStrokeColor, strokeOverprint, strokeWidth,
				strokeCap, strokeJoin, miterLimit,
				dashOffset, dashArray,
				clip, lockClip, windingRule, resolution);
		// Now set again
		if (useCurrent) {
#if kPluginInterfaceVersion >= kAI15
			// TODO: See what advanced stroke parameters are doing in CS5 and decide how to deal with them
			if (sAIPathStyle->SetCurrentPathStyle(&style, &map, NULL))
#else // kPluginInterfaceVersion < kAI15
			if (sAIPathStyle->SetCurrentPathStyle(&style, &map))
#endif // kPluginInterfaceVersion < kAI15
				throw new StringException("Unable to set item style.");
		} else {
			if (sAIPathStyle->SetPathStyle((AIArtHandle) handle, &style))
				throw new StringException("Unable to set item style.");
		}
	} EXCEPTION_CONVERT(env);
	if (prevSelected != NULL) {
		// Restore previous selection and clean up before bailing out in
		// case of an error.
		Item_deselectAll();
		Item_setSelected(prevSelected);
		sAIArtSet->DisposeArtSet(&prevSelected);
	}
}

/*
 * void nativeInitStrokeStyle(int handle, com.scriptographer.ai.Color strokeColor, boolean hasStrokeColor, short strokeOverprint, float strokeWidth, short strokeCap, short strokeJoin, float miterLimit, float dashOffset, float[] dashArray)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_PathStyle_nativeInitStrokeStyle(JNIEnv *env, jclass cls, jint handle, jobject strokeColor, jboolean hasStrokeColor, jshort strokeOverprint, jfloat strokeWidth, jint strokeCap, jint strokeJoin, jfloat miterLimit, jfloat dashOffset, jfloatArray dashArray) {
	try {
		PathStyle_convertStrokeStyle(env, (AIStrokeStyle *) handle, NULL,
				strokeColor, hasStrokeColor, strokeOverprint, strokeWidth,
				strokeCap, strokeJoin, miterLimit,
				dashOffset, dashArray);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeInitFillStyle(int handle, com.scriptographer.ai.Color fillColor, boolean hasFillColor, short fillOverprint)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_PathStyle_nativeInitFillStyle(JNIEnv *env, jclass cls, jint handle, jobject fillColor, jboolean hasFillColor, jshort fillOverprint) {
	try {
		PathStyle_convertFillStyle(env, (AIFillStyle *) handle, NULL, fillColor, hasFillColor, fillOverprint);
	} EXCEPTION_CONVERT(env);
}
