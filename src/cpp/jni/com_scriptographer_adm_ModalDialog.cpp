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
	} EXCEPTION_CONVERT(env)
	// finally set back the doesModal variable to false:
	try {
		gEngine->setBooleanField(env, obj, gEngine->fid_ModalDialog_doesModal, false);
	} EXCEPTION_CONVERT(env)
	return res;
}

/*
 * void endModal()
 */

void endModal(JNIEnv *env, jobject obj, ADMDialogRef dialog) {
	sADMDialog->EndModal(dialog, sADMDialog->GetCancelItemID(dialog), true);
	gEngine->setBooleanField(env, obj, gEngine->fid_ModalDialog_doesModal, false);
}

JNIEXPORT void JNICALL Java_com_scriptographer_adm_ModalDialog_endModal(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
	    endModal(env, obj, dialog);
	} EXCEPTION_CONVERT(env)
}

/*
 * void setVisible(boolean visible)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setVisible(JNIEnv *env, jobject obj, jboolean visible) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		if (!visible && gEngine->getBooleanField(env, obj, gEngine->fid_ModalDialog_doesModal)) {
		    endModal(env, obj, dialog);
		}
		sADMDialog->Show(dialog, visible);
	} EXCEPTION_CONVERT(env)
}
