/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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

#include "StdHeaders.h"
#include "ScriptographerPlugin.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_sg_Timer.h"

/*
 * com.scriptographer.sg.Timer
 */

/*
 * int nativeCreate(java.lang.String name, int period)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_sg_Timer_nativeCreate(JNIEnv *env, jobject obj, jstring name, jint period) {
	try {
		char *str = gEngine->convertString(env, name);
		AITimerHandle timer = NULL;
		sAITimer->AddTimer(gPlugin->getPluginRef(), str, period, &timer);
		sAITimer->SetTimerActive(timer, false);
		delete str;
		return (jint) timer;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * boolean nativeSetActive(int handle, boolean active)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_sg_Timer_nativeSetActive(JNIEnv *env, jobject obj, jint handle, jboolean active) {
	return !sAITimer->SetTimerActive((AITimerHandle) handle, active);
}

/*
 * boolean nativeSetPeriod(int handle, int period)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_sg_Timer_nativeSetPeriod(JNIEnv *env, jobject obj, jint handle, jint period) {
	return !sAITimer->SetTimerPeriod((AITimerHandle) handle, period);
}

/*
 * java.util.ArrayList nativeGetTimers()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_sg_Timer_nativeGetTimers(JNIEnv *env, jclass cls) {
	try {
		if (gEngine != NULL) {
			jobject array = gEngine->newObject(env, gEngine->cls_ArrayList, gEngine->cid_ArrayList);
			long count;
			sAITimer->CountTimers(&count);
			SPPluginRef plugin = gPlugin->getPluginRef();
			for (int i = 0; i < count; i++) {
				AITimerHandle timer;
				SPPluginRef timerPlugin;
				if (!sAITimer->GetNthTimer(i, &timer) &&
					!sAITimer->GetTimerPlugin(timer, &timerPlugin) &&
					plugin == timerPlugin) {
					// Create the wrapper...
					jobject timerObj = gEngine->newObject(env, gEngine->cls_sg_Timer, gEngine->cid_sg_Timer, (jint) timer);
					// ...and add it to the array
					gEngine->callObjectMethod(env, array, gEngine->mid_Collection_add, timerObj);
				}
			}
			return array;
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}
