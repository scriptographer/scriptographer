/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.com/ for updates and contact.
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
 *
 * $Id$
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
		throw new StringException("Cannot create text object. Please make sure there is an open document.");

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
		throw new StringException("Cannot create text object. Please make sure there is an open document.");

	return (jint) art;
}
*/

/*
 * float[] getPathRange()
 */
JNIEXPORT jfloatArray JNICALL Java_com_scriptographer_ai_PathText_getPathRange(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIReal start, end;
		if (!sAITextFrame->GetOnPathTextTRange(art, &start, &end)) {
			// create a float array with these values:
			jfloatArray res = env->NewFloatArray(2);
			jfloat range[] = {
				start, end
			};
			env->SetFloatArrayRegion(res, 0, 2, range);
			return res;
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setPathRange(float start, float end)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_PathText_setPathRange(JNIEnv *env, jobject obj, jfloat start, jfloat end) {
	try {
		// suspend reflow by passing true here
	    AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		sAITextFrame->SetOnPathTextTRange(art, start, end);
	} EXCEPTION_CONVERT(env);
}
