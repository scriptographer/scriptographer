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
#include "com_scriptographer_ui_MenuGroup.h"

/*
 * com.scriptographer.ai.MenuGroup
 */

/*
 * int nativeCreate(String name, String nearGroup, int parentItemHandle, int options)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ui_MenuGroup_nativeCreate(JNIEnv *env, jclass cls, jstring name, jstring nearGroup, jint parentItemHandle, jint options) {
	try {
		char *nameStr = gEngine->convertString(env, name);
		AIMenuGroup group = NULL;
		if (nearGroup != NULL) {
			char *nearGroupStr = gEngine->convertString(env, nearGroup);
			sAIMenu->AddMenuGroup(nameStr, options, nearGroupStr, &group);
			delete nearGroupStr;
		} else if (parentItemHandle != 0) {
			sAIMenu->AddMenuGroupAsSubMenu(nameStr, options, (AIMenuItemHandle) parentItemHandle, &group);
		}
		delete nameStr;
		return (jint) group;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void setOptions(int options)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_MenuGroup_setOptions(JNIEnv *env, jobject obj, jint options) {
	try {
		AIMenuGroup group = gEngine->getMenuGroupHandle(env, obj);
		sAIMenu->SetMenuGroupOptions(group, options);
	} EXCEPTION_CONVERT(env);
}

/*
 * int getOptions()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ui_MenuGroup_getOptions(JNIEnv *env, jobject obj) {
	try {
		AIMenuGroup group = gEngine->getMenuGroupHandle(env, obj);
		long options = 0;
		sAIMenu->GetMenuGroupOptions(group, &options);
		return options;
	} EXCEPTION_CONVERT(env);
	return 0;
}
