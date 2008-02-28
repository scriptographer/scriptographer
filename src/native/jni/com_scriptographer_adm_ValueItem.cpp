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
#include "com_scriptographer_adm_ValueItem.h"

/*
 * com.scriptographer.adm.ValueItem
 */

/*
 * float[] getRange()
 */
JNIEXPORT jfloatArray JNICALL Java_com_scriptographer_adm_ValueItem_getRange(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
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
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetMinFloatValue(item, min);
		sADMItem->SetMaxFloatValue(item, max);
	} EXCEPTION_CONVERT(env);
}

/*
 * float[] getIncrements()
 */
JNIEXPORT jfloatArray JNICALL Java_com_scriptographer_adm_ValueItem_getIncrements(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
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
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetSmallIncrement(item, smallInc);
		sADMItem->SetLargeIncrement(item, largeInc);
	} EXCEPTION_CONVERT(env);
}

/*
 * float getValue()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_adm_ValueItem_getValue(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		return sADMItem->GetFloatValue(item);
	} EXCEPTION_CONVERT(env);
	return 0.0;
}

/*
 * void setValue(float value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ValueItem_setValue(JNIEnv *env, jobject obj, jfloat value) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetFloatValue(item, value);
	} EXCEPTION_CONVERT(env);
}
