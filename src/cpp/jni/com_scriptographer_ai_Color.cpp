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
 * $RCSfile: com_scriptographer_ai_Color.cpp,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/07/22 17:30:56 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_Color.h"

/*
 * com.scriptographer.ai.Color
 */

/*
 * com.scriptographer.ai.Color convert(int conversion)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Color_convert(JNIEnv *env, jobject obj, jint type) {
	try {
		AIColor col;
		AIReal alpha;
		gEngine->convertColor(env, obj, &col, &alpha);
		// type -> conversion tranlsation table:
		static AIColorConversionSpaceValue conversionTypes[] = {
			kAIRGBColorSpace,
			kAICMYKColorSpace,
			kAIGrayColorSpace,
			kAIMonoColorSpace,
			kAIARGBColorSpace,
			kAIACMYKColorSpace,
			kAIAGrayColorSpace,
			kAIMonoColorSpace
		};
		if (gEngine->convertColor(&col, conversionTypes[type], &col, alpha, &alpha) != NULL) {
			return gEngine->convertColor(env, &col, alpha);
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * java.awt.color.ICC_Profile getWSProfile(int whichSpace)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Color_getWSProfile(JNIEnv *env, jclass cls, jint whichSpace) {
	jobject ret = NULL;
	try {
		AIColorProfile profile;
		AIErr err = sAIOverrideColorConversion->GetWSProfile(whichSpace, &profile);
		if (err == kNoErr) {
			ASUInt32 size;
			// first get the size...
			if (!sAIOverrideColorConversion->GetProfileData(profile, &size, NULL)) {
				// now the data:
				char *data = new char[size];
				if (!sAIOverrideColorConversion->GetProfileData(profile, &size, data)) {
					// now use ICC_Profile.getInstance to create a ICC_Profile from it:
					jbyteArray dataArray = env->NewByteArray(size); 
					env->SetByteArrayRegion(dataArray, 0, size, (jbyte *) data); 
					ret = gEngine->callStaticObjectMethod(env, gEngine->cls_awt_ICC_Profile, gEngine->mid_awt_ICC_Profile_getInstance, dataArray);
				}
				delete data;
			}
			sAIOverrideColorConversion->FreeProfile(profile);
		}
	} EXCEPTION_CONVERT(env)
	return ret;
}
