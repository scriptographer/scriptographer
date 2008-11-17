/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id: com_scriptographer_ui_ImageStatic.cpp 578 2008-07-22 21:15:16Z lehni $
 */

#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ui_ImageStatic.h"

/*
 * com.scriptographer.ui.ImageStatic
 */

/*
 * void nativeSetImage(int iconRef)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_ImageStatic_nativeSetImage(JNIEnv *env, jobject obj, jint iconRef) {
	try {
	    ADMItemRef item = gEngine->getItemHandle(env, obj);
		sADMItem->SetPicture(item, (ADMIconRef)iconRef);
	} EXCEPTION_CONVERT(env);
}
