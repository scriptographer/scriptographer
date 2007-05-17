/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id$
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "ScriptographerPlugin.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Art.h"

/*
 * com.scriptographer.ai.Art
 */

bool Art_isValid(AIArtHandle art) {
#if kPluginInterfaceVersion < kAI12
	return sAIArt->ValidArt(art);
#else
	// TODO: If searchAllLayerLists is true, then this does a search through all layers in
	// all layer lists. Otherwise, it only does a search on the current layer list.
	return sAIArt->ValidArt(art, false);
#endif
}

short Art_getType(AIArtHandle art) {
	short type = -1;
	sAIArt->GetArtType(art, &type);
	return type;
}

short Art_getType(JNIEnv *env, jclass cls) {
	if (env->IsSameObject(cls, gEngine->cls_ai_Art)) {
		return kAnyArt;
	} else if (env->IsSameObject(cls, gEngine->cls_ai_Path)) {
		return kPathArt;
	} else if (env->IsSameObject(cls, gEngine->cls_ai_CompoundPath)) {
		return kCompoundPathArt;
	} else if (env->IsSameObject(cls, gEngine->cls_ai_Raster)) {
		return kRasterArt;
	} else if (env->IsSameObject(cls, gEngine->cls_ai_PlacedItem)) {
		return kPlacedArt;
	} else if (env->IsAssignableFrom(cls, gEngine->cls_ai_TextFrame)) {
		return kTextFrameArt;
	} else if (env->IsAssignableFrom(cls, gEngine->cls_ai_Tracing)) {
		// special defined type for tracings, needs handling!
		return com_scriptographer_ai_Art_TYPE_TRACING;
	} else if (env->IsSameObject(cls, gEngine->cls_ai_Layer)) {
		// special defined type for layers, needs handling!
		return com_scriptographer_ai_Art_TYPE_LAYER;
	} else if (env->IsSameObject(cls, gEngine->cls_ai_Group)) {
		return kGroupArt;
	}
	return kUnknownArt;
	// TODO: make sure the above list contains all Art classes!
}

jboolean Art_hasChildren(AIArtHandle art) {
	// don't show the children of textPaths and pointText 
#if kPluginInterfaceVersion < kAI11
	short type = artGetType(art);
	return (type == kTextArt && artGetTextType(art) != kPointTextType) || (type != kTextPathArt);
#else
	return true;
#endif
}

jboolean Art_isLayer(AIArtHandle art) {
	ASBoolean isLayerGroup = false;
	sAIArt->IsArtLayerGroup(art, &isLayerGroup);
	return isLayerGroup;
}

AIArtHandle Art_rasterize(AIArtHandle art, AIRasterizeType type, float resolution, int antialiasing, float width, float height) {
	AIArtSet artSet;
	sAIArtSet->NewArtSet(&artSet);
	sAIArtSet->AddArtToArtSet(artSet, art);
	AIArtHandle raster = ArtSet_rasterize(artSet, type, resolution, antialiasing, width, height);
	sAIArtSet->DisposeArtSet(&artSet);
	return raster;
}

AIArtHandle Art_getInsertionPoint(short *paintOrder, AIDocumentHandle doc) {
	// activate the focused document 
	Document_activate(doc);
	AIArtHandle art = NULL;
	*paintOrder = 0;
	ASBoolean editable = false;
	sAIArt->GetInsertionPoint(&art, paintOrder, &editable);
	if (!editable)
		throw new StringException("Cannot create art object. The active layer is not editable.");
	return art;
}

/*
 * Walks through the given dictionary and finds the key for art :)
 */
AIDictKey Art_getDictionaryKey(AIDictionaryRef dictionary, AIArtHandle art) {
	AIDictKey foundKey = NULL;
	AIDictionaryIterator iterator;
	if (!sAIDictionary->Begin(dictionary, &iterator)) {
		while (!sAIDictionaryIterator->AtEnd(iterator)) {
			AIDictKey key = sAIDictionaryIterator->GetKey(iterator);
			AIArtHandle curArt;
			if (!sAIDictionary->GetArtEntry(dictionary, key, &curArt) && art == curArt) {
				foundKey = key;
				break;
			}
			sAIDictionaryIterator->Next(iterator);
		}
		sAIDictionaryIterator->Release(iterator);
	}
	return foundKey;
}

AIArtHandle Art_copyTo(AIArtHandle artSrc, AIDocumentHandle docSrc, AIDictionaryRef dictSrc, AIArtHandle artDst, AIDocumentHandle docDst, short paintOrder) {
	AIArtHandle res = NULL;
	Document_activate(docDst);
	if (docSrc != docDst) {
		AIDictionaryRef dictDocDst;
		if (!sAIDocument->GetDictionary(&dictDocDst)) {
			Document_activate(docSrc);
			AIDictKey key = sAIDictionary->Key("-scriptographer-append-child");
			if (!sAIDictionary->CopyArtToEntry(dictDocDst, key, artSrc)) {
				Document_activate(docDst);
				sAIDictionary->MoveEntryToArt(dictDocDst, key, paintOrder, artDst, &res);
			}
			sAIDictionary->DeleteEntry(dictDocDst, key);
			sAIDictionary->Release(dictDocDst);
		}
	} else {
		if (dictSrc != NULL) {
			AIDictKey key = Art_getDictionaryKey(dictSrc, artSrc);
			if (key != NULL)
				sAIDictionary->CopyEntryToArt(dictSrc, key, paintOrder, artDst, &res);
		}

		if (res == NULL)
			sAIArt->DuplicateArt(artSrc, paintOrder, artDst, &res);
	}
	return res;
}

bool Art_move(JNIEnv *env, jobject obj, jobject art, short paintOrder) {
	try {
		if (art != NULL) {
			AIDocumentHandle docSrc, docDst;
			AIArtHandle artSrc = gEngine->getArtHandle(env, obj, true, &docSrc);
			AIArtHandle artDst = gEngine->getArtHandle(env, art, false, &docDst);
			if (artSrc != NULL && artDst != NULL && artSrc != artDst) {
				// simply try to reorder it
				if (artSrc != NULL && artDst != NULL) {
					// if art belongs to a dictionary, treat it differently
					AIDictionaryRef dictSrc = gEngine->getArtDictionaryHandle(env, obj);
					if (dictSrc != NULL) {
						AIDictKey key = Art_getDictionaryKey(dictSrc, artSrc);
						if (key != NULL) {
							if (!sAIDictionary->MoveEntryToArt(dictSrc, key, paintOrder, artDst, &artSrc)) {
								gEngine->changeArtHandle(env, obj, artSrc, NULL);
								return true;
							}
						}
					}
					
					// if we're in a different document:
					// move the art from one document to the other by moving it to
					// the the doc's dictionary first, then into the doc from there
					// this is the only way that seems to work...
					if (docSrc != docDst) {
						AIArtHandle res = Art_copyTo(artSrc, docSrc, NULL, artDst, docDst, paintOrder);
						if (res != NULL) {
							gEngine->changeArtHandle(env, obj, res, NULL, docDst);
							// now remove the original object in docDst. Moving does not work directly
							// so this seems to be the most elegant way of handling this
							Document_activate(docSrc);
							sAIArt->DisposeArt(artSrc);
							return true;
						}
					}
					// simply reorder
					if (!sAIArt->ReorderArt(artSrc, paintOrder, artDst))
						return true;
				}
			}
		}
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * int nativeCreate(short type)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Art_nativeCreate(JNIEnv *env, jclass cls, jshort type) {
	AIArtHandle art = NULL;
	try {
		// if type is set to the self defined TYPE_LAYER, create a layer and return the wrapped art group object instead:
		if (type == com_scriptographer_ai_Art_TYPE_LAYER) { // create a layer
			Document_activate();
			// place it above the active layer, or all others if none is active:
			AILayerHandle currentLayer = NULL;
			sAILayer->GetCurrentLayer(&currentLayer);
			AILayerHandle layer = NULL;
			sAILayer->InsertLayer(currentLayer, currentLayer != NULL ? kPlaceAbove : kPlaceAboveAll, &layer);
			if (layer != NULL)
				sAIArt->GetFirstArtOfLayer(layer, &art);
			if (art == NULL)
				throw new StringException("Cannot create layer. Please make sure there is an open document.");
		} else { // create a normal art object
			short paintOrder;
			AIArtHandle artInsert = Art_getInsertionPoint(&paintOrder);
			// try to create in the active layer
			sAIArt->NewArt(type, paintOrder, artInsert, &art);
			if (art == NULL)
				throw new StringException("Cannot create art object. Please make sure there is an open document.");
		}
	} EXCEPTION_CONVERT(env);
	return (jint) art;
}

/*
 * void finalize()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_finalize(JNIEnv *env, jobject obj) {
	try {
		AIDictionaryRef dictionary = gEngine->getArtDictionaryHandle(env, obj);
		if (dictionary != NULL) {
			sAIDictionary->Release(dictionary);
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean nativeRemove(int handle, int docHandle, jint dictionaryRef)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_nativeRemove(JNIEnv *env, jobject obj, jint handle, jint docHandle, jint dictionaryRef) {
	try {
		Document_activate((AIDocumentHandle) docHandle);
		AIArtHandle art = (AIArtHandle) handle;
		// treat the object differently if it's in a dictionary than in the
		// normal artwork tree of the document:
		AIDictionaryRef dictionary = (AIDictionaryRef) dictionaryRef;
		if (dictionary != NULL) {
			AIDictKey key = Art_getDictionaryKey(dictionary, art);
			if (key != NULL) {
				sAIDictionary->DeleteEntry(dictionary, key);
				return true;
			}
		}
		if (Art_isLayer(art)) {
			AILayerHandle layer;
			sAIArt->GetLayerOfArt(art, &layer);
			if (!sAILayer->DeleteLayer(layer))
				return true;
		} else {
			if (!sAIArt->DisposeArt(art))
				return true;
		}
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * com.scriptographer.ai.Art copyTo(com.scriptographer.ai.Document document)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Art_copyTo__Lcom_scriptographer_ai_Document_2(JNIEnv *env, jobject obj, jobject document) {
	try {
		// src
		AIDocumentHandle docSrc = NULL;
	    AIArtHandle artSrc = gEngine->getArtHandle(env, obj, false, &docSrc);
		AIDictionaryRef dictSrc = gEngine->getArtDictionaryHandle(env, obj);
		// dst: from insertion point
		AIDocumentHandle docDst = gEngine->getDocumentHandle(env, document);
		Document_activate(docDst);
		short paintOrder;
		AIArtHandle artDst = Art_getInsertionPoint(&paintOrder);
		// copy
		AIArtHandle res = Art_copyTo(artSrc, docSrc, dictSrc, artDst, docDst, paintOrder);
		if (res != NULL)
			return gEngine->wrapArtHandle(env, res);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Art copyTo(com.scriptographer.ai.Art art)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Art_copyTo__Lcom_scriptographer_ai_Art_2(JNIEnv *env, jobject obj, jobject art) {
	try {
		// src & dst
		AIDocumentHandle docSrc, docDst;
	    AIArtHandle artSrc = gEngine->getArtHandle(env, obj, false, &docSrc);
	    AIArtHandle artDst = gEngine->getArtHandle(env, art, false, &docDst);
		AIDictionaryRef dictSrc = gEngine->getArtDictionaryHandle(env, obj);
		// copy
		AIArtHandle res = Art_copyTo(artSrc, docSrc, dictSrc, artDst, docDst, kPlaceInsideOnTop);
		if (res != NULL)
			return gEngine->wrapArtHandle(env, res);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Art getFirstChild()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Art_getFirstChild(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		if (Art_hasChildren(art)) {
			AIArtHandle child = NULL;
			sAIArt->GetArtFirstChild(art, &child);
			if (child != NULL) {
				return gEngine->wrapArtHandle(env, child);
			}
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Art getLastChild()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Art_getLastChild(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		if (Art_hasChildren(art)) {
			AIArtHandle child = NULL;
#if kPluginInterfaceVersion >= kAI11		
			sAIArt->GetArtLastChild(art, &child);
#else
			// there's no other way to do this on < AI 11
			AIArtHandle curChild = NULL;
			sAIArt->GetArtFirstChild(art, &curChild);
			if (curChild != NULL) {
				do {
					child = curChild;
					sAIArt->GetArtSibling(child, &curChild);
				} while (curChild != NULL);
			}
#endif
			if (child != NULL) {
				return gEngine->wrapArtHandle(env, child);
			}
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Art getNextSibling()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Art_getNextSibling(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIArtHandle child = NULL;
		sAIArt->GetArtSibling(art, &child);
		if (child != NULL) {
			return gEngine->wrapArtHandle(env, child);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Art getPreviousSibling()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Art_getPreviousSibling(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIArtHandle child = NULL;
		sAIArt->GetArtPriorSibling(art, &child);
		if (child != NULL) {
			return gEngine->wrapArtHandle(env, child);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Art getParent()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Art_getParent(JNIEnv *env, jobject obj) {
	jobject res = NULL;
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIArtHandle parent = NULL;
		sAIArt->GetArtParent(art, &parent);
		if (parent != NULL)
			res = gEngine->wrapArtHandle(env, parent);
	} EXCEPTION_CONVERT(env);
	return res;
}

/*
 * com.scriptographer.ai.Rectangle getBounds()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Art_getBounds(JNIEnv *env, jobject obj) {
	try {
		AIRealRect rt;
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
	    sAIArt->GetArtBounds(art, &rt);
	    return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Rectangle getControlBounds()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Art_getControlBounds(JNIEnv *env, jobject obj) {
	try {
		AIRealRect rt;
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
	    sAIArt->GetArtTransformBounds(art, NULL, kControlBounds | kExcludeGuideBounds, &rt);
	    return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Rectangle getGeometricBounds()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Art_getGeometricBounds(JNIEnv *env, jobject obj) {
	try {
		AIRealRect rt;
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
	    sAIArt->GetArtTransformBounds(art, NULL, kVisibleBounds | kNoExtendedBounds | kExcludeGuideBounds, &rt);
	    return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * boolean isCenterVisible()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_isCenterVisible(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIBoolean visible;
		if (!sAIArt->GetArtCenterPointVisible(art, &visible))
			return visible;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setCenterVisible(boolean visible)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_setCenterVisible(JNIEnv *env, jobject obj, jboolean visible) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		sAIArt->SetArtCenterPointVisible(art, visible);
	} EXCEPTION_CONVERT(env);
}

/*
 * void setAttribute(long attribute, boolean value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_setAttribute(JNIEnv *env, jobject obj, jint attribute, jboolean value) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		sAIArt->SetArtUserAttr(art, attribute, value ? attribute : 0);
    } EXCEPTION_CONVERT(env);
}

/*
 * boolean getAttribute(long attribute)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_getAttribute(JNIEnv *env, jobject obj, jint attribute) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		long values;
		sAIArt->GetArtUserAttr(art, attribute, &values);
		return values & attribute;
    } EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean isEditable()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_isEditable(JNIEnv *env, jobject obj) {
	ASBoolean res = false;
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		long values;
		// First check that the art is not hidden or locked
		if (!sAIArt->GetArtUserAttr(art, kArtHidden | kArtLocked, &values) &&
			!values) {
			AIArtHandle prevArt;
			short order;
			ASBoolean editable;
			// Use Get / SetInsertionPoint to find out if object's parent is editable or not
			if (!sAIArt->GetInsertionPoint(&prevArt, &order, &editable)) {
				AIArtHandle checkArt;
				if (!sAIArt->GetArtParent(art, &checkArt)) {
					// Layers do not have parents
					if (checkArt == NULL)
						checkArt = art;
					AIArtHandle curArt;
					if (!sAIArt->SetInsertionPoint(checkArt) &&
						!sAIArt->GetInsertionPoint(&curArt, &order, &editable)) {
						res = editable;
					}
					// Set old insertion point again
					sAIArt->SetInsertionPoint(prevArt);
				}
			}
		}
	} EXCEPTION_CONVERT(env);
	return res;
}

/*
 * int getBlendMode()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Art_getBlendMode(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		return sAIBlendStyle->GetBlendingMode(art);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void setBlendMode(int mode)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_setBlendMode(JNIEnv *env, jobject obj, jint mode) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		sAIBlendStyle->SetBlendingMode(art, (AIBlendingMode) mode);
	} EXCEPTION_CONVERT(env);
}

/*
 * float getOpacity()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_Art_getOpacity(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		return sAIBlendStyle->GetOpacity(art);
	} EXCEPTION_CONVERT(env);
	return 0.0;
}

/*
 * void setOpacity(float opacity)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_setOpacity(JNIEnv *env, jobject obj, jfloat opacity) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		sAIBlendStyle->SetOpacity(art, opacity);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean getIsolated()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_getIsolated(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		return sAIBlendStyle->GetIsolated(art);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setIsolated(boolean isolated)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_setIsolated(JNIEnv *env, jobject obj, jboolean isolated) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		sAIBlendStyle->SetIsolated(art, isolated);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean getKnockout()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_getKnockout(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		return sAIBlendStyle->GetKnockout(art);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setKnockout(int knockout)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_setKnockout(JNIEnv *env, jobject obj, jint knockout) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		sAIBlendStyle->SetKnockout(art, (AIKnockout) knockout);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean getKnockoutInherited()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_getKnockoutInherited(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		return sAIBlendStyle->GetInheritedKnockout(art);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean getAlphaIsShape()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_getAlphaIsShape(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		return sAIBlendStyle->GetAlphaIsShape(art);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setAlphaIsShape(boolean isAlpha)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_setAlphaIsShape(JNIEnv *env, jobject obj, jboolean isAlpha) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		sAIBlendStyle->SetAlphaIsShape(art, isAlpha);
	} EXCEPTION_CONVERT(env);
}

/*
 * java.lang.String getName()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_Art_getName(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
#if kPluginInterfaceVersion < kAI12
		char name[1024];
		if (!sAIArt->GetArtName(art, name, 1024, NULL)) {
#else
		ai::UnicodeString name;
		if (!sAIArt->GetArtName(art, name, NULL)) {
#endif
			return gEngine->convertString(env, name);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setName(java.lang.String name)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_setName(JNIEnv *env, jobject obj, jstring name) {
	try {
		// intersting: setting name does not need document to be active
		AIArtHandle art = gEngine->getArtHandle(env, obj);
#if kPluginInterfaceVersion < kAI12
		char *str = gEngine->convertString(env, name);
		sAIArt->SetArtName(art, str);
		delete str;
#else
		ai::UnicodeString str = gEngine->convertString_UnicodeString(env, name);
		sAIArt->SetArtName(art, str);
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isDefaultName()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_isDefaultName(JNIEnv *env, jobject obj) {
	ASBoolean isDefaultName = true;
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
#if kPluginInterfaceVersion < kAI12
		// at least one byte for the name needs to be specified, otherwise this
		// doesn't work:
		char name;
		sAIArt->GetArtName(art, &name, 1, &isDefaultName);
#else
		ai::UnicodeString name;
		sAIArt->GetArtName(art, name, &isDefaultName);
#endif
	} EXCEPTION_CONVERT(env);
	return isDefaultName;
}

/*
 * boolean appendChild(com.scriptographer.ai.Art art)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_appendChild(JNIEnv *env, jobject obj, jobject art) {
	return Art_move(env, art, obj, kPlaceInsideOnTop);
}

/*
 * boolean moveAbove(com.scriptographer.ai.Art art)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_moveAbove(JNIEnv *env, jobject obj, jobject art) {
	return Art_move(env, obj, art, kPlaceAbove);
}

/*
 * boolean moveBelow(com.scriptographer.ai.Art art)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_moveBelow(JNIEnv *env, jobject obj, jobject art) {
	return Art_move(env, obj, art, kPlaceBelow);
}

// if 'deep' is set, artTransform traverses the children recursively and transforms them: 
void artTransform(JNIEnv *env, AIArtHandle art, AIRealMatrix *matrix, AIReal lineScale, long flags) {
	jobject obj = gEngine->getIfWrapped(env, art);
	if (obj != NULL) {
		// commit it first:
		gEngine->callStaticObjectMethod(env, gEngine->cls_CommitManager, gEngine->mid_CommitManager_commit, obj);
	}
	sAITransformArt->TransformArt(art, matrix, lineScale, flags);
	// TODO: add all art objects that need invalidate to be called after transform!
	// short type = Art_getType(art);
	// if (type == kPathArt)
	if (obj != NULL) {
		// only call this if it's wrapped!
		// increasing version by one causes refetching of cached data:
		gEngine->setIntField(env, obj, gEngine->fid_ai_Art_version, gEngine->getIntField(env, obj, gEngine->fid_ai_Art_version) + 1);
	}

	if (flags & com_scriptographer_ai_Art_TRANSFORM_DEEP) {
		AIArtHandle child;
		sAIArt->GetArtFirstChild(art, &child);
		while (child != NULL) {
			artTransform(env, child, matrix, lineScale, flags);
			sAIArt->GetArtSibling(child, &child);
		}
	}
}

/*
 * void transform(com.scriptographer.ai.Matrix matrix, int scaleFlags)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_transform(JNIEnv *env, jobject obj, jobject matrix, jint flags) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		AIRealMatrix mx;
		gEngine->convertMatrix(env, matrix, &mx);
		// according to adobe sdk manual: linescale = sqrt(scaleX) * sqrt(scaleY)
		AIReal sx, sy;
		sAIRealMath->AIRealMatrixGetScale(&mx, &sx, &sy);
		AIReal lineScale = sAIRealMath->AIRealSqrt(sx) * sAIRealMath->AIRealSqrt(sy);
		artTransform(env, art, &mx, lineScale, flags);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.Raster rasterize(int type, float resolution, int antialiasing, float width, float height)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Art_rasterize(JNIEnv *env, jobject obj, jint type, jfloat resolution, jint antialiasing, jfloat width, jfloat height) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		AIArtHandle raster = Art_rasterize(art, (AIRasterizeType) type, resolution, antialiasing, width, height);
		if (raster != NULL)
			return gEngine->wrapArtHandle(env, raster);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Art expand(int flags, int steps)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Art_expand(JNIEnv *env, jobject obj, jint flags, jint steps) {
	try {
		// commit pending changes first, before native expand is called!
		gEngine->callStaticObjectMethod(env, gEngine->cls_CommitManager, gEngine->mid_CommitManager_commit, obj);
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		// store old selected items:
		AIArtHandle **selected = NULL;
		long numSelected = 0;
		sAIMatchingArt->GetSelectedArt(&selected, &numSelected);
		Document_deselectAll();
		// now selected the art to be expanded, so the result is selected too:
		sAIArt->SetArtUserAttr(art, kArtSelected, kArtSelected);
		// expand
		sAIExpand->Expand(art, flags, steps);
		// now get the new selection, to find the result:
		AIArtSet result = NULL;
		AIArtHandle res = NULL;
		if (!sAIArtSet->NewArtSet(&result) &&
			!sAIArtSet->SelectedArtSet(result)) {
			sAIArtSet->IndexArtSet(result, 0, &res);
			sAIArtSet->DisposeArtSet(&result);
			sAIArt->SetArtUserAttr(res, kArtSelected, 0);
		}
		// deselect again
		Document_deselectAll(true);
		// select the previously selected objects:
		for (long i = 0; i < numSelected; i++)
			sAIArt->SetArtUserAttr((*selected)[i], kArtSelected, kArtSelected);
		sAIMDMemory->MdMemoryDisposeHandle((void **) selected);
		return gEngine->wrapArtHandle(env, res);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * int getOrder(com.scriptographer.ai.Art art)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Art_getOrder(JNIEnv *env, jobject obj, jobject art) {
	try {
		// no need to activate docs for only retrieving information!
		AIArtHandle art1 = gEngine->getArtHandle(env, obj);
		AIArtHandle art2 = gEngine->getArtHandle(env, art);
		short order;
		sAIArt->GetArtOrder(art1, art2, &order);
		return order;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void nativeGetDictionary(com.scriptographer.ai.Dictionary map)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_nativeGetDictionary(JNIEnv *env, jobject obj, jobject map) {
	AIDictionaryRef dictionary = NULL;
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		sAIArt->GetDictionary(art, &dictionary);
		if (dictionary != NULL)
			gEngine->convertDictionary(env, dictionary, map, false, true); 
	} EXCEPTION_CONVERT(env);
	if (dictionary != NULL)
		sAIDictionary->Release(dictionary);
}

/*
 * void nativeSetDictionary(com.scriptographer.ai.Dictionary map)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_nativeSetDictionary(JNIEnv *env, jobject obj, jobject map) {
	AIDictionaryRef dictionary = NULL;
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		sAIArt->GetDictionary(art, &dictionary);
		if (dictionary != NULL)
			gEngine->convertDictionary(env, map, dictionary, false, true); 
	} EXCEPTION_CONVERT(env);
	if (dictionary != NULL)
		sAIDictionary->Release(dictionary);
}

/*
 * boolean isValid()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_isValid(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		return Art_isValid(art);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void activate()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_activate(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		sAIArt->SetInsertionPoint(art);
	} EXCEPTION_CONVERT(env);
}
