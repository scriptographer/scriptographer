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
 * $RCSfile: com_scriptographer_adm_Drawer.cpp,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:58 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_adm_Drawer.h"

/*
 * com.scriptographer.adm.Drawer
 */

/*
 * void clear()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_clear(JNIEnv *env, jobject obj) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		sADMDrawer->Clear(drawer);
	} EXCEPTION_CONVERT(env)
}

/*
 * com.scriptographer.ai.Rectangle getBoundsRect()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Drawer_getBoundsRect(JNIEnv *env, jobject obj) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		sADMDrawer->GetBoundsRect(drawer, &rt);
		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.ai.Rectangle getClipRect()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Drawer_getClipRect(JNIEnv *env, jobject obj) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		sADMDrawer->GetClipRect(drawer, &rt);
		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void setClipRect(com.scriptographer.ai.Rectangle rect)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_setClipRect(JNIEnv *env, jobject obj, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		sADMDrawer->SetClipRect(drawer, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void intersectClipRect(com.scriptographer.ai.Rectangle arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_intersectClipRect(JNIEnv *env, jobject obj, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		sADMDrawer->IntersectClipRect(drawer, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void unionClipRect(com.scriptographer.ai.Rectangle arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_unionClipRect(JNIEnv *env, jobject obj, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		sADMDrawer->UnionClipRect(drawer, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void subtractClipRect(com.scriptographer.ai.Rectangle arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_subtractClipRect(JNIEnv *env, jobject obj, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		sADMDrawer->SubtractClipRect(drawer, &rt);
	} EXCEPTION_CONVERT(env)
}

inline ADMPoint *drawerGetPoints(JNIEnv *env, jobjectArray points, jint &length) {
	length = env->GetArrayLength(points);
	ADMPoint *pts = new ADMPoint[length];
	for (int i = 0; i < length; i++) {
		jobject pt = env->GetObjectArrayElement(points, i);
		gEngine->convertPoint(env, pt, &pts[i]); // checks exceptions...
	}
	return pts;
}

/*
 * void setClipPolygon(com.scriptographer.ai.Point[] arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_setClipPolygon(JNIEnv *env, jobject obj, jobjectArray points) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		jint length;
		ADMPoint *pts = drawerGetPoints(env, points, length);
		sADMDrawer->SetClipPolygon(drawer, pts, length);
		delete pts;
	} EXCEPTION_CONVERT(env)
}

/*
 * void intersectClipPolygon(com.scriptographer.ai.Point[] arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_intersectClipPolygon(JNIEnv *env, jobject obj, jobjectArray points) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		jint length;
		ADMPoint *pts = drawerGetPoints(env, points, length);
		sADMDrawer->IntersectClipPolygon(drawer, pts, length);
		delete pts;
	} EXCEPTION_CONVERT(env)
}

/*
 * void unionClipPolygon(com.scriptographer.ai.Point[] arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_unionClipPolygon(JNIEnv *env, jobject obj, jobjectArray points) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		jint length;
		ADMPoint *pts = drawerGetPoints(env, points, length);
		sADMDrawer->UnionClipPolygon(drawer, pts, length);
		delete pts;
	} EXCEPTION_CONVERT(env)
}

/*
 * void subtractClipPolygon(com.scriptographer.ai.Point[] arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_subtractClipPolygon(JNIEnv *env, jobject obj, jobjectArray points) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		jint length;
		ADMPoint *pts = drawerGetPoints(env, points, length);
		sADMDrawer->SubtractClipPolygon(drawer, pts, length);
		delete pts;
	} EXCEPTION_CONVERT(env)
}

/*
 * com.scriptographer.ai.Point getOrigin()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Drawer_getOrigin(JNIEnv *env, jobject obj) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMPoint pt;
		sADMDrawer->GetOrigin(drawer, &pt);
		return gEngine->convertPoint(env, &pt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void setOrigin(com.scriptographer.ai.Point arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_setOrigin(JNIEnv *env, jobject obj, jobject origin) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMPoint pt;
		gEngine->convertPoint(env, origin, &pt);
		sADMDrawer->SetOrigin(drawer, &pt);
	} EXCEPTION_CONVERT(env)
}

/*
 * java.awt.Color getColor()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Drawer_getColor(JNIEnv *env, jobject obj) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRGBColor col;
		sADMDrawer->GetRGBColor(drawer, &col);
		jobject res = gEngine->convertColor(env, &col);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void setColor(java.awt.Color arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_setColor__Ljava_awt_Color_2(JNIEnv *env, jobject obj, jobject color) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRGBColor col;
		gEngine->convertColor(env, color, &col);
		sADMDrawer->SetRGBColor(drawer, &col);
	} EXCEPTION_CONVERT(env)
}

/*
 * void setColor(int colorType)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_setColor__I(JNIEnv *env, jobject obj, jint colorType) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		sADMDrawer->SetADMColor(drawer, (ADMColor)colorType);
	} EXCEPTION_CONVERT(env)
}

/*
 * int getDrawMode()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Drawer_getDrawMode(JNIEnv *env, jobject obj) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		return sADMDrawer->GetDrawMode(drawer);
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void setDrawMode(int mode)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_setDrawMode(JNIEnv *env, jobject obj, jint mode) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		sADMDrawer->SetDrawMode(drawer, (ADMDrawMode)mode);
	} EXCEPTION_CONVERT(env)
}

/*
 * int getFont()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Drawer_getFont(JNIEnv *env, jobject obj) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		return sADMDrawer->GetFont(drawer);
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void setFont(int font)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_setFont(JNIEnv *env, jobject obj, jint font) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		sADMDrawer->SetFont(drawer, (ADMFont)font);
	} EXCEPTION_CONVERT(env)
}

/*
 * void drawLine(int x1, int y1, int x2, int y2)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_drawLine(JNIEnv *env, jobject obj, jint x1, jint y1, jint x2, jint y2) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMPoint pt1, pt2;
		pt1.h = x1;
		pt1.v = y1;
		pt2.h = x2;
		pt2.v = y2;
		sADMDrawer->DrawLine(drawer, &pt1, &pt2);
	} EXCEPTION_CONVERT(env)
}

/*
 * void drawPolygon(com.scriptographer.ai.Point[] points)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_drawPolygon(JNIEnv *env, jobject obj, jobjectArray points) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		jint length;
		ADMPoint *pts = drawerGetPoints(env, points, length);
		sADMDrawer->DrawPolygon(drawer, pts, length);
		delete pts;
	} EXCEPTION_CONVERT(env)
}

/*
 * void fillPolygon(com.scriptographer.ai.Point[] points)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_fillPolygon(JNIEnv *env, jobject obj, jobjectArray points) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		jint length;
		ADMPoint *pts = drawerGetPoints(env, points, length);
		sADMDrawer->FillPolygon(drawer, pts, length);
		delete pts;
	} EXCEPTION_CONVERT(env)
}

/*
 * void drawRect(com.scriptographer.ai.Rectangle rect)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_drawRect(JNIEnv *env, jobject obj, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		sADMDrawer->DrawRect(drawer, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void fillRect(com.scriptographer.ai.Rectangle rect)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_fillRect(JNIEnv *env, jobject obj, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		sADMDrawer->FillRect(drawer, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void clearRect(com.scriptographer.ai.Rectangle rect)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_clearRect(JNIEnv *env, jobject obj, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		sADMDrawer->ClearRect(drawer, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void drawSunkenRect(com.scriptographer.ai.Rectangle rect)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_drawSunkenRect(JNIEnv *env, jobject obj, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		sADMDrawer->DrawSunkenRect(drawer, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void drawRaisedRect(com.scriptographer.ai.Rectangle rect)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_drawRaisedRect(JNIEnv *env, jobject obj, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		sADMDrawer->DrawRaisedRect(drawer, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void invertRect(com.scriptographer.ai.Rectangle rect)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_invertRect(JNIEnv *env, jobject obj, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		sADMDrawer->InvertRect(drawer, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void drawOval(com.scriptographer.ai.Rectangle rect)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_drawOval(JNIEnv *env, jobject obj, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		sADMDrawer->DrawOval(drawer, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void fillOval(com.scriptographer.ai.Rectangle rect)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_fillOval(JNIEnv *env, jobject obj, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		sADMDrawer->FillOval(drawer, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void drawImage(com.scriptographer.adm.Image image, Lcom.scriptographer.ai.Point topLeft)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_drawImage(JNIEnv *env, jobject obj, jobject image, jobject topLeft) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMImageRef img = gEngine->getImageRef(env, image);
		ADMPoint pt;
		gEngine->convertPoint(env, topLeft, &pt);
		sADMDrawer->DrawADMImage(drawer, img, &pt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void drawImageCentered(com.scriptographer.adm.Image image, Lcom.scriptographer.ai.Rectangle rect)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_drawImageCentered(JNIEnv *env, jobject obj, jobject image, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMImageRef img = gEngine->getImageRef(env, image);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		sADMDrawer->DrawADMImageCentered(drawer, img, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void drawRecoloredImage(com.scriptographer.adm.Image image, Lcom.scriptographer.ai.Point topLeft, int style)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_drawRecoloredImage(JNIEnv *env, jobject obj, jobject image, jobject topLeft, jint style) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMImageRef img = gEngine->getImageRef(env, image);
		ADMIconRef icn = (ADMIconRef)gEngine->callIntMethod(env, obj, gEngine->mid_Image_getIconRef);
		if (icn == NULL) throw new StringException("Cannot create icon from image.");
		ADMPoint pt;
		gEngine->convertPoint(env, topLeft, &pt);
		sADMDrawer->DrawRecoloredIcon(drawer, icn, &pt, (ADMRecolorStyle)style);
	} EXCEPTION_CONVERT(env)
}

/*
 * void drawRecoloredImageCentered(com.scriptographer.adm.Image image, Lcom.scriptographer.ai.Rectangle rect, int style)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_drawRecoloredImageCentered(JNIEnv *env, jobject obj, jobject image, jobject rect, jint style) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMImageRef img = gEngine->getImageRef(env, image);
		ADMIconRef icn = (ADMIconRef)gEngine->callIntMethod(env, obj, gEngine->mid_Image_getIconRef);
		if (icn == NULL) throw new StringException("Cannot create icon from image.");
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		sADMDrawer->DrawRecoloredIconCentered(drawer, icn, &rt, (ADMRecolorStyle)style);
	} EXCEPTION_CONVERT(env)
}

/*
 * int getTextWidth(java.lang.String text)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Drawer_getTextWidth(JNIEnv *env, jobject obj, jstring text) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		const jchar *chars = env->GetStringChars(text, NULL);
		if (chars == NULL) EXCEPTION_CHECK(env)
		int width = sADMDrawer->GetTextWidthW(drawer, chars);
		env->ReleaseStringChars(text, chars);
		return width;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * com.scriptographer.adm.Drawer$FontInfo getFontInfo()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Drawer_getFontInfo__(JNIEnv *env, jobject obj) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
// TODO: define getFontInfo
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.adm.Drawer$FontInfo getFontInfo(int font)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Drawer_getFontInfo__I(JNIEnv *env, jobject obj, jint font) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
// TODO: define getFontInfo
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void drawText(java.lang.String text, Lcom.scriptographer.ai.Point topLeft)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_drawText(JNIEnv *env, jobject obj, jstring text, jobject topLeft) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMPoint pt;
		gEngine->convertPoint(env, topLeft, &pt);
		const jchar *chars = env->GetStringChars(text, NULL);
		if (chars == NULL) EXCEPTION_CHECK(env)
		sADMDrawer->DrawTextW(drawer, chars, &pt);
		env->ReleaseStringChars(text, chars);
	} EXCEPTION_CONVERT(env)
}

/*
 * void drawTextLeft(java.lang.String text, Lcom.scriptographer.ai.Rectangle rect)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_drawTextLeft(JNIEnv *env, jobject obj, jstring text, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		const jchar *chars = env->GetStringChars(text, NULL);
		if (chars == NULL) EXCEPTION_CHECK(env)
		sADMDrawer->DrawTextLeftW(drawer, chars, &rt);
		env->ReleaseStringChars(text, chars);
	} EXCEPTION_CONVERT(env)
}

/*
 * void drawTextCentered(java.lang.String text, Lcom.scriptographer.ai.Rectangle rect)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_drawTextCentered(JNIEnv *env, jobject obj, jstring text, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		const jchar *chars = env->GetStringChars(text, NULL);
		if (chars == NULL) EXCEPTION_CHECK(env)
		sADMDrawer->DrawTextCenteredW(drawer, chars, &rt);
		env->ReleaseStringChars(text, chars);
	} EXCEPTION_CONVERT(env)
}

/*
 * void drawTextRight(java.lang.String text, Lcom.scriptographer.ai.Rectangle rect)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_drawTextRight(JNIEnv *env, jobject obj, jstring text, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		const jchar *chars = env->GetStringChars(text, NULL);
		if (chars == NULL) EXCEPTION_CHECK(env)
		sADMDrawer->DrawTextRightW(drawer, chars, &rt);
		env->ReleaseStringChars(text, chars);
	} EXCEPTION_CONVERT(env)
}

/*
 * void drawTextInABox(Ljava.lang.String text, com.scriptographer.ai.Rectangle rect)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_drawTextInABox(JNIEnv *env, jobject obj, jstring text, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		const jchar *chars = env->GetStringChars(text, NULL);
		if (chars == NULL) EXCEPTION_CHECK(env)
		sADMDrawer->DrawTextInABoxW(drawer, &rt, chars);
		env->ReleaseStringChars(text, chars);
	} EXCEPTION_CONVERT(env)
}

/*
 * void drawUpArrow(com.scriptographer.ai.Rectangle rect)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_drawUpArrow(JNIEnv *env, jobject obj, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		sADMDrawer->DrawUpArrow(drawer, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void drawDownArrow(com.scriptographer.ai.Rectangle rect)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_drawDownArrow(JNIEnv *env, jobject obj, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		sADMDrawer->DrawDownArrow(drawer, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void drawLeftArrow(com.scriptographer.ai.Rectangle rect)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_drawLeftArrow(JNIEnv *env, jobject obj, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		sADMDrawer->DrawLeftArrow(drawer, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void drawRightArrow(com.scriptographer.ai.Rectangle rect)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Drawer_drawRightArrow(JNIEnv *env, jobject obj, jobject rect) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		sADMDrawer->DrawRightArrow(drawer, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * com.scriptographer.ai.Rectangle getUpdateRect()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Drawer_getUpdateRect(JNIEnv *env, jobject obj) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		ADMRect rt;
		sADMDrawer->GetUpdateRect(drawer, &rt);
		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * int getTextRectHeight(int height, java.lang.String text)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Drawer_getTextRectHeight(JNIEnv *env, jobject obj, jint height, jstring text) {
	try {
		ADMDrawerRef drawer = gEngine->getDrawerRef(env, obj);
		const jchar *chars = env->GetStringChars(text, NULL);
		if (chars == NULL) EXCEPTION_CHECK(env)
		height = sADMDrawer->GetTextRectHeightW(drawer, height, chars);
		env->ReleaseStringChars(text, chars);
		return height;
	} EXCEPTION_CONVERT(env)
	return 0;
}
