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
#include "com_scriptographer_ui_ItemGroup.h"

/*
 * com.scriptographer.ui.ItemGroup
 */

/*
 * void nativeAdd(com.scriptographer.ui.Item item)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_ItemGroup_nativeAdd(JNIEnv *env, jobject obj, jobject item) {
	try {
		ADMItemRef itemRef = gEngine->getItemHandle(env, obj);
		ADMItemRef subItemRef = gEngine->getItemHandle(env, item);
		sADMItem->AddItem(itemRef, subItemRef);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeRemove(com.scriptographer.ui.Item item)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_ItemGroup_nativeRemove(JNIEnv *env, jobject obj, jobject item) {
	try {
		ADMItemRef itemRef = gEngine->getItemHandle(env, obj);
		ADMItemRef subItemRef = gEngine->getItemHandle(env, item);
		sADMItem->RemoveItem(itemRef, subItemRef);
	} EXCEPTION_CONVERT(env);
}
