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
#include "com_scriptographer_adm_TextItem.h"

/*
 * com.scriptographer.adm.TextItem
 */

/*
 * void nativeSetText(java.lang.String text)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_TextItem_nativeSetText(JNIEnv *env, jobject obj, jstring text) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		if (text != NULL) {
			ASUnicode *chars = gEngine->convertString_ASUnicode(env, text);
			sADMItem->SetTextW(item, chars);
			delete chars;
		} else {
			sADMItem->SetText(item, "");
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * java.lang.String getText()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_adm_TextItem_getText(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		long len = sADMItem->GetTextLengthW(item);
		ASUnicode *chars = new ASUnicode[len];
		sADMItem->GetTextW(item, chars, len);
		jstring res = gEngine->convertString(env, chars, len);
		delete chars;
		return res;
	} EXCEPTION_CONVERT(env);
	return NULL;
}
