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
 * $RCSfile: com_scriptographer_ai_Path.cpp,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/03/05 21:51:40 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Path.h"

/*
 * com.scriptographer.ai.Path
 */
 
short pathGetBezierCount(AIArtHandle art) {
	short count;
	sAIPath->GetPathSegmentCount(art, &count);
	AIBoolean closed;
	sAIPath->GetPathClosed(art, &closed);
	if (!closed) count--; // number of beziers = number of segments - 1
	return count;
}

/*
 * com.scriptographer.ai.Path createRectangle(com.scriptographer.ai.Rect rect)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Path_createRectangle(JNIEnv *env, jclass cls, jobject rect) {
	try {
		AIRealRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		AIArtHandle handle;
		sAIShapeConstruction->NewRect(rt.top, rt.left, rt.bottom, rt.right, false, &handle);
		return gEngine->wrapArtHandle(env, handle);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.ai.Path createRoundRectangle(com.scriptographer.ai.Rectangle rect, float hor, float ver)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Path_createRoundRectangle(JNIEnv *env, jclass cls, jobject rect, jfloat hor, jfloat ver) {
	try {
		AIRealRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		AIArtHandle handle;
		sAIShapeConstruction->NewRoundedRect(rt.top, rt.left, rt.bottom, rt.right, hor, ver, false, &handle);
		return gEngine->wrapArtHandle(env, handle);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.ai.Path createOval(com.scriptographer.ai.Rectangle rect, boolean circumscribed)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Path_createOval(JNIEnv *env, jclass cls, jobject rect, jboolean circumscribed) {
	try {
		AIRealRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		AIArtHandle handle;
		if (circumscribed)
			sAIShapeConstruction->NewCircumscribedOval(rt.top, rt.left, rt.bottom, rt.right, false, &handle);
		else
			sAIShapeConstruction->NewInscribedOval(rt.top, rt.left, rt.bottom, rt.right, false, &handle);
		return gEngine->wrapArtHandle(env, handle);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.ai.Path createRegularPolygon(int numSides, com.scriptographer.ai.Point center, float radius)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Path_createRegularPolygon(JNIEnv *env, jclass cls, jint numSides, jobject center, jfloat radius) {
	try {
		AIRealPoint pt;
		gEngine->convertPoint(env, center, &pt);
		AIArtHandle handle;
		sAIShapeConstruction->NewRegularPolygon(numSides, pt.h, pt.v, radius, false, &handle);
		return gEngine->wrapArtHandle(env, handle);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.ai.Path createStar(int numPoints, com.scriptographer.ai.Point center, float radius1, float radius2)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Path_createStar(JNIEnv *env, jclass cls, jint numPoints, jobject center, jfloat radius1, jfloat radius2) {
	try {
		AIRealPoint pt;
		gEngine->convertPoint(env, center, &pt);
		AIArtHandle handle;
		sAIShapeConstruction->NewStar(numPoints, pt.h, pt.v, radius1, radius2, false, &handle);
		return gEngine->wrapArtHandle(env, handle);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.ai.Path createSpiral(com.scriptographer.ai.Point firstArcCenter, com.scriptographer.ai.Point start, float decayPercent, int numQuarterTurns, boolean clockwiseFromOutside)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Path_createSpiral(JNIEnv *env, jclass cls, jobject firstArcCenter, jobject start, jfloat decayPercent, jint numQuarterTurns, jboolean clockwiseFromOutside) {
	try {
		AIRealPoint ptCenter, ptStart;
		gEngine->convertPoint(env, firstArcCenter, &ptCenter);
		gEngine->convertPoint(env, start, &ptStart);
		AIArtHandle handle;
		sAIShapeConstruction->NewSpiral(ptCenter, ptStart, decayPercent, numQuarterTurns, clockwiseFromOutside, &handle);
		return gEngine->wrapArtHandle(env, handle);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * boolean getClosed()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Path_getClosed(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		AIBoolean closed;
		if (!sAIPath->GetPathClosed(handle, &closed))
			return closed;
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setClosed(boolean closed)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Path_setClosed(JNIEnv *env, jobject obj, jboolean closed) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		sAIPath->SetPathClosed(handle, closed);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean getGuide()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Path_getGuide(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		AIBoolean guide;
		if (!sAIPath->GetPathGuide(handle, &guide))
			return guide;
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setGuide(boolean guide)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Path_setGuide(JNIEnv *env, jobject obj, jboolean guide) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		sAIPath->SetPathGuide(handle, guide);
	} EXCEPTION_CONVERT(env)
}

/*
 * com.scriptographer.TabletValue[] getTabletData()
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_ai_Path_getTabletData(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		// get the tabletData:
		// first get the number of data:
		int count = 0;
		sAITabletData->GetTabletData(handle, NULL, &count, kTabletPressure);
		// create the array
		AITabletProfile *profiles = new AITabletProfile[count];
		// and get the values:
		sAITabletData->GetTabletData(handle, &profiles, &count, kTabletPressure);
		
		// create an array with the tabletProfiles:
		jobjectArray array = env->NewObjectArray(count, gEngine->cls_TabletValue, NULL); 
		for (int i = 0; i < count; i++) {
			jobject value = env->NewObject(gEngine->cls_TabletValue, gEngine->cid_TabletValue, (jfloat) profiles[i].offset, (jfloat) profiles[i].value);
			env->SetObjectArrayElement(array, i, value); 
		}
		delete profiles;
		return array;
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void setTabletData(com.scriptographer.TabletValue[] data)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Path_setTabletData(JNIEnv *env, jobject obj, jobjectArray data) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		// get the tabletData:
		// first get the number of data:
		if (data != NULL) {
			int count = env->GetArrayLength(data);
			AITabletProfile *profiles = new AITabletProfile[count];
			for (int i = 0; i < count; i++) {
				jobject object = env->GetObjectArrayElement(data, i);
				AITabletProfile *profile = &profiles[i];
				profile->offset = env->GetFloatField(object, gEngine->fid_TabletValue_offset);
				profile->value = env->GetFloatField(object, gEngine->fid_TabletValue_value);
			}
			// set the new values:
			sAITabletData->SetTabletDataInUse(handle, count > 0);
			sAITabletData->SetTabletData(handle, profiles, count, kTabletPressure);
			delete profiles;
		} else {
			sAITabletData->SetTabletDataInUse(handle, false);
		}
	} EXCEPTION_CONVERT(env)
}

/*
 * float getLength(float flatness)
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_Path_getLength(JNIEnv *env, jobject obj, jfloat flatness) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		AIReal length;
		sAIPath->GetPathLength(handle, &length, flatness);
		return length;
	} EXCEPTION_CONVERT(env)
	return 0.0;
}

/*
 * float getArea()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_Path_getArea(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		AIReal area;
		sAIPath->GetPathArea(handle, &area);
		return area;
	} EXCEPTION_CONVERT(env)
	return 0.0;
}

static void *pathAllocate( long size) {
//	return malloc(size);
	void *p;
	PUSH_GLOBALS
	if (sSPBlocks->AllocateBlock(size, "globals", &p)) p = NULL;
	POP_GLOBALS
	return p;
}

static void pathDispose( void *p) {
	PUSH_GLOBALS
	sSPBlocks->FreeBlock(p);
	POP_GLOBALS
//	free(p);
}

static AIPathConstructionMemoryObject pathMemoryObject = {
	pathAllocate, pathDispose
};

/*
 * int nativePointsToCurves(int handle, float tolerance, float threshold, int cornerRadius, float scale)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Path_nativePointsToCurves(JNIEnv *env, jclass cls, jint handle, jfloat tolerance, jfloat threshold, jint cornerRadius, jfloat scale) {
	int res = 0;
	try {
		short count;
		sAIPath->GetPathSegmentCount((AIArtHandle) handle, &count);
		if (count > 0) {
			// convert the segments to points first:
			AIPathSegment *segments = (AIPathSegment *) pathAllocate(count * sizeof(AIPathSegment));
			AIPathConstructionPoint *points = (AIPathConstructionPoint *) pathAllocate(count * sizeof(AIPathConstructionPoint));
			if (segments != NULL && points != NULL) {
				sAIPath->GetPathSegments((AIArtHandle) handle, 0, count, segments);
				for (int i = 0; i < count; i++) {
					points[i].point = segments[i].p;
					points[i].corner = segments[i].corner;
				}
				pathDispose(segments);
				long pointCount;
				pointCount = count;
				long segCount = 0;
				short radius = cornerRadius;
				AIPathSegment *segments = NULL;
				if (!sAIPathConstruction->PointsToCurves(&pointCount, points, &segCount, &segments, &tolerance, &threshold, &radius, &scale, &pathMemoryObject) && segments != NULL) {
					sAIPath->SetPathSegmentCount((AIArtHandle) handle, segCount);	
					sAIPath->SetPathSegments((AIArtHandle) handle, 0, segCount, segments);	
					res = segCount;
					pathDispose(segments);
				}
				pathDispose(points);
			}
		}
	} EXCEPTION_CONVERT(env)
	return res;
}

/*
 * int nativeCurvesToPoints(int handle, float maxPointDistance, float flatness)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Path_nativeCurvesToPoints(JNIEnv *env, jclass cls, jint handle, jfloat maxPointDistance, jfloat flatness) {
	int res = 0;
	try {
		short count;
		sAIPath->GetPathSegmentCount((AIArtHandle) handle, &count);
		if (count > 0) {
			// if the path is closed, we have to reuse the first point at the end (curvesToPoints can't handle closed paths directly...)
			AIBoolean closed;
			sAIPath->GetPathClosed((AIArtHandle) handle, &closed);
			AIPathSegment *segments = (AIPathSegment *)pathAllocate((closed ? count + 1 : count) * sizeof(AIPathSegment));
			if (segments != NULL) {
				AIPathConstructionPoint *points = NULL;
				sAIPath->GetPathSegments((AIArtHandle) handle, 0, count, segments);
				if (closed) {
					memcpy(&segments[count], &segments[0], sizeof(AIPathSegment));
					count++;
				}
				long pointCount = 0;
				if (!sAIPathConstruction->CurvesToPoints(count, segments, &pointCount, &points, maxPointDistance, flatness, &pathMemoryObject) && points != NULL) {
					// convert points to segments:
					AIPathSegment *segments = (AIPathSegment *)pathAllocate(pointCount * sizeof(AIPathSegment));
					if (segments != NULL) {
						for (int i = 0; i < pointCount; i++) {
							segments[i].p = segments[i].in = segments[i].out = points[i].point;
							segments[i].corner = points[i].corner;
						}
						sAIPath->SetPathSegmentCount((AIArtHandle) handle, pointCount);	
						sAIPath->SetPathSegments((AIArtHandle) handle, 0, pointCount, segments);	
						res = pointCount;
					}
					pathDispose(segments);
					pathDispose(points);
				}
			}
		}
	} EXCEPTION_CONVERT(env)
	return res;
}

/*
 * void nativeReduceSegments(int handle, float flatness)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Path_nativeReduceSegments(JNIEnv *env, jclass cls, jint handle, jfloat flatness) {
	try {
		sAIPathConstruction->ReducePathSegments((AIArtHandle) handle, flatness, &pathMemoryObject);
	} EXCEPTION_CONVERT(env)
}

/*
 * int nativeSplit(int handle, int segment, float t)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Path_nativeSplit(JNIEnv *env, jclass cls, jint handle, jint index, jfloat t) {
	AIArtHandle newArt = NULL;
	try {
		short count;
		sAIPath->GetPathSegmentCount((AIArtHandle) handle, &count);

		if (t < 0.0) t = 0.0;
		else if (t >= 1.0) {
			// t = 1 is the same as t = 0 and index ++
			index++;
			t = 0.0;
		}
		if (index >= 0 && index < count - 1) {
			if (t == 0) { // spezial case
				if (index > 0) {
					// create the segments for the new path:
					short newCount = count - index;
					AIPathSegment *segments = new AIPathSegment[newCount];
					if (segments != NULL) {
						// get the segments, cut the path
						if (!sAIPath->GetPathSegments((AIArtHandle) handle, index, newCount, segments) && !sAIPath->SetPathSegmentCount((AIArtHandle) handle, index + 1)) {
							if (!sAIArt->NewArt(kPathArt, kPlaceBelow, (AIArtHandle) handle, &newArt)) {
								sAIPath->SetPathSegments(newArt, 0, newCount, segments);
							}
						}
						delete segments;
					}
				}
			} else { // split the bezier at t
				// create the segments for the new path:
				short newCount = count - index;
				AIPathSegment *segments = new AIPathSegment[newCount];
				if (segments != NULL) {
					if (!sAIPath->GetPathSegments((AIArtHandle) handle, index, newCount, segments)) {
						// fill the bezier structure with the points:
						AIRealBezier b, b1, b2;
						b.p0 = segments[0].p;
						b.p1 = segments[0].out;
						b.p2 = segments[1].in;
						b.p3 = segments[1].p;
						// devide the bezier
						sAIRealBezier->Divide(&b, t, &b1, &b2);
						// now change the points in the current and the new path:
						
						// current path:
						segments[0].p = b1.p0;
						segments[0].out = b1.p1;
						segments[1].in = b1.p2;
						segments[1].p = b1.p3;
						// set the segments, cut the path
						if (!sAIPath->SetPathSegmentCount((AIArtHandle) handle, index + 2) && !sAIPath->SetPathSegments((AIArtHandle) handle, index, 2, segments)) {

							// new path:
							segments[0].p = b2.p0;
							segments[0].out = b2.p1;
							segments[1].in = b2.p2;
							segments[1].p = b2.p3;

							if (!sAIArt->NewArt(kPathArt, kPlaceBelow, (AIArtHandle) handle, &newArt)) {
								sAIPath->SetPathSegments(newArt, 0, newCount, segments);
							}
						}
					}
					delete segments;
				}
			}
		}
	} EXCEPTION_CONVERT(env)
	return (jint)newArt;
}

int quadraticRoots(double a, double b, double c, double roots[2], double epsilon) {
	// Solve, using closed form methods, the quadratic polynomial:	
	//		a*x^2 + b*x + c = 0				
	// for 2 real roots returned in root[0..1].  If error we return 0.
	// We also return 0 or 1 real roots as appropriate, such as when
	// the problem is actually linear.					
	// After _Numerical Recipes in C_, 2nd edition, Press et al.,	
	// page 183, although with some added case testing and forwarding.
	// This is better than the _Graphics Gems_ technique, which admits
	// the possibility of numerical errors cited in Press.		
	int solutions = 0;
	// If problem is actually linear, return 0 or 1 easy roots		
	if (fabs(a)<epsilon) {
		if (fabs(b)>=epsilon) {
			roots[solutions++] = -c/b;
		} else if (fabs(c)<epsilon) { // if all the coefficients are 0, infinite values are possible!
			solutions = -1; // -1 indicates infinite solutions
		}
		return solutions;
	}
	double bb = b*b;
	double q = bb-4.0*a*c;
	if (q<0.0) return solutions;
	q = sqrt(q);
	if (b<0.0) q = -q;
	q = -0.5*(b+q);
	if (fabs(q)>=epsilon) roots[solutions++] = c / q;
	if (fabs(a)>=epsilon) roots[solutions++] = q / a;
	return solutions;
}

int cubicRoots(double a, double b, double c, double d, double roots[3], double epsilon) {
	// Solve, using closed form methods, the cubic polynomial:		
	//		a*x^3 + b*x^2 + c*x + d = 0			
	// for 1 real root returned in root[0], or 3 real roots returned
	// in root[0..2].  If error we return 0.  Note: we alter c[].	
	// If the polynomial is actually quadratic or linear (because	
	// coefficients a or b are zero), we forward the problem to
	// the quadratic/linear solver and return the appropriate 1 or 2
	// roots.								
	// After _Numerical Recipes in C_, 2nd edition, Press et al.,	
	// page 184, although with some added case testing and forwarding.
	// This is better than the _Graphics Gems_ technique, which admits
	// the possibility of numerical errors cited in Press.		
	// Test for a quadratic or linear degeneracy			
	if (fabs(a) < epsilon) {
		return(quadraticRoots(b, c, d, roots, epsilon));
	}
	// Normalize							
	b /= a; c /= a; d /= a; a = 1.0;
	// Compute discriminants						
	double Q = (b * b - 3.0 * c) / 9.0;
	double QQQ = Q * Q * Q;
	double R = (2.0 * b * b * b - 9.0 * b * c + 27.0 * d) / 54.0;
	double RR = R * R;
	if (RR <= QQQ) { // Three real roots
		// This sqrt and division is safe, since RR >= 0, so QQQ > RR,	
		// so QQQ > 0.  The acos is also safe, since RR/QQQ < 1, and	
		// thus R/sqrt(QQQ) < 1.					
		double theta = acos(R / sqrt(QQQ));
		// This sqrt is safe, since QQQ >= 0, and thus Q >= 0
		double v1 = -2.0 * sqrt(Q);
		double v2 = b / 3.0;
		roots[0] = v1 * cos(theta / 3.0) - v2;
		roots[1] = v1 * cos((theta + 2 * PI) / 3.0) - v2;
		roots[2] = v1 * cos((theta - 2 * PI) / 3.0) - v2;
		return 3;
	} else { // One real root							
		double A = -pow(fabs(R)+sqrt(RR - QQQ), 1.0 / 3.0);
		if (A != 0.0) {
			if (R < 0.0) A = -A;
			roots[0] = A + Q / A - b / 3.0;
			return 1;
		}
	}
	return 0;
}

int bezierCubicRoots(double v1, double v2, double v3, double v4, double v, double roots[3], double epsilon) {
	// conversion from the point coordinates (v1 .. v4) to the polynomal coefficients:
	double v1m3 = 3.0 * v1;
	double v2m3 = 3.0 * v2;
	double v3m3 = 3.0 * v3;

	double a = v4 - v3m3 + v2m3        - v1;
	double b =      v3m3 - v2m3 - v2m3 + v1m3;
	double c =             v2m3        - v1m3;
	double d =                           v1 - v;

	return cubicRoots(a, b, c, d, roots, epsilon);
}

/*
 * com.scriptographer.ai.SegmentPosition hitTest(com.scriptographer.ai.Point point, float epsilon)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Path_hitTest(JNIEnv *env, jobject obj, jobject point, jfloat epsilon) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		ADMPoint pt;
		gEngine->convertPoint(env, point, &pt);

		short count = pathGetBezierCount(handle);
		AIRealBezier bezier;
		
		for (int i = 0; i < count; i++) {
			sAIPath->GetPathBezier(handle, i, &bezier);
			double txs[3] = { 0, 0, 0 }; 
			double tys[3] = { 0, 0, 0 };
			int sx = bezierCubicRoots(bezier.p0.h, bezier.p1.h, bezier.p2.h, bezier.p3.h, pt.h, txs, epsilon);
			int sy = bezierCubicRoots(bezier.p0.v, bezier.p1.v, bezier.p2.v, bezier.p3.v, pt.v, tys, epsilon);

			int x = 0;
			// sx, sy == -1 means infinite solutions:
			while (x < sx || sx == -1) {
				double tx = txs[x++];
				if (tx >= 0 && tx <= 1.0 || sx == -1) {
					int y = 0;
					while (y < sy || sy == -1) {
						double ty = tys[y++];
						if (ty >= 0 && ty <= 1.0 || sy == -1) {
							if (sx == -1) tx = ty;
							else if (sy == -1) ty = tx;
							if (fabs(tx - ty) < 0.001) { // tolerance
								float t = (tx + ty) * 0.5;
								return gEngine->newObject(env, gEngine->cls_SegmentPosition, gEngine->cid_SegmentPosition, i, t);
							}
						}
					}
					if (sx == -1) sx = 0; // avoid endless loops here: if sx is infinite and there was no fitting ty, there's no solution for this bezier
				}
			}
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.ai.SegmentPosition getPositionWithLength(float length, float flatness)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Path_getPositionWithLength(JNIEnv *env, jobject obj, jfloat length, jfloat flatness) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		short count = pathGetBezierCount(handle);
		float currentLength = 0;
		for (int i = 0; i < count; i++) {
			AIRealBezier bezier;
			sAIPath->GetPathBezier(handle, i, &bezier);
			float startLength = currentLength;
			currentLength += sAIRealBezier->Length(&bezier, flatness);
			if (currentLength >= length) { // found the segment within which the length lies
				float t = curveGetPositionWithLength(&bezier, length - startLength, flatness);
				return gEngine->newObject(env, gEngine->cls_SegmentPosition, gEngine->cid_SegmentPosition, i, t);
			}
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}
