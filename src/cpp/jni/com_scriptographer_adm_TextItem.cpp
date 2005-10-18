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
 * $RCSfile: com_scriptographer_adm_TextItem.cpp,v $
 * $Author: lehni $
 * $Revision: 1.4 $
 * $Date: 2005/10/18 15:35:46 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_adm_TextItem.h"

/*
 * com.scriptographer.adm.TextItem
 */

/*
 * void setText(java.lang.String text)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_TextItem_setText(JNIEnv *env, jobject obj, jstring text) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		const jchar *chars = env->GetStringChars(text, NULL);
		if (chars == NULL) EXCEPTION_CHECK(env)
		sADMItem->SetTextW(item, chars);
		env->ReleaseStringChars(text, chars);
	} EXCEPTION_CONVERT(env)
}

/*
 * java.lang.String getText()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_adm_TextItem_getText(JNIEnv *env, jobject obj) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		long len = sADMItem->GetTextLength(item);
		jchar *chars = new jchar[len];
		sADMItem->GetTextW(item, chars, len);
		jstring res = env->NewString(chars, len);
		if (res == NULL) EXCEPTION_CHECK(env)
		delete chars;
		return res;
	} EXCEPTION_CONVERT(env)
	return NULL;
}
