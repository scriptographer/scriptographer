#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "admGlobals.h"
#include "com_scriptographer_adm_Dialog.h"
#include "Plugin.h"
#include "resourceIds.h"

/*
 * com.scriptographer.ai.Dialog
 */

ASErr ASAPI callbackDialogInit(ADMDialogRef dialog) {
	// hide the dialog by default:
	sADMDialog->Show(dialog, false);
	jobject obj = gEngine->getDialogObject(dialog);
	JNIEnv *env = gEngine->getEnv();
	// set size and bounds:
	ADMRect rect;
	sADMDialog->GetLocalRect(dialog, &rect);
	DEFINE_ADM_POINT(size, rect.right, rect.bottom);
	gEngine->setObjectField(env, obj, gEngine->fid_Dialog_size, gEngine->convertDimension(env, &size));
	sADMDialog->GetBoundsRect(dialog, &rect);
	gEngine->setObjectField(env, obj, gEngine->fid_Dialog_bounds, gEngine->convertRectangle(env, &rect));
	// Attach the dialog-level callbacks
	sADMDialog->SetDestroyProc(dialog, callbackDialogDestroy);
	sADMDialog->SetNotifyProc(dialog, callbackDialogNotify);
	// resize handler:
	ADMItemRef resizeItemRef = sADMDialog->GetItem(dialog, kADMResizeItemID);
	if (resizeItemRef) sADMItem->SetNotifyProc(resizeItemRef, callbackDialogResize);
	return kNoErr;
}

void ASAPI callbackDialogDestroy(ADMDialogRef dialog) {
	JNIEnv *env = gEngine->getEnv();
	try {
		// dialogSavePreference(dialog, gPlugin->console.name);
		jobject obj = gEngine->getDialogObject(dialog);
		gEngine->callOnDestroy(obj);
		// clear the handle:
		gEngine->setIntField(env, obj, gEngine->fid_ADMObject_handle, 0);
		env->DeleteGlobalRef(obj);
	} EXCEPTION_CATCH_REPORT(env)
}

void ASAPI callbackDialogResize(ADMItemRef item, ADMNotifierRef notifier) {
	sADMItem->DefaultNotify(item, notifier);
	if (sADMNotifier->IsNotifierType(notifier, kADMBoundsChangedNotifier)) {
		JNIEnv *env = gEngine->getEnv();
		try {
			ADMDialogRef dialog = sADMItem->GetDialog(item);
			jobject obj = gEngine->getDialogObject(dialog);
			jobject size = gEngine->getObjectField(env, obj, gEngine->fid_Dialog_size);
			if (size != NULL) {
				ADMPoint pt;
				gEngine->convertDimension(env, size, &pt);
				// calculate differnce:
				ADMRect rt;
				sADMDialog->GetLocalRect(dialog, &rt);
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
			}
		} EXCEPTION_CATCH_REPORT(env)
	}
}

void ASAPI callbackDialogNotify(ADMDialogRef dialog, ADMNotifierRef notifier) {
	sADMDialog->DefaultNotify(dialog, notifier);
	jobject obj = gEngine->getDialogObject(dialog);
	gEngine->callOnNotify(obj, notifier);
}

ASBoolean ASAPI callbackDialogTrack(ADMDialogRef dialog, ADMTrackerRef tracker) {
	jobject obj = gEngine->getDialogObject(dialog);
	ASBoolean ret = gEngine->callOnTrack(obj, tracker);
	if (ret)
		ret = sADMDialog->DefaultTrack(dialog, tracker);
	return ret;
}

void ASAPI callbackDialogDraw(ADMDialogRef dialog, ADMDrawerRef drawer) {
	sADMDialog->DefaultDraw(dialog, drawer);
	jobject obj = gEngine->getDialogObject(dialog);
	gEngine->callOnDraw(obj, drawer);
}

/*
 * int nativeCreate(java.lang.String name, int style, int options)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Dialog_nativeCreate(JNIEnv *env, jobject obj, jstring name, jint style, jint options) {
	try {
		char *str = gEngine->createCString(env, name);
		ADMDialogRef dialog = sADMDialog->Create(gPlugin->getPluginRef(), str, kEmptyDialogID, (ADMDialogStyle) style, callbackDialogInit, env->NewGlobalRef(obj), options);
		delete str;
		if (dialog == NULL)
			throw new StringException("Cannot create dialog.");
		return (jint) dialog;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void nativeDestroy(int handle)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_nativeDestroy(JNIEnv *env, jobject obj, jint handle) {
	try {
		sADMDialog->Destroy((ADMDialogRef) handle);
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeSetTrackCallback(boolean enabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_nativeSetTrackCallback(JNIEnv *env, jobject obj, jboolean enabled) {
	try {
		ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->SetTrackProc(dialog, enabled ? callbackDialogTrack : NULL);
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeSetDrawCallback(boolean enabled)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_nativeSetDrawCallback(JNIEnv *env, jobject obj, jboolean enabled) {
	try {
		ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->SetDrawProc(dialog, enabled ? callbackDialogDraw : NULL);
	} EXCEPTION_CONVERT(env)
}

/*
 * java.awt.Dimension nativeGetSize()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_nativeGetSize(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		ADMRect size;
		sADMDialog->GetLocalRect(dialog, &size);
		return gEngine->convertDimension(env, size.right, size.bottom);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void nativeSetSize(int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_nativeSetSize(JNIEnv *env, jobject obj, jint width, jint height) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
	    DEFINE_ADM_RECT(rt, 0, 0, width, height);
		sADMDialog->SetLocalRect(dialog, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * java.awt.Rectangle nativeGetBounds()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_nativeGetBounds(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		ADMRect rt;
		sADMDialog->GetBoundsRect(dialog, &rt);
		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void nativeSetBounds(int x, int y, int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_nativeSetBounds(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		DEFINE_ADM_RECT(rt, x, y, width, height);
		sADMDialog->SetBoundsRect(dialog, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void setLocation(int x, int y)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setLocation(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->Move(dialog, x, y);
	} EXCEPTION_CONVERT(env)
}

/*
 * java.awt.Point getLocation()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_getLocation(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		ADMRect rt;
		sADMDialog->GetBoundsRect(dialog, &rt);
		DEFINE_ADM_POINT(pt, rt.left, rt.top);
		return gEngine->convertPoint(env, &pt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * java.awt.Dimension getMinimumSize()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_getMinimumSize(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		ADMPoint pt;
		pt.h = sADMDialog->GetMinWidth(dialog);
		pt.v = sADMDialog->GetMinHeight(dialog);
		return gEngine->convertDimension(env, &pt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void setMinimumSize(int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setMinimumSize(JNIEnv *env, jobject obj, jint width, jint height) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->SetMinWidth(dialog, width);
		sADMDialog->SetMinHeight(dialog, height);
	} EXCEPTION_CONVERT(env)
}

/*
 * java.awt.Dimension getMaximumSize()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_getMaximumSize(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		DEFINE_ADM_POINT(pt, sADMDialog->GetMaxWidth(dialog), sADMDialog->GetMaxHeight(dialog));
		return gEngine->convertDimension(env, &pt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void setMaximumSize(int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setMaximumSize(JNIEnv *env, jobject obj, jint width, jint height) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->SetMaxWidth(dialog, width);
		sADMDialog->SetMaxHeight(dialog, height);
	} EXCEPTION_CONVERT(env)
}

/*
 * java.awt.Dimension getIncrement()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_getIncrement(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		ADMPoint pt;
		pt.h = sADMDialog->GetHorizontalIncrement(dialog);
		pt.v = sADMDialog->GetVerticalIncrement(dialog);
		return gEngine->convertDimension(env, &pt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void setIncrement(int hor, int ver)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setIncrement(JNIEnv *env, jobject obj, jint hor, jint ver) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->SetHorizontalIncrement(dialog, hor);
		sADMDialog->SetVerticalIncrement(dialog, ver);
	} EXCEPTION_CONVERT(env)
}

/*
 * int getItemHandle(int itemID)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Dialog_getItemHandle(JNIEnv *env, jobject obj, jint itemID) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		return (jint) sADMDialog->GetItem(dialog, itemID);
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * java.awt.Point localToScreen(int x, int y)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_localToScreen__II(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		DEFINE_ADM_POINT(pt, x, y);
		sADMDialog->LocalToScreenPoint(dialog, &pt);
		return gEngine->convertPoint(env, &pt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * java.awt.Point screenToLocal(int x, int y)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_screenToLocal__II(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		DEFINE_ADM_POINT(pt, x, y);
		sADMDialog->ScreenToLocalPoint(dialog, &pt);
		return gEngine->convertPoint(env, &pt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * java.awt.Rectangle localToScreen(int x, int y, int width, int height)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_localToScreen__IIII(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		DEFINE_ADM_RECT(rt, x, y, width, height);
		sADMDialog->LocalToScreenRect(dialog, &rt);
		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * java.awt.Rectangle screenToLocal(int x, int y, int width, int height)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_screenToLocal__IIII(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		DEFINE_ADM_RECT(rt, x, y, width, height);
		sADMDialog->ScreenToLocalRect(dialog, &rt);
		return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void invalidate()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_invalidate__(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->Invalidate(dialog);
	} EXCEPTION_CONVERT(env)
}

/*
 * void invalidate(int x, int y, int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_invalidate__IIII(JNIEnv *env, jobject obj, jint x, jint y, jint width, jint height) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		DEFINE_ADM_RECT(rt, x, y, width, height);
		sADMDialog->InvalidateRect(dialog, &rt);
	} EXCEPTION_CONVERT(env)
}

/*
 * void update()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_update(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->Update(dialog);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean isVisible()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_Dialog_isVisible(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
	    return sADMDialog->IsVisible(dialog);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setVisible(boolean visible)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setVisible(JNIEnv *env, jobject obj, jboolean visible) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->Show(dialog, visible);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean isActive()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_Dialog_isActive(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
	    return sADMDialog->IsActive(dialog);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setActive(boolean arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setActive(JNIEnv *env, jobject obj, jboolean active) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->Activate(dialog, active);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean isEnabled()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_Dialog_isEnabled(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
	    return sADMDialog->IsEnabled(dialog);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void seEnabled(boolean arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setEnabled(JNIEnv *env, jobject obj, jboolean enabled) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->Enable(dialog, enabled);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean isUpdateEnabled()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_Dialog_isUpdateEnabled(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
	    return sADMDialog->IsUpdateEnabled(dialog);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void seUpdateEnabled(boolean arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setUpdateEnabled(JNIEnv *env, jobject obj, jboolean updateEnabled) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->Enable(dialog, updateEnabled);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean isCollapsed()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_Dialog_isCollapsed(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
	    return sADMDialog->IsCollapsed(dialog);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * int getCursor()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Dialog_getCursor(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		long cursor;
		SPPluginRef pluginRef = sADMDialog->GetPluginRef(dialog);
		sADMDialog->GetCursorID(dialog, &pluginRef, &cursor);
		return cursor;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void setCursor(int cursor)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setCursor(JNIEnv *env, jobject obj, jint cursor) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		if (cursor >= 0) {
			SPPluginRef pluginRef = sADMDialog->GetPluginRef(dialog);
			sADMDialog->SetCursorID(dialog, pluginRef, cursor);
		}
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeSetTitle(java.lang.String title)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_nativeSetTitle(JNIEnv *env, jobject obj, jstring title) {
	try {
		ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		if (title != NULL) {
			const jchar *chars = env->GetStringChars(title, NULL);
			if (chars == NULL) EXCEPTION_CHECK(env)
			sADMDialog->SetTextW(dialog, chars);
			env->ReleaseStringChars(title, chars);
		} else {
			sADMDialog->SetText(dialog, "");
		}
	} EXCEPTION_CONVERT(env)
}

/*
 * int getFont()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Dialog_getFont(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		return sADMDialog->GetFont(dialog);
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void setFont(int font)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setFont(JNIEnv *env, jobject obj, jint font) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		if (font >= 0) sADMDialog->SetFont(dialog, (ADMFont)font);
	} EXCEPTION_CONVERT(env)
}

/*
 * com.scriptographer.adm.DialogItem getDefaultItem()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_getDefaultItem(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		ADMItemRef itm = sADMDialog->GetItem(dialog, sADMDialog->GetDefaultItemID(dialog));
		if (itm != NULL)
			return gEngine->getItemObject(itm);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void setDefaultItem(com.scriptographer.adm.DialogItem item)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setDefaultItem(JNIEnv *env, jobject obj, jobject item) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
	    ADMItemRef itm = gEngine->getItemRef(env, item);
	    if (itm != NULL)
	    	sADMDialog->SetDefaultItemID(dialog, sADMItem->GetID(itm));
	} EXCEPTION_CONVERT(env)
}

/*
 * com.scriptographer.adm.DialogItem getCancelItem()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_getCancelItem(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		ADMItemRef itm = sADMDialog->GetItem(dialog, sADMDialog->GetCancelItemID(dialog));
		if (itm != NULL)
			return gEngine->getItemObject(itm);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void setCancelItem(com.scriptographer.adm.DialogItem item)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setCancelItem(JNIEnv *env, jobject obj, jobject item) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
	    ADMItemRef itm = gEngine->getItemRef(env, item);
	    if (itm != NULL)
	    	sADMDialog->SetCancelItemID(dialog, sADMItem->GetID(itm));
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean isForcedOnScreen()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_adm_Dialog_isForcedOnScreen(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		return sADMDialog->IsForcedOnScreen(dialog);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setForcedOnScreen(boolean forcedOnScreen)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setForcedOnScreen(JNIEnv *env, jobject obj, jboolean forcedOnScreen) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		sADMDialog->SetForcedOnScreen(dialog, forcedOnScreen);
	} EXCEPTION_CONVERT(env)
}

/*
 * com.scriptographer.adm.DialogGroupInfo getGroupInfo()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_getGroupInfo(JNIEnv *env, jobject obj) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		const char *group;
		long positionCode;
		sADMDialogGroup->GetDialogGroupInfo(dialog, &group, &positionCode);
		return gEngine->newObject(env, gEngine->cls_DialogGroupInfo, gEngine->cid_DialogGroupInfo, env->NewStringUTF(group), (jint) positionCode);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void setGroupInfo(java.lang.String group, int positionCode)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Dialog_setGroupInfo(JNIEnv *env, jobject obj, jstring group, jint positionCode) {
	try {
	    ADMDialogRef dialog = gEngine->getDialogRef(env, obj);
		char *groupStr = gEngine->createCString(env, group);
		sADMDialogGroup->SetDialogGroupInfo(dialog, groupStr, positionCode);
		delete groupStr;
	} EXCEPTION_CONVERT(env)
}

/*
 * java.lang.String nativeFileDialog(java.lang.String message, java.lang.String filter, java.lang.String directory, java.lang.String filename, boolean open)
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_adm_Dialog_nativeFileDialog(JNIEnv *env, jclass cls, jstring message, jstring filter, jstring directory, jstring filename, jboolean open) {
	jstring ret = NULL;
	try {
		// Unicode seems to not work (at least not on Windows?)
		// So use normal string instead...
		// const jchar *msg = env->GetStringChars(message, NULL);
		// if (msg == NULL) EXCEPTION_CHECK(env)
		char *msg = gEngine->createCString(env, message);
		char *fltr = gEngine->createCString(env, filter);
		char *name = NULL;
		SPPlatformFileSpecification dir, result;
		bool hasDir = false;

		if (directory != NULL) {
			char *path = gEngine->createCString(env, directory);
			hasDir = gPlugin->pathToFileSpec(path, &dir);
			delete path;

			if (filename != NULL)
				name = gEngine->createCString(env, filename);
		}

		ADMPlatformFileTypesSpecification3 specs;
		// this is needed in order to zero out the mac specific part on mac...
		memset(&specs, 0, sizeof(ADMPlatformFileTypesSpecification3));
		memcpy(specs.filter, fltr, MIN(kADMMaxFilterLength, env->GetStringLength(filter)));

		if (open ? 
			sADMBasic->StandardGetFileDialog(msg, &specs, hasDir ? &dir : NULL, name, &result) :
			sADMBasic->StandardPutFileDialog(msg, &specs, hasDir ? &dir : NULL, name, &result)) {
			char path[kMaxPathLength];
			
			if (gPlugin->fileSpecToPath(&result, path))
				ret = gEngine->createJString(env, path);
		}

		// env->ReleaseStringChars(message, msg);
		delete msg;
		delete fltr;
		if (name != NULL)
			delete name;
	} EXCEPTION_CONVERT(env)
	return ret;
}

/*
 * java.io.File chooseDirectory(java.lang.String message, java.io.File directory)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_chooseDirectory(JNIEnv *env, jclass cls, jstring message, jobject directory) {
	jobject ret = NULL;
	try {
		// Unicode seems to not work (at least not on Windows?)
		// So use normal string instead...
		// const jchar *msg = env->GetStringChars(message, NULL);
		// if (msg == NULL) EXCEPTION_CHECK(env)
		char *msg = gEngine->createCString(env, message);

		SPPlatformFileSpecification dir, result;
		bool hasDir = false;

		if (directory != NULL) {
			char *path = gEngine->createCString(env, 
				(jstring) gEngine->callObjectMethod(env, directory, gEngine->mid_File_getPath));
			hasDir = gPlugin->pathToFileSpec(path, &dir);
			delete path;
		}

		if (sADMBasic->StandardGetDirectoryDialog(msg, &dir, &result)) {
			char path[kMaxPathLength];
			gPlugin->fileSpecToPath(&result, path);
			ret = gEngine->newObject(env, gEngine->cls_File, gEngine->cid_File, gEngine->createJString(env, path));
		}

		// env->ReleaseStringChars(message, msg);
		delete msg;
	} EXCEPTION_CONVERT(env)
	return ret;
}

/*
 * java.awt.Color chooseColor(java.awt.Point point, java.awt.Color color)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_chooseColor(JNIEnv *env, jclass cls, jobject point, jobject color) {
	try {
		ADMRGBColor col, result;
		if (color != NULL) {
			gEngine->convertColor(env, color, &col);
		} else {
			// white:
			col.red = 65535;
			col.green = 65535;
			col.blue = 65535;
		}
		ADMPoint pt;
		if (point != NULL) {
			gEngine->convertPoint(env, point, &pt);
		} else {
			// center it on the main screen:
			pt.h = 0;
			pt.v = 0;
			ADMRect rect;
			sADMBasic->GetScreenDimensions(&pt, &rect);
			pt.h = (rect.right - rect.left) / 2;
			pt.v = (rect.bottom - rect.top) / 2;
		}
		sADMBasic->ChooseColor(pt, &col, &result);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * java.awt.Rectangle getPaletteLayoutBounds()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_adm_Dialog_getPaletteLayoutBounds(JNIEnv *env, jclass cls) {
	try {
		ADMRect bounds;
		sADMBasic->GetPaletteLayoutBounds(&bounds);
		return gEngine->convertRectangle(env, &bounds);
	} EXCEPTION_CONVERT(env)
	return NULL;
}
