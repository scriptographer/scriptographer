/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
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
 * double getLength()
 */
JNIEXPORT jdouble JNICALL Java_com_scriptographer_ai_Path_getLength(JNIEnv *env, jobject obj) {
	try {
		// no need to activate document for this
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		AIReal length;
		// Use a default value for flatness since AI doc says it ignores it anyway in CS4
		sAIPath->GetPathLength(handle, &length, 0.1f);
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

AITabletProfile *Path_getTabletData(AIArtHandle handle, AITabletDataType type, int *count) {
	AITabletProfile *data = NULL;
	// First get the number of data:
	*count = 0;
	sAITabletData->GetTabletData(handle, &data, count, type);
	// Create the array
	data = new AITabletProfile[*count];
	// Get the values:
	sAITabletData->GetTabletData(handle, &data, count, type);
	return data;
}

void Path_setTabletData(AIArtHandle handle, AITabletDataType type, AITabletProfile *data, int count) {
#if kPluginInterfaceVersion <= kAI12
	// At least on CS2, setting the size to 0 first seems to be necessary, when
	// tabletData was already in use before. Otherwise Illustrator crashes (#6).
	ASBoolean inUse = false;
	sAITabletData->GetTabletDataInUse(handle, &inUse);
	if (inUse)
		sAITabletData->SetTabletData(handle, NULL, 0, (AITabletDataType) type);
#endif // kPluginInterfaceVersion <= kAI12
	sAITabletData->SetTabletData(handle, data, count, (AITabletDataType) type);
}

/*
 * boolean hasTabletData()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Path_hasTabletData(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		ASBoolean inUse = false;
		sAITabletData->GetTabletDataInUse(handle, &inUse);
		return inUse;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * float[][] nativeGetTabletData(int type)
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_ai_Path_nativeGetTabletData(JNIEnv *env, jobject obj, jint type) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		// Return null if it's not in use:
		ASBoolean inUse = false;
		sAITabletData->GetTabletDataInUse(handle, &inUse);
		if (inUse) {
			// get the tabletData:
			int count = 0;
			AITabletProfile *profiles = Path_getTabletData(handle, (AITabletDataType) type, &count);
			jobjectArray array = NULL;
			for (int i = 0; i < count; i++) {
				jfloatArray entry = (jfloatArray) env->NewFloatArray(2);
				env->SetFloatArrayRegion(entry, 0, 2, (jfloat *) &profiles[i]);
				// We need entry to create the array, so only create it the first time here in the loop
				if (array == NULL)
					array = env->NewObjectArray(count, env->GetObjectClass(entry), NULL);
				env->SetObjectArrayElement(array, i, entry); 
			}
			delete profiles;
			return array;
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void nativeSetTabletData(int type, float[][] data)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Path_nativeSetTabletData(JNIEnv *env, jobject obj, jint type, jobjectArray data) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj, true);
		// Get the tabletData:
		if (data != NULL) {
			// First convert the passed array to a AITabletProfile array:
			int count = env->GetArrayLength(data);
			AITabletProfile *profiles = new AITabletProfile[count];
			for (int i = 0; i < count; i++) {
				jfloatArray entry = (jfloatArray) env->GetObjectArrayElement(data, i);
				env->GetFloatArrayRegion(entry, 0, 2, (jfloat *) &profiles[i]);
			}
			// Now set the new values:
			Path_setTabletData(handle, (AITabletDataType) type, profiles, count);
			sAITabletData->SetTabletDataInUse(handle, true);
			delete profiles;
		} else {
			// Just setting to 0 doesn't seem to do the trick.
			// First set to a straight envelope, then to 0
			AITabletProfile profiles[] = {
				{ 0, 1 },
				{ 1, 1 }
			};
			sAITabletData->SetTabletData(handle, profiles, 2, (AITabletDataType) type);
			// Now set to 0
			sAITabletData->SetTabletData(handle, profiles, 0, (AITabletDataType) type);
			sAITabletData->SetTabletDataInUse(handle, false);
		}
		// Simply swap the closed flag of this path in order to get  
		// Illustrator to recognize the change in the object, because
		// SetTabletData seems be ignored as a change:
		AIBoolean closed;
		if (!sAIPath->GetPathClosed(handle, &closed)) {
			sAIPath->SetPathClosed(handle, !closed);
			sAIPath->SetPathClosed(handle, closed);
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean nativeSplitTabletData(double offset, com.scriptographer.ai.Path other)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Path_nativeSplitTabletData(JNIEnv *env, jobject obj, jdouble offset, jobject other) {
	try {
		AIArtHandle handle1 = gEngine->getArtHandle(env, obj);
		AIArtHandle handle2 = gEngine->getArtHandle(env, other);
		ASBoolean inUse;
		if (!sAITabletData->GetTabletDataInUse(handle1, &inUse) && inUse) {
			for (int type = 0; type < kTabletTypeCount; type++) {
				int count = 0;
				AITabletProfile *data = Path_getTabletData(handle1, (AITabletDataType) type, &count);
				// When splitting, the new arrays need a maximum of the size of the old one.
				int count1 = count, count2 = count;
				AITabletProfile *part1 = new AITabletProfile[count1];
				AITabletProfile *part2 = new AITabletProfile[count2];
				AIErr err = sAITabletData->SplitTabletData(data, count, &part1, &count1, &part2, &count2, (AIReal) offset);
				Path_setTabletData(handle1, (AITabletDataType) type, part1, count1);
				Path_setTabletData(handle2, (AITabletDataType) type, part2, count2);
				delete data;
				delete part1;
				delete part2;
			}
			sAITabletData->SetTabletDataInUse(handle1, true);
			sAITabletData->SetTabletDataInUse(handle2, true);
		}
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean nativeSwapTabletData(double offset)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Path_nativeSwapTabletData(JNIEnv *env, jobject obj, jdouble offset) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		ASBoolean inUse;
		if (!sAITabletData->GetTabletDataInUse(handle, &inUse) && inUse) {
			for (int type = 0; type < kTabletTypeCount; type++) {
				int count = 0;
				AITabletProfile *data = Path_getTabletData(handle, (AITabletDataType) type, &count);
				// When splitting, the new arrays need a maximum of the size of the old one.
				int count1 = count, count2 = count;
				AITabletProfile *part1 = new AITabletProfile[count1];
				AITabletProfile *part2 = new AITabletProfile[count2];
				AIErr err = sAITabletData->SplitTabletData(data, count, &part1, &count1, &part2, &count2, offset);
				count *= 2;
				AITabletProfile *result = new AITabletProfile[count];
				offset = 1 - offset;
				err = sAITabletData->JoinTabletData(part2, count2, part1, count1, &result, &count, offset, offset);
				Path_setTabletData(handle, (AITabletDataType) type, result, count);

				delete data;
				delete part1;
				delete part2;
			}
			sAITabletData->SetTabletDataInUse(handle, true);
		}
	} EXCEPTION_CONVERT(env);
	return false;
}
