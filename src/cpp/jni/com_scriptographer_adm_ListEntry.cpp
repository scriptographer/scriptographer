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
 * $RCSfile: com_scriptographer_adm_ListEntry.cpp,v $
 * $Author: lehni $
 * $Revision: 1.6 $
 * $Date: 2005/11/04 01:34:14 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "admGlobals.h"
#include "com_scriptographer_adm_ListEntry.h"

/*
 * com.scriptographer.adm.Entry
 */

void ASAPI ListEntry_onDestroy(ADMEntryRef entry) {
	// this seems to be necessary otherwise crashes occur:
	sADMEntry->SetPicture(entry, NULL);
	sADMEntry->SetDisabledPicture(entry, NULL);
	sADMEntry->SetSelectedPicture(entry, NULL);

	if (gEngine != NULL) {
		JNIEnv *env = gEngine->getEnv();
		try {
			jobject obj = gEngine->getListEntryObject(entry);
			// call onDestory on the entry object
			gEngine->callOnDestroy(obj);
			// clear the handle
			gEngine->setIntField(env, obj, gEngine->fid_ADMObject_handle, 0);
			env->DeleteGlobalRef(obj);
		} EXCEPTION_CATCH_REPORT(env)
	}
}

void ASAPI ListEntry_onNotify(ADMEntryRef entry, ADMNotifierRef notifier) {
	sADMEntry->DefaultNotify(entry, notifier);
	ADMListRef list = sADMEntry->GetList(entry);
	jobject entryObj = gEngine->getListEntryObject(entry);
	gEngine->callOnNotify(entryObj, notifier);
}

ASBoolean ASAPI ListEntry_onTrack(ADMEntryRef entry, ADMTrackerRef tracker) {
	ADMListRef list = sADMEntry->GetList(entry);
	jobject entryObj = gEngine->getListEntryObject(entry);
	ASBoolean ret = gEngine->callOnTrack(entryObj, tracker);
	if (ret)
		ret = sADMEntry->DefaultTrack(entry, tracker);
	return ret;
}

void ASAPI ListEntry_onDraw(ADMEntryRef entry, ADMDrawerRef drawer) {
	sADMEntry->DefaultDraw(entry, drawer);
	ADMListRef list = sADMEntry->GetList(entry);
	jobject entryObj = gEngine->getListEntryObject(entry);
	gEngine->callOnDraw(entryObj, drawer);
}

#define DEFINE_METHOD(METHOD) \
		if (env->IsInstanceOf(obj, gEngine->cls_HierarchyListEntry)) { \
			ADMListEntryRef entry = gEngine->getHierarchyListEntryRef(env, obj); \
			METHOD(sADMListEntry) \
		} else { \
			ADMEntryRef entry = gEngine->getListEntryRef(env, obj); \
			METHOD(sADMEntry) \
		}

/*
 * int nativeCreate(com.scriptographer.adm.List list, int index, int id)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_ListEntry_nativeCreate(JNIEnv *env, jobject obj, jobject list, jint index, jint id) {
	try {
		// if index is bellow 0, insert at the end:
		if (env->IsInstanceOf(obj, gEngine->cls_HierarchyListEntry)) {
			ADMHierarchyListRef lst = gEngine->getHierarchyListRef(env, list);
			ADMListEntryRef entry = index < 0 ? sADMListEntry->Create(lst) :
				sADMHierarchyList->InsertEntry(lst, index);
			if (entry != NULL) {
				sADMListEntry->SetID(entry, id);
				sADMListEntry->SetUserData(entry, env->NewGlobalRef(obj));
			}
			return (jint) entry;
		} else {
			ADMListRef lst = gEngine->getListRef(env, list);
			ADMEntryRef entry = index < 0 ? sADMEntry->Create(lst) : 
				sADMList->InsertEntry(lst, index);
			if (entry != NULL) {
				sADMEntry->SetID(entry, id);
				sADMEntry->SetUserData(entry, env->NewGlobalRef(obj));
			}
			return (jint) entry;
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void nativeDestroy()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListEntry_nativeDestroy(JNIEnv *env, jobject obj) {
	try {
		#define DESTROY(SUITE) \
			SUITE->Destroy(entry);

		DEFINE_METHOD(DESTROY)
	} EXCEPTION_CONVERT(env)
}

/*
 * int getIndex()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_ListEntry_getIndex(JNIEnv *env, jobject obj) {
	try {
		#define GET_INDEX(SUITE) \
			return SUITE->GetIndex(entry);

		DEFINE_METHOD(GET_INDEX)
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void setSelected(boolean selected)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListEntry_setSelected(JNIEnv *env, jobject obj, jboolean selected) {
	try {
		#define SET_SELECTED(SUITE) \
			SUITE->Select(entry, selected);

		DEFINE_METHOD(SET_SELECTED)
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean isSelected()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_ListEntry_isSelected(JNIEnv *env, jobject obj) {
	try {
		#define IS_SELECTED(SUITE) \
			return SUITE->IsSelected(entry);

		DEFINE_METHOD(IS_SELECTED)
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void makeInBounds()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListEntry_makeInBounds(JNIEnv *env, jobject obj) {
	try {
		#define MAKE_IN_BOUNDS(SUITE) \
			SUITE->MakeInBounds(entry);

		DEFINE_METHOD(MAKE_IN_BOUNDS)
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean isInBounds()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_ListEntry_isInBounds(JNIEnv *env, jobject obj) {
	try {
		#define IS_IN_BOUNDS(SUITE) \
			return SUITE->IsInBounds(entry);

		DEFINE_METHOD(IS_IN_BOUNDS)
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setEnabled(boolean enabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListEntry_setEnabled(JNIEnv *env, jobject obj, jboolean enabled) {
	try {
		#define SET_ENABLED(SUITE) \
			SUITE->Enable(entry, enabled);

		DEFINE_METHOD(SET_ENABLED)
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean isEnabled()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_ListEntry_isEnabled(JNIEnv *env, jobject obj) {
	try {
		#define IS_ENABLED(SUITE) \
			return SUITE->IsEnabled(entry);

		DEFINE_METHOD(IS_ENABLED)
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setActive(boolean active)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListEntry_setActive(JNIEnv *env, jobject obj, jboolean active) {
	try {
		#define SET_ACTIVE(SUITE) \
			SUITE->Activate(entry, active);

		DEFINE_METHOD(SET_ACTIVE)
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean isActive()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_ListEntry_isActive(JNIEnv *env, jobject obj) {
	try {
		#define IS_ACTIVE(SUITE) \
			return SUITE->IsActive(entry);

		DEFINE_METHOD(IS_ACTIVE)
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setChecked(boolean checked)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListEntry_setChecked(JNIEnv *env, jobject obj, jboolean checked) {
	try {
		#define SET_CHECKED(SUITE) \
			SUITE->Check(entry, checked);

		DEFINE_METHOD(SET_CHECKED)
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean isChecked()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_ListEntry_isChecked(JNIEnv *env, jobject obj) {
	try {
		#define IS_CHECKED(SUITE) \
			return SUITE->IsChecked(entry);

		DEFINE_METHOD(IS_CHECKED)
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setSeparator(boolean seperator)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListEntry_setSeparator(JNIEnv *env, jobject obj, jboolean seperator) {
	try {
		#define SET_SEPARATOR(SUITE) \
			SUITE->MakeSeparator(entry, seperator);

		DEFINE_METHOD(SET_SEPARATOR)
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean isSeparator()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_ListEntry_isSeparator(JNIEnv *env, jobject obj) {
	try {
		#define IS_SEPERATOR(SUITE) \
			return SUITE->IsSeparator(entry);

		DEFINE_METHOD(IS_SEPERATOR)
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * com.scriptographer.ai.Rectangle getLocalRect()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_ListEntry_getLocalRect(JNIEnv *env, jobject obj) {
	try {
		ADMRect rt;
		
		#define GET_LOCAL_RECT(SUITE) \
			SUITE->GetLocalRect(entry, &rt);

		DEFINE_METHOD(GET_LOCAL_RECT)

		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.ai.Rectangle getBounds()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_ListEntry_getBounds(JNIEnv *env, jobject obj) {
	try {
		ADMRect rt;
		
		#define GET_BOUNDS_RECT(SUITE) \
			SUITE->GetBoundsRect(entry, &rt);

		DEFINE_METHOD(GET_BOUNDS_RECT)

		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}


/*
 * java.awt.Point localToScreen(int x, int y)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_ListEntry_localToScreen__II(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
		DEFINE_ADM_POINT(pt, x, y);
		
		#define LOCAL_TO_SCREEN_POINT(SUITE) \
			SUITE->LocalToScreenPoint(entry, &pt);

		DEFINE_METHOD(LOCAL_TO_SCREEN_POINT)

		return gEngine->convertPoint(env, &pt);
		
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * java.awt.Point screenToLocal(int x, int y)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_ListEntry_screenToLocal__II(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
		DEFINE_ADM_POINT(pt, x, y);
		
		#define SCREEN_TO_LOCAL_POINT(SUITE) \
			SUITE->ScreenToLocalPoint(entry, &pt);

		DEFINE_METHOD(SCREEN_TO_LOCAL_POINT)

		return gEngine->convertPoint(env, &pt);

	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * java.awt.Rectangle localToScreen(int x, int y, int width, int height)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_ListEntry_localToScreen__IIII(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
		DEFINE_ADM_RECT(rt, x, y, width, height);
		
		#define LOCAL_TO_SCREEN_RECT(SUITE) \
			SUITE->LocalToScreenRect(entry, &rt);

		DEFINE_METHOD(LOCAL_TO_SCREEN_RECT)

		return gEngine->convertRectangle(env, &rt);

	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * java.awt.Rectangle screenToLocal(int x, int y, int width, int height)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_ListEntry_screenToLocal__IIII(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
		DEFINE_ADM_RECT(rt, x, y, width, height);
		
		#define SCREEN_TO_LOCAL_RECT(SUITE) \
			SUITE->ScreenToLocalRect(entry, &rt);

		DEFINE_METHOD(SCREEN_TO_LOCAL_RECT)

		return gEngine->convertRectangle(env, &rt);

	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void invalidate()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListEntry_invalidate__(JNIEnv *env, jobject obj) {
	try {
		#define INVALIDATE(SUITE) \
			SUITE->Invalidate(entry);

		DEFINE_METHOD(INVALIDATE)
	} EXCEPTION_CONVERT(env)
}

/*
 * void invalidate(int x, int y, int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListEntry_invalidate__IIII(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
		if (env->IsInstanceOf(obj, gEngine->cls_HierarchyListEntry)) {
			throw new StringException("invalidate(Rectangle) is not supported in hierarchy list entries.");
		} else {
			ADMEntryRef entry = gEngine->getListEntryRef(env, obj);
			DEFINE_ADM_RECT(rt, x, y, width, height);
			sADMEntry->InvalidateRect(entry, &rt);
		}
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeSetImage(int iconRef)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListEntry_nativeSetImage(JNIEnv *env, jobject obj, jint iconRef) {
	try {
		#define SET_PICTURE(SUITE) \
			SUITE->SetPicture(entry, (ADMIconRef)iconRef);

		DEFINE_METHOD(SET_PICTURE)
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeSetSelectedImage(int iconRef)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListEntry_nativeSetSelectedImage(JNIEnv *env, jobject obj, jint iconRef) {
	try {
		#define SET_SELECETED_PICTURE(SUITE) \
			SUITE->SetSelectedPicture(entry, (ADMIconRef)iconRef);

		DEFINE_METHOD(SET_SELECETED_PICTURE)
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeSetDisabledImage(int iconRef)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListEntry_nativeSetDisabledImage(JNIEnv *env, jobject obj, jint iconRef) {
	try {
		#define SET_DISABLED_PICTURE(SUITE) \
			SUITE->SetDisabledPicture(entry, (ADMIconRef)iconRef);

		DEFINE_METHOD(SET_DISABLED_PICTURE)
	} EXCEPTION_CONVERT(env)
}

/*
 * void setText(java.lang.String text)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListEntry_setText(JNIEnv *env, jobject obj, jstring text) {
	try {
		#define SET_TEXT(SUITE) \
			const jchar *chars = env->GetStringChars(text, NULL); \
			if (chars == NULL) EXCEPTION_CHECK(env) \
			SUITE->SetTextW(entry, chars); \
			env->ReleaseStringChars(text, chars);

		DEFINE_METHOD(SET_TEXT)
	} EXCEPTION_CONVERT(env)
}

/*
 * java.lang.String getText()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_adm_ListEntry_getText(JNIEnv *env, jobject obj) {
	try {
		#define GET_TEXT(SUITE) \
			long len = SUITE->GetTextLength(entry); \
			jchar *chars = new jchar[len]; \
			SUITE->GetTextW(entry, chars, len); \
			jstring res = env->NewString(chars, len); \
			if (res == NULL) EXCEPTION_CHECK(env) \
			delete chars; \
			return res;
			
		DEFINE_METHOD(GET_TEXT)
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void update()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_ListEntry_update(JNIEnv *env, jobject obj) {
	try {
		#define UPDATE(SUITE) \
			SUITE->Update(entry);

		DEFINE_METHOD(UPDATE)
	} EXCEPTION_CONVERT(env)
}
