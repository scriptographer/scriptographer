#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_adm_ToggleItem.h"

/*
 * com.scriptographer.adm.ToggleItem
 */

/*
 * boolean isChecked()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_ToggleItem_isChecked(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		return sADMItem->GetBooleanValue(item);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setChecked(boolean checked)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ToggleItem_setChecked(JNIEnv *env, jobject obj, jboolean checked) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetBooleanValue(item, checked);
	} EXCEPTION_CONVERT(env)
}
