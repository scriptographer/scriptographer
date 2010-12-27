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
#include "com_scriptographer_adm_ValueItem.h"

/*
 * com.scriptographer.adm.ValueItem
 */

/*
 * float[] getRange()
 */
JNIEXPORT jfloatArray JNICALL Java_com_scriptographer_adm_ValueItem_getRange(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		// create a float array with these values:
		jfloatArray res = env->NewFloatArray(2);
		jfloat range[] = {
			sADMItem->GetMinFloatValue(item), sADMItem->GetMaxFloatValue(item)
		};
		env->SetFloatArrayRegion(res, 0, 2, range);
		return res;
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setRange(float min, float max)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ValueItem_setRange(JNIEnv *env, jobject obj, jfloat min, jfloat max) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		// ADM seems to have 25 bit resolution... Weird!
		if (min < -16777215)
			min = -16777215;
		if (max > 16777216)
			max = 16777216;
		sADMItem->SetMinFloatValue(item, min);
		sADMItem->SetMaxFloatValue(item, max);
	} EXCEPTION_CONVERT(env);
}

/*
 * float[] getIncrements()
 */
JNIEXPORT jfloatArray JNICALL Java_com_scriptographer_adm_ValueItem_getIncrements(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		// create a float array with these values:
		jfloatArray res = env->NewFloatArray(2);
		jfloat range[] = {
			sADMItem->GetSmallIncrement(item), sADMItem->GetLargeIncrement(item)
		};
		env->SetFloatArrayRegion(res, 0, 2, range);
		return res;
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setIncrements(float smallInc, float largeInc)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ValueItem_setIncrements(JNIEnv *env, jobject obj, jfloat smallInc, jfloat largeInc) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		sADMItem->SetSmallIncrement(item, smallInc);
		sADMItem->SetLargeIncrement(item, largeInc);
	} EXCEPTION_CONVERT(env);
}

/*
 * float getValue()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_adm_ValueItem_getValue(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		return sADMItem->GetFloatValue(item);
	} EXCEPTION_CONVERT(env);
	return 0.0;
}

/*
 * void setValue(float value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ValueItem_setValue(JNIEnv *env, jobject obj, jfloat value) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		sADMItem->SetFloatValue(item, value);
	} EXCEPTION_CONVERT(env);
}
