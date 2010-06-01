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
 */

#include "stdHeaders.h"
#include "ScriptographerPlugin.h"
#include "ScriptographerEngine.h"
#include "uiGlobals.h"
#include "com_scriptographer_adm_HierarchyListBox.h"

/*
 * com.scriptographer.adm.HierarchyListBox
 */
 
// lists don't have init callbacks that automatically get called, but just for simetry let's use the same scheme:
ASErr ASAPI HierarchyListBox_onInit(ADMHierarchyListRef list) {
	DEFINE_CALLBACK_PROC(HierarchyListEntry_onDestroy);
	sADMHierarchyList->SetDestroyProc(list, (ADMListEntryDestroyProc) CALLBACK_PROC(HierarchyListEntry_onDestroy));
	DEFINE_CALLBACK_PROC(HierarchyListEntry_onNotify);
	sADMHierarchyList->SetNotifyProc(list, (ADMListEntryNotifyProc) CALLBACK_PROC(HierarchyListEntry_onNotify));
	// These are activated in enable****Callback
	// sADMHierarchyList->SetTrackProc(list, HierarchyListEntry_onTrack);
	// sADMHierarchyList->SetDrawProc(list, HierarchyListEntry_onDraw);
	return kNoErr;
}

void ASAPI HierarchyListBox_onDestroy(ADMHierarchyListRef list) {
	if (gEngine != NULL) {
		jobject listObj = gEngine->getListObject(list);
		JNIEnv *env = gEngine->getEnv();
		sADMHierarchyList->SetUserData(list, NULL);
		// clear the handle
		gEngine->setIntField(env, listObj, gEngine->fid_adm_ListItem_listHandle, 0);
		// and remove global ref
		env->DeleteGlobalRef(listObj);
	}
}

/*
 * int nativeCreateChildList(com.scriptographer.adm.HierarchyListEntry entry)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_HierarchyListBox_nativeCreateChildList(JNIEnv *env, jobject obj, jobject entryObj) {
	try {
		ADMListEntryRef entry = gEngine->getHierarchyListEntryHandle(env, entryObj); 
		ADMHierarchyListRef list = sADMListEntry->CreateChildList(entry);
		// link it with the java object that calls this
		sADMHierarchyList->SetUserData(list, env->NewGlobalRef(obj));
		HierarchyListBox_onInit(list);
		return (jint) list;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * com.scriptographer.adm.HierarchyListEntry nativeRemoveList(int arg1)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_HierarchyListBox_nativeRemoveList(JNIEnv *env, jobject obj, jint arg1) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		ADMListEntryRef entry = sADMHierarchyList->GetParentEntry(list);
		if (entry != NULL) {
			sADMListEntry->DeleteChildList(entry);
			return gEngine->getListEntryObject(entry);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setEntrySize(int width, int height, boolean recursive)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_HierarchyListBox_setEntrySize(JNIEnv *env, jobject obj, jint width, jint height, jboolean recursive) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		if (recursive) {
			sADMHierarchyList->SetEntryWidthRecursive(list, width);
			sADMHierarchyList->SetEntryHeightRecursive(list, height);
		} else {
			sADMHierarchyList->SetEntryWidth(list, width);
			sADMHierarchyList->SetEntryHeight(list, height);
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * void setEntryTextRect(int x, int y, int width, int height, boolean recursive)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_HierarchyListBox_setEntryTextRect(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height, jboolean recursive) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		ADMRect rt;
		rt.left = x;
		rt.top = y;
		rt.right = x + width;
		rt.bottom = y + height;

		if (recursive)
			sADMHierarchyList->SetEntryTextRectRecursive(list, &rt);
		else
			sADMHierarchyList->SetEntryTextRect(list, &rt);
	} EXCEPTION_CONVERT(env);
}

/*
 * int getNonLeafEntryWidth()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_HierarchyListBox_getNonLeafEntryWidth(JNIEnv *env, jobject obj) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		return sADMHierarchyList->GetNonLeafEntryWidth(list);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void setNonLeafEntryTextRect(int x, int y, int width, int height, boolean recursive)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_HierarchyListBox_setNonLeafEntryTextRect(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height, jboolean recursive) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		ADMRect rt;
		rt.left = x;
		rt.top = y;
		rt.right = x + width;
		rt.bottom = y + height;

		if (recursive)
			sADMHierarchyList->SetNonLeafEntryTextRectRecursive(list, &rt);
		else 
			sADMHierarchyList->SetNonLeafEntryTextRect(list, &rt);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.Rectangle getNonLeafEntryTextRect()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_HierarchyListBox_getNonLeafEntryTextRect(JNIEnv *env, jobject obj) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		ADMRect rt;
		sADMHierarchyList->GetEntryTextRect(list, &rt);
		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.adm.ListEntry getLeafEntryAt(int x, int y)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_HierarchyListBox_getLeafEntryAt(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		DEFINE_ADM_POINT(pt, x, y);
		ADMListEntryRef ent = sADMHierarchyList->PickLeafEntry(list, &pt);
		return gEngine->getListEntryObject(ent);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.adm.HierarchyListEntry getSelectedLeafEntry()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_HierarchyListBox_getSelectedLeafEntry(JNIEnv *env, jobject obj) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		ADMListEntryRef entry = sADMHierarchyList->GetActiveLeafEntry(list);
		if (entry != NULL)
			return gEngine->getListEntryObject(entry);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

#define GET_ENTRIES(GET_NUMBER, INDEX_ENTRY) \
	try { \
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj); \
		int length = sADMHierarchyList->GET_NUMBER(list); \
		jobjectArray res = env->NewObjectArray(length, gEngine->cls_adm_ListEntry, NULL); \
		if (res == NULL) EXCEPTION_CHECK(env); \
		for (int i = 0; i < length; i++) { \
			ADMListEntryRef ent = sADMHierarchyList->INDEX_ENTRY(list, i); \
			env->SetObjectArrayElement(res, i, gEngine->getListEntryObject(ent)); \
		} \
		EXCEPTION_CHECK(env); \
		return res; \
	} EXCEPTION_CONVERT(env); \
	return NULL;

/*
 * com.scriptographer.adm.HierarchyListEntry[] getSelectedEntries()
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_adm_HierarchyListBox_getSelectedEntries(JNIEnv *env, jobject obj) {
	GET_ENTRIES(NumberOfAllSelectedEntriesInHierarchy, IndexAllSelectedEntriesInHierarchy)
}

/*
 * com.scriptographer.adm.HierarchyListEntry[] getUnnestedSelectedEntries()
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_adm_HierarchyListBox_getUnnestedSelectedEntries(JNIEnv *env, jobject obj) {
	GET_ENTRIES(NumberOfUnNestedSelectedEntriesInHierarchy, IndexUnNestedSelectedEntriesInHierarchy)
}

/*
 * com.scriptographer.ai.Rectangle getLocalRect()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_HierarchyListBox_getLocalRect(JNIEnv *env, jobject obj) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		ADMRect rt;
		sADMHierarchyList->GetLocalRect(list, &rt);
		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}


/*
 * com.scriptographer.adm.Point localToScreen(int x, int y)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_HierarchyListBox_localToScreen(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		DEFINE_ADM_POINT(pt, x, y);
		sADMHierarchyList->LocalToScreenPoint(list, &pt);
		return gEngine->convertPoint(env, &pt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.adm.Point screenToLocal(int x, int y)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_HierarchyListBox_screenToLocal(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		DEFINE_ADM_POINT(pt, x, y);
		sADMHierarchyList->ScreenToLocalPoint(list, &pt);
		return gEngine->convertPoint(env, &pt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.adm.Point localToGlobal(int x, int y)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_HierarchyListBox_localToGlobal__II(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		DEFINE_ADM_POINT(pt, x, y);
		sADMHierarchyList->LocalToGlobalPoint(list, &pt);
		return gEngine->convertPoint(env, &pt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.adm.Point globalToLocal(int arg1, int arg2)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_HierarchyListBox_globalToLocal__II(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		DEFINE_ADM_POINT(pt, x, y);
		sADMHierarchyList->GlobalToLocalPoint(list, &pt);
		return gEngine->convertPoint(env, &pt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * java.awt.Rectangle localToGlobal(int x, int y, int width, int height)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_HierarchyListBox_localToGlobal__IIII(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		DEFINE_ADM_RECT(rt, x, y, width, height);
		sADMHierarchyList->LocalToGlobalRect(list, &rt);
		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * java.awt.Rectangle globalToLocal(int x, int y, int width, int height)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_HierarchyListBox_globalToLocal__IIII(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		DEFINE_ADM_RECT(rt, x, y, width, height);
		sADMHierarchyList->GlobalToLocalRect(list, &rt);
		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setIndentationWidth(int width, boolean recursive)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_HierarchyListBox_setIndentationWidth(JNIEnv *env, jobject obj, jint width, jboolean recursive) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		if (recursive)
			sADMHierarchyList->SetIndentationWidthRecursive(list, width);
		else 
			sADMHierarchyList->SetIndentationWidth(list, width);
	} EXCEPTION_CONVERT(env);
}

/*
 * int getIndentationWidth()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_HierarchyListBox_getIndentationWidth(JNIEnv *env, jobject obj) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		return sADMHierarchyList->GetIndentationWidth(list);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void setLocalLeftMargin(int width)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_HierarchyListBox_setLocalLeftMargin(JNIEnv *env, jobject obj, jint width) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		sADMHierarchyList->SetLocalLeftMargin(list, width);
	} EXCEPTION_CONVERT(env);
}

/*
 * int getLocalLeftMargin()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_HierarchyListBox_getLocalLeftMargin(JNIEnv *env, jobject obj) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		return sADMHierarchyList->GetLocalLeftMargin(list);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int getGlobalLeftMargin()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_HierarchyListBox_getGlobalLeftMargin(JNIEnv *env, jobject obj) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		return sADMHierarchyList->GetGlobalLeftMargin(list);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void setDivided(boolean divided, boolean recursive)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_HierarchyListBox_setDivided(JNIEnv *env, jobject obj, jboolean divided, jboolean recursive) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		if (recursive)
			sADMHierarchyList->SetDividedRecursive(list, divided);
		else 
			sADMHierarchyList->SetDivided(list, divided);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isDivided()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_HierarchyListBox_isDivided(JNIEnv *env, jobject obj) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		return sADMHierarchyList->GetDivided(list);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setFlags(int flags, boolean recursive)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_HierarchyListBox_setFlags(JNIEnv *env, jobject obj, jint flags, jboolean recursive) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		if (recursive)
			sADMHierarchyList->SetFlagsRecursive(list, flags);
		else 
			sADMHierarchyList->SetFlags(list, flags);
	} EXCEPTION_CONVERT(env);
}

/*
 * int getFlags()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_HierarchyListBox_getFlags(JNIEnv *env, jobject obj) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		return sADMHierarchyList->GetFlags(list);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void invalidate()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_HierarchyListBox_invalidate(JNIEnv *env, jobject obj) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		sADMHierarchyList->Invalidate(list);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.adm.HierarchyListEntry[] getLeafEntries()
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_adm_HierarchyListBox_getLeafEntries(JNIEnv *env, jobject obj) {
	GET_ENTRIES(NumberOfLeafEntries, IndexLeafEntry)
}

/*
 * int getLeafEntryIndex(com.scriptographer.adm.HierarchyListEntry entry)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_HierarchyListBox_getLeafEntryIndex(JNIEnv *env, jobject obj, jobject entry) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		ADMListEntryRef ent = gEngine->getHierarchyListEntryHandle(env, entry);
		return sADMHierarchyList->GetLeafIndex(list, ent);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void swap(int fromIndex, int toIndex)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_HierarchyListBox_swap(JNIEnv *env, jobject obj, jint fromIndex, jint toIndex) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		sADMHierarchyList->SwapEntries(list, fromIndex, toIndex);
	} EXCEPTION_CONVERT(env);
}

/*
 * void deselectAll()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_HierarchyListBox_deselectAll(JNIEnv *env, jobject obj) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		sADMHierarchyList->DeselectAll(list);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.adm.HierarchyListEntry[] getExpandedEntries()
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_adm_HierarchyListBox_getExpandedEntries(JNIEnv *env, jobject obj) {
	GET_ENTRIES(NumberOfExpandedEntries, IndexExpandedEntry)
}

/*
 * int getExpandedEntryIndex(com.scriptographer.adm.HierarchyListEntry entry)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_HierarchyListBox_getExpandedEntryIndex(JNIEnv *env, jobject obj, jobject entry) {
	try {
		ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
		ADMListEntryRef ent = gEngine->getHierarchyListEntryHandle(env, entry);
		return sADMHierarchyList->GetExpandedIndex(list, ent);
	} EXCEPTION_CONVERT(env);
	return 0;
}
