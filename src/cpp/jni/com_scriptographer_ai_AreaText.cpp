#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_AreaText.h"

/*
 * com.scriptographer.ai.AreaText
 */

/*
 * long nativeCreate(int docHandle, int orient, int artHandle)
 */
JNIEXPORT jlong JNICALL Java_com_scriptographer_ai_AreaText_nativeCreate(JNIEnv *env, jclass cls, jint docHandle, jint orient, jint artHandle) {
	AIArtHandle art = NULL;

	CREATEART_BEGIN

	AIArtHandle artLayer = Layer_beginCreateArt();
	sAITextFrame->NewInPathText(artLayer != NULL ? kPlaceInsideOnTop : kPlaceAboveAll, artLayer, (AITextOrientation) orient, (AIArtHandle) artHandle, NULL, false, &art);
	if (art == NULL)
		throw new StringException("Cannot create text object. Please make sure there is an open document.");

	CREATEART_END

	return (jlong) art;
}

/*
 * int getRowCount()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_AreaText_getRowCount(JNIEnv *env, jobject obj) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		ASInt32 count;
		if (!ATE::sTextFrame->GetRowCount(frame, &count))
			return count;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void setRowCount(int count)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_AreaText_setRowCount(JNIEnv *env, jobject obj, jint count) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		ATE::sTextFrame->SetRowCount(frame, count);
	} EXCEPTION_CONVERT(env)
}

/*
 * int getColumnCount()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_AreaText_getColumnCount(JNIEnv *env, jobject obj) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		ASInt32 count;
		if (!ATE::sTextFrame->GetColumnCount(frame, &count))
			return count;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void setColumnCount(int count)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_AreaText_setColumnCount(JNIEnv *env, jobject obj, jint count) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		ATE::sTextFrame->SetColumnCount(frame, count);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean getRowMajorOrder()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_AreaText_getRowMajorOrder(JNIEnv *env, jobject obj) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		bool rowMajorOrder;
		if (!ATE::sTextFrame->GetRowMajorOrder(frame, &rowMajorOrder))
			return rowMajorOrder;
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setRowMajorOrder(boolean rowMajorOrder)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_AreaText_setRowMajorOrder(JNIEnv *env, jobject obj, jboolean rowMajorOrder) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		ATE::sTextFrame->SetRowMajorOrder(frame, rowMajorOrder);
	} EXCEPTION_CONVERT(env)
}

/*
 * float getRowGutter()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_AreaText_getRowGutter(JNIEnv *env, jobject obj) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		ASReal gutter;
		if (!ATE::sTextFrame->GetRowGutter(frame, &gutter))
			return gutter;
	} EXCEPTION_CONVERT(env)
	return 0.0;
}

/*
 * void setRowGutter(float gutter)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_AreaText_setRowGutter(JNIEnv *env, jobject obj, jfloat gutter) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		ATE::sTextFrame->SetRowGutter(frame, gutter);
	} EXCEPTION_CONVERT(env)
}

/*
 * float getColumnGutter()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_AreaText_getColumnGutter(JNIEnv *env, jobject obj) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		ASReal gutter;
		if (!ATE::sTextFrame->GetColumnGutter(frame, &gutter))
			return gutter;
	} EXCEPTION_CONVERT(env)
	return 0.0;
}

/*
 * void setColumnGutter(float gutter)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_AreaText_setColumnGutter(JNIEnv *env, jobject obj, jfloat gutter) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		ATE::sTextFrame->SetColumnGutter(frame, gutter);
	} EXCEPTION_CONVERT(env)
}
