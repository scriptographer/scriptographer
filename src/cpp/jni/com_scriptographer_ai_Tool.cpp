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
 * $RCSfile: com_scriptographer_ai_Tool.cpp,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/03/25 00:27:58 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "Plugin.h"
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
		sAITool->SystemHasPressure(&hasPressure);
		return hasPressure;
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * java.util.HashMap nativeGetTools()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Tool_nativeGetTools(JNIEnv *env, jclass cls) {
	try {
		jobject map = gEngine->newObject(env, gEngine->cls_HashMap, gEngine->cid_HashMap);
		long count;
		sAITool->CountTools(&count);
		SPPluginRef plugin = gPlugin->getPluginRef();
		for (int i = 0; i < count; i++) {
			AIToolHandle tool;
			sAITool->GetNthTool(i, &tool);
			SPPluginRef toolPlugin;
			sAITool->GetToolPlugin(tool, &toolPlugin);
			if (plugin == toolPlugin) {
				char *title;
				sAITool->GetToolTitle(tool, &title);
				// extract the index from the title, assume that the last word is a number:
				int index = atoi(strrchr(title, ' ')) - 1;
				jobject toolObj = gEngine->newObject(env, gEngine->cls_Tool, gEngine->cid_Tool, (jint) tool, index);
				jobject keyObj = gEngine->newObject(env, gEngine->cls_Handle, gEngine->cid_Handle, (jint) tool);
				gEngine->callObjectMethod(env, map, gEngine->mid_Map_put, keyObj, toolObj);
			}
		}
		return map;
	} EXCEPTION_CONVERT(env)
	return NULL;
}
