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
