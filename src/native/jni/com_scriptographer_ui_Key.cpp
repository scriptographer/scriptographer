#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "ScriptographerPlugin.h"
#include "com_scriptographer_ui_Key.h"

/*
 * com.scriptographer.ui.Key
 */

/*
 * boolean nativeIsDown(int keycode)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ui_Key_nativeIsDown(JNIEnv *env, jclass cls, jint keycode) {
	try {
		return gPlugin->isKeyDown(keycode);
	} EXCEPTION_CONVERT(env);
	return false;
}
