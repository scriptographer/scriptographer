/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile: com_scriptographer_ai_View.cpp,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/04/08 21:56:40 $
 */

#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "admGlobals.h"
#include "com_scriptographer_ai_View.h"

/*
 * com.scriptographer.ai.View
 */

/*
 * com.scriptographer.ai.Rectangle getBounds()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_View_getBounds(JNIEnv *env, jobject obj) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		AIRealRect rect;
		sAIDocumentView->GetDocumentViewBounds(view, &rect);
		return gEngine->convertRectangle(env, &rect);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.ai.Point getCenter()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_View_getCenter(JNIEnv *env, jobject obj) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		AIRealPoint point;
		sAIDocumentView->GetDocumentViewCenter(view, &point);
		return gEngine->convertPoint(env, &point);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void setCenter(float x, float y)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_View_setCenter(JNIEnv *env, jobject obj, jfloat x, jfloat y) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		DEFINE_POINT(point, x, y);
		sAIDocumentView->SetDocumentViewCenter(view, &point);
	} EXCEPTION_CONVERT(env)
}

/*
 * float getZoom()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_View_getZoom(JNIEnv *env, jobject obj) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		AIReal zoom = 0;
		sAIDocumentView->GetDocumentViewZoom(view, &zoom);
		return zoom;
	} EXCEPTION_CONVERT(env)
	return 0.0;
}

/*
 * void setZoom(float zoom)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_View_setZoom(JNIEnv *env, jobject obj, jfloat zoom) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		sAIDocumentView->SetDocumentViewZoom(view, zoom);
	} EXCEPTION_CONVERT(env)
}


/*
 * Point artworkToView(float x, float y)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_View_artworkToView__FF(JNIEnv *env, jobject obj, jfloat x, jfloat y) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		DEFINE_POINT(pointIn, x, y);
		AIRealPoint pointOut;
		sAIDocumentView->FixedArtworkPointToViewPoint(view, &pointIn, &pointOut);
		return gEngine->convertPoint(env, &pointOut);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * Rectangle artworkToView(float x, float y, float width, float height)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_View_artworkToView__FFFF(JNIEnv *env, jobject obj, jfloat x, jfloat y, jfloat width, jfloat height) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		DEFINE_POINT(bottomLeftIn, x, y);
		DEFINE_POINT(topRightIn, x + width, y + height);
		AIRealPoint bottomLeftOut, topRightOut;
		sAIDocumentView->FixedArtworkPointToViewPoint(view, &bottomLeftIn, &bottomLeftOut);
		sAIDocumentView->FixedArtworkPointToViewPoint(view, &topRightIn, &topRightOut);
		return gEngine->convertRectangle(env, bottomLeftOut.h, topRightOut.v, topRightOut.h, bottomLeftOut.v);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.ai.Point viewToArtwork(float x, float y)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_View_viewToArtwork__FF(JNIEnv *env, jobject obj, jfloat x, jfloat y) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		DEFINE_POINT(pointIn, x, y);
		AIRealPoint pointOut;
		sAIDocumentView->FixedViewPointToArtworkPoint(view, &pointIn, &pointOut);
		return gEngine->convertPoint(env, &pointOut);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.ai.Rectangle viewToArtwork(float x, float y, float width, float height)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_View_viewToArtwork__FFFF(JNIEnv *env, jobject obj, jfloat x, jfloat y, jfloat width, jfloat height) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		DEFINE_POINT(bottomLeftIn, x, y);
		DEFINE_POINT(topRightIn, x + width, y + height);
		AIRealPoint bottomLeftOut, topRightOut;
		sAIDocumentView->FixedViewPointToArtworkPoint(view, &bottomLeftIn, &bottomLeftOut);
		sAIDocumentView->FixedViewPointToArtworkPoint(view, &topRightIn, &topRightOut);
		return gEngine->convertRectangle(env, bottomLeftOut.h, topRightOut.v, topRightOut.h, bottomLeftOut.v);
	} EXCEPTION_CONVERT(env)
	return NULL;
}


/*
 * void setScreenMode(int mode)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_View_setScreenMode(JNIEnv *env, jobject obj, jint mode) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		sAIDocumentView->SetScreenMode(view, (AIScreenMode) mode);
	} EXCEPTION_CONVERT(env)
}

/*
 * int getScreenMode()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_View_getScreenMode(JNIEnv *env, jobject obj) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		AIScreenMode mode = kNoScreenMode;
		sAIDocumentView->GetScreenMode(view, &mode);
		return (jint) mode;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * boolean isTemplateVisible()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_View_isTemplateVisible(JNIEnv *env, jobject obj) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		AIBoolean visible = false;
		sAIDocumentView->GetTemplateVisible(view, &visible);
		return visible;
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void scrollBy(float x, float y)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_View_scrollBy(JNIEnv *env, jobject obj, jfloat x, jfloat y) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		DEFINE_POINT(point, x, y);
		sAIDocumentView->DocumentViewScrollDelta(view, &point);
	} EXCEPTION_CONVERT(env)
}

/*
 * com.scriptographer.ai.Rectangle getUpdateRect()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_View_getUpdateRect(JNIEnv *env, jobject obj) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		AIRealRect rect;
		sAIDocumentView->GetDocumentViewInvalidRect(view, &rect);
		return gEngine->convertRectangle(env, &rect);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void invalidate(float x, float y, float width, float height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_View_invalidate(JNIEnv *env, jobject obj, jfloat x, jfloat y, jfloat width, jfloat height) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		DEFINE_RECT(rect, x, y, width, height);
		sAIDocumentView->SetDocumentViewInvalidRect(view, &rect);
	} EXCEPTION_CONVERT(env)
}

/*
 * int getStyle()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_View_getStyle(JNIEnv *env, jobject obj) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		short style;
		sAIDocumentView->GetDocumentViewStyle(view, &style);
		return style;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * boolean getShowPageTiling()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_View_getShowPageTiling(JNIEnv *env, jobject obj) {
	try {
		ASBoolean show = false;
		sAIDocumentView->GetShowPageTiling(&show);
		return show;
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setShowPageTiling(boolean show)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_View_setShowPageTiling(JNIEnv *env, jobject obj, jboolean show) {
	sAIDocumentView->SetShowPageTiling(show);
}

/*
 * boolean getShowGrid()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_View_getShowGrid(JNIEnv *env, jobject obj) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		ASBoolean show, snap;
		sAIDocumentView->GetGridOptions(view, &show, &snap);
		return show;
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * boolean getSnapGrid()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_View_getSnapGrid(JNIEnv *env, jobject obj) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		ASBoolean show, snap;
		sAIDocumentView->GetGridOptions(view, &show, &snap);
		return snap;
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setShowGrid(boolean show)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_View_setShowGrid(JNIEnv *env, jobject obj, jboolean show) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		ASBoolean curShow, curSnap;
		sAIDocumentView->GetGridOptions(view, &curShow, &curSnap);
		sAIDocumentView->SetGridOptions(view, show, curSnap);
	} EXCEPTION_CONVERT(env)
}

/*
 * void setSnapGrid(boolean snap)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_View_setSnapGrid(JNIEnv *env, jobject obj, jboolean snap) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		ASBoolean curShow, curSnap;
		sAIDocumentView->GetGridOptions(view, &curShow, &curSnap);
		sAIDocumentView->SetGridOptions(view, curShow, snap);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean getShowTransparencyGrid()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_View_getShowTransparencyGrid(JNIEnv *env, jobject obj) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		ASBoolean show = false;
		sAIDocumentView->GetShowTransparencyGrid(view, &show);
		return show;
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setShowTransparencyGrid(boolean show)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_View_setShowTransparencyGrid(JNIEnv *env, jobject obj, jboolean show) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		sAIDocumentView->SetShowTransparencyGrid(view, show);
	} EXCEPTION_CONVERT(env)
}

/*
 * int nativeGetDocument()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_View_nativeGetDocument(JNIEnv *env, jobject obj) {
	try {
		AIDocumentViewHandle view = gEngine->getDocumentViewHandle(env, obj);
		AIDocumentHandle doc = NULL;
		sAIDocumentView->GetDocumentViewDocument(view, &doc);
		return (jint) doc;
	} EXCEPTION_CONVERT(env)
	return 0;
}
