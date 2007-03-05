#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_GradientStopList.h"

/*
 * com.scriptographer.ai.GradientStopList
 */

/*
 * void nativeGet(int handle, int index, com.scriptographer.ai.GradientStop stop)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_GradientStopList_nativeGet(JNIEnv *env, jclass cls, jint handle, jint index, jobject stop) {
	try {
		AIGradientStop s;
		if (sAIGradient->GetNthGradientStop((AIGradientHandle) handle, index, &s))
			throw new StringException("Cannot get gradient stop");
		jobject color = gEngine->convertColor(env, &s.color);
		gEngine->callVoidMethod(env, stop, gEngine->mid_GradientStop_init, s.midPoint, s.rampPoint, color);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSet(int handle, int docHandle, int index, float midPoint, float rampPoint, float[] color)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_GradientStopList_nativeSet(JNIEnv *env, jclass cls, jint handle, jint docHandle, jint index, jfloat midPoint, jfloat rampPoint, jfloatArray color) {
	try {
		AIGradientStop s;
		s.midPoint = midPoint;
		s.rampPoint = rampPoint;
		gEngine->convertColor(env, color, &s.color);
		if (sAIGradient->SetNthGradientStop((AIGradientHandle) handle, index, &s))
			throw new StringException("Cannot set gradient stop");
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeInsert(int handle, int docHandle, int index, float midPoint, float rampPoint, float[] color)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_GradientStopList_nativeInsert(JNIEnv *env, jclass cls, jint handle, jint docHandle, jint index, jfloat midPoint, jfloat rampPoint, jfloatArray color) {
	try {
		AIGradientStop s;
		s.midPoint = midPoint;
		s.rampPoint = rampPoint;
		gEngine->convertColor(env, color, &s.color);
		if (sAIGradient->InsertGradientStop((AIGradientHandle) handle, index, &s))
			throw new StringException("Cannot insert gradient stop");
	} EXCEPTION_CONVERT(env);
}

/*
 * int nativeGetSize(int handle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_GradientStopList_nativeGetSize(JNIEnv *env, jclass cls, jint handle) {
	try {
		short count = 0;
		sAIGradient->GetGradientStopCount((AIGradientHandle) handle, &count);
		return count;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeRemove(int handle, int docHandle, int fromIndex, int toIndex)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_GradientStopList_nativeRemove(JNIEnv *env, jclass cls, jint handle, jint docHandle, jint fromIndex, jint toIndex) {
	try {
		for (int i = toIndex - 1; i >= fromIndex; i--) {
			// TODO: we might pass NULL instead of &s (verify)
			AIGradientStop s;
			sAIGradient->DeleteGradientStop((AIGradientHandle) handle, i, &s);
		}
		short count = 0;
		sAIGradient->GetGradientStopCount((AIGradientHandle) handle, &count);
		return count;
	} EXCEPTION_CONVERT(env);
	return 0;
}
