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
#include "com_scriptographer_adm_ToggleItem.h"

/*
 * com.scriptographer.adm.ToggleItem
 */

/*
 * boolean isChecked()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_ToggleItem_isChecked(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		return sADMItem->GetBooleanValue(item);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setChecked(boolean checked)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ToggleItem_setChecked(JNIEnv *env, jobject obj, jboolean checked) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetBooleanValue(item, checked);
	} EXCEPTION_CONVERT(env);
}
