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
