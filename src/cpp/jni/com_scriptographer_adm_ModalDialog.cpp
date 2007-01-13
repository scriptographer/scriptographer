/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile$
 * $Author$
 * $Revision$
 * $Date$
 */

#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_adm_ModalDialog.h"

/*
 * com.scriptographer.adm.ModalDialog
 */

/*
 * com.scriptographer.adm.Item doModal()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_ModalDialog_doModal(JNIEnv *env, jobject obj) {
	jobject res = NULL;
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		gEngine->setBooleanField(env, obj, gEngine->fid_ModalDialog_doesModal, true);
		sADMDialog->Show(dialog, true);
		int id = env->IsInstanceOf(obj, gEngine->cls_PopupDialog) ? sADMDialog->DisplayAsPopupModal(dialog) : sADMDialog->DisplayAsModal(dialog);
		ADMItemRef item = sADMDialog->GetItem(dialog, id);
		if (item != NULL) {
			res = gEngine->getItemObject(item);
		}
		sADMDialog->Show(dialog, false);
	} EXCEPTION_CONVERT(env);
	// finally set back the doesModal variable to false:
	try {
		gEngine->setBooleanField(env, obj, gEngine->fid_ModalDialog_doesModal, false);
	} EXCEPTION_CONVERT(env);
	return res;
}

/*
 * void endModal()
 */

JNIEXPORT void JNICALL Java_com_scriptographer_adm_ModalDialog_endModal(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->EndModal(dialog, sADMDialog->GetCancelItemID(dialog), true);
		gEngine->setBooleanField(env, obj, gEngine->fid_ModalDialog_doesModal, false);
	} EXCEPTION_CONVERT(env);
}
