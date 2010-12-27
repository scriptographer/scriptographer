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
#include "aiGlobals.h"
#include "com_scriptographer_ai_PathText.h"

/*
 * com.scriptographer.ai.PathText
 */

/*
 * int nativeCreate(int orientation, int artHandle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_PathText_nativeCreate(JNIEnv *env, jclass cls, jint orientation, jint artHandle) {
	AIArtHandle art = NULL;

	short paintOrder;
	AIArtHandle artInsert = Item_getInsertionPoint(&paintOrder);
	sAITextFrame->NewOnPathText(paintOrder, artInsert, (AITextOrientation) orientation, (AIArtHandle) artHandle, 0, -1, NULL, false, &art);
	if (art == NULL)
		throw new StringException("Unable to create text object. Please make sure there is an open document.");

	return (jint) art;
}

/*
 * int nativeCreate(int orientation, int artHandle, double x, double y)
 *
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_PathText_nativeCreate__IIFF(JNIEnv *env, jclass cls, jint orientation, jint artHandle, jdouble x, jdouble y) {
	AIArtHandle art = NULL;

	short paintOrder;
	AIArtHandle artInsert = Item_getInsertionPoint(&paintOrder);
	AIRealPoint pt;
	gEngine->convertPoint(env, kArtboardCoordinates, x, y, &pt);
	sAITextFrame->NewOnPathText2(paintOrder, artInsert, (AITextOrientation) orientation, (AIArtHandle) artHandle, pt, NULL, false, &art);
	if (art == NULL)
		throw new StringException("Unable to create text object. Please make sure there is an open document.");

	return (jint) art;
}
*/

/*
 * double[] nativeGetPathOffsets()
 */
JNIEXPORT jdoubleArray JNICALL Java_com_scriptographer_ai_PathText_nativeGetPathOffsets(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIReal start, end;
		if (!sAITextFrame->GetOnPathTextTRange(art, &start, &end)) {
			// Create a float array with these values:
			jdoubleArray res = env->NewDoubleArray(2);
			jdouble range[] = {
				start, end
			};
			env->SetDoubleArrayRegion(res, 0, 2, range);
			return res;
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void nativeSetPathOffsets(double start, double end)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_PathText_nativeSetPathOffsets(JNIEnv *env, jobject obj, jdouble start, jdouble end) {
	try {
		// Suspend reflow by passing true here
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		sAITextFrame->SetOnPathTextTRange(art, start, end);
	} EXCEPTION_CONVERT(env);
}
