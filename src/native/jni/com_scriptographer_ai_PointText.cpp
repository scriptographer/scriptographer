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
#include "com_scriptographer_ai_PointText.h"

/*
 * com.scriptographer.ai.PointText
 */

/*
 * int nativeCreate(int orientation, double x, double y)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_PointText_nativeCreate(JNIEnv *env, jclass cls, jint orientation, jdouble x, jdouble y) {
	AIArtHandle art = NULL;
	try {
		AIRealPoint pt;
		gEngine->convertPoint(env, kArtboardCoordinates, x, y, &pt);
		
		short paintOrder;
		AIArtHandle artInsert = Item_getInsertionPoint(&paintOrder);
		sAITextFrame->NewPointText(paintOrder, artInsert, (AITextOrientation) orientation, pt, &art);
		if (art == NULL)
			throw new StringException("Unable to create text object. Please make sure there is an open document.");
		
	} EXCEPTION_CONVERT(env);
	return (jint) art;
}

/*
 * com.scriptographer.ai.Point getPoint()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_PointText_getPoint(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle text = gEngine->getArtHandle(env, obj);
		AIRealPoint anchor;
		if (!sAITextFrame->GetPointTextAnchor(text, &anchor))
			return gEngine->convertPoint(env, kArtboardCoordinates, &anchor);		
	} EXCEPTION_CONVERT(env);
	return NULL;
}
