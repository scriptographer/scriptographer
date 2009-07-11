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
 * $Id$
 */

#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_FileFormat.h"

/*
 * com.scriptographer.ai.FileFormat
 */

/*
 * java.util.ArrayList getFileFormats()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_FileFormat_getFileFormats(JNIEnv *env, jclass cls) {
	try {
		long count;
		if (!sAIFileFormat->CountFileFormats(&count)) {
			jobject array = gEngine->newObject(env, gEngine->cls_ArrayList, gEngine->cid_ArrayList);
			for (int i = 0; i < count; i++) {
				AIFileFormatHandle format;
				if (!sAIFileFormat->GetNthFileFormat(i, &format)) {
					char *name;
					char title[256];
					char extension[256];
					long options;
					if (!sAIFileFormat->GetFileFormatName(format, &name) &&
						!sAIFileFormat->GetFileFormatTitle(format, title) &&
						!sAIFileFormat->GetFileFormatExtension(format, extension) &&
						!sAIFileFormat->GetFileFormatOptions(format, &options)) {
						if (strcmp(name, "") != 0 && (
							options & kFileFormatExport ||
							options & kFileFormatWrite)) {
							jobject formatObj = gEngine->newObject(env, gEngine->cls_ai_FileFormat, gEngine->cid_ai_FileFormat,
								(jint) format,
								gEngine->convertString(env, name),
								gEngine->convertString(env, title),
								gEngine->convertString(env, extension),
								(jlong) options
							);
							gEngine->callObjectMethod(env, array, gEngine->mid_Collection_add, formatObj);
						}
					}
				}
			}
			return array;
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}
