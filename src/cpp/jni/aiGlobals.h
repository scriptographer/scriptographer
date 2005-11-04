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
 * $RCSfile: aiGlobals.h,v $
 * $Author: lehni $
 * $Revision: 1.8 $
 * $Date: 2005/11/04 01:34:14 $
 */

short Art_getType(AIArtHandle handle);
short Art_getType(JNIEnv *env, jclass cls);
jboolean Art_hasChildren(AIArtHandle handle);
jboolean Art_isLayer(AIArtHandle handle);
AIArtHandle Art_rasterize(AIArtHandle handle, AIRasterizeType type, float resolution, int antialiasing, float width, float height);

void ArtSet_filter(AIArtSet set, bool layerOnly = false);
AIArtHandle ArtSet_rasterize(AIArtSet artSet, AIRasterizeType type, float resolution, int antialiasing, float width, float height);

short Path_getBezierCount(AIArtHandle art);

void PathStyle_init(JNIEnv *env, jobject obj, AIPathStyle *style, AIPathStyleMap *map);
void PathStyle_convertPathStyle(JNIEnv *env, AIPathStyle *style, AIPathStyleMap *map, jfloatArray fillColor, jboolean hasFillColor, jshort fillOverprint, jfloatArray strokeColor, jboolean hasStrokeColor, jshort strokeOverprint, jfloat strokeWidth, jfloat dashOffset, jfloatArray dashArray, jshort cap, jshort join, jfloat miterLimit, jshort clip, jshort lockClip, jshort evenOdd, jfloat resolution);
int PathStyle_convertFillStyle(JNIEnv *env, AIFillStyle *style, AIFillStyleMap *map, jfloatArray fillColor, jboolean hasFillColor, jshort fillOverprint);
int PathStyle_convertStrokeStyle(JNIEnv *env, AIStrokeStyle *style, AIStrokeStyleMap *map, jfloatArray strokeColor, jboolean hasStrokeColor, jshort strokeOverprint, jfloat strokeWidth, jfloat dashOffset, jfloatArray dashArray, jshort cap, jshort join, jfloat miterLimit);

jobject TextRange_convertTextRanges(JNIEnv *env, ATE::TextRangesRef ranges);

#define DEFINE_SEGMENT(NAME, PTX, PTY, INX, INY, OUTX, OUTY, CORNER) \
	AIPathSegment NAME; \
	NAME.p.h = PTX; \
	NAME.p.v = PTY; \
	NAME.in.h = INX; \
	NAME.in.v = INY; \
	NAME.out.h = OUTX; \
	NAME.out.v = OUTY; \
	NAME.corner = CORNER;

#define DEFINE_BEZIER(NAME, P1X, P1Y, H1X, H1Y, H2X, H2Y, P2X, P2Y) \
	AIRealBezier NAME; \
	NAME.p0.h = P1X; \
	NAME.p0.v = P1Y; \
	NAME.p1.h = H1X; \
	NAME.p1.v = H1Y; \
	NAME.p2.h = H2X; \
	NAME.p2.v = H2Y; \
	NAME.p3.h = P2X; \
	NAME.p3.v = P2Y;

#define DEFINE_POINT(NAME, X, Y) \
	AIRealPoint NAME; \
	NAME.h = X; \
	NAME.v = Y;

#define DEFINE_RECT(RT, X, Y, WIDTH, HEIGHT) \
	AIRealRect RT; \
	RT.left = X; \
	RT.top  = Y; \
	RT.right =  X + WIDTH; \
	RT.bottom = Y + HEIGHT;

// switch to the specified document first if it differs from the current one:
#define CREATEART_BEGIN \
	AIDocumentHandle activeDoc = NULL; \
	AIDocumentHandle prevDoc = NULL; \
	try { \
		AIDocumentHandle doc = (AIDocumentHandle) docHandle; \
		sAIDocument->GetDocument(&activeDoc); \
		if (activeDoc != doc) { \
			prevDoc = activeDoc; \
			sAIDocumentList->Activate(doc, false); \
		}
		
// switch back to the previously active document:
#define CREATEART_END \
	} EXCEPTION_CONVERT(env) \
	if (prevDoc != NULL) \
		sAIDocumentList->Activate(prevDoc, false);

// macros for style getters and setters (CharacterStyle, ParagraphStyle)
#define STYLE_GET(NAME, TYPE, CLASS, JTYPE) \
	try { \
		CharFeaturesRef features = gEngine->getCharFeaturesRef(env, obj); \
		bool isAssigned; \
		TYPE value; \
		if (!sCharFeatures->Get##NAME##(features, &isAssigned, &value) && isAssigned) \
			return gEngine->newObject(env, gEngine->cls_##CLASS##, gEngine->cid_##CLASS##, (JTYPE) value); \
	} EXCEPTION_CONVERT(env) \
	return NULL;

#define STYLE_SET(NAME, TYPE, METHOD_TYPE, METHOD_NAME) \
	try { \
		CharFeaturesRef features = gEngine->getCharFeaturesRef(env, obj); \
		ASErr err; \
		if (value == NULL) \
			err = sCharFeatures->Clear##NAME##(features); \
		else \
			err = sCharFeatures->Set##NAME##(features, (TYPE) gEngine->call##METHOD_TYPE##Method(env, value, gEngine->METHOD_NAME)); \
		if (!err) \
			gEngine->callVoidMethod(env, obj, gEngine->mid_CharacterStyle_markSetStyle); \
	} EXCEPTION_CONVERT(env)

#define STYLE_GET_FLOAT(NAME) \
	STYLE_GET(NAME, ASReal, Float, jfloat)

#define STYLE_SET_FLOAT(NAME) \
	STYLE_SET(NAME, ASReal, Float, mid_Number_floatValue)

#define STYLE_GET_BOOLEAN(NAME) \
	STYLE_GET(NAME, bool, Boolean, jboolean)

#define STYLE_SET_BOOLEAN(NAME) \
	STYLE_SET(NAME, bool, Boolean, mid_Boolean_booleanValue)

#define STYLE_GET_INTEGER(NAME) \
	STYLE_GET(NAME, ASInt32, Integer, jint)

#define STYLE_SET_INTEGER(NAME) \
	STYLE_SET(NAME, ASInt32, Int, mid_Number_intValue)

#define STYLE_GET_ENUM(NAME) \
	STYLE_GET(NAME, NAME, Integer, jint)

#define STYLE_SET_ENUM(NAME) \
	STYLE_SET(NAME, NAME, Int, mid_Number_intValue)
