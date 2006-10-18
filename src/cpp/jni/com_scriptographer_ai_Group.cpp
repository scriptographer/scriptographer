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
 * $RCSfile: com_scriptographer_ai_Group.cpp,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2006/10/18 14:17:17 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_Group.h"

/*
 * com.scriptographer.ai.Group
 */

/*
 * boolean isClipped()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Group_isClipped(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		AIBoolean clipped;
		if (!sAIGroup->GetGroupClipped(handle, &clipped))
			return clipped;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setClipped(boolean clipped)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Group_setClipped(JNIEnv *env, jobject obj, jboolean clipped) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj, true);
		sAIGroup->SetGroupClipped(handle, clipped);
	} EXCEPTION_CONVERT(env);
}
