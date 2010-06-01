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
#include "ScriptographerPlugin.h"
#include "com_scriptographer_adm_ModalDialog.h"

/*
 * com.scriptographer.adm.ModalDialog
 */

/*
 * com.scriptographer.adm.Item nativeDoModal()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_ModalDialog_nativeDoModal(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		sADMDialog->Show(dialog, true);
		int id = env->IsInstanceOf(obj, gEngine->cls_adm_PopupDialog) ? sADMDialog->DisplayAsPopupModal(dialog) : sADMDialog->DisplayAsModal(dialog);
		ADMItemRef item = sADMDialog->GetItem(dialog, id);
		sADMDialog->Activate(dialog, false);
		sADMDialog->Show(dialog, false);
		if (item != NULL)
			return gEngine->getItemObject(item);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void endModal()
 */

JNIEXPORT void JNICALL Java_com_scriptographer_adm_ModalDialog_endModal(JNIEnv *env, jobject obj) {
	try {
		ADMDialogRef dialog = gEngine->getDialogHandle(env, obj);
		sADMDialog->EndModal(dialog, sADMDialog->GetCancelItemID(dialog), true);
	} EXCEPTION_CONVERT(env);
}
