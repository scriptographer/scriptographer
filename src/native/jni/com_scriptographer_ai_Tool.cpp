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

#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "ScriptographerPlugin.h"
#include "resourceIds.h"
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
 * int getEventInterval()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Tool_getEventInterval(JNIEnv *env, jobject obj) {
	try {
		AIToolHandle tool = gEngine->getToolHandle(env, obj);
		AIToolTime interval;
		if (!sAITool->GetToolNullEventInterval(tool, &interval))
			return interval >= 0 ? (jint) interval * 1000 : -1;
	} EXCEPTION_CONVERT(env);
	return -1;
}

/*
 * void setEventInterval(int interval)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tool_setEventInterval(JNIEnv *env, jobject obj, jint interval) {
	try {
		AIToolHandle tool = gEngine->getToolHandle(env, obj);
		sAITool->SetToolNullEventInterval(tool,  (AIToolTime) (interval >= 0 ? double(interval) / 1000.0 : -1));
	} EXCEPTION_CONVERT(env);
}

/*
 * java.util.ArrayList nativeGetTools()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Tool_nativeGetTools(JNIEnv *env, jclass cls) {
	try {
		if (gEngine != NULL) {
			jobject array = gEngine->newObject(env, gEngine->cls_ArrayList, gEngine->cid_ArrayList);
			long count;
			sAITool->CountTools(&count);
			SPPluginRef plugin = gPlugin->getPluginRef();
			for (int i = 0; i < count; i++) {
				AIToolHandle tool;
				SPPluginRef toolPlugin;
				if (!sAITool->GetNthTool(i, &tool) &&
					!sAITool->GetToolPlugin(tool, &toolPlugin) &&
					plugin == toolPlugin) {
					char *name;
					sAITool->GetToolName(tool, &name);
					// Create the wrapper
					jobject toolObj = gEngine->newObject(env, gEngine->cls_ai_Tool, gEngine->cid_ai_Tool, (jint) tool, gEngine->convertString(env, name));
					// And add it to the array
					gEngine->callObjectMethod(env, array, gEngine->mid_Collection_add, toolObj);
				}
			}
			return array;
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * int nativeCreate(java.lang.String name, int iconHandle, int options, int groupTool, int toolsetTool)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Tool_nativeCreate(JNIEnv *env, jobject obj, jstring name, jint iconHandle, jint options, jint groupTool, jint toolsetTool) {
	try {
		if (gPlugin->isStarted())
			throw new StringException("Tools can only be created on startup");

		AIAddToolData data;
		char *title = gEngine->convertString(env, name);
		
		data.title = title;
		data.tooltip = title;

		data.icon =  iconHandle != 0 
			? (ADMIconRef) iconHandle 
			: sADMIcon->GetFromResource(gPlugin->getPluginRef(), NULL, kEmptyIconID, 0);

		ASErr error = kNoErr;

		// TODO: handle errors
		if (groupTool != 0) {
			error = sAITool->GetToolNumberFromHandle((AIToolHandle) groupTool, &data.sameGroupAs);
//			if (error) return error;
		} else {
			data.sameGroupAs = kNoTool;
		}

		if (toolsetTool != 0) {
			error = sAITool->GetToolNumberFromHandle((AIToolHandle) toolsetTool, &data.sameToolsetAs);
//			if (error) return error;
		} else {
			data.sameToolsetAs = kNoTool;
		}

		AIToolHandle handle = NULL;
		// Always set kToolWantsToTrackCursorOption option on creation of the tool,
		// since setting that option after seems to have no effect and we need cursor updates.
		// TODO: Find out if this is true for all options, in which case remove get / setOptions for later modification.
		error = sAITool->AddTool(gPlugin->getPluginRef(), title, &data, options | kToolWantsToTrackCursorOption, &handle);
//		if (error) return error;

		delete title;

		return (jint) handle;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeGetOptions()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Tool_nativeGetOptions(JNIEnv *env, jobject obj) {
	try {
		AIToolHandle tool = gEngine->getToolHandle(env, obj);
		long options = 0;
		sAITool->GetToolOptions(tool, &options);
		return (jint) options;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void nativeSetOptions(int options)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tool_nativeSetOptions(JNIEnv *env, jobject obj, jint options) {
	try {
		AIToolHandle tool = gEngine->getToolHandle(env, obj);
		sAITool->SetToolOptions(tool, options);
	} EXCEPTION_CONVERT(env);
}

/*
 * java.lang.String getTitle()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_Tool_getTitle(JNIEnv *env, jobject obj) {
	try {
		AIToolHandle tool = gEngine->getToolHandle(env, obj);
		char *title;
		sAITool->GetToolTitle(tool, &title);
		return gEngine->convertString(env, title);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setTitle(java.lang.String title)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tool_setTitle(JNIEnv *env, jobject obj, jstring title) {
	try {
		if (title != NULL) {
			AIToolHandle tool = gEngine->getToolHandle(env, obj);
			char *str = gEngine->convertString(env, title);
			sAITool->SetToolTitle(tool, str);
			delete str;
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * java.lang.String getTooltip()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_Tool_getTooltip(JNIEnv *env, jobject obj) {
	try {
		AIToolHandle tool = gEngine->getToolHandle(env, obj);
		char *title;
		sAITool->GetTooltip(tool, &title);
		return gEngine->convertString(env, title);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setTooltip(java.lang.String text)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tool_setTooltip(JNIEnv *env, jobject obj, jstring text) {
	try {
		if (text != NULL) {
			AIToolHandle tool = gEngine->getToolHandle(env, obj);
			char *str = gEngine->convertString(env, text);
			sAITool->SetTooltip(tool, str);
			delete str;
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean getSelected()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Tool_getSelected(JNIEnv *env, jobject obj) {
	try {
		AIToolHandle tool = gEngine->getToolHandle(env, obj);
		AIToolHandle selected;
		if (!sAITool->GetSelectedTool(&selected) && selected == tool)
			return true;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setSelected(boolean selected)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tool_setSelected(JNIEnv *env, jobject obj, jboolean selected) {
	try {
		AIToolHandle tool = gEngine->getToolHandle(env, obj);
		if (!selected) {
			// If we're deselecting, we need to select the previous or next tool instead
			AIToolType toolNum;
			long count;
			if (!sAITool->GetToolNumberFromHandle(tool, &toolNum)
					&& !sAITool->CountTools(&count)) {
				if (toolNum < count - 1)
					toolNum++;
				else
					toolNum--;
				if (sAITool->GetToolHandleFromNumber(toolNum, &tool))
					tool = NULL;
			}
		}
		if (tool != NULL) {
			sAITool->SetSelectedTool(tool);
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetImage(int iconHandle)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tool_nativeSetImage(JNIEnv *env, jobject obj, jint iconHandle) {
	try {
		AIToolHandle tool = gEngine->getToolHandle(env, obj);
		sAITool->SetToolIcon(tool, (ADMIconRef) iconHandle);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetRolloverImage(int iconHandle)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Tool_nativeSetRolloverImage(JNIEnv *env, jobject obj, jint iconHandle) {
	try {
		AIToolHandle tool = gEngine->getToolHandle(env, obj);
		sAITool->SetToolRolloverIcon(tool, (ADMIconRef) iconHandle);
	} EXCEPTION_CONVERT(env);
}
