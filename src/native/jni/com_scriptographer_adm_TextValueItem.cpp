/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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
 */

#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_adm_TextValueItem.h"

/*
 * com.scriptographer.adm.TextValueItem
 */

/*
 * void setText(java.lang.String text)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_TextValueItem_setText(JNIEnv *env, jobject obj, jstring text) {
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
JNIEXPORT jstring JNICALL Java_com_scriptographer_adm_TextValueItem_getText(JNIEnv *env, jobject obj) {
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

/*
 * void nativeSetJustification(int justification)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_TextValueItem_nativeSetJustification(JNIEnv *env, jobject obj, jint justification) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		sADMItem->SetJustify(item, (ADMJustify) justification);
	} EXCEPTION_CONVERT(env);
}

/*
 * int nativeGetJustification()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_TextValueItem_nativeGetJustification(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		return sADMItem->GetJustify(item);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void nativeSetUnits(int units)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_TextValueItem_nativeSetUnits(JNIEnv *env, jobject obj, jint units) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		sADMItem->SetUnits(item, (ADMUnits) units);
	} EXCEPTION_CONVERT(env);
}

/*
 * int nativeGetUnits()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_TextValueItem_nativeGetUnits(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		return sADMItem->GetUnits(item);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void setShowUnits(boolean showUnits)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_TextValueItem_setShowUnits(JNIEnv *env, jobject obj, jboolean showUnits) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		sADMItem->ShowUnits(item, showUnits);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean getShowUnits()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_TextValueItem_getShowUnits(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		return sADMItem->GetShowUnits(item);
	} EXCEPTION_CONVERT(env);
	return false;
}