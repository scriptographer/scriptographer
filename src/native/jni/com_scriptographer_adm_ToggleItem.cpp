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
#include "com_scriptographer_adm_ToggleItem.h"

/*
 * com.scriptographer.adm.ToggleItem
 */

/*
 * boolean isChecked()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_ToggleItem_isChecked(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		return sADMItem->GetBooleanValue(item);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setChecked(boolean checked)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ToggleItem_setChecked(JNIEnv *env, jobject obj, jboolean checked) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		sADMItem->SetBooleanValue(item, checked);
	} EXCEPTION_CONVERT(env);
}
