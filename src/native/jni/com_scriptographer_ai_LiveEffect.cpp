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
#include "com_scriptographer_ai_LiveEffect.h"

#define NAME_PREFIX "Scriptographer "

/*
 * com.scriptographer.ai.LiveEffect
 */

/*
 * int nativeCreate(java.lang.String name, String title, int position, int preferedInput, int flags, int majorVersion, int minorVersion)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_LiveEffect_nativeCreate(JNIEnv *env, jobject obj, jstring name, jstring title, jint position, jint preferedInput, jint flags, jint majorVersion, jint minorVersion) {
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
		effectInfo.styleFilterFlags = position | flags;
		sAILiveEffect->AddLiveEffect(&effectInfo, &liveEffectHandle);
		delete shortName;
		delete longName;
		delete effectInfo.title;
	} EXCEPTION_CONVERT(env);
	return (jint) liveEffectHandle;
}

/*
 * com.scriptographer.ui.MenuItem nativeAddMenuItem(String name, String category, String title)
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
	} EXCEPTION_CONVERT(env);
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
				sAILiveEffect->GetStyleFilterFlags(effect, &styleFilterFlags);
				sAILiveEffect->GetInputPreference(effect, &inputPreference);
				sAILiveEffect->GetLiveEffectVersion(effect, &major, &minor);
				
				// Separate flags into position and flags
				jint position = styleFilterFlags & kFilterTypeMask;
				jint flags = styleFilterFlags & ~kFilterTypeMask;
				// Create the wrapper
				jobject effectObj = gEngine->newObject(env, gEngine->cls_ai_LiveEffect, gEngine->cid_ai_LiveEffect,
						(jint) effect, gEngine->convertString(env, realname), gEngine->convertString(env, title),
						position, (jint) inputPreference, flags, (jint) major, (jint) minor);
				// And add it to the array
				gEngine->callObjectMethod(env, array, gEngine->mid_Collection_add, effectObj);
			}
		}
		return array;
	} EXCEPTION_CONVERT(env);
	return NULL;
}
