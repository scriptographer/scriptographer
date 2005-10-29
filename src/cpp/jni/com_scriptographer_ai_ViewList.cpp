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
 * $RCSfile: com_scriptographer_ai_ViewList.cpp,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/10/29 10:18:38 $
 */

#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_ViewList.h"

/*
 * com.scriptographer.ai.ViewList
 */

#define VIEWLIST_BEGIN \
	AIDocumentHandle activeDoc = NULL; \
	AIDocumentHandle prevDoc = NULL; \
	sAIDocument->GetDocument(&activeDoc); \
	if (activeDoc != (AIDocumentHandle) docHandle) { \
		prevDoc = activeDoc; \
		sAIDocumentList->Activate((AIDocumentHandle) docHandle, false); \
	} \

#define VIEWLIST_END \
	if (prevDoc != NULL) \
		sAIDocumentList->Activate(prevDoc, false);

/*
 * int nativeGetLength(int docHandle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_ViewList_nativeGetLength(JNIEnv *env, jclass cls, jint docHandle) {
	VIEWLIST_BEGIN

	long count = 0;
	sAIDocumentView->CountDocumentViews(&count);

	VIEWLIST_END

	return (jint) count;
}

/*
 * int nativeGetActiveView(int docHandle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_ViewList_nativeGetActiveView(JNIEnv *env, jclass cls, jint docHandle) {
	VIEWLIST_BEGIN

	AIDocumentViewHandle view = NULL;
	// the active view is at index 0:
	sAIDocumentView->GetNthDocumentView(0, &view);

	VIEWLIST_END

	return (jint) view;
}

/*
 * int nativeGetView(int docHandle, int index)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_ViewList_nativeGet(JNIEnv *env, jclass cls, jint docHandle, jint index) {
	VIEWLIST_BEGIN

	AIDocumentViewHandle view = NULL;
	// according to the documentation, the views start at index 1:
	sAIDocumentView->GetNthDocumentView(index + 1, &view);
	
	VIEWLIST_END

	return (jint) view;
}
