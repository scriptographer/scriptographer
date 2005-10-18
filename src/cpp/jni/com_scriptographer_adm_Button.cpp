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
 * $RCSfile: com_scriptographer_adm_Button.cpp,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/10/18 15:35:46 $
 */

#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_adm_Button.h"

/*
 * com.scriptographer.adm.Button
 */

/*
 * void nativeSetPicture(int iconRef)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Button_nativeSetPicture(JNIEnv *env, jobject obj, jint iconRef) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetPicture(item, (ADMIconRef)iconRef);
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeSetRolloverPicture(int iconRef)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Button_nativeSetRolloverPicture(JNIEnv *env, jobject obj, jint iconRef) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetRolloverPicture(item, (ADMIconRef)iconRef);
		sADMItem->SetHasRollOverProperty(item, iconRef != 0); 
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeSetSelectedPicture(int iconRef)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Button_nativeSetSelectedPicture(JNIEnv *env, jobject obj, jint iconRef) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetSelectedPicture(item, (ADMIconRef)iconRef);
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeSetDisabledPicture(int iconRef)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Button_nativeSetDisabledPicture(JNIEnv *env, jobject obj, jint iconRef) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetDisabledPicture(item, (ADMIconRef)iconRef);
	} EXCEPTION_CONVERT(env)
}
