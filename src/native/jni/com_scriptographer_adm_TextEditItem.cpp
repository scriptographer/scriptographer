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
#include "ScriptographerPlugin.h"
#include "com_scriptographer_adm_TextEditItem.h"

/*
 * com.scriptographer.adm.TextEdit
 */

/*
 * int getFractionDigits()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_TextEditItem_getFractionDigits(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		return sADMItem->GetPrecision(item);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void setFractionDigits(int precision)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_TextEditItem_setFractionDigits(JNIEnv *env, jobject obj, jint precision) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		sADMItem->SetPrecision(item, precision);
	} EXCEPTION_CONVERT(env);
}

/*
 * void setMaxLength(int length)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_TextEditItem_setMaxLength(JNIEnv *env, jobject obj, jint length) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		// 32767 appears to be the maximum, so internally it's a signed 16 bit value.
		// Calling setMaxLength(-1) sets the maximum allowed value.
		if (length < 0 || length > 32767)
			length = 32767;
		sADMItem->SetMaxTextLength(item, length);
	} EXCEPTION_CONVERT(env);
}

/*
 * int getMaxLength()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_TextEditItem_getMaxLength(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		return sADMItem->GetMaxTextLength(item);
	} EXCEPTION_CONVERT(env);
	return 0;
}

#if defined(MAC_ENV) && kPluginInterfaceVersion >= kAI14

// On Illustrator 14 on Mac, the scrolling only seems to work if update is called on the dialog before.
// Execute a one-shot timer in setSelection that updates the state after execution of the script
// only once, for a massive speed increase of e.g. console printing.

class SetSelectionTimer {
public:
	ADMItemRef m_item;
	ADMTimerRef m_timerId;
	int m_start;
	int m_end;
	
	SetSelectionTimer(ADMItemRef item) {
		m_item = item;
		DEFINE_CALLBACK_PROC(SetSelectionTimer::setSelection);
		m_timerId = sADMItem->CreateTimer(item, 0, 0, (ADMItemTimerProc) CALLBACK_PROC(SetSelectionTimer::setSelection), NULL, 0);
	}
	
	void setRange(int start, int end) {
		m_start = start;
		m_end = end;
	}

	void setSelection() {
		ADMDialogRef dialog = sADMItem->GetDialog(m_item);
		sADMDialog->Update(dialog);
		sADMItem->SetSelectionRange(m_item, m_start, m_end);
	}

	static ADMBoolean ADMAPI setSelection(ADMItemRef item, ADMTimerRef timerID) {
		// Clear timer
		sADMItem->AbortTimer(item, timerID);
		JNIEnv *env = gEngine->getEnv();
		try {
			jobject obj = gEngine->getItemObject(item);
			SetSelectionTimer *timer = (SetSelectionTimer *) gEngine->getIntField(env, obj, gEngine->fid_adm_TextEditItem_setSelectionTimer);
			timer->setSelection();
			// Clean up again, since we're done
			delete timer;
			gEngine->setIntField(env, obj, gEngine->fid_adm_TextEditItem_setSelectionTimer, 0);
		} EXCEPTION_CATCH_REPORT(env);
		return true;
	}
};

#endif

/*
 * void setSelection(int start, int end)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_TextEditItem_setSelection(JNIEnv *env, jobject obj, jint start, jint end) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
#if defined(MAC_ENV) && kPluginInterfaceVersion >= kAI14
		// See if a previous timer was started already. if so, just update its data.
		SetSelectionTimer *timer = (SetSelectionTimer *) gEngine->getIntField(env, obj, gEngine->fid_adm_TextEditItem_setSelectionTimer);
		if (timer == NULL) {
			timer = new SetSelectionTimer(item);
			gEngine->setIntField(env, obj, gEngine->fid_adm_TextEditItem_setSelectionTimer, (jint) timer);
		}
		timer->setRange(start, end);
#else
		sADMItem->SetSelectionRange(item, start, end);
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * int[] getSelection()
 */
JNIEXPORT jintArray JNICALL Java_com_scriptographer_adm_TextEditItem_getSelection(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		long start, end;
		sADMItem->GetSelectionRange(item, &start, &end);
		// create an int array with these values:
		jintArray res = env->NewIntArray(2);
		jint range[] = {
			start, end
		};
		env->SetIntArrayRegion(res, 0, 2, range);
		return res;
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void selectAll()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_TextEditItem_selectAll(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		sADMItem->SelectAll(item);
	} EXCEPTION_CONVERT(env);
}

/*
 * void setAllowMath(boolean arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_TextEditItem_setAllowMath(JNIEnv *env, jobject obj, jboolean allowMath) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		sADMItem->SetAllowUnits(item, allowMath);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean geAllowMath()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_TextEditItem_getAllowMath(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		return sADMItem->GetAllowMath(item);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setAllowUnits(boolean arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_TextEditItem_setAllowUnits(JNIEnv *env, jobject obj, jboolean allowUnits) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		sADMItem->SetAllowUnits(item, allowUnits);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean getAllowUnits()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_TextEditItem_getAllowUnits(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemHandle(env, obj);
		return sADMItem->GetAllowUnits(item);
	} EXCEPTION_CONVERT(env);
	return false;
}
