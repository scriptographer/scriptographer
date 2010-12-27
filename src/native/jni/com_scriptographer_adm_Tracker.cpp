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
#include "ScriptographerEngine.h"
#include "com_scriptographer_adm_Tracker.h"

/*
 * com.scriptographer.adm.Tracker
 */

/*
 * int getCurrentModifiers()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Tracker_getCurrentModifiers(JNIEnv *env, jclass cls) {
	try {
		return (jint)sADMTracker->GetModifiers(NULL);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void abort()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Tracker_abort(JNIEnv *env, jobject obj) {
	try {
		ADMTrackerRef tracker = gEngine->getTrackerHandle(env, obj);
		sADMTracker->Abort(tracker);
	} EXCEPTION_CONVERT(env);
}

/*
 * void releaseMouseCapture()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Tracker_releaseMouseCapture(JNIEnv *env, jobject obj) {
	try {
		ADMTrackerRef tracker = gEngine->getTrackerHandle(env, obj);
		sADMTracker->ReleaseMouseCapture(tracker);
	} EXCEPTION_CONVERT(env);
}
