/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id:com_scriptographer_ui_Dialog.cpp 402 2007-08-22 23:24:49Z lehni $
 */

#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "ScriptographerPlugin.h"
#include "AppContext.h"
#include "uiGlobals.h"
#include "resourceIds.h"
#include "com_scriptographer_ui_Dialog.h"
#include "com_scriptographer_ui_Notifier.h"

/*
 * com.scriptographer.ai.Dialog
 */

ASErr ASAPI Dialog_onInit(ADMDialogRef dialog) {
	AppContext context;
	// Hide the dialog by default:
	sADMDialog->Show(dialog, false);
	
	// Attach the dialog-level callbacks
	DEFINE_CALLBACK_PROC(Dialog_onDestroy);
	sADMDialog->SetDestroyProc(dialog, (ADMDialogDestroyProc) CALLBACK_PROC(Dialog_onDestroy));
	
	DEFINE_CALLBACK_PROC(Dialog_onNotify);
	sADMDialog->SetNotifyProc(dialog, (ADMDialogNotifyProc) CALLBACK_PROC(Dialog_onNotify));
	
	// Resize handler:
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
	// Clear timer
	sADMDialog->AbortTimer(dialog, timerID);
	// Call onNotify with NOTIFIER_INITIALIZE
	JNIEnv *env = gEngine->getEnv();
	try {
		jobject obj = gEngine->getDialogObject(dialog);
		gEngine->callOnNotify(obj, kADMInitializeWindowNotifier);
	} EXCEPTION_CATCH_REPORT(env);
	return true;
}

void ASAPI Dialog_onDestroy(ADMDialogRef dialog) {
	if (gEngine != NULL) {
		JNIEnv *env = gEngine->getEnv();
		try {
			jobject obj = gEngine->getDialogObject(dialog);
			gEngine->callOnDestroy(obj);
			// Clear the handle:
			gEngine->setIntField(env, obj, gEngine->fid_ui_NativeObject_handle, 0);
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
			gEngine->callVoidMethod(env, obj, gEngine->mid_ui_Dialog_onSizeChanged, size.right, size.bottom, true);
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
	jobject obj = gEngine->getDialogObject(dialog);
	ASBoolean ret = gEngine->callOnDraw(obj, drawer);
	if (ret)
		sADMDialog->DefaultDraw(dialog, drawer);
}

/*
 * int nativeCreate(java.lang.String name, int style, int options)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ui_Dialog_nativeCreate(JNIEnv *env, jobject obj, jstring name, jint style, jint options) {
	try {
		char *str = gEngine->convertString(env, name);
		DEFINE_CALLBACK_PROC(Dialog_onInit);
		ADMDialogRef dialog = sADMDialog->Create(gPlugin->getPluginRef(), str, kEmptyDialogID, (ADMDialogStyle) style, (ADMDialogInitProc) CALLBACK_PROC(Dialog_onInit), env->NewGlobalRef(obj), options);
		sADMDialog->Size(dialog, 200, 200);
		delete str;
		if (dialog == NULL)
			throw new StringException("Unable to create dialog.");
		return (jint) dialog;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void nativeDestroy(int handle)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_nativeDestroy(JNIEnv *env, jobject obj, jint handle) {
	try {
		sADMDialog->Destroy((ADMDialogRef) handle);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetTrackCallback(boolean enabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_nativeSetTrackCallback(JNIEnv *env, jobject obj, jboolean enabled) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		DEFINE_CALLBACK_PROC(Dialog_onTrack);
		sADMDialog->SetTrackProc(dialog, enabled ? (ADMDialogTrackProc) CALLBACK_PROC(Dialog_onTrack) : NULL);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean defaultTrack(com.scriptographer.ui.Tracker tracker)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ui_Dialog_defaultTrack(JNIEnv *env, jobject obj, jobject tracker) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		return sADMDialog->DefaultTrack(dialog, gEngine->getTrackerHandle(env, tracker));
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void defaultDraw(com.scriptographer.ui.Drawer drawer)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_defaultDraw(JNIEnv *env, jobject obj, jobject drawer) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		sADMDialog->DefaultDraw(dialog, gEngine->getDrawerHandle(env, drawer));
	} EXCEPTION_CONVERT(env);
}

/*
 * int getTrackMask()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ui_Dialog_getTrackMask(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		return sADMDialog->GetMask(dialog);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void setTrackMask(int mask)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_setTrackMask(JNIEnv *env, jobject obj, jint mask) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		sADMDialog->SetMask(dialog, mask);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetDrawCallback(boolean enabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_nativeSetDrawCallback(JNIEnv *env, jobject obj, jboolean enabled) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		DEFINE_CALLBACK_PROC(Dialog_onTrack);
		sADMDialog->SetDrawProc(dialog, enabled ? (ADMDialogDrawProc) CALLBACK_PROC(Dialog_onDraw) : NULL);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ui.Size nativeGetSize()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_Dialog_nativeGetSize(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		ADMRect size;
		sADMDialog->GetLocalRect(dialog, &size);
		return gEngine->convertSize(env, size.right, size.bottom);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void nativeSetSize(int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_nativeSetSize(JNIEnv *env, jobject obj, jint width, jint height) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		sADMDialog->Size(dialog, width, height);
	} EXCEPTION_CONVERT(env);
}

/*
 * java.awt.Rectangle nativeGetBounds()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_Dialog_nativeGetBounds(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		ADMRect rect;
		sADMDialog->GetBoundsRect(dialog, &rect);
		return gEngine->convertRectangle(env, &rect);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void nativeSetBounds(int x, int y, int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_nativeSetBounds(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		DEFINE_ADM_RECT(rect, x, y, width, height);
		sADMDialog->SetBoundsRect(dialog, &rect);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ui.Point getPosition()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_Dialog_getPosition(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		ADMRect rect;
		sADMDialog->GetBoundsRect(dialog, &rect);
		return gEngine->convertPoint(env, rect.left, rect.top);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setPosition(int x, int y)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_setPosition(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		sADMDialog->Move(dialog, x, y);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ui.Size nativeGetimumMinSize()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_Dialog_nativeGetMinimumSize(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		ADMPoint pt;
		pt.h = sADMDialog->GetMinWidth(dialog);
		pt.v = sADMDialog->GetMinHeight(dialog);
		return gEngine->convertSize(env, &pt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void nativeSetMinimumSize(int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_nativeSetMinimumSize(JNIEnv *env, jobject obj, jint width, jint height) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		sADMDialog->SetMinWidth(dialog, width);
		sADMDialog->SetMinHeight(dialog, height);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ui.Size nativeGetMaximumSize()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_Dialog_nativeGetMaximumSize(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		DEFINE_ADM_POINT(pt, sADMDialog->GetMaxWidth(dialog), sADMDialog->GetMaxHeight(dialog));
		return gEngine->convertSize(env, &pt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void nativeSetMaximumSize(int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_nativeSetMaximumSize(JNIEnv *env, jobject obj, jint width, jint height) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		sADMDialog->SetMaxWidth(dialog, width);
		sADMDialog->SetMaxHeight(dialog, height);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ui.Size getIncrement()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_Dialog_getIncrement(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		ADMPoint pt;
		pt.h = sADMDialog->GetHorizontalIncrement(dialog);
		pt.v = sADMDialog->GetVerticalIncrement(dialog);
		return gEngine->convertSize(env, &pt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setIncrement(int hor, int ver)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_setIncrement(JNIEnv *env, jobject obj, jint hor, jint ver) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		sADMDialog->SetHorizontalIncrement(dialog, hor);
		sADMDialog->SetVerticalIncrement(dialog, ver);
	} EXCEPTION_CONVERT(env);
}

/*
 * int getItemHandle(int itemID)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ui_Dialog_getItemHandle(JNIEnv *env, jobject obj, jint itemID) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
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
 * com.scriptographer.ui.Point localToScreen(int x, int y)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_Dialog_localToScreen__II(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		DEFINE_ADM_POINT(pt, x, y);
		sADMDialog->LocalToScreenPoint(dialog, &pt);
		return gEngine->convertPoint(env, &pt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ui.Point screenToLocal(int x, int y)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_Dialog_screenToLocal__II(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		DEFINE_ADM_POINT(pt, x, y);
		sADMDialog->ScreenToLocalPoint(dialog, &pt);
		return gEngine->convertPoint(env, &pt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * java.awt.Rectangle localToScreen(int x, int y, int width, int height)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_Dialog_localToScreen__IIII(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		DEFINE_ADM_RECT(rt, x, y, width, height);
		sADMDialog->LocalToScreenRect(dialog, &rt);
		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * java.awt.Rectangle screenToLocal(int x, int y, int width, int height)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_Dialog_screenToLocal__IIII(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		DEFINE_ADM_RECT(rt, x, y, width, height);
		sADMDialog->ScreenToLocalRect(dialog, &rt);
		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void invalidate()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_invalidate__(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		sADMDialog->Invalidate(dialog);
	} EXCEPTION_CONVERT(env);
}

/*
 * void invalidate(int x, int y, int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_invalidate__IIII(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		DEFINE_ADM_RECT(rt, x, y, width, height);
		sADMDialog->InvalidateRect(dialog, &rt);
	} EXCEPTION_CONVERT(env);
}

/*
 * void update()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_update(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		sADMDialog->Update(dialog);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetVisible(boolean visible)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_nativeSetVisible(JNIEnv *env, jobject obj, jboolean visible) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		sADMDialog->Show(dialog, visible);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetActive(boolean active)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_nativeSetActive(JNIEnv *env, jobject obj, jboolean active) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		sADMDialog->Activate(dialog, active);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isEnabled()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ui_Dialog_isEnabled(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		return sADMDialog->IsEnabled(dialog);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setEnabled(boolean enabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_setEnabled(JNIEnv *env, jobject obj, jboolean enabled) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		sADMDialog->Enable(dialog, enabled);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isUpdateEnabled()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ui_Dialog_isUpdateEnabled(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		return sADMDialog->IsUpdateEnabled(dialog);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void seUpdateEnabled(boolean updateEnabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_setUpdateEnabled(JNIEnv *env, jobject obj, jboolean updateEnabled) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		sADMDialog->Enable(dialog, updateEnabled);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isCollapsed()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ui_Dialog_isCollapsed(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		return sADMDialog->IsCollapsed(dialog);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * int nativeGetCursor()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ui_Dialog_nativeGetCursor(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		long cursor;
		SPPluginRef pluginRef = sADMDialog->GetPluginRef(dialog);
		sADMDialog->GetCursorID(dialog, &pluginRef, &cursor);
		return cursor;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void nativeSetCursor(int cursor)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_nativeSetCursor(JNIEnv *env, jobject obj, jint cursor) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		if (cursor >= 0) {
			SPPluginRef pluginRef = sADMDialog->GetPluginRef(dialog);
			sADMDialog->SetCursorID(dialog, pluginRef, cursor);
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetTitle(java.lang.String title)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_nativeSetTitle(JNIEnv *env, jobject obj, jstring title) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
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
 * void nativeSetName(java.lang.String name)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_nativeSetName(JNIEnv *env, jobject obj, jstring name) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		if (name != NULL) {
			char *chars = gEngine->convertString(env, name);
			sADMDialog->SetDialogName(dialog, chars);
			delete chars;
		} else {
			sADMDialog->SetDialogName(dialog, "");
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * int nativeGetFont()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ui_Dialog_nativeGetFont(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		return sADMDialog->GetFont(dialog);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void nativeSetFont(int font)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_nativeSetFont(JNIEnv *env, jobject obj, jint font) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		if (font >= 0) sADMDialog->SetFont(dialog, (ADMFont)font);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ui.DialogItem getDefaultItem()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_Dialog_getDefaultItem(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		ADMItemRef itm = sADMDialog->GetItem(dialog, sADMDialog->GetDefaultItemID(dialog));
		if (itm != NULL)
			return gEngine->getItemObject(itm);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setDefaultItem(com.scriptographer.ui.DialogItem item)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_setDefaultItem(JNIEnv *env, jobject obj, jobject item) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		ADMItemRef itm = gEngine->getItemHandle(env, item);
		if (itm != NULL)
			sADMDialog->SetDefaultItemID(dialog, sADMItem->GetID(itm));
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ui.DialogItem getCancelItem()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_Dialog_getCancelItem(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		ADMItemRef itm = sADMDialog->GetItem(dialog, sADMDialog->GetCancelItemID(dialog));
		if (itm != NULL)
			return gEngine->getItemObject(itm);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setCancelItem(com.scriptographer.ui.DialogItem item)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_setCancelItem(JNIEnv *env, jobject obj, jobject item) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		ADMItemRef itm = gEngine->getItemHandle(env, item);
		if (itm != NULL)
			sADMDialog->SetCancelItemID(dialog, sADMItem->GetID(itm));
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isForcedOnScreen()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ui_Dialog_isForcedOnScreen(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		return sADMDialog->IsForcedOnScreen(dialog);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setForcedOnScreen(boolean forcedOnScreen)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_setForcedOnScreen(JNIEnv *env, jobject obj, jboolean forcedOnScreen) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		sADMDialog->SetForcedOnScreen(dialog, forcedOnScreen);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ui.DialogGroupInfo getGroupInfo()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_Dialog_getGroupInfo(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		const char *group;
#if _kADMDialogGroupSuiteVersion >= 7
		ADMPositionCode code;
		sADMDialogGroup->GetDialogGroupInfo(dialog, &group, &code);
		ADMInt32 positionCode = code.fPositionCode;
#else
		ADMInt32 positionCode = 0;
		sADMDialogGroup->GetDialogGroupInfo(dialog, &group, &positionCode);
#endif
		return gEngine->newObject(env, gEngine->cls_ui_DialogGroupInfo, gEngine->cid_ui_DialogGroupInfo, env->NewStringUTF(group), (jint) positionCode);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void nativeSetGroupInfo(java.lang.String group, int positionCode)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_nativeSetGroupInfo(JNIEnv *env, jobject obj, jstring group, jint positionCode) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		char *groupStr = gEngine->convertString(env, group);
#if _kADMDialogGroupSuiteVersion >= 7
		ADMPositionCode code;
		code.fPositionCode= positionCode;
		code.fExPositionCode = 0;
		sADMDialogGroup->SetDialogGroupInfo(dialog, groupStr, code);
#else
		sADMDialogGroup->SetDialogGroupInfo(dialog, groupStr, positionCode);
#endif
		delete groupStr;
	} EXCEPTION_CONVERT(env);
}

#ifdef MAC_ENV

class NavigationFilter {
private:
	CFStringRef name;
	Array<UniChar *> patterns;

public:
	NavigationFilter(CFStringRef filterName, CFStringRef filter) {
		name = CFStringCreateCopy(NULL, filterName);
		CFArrayRef parts = CFStringCreateArrayBySeparatingStrings(NULL, filter, CFSTR(";"));
		int count = CFArrayGetCount(parts);
		for (int i = 0; i < count; i++) {
			CFStringRef part = (CFStringRef) CFArrayGetValueAtIndex(parts, i);
			if (CFStringGetLength(part) == 0)
				break;
			patterns.add(getCharacters(part));
		}
		CFRelease(filter);
	}

	~NavigationFilter() {
		for (int i = 0; i < patterns.size(); i++)
			delete patterns.get(i);
		CFRelease(name);
	}

	bool match(CFStringRef str) {
		UniChar *chars = getCharacters(str); 
		bool match = true;
		for (int i = 0; match && i < patterns.size(); i++) {
			if (!matchPattern(chars, patterns.get(i)))
				match = false;
		}
		delete chars;
		return match;
	}

	inline CFStringRef getName() {
		return name;
	}

private:
	UniChar *getCharacters(CFStringRef str) {
		// Returns a 0 determined UniChar string
		int len = CFStringGetLength(str);
		UniChar *chars = new UniChar[len + 1];
		CFRange range = { 0, len };
		CFStringGetCharacters(str, range, chars);
		chars[len] = '\0';
		return chars;
	}

	bool matchPattern(const UniChar *str, const UniChar *pattern) {
		const UniChar *cp = NULL, *mp = NULL;
		
		while (*str && *pattern != '*') {
			if (*pattern != *str && *pattern != '?')
				return false;
			pattern++;
			str++;
		}
		while (*str) {
			if (*pattern == '*') {
				if (!*++pattern)
					return 1;
				mp = pattern;
				cp = str + 1;
			} else if (*pattern == *str || *pattern == '?') {
				pattern++;
				str++;
			} else {
				pattern = mp;
				str = cp++;
			}
		}
		while (*pattern == '*')
			pattern++;
		return !*pattern;
	}
};

enum NavigationType {
	FileOpen,
	FileSave,
	FolderChoose
};

class NavigationService {
	NavigationType type;
	Array<NavigationFilter *> filters;
	int currentIndex;
	NavDialogCreationOptions options;
	NavDialogRef dialog;
	NavEventUPP eventCallbackUPP;
	NavObjectFilterUPP filterCallbackUPP;

public:
	NavigationService(JNIEnv *env, NavigationType navigationType, jstring message, jstring filename, jstring filter) {
		type = navigationType;
		dialog = NULL;
		eventCallbackUPP = NULL;
		filterCallbackUPP = NULL;
		memset(&options, 0, sizeof(NavDialogCreationOptions));
		if (!NavGetDefaultDialogCreationOptions(&options)) {
			options.message = gEngine->convertString_CFString(env, message);
			options.saveFileName = gEngine->convertString_CFString(env, filename);
			eventCallbackUPP = NewNavEventUPP(eventCallback);
			if (type == FileOpen && filter != NULL) {
				CFStringRef str = gEngine->convertString_CFString(env, filter);
				CFStringRef seperator = CFStringCreateWithBytes (NULL, (const UInt8*) "\0", 1, kCFStringEncodingUTF8, false);
				CFArrayRef parts = CFStringCreateArrayBySeparatingStrings(NULL, str, seperator);
				CFIndex count = CFArrayGetCount(parts);
				CFStringRef name;
				for (int i = 0; i < count; i++) {
					CFStringRef part = (CFStringRef) CFArrayGetValueAtIndex(parts, i);
					if (CFStringGetLength(part) == 0)
						break;
					if (i % 2 == 0) {
						name = part;
					} else {
						filters.add(new NavigationFilter(name, part));
						name = NULL;
					}
				}
				if (name != NULL)
					CFRelease(name);
				CFRelease(parts);
				CFRelease(seperator);
				CFRelease(str);
				if (filters.size() > 0) {
					options.popupExtension = getFilterNames();
					filterCallbackUPP = NewNavObjectFilterUPP(filterCallback);
				}
			}
			if (type == FileOpen) {
				NavCreateGetFileDialog(&options, NULL, eventCallbackUPP, NULL, filterCallbackUPP, this, &dialog);
			} else if (type == FileSave) {
				NavCreatePutFileDialog(&options, 0, 0, eventCallbackUPP, this, &dialog);
			} else if (type == FolderChoose) {
				NavCreateChooseFolderDialog(&options, eventCallbackUPP, filterCallbackUPP, this, &dialog);
			}
		}
	}
	
	~NavigationService() {
		for (int i = 0; i < filters.size(); i++)
			delete filters.get(i);
		if (eventCallbackUPP != NULL)
			DisposeNavEventUPP(eventCallbackUPP);
		if (filterCallbackUPP != NULL)
			DisposeNavObjectFilterUPP(filterCallbackUPP);
		if (options.popupExtension != NULL)
			CFRelease(options.popupExtension);
		if (options.windowTitle != NULL)
			CFRelease(options.windowTitle);
		if (options.saveFileName != NULL)
			CFRelease(options.saveFileName);
		if (options.message != NULL)
			CFRelease(options.message);
		if (options.clientName != NULL)
			CFRelease(options.clientName);
		if (dialog != NULL)
			NavDialogDispose(dialog);
	}

	jobject execute(JNIEnv *env, jobject initialDir = NULL, jobject selectedFile = NULL) {
		jobject res = NULL;
		if (dialog != NULL) {
			SPPlatformFileSpecification spec;
			if (initialDir != NULL) {
				AEDesc location = { typeNull, NULL };
				AECreateDesc(typeFSS, (FSSpec *) gEngine->convertFile(env, initialDir, &spec), sizeof(FSSpec), &location);
				NavCustomControl(dialog, kNavCtlSetLocation, &location);
			}
			if (selectedFile != NULL) {
				AEDesc location = { typeNull, NULL };
				AECreateDesc(typeFSS, (FSSpec *) gEngine->convertFile(env, selectedFile, &spec), sizeof(FSSpec), &location);
				NavCustomControl(dialog, kNavCtlSetSelection, &location);
			}
			if (!NavDialogRun(dialog)) {
				NavUserAction action = NavDialogGetUserAction(dialog);
				NavReplyRecord reply;
				if (action != kNavUserActionCancel && action != kNavUserActionNone
					&& !NavDialogGetReply(dialog, &reply) && reply.validRecord) {
					long count;
					if (!AECountItems(&reply.selection, &count)) {
						for (long i = 1; i <= count; i++) {
							AEDesc resultDesc;
							FSRef result;
							if (!AEGetNthDesc(&reply.selection, i, typeFSRef, NULL, &resultDesc)
								&& !AEGetDescData(&resultDesc, &result, sizeof(FSRef))) {
								char path[1024];
								if (!FSRefMakePath(&result, (unsigned char*) path, 1024)) {
									if (type == FileSave) {
										CFStringRef saveName = NULL;
										char name[1024];
										if ((saveName = NavDialogGetSaveFileName(dialog))
											&& CFStringGetCString(saveName, name, 1024, kCFStringEncodingUTF8)
											&& strlen(path) + strlen(name) + strlen(PATH_SEP_STR) < 1024) {
											strcat(path, PATH_SEP_STR);
											strcat(path, name);
											res = gEngine->convertFile(env, path);
										}
									} else {
										res = gEngine->convertFile(env, path);
									}
								}
								AEDisposeDesc(&resultDesc);
							}
						}
					}
					NavDisposeReply(&reply);
				}
			}
		}
		return res;
	}

	static jobject execute(JNIEnv *env, NavigationType type, jstring message, jstring filename, jstring filter, jobject initialDir = NULL, jobject selectedFile = NULL) {
		return NavigationService(env, type, message, filename, filter).execute(env, initialDir, selectedFile);
	}
	
private:
	CFArrayRef getFilterNames() {
		CFMutableArrayRef array = CFArrayCreateMutable(NULL, filters.size(), NULL);
		for (int i = 0; i < filters.size(); i++)
			CFArrayAppendValue(array, filters.get(i)->getName());
		return array;
	}

	static pascal void eventCallback(NavEventCallbackMessage callBackSelector, NavCBRecPtr callBackParams, NavCallBackUserData callBackData) {
		if (callBackSelector ==  kNavCBPopupMenuSelect) {
			NavMenuItemSpec *chosenItem = (NavMenuItemSpec *) callBackParams->eventData.eventDataParms.param;
			((NavigationService *) callBackData)->currentIndex = chosenItem->menuType;
		}
	}
	
	static pascal Boolean filterCallback(AEDesc* item, void* info, NavCallBackUserData callBackData, NavFilterModes filterMode) {
		NavigationService *service = (NavigationService *) callBackData;
		if (service == NULL) {
			return true;
		} else {
			if (item->descriptorType == typeFSS || item->descriptorType == typeFSRef) {
				NavFileOrFolderInfo* folderInfo = (NavFileOrFolderInfo *) info;
				NavigationFilter *filter = service->filters.get(service->currentIndex);
				if (filter != NULL && !folderInfo->isFolder) {
					CFStringRef filename;
					// OSType fileType = folderInfo->fileAndFolder.fileInfo.finderInfo.fdType;
					if (item->descriptorType == typeFSS) {
						filename = CFStringCreateWithPascalString(NULL, ((FSSpec *) *item->dataHandle)->name, kCFStringEncodingUTF8);
					} else if ((item->descriptorType = typeFSRef)) {
						HFSUniStr255 uname;
						if (!FSGetCatalogInfo((FSRef *) *item->dataHandle, kFSCatInfoNone, NULL, &uname, NULL, NULL)) {
							filename = CFStringCreateWithCharacters(NULL, uname.unicode, uname.length);
						}
					}
					if (filename != NULL) {
						bool match = filter->match(filename);
						CFRelease(filename);
						return match;
					}
				} else {
					return true;
				}
			}
			return true;
		}
	}
};

#endif

/*
 * java.io.File nativeFileDialog(java.lang.String message, java.lang.String filter, java.io.File directory, java.lang.String filename, boolean open)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_Dialog_nativeFileDialog(JNIEnv *env, jclass cls, jstring message, jstring filter, jobject directory, jstring filename, jboolean open) {
	jobject ret = NULL;
	try {
#ifdef MAC_ENV
		ret = NavigationService::execute(env, open ? FileOpen : FileSave, message, filename, filter, directory, NULL);
#else
		// Unicode seems to not work (at least not on Windows?)
		// So use normal string instead...
		char *msg = gEngine->convertString(env, message);
		char *fltr = gEngine->convertString(env, filter);
		char *name = gEngine->convertString(env, filename);

		ADMPlatformFileTypesSpecification3 specs;
		// This is needed in order to zero out the mac specific part on mac...
		memset(&specs, 0, sizeof(ADMPlatformFileTypesSpecification3));
		memcpy(specs.filter, fltr, MIN(kADMMaxFilterLength, env->GetStringLength(filter)));

		SPPlatformFileSpecification result, dir;
		SPPlatformFileSpecification *initial = gEngine->convertFile(env, directory, &dir);
		if (open ? 
			sADMBasic->StandardGetFileDialog(msg, &specs, initial, name, &result) :
			sADMBasic->StandardPutFileDialog(msg, &specs, initial, name, &result)) {
			ret = gEngine->convertFile(env, &result);
		}

		delete msg;
		delete fltr;
		if (name != NULL)
			delete name;
#endif
	} EXCEPTION_CONVERT(env);
	return ret;
}

/*
 * java.io.File chooseDirectory(java.lang.String message, java.io.File directory)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_Dialog_chooseDirectory(JNIEnv *env, jclass cls, jstring message, jobject directory) {
	jobject ret = NULL;
	try {
#ifdef MAC_ENV
		ret = NavigationService::execute(env, FolderChoose, message, NULL, NULL, directory, NULL);
#else
		// Unicode seems to not work (at least not on Windows?)
		// So use normal string instead...
		char *msg = gEngine->convertString(env, message);
		SPPlatformFileSpecification result, dir;
		if (sADMBasic->StandardGetDirectoryDialog(msg, gEngine->convertFile(env, directory, &dir), &result))
			ret = gEngine->convertFile(env, &result);
		
//		ASUnicode *msg = gEngine->convertString_ASUnicode(env, message);
//		if (sADMBasic->StandardGetDirectoryDialogW(msg, gEngine->convertFile(env, directory, &dir), &result))
//			ret = gEngine->convertFile(env, &result);
		delete msg;
#endif
	} EXCEPTION_CONVERT(env);
	return ret;
}

/*
 * java.awt.Color chooseColor(com.scriptographer.ui.Point point, java.awt.Color color)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_Dialog_chooseColor(JNIEnv *env, jclass cls, jobject point, jobject color) {
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
		if (sADMBasic->ChooseColor(pt, &col, &result))
			return gEngine->convertColor(env, &result);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * java.awt.Rectangle getPaletteLayoutBounds()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_Dialog_getPaletteLayoutBounds(JNIEnv *env, jclass cls) {
	try {
		ADMRect bounds;
		if (sADMBasic->GetPaletteLayoutBounds(&bounds))
			return gEngine->convertRectangle(env, &bounds);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void alert(java.lang.String message)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_alert(JNIEnv *env, jclass cls, jstring message) {
	try {
		ASUnicode *text = gEngine->convertString_ASUnicode(env, message);
		sADMBasic->MessageAlertW(text);
		delete text;
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean confirm(java.lang.String message)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ui_Dialog_confirm(JNIEnv *env, jclass cls, jstring message) {
	try {
		ASUnicode *text = gEngine->convertString_ASUnicode(env, message);
		ADMAnswer ret = sADMBasic->YesNoAlertW(text);
		delete text;
		return ret == kADMYesAnswer;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * com.scriptographer.ui.Size getScreenSize()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_Dialog_getScreenSize(JNIEnv *env, jclass cls) {
	try {
		// TODO: add multi screen support and place where Illustrator resides?
#ifdef MAC_ENV
		GDHandle screen = DMGetFirstScreenDevice(true);  
		Rect rect = (*screen)->gdRect;
		return gEngine->convertSize(env, rect.right - rect.left, rect.bottom - rect.top);
#endif
#ifdef WIN_ENV
		RECT rect;
		GetWindowRect(GetDesktopWindow(), &rect);
		return gEngine->convertSize(env, rect.right - rect.left, rect.bottom - rect.top);
#endif
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * int getWindowHandle()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ui_Dialog_getWindowHandle(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		ADMWindowRef window = sADMDialog->GetWindowRef(dialog);
#ifdef MAC_ENV
		// As new items will be created in the root control (as returned by HIViewGetRoot too),
		// make sure a root exists. This is required by SWT.
		ControlRef root;
		GetRootControl(window, &root);
		if (root == NULL)
			CreateRootControl(window, &root);
#endif
		return (jint) window;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void dumpControlHierarchy(java.io.File file)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_dumpControlHierarchy(JNIEnv *env, jobject obj, jobject file) {
	try {
#ifdef MAC_ENV
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		ADMWindowRef window = sADMDialog->GetWindowRef(dialog);
		SPPlatformFileSpecification fsSpec;
		gEngine->convertFile(env, file, &fsSpec);
		DumpControlHierarchy(window, (FSSpec*) &fsSpec);
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * void makeOverlay(int handle)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Dialog_makeOverlay(JNIEnv *env, jobject obj, jint handle) {
	try {
#ifdef MAC_ENV
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		WindowRef admWindow = sADMDialog->GetWindowRef(dialog);
		WindowRef swtWindow = GetControlOwner((ControlRef) handle);
		// WindowGroupRef groupRef = GetWindowGroup(admWindow);
		WindowGroupRef groupRef;
		CreateWindowGroup(/* kWindowGroupAttrLayerTogether |*/ kWindowGroupAttrHideOnCollapse, &groupRef);
		// SetWindowGroupParent(groupRef, GetWindowGroupOfClass(kOverlayWindowClass));
		SendWindowGroupBehind(groupRef, GetWindowGroupOfClass(kAlertWindowClass));
		SetWindowGroup(swtWindow, groupRef);
#endif
	} EXCEPTION_CONVERT(env);
}

ADMBoolean ADMAPI Dialog_onInvokeLater(ADMDialogRef dialog, ADMTimerRef timerId) {
	// Clear timer
	sADMDialog->AbortTimer(dialog, timerId);
	// Call run on runnable
	JNIEnv *env = gEngine->getEnv();
	try {
		jobject obj = gEngine->getDialogObject(dialog);
		gEngine->callVoidMethod(env, obj, gEngine->mid_ui_Dialog_onInvokeLater, (jint) timerId);
	} EXCEPTION_CATCH_REPORT(env);
	return true;
}

/*
 * int nativeInvokeLater()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ui_Dialog_nativeInvokeLater(JNIEnv *env, jobject obj) {
	try {
		// Execute a one-shot timer right after
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		DEFINE_CALLBACK_PROC(Dialog_onInvokeLater);
		ADMTimerRef timerId = sADMDialog->CreateTimer(dialog, 0, 0, (ADMDialogTimerProc) CALLBACK_PROC(Dialog_onInvokeLater), NULL, 0);
		return (jint) timerId;
	} EXCEPTION_CONVERT(env);
	return 0;
}
