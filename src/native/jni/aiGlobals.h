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

extern AIDocumentHandle gWorkingDoc;
extern AIDocumentHandle gActiveDoc;
extern AIDocumentHandle gCreationDoc;

bool Item_isValid(AIArtHandle art);
short Item_getType(AIArtHandle handle);
short Item_getType(JNIEnv *env, jclass cls);
jboolean Item_hasChildren(AIArtHandle handle);
jboolean Item_isLayer(AIArtHandle handle);
AIArtHandle Item_rasterize(AIArtHandle handle, AIRasterizeType type, float resolution, int antialiasing, float width, float height);
AIArtHandle Item_getInsertionPoint(short *paintOrder, AIDocumentHandle doc = NULL);
void Item_commit(JNIEnv *env, AIArtHandle art, bool invalidate = false, bool children = true);

void ItemList_filter(AIArtSet set, bool layerOnly = false);
AIArtSet ItemList_getSelected(JNIEnv *env);
AIArtHandle ItemList_rasterize(AIArtSet artSet, AIRasterizeType type, float resolution, int antialiasing, float width, float height);

AIArtHandle JNICALL PlacedFile_place(JNIEnv *env, AIDocumentHandle doc, jobject file, jboolean linked);

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
