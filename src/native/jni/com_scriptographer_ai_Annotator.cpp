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

#include "StdHeaders.h"
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
	DEFINE_ADM_RECT(rect, x, y, width, height);
	sAIAnnotator->InvalAnnotationRect((AIDocumentViewHandle) viewHandle, &rect);
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
	} EXCEPTION_CONVERT(env);
	return NULL;
}
