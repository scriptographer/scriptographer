#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_Test.h"

/*
 * com.scriptographer.Test
 */

/*
 * int testExternal(int arg1, int arg2, int arg3, int arg4, int arg5, int arg6)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_Test_testExternal(JNIEnv *env, jclass cls, jint arg1, jint arg2, jint arg3, jint arg4, jint arg5, jint arg6) {
//	try {
		return arg1 + arg2 + arg3 + arg4 + arg5 + arg6;
//	} EXCEPTION_CONVERT(env)
//	return 0;
}
