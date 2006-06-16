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
 * $Revision: 1.7 $
 * $Date: 2006/06/16 16:18:26 $
 */

#include "StdHeaders.h"
#include "ScriptographerPlugin.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_LiveEffect.h"

#define NAME_PREFIX "Scriptographer "

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
		// add the prefix to the name so getCreatedEffects() knows which live effects where created by this plugin:
		char *shortName = gEngine->convertString(env, name);
		char *longName = new char[strlen(shortName) + strlen(NAME_PREFIX) + 1];
		strcpy(longName, NAME_PREFIX);
		strcat(longName, shortName);
		effectInfo.name = longName;
		effectInfo.title = gEngine->convertString(env, title);
		effectInfo.majorVersion = majorVersion;
		effectInfo.minorVersion = minorVersion;
		effectInfo.prefersAsInput = preferedInput;
		effectInfo.styleFilterFlags = type | flags;
		sAILiveEffect->AddLiveEffect(&effectInfo, &liveEffectHandle);
		delete shortName;
		delete longName;
		delete effectInfo.title;
	} EXCEPTION_CONVERT(env)
	return (jint) liveEffectHandle;
}

/*
 * com.scriptographer.adm.MenuItem nativeAddMenuItem(String name, String category, String title)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_LiveEffect_nativeAddMenuItem(JNIEnv *env, jobject obj, jstring name, jstring category, jstring title) {
	try {
		AILiveEffectHandle effect = gEngine->getLiveEffectHandle(env, obj);
	
		AddLiveEffectMenuData menuData;
		menuData.category = gEngine->convertString(env, category);
		menuData.title = gEngine->convertString(env, title);
		menuData.options = 0;

		AIMenuItemHandle menuItem = NULL;
		char *strName = gEngine->convertString(env, name);
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

/*
 * java.util.ArrayList nativeGetEffects()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_LiveEffect_nativeGetEffects(JNIEnv *env, jclass cls) {
	try {
		jobject array = gEngine->newObject(env, gEngine->cls_ArrayList, gEngine->cid_ArrayList);
		long count;
		sAILiveEffect->CountLiveEffects(&count);
		int prefixLen = strlen(NAME_PREFIX);
		for (int i = 0; i < count; i++) {
			AILiveEffectHandle effect;
			sAILiveEffect->GetNthLiveEffect(i, &effect);
			const char *name;
			sAILiveEffect->GetLiveEffectName(effect, &name);
			// see wether it starts with Scriptographer :
			if (strstr(name, NAME_PREFIX) == name) {
				// collect all the settings:
				const char *realname = &name[prefixLen];
				const char *title;
				long major, minor, inputPreference, styleFilterFlags;
				sAILiveEffect->GetLiveEffectTitle(effect, &title);
				sAILiveEffect->GetLiveEffectVersion(effect, &major, &minor);
				sAILiveEffect->GetInputPreference(effect, &inputPreference);
				sAILiveEffect->GetStyleFilterFlags(effect, &styleFilterFlags);
				
				jint type = styleFilterFlags & kFilterTypeMask;
				jint flags = styleFilterFlags & ~kFilterTypeMask;
				// create the wrapper
				jobject effectObj = gEngine->newObject(env, gEngine->cls_LiveEffect, gEngine->cid_LiveEffect,
						(jint) effect, gEngine->convertString(env, realname), gEngine->convertString(env, title),
						(jint) inputPreference, type, flags, (jint) major, (jint) minor);
				// and add it to the array
				gEngine->callObjectMethod(env, array, gEngine->mid_Collection_add, effectObj);
			}
		}
		return array;
	} EXCEPTION_CONVERT(env)
	return NULL;
}
