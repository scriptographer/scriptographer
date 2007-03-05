/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id$
 */

#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "ScriptographerPlugin.h"
#include "AppContext.h"
#include "admGlobals.h"
#include "com_scriptographer_adm_Dialog.h"
#include "com_scriptographer_adm_Notifier.h"
#include "resourceIds.h"

/*
 * com.scriptographer.ai.Dialog
 */

ASErr ASAPI Dialog_onInit(ADMDialogRef dialog) {
	AppContext context;
	// hide the dialog by default:
	sADMDialog->Show(dialog, false);
	
	// Attach the dialog-level callbacks
	DEFINE_CALLBACK_PROC(Dialog_onDestroy);
	sADMDialog->SetDestroyProc(dialog, (ADMDialogDestroyProc) CALLBACK_PROC(Dialog_onDestroy));
	
	DEFINE_CALLBACK_PROC(Dialog_onNotify);
	sADMDialog->SetNotifyProc(dialog, (ADMDialogNotifyProc) CALLBACK_PROC(Dialog_onNotify));
	
	// resize handler:
	ADMItemRef resizeItemRef = sADMDialog->GetItem(dialog, kADMResizeItemID);
	if (resizeItemRef) {
		DEFINE_CALLBACK_PROC(Dialog_onSizeChanged);
		sADMItem->SetNotifyProc(resizeItemRef, (ADMItemNotifyProc) CALLBACK_PROC(Dialog_onSizeChanged));
	}
	
	// Execute a one-shot timer right after creation of the dialog, to run initialize()
	DEFINE_CALLBACK_PROC(Dialog_onInitialize);
	sADMDialog->CreateTimer(dialog, 0, 0, (ADMDialogTimerProc) CALLBACK_PROC(Dialog_onInitialize), NULL, 0);
	return kNoErr;
}

ADMBoolean ADMAPI Dialog_onInitialize(ADMDialogRef dialog, ADMTimerRef timerID) {
	// clear timer
	sADMDialog->AbortTimer(dialog, timerID);
	// call onNotify with NOTIFIER_WINDOW_INITIALIZE
	JNIEnv *env = gEngine->getEnv();
	try {
		jobject obj = gEngine->getDialogObject(dialog);
		gEngine->callVoidMethodReport(env, obj, gEngine->mid_NotificationHandler_onNotify_int,
									  (jint) com_scriptographer_adm_Notifier_NOTIFIER_WINDOW_INITIALIZE);
	} EXCEPTION_CATCH_REPORT(env);
	return true;
}

void ASAPI Dialog_onDestroy(ADMDialogRef dialog) {
	if (gEngine != NULL) {
		JNIEnv *env = gEngine->getEnv();
		try {
			jobject obj = gEngine->getDialogObject(dialog);
			gEngine->callOnDestroy(obj);
			// clear the handle:
			gEngine->setIntField(env, obj, gEngine->fid_ADMObject_handle, 0);
			env->DeleteGlobalRef(obj);
		} EXCEPTION_CATCH_REPORT(env);
	}
}

void ASAPI Dialog_onSizeChanged(ADMItemRef item, ADMNotifierRef notifier) {
	sADMItem->DefaultNotify(item, notifier);
	if (sADMNotifier->IsNotifierType(notifier, kADMBoundsChangedNotifier)) {
		JNIEnv *env = gEngine->getEnv();
		try {
			ADMDialogRef dialog = sADMItem->GetDialog(item);
			jobject obj = gEngine->getDialogObject(dialog);
			ADMRect size;
			sADMDialog->GetLocalRect(dialog, &size);
			gEngine->callVoidMethod(env, obj, gEngine->mid_Dialog_onSizeChanged, size.right, size.bottom);
		} EXCEPTION_CATCH_REPORT(env);
	}
}

void ASAPI Dialog_onNotify(ADMDialogRef dialog, ADMNotifierRef notifier) {
	sADMDialog->DefaultNotify(dialog, notifier);
	jobject obj = gEngine->getDialogObject(dialog);
	if (gEngine != NULL)
		gEngine->callOnNotify(obj, notifier);
}

ASBoolean ASAPI Dialog_onTrack(ADMDialogRef dialog, ADMTrackerRef tracker) {
	jobject obj = gEngine->getDialogObject(dialog);
	ASBoolean ret = gEngine->callOnTrack(obj, tracker);
	if (ret)
		ret = sADMDialog->DefaultTrack(dialog, tracker);
	return ret;
}

void ASAPI Dialog_onDraw(ADMDialogRef dialog, ADMDrawerRef drawer) {
	sADMDialog->DefaultDraw(dialog, drawer);
	jobject obj = gEngine->getDialogObject(dialog);
	gEngine->callOnDraw(obj, drawer);
}

/*
 * int nativeCreate(java.lang.String name, int style, int options)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Dialog_nativeCreate(JNIEnv *env, jobject obj, jstring name, jint style, jint options) {
	try {
		char *str = gEngine->convertString(env, name);
		DEFINE_CALLBACK_PROC(Dialog_onInit);
		ADMDialogRef dialog = sADMDialog->Create(gPlugin->getPluginRef(), str, kEmptyDialogID, (ADMDialogStyle) style, (ADMDialogInitProc) CALLBACK_PROC(Dialog_onInit), env->NewGlobalRef(obj), options);
		sADMDialog->Size(dialog, 200, 200);
		delete str;
		if (dialog == NULL)
			throw new StringException("Cannot create dialog.");
		return (jint) dialog;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void nativeDestroy(int handle)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_nativeDestroy(JNIEnv *env, jobject obj, jint handle) {
	try {
		sADMDialog->Destroy((ADMDialogRef) handle);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetTrackCallback(boolean enabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_nativeSetTrackCallback(JNIEnv *env, jobject obj, jboolean enabled) {
	try {
		ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		DEFINE_CALLBACK_PROC(Dialog_onTrack);
		sADMDialog->SetTrackProc(dialog, enabled ? (ADMDialogTrackProc) CALLBACK_PROC(Dialog_onTrack) : NULL);
	} EXCEPTION_CONVERT(env);
}

/*
 * int getTrackMask()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Dialog_getTrackMask(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		return sADMDialog->GetMask(dialog);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void setTrackMask(int mask)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setTrackMask(JNIEnv *env, jobject obj, jint mask) {
	try {
		ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->SetMask(dialog, mask);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetDrawCallback(boolean enabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_nativeSetDrawCallback(JNIEnv *env, jobject obj, jboolean enabled) {
	try {
		ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		DEFINE_CALLBACK_PROC(Dialog_onTrack);
		sADMDialog->SetDrawProc(dialog, enabled ? (ADMDialogDrawProc) CALLBACK_PROC(Dialog_onTrack) : NULL);
	} EXCEPTION_CONVERT(env);
}

/*
 * java.awt.Dimension nativeGetSize()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_nativeGetSize(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		ADMRect size;
		sADMDialog->GetLocalRect(dialog, &size);
		return gEngine->convertDimension(env, size.right, size.bottom);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void nativeSetSize(int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_nativeSetSize(JNIEnv *env, jobject obj, jint width, jint height) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->Size(dialog, width, height);
	} EXCEPTION_CONVERT(env);
}

/*
 * java.awt.Rectangle nativeGetBounds()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_nativeGetBounds(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		ADMRect rect;
		sADMDialog->GetBoundsRect(dialog, &rect);
		return gEngine->convertRectangle(env, &rect);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void nativeSetBounds(int x, int y, int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_nativeSetBounds(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		DEFINE_ADM_RECT(rt, x, y, width, height);
		sADMDialog->SetBoundsRect(dialog, &rt);
	} EXCEPTION_CONVERT(env);
}

/*
 * java.awt.Point getLocation()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_getLocation(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		ADMRect rect;
		sADMDialog->GetBoundsRect(dialog, &rect);
		return gEngine->convertPoint(env, rect.left, rect.top);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setLocation(int x, int y)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setLocation(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->Move(dialog, x, y);
	} EXCEPTION_CONVERT(env);
}

/*
 * java.awt.Dimension nativeGetMinimumSize()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_nativeGetMinimumSize(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		ADMPoint pt;
		pt.h = sADMDialog->GetMinWidth(dialog);
		pt.v = sADMDialog->GetMinHeight(dialog);
		return gEngine->convertDimension(env, &pt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void nativeSetMinimumSize(int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_nativeSetMinimumSize(JNIEnv *env, jobject obj, jint width, jint height) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->SetMinWidth(dialog, width);
		sADMDialog->SetMinHeight(dialog, height);
	} EXCEPTION_CONVERT(env);
}

/*
 * java.awt.Dimension nativeGetMaximumSize()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_nativeGetMaximumSize(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		DEFINE_ADM_POINT(pt, sADMDialog->GetMaxWidth(dialog), sADMDialog->GetMaxHeight(dialog));
		return gEngine->convertDimension(env, &pt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void nativeSetMaximumSize(int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_nativeSetMaximumSize(JNIEnv *env, jobject obj, jint width, jint height) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->SetMaxWidth(dialog, width);
		sADMDialog->SetMaxHeight(dialog, height);
	} EXCEPTION_CONVERT(env);
}

/*
 * java.awt.Dimension getIncrement()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_getIncrement(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		ADMPoint pt;
		pt.h = sADMDialog->GetHorizontalIncrement(dialog);
		pt.v = sADMDialog->GetVerticalIncrement(dialog);
		return gEngine->convertDimension(env, &pt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setIncrement(int hor, int ver)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setIncrement(JNIEnv *env, jobject obj, jint hor, jint ver) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->SetHorizontalIncrement(dialog, hor);
		sADMDialog->SetVerticalIncrement(dialog, ver);
	} EXCEPTION_CONVERT(env);
}

/*
 * int getItemHandle(int itemID)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Dialog_getItemHandle(JNIEnv *env, jobject obj, jint itemID) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		ADMItemRef item = sADMDialog->GetItem(dialog, itemID);
		// Workaround for CS3 problem, where popup menu only appears if it's
		// associated with a menu resource containing one entry on Mac
		// TODO: how about PC?
		if (itemID == kADMMenuItemID) {
			ADMListRef list = sADMItem->GetList(item);
			if (list) {
				sADMList->SetMenuID(list, gPlugin->getPluginRef(), kEmptyMenuID, NULL);
				sADMList->RemoveEntry(list, 0);
			}
		}
		return (jint) item;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * java.awt.Point localToScreen(int x, int y)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_localToScreen__II(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		DEFINE_ADM_POINT(pt, x, y);
		sADMDialog->LocalToScreenPoint(dialog, &pt);
		return gEngine->convertPoint(env, &pt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * java.awt.Point screenToLocal(int x, int y)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_screenToLocal__II(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		DEFINE_ADM_POINT(pt, x, y);
		sADMDialog->ScreenToLocalPoint(dialog, &pt);
		return gEngine->convertPoint(env, &pt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * java.awt.Rectangle localToScreen(int x, int y, int width, int height)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_localToScreen__IIII(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		DEFINE_ADM_RECT(rt, x, y, width, height);
		sADMDialog->LocalToScreenRect(dialog, &rt);
		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * java.awt.Rectangle screenToLocal(int x, int y, int width, int height)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_screenToLocal__IIII(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		DEFINE_ADM_RECT(rt, x, y, width, height);
		sADMDialog->ScreenToLocalRect(dialog, &rt);
		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void invalidate()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_invalidate__(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->Invalidate(dialog);
	} EXCEPTION_CONVERT(env);
}

/*
 * void invalidate(int x, int y, int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_invalidate__IIII(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		DEFINE_ADM_RECT(rt, x, y, width, height);
		sADMDialog->InvalidateRect(dialog, &rt);
	} EXCEPTION_CONVERT(env);
}

/*
 * void update()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_update(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->Update(dialog);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetVisible(boolean visible)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_nativeSetVisible(JNIEnv *env, jobject obj, jboolean visible) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->Show(dialog, visible);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetActive(boolean active)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_nativeSetActive(JNIEnv *env, jobject obj, jboolean active) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->Activate(dialog, active);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isEnabled()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_Dialog_isEnabled(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
	    return sADMDialog->IsEnabled(dialog);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setEnabled(boolean enabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setEnabled(JNIEnv *env, jobject obj, jboolean enabled) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->Enable(dialog, enabled);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isUpdateEnabled()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_Dialog_isUpdateEnabled(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
	    return sADMDialog->IsUpdateEnabled(dialog);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void seUpdateEnabled(boolean updateEnabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setUpdateEnabled(JNIEnv *env, jobject obj, jboolean updateEnabled) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->Enable(dialog, updateEnabled);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isCollapsed()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_Dialog_isCollapsed(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
	    return sADMDialog->IsCollapsed(dialog);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * int getCursor()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Dialog_getCursor(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		long cursor;
		SPPluginRef pluginRef = sADMDialog->GetPluginRef(dialog);
		sADMDialog->GetCursorID(dialog, &pluginRef, &cursor);
		return cursor;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void setCursor(int cursor)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setCursor(JNIEnv *env, jobject obj, jint cursor) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		if (cursor >= 0) {
			SPPluginRef pluginRef = sADMDialog->GetPluginRef(dialog);
			sADMDialog->SetCursorID(dialog, pluginRef, cursor);
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetTitle(java.lang.String title)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_nativeSetTitle(JNIEnv *env, jobject obj, jstring title) {
	try {
		ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		if (title != NULL) {
			ASUnicode *chars = gEngine->convertString_ASUnicode(env, title);
			sADMDialog->SetTextW(dialog, chars);
			delete chars;
		} else {
			sADMDialog->SetText(dialog, "");
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * int getFont()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Dialog_getFont(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		return sADMDialog->GetFont(dialog);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void setFont(int font)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setFont(JNIEnv *env, jobject obj, jint font) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		if (font >= 0) sADMDialog->SetFont(dialog, (ADMFont)font);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.adm.DialogItem getDefaultItem()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_getDefaultItem(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		ADMItemRef itm = sADMDialog->GetItem(dialog, sADMDialog->GetDefaultItemID(dialog));
		if (itm != NULL)
			return gEngine->getItemObject(itm);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setDefaultItem(com.scriptographer.adm.DialogItem item)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setDefaultItem(JNIEnv *env, jobject obj, jobject item) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
	    ADMItemRef itm = gEngine->getItemRef(env, item);
	    if (itm != NULL)
	    	sADMDialog->SetDefaultItemID(dialog, sADMItem->GetID(itm));
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.adm.DialogItem getCancelItem()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_getCancelItem(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		ADMItemRef itm = sADMDialog->GetItem(dialog, sADMDialog->GetCancelItemID(dialog));
		if (itm != NULL)
			return gEngine->getItemObject(itm);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setCancelItem(com.scriptographer.adm.DialogItem item)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setCancelItem(JNIEnv *env, jobject obj, jobject item) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
	    ADMItemRef itm = gEngine->getItemRef(env, item);
	    if (itm != NULL)
	    	sADMDialog->SetCancelItemID(dialog, sADMItem->GetID(itm));
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isForcedOnScreen()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_Dialog_isForcedOnScreen(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		return sADMDialog->IsForcedOnScreen(dialog);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setForcedOnScreen(boolean forcedOnScreen)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setForcedOnScreen(JNIEnv *env, jobject obj, jboolean forcedOnScreen) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->SetForcedOnScreen(dialog, forcedOnScreen);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.adm.DialogGroupInfo getGroupInfo()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_getGroupInfo(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		const char *group;
		long positionCode;
		sADMDialogGroup->GetDialogGroupInfo(dialog, &group, &positionCode);
		return gEngine->newObject(env, gEngine->cls_DialogGroupInfo, gEngine->cid_DialogGroupInfo, env->NewStringUTF(group), (jint) positionCode);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setGroupInfo(java.lang.String group, int positionCode)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setGroupInfo(JNIEnv *env, jobject obj, jstring group, jint positionCode) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		char *groupStr = gEngine->convertString(env, group);
		sADMDialogGroup->SetDialogGroupInfo(dialog, groupStr, positionCode);
		delete groupStr;
	} EXCEPTION_CONVERT(env);
}

/*
 * java.io.File nativeFileDialog(java.lang.String message, java.lang.String filter, java.io.File directory, java.lang.String filename, boolean open)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_nativeFileDialog(JNIEnv *env, jclass cls, jstring message, jstring filter, jobject directory, jstring filename, jboolean open) {
	jobject ret = NULL;
	try {
		// Unicode seems to not work (at least not on Windows?)
		// So use normal string instead...
		char *msg = gEngine->convertString(env, message);
		char *fltr = gEngine->convertString(env, filter);
		char *name = gEngine->convertString(env, filename);
		SPPlatformFileSpecification dir, result;
		bool hasDir = false;
		if (directory != NULL)
			hasDir = gEngine->convertFile(env, directory, &dir) != NULL;

		ADMPlatformFileTypesSpecification3 specs;
		// this is needed in order to zero out the mac specific part on mac...
		memset(&specs, 0, sizeof(ADMPlatformFileTypesSpecification3));
		memcpy(specs.filter, fltr, MIN(kADMMaxFilterLength, env->GetStringLength(filter)));

		if (open ? 
			sADMBasic->StandardGetFileDialog(msg, &specs, hasDir ? &dir : NULL, name, &result) :
			sADMBasic->StandardPutFileDialog(msg, &specs, hasDir ? &dir : NULL, name, &result)) {
			ret = gEngine->convertFile(env, &result);
		}

		delete msg;
		delete fltr;
		if (name != NULL)
			delete name;
	} EXCEPTION_CONVERT(env);
	return ret;
}

/*
 * java.io.File chooseDirectory(java.lang.String message, java.io.File directory)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_chooseDirectory(JNIEnv *env, jclass cls, jstring message, jobject directory) {
	jobject ret = NULL;
	try {
		// Unicode seems to not work (at least not on Windows?)
		// So use normal string instead...
		char *msg = gEngine->convertString(env, message);

		SPPlatformFileSpecification dir, result;
		bool hasDir = false;

		if (directory != NULL)
			hasDir = gEngine->convertFile(env, directory, &dir) != NULL;

		if (sADMBasic->StandardGetDirectoryDialog(msg, &dir, &result))
			ret = gEngine->convertFile(env, &result);

		delete msg;
	} EXCEPTION_CONVERT(env);
	return ret;
}

/*
 * java.awt.Color chooseColor(java.awt.Point point, java.awt.Color color)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_chooseColor(JNIEnv *env, jclass cls, jobject point, jobject color) {
	try {
		ADMRGBColor col, result;
		if (color != NULL) {
			gEngine->convertColor(env, color, &col);
		} else {
			// white:
			col.red = 65535;
			col.green = 65535;
			col.blue = 65535;
		}
		ADMPoint pt;
		if (point != NULL) {
			gEngine->convertPoint(env, point, &pt);
		} else {
			// center it on the main screen:
			pt.h = 0;
			pt.v = 0;
			ADMRect rect;
			sADMBasic->GetScreenDimensions(&pt, &rect);
			pt.h = (rect.right - rect.left) / 2;
			pt.v = (rect.bottom - rect.top) / 2;
		}
		sADMBasic->ChooseColor(pt, &col, &result);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * java.awt.Rectangle getPaletteLayoutBounds()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_getPaletteLayoutBounds(JNIEnv *env, jclass cls) {
	try {
		ADMRect bounds;
		sADMBasic->GetPaletteLayoutBounds(&bounds);
		return gEngine->convertRectangle(env, &bounds);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void alert(java.lang.String message)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_alert(JNIEnv *env, jclass cls, jstring message) {
	try {
		ASUnicode *text = gEngine->convertString_ASUnicode(env, message);
		sADMBasic->MessageAlertW(text);
		delete text;
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean confirm(java.lang.String message)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_Dialog_confirm(JNIEnv *env, jclass cls, jstring message) {
	try {
		ASUnicode *text = gEngine->convertString_ASUnicode(env, message);
		ADMAnswer ret = sADMBasic->YesNoAlertW(text);
		delete text;
		return ret == kADMYesAnswer;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * int createPlatformControl()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Dialog_createPlatformControl(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		ADMWindowRef window = sADMDialog->GetWindowRef(dialog);
#ifdef MAC_ENV
		/*
		ControlRef ctrl = NULL, root = NULL;
		Rect rect = { 0, 0, 400, 400 };
		OSStatus err = CreateUserPaneControl(window, &rect, kControlSupportsEmbedding | kControlSupportsFocus | kControlGetsFocusOnClick, &ctrl);
		err = AutoEmbedControl(ctrl, window);
		return (jint) ctrl;
		*/
		ControlRef ctrl = NULL, root = NULL;
		GetRootControl(window, &root);
		GetIndexedSubControl(root, 1, &ctrl);
		return (jint) ctrl;
#endif
#ifdef WIN_ENV
		return (jint) window;
		/*
		ADMRect rect = {50, 50, 400, 400};
		ADMItemRef item = sADMItem->Create(dialog, kADMUniqueItemID, kADMItemGroupType, &rect, NULL, NULL, 0);
		ADMWindowRef itemWnd = sADMItem->GetWindowRef(item);
		return (jint) itemWnd;
		/*
		int hHeap = GetProcessHeap();
		int hInstance = GetModuleHandle(NULL);
		WNDCLASS wWndClass;
		wndClass.hInstance = hInstance;
		wndClass.lpfnWndProc = windowProc;
		wndClass.style = OS.CS_BYTEALIGNWINDOW | OS.CS_DBLCLKS;
		wndClass.hCursor = OS.LoadCursor (0, OS.IDC_ARROW);
		int byteCount = windowClass.length () * TCHAR.sizeof;
		lpWndClass.lpszClassName = OS.HeapAlloc (hHeap, OS.HEAP_ZERO_MEMORY, byteCount);
		OS.MoveMemory (lpWndClass.lpszClassName, windowClass, byteCount);
		OS.RegisterClass (lpWndClass);
		*/
		/*
		HWND wnd = CreateWindowEx(
			0, ("Test"), NULL,
			WS_CHILD | WS_VISIBLE | WS_CLIPSIBLINGS, 
			0, 0,
			400, 400, 
			(HWND) window, 
			0, 
			GetModuleHandle(NULL),
			NULL);
		// SetWindowPos (wnd, (HWND) window, 0, 0, 400, 400, SWP_NOZORDER | SWP_DRAWFRAME | SWP_NOACTIVATE);
		// ShowWindow(wnd, true);
		*/
#endif
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void dumpControlHierarchy(java.io.File file)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_dumpControlHierarchy(JNIEnv *env, jobject obj, jobject file) {
	try {
#ifdef MAC_ENV
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		ADMWindowRef window = sADMDialog->GetWindowRef(dialog);
		SPPlatformFileSpecification fsSpec;
		gEngine->convertFile(env, file, &fsSpec);
		FSSpec fileSpec;
		fileSpec.vRefNum = fsSpec.vRefNum;
		fileSpec.parID = fsSpec.parID;
		memcpy(fileSpec.name, fsSpec.name, 64);
		DumpControlHierarchy(window, (FSSpec*) &fileSpec);
#endif
	} EXCEPTION_CONVERT(env);
}
