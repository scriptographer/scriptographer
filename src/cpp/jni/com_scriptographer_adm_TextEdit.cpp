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
 * $RCSfile: com_scriptographer_adm_TextEdit.cpp,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/03/07 13:42:29 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_adm_TextEdit.h"

/*
 * com.scriptographer.adm.TextEdit
 */

/*
 * void setMaxLength(int arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_TextEdit_setMaxLength(JNIEnv *env, jobject obj, jint length) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetMaxTextLength(item, length);
	} EXCEPTION_CONVERT(env)
}

/*
 * int getMaxLength()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_TextEdit_getMaxLength(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		return sADMItem->GetMaxTextLength(item);
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void setSelection(int start, int end)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_TextEdit_setSelection(JNIEnv *env, jobject obj, jint start, jint end) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetSelectionRange(item, start, end);
	} EXCEPTION_CONVERT(env)
}

/*
 * int[] getSelection()
 */
JNIEXPORT jintArray JNICALL Java_com_scriptographer_adm_TextEdit_getSelection(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		long start, end;
		sADMItem->GetSelectionRange(item, &start, &end);
		// create an int array with these values:
		jintArray res = env->NewIntArray(2);
		jint range[] = {
			start, end
		};
		env->SetIntArrayRegion(res, 0, 2, range);
		return res;
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void selectAll()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_TextEdit_selectAll(JNIEnv *env, jobject obj) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SelectAll(item);
	} EXCEPTION_CONVERT(env)
}

/*
 * void setAllowMath(boolean arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_TextEdit_setAllowMath(JNIEnv *env, jobject obj, jboolean allowMath) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetAllowUnits(item, allowMath);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean geAllowMath()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_TextEdit_getAllowMath(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		return sADMItem->GetAllowMath(item);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setAllowUnits(boolean arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_TextEdit_setAllowUnits(JNIEnv *env, jobject obj, jboolean allowUnits) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetAllowUnits(item, allowUnits);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean getAllowUnits()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_TextEdit_getAllowUnits(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		return sADMItem->GetAllowUnits(item);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}
