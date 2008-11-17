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
 * $Id: com_scriptographer_ui_HierarchyListEntry.cpp 578 2008-07-22 21:15:16Z lehni $
 */
 
#include "stdHeaders.h"
#include "ScriptographerPlugin.h"
#include "ScriptographerEngine.h"
#include "admGlobals.h"
#include "com_scriptographer_ui_HierarchyListEntry.h"

/*
 * com.scriptographer.ui.HierarchyEntry
 */

void ASAPI HierarchyListEntry_onDestroy(ADMListEntryRef entry) {
	// this seems to be necessary:
	sADMListEntry->SetPicture(entry, NULL);
	sADMListEntry->SetDisabledPicture(entry, NULL);
	sADMListEntry->SetSelectedPicture(entry, NULL);

	if (gEngine != NULL) {
		JNIEnv *env = gEngine->getEnv();
		try {
			jobject obj = gEngine->getListEntryObject(entry);
			// call onDestry on the entry object
			gEngine->callOnDestroy(obj);
			// clear the handle
			gEngine->setIntField(env, obj, gEngine->fid_ui_NativeObject_handle, 0);
			env->DeleteGlobalRef(obj);
			// if the object is the last of its parent list, remove the parent as well. do like that so that
			// all the itmes destory proc get called before the parent's!
			// but only call if it's not the root list:
			ADMHierarchyListRef list = sADMListEntry->GetList(entry);
			if (sADMHierarchyList->NumberOfEntries(list) == 0 && sADMHierarchyList->GetParentEntry(list) != NULL) {
				HierarchyList_onDestroy(list);
			}
		} EXCEPTION_CATCH_REPORT(env);
	}
}

void ASAPI HierarchyListEntry_onNotify(ADMListEntryRef entry, ADMNotifierRef notifier) {
	sADMListEntry->DefaultNotify(entry, notifier);
	ADMHierarchyListRef list = sADMListEntry->GetList(entry);
	jobject entryObj = gEngine->getListEntryObject(entry);
	gEngine->callOnNotify(entryObj, notifier);
}

ASBoolean ASAPI HierarchyListEntry_onTrack(ADMListEntryRef entry, ADMTrackerRef tracker) {
	ADMHierarchyListRef list = sADMListEntry->GetList(entry);
	jobject entryObj = gEngine->getListEntryObject(entry);
	ASBoolean ret = gEngine->callOnTrack(entryObj, tracker);
	if (ret)
		ret = sADMListEntry->DefaultTrack(entry, tracker);
	return ret;
}

void ASAPI HierarchyListEntry_onDraw(ADMListEntryRef entry, ADMDrawerRef drawer) {
	sADMListEntry->DefaultDraw(entry, drawer);
	ADMHierarchyListRef list = sADMListEntry->GetList(entry);
	jobject entryObj = gEngine->getListEntryObject(entry);
	gEngine->callOnDraw(entryObj, drawer);
}

/*
 * com.scriptographer.ui.Item getItem()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_HierarchyListEntry_getItem(JNIEnv *env, jobject obj) {
	try {
		ADMListEntryRef entry = gEngine->getHierarchyListEntryHandle(env, obj);
		return gEngine->getItemObject(sADMListEntry->GetItem(entry));
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setHierarchyExpanded(boolean expanded)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_HierarchyListEntry_setExpanded(JNIEnv *env, jobject obj, jboolean expanded) {
	try {
		ADMListEntryRef entry = gEngine->getHierarchyListEntryHandle(env, obj);
		sADMListEntry->ExpandHierarchy(entry, expanded);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isHierarchyExpanded()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ui_HierarchyListEntry_isExpanded(JNIEnv *env, jobject obj) {
	try {
		ADMListEntryRef entry = gEngine->getHierarchyListEntryHandle(env, obj);
		return sADMListEntry->IsHierarchyExpanded(entry);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setEntryNameHidden(boolean nameHidden)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_HierarchyListEntry_setEntryNameHidden(JNIEnv *env, jobject obj, jboolean nameHidden) {
	try {
		ADMListEntryRef entry = gEngine->getHierarchyListEntryHandle(env, obj);
		sADMListEntry->HideEntryName(entry, nameHidden);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isEntryNameHidden()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ui_HierarchyListEntry_isEntryNameHidden(JNIEnv *env, jobject obj) {
	try {
		ADMListEntryRef entry = gEngine->getHierarchyListEntryHandle(env, obj);
		return sADMListEntry->IsEntryNameHidden(entry);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setChildSelectable(boolean selectable)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_HierarchyListEntry_setChildSelectable(JNIEnv *env, jobject obj, jboolean selectable) {
	try {
		ADMListEntryRef entry = gEngine->getHierarchyListEntryHandle(env, obj);
		sADMListEntry->EnableChildSelection(entry, selectable);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isChildSelectable()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ui_HierarchyListEntry_isChildSelectable(JNIEnv *env, jobject obj) {
	try {
		ADMListEntryRef entry = gEngine->getHierarchyListEntryHandle(env, obj);
		return sADMListEntry->IsChildSelectable(entry);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * int getHierarchyDepth()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ui_HierarchyListEntry_getDepth(JNIEnv *env, jobject obj) {
	try {
		ADMListEntryRef entry = gEngine->getHierarchyListEntryHandle(env, obj);
		return sADMListEntry->GetHierarchyDepth(entry);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int getVisualHierarchyDepth()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ui_HierarchyListEntry_getVisualDepth(JNIEnv *env, jobject obj) {
	try {
		ADMListEntryRef entry = gEngine->getHierarchyListEntryHandle(env, obj);
		return sADMListEntry->GetVisualHierarchyDepth(entry);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * boolean areChildrenSelected()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ui_HierarchyListEntry_areChildrenSelected(JNIEnv *env, jobject obj) {
	try {
		ADMListEntryRef entry = gEngine->getHierarchyListEntryHandle(env, obj);
		return sADMListEntry->AreChildrenSelected(entry);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * com.scriptographer.ai.Rectangle getExpandArrowLocalRect()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_HierarchyListEntry_getExpandArrowRect(JNIEnv *env, jobject obj) {
	try {
		ADMListEntryRef entry = gEngine->getHierarchyListEntryHandle(env, obj);
		ADMRect rt;
		sADMListEntry->GetExpandArrowLocalRect(entry, &rt);
		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setEntryTextRect(com.scriptographer.ai.Rectangle rect)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_HierarchyListEntry_setTextRect(JNIEnv *env, jobject obj, jobject rect) {
	try {
		ADMListEntryRef entry = gEngine->getHierarchyListEntryHandle(env, obj);
		ADMRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		sADMListEntry->SetEntryTextRect(entry, &rt);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ui.Item getEntryItem()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ui_HierarchyListEntry_getEntryItem(JNIEnv *env, jobject obj) {
	try {
		ADMListEntryRef entry = gEngine->getHierarchyListEntryHandle(env, obj);
		ADMItemRef itm = sADMListEntry->GetEntryItem(entry);
		if (itm != NULL)
			return gEngine->getItemObject(itm);	
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setEntryItem(com.scriptographer.ui.Item item)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_HierarchyListEntry_setEntryItem(JNIEnv *env, jobject obj, jobject item) {
	try {
		ADMListEntryRef entry = gEngine->getHierarchyListEntryHandle(env, obj);
		ADMItemRef itm = gEngine->getItemHandle(env, item);
		sADMListEntry->SetEntryItem(entry, itm);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetFont(int font)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_HierarchyListEntry_nativeSetFont(JNIEnv *env, jobject obj, jint font) {
	try {
		ADMListEntryRef entry = gEngine->getHierarchyListEntryHandle(env, obj);
		sADMListEntry->SetFont(entry, (ADMFont)font);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetTextColor(int color)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_HierarchyListEntry_nativeSetTextColor(JNIEnv *env, jobject obj, jint color) {
	try {
		ADMListEntryRef entry = gEngine->getHierarchyListEntryHandle(env, obj);
		sADMListEntry->SetTextColor(entry, (ADMColor)color);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetBackgroundColor(int color)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_HierarchyListEntry_nativeSetBackgroundColor(JNIEnv *env, jobject obj, jint color) {
	try {
		ADMListEntryRef entry = gEngine->getHierarchyListEntryHandle(env, obj);
		sADMListEntry->SetBackgroundColor(entry, (ADMColor)color);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetDividerColor(int color)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_HierarchyListEntry_nativeSetDividerColor(JNIEnv *env, jobject obj, jint color) {
	try {
		ADMListEntryRef entry = gEngine->getHierarchyListEntryHandle(env, obj);
		sADMListEntry->SetDividingLineColor(entry, (ADMColor)color);
	} EXCEPTION_CONVERT(env);
}
