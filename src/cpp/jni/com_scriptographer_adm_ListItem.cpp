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
 * $RCSfile: com_scriptographer_adm_ListItem.cpp,v $
 * $Author: lehni $
 * $Revision: 1.4 $
 * $Date: 2005/10/23 00:28:48 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "admGlobals.h"
#include "com_scriptographer_adm_ListItem.h"

/*
 * com.scriptographer.adm.ListItem
 */
 
// lists don't have init callbacks that automatically get called, but just for simetry let's use the same scheme:
ASErr ASAPI callbackListInit(ADMListRef list) {
	sADMList->SetDestroyProc(list, callbackListEntryDestroy);
	sADMList->SetNotifyProc(list, callbackListEntryNotify);
	/* these are activated in enable****Callback
	sADMList->SetTrackProc(list, callbackListEntryTrack);
	sADMList->SetDrawProc(list, callbackListEntryDraw);
	*/
	return kNoErr;
}

void ASAPI callbackListDestroy(ADMListRef list) {
	if (gEngine != NULL) {
		jobject listObj = gEngine->getListObject(list);
		JNIEnv *env = gEngine->getEnv();
		sADMList->SetUserData(list, NULL);
		// clear the handle
		gEngine->setIntField(env, listObj, gEngine->fid_ListItem_listHandle, 0);
		// and remove global ref
		env->DeleteGlobalRef(listObj);
	}
}

#define DEFINE_METHOD(METHOD) \
		if (env->IsInstanceOf(obj, gEngine->cls_HierarchyList)) { \
			ADMHierarchyListRef list = gEngine->getHierarchyListRef(env, obj); \
			METHOD(sADMHierarchyList, sADMListEntry, ADMListEntryRef) \
		} else { \
			ADMListRef list = gEngine->getListRef(env, obj); \
			METHOD(sADMList, sADMEntry, ADMEntryRef) \
		}

/*
 * int nativeInit(int itemRef)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_ListItem_nativeInit(JNIEnv *env, jobject obj, jint itemRef) {
	try {
		// the init callbacks need to be called by hand, as 
		// there are no automatic ones for these
		ADMListRef list = sADMItem->GetList((ADMItemRef) itemRef);
		if (list != NULL) {
			// link it with the java object that calls this
			sADMList->SetUserData(list, env->NewGlobalRef(obj));
			callbackListInit(list);
			return (jint) list;
		} else {
			ADMHierarchyListRef hierarchyList = sADMItem->GetHierarchyList((ADMItemRef) itemRef);
			if (hierarchyList != NULL) {
				// link it with the java object that calls this
				sADMHierarchyList->SetUserData(hierarchyList, env->NewGlobalRef(obj));
				callbackHierarchyListInit(hierarchyList);
				return (jint) hierarchyList;
			}
		}
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void nativeSetTrackEntryCallback(boolean enabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListItem_nativeSetTrackEntryCallback(JNIEnv *env, jobject obj, jboolean enabled) {
	try {
		if (env->IsInstanceOf(obj, gEngine->cls_HierarchyList)) {
			ADMHierarchyListRef list = gEngine->getHierarchyListRef(env, obj);
			sADMHierarchyList->SetTrackProc(list, enabled ? callbackHierarchyListEntryTrack : NULL);
		} else {
			ADMListRef list = gEngine->getListRef(env, obj);
			sADMList->SetTrackProc(list, enabled ? callbackListEntryTrack : NULL);
		}
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeSetDrawEntryCallback(boolean enabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListItem_nativeSetDrawEntryCallback(JNIEnv *env, jobject obj, jboolean enabled) {
	try {
		if (env->IsInstanceOf(obj, gEngine->cls_HierarchyList)) {
			ADMHierarchyListRef list = gEngine->getHierarchyListRef(env, obj);
			sADMHierarchyList->SetDrawProc(list, enabled ? callbackHierarchyListEntryDraw : NULL);
		} else {
			ADMListRef list = gEngine->getListRef(env, obj);
			sADMList->SetDrawProc(list, enabled ? callbackListEntryDraw : NULL);
		}
	} EXCEPTION_CONVERT(env)
}

/*
 * void setEntrySize(int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListItem_setEntrySize(JNIEnv *env, jobject obj, jint width, jint height) {
	try {
		#define SET_ENTRY_SIZE(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			LIST_SUITE->SetEntryWidth(list, width); \
			LIST_SUITE->SetEntryHeight(list, height);

		DEFINE_METHOD(SET_ENTRY_SIZE)
	} EXCEPTION_CONVERT(env)
}

/*
 * void setTrackMask(int mask)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListItem_setTrackMask(JNIEnv *env, jobject obj, jint mask) {
	try {
		#define SET_TRACK_MASK(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			LIST_SUITE->SetMask(list, mask);

		DEFINE_METHOD(SET_TRACK_MASK)
	} EXCEPTION_CONVERT(env)
}

/*
 * int getTrackMask()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_ListItem_getTrackMask(JNIEnv *env, jobject obj) {
	try {
		#define GET_TRACK_MASK(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			return (jint)LIST_SUITE->GetMask(list);

		DEFINE_METHOD(GET_TRACK_MASK)
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * com.scriptographer.ai.Point getEntrySize()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_ListItem_getEntrySize(JNIEnv *env, jobject obj) {
	try {
		ADMPoint pt;
		
		#define GET_ENTRY_SIZE(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			pt.h = LIST_SUITE->GetEntryWidth(list); \
			pt.v = LIST_SUITE->GetEntryHeight(list);

		DEFINE_METHOD(GET_ENTRY_SIZE)
		
		return gEngine->convertPoint(env, &pt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void setEntryTextRect(int x, int y, int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListItem_setEntryTextRect(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
		ADMRect rt;
		rt.left = x;
		rt.top = y;
		rt.right = x + width;
		rt.bottom = y + height;
		
		#define SET_ENTRY_TEXT_RECT(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			LIST_SUITE->SetEntryTextRect(list, &rt);

		DEFINE_METHOD(SET_ENTRY_TEXT_RECT)
	} EXCEPTION_CONVERT(env)
}

/*
 * com.scriptographer.ai.Rectangle getEntryTextRect()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_ListItem_getEntryTextRect(JNIEnv *env, jobject obj) {
	try {
		ADMRect rt;
		
		#define GET_ENTRY_TEXT_RECT(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			LIST_SUITE->GetEntryTextRect(list, &rt);

		DEFINE_METHOD(GET_ENTRY_TEXT_RECT)

		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * int size()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_ListItem_size(JNIEnv *env, jobject obj) {
	try {
		#define SIZE(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			return LIST_SUITE->NumberOfEntries(list);

		DEFINE_METHOD(SIZE)
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * java.lang.Object remove(int index)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_ListItem_remove(JNIEnv *env, jobject obj, jint index) {
	try {
		// before removing, a local reference needs to be created, as the global reference is destroyed in callbackListDestroy 
	
		#define REMOVE_ENTRY(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			ENTRY_TYPE ent = LIST_SUITE->GetEntry(list, index); \
			if (ent != NULL) { \
				jobject entry = gEngine->getListEntryObject(ent); \
				if (entry != NULL) entry = env->NewLocalRef(entry); \
				LIST_SUITE->RemoveEntry(list, index); \
				return entry; \
			}

		DEFINE_METHOD(REMOVE_ENTRY)
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * java.lang.Object get(int index)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_ListItem_get__I(JNIEnv *env, jobject obj, jint index) {
	try {
		#define GET_ENTRY(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			ENTRY_TYPE ent = LIST_SUITE->GetEntry(list, index); \
			return ent != NULL ? gEngine->getListEntryObject(ent) : NULL;

		DEFINE_METHOD(GET_ENTRY)
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.adm.ListEntry get(java.lang.String text)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_ListItem_get__Ljava_lang_String_2(JNIEnv *env, jobject obj, jstring text) {
	const jchar *chars = NULL;
	try {
		chars = env->GetStringChars(text, NULL);
		if (chars == NULL) EXCEPTION_CHECK(env)

		#define FIND_ENTRY(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			ENTRY_TYPE ent = LIST_SUITE->FindEntryW(list, chars); \
			return ent != NULL ? gEngine->getListEntryObject(ent) : NULL;

		DEFINE_METHOD(FIND_ENTRY)
	} EXCEPTION_CONVERT(env)
	if (chars != NULL)
		env->ReleaseStringChars(text, chars);
	return NULL;
}

/*
 * com.scriptographer.adm.ListEntry getAt(int x, int y)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_ListItem_getAt(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
		ADMPoint pt;
		pt.h = x;
		pt.v = y;

		#define PICK_ENTRY(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			ENTRY_TYPE ent = LIST_SUITE->PickEntry(list, &pt); \
			return ent != NULL ? gEngine->getListEntryObject(ent) : NULL;

		DEFINE_METHOD(PICK_ENTRY)

	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.adm.ListEntry getActive()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_ListItem_getActive(JNIEnv *env, jobject obj) {
	try {
		#define GET_ACTIVE_ENTRY(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			ENTRY_TYPE ent = LIST_SUITE->GetActiveEntry(list); \
			return ent != NULL ? gEngine->getListEntryObject(ent) : NULL;

		DEFINE_METHOD(GET_ACTIVE_ENTRY)
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.adm.ListEntry[] getSelected()
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_adm_ListItem_getSelected(JNIEnv *env, jobject obj) {
	try {
		#define GET_SELECTED_ENTRIES(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			int length = LIST_SUITE->NumberOfSelectedEntries(list); \
			jobjectArray res = env->NewObjectArray(length, gEngine->cls_ListEntry, NULL); \
			if (res == NULL) EXCEPTION_CHECK(env) \
			for (int i = 0; i < length; i++) { \
				ENTRY_TYPE ent = LIST_SUITE->IndexSelectedEntry(list, i); \
				env->SetObjectArrayElement(res, i, ent != NULL ? gEngine->getListEntryObject(ent) : NULL); \
			} \
			EXCEPTION_CHECK(env) \
			return res;

		DEFINE_METHOD(GET_SELECTED_ENTRIES)
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void nativeSetBackgroundColor(int color)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListItem_nativeSetBackgroundColor(JNIEnv *env, jobject obj, jint color) {
	try {
		#define SET_BG_COLOR(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			LIST_SUITE->SetBackgroundColor(list, (ADMColor) color);

		DEFINE_METHOD(SET_BG_COLOR)
	} EXCEPTION_CONVERT(env)
}

/*
 * void selectByText(java.lang.String text)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListItem_selectByText(JNIEnv *env, jobject obj, jstring text) {
	try {
		if (env->IsInstanceOf(obj, gEngine->cls_HierarchyList)) {
			throw new StringException("selectByText is not supported in hierarchy lists.");
		} else {
			ADMListRef list = gEngine->getListRef(env, obj);
			const jchar *chars = env->GetStringChars(text, NULL);
			if (chars == NULL) EXCEPTION_CHECK(env)
			sADMList->SelectByTextW(list, chars);
			env->ReleaseStringChars(text, chars);
		}
	} EXCEPTION_CONVERT(env)
}
