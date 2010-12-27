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

#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ui_MenuGroup.h"

#define NAME_PREFIX "Scriptographer "

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

/*
 * java.util.ArrayList nativeGetGroups()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_MenuGroup_nativeGetGroups(JNIEnv *env, jclass cls) {
	try {
		jobject array = gEngine->newObject(env, gEngine->cls_ArrayList, gEngine->cid_ArrayList);
#if kPluginInterfaceVersion < kAI15
		long count;
#else // kPluginInterfaceVersion >= kAI15
		ai::int32 count;
#endif // kPluginInterfaceVersion >= kAI15
		sAIMenu->CountMenuGroups(&count);
		for (int i = 0; i < count; i++) {
			AIMenuGroup group;
			sAIMenu->GetNthMenuGroup(i, &group);
#if kPluginInterfaceVersion < kAI12
			char *name;
#else // kPluginInterfaceVersion >= kAI12
			const char *name;
#endif // kPluginInterfaceVersion >= kAI12
			sAIMenu->GetMenuGroupName(group, &name);
			// See wether it starts with Scriptographer :
			if (strstr(name, NAME_PREFIX) == name) {
				// Create the wrapper
				jobject groupObj = gEngine->wrapMenuGroupHandle(env, group);
				// And add it to the array
				gEngine->callObjectMethod(env, array, gEngine->mid_Collection_add, groupObj);
			}
		}
		return array;
	} EXCEPTION_CONVERT(env);
	return NULL;
}
