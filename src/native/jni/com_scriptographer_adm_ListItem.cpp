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

#include "stdHeaders.h"
#include "ScriptographerPlugin.h"
#include "ScriptographerEngine.h"
#include "uiGlobals.h"
#include "com_scriptographer_adm_ListItem.h"

/*
 * com.scriptographer.adm.ListItem
 */

// lists don't have init callbacks that automatically get called, but just for simetry let's use the same scheme:
ASErr ASAPI ListItem_onInit(ADMListRef list) {
	DEFINE_CALLBACK_PROC(ListEntry_onDestroy);
	sADMList->SetDestroyProc(list, (ADMEntryDestroyProc) CALLBACK_PROC(ListEntry_onDestroy));
	DEFINE_CALLBACK_PROC(ListEntry_onNotify);
	sADMList->SetNotifyProc(list, (ADMEntryNotifyProc) CALLBACK_PROC(ListEntry_onNotify));
	/* these are activated in enable****Callback
	sADMList->SetTrackProc(list, ListEntry_onTrack);
	sADMList->SetDrawProc(list, ListEntry_onDraw);
	*/
	return kNoErr;
}

void ASAPI ListItem_onDestroy(ADMListRef list) {
	if (gEngine != NULL) {
		jobject listObj = gEngine->getListObject(list);
		JNIEnv *env = gEngine->getEnv();
		sADMList->SetUserData(list, NULL);
		// clear the handle
		gEngine->setIntField(env, listObj, gEngine->fid_adm_ListItem_listHandle, 0);
		// and remove global ref
		env->DeleteGlobalRef(listObj);
	}
}

#define DEFINE_METHOD(METHOD) \
		if (env->IsInstanceOf(obj, gEngine->cls_adm_HierarchyListBox)) { \
			ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj); \
			METHOD(sADMHierarchyList, sADMListEntry, ADMListEntryRef) \
		} else { \
			ADMListRef list = gEngine->getListBoxHandle(env, obj); \
			METHOD(sADMList, sADMEntry, ADMEntryRef) \
		}

/*
 * int nativeInit(int itemHandle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_ListItem_nativeInit(JNIEnv *env, jobject obj, jint itemHandle) {
	try {
		// the init callbacks need to be called by hand, as 
		// there are no automatic ones for these
		ADMListRef list = sADMItem->GetList((ADMItemRef) itemHandle);
		if (list != NULL) {
			// link it with the java object that calls this
			sADMList->SetUserData(list, env->NewGlobalRef(obj));
			ListItem_onInit(list);
			return (jint) list;
		} else {
			ADMHierarchyListRef hierarchyList = sADMItem->GetHierarchyList((ADMItemRef) itemHandle);
			if (hierarchyList != NULL) {
				// link it with the java object that calls this
				sADMHierarchyList->SetUserData(hierarchyList, env->NewGlobalRef(obj));
				HierarchyListBox_onInit(hierarchyList);
				return (jint) hierarchyList;
			}
		}
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void nativeSetTrackEntryCallback(boolean enabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListItem_nativeSetTrackEntryCallback(JNIEnv *env, jobject obj, jboolean enabled) {
	try {
		if (env->IsInstanceOf(obj, gEngine->cls_adm_HierarchyListBox)) {
			ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
			DEFINE_CALLBACK_PROC(HierarchyListEntry_onTrack);
			sADMHierarchyList->SetTrackProc(list, enabled ? (ADMListEntryTrackProc) CALLBACK_PROC(HierarchyListEntry_onTrack) : NULL);
		} else {
			ADMListRef list = gEngine->getListBoxHandle(env, obj);
			DEFINE_CALLBACK_PROC(ListEntry_onTrack);
			sADMList->SetTrackProc(list, enabled ? (ADMEntryTrackProc) CALLBACK_PROC(ListEntry_onTrack) : NULL);
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetDrawEntryCallback(boolean enabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListItem_nativeSetDrawEntryCallback(JNIEnv *env, jobject obj, jboolean enabled) {
	try {
		if (env->IsInstanceOf(obj, gEngine->cls_adm_HierarchyListBox)) {
			ADMHierarchyListRef list = gEngine->getHierarchyListBoxHandle(env, obj);
			DEFINE_CALLBACK_PROC(HierarchyListEntry_onDraw);
			sADMHierarchyList->SetDrawProc(list, enabled ? (ADMListEntryDrawProc) CALLBACK_PROC(HierarchyListEntry_onDraw) : NULL);
		} else {
			ADMListRef list = gEngine->getListBoxHandle(env, obj);
			DEFINE_CALLBACK_PROC(ListEntry_onDraw);
			sADMList->SetDrawProc(list, enabled ? (ADMEntryDrawProc) CALLBACK_PROC(ListEntry_onDraw) : NULL);
		}
	} EXCEPTION_CONVERT(env);
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
	} EXCEPTION_CONVERT(env);
}

/*
 * void setTrackEntryMask(int mask)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListItem_setTrackEntryMask(JNIEnv *env, jobject obj, jint mask) {
	try {
		#define SET_TRACK_MASK(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			LIST_SUITE->SetMask(list, mask);

		DEFINE_METHOD(SET_TRACK_MASK)
	} EXCEPTION_CONVERT(env);
}

/*
 * int getTrackEntryMask()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_ListItem_getTrackEntryMask(JNIEnv *env, jobject obj) {
	try {
		#define GET_TRACK_MASK(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			return (jint)LIST_SUITE->GetMask(list);

		DEFINE_METHOD(GET_TRACK_MASK)
	} EXCEPTION_CONVERT(env);
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
	} EXCEPTION_CONVERT(env);
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
	} EXCEPTION_CONVERT(env);
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
	} EXCEPTION_CONVERT(env);
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
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * java.lang.Object remove(int index)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_ListItem_remove(JNIEnv *env, jobject obj, jint index) {
	try {
		// before removing, a local reference needs to be created, as the global reference is destroyed in ListItem_onDestroy 

		#define REMOVE_ENTRY(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			ENTRY_TYPE ent = LIST_SUITE->IndexEntry(list, index); \
			if (ent != NULL) { \
				jobject entry = gEngine->getListEntryObject(ent); \
				if (entry != NULL) entry = env->NewLocalRef(entry); \
				LIST_SUITE->RemoveEntry(list, index); \
				return entry; \
			}

		DEFINE_METHOD(REMOVE_ENTRY)
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * java.lang.Object get(int index)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_ListItem_get__I(JNIEnv *env, jobject obj, jint index) {
	try {
		#define GET_ENTRY(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			ENTRY_TYPE ent = LIST_SUITE->IndexEntry(list, index); \
			return gEngine->getListEntryObject(ent);

		DEFINE_METHOD(GET_ENTRY)
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.adm.ListEntry get(java.lang.String text)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_ListItem_get__Ljava_lang_String_2(JNIEnv *env, jobject obj, jstring text) {
	ASUnicode *chars = NULL;
	try {
		chars = gEngine->convertString_ASUnicode(env, text);
		if (chars == NULL) EXCEPTION_CHECK(env);

		#define FIND_ENTRY(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			ENTRY_TYPE ent = LIST_SUITE->FindEntryW(list, chars); \
			return gEngine->getListEntryObject(ent);

		DEFINE_METHOD(FIND_ENTRY)
	} EXCEPTION_CONVERT(env);
	if (chars != NULL)
		delete chars;
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
			return gEngine->getListEntryObject(ent);

		DEFINE_METHOD(PICK_ENTRY)

	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.adm.ListEntry getSelectedEntry()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_ListItem_getSelectedEntry(JNIEnv *env, jobject obj) {
	try {
		#define GET_ACTIVE_ENTRY(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			ENTRY_TYPE ent = LIST_SUITE->GetActiveEntry(list); \
			return gEngine->getListEntryObject(ent);

		DEFINE_METHOD(GET_ACTIVE_ENTRY)
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.adm.ListEntry[] getSelectedEntries()
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_adm_ListItem_getSelectedEntries(JNIEnv *env, jobject obj) {
	try {
		#define GET_SELECTED_ENTRIES(LIST_SUITE, ENTRY_SUITE, ENTRY_TYPE) \
			int length = LIST_SUITE->NumberOfSelectedEntries(list); \
			jobjectArray res = env->NewObjectArray(length, gEngine->cls_adm_ListEntry, NULL); \
			if (res == NULL) EXCEPTION_CHECK(env); \
			for (int i = 0; i < length; i++) { \
				ENTRY_TYPE ent = LIST_SUITE->IndexSelectedEntry(list, i); \
				env->SetObjectArrayElement(res, i, gEngine->getListEntryObject(ent)); \
			} \
			EXCEPTION_CHECK(env); \
			return res;

		DEFINE_METHOD(GET_SELECTED_ENTRIES)
	} EXCEPTION_CONVERT(env);
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
	} EXCEPTION_CONVERT(env);
}

/*
 * void selectByText(java.lang.String text)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListItem_selectByText(JNIEnv *env, jobject obj, jstring text) {
	try {
		if (env->IsInstanceOf(obj, gEngine->cls_adm_HierarchyListBox)) {
			throw new StringException("selectByText is not supported in hierarchy lists.");
		} else {
			ADMListRef list = gEngine->getListBoxHandle(env, obj);
			ASUnicode *chars = gEngine->convertString_ASUnicode(env, text);
			sADMList->SelectByTextW(list, chars);
			delete chars;
		}
	} EXCEPTION_CONVERT(env);
}
