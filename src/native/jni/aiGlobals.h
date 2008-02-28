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

extern AIDocumentHandle gActiveDoc;
extern AIDocumentHandle gWorkingDoc;
extern AIDocumentHandle gCreationDoc;

bool Art_isValid(AIArtHandle art);
short Art_getType(AIArtHandle handle);
short Art_getType(JNIEnv *env, jclass cls);
jboolean Art_hasChildren(AIArtHandle handle);
jboolean Art_isLayer(AIArtHandle handle);
AIArtHandle Art_rasterize(AIArtHandle handle, AIRasterizeType type, float resolution, int antialiasing, float width, float height);
AIArtHandle Art_getInsertionPoint(short *paintOrder, AIDocumentHandle doc = NULL);

void ArtSet_filter(AIArtSet set, bool layerOnly = false);
jobject ArtSet_getSelected(JNIEnv *env);
AIArtHandle ArtSet_rasterize(AIArtSet artSet, AIRasterizeType type, float resolution, int antialiasing, float width, float height);

AIArtHandle JNICALL PlacedItem_place(JNIEnv *env, AIDocumentHandle doc, jobject file, jboolean linked);

void Document_activate(AIDocumentHandle doc = NULL);
void Document_deselectAll(bool force = false);

short Path_getBezierCount(AIArtHandle art);

void PathStyle_init(JNIEnv *env, jobject obj, AIPathStyle *style, AIPathStyleMap *map);
void PathStyle_convertPathStyle(JNIEnv *env, AIPathStyle *style, AIPathStyleMap *map, jobject fillColor, jboolean hasFillColor, jshort fillOverprint, jobject strokeColor, jboolean hasStrokeColor, jshort strokeOverprint, jfloat strokeWidth, jfloat dashOffset, jfloatArray dashArray, jshort cap, jshort join, jfloat miterLimit, jshort clip, jshort lockClip, jshort evenOdd, jfloat resolution);
int PathStyle_convertFillStyle(JNIEnv *env, AIFillStyle *style, AIFillStyleMap *map, jobject fillColor, jboolean hasFillColor, jshort fillOverprint);
int PathStyle_convertStrokeStyle(JNIEnv *env, AIStrokeStyle *style, AIStrokeStyleMap *map, jobject strokeColor, jboolean hasStrokeColor, jshort strokeOverprint, jfloat strokeWidth, jfloat dashOffset, jfloatArray dashArray, jshort cap, jshort join, jfloat miterLimit);

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

// macros for style getters and setters (CharacterStyle, ParagraphStyle)
#define CHARACTERSTYLE_GET(NAME, TYPE, CLASS, JTYPE) \
	try { \
		CharFeaturesRef features = gEngine->getCharFeaturesRef(env, obj); \
		ATEBool8 isAssigned; \
		TYPE value; \
		if (!sCharFeatures->Get##NAME(features, &isAssigned, &value) && isAssigned) \
			return gEngine->newObject(env, gEngine->cls_##CLASS, gEngine->cid_##CLASS, (JTYPE) value); \
	} EXCEPTION_CONVERT(env); \
	return NULL;

#define CHARACTERSTYLE_SET(NAME, TYPE, METHOD_TYPE, METHOD_NAME) \
	try { \
		CharFeaturesRef features = gEngine->getCharFeaturesRef(env, obj); \
		ASErr err; \
		if (value == NULL) \
			err = sCharFeatures->Clear##NAME(features); \
		else \
			err = sCharFeatures->Set##NAME(features, (TYPE) gEngine->call##METHOD_TYPE##Method(env, value, gEngine->METHOD_NAME)); \
		if (!err) \
			gEngine->callVoidMethod(env, obj, gEngine->mid_ai_CharacterStyle_markSetStyle); \
	} EXCEPTION_CONVERT(env);

#define	CHARACTERSTYLE_GET_FLOAT(NAME) \
	CHARACTERSTYLE_GET(NAME, ASReal, Float, jfloat)

#define CHARACTERSTYLE_SET_FLOAT(NAME) \
	CHARACTERSTYLE_SET(NAME, ASReal, Float, mid_Number_floatValue)

#define CHARACTERSTYLE_GET_BOOLEAN(NAME) \
	CHARACTERSTYLE_GET(NAME, ATEBool8, Boolean, jboolean)

#define CHARACTERSTYLE_SET_BOOLEAN(NAME) \
	CHARACTERSTYLE_SET(NAME, ATEBool8, Boolean, mid_Boolean_booleanValue)

#define CHARACTERSTYLE_GET_INTEGER(NAME) \
	CHARACTERSTYLE_GET(NAME, ASInt32, Integer, jint)

#define CHARACTERSTYLE_SET_INTEGER(NAME) \
	CHARACTERSTYLE_SET(NAME, ASInt32, Int, mid_Number_intValue)

#define CHARACTERSTYLE_GET_ENUM(NAME) \
	CHARACTERSTYLE_GET(NAME, NAME, Integer, jint)

#define CHARACTERSTYLE_SET_ENUM(NAME) \
	CHARACTERSTYLE_SET(NAME, NAME, Int, mid_Number_intValue)

#define PARAGRAPHSTYLE_GET(NAME, TYPE, CLASS, JTYPE) \
	try { \
		ParaFeaturesRef features = gEngine->getParaFeaturesRef(env, obj); \
		ATEBool8 isAssigned; \
		TYPE value; \
		if (!sParaFeatures->Get##NAME(features, &isAssigned, &value) && isAssigned) \
			return gEngine->newObject(env, gEngine->cls_##CLASS, gEngine->cid_##CLASS, (JTYPE) value); \
	} EXCEPTION_CONVERT(env); \
	return NULL;

#define PARAGRAPHSTYLE_SET_CLEAR(NAME, CLEAR, TYPE, METHOD_TYPE, METHOD_NAME) \
	try { \
		ParaFeaturesRef features = gEngine->getParaFeaturesRef(env, obj); \
		ASErr err; \
		if (value == NULL) \
			err = sParaFeatures->Clear##CLEAR(features); \
		else \
			err = sParaFeatures->Set##NAME(features, (TYPE) gEngine->call##METHOD_TYPE##Method(env, value, gEngine->METHOD_NAME)); \
		if (!err) \
			gEngine->callVoidMethod(env, obj, gEngine->mid_ai_ParagraphStyle_markSetStyle); \
	} EXCEPTION_CONVERT(env);

#define PARAGRAPHSTYLE_SET(NAME, TYPE, METHOD_TYPE, METHOD_NAME) \
	PARAGRAPHSTYLE_SET_CLEAR(NAME, NAME, TYPE, METHOD_TYPE, METHOD_NAME)

#define PARAGRAPHSTYLE_GET_FLOAT(NAME) \
	PARAGRAPHSTYLE_GET(NAME, ASReal, Float, jfloat)

#define PARAGRAPHSTYLE_SET_FLOAT(NAME) \
	PARAGRAPHSTYLE_SET(NAME, ASReal, Float, mid_Number_floatValue)

#define PARAGRAPHSTYLE_SET_FLOAT_CLEAR(NAME, CLEAR) \
	PARAGRAPHSTYLE_SET_CLEAR(NAME, CLEAR, ASReal, Float, mid_Number_floatValue)

#define PARAGRAPHSTYLE_GET_BOOLEAN(NAME) \
	PARAGRAPHSTYLE_GET(NAME, ATEBool8, Boolean, jboolean)

#define PARAGRAPHSTYLE_SET_BOOLEAN(NAME) \
	PARAGRAPHSTYLE_SET(NAME, ATEBool8, Boolean, mid_Boolean_booleanValue)

#define PARAGRAPHSTYLE_GET_INTEGER(NAME) \
	PARAGRAPHSTYLE_GET(NAME, ASInt32, Integer, jint)

#define PARAGRAPHSTYLE_SET_INTEGER(NAME) \
	PARAGRAPHSTYLE_SET(NAME, ASInt32, Int, mid_Number_intValue)

#define PARAGRAPHSTYLE_GET_ENUM(NAME, TYPE) \
	PARAGRAPHSTYLE_GET(NAME, TYPE, Integer, jint)

#define PARAGRAPHSTYLE_SET_ENUM(NAME, TYPE) \
	PARAGRAPHSTYLE_SET(NAME, TYPE, Int, mid_Number_intValue)

