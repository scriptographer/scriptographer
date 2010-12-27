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

#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Gradient.h"

/*
 * com.scriptographer.ai.Gradient
 */

/*
 * int nativeCreate()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Gradient_nativeCreate(JNIEnv *env, jclass cls) {
	try {
		// Make sure we're switching to the right doc (gCreationDoc)
		Document_activate();
		AIGradientHandle gradient = NULL;
		sAIGradient->NewGradient(&gradient);
		return (jint) gradient;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeGetType()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Gradient_nativeGetType(JNIEnv *env, jobject obj) {
	try {
		AIGradientHandle gradient = gEngine->getGradientHandle(env, obj);
		short type = 0;
		sAIGradient->GetGradientType(gradient, &type);
		return type;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void setType(int type)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Gradient_nativeSetType(JNIEnv *env, jobject obj, jint type) {
	try {
		AIGradientHandle gradient = gEngine->getGradientHandle(env, obj, true);
		sAIGradient->SetGradientType(gradient, type);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isValid()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Gradient_isValid(JNIEnv *env, jobject obj) {
	try {
		AIGradientHandle gradient = gEngine->getGradientHandle(env, obj, true);
		return sAIGradient->ValidateGradient(gradient);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean nativeRemove()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Gradient_nativeRemove(JNIEnv *env, jobject obj) {
	try {
		AIGradientHandle gradient = gEngine->getGradientHandle(env, obj, true);
		return !sAIGradient->DeleteGradient(gradient);
	} EXCEPTION_CONVERT(env);
	return false;
}
