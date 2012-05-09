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
#include "com_scriptographer_ai_GradientStopList.h"

/*
 * com.scriptographer.ai.GradientStopList
 */

/*
 * int nativeGetSize(int handle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_GradientStopList_nativeGetSize(JNIEnv *env, jclass cls, jint handle) {
	try {
		// TODO: Does document need activating?
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
		// TODO: Does document need activating?
		for (int i = toIndex - 1; i >= fromIndex; i--) {
			AIGradientStop s;
			sAIGradient->DeleteGradientStop((AIGradientHandle) handle, i, &s);
		}
		short count = 0;
		sAIGradient->GetGradientStopCount((AIGradientHandle) handle, &count);
		return count;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * boolean nativeGet(int handle, int index, com.scriptographer.ai.GradientStop stop)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_GradientStopList_nativeGet(JNIEnv *env, jclass cls, jint handle, jint index, jobject stop) {
	try {
		AIGradientStop s;
		if (sAIGradient->GetNthGradientStop((AIGradientHandle) handle, index, &s))
			throw new StringException("Cannot get gradient stop");
#if kPluginInterfaceVersion < kAI14
		jobject color = gEngine->convertColor(env, &s.color);
#else
		jobject color = gEngine->convertColor(env, &s.color, s.opacity);
		if (s.opacity == -1)
			s.opacity = 1;
#endif
		gEngine->callVoidMethod(env, stop, gEngine->mid_ai_GradientStop_set, s.midPoint, s.rampPoint, color);
		return true;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean nativeSet(int handle, int docHandle, int index, double midPoint, double rampPoint, float[] color)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_GradientStopList_nativeSet(JNIEnv *env, jclass cls, jint handle, jint docHandle, jint index, jdouble midPoint, jdouble rampPoint, jfloatArray color) {
	try {
		AIGradientStop s;
#if kPluginInterfaceVersion < kAI14
		gEngine->convertColor(env, color, &s.color);
#else
		gEngine->convertColor(env, color, &s.color, &s.opacity);
		if (s.opacity == -1)
			s.opacity = 1;
#endif
		s.rampPoint = rampPoint;
		s.midPoint = midPoint;
		gEngine->convertColor(env, color, &s.color);
		if (sAIGradient->SetNthGradientStop((AIGradientHandle) handle, index, &s))
			throw new StringException("Cannot set gradient stop");
		return true;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean nativeInsert(int handle, int docHandle, int index, double midPoint, double rampPoint, float[] color)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_GradientStopList_nativeInsert(JNIEnv *env, jclass cls, jint handle, jint docHandle, jint index, jdouble midPoint, jdouble rampPoint, jfloatArray color) {
	try {
		AIGradientStop s;
#if kPluginInterfaceVersion < kAI14
		gEngine->convertColor(env, color, &s.color);
#else
		gEngine->convertColor(env, color, &s.color, &s.opacity);
		if (s.opacity == -1)
			s.opacity = 1;
#endif
		s.rampPoint = rampPoint;
		s.midPoint = midPoint;
		if (sAIGradient->InsertGradientStop((AIGradientHandle) handle, index, &s))
			throw new StringException("Cannot insert gradient stop");
		return true;
	} EXCEPTION_CONVERT(env);
	return false;
}
