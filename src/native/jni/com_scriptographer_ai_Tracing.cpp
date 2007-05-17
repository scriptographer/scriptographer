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
#include "com_scriptographer_ai_Tracing.h"

/*
 * com.scriptographer.ai.Tracing
 */

#define TRACING_EXCEPTION \
		throw new StringException("Tracing is only supported on CS2 and above.");

// Use inline functions outside the macros because #if is not possible within macros

inline AIDictionaryRef Tracing_getOptions(AIArtHandle art) {
#if kPluginInterfaceVersion >= kAI12
	AIDictionaryRef dict = NULL;
	sAITracing->AcquireTracingOptions(art, &dict);
	return dict;
#else
	TRACING_EXCEPTION
#endif
}

inline AIDictionaryRef Tracing_getStatistics(AIArtHandle art) {
#if kPluginInterfaceVersion >= kAI12
	AIDictionaryRef dict = NULL;
	sAITracing->AcquireTracingOptions(art, &dict);
	return dict;
#else
	TRACING_EXCEPTION
#endif
}

#define TRACING_OPERATION(DICT, OPERATION, KEY, TYPE, VALUE) \
	AIArtHandle art = gEngine->getArtHandle(env, obj); \
	AIDictionaryRef dict = Tracing_get##DICT(art); \
	sAIDictionary->OPERATION##TYPE##Entry(dict, sAIDictionary->Key(KEY), VALUE); \
	sAIDictionary->Release(dict);

#define TRACING_GET_OPTION(KEY, TYPE, VALUE) \
	TRACING_OPERATION(Options, Get, KEY, TYPE, VALUE);

#define TRACING_SET_OPTION(KEY, TYPE, VALUE) \
	TRACING_OPERATION(Options, Set, KEY, TYPE, VALUE); \
	gEngine->callVoidMethod(env, obj, gEngine->mid_ai_Tracing_markDirty);

/*
 * int nativeCreate(int docHandle, int artHandle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Tracing_nativeCreate(JNIEnv *env, jclass cls, jint docHandle, jint artHandle) {
	try {
#if kPluginInterfaceVersion >= kAI12
		AIArtHandle tracing = NULL;
		Document_activate((AIDocumentHandle) docHandle);
		// we just pass the image itself for prep. TODO: find out what we're supposed to pass here!
		sAITracing->CreateTracing(kPlaceAbove, (AIArtHandle) artHandle, (AIArtHandle) artHandle, &tracing);
		return (jint) tracing;
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void update()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tracing_update(JNIEnv *env, jobject obj) {
	try {
#if kPluginInterfaceVersion >= kAI12
	    AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		sAITracing->Update(art);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean getResample()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Tracing_getResample(JNIEnv *env, jobject obj) {
	ASBoolean value = false;
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_GET_OPTION(kTracingResampleKey, Boolean, &value);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
	return value;
}

/*
 * void setResample(boolean resample)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tracing_setResample(JNIEnv *env, jobject obj, jboolean resample) {
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_SET_OPTION(kTracingResampleKey, Boolean, resample);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * float getResampleResolution()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_Tracing_getResampleResolution(JNIEnv *env, jobject obj) {
	ASReal resolution = 0.0;
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_GET_OPTION(kTracingResampleResolutionKey, Real, &resolution);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
	return resolution;
}

/*
 * void setResampleResolution(float resolution)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tracing_setResampleResolution(JNIEnv *env, jobject obj, jfloat resolution) {
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_SET_OPTION(kTracingResampleResolutionKey, Real, resolution);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * int getMode()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Tracing_getMode(JNIEnv *env, jobject obj) {
	ASInt32 mode = 0;
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_GET_OPTION(kTracingModeKey, Integer, &mode);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
	return mode;
}

/*
 * void setMode(int mode)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tracing_setMode(JNIEnv *env, jobject obj, jint mode) {
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_SET_OPTION(kTracingModeKey, Integer, mode);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * float getBlur()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_Tracing_getBlur(JNIEnv *env, jobject obj) {
	ASReal blur = 0.0;
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_GET_OPTION(kTracingBlurKey, Real, &blur);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
	return blur;
}

/*
 * void setBlur(float blur)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tracing_setBlur(JNIEnv *env, jobject obj, jfloat blur) {
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_SET_OPTION(kTracingBlurKey, Real, blur);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * int getThreshold()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Tracing_getThreshold(JNIEnv *env, jobject obj) {
	ASInt32 threshold = 0;
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_GET_OPTION(kTracingThresholdKey, Integer, &threshold);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
	return threshold;
}

/*
 * void setThreshold(int threshold)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tracing_setThreshold(JNIEnv *env, jobject obj, jint threshold) {
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_SET_OPTION(kTracingThresholdKey, Integer, threshold);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * int getMaxColors()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Tracing_getMaxColors(JNIEnv *env, jobject obj) {
	ASInt32 maxColors = 0;
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_GET_OPTION(kTracingMaxColorsKey, Integer, &maxColors);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
	return maxColors;
}

/*
 * void setMaxColors(int maxColors)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tracing_setMaxColors(JNIEnv *env, jobject obj, jint maxColors) {
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_SET_OPTION(kTracingMaxColorsKey, Integer, maxColors);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean getFills()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Tracing_getFills(JNIEnv *env, jobject obj) {
	ASBoolean fills = false;
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_GET_OPTION(kTracingFillsKey, Boolean, &fills);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
	return fills;
}

/*
 * void setFills(boolean fills)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tracing_setFills(JNIEnv *env, jobject obj, jboolean fills) {
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_SET_OPTION(kTracingFillsKey, Boolean, fills);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean getStrokes()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Tracing_getStrokes(JNIEnv *env, jobject obj) {
	ASBoolean strokes = false;
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_GET_OPTION(kTracingStrokesKey, Boolean, &strokes);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
	return strokes;
}

/*
 * void setStrokes(boolean strokes)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tracing_setStrokes(JNIEnv *env, jobject obj, jboolean strokes) {
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_SET_OPTION(kTracingStrokesKey, Boolean, strokes);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * float getMaxStrokeWeight()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_Tracing_getMaxStrokeWeight(JNIEnv *env, jobject obj) {
	ASReal maxWeight = 0.0;
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_GET_OPTION(kTracingMaxStrokeWeightKey, Real, &maxWeight);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
	return maxWeight;
}

/*
 * void setMaxStrokeWeight(float maxWeight)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tracing_setMaxStrokeWeight(JNIEnv *env, jobject obj, jfloat maxWeight) {
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_SET_OPTION(kTracingMaxStrokeWeightKey, Real, maxWeight);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * float getMinStrokeLength()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_Tracing_getMinStrokeLength(JNIEnv *env, jobject obj) {
	ASReal minLength = 0.0;
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_GET_OPTION(kTracingMinStrokeLengthKey, Real, &minLength);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
	return minLength;
}

/*
 * void setMinStrokeLength(float minLength)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tracing_setMinStrokeLength(JNIEnv *env, jobject obj, jfloat minLength) {
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_SET_OPTION(kTracingMinStrokeLengthKey, Real, minLength);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * float getPathTightness()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_Tracing_getPathTightness(JNIEnv *env, jobject obj) {
	ASReal tightness = 0.0;
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_GET_OPTION(kTracingPathTightnessKey, Real, &tightness);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
	return tightness;
}

/*
 * void setPathTightness(float tightness)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tracing_setPathTightness(JNIEnv *env, jobject obj, jfloat tightness) {
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_SET_OPTION(kTracingPathTightnessKey, Real, tightness);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * float getCornerAngle()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_Tracing_getCornerAngle(JNIEnv *env, jobject obj) {
	ASReal angle = 0.0;
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_GET_OPTION(kTracingCornerAngleKey, Real, &angle);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
	return angle;
}

/*
 * void setCornerAngle(float angle)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tracing_setCornerAngle(JNIEnv *env, jobject obj, jfloat angle) {
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_SET_OPTION(kTracingCornerAngleKey, Real, angle);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * int getMinArea()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Tracing_getMinArea(JNIEnv *env, jobject obj) {
	ASInt32 minArea = 0;
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_GET_OPTION(kTracingMinimumAreaKey, Integer, &minArea);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
	return minArea;
}

/*
 * void setMinArea(int minArea)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tracing_setMinArea(JNIEnv *env, jobject obj, jint minArea) {
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_SET_OPTION(kTracingMinimumAreaKey, Integer, minArea);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * int getVectorDisplay()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Tracing_getVectorDisplay(JNIEnv *env, jobject obj) {
	ASInt32 display = 0;
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_GET_OPTION(kTracingVisualizeVectorKey, Integer, &display);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
	return display;
}

/*
 * void setVectorDisplay(int display)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tracing_setVectorDisplay(JNIEnv *env, jobject obj, jint display) {
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_SET_OPTION(kTracingVisualizeVectorKey, Integer, display);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * int getRasterDisplay()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Tracing_getRasterDisplay(JNIEnv *env, jobject obj) {
	ASInt32 display = 0;
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_GET_OPTION(kTracingVisualizeRasterKey, Integer, &display);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
	return display;
}

/*
 * void setRasterDisplay(int display)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tracing_setRasterDisplay(JNIEnv *env, jobject obj, jint display) {
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_SET_OPTION(kTracingVisualizeRasterKey, Integer, display);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean getLivePaint()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Tracing_getLivePaint(JNIEnv *env, jobject obj) {
	ASBoolean livePaint = 0;
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_GET_OPTION(kTracingLivePaintKey, Boolean, &livePaint);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
	return livePaint;
}

/*
 * void setLivePaint(boolean livePaint)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tracing_setLivePaint(JNIEnv *env, jobject obj, jboolean livePaint) {
	try {
#if kPluginInterfaceVersion >= kAI12
		TRACING_SET_OPTION(kTracingLivePaintKey, Boolean, livePaint);
#else
		TRACING_EXCEPTION
#endif
	} EXCEPTION_CONVERT(env);
}
