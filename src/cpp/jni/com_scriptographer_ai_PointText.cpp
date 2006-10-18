/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile: com_scriptographer_ai_PointText.cpp,v $
 * $Author: lehni $
 * $Revision: 1.5 $
 * $Date: 2006/10/18 14:17:17 $
 */

#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_PointText.h"

/*
 * com.scriptographer.ai.PointText
 */

/*
 * int nativeCreate(int orient, float x, float y)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_PointText_nativeCreate(JNIEnv *env, jclass cls, jint orient, jfloat x, jfloat y) {
	AIArtHandle art = NULL;

	DEFINE_POINT(pt, x, y);

	short paintOrder;
	AIArtHandle artInsert = Art_getInsertionPoint(&paintOrder);
	sAITextFrame->NewPointText(paintOrder, artInsert, (AITextOrientation) orient, pt, &art);
	if (art == NULL)
		throw new StringException("Cannot create text object. Please make sure there is an open document.");

	return (jint) art;
}

/*
 * com.scriptographer.ai.Point getAnchor()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_PointText_getAnchor(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle text = gEngine->getArtHandle(env, obj);
		AIRealPoint anchor;
		if (!sAITextFrame->GetPointTextAnchor(text, &anchor))
			return gEngine->convertPoint(env, &anchor);		
	} EXCEPTION_CONVERT(env);
	return NULL;
}
