/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 
#include "stdHeaders.h"
#include "ScriptographerPlugin.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Path.h"

/*
 * com.scriptographer.ai.Path
 */
 
short Path_getBezierCount(AIArtHandle art) {
	short count;
	sAIPath->GetPathSegmentCount(art, &count);
	AIBoolean closed;
	sAIPath->GetPathClosed(art, &closed);
	if (!closed) count--; // number of beziers = number of segments - 1
	return count;
}

static void *Path_allocate(long size) {
	void *p;
	if (sSPBlocks->AllocateBlock(size, "com.scriptographer.ai.Path", &p) != 0) {
		throw new StringException("Out of memory.");
	}
	return p;
}

static void Path_dispose(void *p) {
	sSPBlocks->FreeBlock(p);
}

DEFINE_CALLBACK_PROC(Path_allocate);
DEFINE_CALLBACK_PROC(Path_dispose);

static AIPathConstructionMemoryObject Path_memoryObject = {
	(void * (*)(long int)) CALLBACK_PROC(Path_allocate),
	(void (*)(void *)) CALLBACK_PROC(Path_dispose)
};

/*
 * boolean isClosed()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Path_isClosed(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		AIBoolean closed;
		if (!sAIPath->GetPathClosed(handle, &closed))
			return closed;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void nativeSetClosed(boolean closed)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Path_nativeSetClosed(JNIEnv *env, jobject obj, jboolean closed) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj, true);
		sAIPath->SetPathClosed(handle, closed);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isGuide()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Path_isGuide(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		AIBoolean guide;
		if (!sAIPath->GetPathGuide(handle, &guide))
			return guide;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setGuide(boolean guide)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Path_setGuide(JNIEnv *env, jobject obj, jboolean guide) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj, true);
		sAIPath->SetPathGuide(handle, guide);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.TabletValue[] getTabletData()
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_ai_Path_getTabletData(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		// return null if it's not in use:
		ASBoolean inUse = false;
		sAITabletData->GetTabletDataInUse(handle, &inUse);
		if (inUse) {
			// get the tabletData:
			// first get the number of data:
			int count = 0;
			AITabletProfile *profiles = NULL;
			sAITabletData->GetTabletData(handle, &profiles, &count, kTabletPressure);
			// create the array
			profiles = new AITabletProfile[count];
			// and get the values:
			sAITabletData->GetTabletData(handle, &profiles, &count, kTabletPressure);
			
			// create an array with the tabletProfiles:
			jobjectArray array = env->NewObjectArray(count, gEngine->cls_ai_TabletValue, NULL); 
			for (int i = 0; i < count; i++) {
				jobject value = env->NewObject(gEngine->cls_ai_TabletValue, gEngine->cid_ai_TabletValue, (jfloat) profiles[i].offset, (jfloat) profiles[i].value);
				env->SetObjectArrayElement(array, i, value); 
			}
			delete profiles;
			return array;
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setTabletData(com.scriptographer.TabletValue[] data)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Path_setTabletData(JNIEnv *env, jobject obj, jobjectArray data) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj, true);
		// Get the tabletData:
		if (data != NULL) {
			// First convert the passed array to a AITabletProfile array:
			int count = env->GetArrayLength(data);
			AITabletProfile *profiles = new AITabletProfile[count];
			for (int i = 0; i < count; i++) {
				jobject obj = env->GetObjectArrayElement(data, i);
				AITabletProfile *profile = &profiles[i];
				profile->offset = env->GetFloatField(obj, gEngine->fid_ai_TabletValue_offset);
				profile->value = env->GetFloatField(obj, gEngine->fid_ai_TabletValue_value);
			}
			// Now set the new values:
			// At least on CS2, setting the size to 0 first seems to be necessary, when
			// tabletData was already in use before. Otherwise Illustrator crashes (#6).
			ASBoolean inUse = false;
			sAITabletData->GetTabletDataInUse(handle, &inUse);
			if (inUse)
				sAITabletData->SetTabletData(handle, NULL, 0, kTabletPressure);
			sAITabletData->SetTabletData(handle, profiles, count, kTabletPressure);
			sAITabletData->SetTabletDataInUse(handle, count > 0);
			delete profiles;
		} else {
			// Just setting to 0 doesn't seem to do the trick.
			// First set to a straight envelope, then to 0
			AITabletProfile profiles[] = {
				{ 0, 1 },
				{ 1, 1 }
			};
			sAITabletData->SetTabletData(handle, profiles, 2, kTabletPressure);
			// Now set to 0
			sAITabletData->SetTabletData(handle, profiles, 0, kTabletPressure);
			sAITabletData->SetTabletDataInUse(handle, false);
		}
		// Simply swap the closed flag of this path in order to get  
		// Illustrator to recognize the change in the object, because
		// SetTabletData seems be ignored as a change:
		AIBoolean closed = false;
		sAIPath->GetPathClosed(handle, &closed);
		sAIPath->SetPathClosed(handle, !closed);
		sAIPath->SetPathClosed(handle, closed);
	} EXCEPTION_CONVERT(env);
}

/*
 * float getLength(float flatness)
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_Path_getLength(JNIEnv *env, jobject obj, jfloat flatness) {
	try {
		// no need to activate document for this
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		AIReal length;
		sAIPath->GetPathLength(handle, &length, flatness);
		return length;
	} EXCEPTION_CONVERT(env);
	return 0.0;
}

/*
 * float getArea()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_Path_getArea(JNIEnv *env, jobject obj) {
	try {
		// no need to activate document for this
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		AIReal area;
		sAIPath->GetPathArea(handle, &area);
		return area;
	} EXCEPTION_CONVERT(env);
	return 0.0;
}

/*
 * int nativePointsToCurves(float tolerance, float threshold, int cornerRadius, float scale)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Path_nativePointsToCurves(JNIEnv *env, jobject obj, jfloat tolerance, jfloat threshold, jint cornerRadius, jfloat scale) {
	int res = 0;
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj, true);
		short count;
		sAIPath->GetPathSegmentCount(handle, &count);
		if (count > 0) {
			// convert the segments to points first:
			AIPathSegment *segments = new AIPathSegment[count];
			AIPathConstructionPoint *points = new AIPathConstructionPoint[count];
			if (segments != NULL && points != NULL) {
				sAIPath->GetPathSegments(handle, 0, count, segments);
				for (int i = 0; i < count; i++) {
					points[i].point = segments[i].p;
					points[i].corner = segments[i].corner;
				}
				delete[] segments;
				long pointCount;
				pointCount = count;
				long segCount = 0;
				short radius = cornerRadius;
				segments = NULL;
				if (!sAIPathConstruction->PointsToCurves(&pointCount, points, &segCount, &segments, &tolerance, &threshold, &radius, &scale, &Path_memoryObject) && segments != NULL) {
					sAIPath->SetPathSegmentCount(handle, segCount);	
					sAIPath->SetPathSegments(handle, 0, segCount, segments);	
					res = segCount;
					Path_dispose(segments);
				}
				delete[] points;
			}
		}
	} EXCEPTION_CONVERT(env);
	return res;
}

/*
 * int nativeCurvesToPoints(float maxPointDistance, float flatness)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Path_nativeCurvesToPoints(JNIEnv *env, jobject obj, jfloat maxPointDistance, jfloat flatness) {
	int res = 0;
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj, true);
		short count;
		sAIPath->GetPathSegmentCount(handle, &count);
		if (count > 0) {
			// if the path is closed, we have to reuse the first point at the end (curvesToPoints can't handle closed paths directly...)
			AIBoolean closed;
			sAIPath->GetPathClosed(handle, &closed);
			AIPathSegment *segments = new AIPathSegment[closed ? count + 1 : count];
			if (segments != NULL) {
				sAIPath->GetPathSegments(handle, 0, count, segments);
				if (closed) {
					memcpy(&segments[count], &segments[0], sizeof(AIPathSegment));
					count++;
				}
				long pointCount = 0;
				AIPathConstructionPoint *points = NULL;
				if (!sAIPathConstruction->CurvesToPoints(count, segments, &pointCount, &points, maxPointDistance, flatness, &Path_memoryObject) && points != NULL) {
					// convert points to segments:
					AIPathSegment *segments = new AIPathSegment[pointCount];
					if (segments != NULL) {
						for (int i = 0; i < pointCount; i++) {
							segments[i].p = segments[i].in = segments[i].out = points[i].point;
							segments[i].corner = points[i].corner;
						}
						Path_dispose(points);
						sAIPath->SetPathSegmentCount(handle, pointCount);	
						sAIPath->SetPathSegments(handle, 0, pointCount, segments);	
						res = pointCount;
						delete[] segments;
					}
				}
			}
		}
	} EXCEPTION_CONVERT(env);
	return res;
}

/*
 * void nativeReduceSegments(float flatness)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Path_nativeReduceSegments(JNIEnv *env, jobject obj, jfloat flatness) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj, true);
		sAIPathConstruction->ReducePathSegments(handle, flatness, &Path_memoryObject);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeReverse()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Path_nativeReverse(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj, true);
		if (sAIPath->ReversePathSegments(handle))
			throw new StringException("Cannot reverse path segments");
	} EXCEPTION_CONVERT(env);
}
