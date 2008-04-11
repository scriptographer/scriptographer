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
#include "com_scriptographer_ai_Gradient.h"

/*
 * com.scriptographer.ai.Gradient
 */

/*
 * int nativeCreate()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Gradient_nativeCreate(JNIEnv *env, jclass cls) {
	try {
		AIGradientHandle gradient = NULL;
		sAIGradient->NewGradient(&gradient);
		return (jint) gradient;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * java.lang.String getName()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_Gradient_getName(JNIEnv *env, jobject obj) {
	try {
		AIGradientHandle gradient = gEngine->getGradientHandle(env, obj);
#if kPluginInterfaceVersion < kAI12
		unsigned char name[256];
		if (!sAIGradient->GetGradientName(gradient, name)) {
#else
		ai::UnicodeString name;
		if (!sAIGradient->GetGradientName(gradient, name)) {
#endif
			return gEngine->convertString(env, name);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setName(java.lang.String name)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Gradient_setName(JNIEnv *env, jobject obj, jstring name) {
	try {
		AIGradientHandle gradient = gEngine->getGradientHandle(env, obj, true);
#if kPluginInterfaceVersion < kAI12
		unsigned char *str = gEngine->convertString_Pascal(env, name);
		sAIGradient->SetGradientName(gradient, str);
		delete str;
#else
		ai::UnicodeString str = gEngine->convertString_UnicodeString(env, name);
		sAIGradient->SetGradientName(gradient, str);
#endif
	} EXCEPTION_CONVERT(env);
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
