/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 */

#include "stdHeaders.h"
#include "ScriptographerPlugin.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ui_MenuItem.h"

/*
 * com.scriptographer.ai.MenuItem
 */

/*
 * int nativeCreate(String name, String text, String group, int options)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ui_MenuItem_nativeCreate(JNIEnv *env, jclass cls, jstring name, jstring text, jstring group, jint options) {
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
JNIEXPORT jint JNICALL Java_com_scriptographer_ui_MenuItem_nativeRemove(JNIEnv *env, jclass cls, jint itemHandle) {
	try {
		sAIMenu->RemoveMenuItem((AIMenuItemHandle) itemHandle);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * java.lang.String getText()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ui_MenuItem_getText(JNIEnv *env, jobject obj) {
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
JNIEXPORT void JNICALL Java_com_scriptographer_ui_MenuItem_setText(JNIEnv *env, jobject obj, jstring text) {
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
JNIEXPORT void JNICALL Java_com_scriptographer_ui_MenuItem_setOptions(JNIEnv *env, jobject obj, jint options) {
	try {
		AIMenuItemHandle item = gEngine->getMenuItemHandle(env, obj);
		sAIMenu->SetMenuItemOptions(item, options);
	} EXCEPTION_CONVERT(env);
}

/*
 * int getOptions()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ui_MenuItem_getOptions(JNIEnv *env, jobject obj) {
	try {
		AIMenuItemHandle item = gEngine->getMenuItemHandle(env, obj);
		ai::int32 options = 0;
		sAIMenu->GetMenuItemOptions(item, &options);
		return options;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void setEnabled(boolean enabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_MenuItem_setEnabled(JNIEnv *env, jobject obj, jboolean enabled) {
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
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ui_MenuItem_isEnabled(JNIEnv *env, jobject obj) {
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
JNIEXPORT void JNICALL Java_com_scriptographer_ui_MenuItem_setChecked(JNIEnv *env, jobject obj, jboolean checked) {
	try {
		AIMenuItemHandle item = gEngine->getMenuItemHandle(env, obj);
		sAIMenu->CheckItem(item, checked);

	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isChecked()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ui_MenuItem_isChecked(JNIEnv *env, jobject obj) {
	try {
		AIMenuItemHandle item = gEngine->getMenuItemHandle(env, obj);
		ASBoolean checked = false;
		sAIMenu->IsItemChecked(item, &checked);
		return checked;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean setCommand(java.lang.String key, int arg2)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ui_MenuItem_setCommand(JNIEnv *env, jobject obj, jstring key, jint modifiers) {
	try {
		AIMenuItemHandle item = gEngine->getMenuItemHandle(env, obj);
		char *chars = gEngine->convertString(env, key);
		int len = strlen(chars);
		if (len == 1) {
			return !sAIMenu->SetItemCmd(item, chars[0], modifiers);
		} else if (len >= 2 && (chars[0] == 'f' || chars[0] == 'F')) {
			return !sAIMenu->SetItemFunctionKey(item, strtol(&chars[1], NULL, 10), modifiers);
		}
		delete chars;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * java.util.ArrayList nativeGetItems()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_MenuItem_nativeGetItems(JNIEnv *env, jclass cls) {
	try {
		jobject array = gEngine->newObject(env, gEngine->cls_ArrayList, gEngine->cid_ArrayList);
#if kPluginInterfaceVersion < kAI15
		long count;
#else // kPluginInterfaceVersion >= kAI15
		ai::int32 count;
#endif // kPluginInterfaceVersion >= kAI15
		sAIMenu->CountMenuItems(&count);
		SPPluginRef plugin = gPlugin->getPluginRef();
		for (int i = 0; i < count; i++) {
			AIMenuItemHandle item;
			SPPluginRef itemPlugin;
			if (!sAIMenu->GetNthMenuItem(i, &item)
					&& !sAIMenu->GetMenuItemPlugin(item, &itemPlugin)
					&& plugin == itemPlugin) {
				// Create the wrapper
				jobject itemObj = gEngine->wrapMenuItemHandle(env, item);
				// And add it to the array
				gEngine->callObjectMethod(env, array, gEngine->mid_Collection_add, itemObj);
			}
		}
		return array;
	} EXCEPTION_CONVERT(env);
	return NULL;
}
