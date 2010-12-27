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
#include "com_scriptographer_adm_Button.h"

/*
 * com.scriptographer.adm.Button
 */

/*
 * void nativeSetImage(int iconRef)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Button_nativeSetImage(JNIEnv *env, jobject obj, jint iconRef) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		sADMItem->SetPicture(item, (ADMIconRef)iconRef);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetRolloverImage(int iconRef)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Button_nativeSetRolloverImage(JNIEnv *env, jobject obj, jint iconRef) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		sADMItem->SetRolloverPicture(item, (ADMIconRef)iconRef);
		sADMItem->SetHasRollOverProperty(item, iconRef != 0); 
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetSelectedImage(int iconRef)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Button_nativeSetSelectedImage(JNIEnv *env, jobject obj, jint iconRef) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		sADMItem->SetSelectedPicture(item, (ADMIconRef)iconRef);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetDisabledImage(int iconRef)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Button_nativeSetDisabledImage(JNIEnv *env, jobject obj, jint iconRef) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		sADMItem->SetDisabledPicture(item, (ADMIconRef)iconRef);
	} EXCEPTION_CONVERT(env);
}
