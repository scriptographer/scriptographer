#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_adm_Key.h"

/*
 * com.scriptographer.adm.Key
 */

/*
 * boolean isDown(short keycode)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_Key_isDown(JNIEnv *env, jclass cls, jshort keycode) {
	try {
		return gEngine->isKeyDown(keycode);
	} EXCEPTION_CONVERT(env);
	return false;
}
