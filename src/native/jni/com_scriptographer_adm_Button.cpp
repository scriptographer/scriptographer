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
