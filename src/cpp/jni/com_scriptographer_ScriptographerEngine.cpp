#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ScriptographerEngine.h"

/*
 * com.scriptographer.ScriptographerEngine
 */

/*
 * java.lang.String reload()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ScriptographerEngine_reload(JNIEnv *env, jclass cls) {
	try {
		return gEngine->reloadEngine();
	} EXCEPTION_CONVERT(env)
	return NULL;
}
