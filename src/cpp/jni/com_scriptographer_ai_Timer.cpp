#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "Plugin.h"
#include "com_scriptographer_ai_Timer.h"

/*
 * com.scriptographer.ai.Timer
 */

/*
 * int nativeCreate(java.lang.String name, int period)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Timer_nativeCreate(JNIEnv *env, jobject obj, jstring name, jint period) {
	try {
		char *str = gEngine->createCString(env, name);
		AITimerHandle timer;
		sAITimer->AddTimer(gPlugin->getPluginRef(), str, period, &timer);
		return (jint) timer;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * boolean nativeSetActive(int handle, boolean active)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Timer_nativeSetActive(JNIEnv *env, jobject obj, jint handle, jboolean active) {
	try {
		return !sAITimer->SetTimerActive((AITimerHandle) handle, active);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * boolean nativeSetPeriod(int handle, int period)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Timer_nativeSetPeriod(JNIEnv *env, jobject obj, jint handle, jint period) {
	try {
		return !sAITimer->SetTimerPeriod((AITimerHandle) handle, period);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * java.util.ArrayList nativeGetTimers()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Timer_nativeGetTimers(JNIEnv *env, jclass cls) {
	try {
		jobject array = gEngine->newObject(env, gEngine->cls_ArrayList, gEngine->cid_ArrayList);
		long count;
		sAITimer->CountTimers(&count);
		SPPluginRef plugin = gPlugin->getPluginRef();
		for (int i = 0; i < count; i++) {
			AITimerHandle timer;
			SPPluginRef timerPlugin;
			if (!sAITimer->GetNthTimer(i, &timer) &&
				!sAITimer->GetTimerPlugin(timer, &timerPlugin) &&
				plugin == timerPlugin) {
				// create the wrapper
				jobject timerObj = gEngine->newObject(env, gEngine->cls_Timer, gEngine->cid_Timer, (jint) timer);
				// and add it to the array
				gEngine->callObjectMethod(env, array, gEngine->mid_Collection_add, timerObj);
			}
		}
		return array;
	} EXCEPTION_CONVERT(env)
	return NULL;
}
