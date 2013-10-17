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
#include "com_scriptographer_ai_Annotator.h"

/*
 * com.scriptographer.ai.Annotator
 */

/*
 * int nativeCreate(java.lang.String name)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Annotator_nativeCreate(JNIEnv *env, jobject obj, jstring name) {
	try {
		char *str = gEngine->convertString(env, name);
		AIAnnotatorHandle annotator = NULL;
		sAIAnnotator->AddAnnotator(gPlugin->getPluginRef(), str, &annotator);
		delete str;
		return (jint) annotator;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * java.util.ArrayList nativeGetAnnotators()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Annotator_nativeGetAnnotators(JNIEnv *env, jclass cls) {
	try {
		jobject array = gEngine->newObject(env, gEngine->cls_ArrayList, gEngine->cid_ArrayList);
		long count;
		sAIAnnotator->CountAnnotators(&count);
		SPPluginRef plugin = gPlugin->getPluginRef();
		for (int i = 0; i < count; i++) {
			AIAnnotatorHandle annotator;
			SPPluginRef annotatorPlugin;
			if (!sAIAnnotator->GetNthAnnotator(i, &annotator) &&
				!sAIAnnotator->GetAnnotatorPlugin(annotator, &annotatorPlugin) &&
				plugin == annotatorPlugin) {
				// create the wrapper
				jobject annotatorObj = gEngine->newObject(env, gEngine->cls_ai_Annotator, gEngine->cid_ai_Annotator, (jint) annotator);
				// and add it to the array
				gEngine->callObjectMethod(env, array, gEngine->mid_Collection_add, annotatorObj);
			}
		}
		return array;
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * boolean nativeSetActive(int handle, boolean active)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Annotator_nativeSetActive(JNIEnv *env, jobject obj, jint handle, jboolean active) {
	return !sAIAnnotator->SetAnnotatorActive((AIAnnotatorHandle) handle, active);
}

/*
 * void nativeInvalidate(int viewHandle, int x, int y, int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Annotator_nativeInvalidate(JNIEnv *env, jobject obj, jint viewHandle, jint x, jint y, jint width, jint height) {
#ifndef ADM_FREE
	DEFINE_ADM_RECT(rect, x, y, width, height);
	sAIAnnotator->InvalAnnotationRect((AIDocumentViewHandle) viewHandle, &rect);
#endif //#ifndef ADM_FREE
}

/*
 * com.scriptographer.ui.Drawer nativeCreateDrawer(int portHandle)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Annotator_nativeCreateDrawer(JNIEnv *env, jclass cls, jint portHandle) {
	try {
		// This is a nice trick: (Ab)use the ADMDrawer for drawing into
		// the current view's viewport, as the ports are compatible!
		// use a rect that is big enough
		ADMRect rect;
		rect.left = 0;
		rect.top = 0;
		rect.right = 10000;
		rect.bottom = 10000;
#ifndef ADM_FREE
		// on windows, ADMPortRef and AIPortRef is the same
		// on mac, ADMPortRef is a GrafPort *, and AIPortRef is a OpaqueGrafPtr *
		// but for some reason, assuming it's a ADMPortRef works on both, so it 
		// must be actually the same thing...
#if _kADMDrawerSuiteVersion == 5
		ADMDrawerRef drawer = sADMDrawer->Create((ADMPortRef) portHandle, &rect, kADMDefaultFont, false);
#else
		ADMDrawerRef drawer = sADMDrawer->Create((ADMPortRef) portHandle, &rect, kADMDefaultFont);
#endif
		return gEngine->newObject(env, gEngine->cls_adm_Drawer, gEngine->cid_adm_Drawer, (jint) drawer);
#endif //#ifndef ADM_FREE
	} EXCEPTION_CONVERT(env);
	return NULL;
}
