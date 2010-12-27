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
#include "ScriptographerEngine.h"
#include "com_scriptographer_adm_ItemGroup.h"

/*
 * com.scriptographer.adm.ItemGroup
 */

/*
 * void nativeAdd(com.scriptographer.adm.Item item)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ItemGroup_nativeAdd(JNIEnv *env, jobject obj, jobject item) {
	try {
		ADMItemRef itemRef = gEngine->getItemHandle(env, obj);
		ADMItemRef subItemRef = gEngine->getItemHandle(env, item);
		sADMItem->AddItem(itemRef, subItemRef);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeRemove(com.scriptographer.adm.Item item)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ItemGroup_nativeRemove(JNIEnv *env, jobject obj, jobject item) {
	try {
		ADMItemRef itemRef = gEngine->getItemHandle(env, obj);
		ADMItemRef subItemRef = gEngine->getItemHandle(env, item);
		sADMItem->RemoveItem(itemRef, subItemRef);
	} EXCEPTION_CONVERT(env);
}
