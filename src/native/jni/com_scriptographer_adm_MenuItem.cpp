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
#include "ScriptographerPlugin.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_adm_MenuItem.h"

/*
 * com.scriptographer.ai.MenuItem
 */

/*
 * int nativeCreate(String name, String text, String group, int options)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_MenuItem_nativeCreate(JNIEnv *env, jclass cls, jstring name, jstring text, jstring group, jint options) {
	try {
		AIMenuItemHandle menuItem = NULL;
		char *nameStr = gEngine->convertString(env, name);

#if kPluginInterfaceVersion < kAI12
		AIPlatformAddMenuItemData data;
		data.groupName = gEngine->convertString(env, group);
		data.itemText = gEngine->convertString_Pascal(env, text);
		sAIMenu->AddMenuItem(gPlugin->getPluginRef(), nameStr, &data, options, &menuItem);
		delete data.itemText;
#else
		AIPlatformAddMenuItemDataUS data;
		data.groupName = gEngine->convertString(env, group);
		data.itemText = gEngine->convertString_UnicodeString(env, text);
		sAIMenu->AddMenuItem(gPlugin->getPluginRef(), nameStr, &data, options, &menuItem);
#endif
		delete data.groupName;
		delete nameStr;
		return (jint) menuItem;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeRemove(int itemHandle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_MenuItem_nativeRemove(JNIEnv *env, jclass cls, jint itemHandle) {
	try {
		sAIMenu->RemoveMenuItem((AIMenuItemHandle) itemHandle);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * java.lang.String getText()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_adm_MenuItem_getText(JNIEnv *env, jobject obj) {
	try {
		AIMenuItemHandle item = gEngine->getMenuItemHandle(env, obj);

#if kPluginInterfaceVersion < kAI12
		char text[256];
#else
		ai::UnicodeString text;
#endif
		if (!sAIMenu->GetItemText(item, text))
			return gEngine->convertString(env, text);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setText(java.lang.String text)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_MenuItem_setText(JNIEnv *env, jobject obj, jstring text) {
	try {
		AIMenuItemHandle item = gEngine->getMenuItemHandle(env, obj);
#if kPluginInterfaceVersion < kAI12
		char *textStr = gEngine->convertString(env, text);
		sAIMenu->SetItemText(item, textStr);
		delete textStr;
#else
		ai::UnicodeString textStr = gEngine->convertString_UnicodeString(env, text);
		sAIMenu->SetItemText(item, textStr);
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * void setOptions(int options)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_MenuItem_setOptions(JNIEnv *env, jobject obj, jint options) {
	try {
		AIMenuItemHandle item = gEngine->getMenuItemHandle(env, obj);
		sAIMenu->SetMenuItemOptions(item, options);
	} EXCEPTION_CONVERT(env);
}

/*
 * int getOptions()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_MenuItem_getOptions(JNIEnv *env, jobject obj) {
	try {
		AIMenuItemHandle item = gEngine->getMenuItemHandle(env, obj);
		long options = 0;
		sAIMenu->GetMenuItemOptions(item, &options);
		return options;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void setEnabled(boolean enabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_MenuItem_setEnabled(JNIEnv *env, jobject obj, jboolean enabled) {
	try {
		AIMenuItemHandle item = gEngine->getMenuItemHandle(env, obj);
		if (enabled) {
			sAIMenu->EnableItem(item);
		} else {
			sAIMenu->DisableItem(item);
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isEnabled()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_MenuItem_isEnabled(JNIEnv *env, jobject obj) {
	try {
		AIMenuItemHandle item = gEngine->getMenuItemHandle(env, obj);
		ASBoolean enabled = false;
		sAIMenu->IsItemEnabled(item, &enabled);
		return enabled;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setChecked(boolean checked)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_MenuItem_setChecked(JNIEnv *env, jobject obj, jboolean checked) {
	try {
		AIMenuItemHandle item = gEngine->getMenuItemHandle(env, obj);
		sAIMenu->CheckItem(item, checked);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isChecked()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_MenuItem_isChecked(JNIEnv *env, jobject obj) {
	try {
		AIMenuItemHandle item = gEngine->getMenuItemHandle(env, obj);
		ASBoolean checked = false;
		sAIMenu->IsItemChecked(item, &checked);
		return checked;
	} EXCEPTION_CONVERT(env);
	return false;
}
