/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile: com_scriptographer_ai_Tool.cpp,v $
 * $Author: lehni $
 * $Revision: 1.13 $
 * $Date: 2006/10/18 14:17:18 $
 */

#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "ScriptographerPlugin.h"
#include "com_scriptographer_ai_Tool.h"

/*
 * com.scriptographer.ai.Tool
 */

/*
 * boolean hasPressure()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Tool_hasPressure(JNIEnv *env, jobject obj) {
	try {
		ASBoolean hasPressure = false;
		if (!sAITool->SystemHasPressure(&hasPressure))
			return hasPressure;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * int getIdleEventInterval()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Tool_getIdleEventInterval(JNIEnv *env, jobject obj) {
	try {
		AIToolHandle tool = gEngine->getToolHandle(env, obj);
		AIToolTime interval;
		if (!sAITool->GetToolNullEventInterval(tool, &interval))
			return interval >= 0 ? (jint) interval * 1000 : -1;
	} EXCEPTION_CONVERT(env);
	return -1;
}

/*
 * void setIdleEventInterval(int interval)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tool_setIdleEventInterval(JNIEnv *env, jobject obj, jint interval) {
	try {
		AIToolHandle tool = gEngine->getToolHandle(env, obj);
		sAITool->SetToolNullEventInterval(tool,  (AIToolTime) (interval >= 0 ? double(interval) / 1000.0 : -1));
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.util.ReferenceMap nativeGetTools()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Tool_nativeGetTools(JNIEnv *env, jclass cls) {
	try {
		jobject map = gEngine->newObject(env, gEngine->cls_IntMap, gEngine->cid_IntMap);
		int count;
		Tool *tools = gPlugin->getTools(&count);
		for (int i = 0; i < count; i++) {
			AIToolHandle handle = tools[i].handle;
			jobject toolObj = gEngine->newObject(env, gEngine->cls_Tool, gEngine->cid_Tool, (jint) handle, i);
			gEngine->callObjectMethod(env, map, gEngine->mid_IntMap_put, (jint) handle, toolObj);
		}
		return map;
	} EXCEPTION_CONVERT(env);
	return NULL;
}
