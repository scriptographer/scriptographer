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
 * $RCSfile: com_scriptographer_adm_ScrollBar.cpp,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:59 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_adm_ScrollBar.h"

/*
 * com.scriptographer.adm.ScrollBar
 */

/*
 * float[] getIncrements()
 */
JNIEXPORT jfloatArray JNICALL Java_com_scriptographer_adm_ScrollBar_getIncrements(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		// create a float array with these values:
		jfloatArray res = env->NewFloatArray(2);
		jfloat range[] = {
			sADMItem->GetSmallIncrement(item), sADMItem->GetLargeIncrement(item)
		};
		env->SetFloatArrayRegion(res, 0, 2, range);
		return res;
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void setIncrements(float arg1, float arg2)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ScrollBar_setIncrements(JNIEnv *env, jobject obj, jfloat small, jfloat large) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetSmallIncrement(item, small);
		sADMItem->SetLargeIncrement(item, large);
	} EXCEPTION_CONVERT(env)
}
