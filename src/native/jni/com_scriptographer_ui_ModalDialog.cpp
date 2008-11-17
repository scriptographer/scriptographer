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
 * $Id: com_scriptographer_ui_ModalDialog.cpp 578 2008-07-22 21:15:16Z lehni $
 */

#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "ScriptographerPlugin.h"
#include "com_scriptographer_ui_ModalDialog.h"

/*
 * com.scriptographer.ui.ModalDialog
 */

/*
 * com.scriptographer.ui.Item nativeDoModal()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_ModalDialog_nativeDoModal(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		sADMDialog->Show(dialog, true);
		int id = env->IsInstanceOf(obj, gEngine->cls_ui_PopupDialog) ? sADMDialog->DisplayAsPopupModal(dialog) : sADMDialog->DisplayAsModal(dialog);
		ADMItemRef item = sADMDialog->GetItem(dialog, id);
		sADMDialog->Show(dialog, false);
		if (item != NULL)
			return gEngine->getItemObject(item);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void endModal()
 */

JNIEXPORT void JNICALL Java_com_scriptographer_ui_ModalDialog_endModal(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		sADMDialog->EndModal(dialog, sADMDialog->GetCancelItemID(dialog), true);
	} EXCEPTION_CONVERT(env);
}

/*
 *
 */
ADMBoolean ADMAPI ModalDialog_fixModal(ADMDialogRef dialog, ADMTimerRef timerID) {
	// Clear timer
	sADMDialog->AbortTimer(dialog, timerID);
	if (!sADMDialog->IsVisible(dialog))
		sADMDialog->Activate(dialog, false);
	return true;
}

/*
 * void fixModal()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_ModalDialog_fixModal(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		// Execute a one-shot timer that deactivates a invisible modal dialog
		// right after it was accidentaly activated by a Illustrator CS3 bug.
		// Immediately deactivating it does not work.
		DEFINE_CALLBACK_PROC(ModalDialog_fixModal);
		sADMDialog->CreateTimer(dialog, 0, 0, (ADMDialogTimerProc) CALLBACK_PROC(ModalDialog_fixModal), NULL, 0);
	} EXCEPTION_CONVERT(env);
}
