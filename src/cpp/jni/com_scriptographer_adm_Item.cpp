#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "admGlobals.h"
#include "com_scriptographer_adm_Item.h"

/*
 * com.scriptographer.awt.Item
 */

/*
 * various callbacks for item:
 *
 */

ASErr ASAPI callbackItemInit(ADMItemRef item) {
	jobject obj = gEngine->getItemObject(item);
	// Attach the item-level callbacks
	sADMItem->SetDestroyProc(item, callbackItemDestroy);
	sADMItem->SetNotifyProc(item, callbackItemNotify);
	// is this a list or hierarchy list?
	// if so, call its init function, as this is not automatically done:
	ADMListRef list = sADMItem->GetList(item);
	if (list != NULL) { // List
		callbackListInit(list);
	} else {
		ADMHierarchyListRef hierarchyList = sADMItem->GetHierarchyList(item);
		if (hierarchyList != NULL) { // HierarchyList
			callbackHierarchyListInit(hierarchyList);
		}
	}
	return kNoErr;
}

void ASAPI callbackItemDestroy(ADMItemRef item) {
	JNIEnv *env = gEngine->getEnv();
	try {
		jobject obj = gEngine->getItemObject(item);
		gEngine->callVoidMethodReport(env, obj, gEngine->mid_CallbackHandler_onDestroy, NULL);

		// is this a list or hierarchy list?
		// if so, call its destroy function, as this is not automatically done:

		// unfortunatelly, calling sADMItem->GetHierarchyList in callbackItemDestroy crashes illustrator,
		// so let's access the listRefs through the existing java wrapper instead:
		
		if (env->IsInstanceOf(obj, gEngine->cls_ListBox)) {
			jobject listObj = gEngine->getObjectField(env, obj, gEngine->fid_ListBox_list);
			if (listObj != NULL) {
				if (env->IsInstanceOf(listObj, gEngine->cls_HierarchyList)) {
					ADMHierarchyListRef list = gEngine->getHierarchyListRef(env, listObj);
					callbackHierarchyListDestroy(list);
				} else {
					ADMListRef list = gEngine->getListRef(env, listObj);
					callbackListDestroy(list);
				}
			}
		}
		env->DeleteGlobalRef(obj);
	} EXCEPTION_CATCH_REPORT(env)
}

void ASAPI callbackItemNotify(ADMItemRef item, ADMNotifierRef notifier) {
	sADMItem->DefaultNotify(item, notifier);
	jobject obj = gEngine->getItemObject(item);
	if (sADMNotifier->IsNotifierType(notifier, kADMBoundsChangedNotifier)) {
		JNIEnv *env = gEngine->getEnv();
		try {
			jobject size = gEngine->getObjectField(env, obj, gEngine->fid_Item_size);
			ADMPoint pt;
			gEngine->convertDimension(env, size, &pt);
			// calculate differnce:
			ADMRect rt;
			sADMItem->GetLocalRect(item, &rt);
			int dx = rt.right - pt.h;
			int dy = rt.bottom - pt.v;
			if (dx != 0 || dy != 0) {
				// write size back:
				pt.h = rt.right;
				pt.v = rt.bottom;
				gEngine->convertDimension(env, &pt, size);
				// and call handler:
				gEngine->callVoidMethod(env, obj, gEngine->mid_CallbackHandler_onResize, dx, dy);
			}
		} EXCEPTION_CATCH_REPORT(env)
	} else {
		gEngine->callOnNotify(obj, notifier);
	}
}

ASBoolean ASAPI callbackItemTrack(ADMItemRef item, ADMTrackerRef tracker) {
	jobject obj = gEngine->getItemObject(item);
	gEngine->callOnTrack(obj, tracker);
	return sADMItem->DefaultTrack(item, tracker);
}

void ASAPI callbackItemDraw(ADMItemRef item, ADMDrawerRef drawer) {
	sADMItem->DefaultDraw(item, drawer);
	jobject obj = gEngine->getItemObject(item);
	gEngine->callOnDraw(obj, drawer);
}

/*
 * int nativeCreate(int dialogRef, java.lang.String type, java.awt.Rectangle bounds, int options)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Item_nativeCreate(JNIEnv *env, jobject obj, jobject dialog, jstring type, jobject bounds, jint options) {
	try {
		ADMDialogRef dlg = gEngine->getDialogRef(env, dialog);
		char *itemType = gEngine->createCString(env, type);
		ADMRect rt;
		gEngine->convertRectangle(env, bounds, &rt);
		ADMItemRef item = sADMItem->Create(dlg, kADMUniqueItemID, itemType, &rt, callbackItemInit, env->NewGlobalRef(obj), options);
		delete itemType;
		if (item == NULL)
			throw new StringException("Cannot create dialog item.");
		
		return (jint)item;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void destroy()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_nativeDestroy(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->Destroy(item);
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeSetTrackCallbackEnabled(boolean enabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_nativeSetTrackCallbackEnabled(JNIEnv *env, jobject obj, jboolean enabled) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetTrackProc(item, enabled ? callbackItemTrack : NULL);
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeSetDrawCallbackEnabled(boolean enabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_nativeSetDrawCallbackEnabled(JNIEnv *env, jobject obj, jboolean enabled) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetDrawProc(item, enabled ? callbackItemDraw : NULL);
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeSetStyle(int style)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_nativeSetStyle(JNIEnv *env, jobject obj, jint style) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetItemStyle(item, (ADMItemStyle)style);
	} EXCEPTION_CONVERT(env)
}

/*
 * java.awt.Dimension nativeGetSize()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Item_nativeGetSize(JNIEnv *env, jobject obj) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		ADMRect size;
		sADMItem->GetLocalRect(item, &size);
		DEFINE_ADM_POINT(pt, size.right, size.bottom);
		return gEngine->convertDimension(env, &pt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void nativeSetSize(int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_nativeSetSize(JNIEnv *env, jobject obj, jint width, jint height) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
	    DEFINE_ADM_RECT(rt, 0, 0, width, height);
		sADMItem->SetLocalRect(item, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * java.awt.Rectangle nativeGetBounds()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Item_nativeGetBounds(JNIEnv *env, jobject obj) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		ADMRect rt;
		sADMItem->GetBoundsRect(item, &rt);
		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void nativeSetBounds(int x, int y, int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_nativeSetBounds(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
	    DEFINE_ADM_RECT(rt, x, y, width, height);
		sADMItem->SetBoundsRect(item, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void setLocation(int x, int y)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_setLocation(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->Move(item, x, y);
	} EXCEPTION_CONVERT(env)
}

/*
 * java.awt.Point getLocation()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Item_getLocation(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		ADMRect rt;
		sADMItem->GetBoundsRect(item, &rt);
		DEFINE_ADM_POINT(pt, rt.left, rt.top);
		return gEngine->convertPoint(env, &pt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * java.awt.Point localToScreen(int x, int y)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Item_localToScreen__II(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		DEFINE_ADM_POINT(pt, x, y);
		sADMItem->LocalToScreenPoint(item, &pt);
		return gEngine->convertPoint(env, &pt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * java.awt.Point screenToLocal(int x, int y)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Item_screenToLocal__II(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		DEFINE_ADM_POINT(pt, x, y);
		sADMItem->ScreenToLocalPoint(item, &pt);
		return gEngine->convertPoint(env, &pt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * java.awt.Rectangle localToScreen(int x, int y, int width, int height)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Item_localToScreen__IIII(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		DEFINE_ADM_RECT(rt, x, y, width, height);
		sADMItem->LocalToScreenRect(item, &rt);
		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * java.awt.Rectangle screenToLocal(int x, int y, int width, int height)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Item_screenToLocal__IIII(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		DEFINE_ADM_RECT(rt, x, y, width, height);
		sADMItem->ScreenToLocalRect(item, &rt);
		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void invalidate()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_invalidate__(JNIEnv *env, jobject obj) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->Invalidate(item);
	} EXCEPTION_CONVERT(env)
}

/*
 * void invalidate(int x, int y, int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_invalidate__IIII(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		DEFINE_ADM_RECT(rt, x, y, width, height);
		sADMItem->InvalidateRect(item, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void update()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_update(JNIEnv *env, jobject obj) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->Update(item);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean isVisible()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_Item_isVisible(JNIEnv *env, jobject obj) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
	    return sADMItem->IsVisible(item);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setVisible(boolean arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_setVisible(JNIEnv *env, jobject obj, jboolean visible) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->Show(item, visible);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean isActive()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_Item_isActive(JNIEnv *env, jobject obj) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
	    return sADMItem->IsActive(item);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setActive(boolean arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_setActive(JNIEnv *env, jobject obj, jboolean active) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->Activate(item, active);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean isEnabled()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_Item_isEnabled(JNIEnv *env, jobject obj) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
	    return sADMItem->IsEnabled(item);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void seEnabled(boolean enabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_setEnabled(JNIEnv *env, jobject obj, jboolean enabled) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->Enable(item, enabled);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean isKnown()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_Item_isKnown(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		return sADMItem->IsKnown(item);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setKnown(boolean known)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_setKnown(JNIEnv *env, jobject obj, jboolean known) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->Known(item, known);
	} EXCEPTION_CONVERT(env)
}

/*
 * int getCursor()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Item_getCursor(JNIEnv *env, jobject obj) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		long cursor;
		const char* name;
		SPPluginRef pluginRef = sADMItem->GetPluginRef(item);
		sADMItem->GetCursorID(item, &pluginRef, &cursor, &name);
		return cursor;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void setCursor(int arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_setCursor(JNIEnv *env, jobject obj, jint cursor) {
	try {
	    ADMItemRef item = gEngine->getItemRef(env, obj);
		if (cursor >= 0) {
			SPPluginRef pluginRef = sADMItem->GetPluginRef(item);
			sADMItem->SetCursorID(item, pluginRef, cursor, NULL);
		}
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean wantsFocus()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_Item_wantsFocus(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		return sADMItem->GetWantsFocus(item);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setWantsFocus(boolean arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_setWantsFocus(JNIEnv *env, jobject obj, jboolean wantsFocus) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->SetWantsFocus(item, wantsFocus);
	} EXCEPTION_CONVERT(env)
}

/*
 * java.awt.Dimension getPreferredSize()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Item_getPreferredSize(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		ADMPoint point;
		sADMItem->GetBestSize(item, &point);
		return gEngine->convertDimension(env, &point);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void nativeSetTooltip(java.lang.String toolTip)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_nativeSetTooltip(JNIEnv *env, jobject obj, jstring toolTip) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		const jchar *chars = env->GetStringChars(toolTip, NULL);
		if (chars == NULL) EXCEPTION_CHECK(env)
		sADMItem->SetTipStringW(item, chars);
		env->ReleaseStringChars(toolTip, chars);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean isToolTipEnabled()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_Item_isToolTipEnabled(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		return sADMItem->IsTipEnabled(item);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setToolTipEnabled(boolean enabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_setToolTipEnabled(JNIEnv *env, jobject obj, jboolean enabled) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		return sADMItem->EnableTip(item, enabled);
	} EXCEPTION_CONVERT(env)
}

/*
 * void showToolTip(int x, int y)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_showToolTip(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		DEFINE_ADM_POINT(pt, x, y);
		sADMItem->ShowToolTip(item, &pt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void hideToolTip()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Item_hideToolTip(JNIEnv *env, jobject obj) {
	try {
		ADMItemRef item = gEngine->getItemRef(env, obj);
		sADMItem->HideToolTip(item);
	} EXCEPTION_CONVERT(env)
}
