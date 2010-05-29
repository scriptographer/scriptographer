/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
 *
 * -- GPL LICENSE NOTICE --
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -- GPL LICENSE NOTICE --
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
 * int nativeCreate(int orientation, int artHandle, float x, float y)
 *
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_PathText_nativeCreate__IIFF(JNIEnv *env, jclass cls, jint orientation, jint artHandle, jfloat x, jfloat y) {
	AIArtHandle art = NULL;

	short paintOrder;
	AIArtHandle artInsert = Item_getInsertionPoint(&paintOrder);
	DEFINE_POINT(pt, x, y);
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
