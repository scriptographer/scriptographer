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
 * $Revision: 1.8 $
 * $Date: 2006/05/30 16:03:40 $
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

/*
 * boolean isClosed()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Path_isClosed(JNIEnv *env, jobject obj) {
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
 * boolean isGuide()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Path_isGuide(JNIEnv *env, jobject obj) {
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
			jobjectArray array = env->NewObjectArray(count, gEngine->cls_TabletValue, NULL); 
			for (int i = 0; i < count; i++) {
				jobject value = env->NewObject(gEngine->cls_TabletValue, gEngine->cid_TabletValue, (jfloat) profiles[i].offset, (jfloat) profiles[i].value);
				env->SetObjectArrayElement(array, i, value); 
			}
			delete profiles;
			return array;
		}
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
				jobject obj = env->GetObjectArrayElement(data, i);
				AITabletProfile *profile = &profiles[i];
				profile->offset = env->GetFloatField(obj, gEngine->fid_TabletValue_offset);
				profile->value = env->GetFloatField(obj, gEngine->fid_TabletValue_value);
			}
			// set the new values:
			sAITabletData->SetTabletData(handle, profiles, count, kTabletPressure);
			sAITabletData->SetTabletDataInUse(handle, count > 0);
			delete profiles;
		} else {
			// just setting to 0 doesn't seem to work. first set to a straight envelope, then to 0
			AITabletProfile profiles[] = {
				{ 0, 1 },
				{ 1, 1 }
			};
			sAITabletData->SetTabletData(handle, profiles, 2, kTabletPressure);
			// now set to 0
			sAITabletData->SetTabletData(handle, profiles, 0, kTabletPressure);
			sAITabletData->SetTabletDataInUse(handle, false);
		}
		// simply swap the closed flag of this path in order to get the 
		// Illustrator to recognize the change in the object, because
		// SetTabletData seems be ignored as a change:
		AIBoolean closed = false;
		sAIPath->GetPathClosed(handle, &closed);
		sAIPath->SetPathClosed(handle, !closed);
		sAIPath->SetPathClosed(handle, closed);
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

static void *pathAllocate(long size) {
	void *p;
	if (sSPBlocks->AllocateBlock(size * 2, "com.scriptographer.ai.Path", &p) != 0) {
		throw new StringException("Out of memory.");
	}
	return p;
}

static void pathDispose(void *p) {
	sSPBlocks->FreeBlock(p);
}

DEFINE_CALLBACK_PROC(pathAllocate);
DEFINE_CALLBACK_PROC(pathDispose);

static AIPathConstructionMemoryObject pathMemoryObject = {
	(void * (*)(long int)) CALLBACK_PROC(pathAllocate),
	(void (*)(void *)) CALLBACK_PROC(pathDispose)
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
