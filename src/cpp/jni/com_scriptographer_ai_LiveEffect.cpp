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
 * $RCSfile: com_scriptographer_ai_LiveEffect.cpp,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:59 $
 */

#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "Plugin.h"
#include "com_scriptographer_ai_LiveEffect.h"

/*
 * com.scriptographer.ai.LiveEffect
 */

/*
 * int nativeCreate(java.lang.String name, String title, int preferedInput, int type, int flags, int majorVersion, int minorVersion)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_LiveEffect_nativeCreate(JNIEnv *env, jobject obj, jstring name, jstring title, jint preferedInput, jint type, jint flags, jint majorVersion, jint minorVersion) {
	AILiveEffectHandle liveEffectHandle = NULL;
	try {
		AILiveEffectData effectInfo;
		effectInfo.self = gPlugin->getPluginRef();
		effectInfo.name = gEngine->createCString(env, name);
		effectInfo.title = gEngine->createCString(env, title);
		effectInfo.majorVersion = majorVersion;
		effectInfo.minorVersion = minorVersion;
		effectInfo.prefersAsInput = preferedInput;
		effectInfo.styleFilterFlags = type | flags;
		sAILiveEffect->AddLiveEffect(&effectInfo, &liveEffectHandle);
		delete effectInfo.name;
		delete effectInfo.title;
	} EXCEPTION_CONVERT(env)
	return (jint) liveEffectHandle;
}

/*
 * com.scriptographer.ai.MenuItem nativeAddMenuItem(String name, String category, String title)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_LiveEffect_nativeAddMenuItem(JNIEnv *env, jobject obj, jstring name, jstring category, jstring title) {
	try {
		AILiveEffectHandle effect = gEngine->getLiveEffectHandle(env, obj);
	
		AddLiveEffectMenuData menuData;
		menuData.category = gEngine->createCString(env, category);
		menuData.title = gEngine->createCString(env, title);
		menuData.options = 0;

		AIMenuItemHandle menuItem = NULL;
		char *strName = gEngine->createCString(env, name);
		sAILiveEffect->AddLiveEffectMenuItem(effect, strName, &menuData, &menuItem, NULL);
		delete menuData.category;
		delete menuData.title;
		delete strName;
		if (menuItem != NULL) {
			return gEngine->wrapMenuItemHandle(env, menuItem);	
		}
		/*
		AIMenuGroup menuGroup;
		sAIMenu->GetItemMenuGroup(menuItem, &menuGroup);
		AIPlatformMenuHandle platformMenuHandle;
		short firstItem, lastItem;
		sAIMenu->RemoveMenuItem(menuItem);
		sAIMenu->GetMenuGroupRange(menuGroup, &platformMenuHandle, &firstItem, &lastItem);
#ifdef MAC_ENV
		MenuID id = GetMenuID(platformMenuHandle);
		ReleaseMenu(platformMenuHandle);
		DeleteMenu(id);
		DisposeMenu(platformMenuHandle);
#else 
	error
#endif
		*/
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * boolean updateParameters(java.util.Map parameters)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_LiveEffect_updateParameters(JNIEnv *env, jobject obj, jobject parameters) {
	try {
		AILiveEffectParamContext context = gEngine->getLiveEffectContext(env, parameters);
		if (context != NULL) {
			if (!sAILiveEffect->UpdateParameters(context))
				return true;
		}
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * java.lang.Object getMenuItem(java.util.Map parameters)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_LiveEffect_getMenuItem(JNIEnv *env, jobject obj, jobject parameters) {
	try {
		AILiveEffectParamContext context = gEngine->getLiveEffectContext(env, parameters);
		AIMenuItemHandle menuItem = sAILiveEffect->GetMenuItem(context);
		if (menuItem != NULL) {
			return gEngine->wrapMenuItemHandle(env, menuItem);	
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}
