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
 * $RCSfile: com_scriptographer_ai_SegmentList.cpp,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/04/20 13:49:36 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_SegmentList.h"

/*
 * com.scriptographer.ai.SegmentList
 */

/*
 * int nativeGetSize(int handle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_SegmentList_nativeGetSize(JNIEnv *env, jclass cls, jint handle) {
	try {
		short count;
		if (sAIPath->GetPathSegmentCount((AIArtHandle)handle, &count))
			throw new StringException("Cannot get path segments count");
		return count;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void nativeFetch(int handle, int index, int count, float[] values)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_SegmentList_nativeFetch(JNIEnv *env, jclass cls, jint handle, jint index, jint count, jfloatArray values) {
	try {
		// use this ugly but fast hack: the AIPathSegment represents roughly an array 
		// of 6 floats (for the 3 AIRealPoints p, in, out) + a char (boolean corner)
		// due to the data alignment, there are 3 empty bytes after the char.
		// when using a float array with seven elements, the first 6 are set correctly.
		// the first byte of the 7th represents the boolean value. if this float is set
		// to 0 before fetching, it will be == 0 if false, and != 0 if true, so that's 
		// all we want in the java environment!
		
		if (count == 1) {
			// for only one segment, this seems to be faster than the GetPrimitiveArrayCritical way.
			jfloat data[com_scriptographer_ai_SegmentList_VALUES_PER_SEGMENT];
			int i = com_scriptographer_ai_SegmentList_VALUES_PER_SEGMENT * sizeof(float);
			int j = sizeof(AIPathSegment);
			data[6] = 0; // make shure the upper 3 bytes are not set to arbitrary values
			if (sAIPath->GetPathSegments((AIArtHandle) handle, index, 1, (AIPathSegment *) data))
				throw new StringException("Cannot get path segment");
		
			// now write this values into the float array that was passed and we're done. 
			env->SetFloatArrayRegion(values, 0, com_scriptographer_ai_SegmentList_VALUES_PER_SEGMENT, data); 
		} else {
			jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(values, NULL); 
			ASErr error = sAIPath->GetPathSegments((AIArtHandle) handle, index, count, (AIPathSegment *) data);
			env->ReleasePrimitiveArrayCritical(values, data, 0);
			if (error)
				throw new StringException("Cannot get path segments");
		}
		EXCEPTION_CHECK(env)
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeCommit(int handle, int index, float ptx, float pty, float inx, float iny, float outx, float outy, boolean corner)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_SegmentList_nativeCommit__IIFFFFFFZ(JNIEnv *env, jclass cls, jint handle, jint index, jfloat ptx, jfloat pty, jfloat inx, jfloat iny, jfloat outx, jfloat outy, jboolean corner) {
	try {
		DEFINE_SEGMENT(segment, ptx, pty, inx, iny, outx, outy, corner);
		if (sAIPath->SetPathSegments((AIArtHandle)handle, index, 1, &segment))
			throw new StringException("Cannot set path segments");
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeCommit(int  handle, int index, int count, float[] values)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_SegmentList_nativeCommit__III_3F(JNIEnv *env, jclass cls, jint handle, jint index, jint count, jfloatArray values) {
	try {
		if (count == 1) {
			// for only one segment, this seems to be faster than the GetPrimitiveArrayCritical way.
			jfloat data[com_scriptographer_ai_SegmentList_VALUES_PER_SEGMENT];
			env->GetFloatArrayRegion(values, 0, com_scriptographer_ai_SegmentList_VALUES_PER_SEGMENT, data);
			if (sAIPath->SetPathSegments((AIArtHandle) handle, index, 1, (AIPathSegment *) data))
				throw new StringException("Cannot get path segment");
		} else {
			jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(values, NULL); 
			ASErr error = sAIPath->SetPathSegments((AIArtHandle) handle, index, count, (AIPathSegment *) data);
			env->ReleasePrimitiveArrayCritical(values, data, 0);
			if (error)
				throw new StringException("Cannot get path segments");
		}
		EXCEPTION_CHECK(env)
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeInsert(int handle, int index, float ptx, float pty, float inx, float iny, float outx, float outy, boolean corner)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_SegmentList_nativeInsert__IIFFFFFFZ(JNIEnv *env, jclass cls, jint handle, jint index, jfloat ptx, jfloat pty, jfloat inx, jfloat iny, jfloat outx, jfloat outy, jboolean corner) {
	try {
		DEFINE_SEGMENT(segment, ptx, pty, inx, iny, outx, outy, corner);
		if (sAIPath->InsertPathSegments((AIArtHandle)handle, index, 1, &segment))
			throw new StringException("Cannot insert path segments");
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeInsert(int handle, int index, int count, float[] values)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_SegmentList_nativeInsert__III_3F(JNIEnv *env, jclass cls, jint handle, jint index, jint count, jfloatArray values) {
	try {
		if (count == 1) {
			// for only one segment, this seems to be faster than the GetPrimitiveArrayCritical way.
			jfloat data[com_scriptographer_ai_SegmentList_VALUES_PER_SEGMENT];
			env->GetFloatArrayRegion(values, 0, com_scriptographer_ai_SegmentList_VALUES_PER_SEGMENT, data);
			if (sAIPath->InsertPathSegments((AIArtHandle) handle, index, 1, (AIPathSegment *) data))
				throw new StringException("Cannot get path segment");
		} else {
			jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(values, NULL); 
			ASErr error = sAIPath->InsertPathSegments((AIArtHandle) handle, index, count, (AIPathSegment *) data);
			env->ReleasePrimitiveArrayCritical(values, data, 0);
			if (error)
				throw new StringException("Cannot get path segments");
		}
		EXCEPTION_CHECK(env)
	} EXCEPTION_CONVERT(env)
}

/*
 * int nativeRemove(int handle, int index, int count)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_SegmentList_nativeRemove(JNIEnv *env, jclass cls, jint handle, jint index, jint count) {
	try {
		if (sAIPath->DeletePathSegments((AIArtHandle)handle, index, count))
			throw new StringException("Cannot delete path segments");
		short count;
		if (sAIPath->GetPathSegmentCount((AIArtHandle)handle, &count))
			throw new StringException("Cannot get path segments count");
		return count;
	} EXCEPTION_CONVERT(env)
	return 0;
}

 
/*
 * void nativeReverse(int handle)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_SegmentList_nativeReverse(JNIEnv *env, jclass cls, jint handle) {
	try {
		if (sAIPath->ReversePathSegments((AIArtHandle)handle))
			throw new StringException("Cannot reverse path segments");
	} EXCEPTION_CONVERT(env)
}
