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
 * $RCSfile: com_scriptographer_ai_Document.cpp,v $
 * $Author: lehni $
 * $Revision: 1.16 $
 * $Date: 2006/04/30 14:37:48 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerPlugin.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Document.h"
#include "com_scriptographer_ai_Art.h"

/*
 * com.scriptographer.ai.Document
 */

// DOCUMENT_BEGIN and DOCUMENT_END are necessary because only the active document
// can be accessed and modified throught sAIDocument. it seems like adobe forgot
// tu use the AIDocumentHandle parameter there...

#define DOCUMENT_BEGIN \
	AIDocumentHandle activeDoc = NULL; \
	AIDocumentHandle prevDoc = NULL; \
	try { \
		AIDocumentHandle doc = gEngine->getDocumentHandle(env, obj); \
		sAIDocument->GetDocument(&activeDoc); \
		if (activeDoc != doc) { \
			prevDoc = activeDoc; \
			sAIDocumentList->Activate(doc, false); \
		} \

#define DOCUMENT_END \
	} EXCEPTION_CONVERT(env) \
	if (prevDoc != NULL) \
		sAIDocumentList->Activate(prevDoc, false);
	
/*
 * int nativeCreate(java.io.File file, int colorModel, int dialogStatus)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Document_nativeCreate__Ljava_io_File_2II(JNIEnv *env, jclass cls, jobject file, jint colorModel, jint dialogStatus) {
	char *str = NULL;
	AIDocumentHandle doc = NULL;
	try {
		jstring path = (jstring) gEngine->callObjectMethod(env, file, gEngine->mid_File_getPath);
		str = gEngine->convertString(env, path);
		SPPlatformFileSpecification fileSpec;
		if (gPlugin->pathToFileSpec(str, &fileSpec)) {
#if kPluginInterfaceVersion < kAI12
			sAIDocumentList->Open(&fileSpec, (AIColorModel) colorModel, (ActionDialogStatus) dialogStatus, &doc);
#else
			ai::FilePath filePath(fileSpec);
			sAIDocumentList->Open(filePath, (AIColorModel) colorModel, (ActionDialogStatus) dialogStatus, &doc);
#endif
		}		
	} EXCEPTION_CONVERT(env)
	if (str != NULL)
		delete str;
	return (jint) doc;
}

/*
 * int nativeCreate(java.lang.String title, float width, float height, int colorModel, int dialogStatus)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Document_nativeCreate__Ljava_lang_String_2FFII(JNIEnv *env, jclass cls, jstring title, jfloat width, jfloat height, jint colorModel, jint dialogStatus) {
	AIDocumentHandle doc = NULL;
	AIColorModel model = (AIColorModel) colorModel;
#if kPluginInterfaceVersion < kAI12
	char *str = NULL;
	try {
		str = gEngine->convertString(env, title);
		sAIDocumentList->New(str, &model, &width, &height, (ActionDialogStatus) dialogStatus, &doc);
	} EXCEPTION_CONVERT(env)
	if (str != NULL)
		delete str;
#else
	try {
		ai::UnicodeString str = gEngine->convertUnicodeString(env, title);
		sAIDocumentList->New(str, &model, &width, &height, (ActionDialogStatus) dialogStatus, &doc);
	} EXCEPTION_CONVERT(env)
#endif
	return (jint) doc;
}

/*
 * com.scriptographer.ai.Point getPageOrigin()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_getPageOrigin(JNIEnv *env, jobject obj) {
	jobject origin = NULL;
	DOCUMENT_BEGIN

	AIRealPoint pt;
	sAIDocument->GetDocumentPageOrigin(&pt);
	origin = gEngine->convertPoint(env, &pt);

	DOCUMENT_END
	return origin;
}

/*
 * void setPageOrigin(com.scriptographer.ai.Point origin)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_setPageOrigin(JNIEnv *env, jobject obj, jobject origin) {
	DOCUMENT_BEGIN
		
	AIRealPoint pt;
	gEngine->convertPoint(env, origin, &pt);
	sAIDocument->SetDocumentPageOrigin(&pt);

	DOCUMENT_END
}

/*
 * com.scriptographer.ai.Point getRulerOrigin()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_getRulerOrigin(JNIEnv *env, jobject obj) {
	jobject origin = NULL;

	DOCUMENT_BEGIN

	AIRealPoint pt;
	sAIDocument->GetDocumentRulerOrigin(&pt);
	origin = gEngine->convertPoint(env, &pt);

	DOCUMENT_END

	return origin;
}

/*
 * void setRulerOrigin(com.scriptographer.ai.Point origin)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_setRulerOrigin(JNIEnv *env, jobject obj, jobject origin) {
	DOCUMENT_BEGIN

	AIRealPoint pt;
	gEngine->convertPoint(env, origin, &pt);
	sAIDocument->SetDocumentRulerOrigin(&pt);

	DOCUMENT_END
}

/*
 * com.scriptographer.ai.Point getSize()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_getSize(JNIEnv *env, jobject obj) {
	jobject size = NULL;

	DOCUMENT_BEGIN

	AIDocumentSetup setup;
	sAIDocument->GetDocumentSetup(&setup);
	DEFINE_POINT(pt, setup.width, setup.height);
	size = gEngine->convertPoint(env, &pt);

	DOCUMENT_END

	return size;
}

/*
 * void setSize(float width, float height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_setSize(JNIEnv *env, jobject obj, jfloat width, jfloat height) {
	DOCUMENT_BEGIN

	AIDocumentSetup setup;
	sAIDocument->GetDocumentSetup(&setup);
	setup.width = width;
	setup.height = height;
	sAIDocument->SetDocumentSetup(&setup);

	DOCUMENT_END
}

/*
 * com.scriptographer.ai.Rectangle getCropBox()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_getCropBox(JNIEnv *env, jobject obj) {
	jobject cropBox = NULL;

	DOCUMENT_BEGIN

	AIRealRect rt;
	sAIDocument->GetDocumentCropBox(&rt);
	cropBox = gEngine->convertRectangle(env, &rt);

	DOCUMENT_END
	
	return cropBox;
}

/*
 * void setCropBox(com.scriptographer.ai.Rectangle cropBox)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_setCropBox(JNIEnv *env, jobject obj, jobject cropBox) {
	DOCUMENT_BEGIN

	AIRealRect rt;
	gEngine->convertRectangle(env, cropBox, &rt);
	sAIDocument->SetDocumentCropBox(&rt);

	DOCUMENT_END
}

/*
 * boolean isModified()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Document_isModified(JNIEnv *env, jobject obj) {
	ASBoolean modified = false;
	
	DOCUMENT_BEGIN
	
	sAIDocument->GetDocumentModified(&modified);
	
	DOCUMENT_END
	
	return modified;
}

/*
 * void setModified(boolean modified)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_setModified(JNIEnv *env, jobject obj, jboolean modified) {
	DOCUMENT_BEGIN
	
	sAIDocument->SetDocumentModified(modified);

	DOCUMENT_END
}

/*
 * java.io.File getFile()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_getFile(JNIEnv *env, jobject obj) {
	jobject file = NULL;
	
	DOCUMENT_BEGIN

	SPPlatformFileSpecification fileSpec;
#if kPluginInterfaceVersion < kAI12
	sAIDocument->GetDocumentFileSpecification(&fileSpec);
#else
	ai::FilePath filePath;
	sAIDocument->GetDocumentFileSpecification(filePath);
	filePath.GetAsSPPlatformFileSpec(fileSpec);
#endif
	char path[kMaxPathLength];
	if (gPlugin->fileSpecToPath(&fileSpec, path)) {
		file = env->NewObject(gEngine->cls_File, gEngine->cid_File, gEngine->convertString(env, path));
	}
	
	DOCUMENT_END
	
	return file;
}

/*
 * java.lang.String[] nativeGetFormats()
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_ai_Document_nativeGetFormats(JNIEnv *env, jclass cls) {
	try {
		long count;
		sAIFileFormat->CountFileFormats(&count);
		jobjectArray array = env->NewObjectArray(count, gEngine->cls_String, NULL); 
		for (int i = 0; i < count; i++) {
			AIFileFormatHandle fileFormat = NULL;
			sAIFileFormat->GetNthFileFormat(i, &fileFormat);
			if (fileFormat != NULL) {
				char *name = NULL;
				sAIFileFormat->GetFileFormatName(fileFormat, &name);
				if (name != NULL) {
					env->SetObjectArrayElement(array, i, gEngine->convertString(env, name));
				}
			}
		}
		return array;
	} EXCEPTION_CONVERT(env)
	return NULL;
}


/*
 * void activate()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_activate(JNIEnv *env, jobject obj) {
	try {
		AIDocumentHandle doc = gEngine->getDocumentHandle(env, obj);
		sAIDocumentList->Activate(doc, true);
	} EXCEPTION_CONVERT(env)
}

/*
 * void print(int dialogStatus)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_print(JNIEnv *env, jobject obj, jint dialogStatus) {
	try {
		AIDocumentHandle doc = gEngine->getDocumentHandle(env, obj);
		sAIDocumentList->Print(doc, (ActionDialogStatus) dialogStatus);
	} EXCEPTION_CONVERT(env)
}

/*
 * void save()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_save(JNIEnv *env, jobject obj) {
	try {
		AIDocumentHandle doc = gEngine->getDocumentHandle(env, obj);
		sAIDocumentList->Save(doc);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean write(java.io.File file, Ljava.lang.String format, boolean ask)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Document_write(JNIEnv *env, jobject obj, jobject fileObj, jstring formatObj, jboolean ask) {
	jboolean ret = false;
	char *path = NULL;
	char *format = NULL;
	
	DOCUMENT_BEGIN
	
	jstring pathObj = (jstring) gEngine->callObjectMethod(env, fileObj, gEngine->mid_File_getPath);
	path = gEngine->convertString(env, pathObj);
	if (formatObj == NULL) format = "Adobe Illustrator Any Format Writer";
	else format = gEngine->convertString(env, formatObj);
	SPPlatformFileSpecification fileSpec;
	if (gPlugin->pathToFileSpec(path, &fileSpec)) {
#if kPluginInterfaceVersion < kAI12
		ret = !sAIDocument->WriteDocument(&fileSpec, format, ask);
#else
		ai::FilePath filePath(fileSpec);
		ret = !sAIDocument->WriteDocument(filePath, format, ask);
#endif
	}
	
	DOCUMENT_END

	if (path != NULL)
		delete path;
	if (format != NULL && formatObj != NULL)
		delete format;

	return ret;
}

/*
 * void close()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_close(JNIEnv *env, jobject obj) {
	try {
		AIDocumentHandle doc = gEngine->getDocumentHandle(env, obj);
		sAIDocumentList->Close(doc);
	} EXCEPTION_CONVERT(env)
}

/*
 * void redraw()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_redraw__(JNIEnv *env, jobject obj) {
	DOCUMENT_BEGIN
	
	sAIDocument->RedrawDocument();
	
	DOCUMENT_END
}

/*
 * void redraw(float x, float y, float width, float height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_redraw__FFFF(JNIEnv *env, jobject obj, jfloat x, jfloat y, jfloat width, jfloat height) {
	DOCUMENT_BEGIN

	// use the DocumentView's function SetDocumentViewInvalidDocumentRect that fits
	// much better here.
	// Acording to DocumentView.h, we don't need to pass a view handle, as this
	// document is now the current one anyhow and its view is on top of the others:	
	DEFINE_RECT(rect, x, y, width, height);
	sAIDocumentView->SetDocumentViewInvalidDocumentRect(NULL, &rect);
	
	DOCUMENT_END
}

/*
 * void copy()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_copy(JNIEnv *env, jobject obj) {
	DOCUMENT_BEGIN
	
	sAIDocument->Copy();
	
	DOCUMENT_END
}

/*
 * void cut()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_cut(JNIEnv *env, jobject obj) {
	DOCUMENT_BEGIN
	
	sAIDocument->Cut();
	
	DOCUMENT_END
}

/*
 * void paste()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_paste(JNIEnv *env, jobject obj) {
	DOCUMENT_BEGIN
	
	sAIDocument->Paste();
	
	DOCUMENT_END
}

// ArtSet stuff:

/*
 * boolean hasSelectedItems()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Document_hasSelectedItems(JNIEnv *env, jobject obj) {
	jboolean selected = false;

	DOCUMENT_BEGIN

	selected = sAIMatchingArt->IsSomeArtSelected();
	
	DOCUMENT_END

	return selected;
}

/*
 * com.scriptographer.ai.ArtSet getSelectedItems()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_getSelectedItems(JNIEnv *env, jobject obj) {
	jobject artSet = NULL;

	DOCUMENT_BEGIN

	// TODO: consider using Matching Art Suite instead!!! (faster, direct array access)
	AIArtSet set;
	if (!sAIArtSet->NewArtSet(&set)) {
		if (!sAIArtSet->SelectedArtSet(set)) {
			artSet = gEngine->convertArtSet(env, set);
			sAIArtSet->DisposeArtSet(&set);
		}
	}

	DOCUMENT_END

	return artSet;
}

/*
 * void deselectAll()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_deselectAll(JNIEnv *env, jobject obj) {
	DOCUMENT_BEGIN

#if kPluginInterfaceVersion >= kAI11
	sAIMatchingArt->DeselectAll();
#else
	AIArtHandle **matches;
	long numMatches;
	if (!sAIMatchingArt->GetSelectedArt(&matches, &numMatches)) {
		for (int i = 0; i < numMatches; i++)
			sAIArt->SetArtUserAttr((*matches)[i], kArtSelected, 0);
		sAIMDMemory->MdMemoryDisposeHandle((void **)matches);
	}
#endif

	DOCUMENT_END
}

/*
 * com.scriptographer.ai.ArtSet getMatchingItems(java.lang.Class typeClass, java.util.Map attributes)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_getMatchingItems(JNIEnv *env, jobject obj, jclass typeClass, jobject attributes) {
	jobject artSet = NULL;

	DOCUMENT_BEGIN

	AIArtSet set;
	if (!sAIArtSet->NewArtSet(&set)) {
		bool layerOnly = false;
		short type = Art_getType(env, typeClass);
		if (type == com_scriptographer_ai_Art_TYPE_LAYER) {
			type = kGroupArt;
			layerOnly = true;
		}
		AIArtSpec spec;
		spec.type = type;
		spec.whichAttr = 0;
		spec.attr = 0;
		// use the env's version of the callers for speed reasons. check for exceptions only once for every entry:
		jobject keySet = env->CallObjectMethod(attributes, gEngine->mid_Map_keySet);
		jobject iterator = env->CallObjectMethod(keySet, gEngine->mid_Set_iterator);
		while (env->CallBooleanMethod(iterator, gEngine->mid_Iterator_hasNext)) {
			jobject key = env->CallObjectMethod(iterator, gEngine->mid_Iterator_next);
			jobject value = env->CallObjectMethod(attributes, gEngine->mid_Map_get, key);
			jint set = -1;
			if (env->IsInstanceOf(value, gEngine->cls_Boolean)) {
				set = env->CallBooleanMethod(value, gEngine->mid_Boolean_booleanValue);
			} else if (env->IsInstanceOf(value, gEngine->cls_Number)) {
				set = env->CallIntMethod(value, gEngine->mid_Number_intValue) != 0;
			}
			if (set != -1) {
				jint flag = -1;
				// the flag can be specified both as a Art.ATTR_* Integer or a string. These strings are allowed:
				// selected, locked, hidden.
				// TODO: Implement full wrapping of ATTR_* values in Strings!
				if (env->IsInstanceOf(key, gEngine->cls_Number)) {
					flag = env->CallIntMethod(key, gEngine->mid_Number_intValue);
				} else if (env->IsInstanceOf(key, gEngine->cls_String)) {
					char *str = gEngine->convertString(env, (jstring) key);
					if (strcmp(str, "selected") == 0) {
						flag = kArtSelected;
					} else if (strcmp(str, "locked") == 0) {
						flag = kArtLocked;
					} else if (strcmp(str, "hidden") == 0) {
						flag = kArtHidden;
					}
					delete str;
				}
				if (flag != -1) {
					spec.whichAttr |= flag;
					if (set) spec.attr |=  flag;
				}
			}
			EXCEPTION_CHECK(env)
		}
		if (!sAIArtSet->MatchingArtSet(&spec, 1, set)) {
			artSet = gEngine->convertArtSet(env, set);
			sAIArtSet->DisposeArtSet(&set);
		}
	}

	DOCUMENT_END

	return artSet;
}

/*
 * com.scriptographer.ai.Path createRectangle(com.scriptographer.ai.Rect rect)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_createRectangle(JNIEnv *env, jobject obj, jobject rect) {
	jobject path = NULL;

	DOCUMENT_BEGIN

	AIRealRect rt;
	gEngine->convertRectangle(env, rect, &rt);
	AIArtHandle handle;
	sAIShapeConstruction->NewRect(rt.top, rt.left, rt.bottom, rt.right, false, &handle);
	path = gEngine->wrapArtHandle(env, handle);

	DOCUMENT_END

	return path;
}

/*
 * com.scriptographer.ai.Path createRoundRectangle(com.scriptographer.ai.Rectangle rect, float hor, float ver)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_createRoundRectangle(JNIEnv *env, jobject obj, jobject rect, jfloat hor, jfloat ver) {
	jobject path = NULL;

	DOCUMENT_BEGIN

	AIRealRect rt;
	gEngine->convertRectangle(env, rect, &rt);
	AIArtHandle handle;
	sAIShapeConstruction->NewRoundedRect(rt.top, rt.left, rt.bottom, rt.right, hor, ver, false, &handle);
	path = gEngine->wrapArtHandle(env, handle);

	DOCUMENT_END

	return path;
}

/*
 * com.scriptographer.ai.Path createOval(com.scriptographer.ai.Rectangle rect, boolean circumscribed)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_createOval(JNIEnv *env, jobject obj, jobject rect, jboolean circumscribed) {
	jobject path = NULL;

	DOCUMENT_BEGIN

	AIRealRect rt;
	gEngine->convertRectangle(env, rect, &rt);
	AIArtHandle handle;
	if (circumscribed)
		sAIShapeConstruction->NewCircumscribedOval(rt.top, rt.left, rt.bottom, rt.right, false, &handle);
	else
		sAIShapeConstruction->NewInscribedOval(rt.top, rt.left, rt.bottom, rt.right, false, &handle);
	path = gEngine->wrapArtHandle(env, handle);

	DOCUMENT_END

	return path;
}

/*
 * com.scriptographer.ai.Path createRegularPolygon(int numSides, com.scriptographer.ai.Point center, float radius)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_createRegularPolygon(JNIEnv *env, jobject obj, jint numSides, jobject center, jfloat radius) {
	jobject path = NULL;

	DOCUMENT_BEGIN

	AIRealPoint pt;
	gEngine->convertPoint(env, center, &pt);
	AIArtHandle handle;
	sAIShapeConstruction->NewRegularPolygon(numSides, pt.h, pt.v, radius, false, &handle);
	path = gEngine->wrapArtHandle(env, handle);

	DOCUMENT_END

	return path;
}

/*
 * com.scriptographer.ai.Path createStar(int numPoints, com.scriptographer.ai.Point center, float radius1, float radius2)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_createStar(JNIEnv *env, jobject obj, jint numPoints, jobject center, jfloat radius1, jfloat radius2) {
	jobject path = NULL;

	DOCUMENT_BEGIN

	AIRealPoint pt;
	gEngine->convertPoint(env, center, &pt);
	AIArtHandle handle;
	sAIShapeConstruction->NewStar(numPoints, pt.h, pt.v, radius1, radius2, false, &handle);
	path = gEngine->wrapArtHandle(env, handle);

	DOCUMENT_END

	return path;
}

/*
 * com.scriptographer.ai.Path createSpiral(com.scriptographer.ai.Point firstArcCenter, com.scriptographer.ai.Point start, float decayPercent, int numQuarterTurns, boolean clockwiseFromOutside)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_createSpiral(JNIEnv *env, jobject obj, jobject firstArcCenter, jobject start, jfloat decayPercent, jint numQuarterTurns, jboolean clockwiseFromOutside) {
	jobject path = NULL;

	DOCUMENT_BEGIN

	AIRealPoint ptCenter, ptStart;
	gEngine->convertPoint(env, firstArcCenter, &ptCenter);
	gEngine->convertPoint(env, start, &ptStart);
	AIArtHandle handle;
	sAIShapeConstruction->NewSpiral(ptCenter, ptStart, decayPercent, numQuarterTurns, clockwiseFromOutside, &handle);
	path = gEngine->wrapArtHandle(env, handle);

	DOCUMENT_END

	return path;
}

/*
 * void nativeGetDictionary(com.scriptographer.ai.Dictionary map)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_nativeGetDictionary(JNIEnv *env, jobject obj, jobject map) {
	AIDictionaryRef dictionary = NULL;

	DOCUMENT_BEGIN

	AIArtHandle handle = gEngine->getArtHandle(env, obj);
	sAIDocument->GetDictionary(&dictionary);
	if (dictionary != NULL)
		gEngine->convertDictionary(env, dictionary, map, false, true); 

	DOCUMENT_END

	if (dictionary != NULL)
		sAIDictionary->Release(dictionary);
}

/*
 * void nativeSetDictionary(com.scriptographer.ai.Dictionary map)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_nativeSetDictionary(JNIEnv *env, jobject obj, jobject map) {
	AIDictionaryRef dictionary = NULL;

	DOCUMENT_BEGIN

	AIArtHandle handle = gEngine->getArtHandle(env, obj);
	sAIDocument->GetDictionary(&dictionary);
	if (dictionary != NULL)
		gEngine->convertDictionary(env, map, dictionary, false, true); 

	DOCUMENT_END

	if (dictionary != NULL)
		sAIDictionary->Release(dictionary);
}

/*
 * com.scriptographer.ai.HitTest hitTest(com.scriptographer.ai.Point point, int type, com.scriptographer.ai.Art art)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_hitTest(JNIEnv *env, jobject obj, jobject point, jint type, jobject art) {
	jobject hitTest = NULL;

	DOCUMENT_BEGIN

	AIRealPoint pt;
	gEngine->convertPoint(env, point, &pt);

    AIArtHandle handle = gEngine->getArtHandle(env, art);
    
	AIHitRef hit;
	if (!sAIHitTest->HitTest(handle, &pt, type, &hit)) {
		AIToolHitData toolHit;
		if (sAIHitTest->IsHit(hit) && !sAIHitTest->GetHitData(hit, &toolHit)) {
			int type = toolHit.type;
			// Support for hittest on text frames:
			if (Art_getType(toolHit.object) == kTextFrameArt) {
				int textPart = sAITextFrameHit->GetPart(hit);
				if (textPart != kAITextNowhere)
					type = textPart + 6;
			}
			jobject art = gEngine->wrapArtHandle(env, toolHit.object);
			jobject point = gEngine->convertPoint(env, &toolHit.point);
			hitTest = gEngine->newObject(env, gEngine->cls_HitTest, gEngine->cid_HitTest, type, art, (jint) toolHit.segment, (jfloat) toolHit.t, point);
		}
		sAIHitTest->Release(hit);
	}

	DOCUMENT_END

	return hitTest;
}

/*
 * int nativeGetStories()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Document_nativeGetStories(JNIEnv *env, jobject obj) {
	using namespace ATE;

	jint ret = 0;

	DOCUMENT_BEGIN
	
	// this is annoying:
	// request a text frame and get the stories from there....
	AIMatchingArtSpec spec;
	spec.type = kTextFrameArt;
	spec.whichAttr = 0;
	spec.attr = 0;

	AIArtHandle **matches;
	long numMatches;
	if (!sAIMatchingArt->GetMatchingArt(&spec, 1, &matches, &numMatches)) {
		if (numMatches > 0) {
			TextFrameRef frame;
			StoryRef story;
			StoriesRef stories;
			if (!sAITextFrame->GetATETextFrame((*matches)[0], &frame) &&
				!sTextFrame->GetStory(frame, &story) &&
				!sStory->GetStories(story, &stories)) {
				ret = (jint) stories;
			}
		}
		sAIMDMemory->MdMemoryDisposeHandle((void **)matches);
	}

	DOCUMENT_END

	return ret;
}

/*
 * void suspendTextReflow()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_suspendTextReflow(JNIEnv *env, jclass cls) {
	try {
		sAIDocument->SuspendTextReflow();
	} EXCEPTION_CONVERT(env)
}

/*
 * void resumeTextReflow()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_resumeTextReflow(JNIEnv *env, jclass cls) {
	try {
		sAIDocument->ResumeTextReflow();
	} EXCEPTION_CONVERT(env)
}
