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
 * $Revision: 1.4 $
 * $Date: 2005/03/30 08:15:38 $
 */

short artGetType(AIArtHandle handle);
short artGetType(JNIEnv *env, jclass cls);
jboolean artHasChildren(AIArtHandle handle);
jboolean artIsLayer(AIArtHandle handle);
AIArtHandle artRasterize(AIArtHandle handle, AIRasterizeType type, float resolution, int antialiasing, float width, float height);

void artSetFilter(AIArtSet set, bool layerOnly = false);
AIArtHandle artSetRasterize(AIArtSet artSet, AIRasterizeType type, float resolution, int antialiasing, float width, float height);

short pathGetBezierCount(AIArtHandle art);

double curveGetPartLength(AIRealBezier *bezier, AIReal t1, AIReal t2, AIReal step);
double curveGetPositionWithLength(AIRealBezier *bezier, AIReal length, AIReal flatness);

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
