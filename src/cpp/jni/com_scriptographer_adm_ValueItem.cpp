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
 * $RCSfile: com_scriptographer_adm_ValueItem.cpp,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:59 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_adm_ValueItem.h"

/*
 * com.scriptographer.adm.ValueItem
 */

/*
 * float[] getValueRange()
 */
JNIEXPORT jfloatArray JNICALL Java_com_scriptographer_adm_ValueItem_getValueRange(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		// create a float array with these values:
		jfloatArray res = env->NewFloatArray(2);
		jfloat range[] = {
			sADMItem->GetMinFloatValue(item), sADMItem->GetMaxFloatValue(item)
		};
		env->SetFloatArrayRegion(res, 0, 2, range);
		return res;
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void setValueRange(float arg1, float arg2)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ValueItem_setValueRange(JNIEnv *env, jobject obj, jfloat min, jfloat max) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetMinFloatValue(item, min);
		sADMItem->SetMaxFloatValue(item, max);
	} EXCEPTION_CONVERT(env)
}

/*
 * int getPrecision()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_ValueItem_getPrecision(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		return sADMItem->GetPrecision(item);
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void setPrecision(int arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ValueItem_setPrecision(JNIEnv *env, jobject obj, jint precision) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetPrecision(item, precision);
	} EXCEPTION_CONVERT(env)
}


/*
 * boolean getBooleanValue()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_ValueItem_getBooleanValue(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		return sADMItem->GetBooleanValue(item);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setBooleanValue(boolean arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ValueItem_setBooleanValue(JNIEnv *env, jobject obj, jboolean value) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetBooleanValue(item, value);
	} EXCEPTION_CONVERT(env)
}

/*
 * float getFloatValue()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_adm_ValueItem_getFloatValue(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		return sADMItem->GetFloatValue(item);
	} EXCEPTION_CONVERT(env)
	return 0.0;
}

/*
 * void setFloatValue(float arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ValueItem_setFloatValue(JNIEnv *env, jobject obj, jfloat value) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetFloatValue(item, value);
	} EXCEPTION_CONVERT(env)
}
