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
#include "ScriptographerEngine.h"
#include "ScriptographerPlugin.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Item.h"

/*
 * com.scriptographer.ai.Item
 */

short Item_getType(AIArtHandle art) {
	short type = -1;
	sAIArt->GetArtType(art, &type);
	return type;
}

short Item_getType(JNIEnv *env, jclass cls) {
	if (env->IsSameObject(cls, gEngine->cls_ai_Item)) {
		return kAnyArt;
	} else if (env->IsSameObject(cls, gEngine->cls_ai_Path)) {
		return kPathArt;
	} else if (env->IsSameObject(cls, gEngine->cls_ai_CompoundPath)) {
		return kCompoundPathArt;
	} else if (env->IsSameObject(cls, gEngine->cls_ai_Group)) {
		return kGroupArt;
	} else if (env->IsSameObject(cls, gEngine->cls_ai_Raster)) {
		return kRasterArt;
	} else if (env->IsSameObject(cls, gEngine->cls_ai_PlacedFile)) {
		return kPlacedArt;
	} else if (env->IsSameObject(cls, gEngine->cls_ai_PlacedSymbol)) {
		return kSymbolArt;
	} else if (env->IsAssignableFrom(cls, gEngine->cls_ai_TextItem)) {
		return kTextFrameArt;
	} else if (env->IsSameObject(cls, gEngine->cls_ai_Tracing)) {
		// special defined type for tracings, needs handling!
		return com_scriptographer_ai_Item_TYPE_TRACING;
	} else if (env->IsSameObject(cls, gEngine->cls_ai_Layer)) {
		// special defined type for layers, needs handling!
		return com_scriptographer_ai_Item_TYPE_LAYER;
	} 
	return kUnknownArt;
	// TODO: make sure the above list contains all Item classes!
}

jboolean Item_isLayer(AIArtHandle art) {
	ASBoolean isLayerGroup = false;
	sAIArt->IsArtLayerGroup(art, &isLayerGroup);
	return isLayerGroup;
}

void Item_filter(AIArtSet set, bool layerOnly) {
	// takes out all kUnknownAr items and layergroups
	long count = 0;
	sAIArtSet->CountArtSet(set, &count);
	for (long i = count - 1; i >= 0; i--) {
		AIArtHandle art = NULL;
		if (!sAIArtSet->IndexArtSet(set, i, &art)) {
			short type = Item_getType(art);
			bool isLayer = Item_isLayer(art);
			if (type == kUnknownArt ||
					(layerOnly && !isLayer || !layerOnly && isLayer)) {
				sAIArtSet->RemoveArtFromArtSet(set, art);
			}
		}
	}
}

AIArtSet Item_getSelected(bool filter) {
	AIArtSet set = NULL;
	if (!sAIArtSet->NewArtSet(&set)) {
		if (!sAIArtSet->SelectedArtSet(set)) {
			if (filter) {
				// Now filter out objects of which the parents are selected too
				long count;
				sAIArtSet->CountArtSet(set, &count);
				for (long i = count - 1; i >= 0; i--) {
					AIArtHandle art;
					if (!sAIArtSet->IndexArtSet(set, i, &art)) {
						long values;
						if (!sAIArt->GetArtUserAttr(art, kArtFullySelected, &values)
							&& !(values & kArtFullySelected)) {
							sAIArtSet->RemoveArtFromArtSet(set, art);
						} else {
							AIArtHandle parent = NULL;
							sAIArt->GetArtParent(art, &parent);
							if (parent != NULL && !Item_isLayer(parent)) {
								if (!sAIArt->GetArtUserAttr(parent, kArtFullySelected, &values)
									&& (values & kArtFullySelected))
									sAIArtSet->RemoveArtFromArtSet(set, art);
							}
						}
					}
				}
			}
		}
	}
	return set;
}

void Item_setSelected(AIArtHandle art, bool children) {
	sAIArt->SetArtUserAttr(art, kArtSelected, kArtSelected);
	if (children) {
		AIArtHandle child = NULL;
		sAIArt->GetArtFirstChild(art, &child);
		while (child != NULL) {
			Item_setSelected(child, true);
			// Catch errors
			if (sAIArt->GetArtSibling(child, &child))
				child = NULL;
		}
	}
}

void Item_setSelected(AIArtSet set) {
	long count;
	if (set != NULL) {
		sAIArtSet->CountArtSet(set, &count);
		AIArtHandle art = NULL;
		for (long i = 0; i < count; i++) {
			if (!sAIArtSet->IndexArtSet(set, i, &art))
				sAIArt->SetArtUserAttr(art, kArtSelected, kArtSelected);
		}
	}
}

void Item_deselectAll() {
	// sAIMatchingArt->DeselectAll(); is not used for multiple reasons:
	// In some cases (e.g. after Pathfinder / expand) it does not seem to do the 
	// trick. Also, calling it seems to cause a screen refresh, something we 
	// don't want to happen in Sg.
	AIArtHandle **matches;
	long numMatches;
	if (!sAIMatchingArt->GetSelectedArt(&matches, &numMatches)) {
		for (int i = 0; i < numMatches; i++)
			sAIArt->SetArtUserAttr((*matches)[i], kArtSelected, 0);
		sAIMDMemory->MdMemoryDisposeHandle((void **) matches);
	}
}

void Item_activateDocument(JNIEnv *env, AIArtSet set) {
	long count = 0;
	// Walk through the items to find the first wrapped one and get the document handle from there.
	sAIArtSet->CountArtSet(set, &count);
	for (long i = 0; i < count; i++) {
		AIArtHandle art = NULL;
		if (!sAIArtSet->IndexArtSet(set, 0, &art)) {
			jobject obj = gEngine->callStaticObjectMethod(env,
					gEngine->cls_ai_Item, gEngine->mid_ai_Item_getIfWrapped,
					(jint) art);
			if (obj != NULL) {
				gEngine->getDocumentHandle(env, obj, true);
				// Break since we're done.
				break;
			}
		}
	}
}

AIArtHandle Item_rasterize(AIArtSet set, AIRasterizeType type, float resolution, int antialiasing, float width, float height) {
	AIRasterizeSettings settings;
	if (type == -1) {
		// deterimine from document color model:
		short colorModel;
		sAIDocument->GetDocumentColorModel(&colorModel);
		switch (colorModel) {
			case kDocGrayColor:
				type = kRasterizeAGrayscale;
				break;
			case kDocRGBColor:
				type = kRasterizeARGB;
				break;
			case kDocCMYKColor:
				type = kRasterizeACMYK;
				break;
		}
	}
	settings.type = type;
	settings.resolution = resolution;
	settings.antialiasing = antialiasing;
	// TODO: Support options
	settings.options = kRasterizeOptionsNone;
	AIRealRect artBounds;
	sAIRasterize->ComputeArtBounds(set, &artBounds, false);
	artBounds.left = floor(artBounds.left);
	artBounds.bottom = floor(artBounds.bottom);
	if (width >= 0)
		artBounds.right = artBounds.left + width;
	if (height >= 0)
		artBounds.top = artBounds.bottom + height;
	artBounds.right = ceil(artBounds.right);
	artBounds.top = ceil(artBounds.top);
	AIArtHandle raster = NULL;
	// walk through the set and find the art that is blaced above all others:
	AIArtHandle top = NULL;
	long count;
	sAIArtSet->CountArtSet(set, &count);
	for (long i = count - 1; i >= 0; i--) {
		AIArtHandle art;
		if (!sAIArtSet->IndexArtSet(set, i, &art)) {
			if (top == NULL) {
				top = art;
			} else {
				short order;
				sAIArt->GetArtOrder(art, top, &order);
				if (order == kFirstBeforeSecond || order == kSecondInsideFirst)
					top = art;
			}
		}
	}
	sAIRasterize->Rasterize(set, &settings, &artBounds, kPlaceAbove, top, &raster, NULL);
	return raster;
}

AIArtHandle Item_rasterize(AIArtHandle art, AIRasterizeType type, float resolution, int antialiasing, float width, float height) {
	AIArtSet set;
	sAIArtSet->NewArtSet(&set);
	sAIArtSet->AddArtToArtSet(set, art);
	AIArtHandle raster = Item_rasterize(set, type, resolution, antialiasing, width, height);
	sAIArtSet->DisposeArtSet(&set);
	return raster;
}

AIArtHandle Item_getInsertionPoint(short *paintOrder, AIDocumentHandle doc) {
	// activate the focused document 
	Document_activate(doc);
	AIArtHandle art = NULL;
	*paintOrder = 0;
	ASBoolean editable = false;
	sAIArt->GetInsertionPoint(&art, paintOrder, &editable);
	if (!editable)
		throw new StringException("Unable to create item. The active layer is not editable.");
	return art;
}

/*
 * Commits and invalidates wrapped art objects.
 * if 'children' is set, it traverses the children recursively and commits and
 * invalidates them too.
 */
void Item_commit(JNIEnv *env, AIArtHandle art, bool invalidate, bool children) {
	// Only commit if it's wrapped
	gEngine->callStaticVoidMethod(env, gEngine->cls_ai_Item,
			gEngine->mid_ai_Item_commitIfWrapped, (jint) art, invalidate);
	if (children) {
		AIArtHandle child = NULL;
		sAIArt->GetArtFirstChild(art, &child);
		while (child != NULL) {
			Item_commit(env, child, invalidate, true);
			// Catch errors
			if (sAIArt->GetArtSibling(child, &child))
				child = NULL;
		}
	}
}

void Item_clearArtHandles(AIArtHandle art) {
	AIDictionaryRef dict;
	if (!sAIArt->GetDictionary(art, &dict)) {
		sAIDictionary->DeleteEntry(dict, gEngine->m_artHandleKey);
		sAIDictionary->Release(dict);
	}
	// Clear children as well:
	AIArtHandle child = NULL;
	sAIArt->GetArtFirstChild(art, &child);
	if (child != NULL) {
		do {
			Item_clearArtHandles(child);
			if (sAIArt->GetArtSibling(child, &child))
				child = NULL;
		} while (child != NULL);
	}
}

AIArtHandle Item_copyTo(JNIEnv *env, jobject src, AIArtHandle artDst, AIDocumentHandle docDst, short paintOrder, bool commitFirst = true) {
	// src
	AIDocumentHandle docSrc = NULL;
	AIArtHandle artSrc = gEngine->getArtHandle(env, src, false, &docSrc);
	AIDictionaryRef dictSrc = gEngine->getArtDictionaryHandle(env, src);
	AIArtHandle res = NULL;
	AIRealMatrix matrix;
	bool transform = false;
	// Determine the shift in coordinate space between the two documents
	// by hardening and softening an identity transform, then reversing
	// the transform. This transformation then is applied to the resulting
	// item further down.
	if (docSrc != docDst) {
		Document_activate(docSrc);
		sAIRealMath->AIRealMatrixSetIdentity(&matrix);
		sAIHardSoft->AIRealMatrixHarden(&matrix);
		Document_activate(docDst);
		sAIHardSoft->AIRealMatrixSoften(&matrix);
		matrix.tx = -matrix.tx;
		matrix.ty = -matrix.ty;
		transform = true;
	} else {
		Document_activate(docDst);
	}
	// commit
	if (commitFirst)
		Item_commit(env, artSrc);
	if (dictSrc != NULL) {
		AIDictKey key = gEngine->getArtDictionaryKey(env, src);
		if (key != NULL)
			sAIDictionary->CopyEntryToArt(dictSrc, key, paintOrder, artDst, &res);
	}
	if (res == NULL)
		sAIArt->DuplicateArt(artSrc, paintOrder, artDst, &res);
	if (res != NULL) {
		// TODO: Find out which additional options should be passed here:
		if (transform) 
			sAITransformArt->TransformArt(res, &matrix, 1, kTransformObjects | kTransformChildren);
		// Duplicate art also duplicated the dictionary. Remove the artHandleKey from it, since it's
		// a new object that needs to be wrapped differently.
		// It appears that it can also happen that newly duplicated items receive dictionaries from already gone
		// items, e.g. after undo, at least in CS4. Clearing here gets rid of these problems...
		Item_clearArtHandles(res);
	}
	return res;
}

bool Item_move(JNIEnv *env, jobject obj, jobject item, short paintOrder) {
	try {
		if (item != NULL) {
			AIDocumentHandle docSrc, docDst;
			AIArtHandle artSrc = gEngine->getArtHandle(env, obj, true, &docSrc);
			AIArtHandle artDst = gEngine->getArtHandle(env, item, false, &docDst);
			// Commit source first
			Item_commit(env, artSrc);
			if (artSrc != NULL && artDst != NULL && artSrc != artDst) {
				// Simply try to reorder it
				if (artSrc != NULL && artDst != NULL) {
					// If art belongs to a dictionary, treat it differently
					AIDictionaryRef dictSrc = gEngine->getArtDictionaryHandle(env, obj);
					if (dictSrc != NULL) {
						AIDictKey key = gEngine->getArtDictionaryKey(env, obj);
						if (key != NULL) {
							if (!sAIDictionary->MoveEntryToArt(dictSrc, key, paintOrder, artDst, &artSrc)) {
								// changeArtHandle also erases dictionaryHandle and dictionaryKey in obj
								gEngine->changeArtHandle(env, obj, artSrc, NULL, true);
								return true;
							}
						}
					}
					// If we're in the same document, try to simply reorder
					// In some occasions this might fail, e.g. when working with life effects,
					// where group receiving the life effect result has the same document but
					// is not actually living within the document. In this case, use the same
					// strategy as for copying accross different document below. 
					if (docSrc == docDst && !sAIArt->ReorderArt(artSrc, paintOrder, artDst))
						return true;
					// We are copying accross documents, or into a live effect group.
					// move the art from one document to the other by copying it over using
					// copyTo, then changing the handle and removing the original, to mimick
					// reordering.
					// Pass false for commitFirst since it was already commited above.
					AIArtHandle res = Item_copyTo(env, obj, artDst, docDst, paintOrder, false);
					if (res != NULL) {
						gEngine->changeArtHandle(env, obj, res, docDst, true);
						// now remove the original object in docDst. Moving does not work directly
						// so this seems to be the most elegant way of handling this
						// TODO: Since Item_copyTo now seems to work with sAIArt->DuplicateArt for this,
						// check again if normal moving across documents might work.
						if (docDst != docSrc)
							Document_activate(docSrc);
						sAIArt->DisposeArt(artSrc);
						return true;
					}
				}
			}
		}
	} EXCEPTION_CONVERT(env);
	return false;
}

short Item_getOrder(JNIEnv *env, jobject obj1, jobject obj2) {
	// No need to activate docs for only retrieving information!
	AIArtHandle art1 = gEngine->getArtHandle(env, obj1);
	AIArtHandle art2 = gEngine->getArtHandle(env, obj2);
	short order;
	sAIArt->GetArtOrder(art1, art2, &order);
	return order;
}

/*
 * int nativeCreate(short type)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Item_nativeCreate(JNIEnv *env, jclass cls, jshort type) {
	AIArtHandle art = NULL;
	try {
		// if type is set to the self defined TYPE_LAYER, create a layer and return the wrapped art group object instead:
		if (type == com_scriptographer_ai_Item_TYPE_LAYER) { // Create a layer
			// Make sure we're switching to the right doc (gCreationDoc)
			Document_activate();
			// place it above the active layer, or all others if none is active:
			AILayerHandle currentLayer = NULL;
			sAILayer->GetCurrentLayer(&currentLayer);
			AILayerHandle layer = NULL;
			sAILayer->InsertLayer(currentLayer, currentLayer != NULL ? kPlaceAbove : kPlaceAboveAll, &layer);
			if (layer != NULL)
				sAIArt->GetFirstArtOfLayer(layer, &art);
			if (art == NULL)
				throw new StringException("Unable to create layer. Make sure there is an open document.");
		} else { // create a normal art object
			short paintOrder;
			AIArtHandle artInsert = Item_getInsertionPoint(&paintOrder);
			// try to create in the active layer
			sAIArt->NewArt(type, paintOrder, artInsert, &art);
			if (art == NULL)
				throw new StringException("Unable to create item. Make sure there is an open document.");
		}
		// Set the m_artHandleKey value on newly created items, so they are found in the wrappers list.
		AIDictionaryRef dict;
		if (!sAIArt->GetDictionary(art, &dict)) {
			sAIDictionary->SetIntegerEntry(dict, gEngine->m_artHandleKey, (ASInt32) art);
			sAIDictionary->Release(dict);
		}
	} EXCEPTION_CONVERT(env);
	return (jint) art;
}

/*
 * void finalize()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Item_finalize(JNIEnv *env, jobject obj) {
	try {
		AIDictionaryRef dictionary = gEngine->getArtDictionaryHandle(env, obj);
		if (dictionary != NULL) {
			sAIDictionary->Release(dictionary);
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean nativeRemove(int handle, int docHandle, jint dictionaryHandle)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Item_nativeRemove(JNIEnv *env, jclass cls, jint handle, jint docHandle, jint dictionaryHandle) {
	try {
		Document_activate((AIDocumentHandle) docHandle);
		AIArtHandle art = (AIArtHandle) handle;
		// Sometimes removing non-valid items, e.g. after undo was executed, leads
		// to crashes that cannot be caught in a catch even (at least on Mac).
		// We need to check for them to be valid. The check is simply seeing if
		// GetArtUserAttr executes without problems...
		long values;
		if (!sAIArt->GetArtUserAttr(art, kArtLocked, &values)) { 
			// Treat the object differently if it's in a dictionary than in the
			// normal artwork tree of the document:
			AIDictionaryRef dictionary = (AIDictionaryRef) dictionaryHandle;
			if (dictionary != NULL) {
				AIDictKey key = NULL; // TODO: Item_getDictionaryKey(dictionary, art);
				if (key != NULL) {
					sAIDictionary->DeleteEntry(dictionary, key);
					return true;
				}
			}
			if (Item_isLayer(art)) {
				AILayerHandle layer;
				sAIArt->GetLayerOfArt(art, &layer);
				if (!sAILayer->DeleteLayer(layer))
					return true;
			} else {
				if (!sAIArt->DisposeArt(art))
					return true;
			}
		}
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean[] nativeCheckItems(int[] values, int length)
 */
JNIEXPORT jbooleanArray JNICALL Java_com_scriptographer_ai_Item_nativeCheckItems(JNIEnv *env, jclass cls, jintArray array, jint length) {
	try {
		// Get handles
		jint *values = new jint[length];
		env->GetIntArrayRegion(array, 0, length, values);
		// Collect valid values
		int validLength = length / 3;
		jboolean *valid = new jboolean[validLength];
		// In order to check dictionary art items, we need to see if their dictionary
		// still contains them. If the dicitionary is not valid any longer, or it does
		// not contain the item any longer, it is not regarded as valid.
		// 'values' contains tripples of handle / dict / key for each item to check.
		for (int i = 0, j = 0; i < validLength; i++) {
			AIArtHandle art = (AIArtHandle) values[j++];
			AIDictionaryRef dict = (AIDictionaryRef) values[j++];
			AIDictKey key = (AIDictKey) values[j++];
			if (dict != NULL) {
				AIArtHandle other;
				valid[i] = !sAIDictionary->GetArtEntry(dict, key, &other) && other == art;
			} else {
#if kPluginInterfaceVersion < kAI12
				valid[i] = sAIArt->ValidArt(art);
#else // kPluginInterfaceVersion >= kAI12
				valid[i] = sAIArt->ValidArt(art, false);
#endif // kPluginInterfaceVersion >= kAI12
			}
		}
		// Set the valid values and return
		jbooleanArray validArray = env->NewBooleanArray(validLength);
		env->SetBooleanArrayRegion(validArray, 0, validLength, valid);
		return validArray;
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Item copyTo(com.scriptographer.ai.Document document)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Item_copyTo__Lcom_scriptographer_ai_Document_2(JNIEnv *env, jobject obj, jobject document) {
	try {
		// dst: from insertion point
		AIDocumentHandle docDst = gEngine->getDocumentHandle(env, document);
		Document_activate(docDst);
		short paintOrder;
		AIArtHandle artDst = Item_getInsertionPoint(&paintOrder);
		// copy
		AIArtHandle copy = Item_copyTo(env, obj, artDst, docDst, paintOrder);
		if (copy != NULL)
			return gEngine->wrapArtHandle(env, copy, docDst, true);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Item copyTo(com.scriptographer.ai.Item item)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Item_copyTo__Lcom_scriptographer_ai_Item_2(JNIEnv *env, jobject obj, jobject item) {
	try {
		// src & dst
		AIDocumentHandle docDst;
		AIArtHandle artDst = gEngine->getArtHandle(env, item, false, &docDst);
		// copy
		AIArtHandle copy = Item_copyTo(env, obj, artDst, docDst, kPlaceInsideOnTop);
		if (copy != NULL)
			return gEngine->wrapArtHandle(env, copy, docDst, true);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Item getFirstChild()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Item_getFirstChild(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIArtHandle child = NULL;
		sAIArt->GetArtFirstChild(art, &child);
		if (child != NULL)
			return gEngine->wrapArtHandle(env, child, gEngine->getDocumentHandle(env, obj));
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Item getLastChild()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Item_getLastChild(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIArtHandle child = NULL;
		sAIArt->GetArtLastChild(art, &child);
		if (child != NULL)
			return gEngine->wrapArtHandle(env, child, gEngine->getDocumentHandle(env, obj));
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Item getNextSibling()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Item_getNextSibling(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIArtHandle child = NULL;
		sAIArt->GetArtSibling(art, &child);
		if (child != NULL)
			return gEngine->wrapArtHandle(env, child, gEngine->getDocumentHandle(env, obj));
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Item getPreviousSibling()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Item_getPreviousSibling(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIArtHandle child = NULL;
		sAIArt->GetArtPriorSibling(art, &child);
		if (child != NULL)
			return gEngine->wrapArtHandle(env, child, gEngine->getDocumentHandle(env, obj));
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Item getParent()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Item_getParent(JNIEnv *env, jobject obj) {
	jobject res = NULL;
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIArtHandle parent = NULL;
		sAIArt->GetArtParent(art, &parent);
		if (parent != NULL)
			res = gEngine->wrapArtHandle(env, parent, gEngine->getDocumentHandle(env, obj));
	} EXCEPTION_CONVERT(env);
	return res;
}

/*
 * com.scriptographer.ai.Layer getLayer()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Item_getLayer(JNIEnv *env, jobject obj) {
	jobject res = NULL;
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		// If this is already a layer, get the layer of it's parent, since getLayer would point
		// to itself otherwise (getLayer is supposed to return the layer the item is nested in).
		if (Item_isLayer(art))
			sAIArt->GetArtParent(art, &art);
		AILayerHandle layer = NULL;
		sAIArt->GetLayerOfArt(art, &layer);
		if (layer != NULL)
			res = gEngine->wrapLayerHandle(env, layer, gEngine->getDocumentHandle(env, obj));
	} EXCEPTION_CONVERT(env);
	return res;
}

/*
 * com.scriptographer.ai.Rectangle getBounds()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Item_getBounds(JNIEnv *env, jobject obj) {
	try {
		// TODO: Document activation needed?
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		// Commit pending changes first, since they might influence the bounds
		Item_commit(env, art);
		AIRealRect rt;
		sAIArt->GetArtTransformBounds(art, NULL, kVisibleBounds | kNoStrokeBounds | kNoExtendedBounds | kExcludeGuideBounds, &rt);
		return gEngine->convertRectangle(env, kArtboardCoordinates, &rt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Rectangle getStrokeBounds()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Item_getStrokeBounds(JNIEnv *env, jobject obj) {
	try {
		AIRealRect rt;
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		// Commit pending changes first, since they might influence the bounds
		Item_commit(env, art);
		sAIArt->GetArtTransformBounds(art, NULL, kVisibleBounds | kExcludeGuideBounds, &rt);
		return gEngine->convertRectangle(env, kArtboardCoordinates, &rt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Rectangle getControlBounds()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Item_getControlBounds(JNIEnv *env, jobject obj) {
	try {
		AIRealRect rt;
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		// Commit pending changes first, since they might influence the bounds
		Item_commit(env, art);
		sAIArt->GetArtTransformBounds(art, NULL, kVisibleBounds | kControlBounds | kExcludeGuideBounds, &rt);
		return gEngine->convertRectangle(env, kArtboardCoordinates, &rt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Point getPosition()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Item_getPosition(JNIEnv *env, jobject obj) {
	try {
		AIRealRect rt;
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		// Commit pending changes first, since they might influence the bounds
		Item_commit(env, art);
		// Return the center point of the bounds
		// TODO: maybe find a way to define and store reg points per art objects?
		sAIArt->GetArtTransformBounds(art, NULL, kVisibleBounds | kNoStrokeBounds | kNoExtendedBounds | kExcludeGuideBounds, &rt);
		DEFINE_POINT(pt, (rt.left + rt.right) / 2.0, (rt.top + rt.bottom) / 2.0);
		return gEngine->convertPoint(env, kArtboardCoordinates, &pt);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * boolean isCenterVisible()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Item_isCenterVisible(JNIEnv *env, jobject obj) {
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
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Item_setCenterVisible(JNIEnv *env, jobject obj, jboolean visible) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		sAIArt->SetArtCenterPointVisible(art, visible);
	} EXCEPTION_CONVERT(env);
}

/*
 * int nativeGetAttributes(int attributes)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Item_nativeGetAttributes(JNIEnv *env, jobject obj, jint attributes) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		long values;
		if (!sAIArt->GetArtUserAttr(art, attributes, &values))
			return values;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void nativeSetAttributes(int attributes, jint values)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Item_nativeSetAttributes(JNIEnv *env, jobject obj, jint attributes, jint values) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		sAIArt->SetArtUserAttr(art, attributes, values);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isEditable()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Item_isEditable(JNIEnv *env, jobject obj) {
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
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Item_nativeGetBlendMode(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		return sAIBlendStyle->GetBlendingMode(art);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void nativeSetBlendMode(int mode)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Item_nativeSetBlendMode(JNIEnv *env, jobject obj, jint mode) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		sAIBlendStyle->SetBlendingMode(art, (AIBlendingMode) mode);
	} EXCEPTION_CONVERT(env);
}

/*
 * float getOpacity()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_Item_getOpacity(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		return sAIBlendStyle->GetOpacity(art);
	} EXCEPTION_CONVERT(env);
	return 0.0;
}

/*
 * void setOpacity(float opacity)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Item_setOpacity(JNIEnv *env, jobject obj, jfloat opacity) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		sAIBlendStyle->SetOpacity(art, opacity);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean getIsolated()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Item_getIsolated(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		return sAIBlendStyle->GetIsolated(art);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setIsolated(boolean isolated)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Item_setIsolated(JNIEnv *env, jobject obj, jboolean isolated) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		sAIBlendStyle->SetIsolated(art, isolated);
	} EXCEPTION_CONVERT(env);
}

/*
 * jint nativeGetKnockout(boolean inherited)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Item_nativeGetKnockout(JNIEnv *env, jobject obj, jboolean inherited) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		return inherited ? sAIBlendStyle->GetInheritedKnockout(art) : sAIBlendStyle->GetKnockout(art);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void nativeSetKnockout(int knockout)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Item_nativeSetKnockout(JNIEnv *env, jobject obj, jint knockout) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		sAIBlendStyle->SetKnockout(art, (AIKnockout) knockout);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean getAlphaIsShape()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Item_getAlphaIsShape(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		return sAIBlendStyle->GetAlphaIsShape(art);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setAlphaIsShape(boolean isAlpha)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Item_setAlphaIsShape(JNIEnv *env, jobject obj, jboolean isAlpha) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		sAIBlendStyle->SetAlphaIsShape(art, isAlpha);
	} EXCEPTION_CONVERT(env);
}

/*
 * java.lang.String getName()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_Item_getName(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		// Handle pseudo HANDLE_CURRENT_STYLE item which returns NULL, but
		// getName will get called in toString().
		if (art != NULL) {
			ASBoolean isDefaultName = true;
#if kPluginInterfaceVersion < kAI12
			char name[1024];
			if (!sAIArt->GetArtName(art, name, 1024, &isDefaultName)) {
#else
			ai::UnicodeString name;
			if (!sAIArt->GetArtName(art, name, &isDefaultName)) {
#endif
				if (!isDefaultName)
					return gEngine->convertString(env, name);
			}
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setName(java.lang.String name)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Item_setName(JNIEnv *env, jobject obj, jstring name) {
	try {
		// Intersting: setting name does not need document to be active
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
 * boolean appendTop(com.scriptographer.ai.Item item)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Item_appendTop(JNIEnv *env, jobject obj, jobject item) {
	try {
		return Item_move(env, item, obj, kPlaceInsideOnTop);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean appendBottom(com.scriptographer.ai.Item item)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Item_appendBottom(JNIEnv *env, jobject obj, jobject item) {
	try {
		return Item_move(env, item, obj, kPlaceInsideOnBottom);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean isAbove(com.scriptographer.ai.Item item)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Item_isAbove(JNIEnv *env, jobject obj, jobject item) {
	try {
		return Item_getOrder(env, obj, item) == kFirstBeforeSecond;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean isBelow(com.scriptographer.ai.Item item)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Item_isBelow(JNIEnv *env, jobject obj, jobject item) {
	try {
		return Item_getOrder(env, obj, item) == kSecondBeforeFirst;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean isDescendant(com.scriptographer.ai.Item item)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Item_isDescendant(JNIEnv *env, jobject obj, jobject item) {
	try {
		return Item_getOrder(env, obj, item) == kFirstInsideSecond;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean isAncestor(com.scriptographer.ai.Item item)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Item_isAncestor(JNIEnv *env, jobject obj, jobject item) {
	try {
		return Item_getOrder(env, obj, item) == kSecondInsideFirst;
	} EXCEPTION_CONVERT(env);
	return false;
}
	
/*
 * boolean moveAbove(com.scriptographer.ai.Item art)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Item_moveAbove(JNIEnv *env, jobject obj, jobject item) {
	return Item_move(env, obj, item, kPlaceAbove);
}

/*
 * boolean moveBelow(com.scriptographer.ai.Item art)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Item_moveBelow(JNIEnv *env, jobject obj, jobject item) {
	return Item_move(env, obj, item, kPlaceBelow);
}

/*
 * void nativeTransform(com.scriptographer.ai.Matrix matrix, int scaleFlags)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Item_nativeTransform(JNIEnv *env, jobject obj, jobject matrix, jint flags) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		AIRealMatrix mx;
		gEngine->convertMatrix(env, kArtboardCoordinates, kArtboardCoordinates, matrix, &mx);
/*
		// Modify the matrix so that it 'acts' on the center of the selected object
		// TODO: Introduce reg points?
		AIRealRect bounds;
		sAIArt->GetArtTransformBounds(art, NULL, kVisibleBounds | kNoStrokeBounds | kNoExtendedBounds | kExcludeGuideBounds, &bounds);
		DEFINE_POINT(center,
			(bounds.left + bounds.right) / 2,
			(bounds.top + bounds.bottom) / 2);

		AIRealMatrix m;
		sAIRealMath->AIRealMatrixSetTranslate(&m, -center.h, -center.v);
		sAIRealMath->AIRealMatrixConcat(&m, &mx, &m);
		sAIRealMath->AIRealMatrixConcatTranslate(&m, center.h, center.v);
*/
		// According to adobe sdk manual: linescale = sqrt(scaleX) * sqrt(scaleY)
		AIReal sx, sy;
		sAIRealMath->AIRealMatrixGetScale(&mx, &sx, &sy);
		AIReal lineScale = sAIRealMath->AIRealSqrt(sx) * sAIRealMath->AIRealSqrt(sy);
		Item_commit(env, art, true, flags & kTransformChildren);
		sAITransformArt->TransformArt(art, &mx, lineScale, flags);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.Raster nativeRasterize(int type, float resolution, int antialiasing, float width, float height)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Item_nativeRasterize__IFIFF(JNIEnv *env, jobject obj, jint type, jfloat resolution, jint antialiasing, jfloat width, jfloat height) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		AIArtHandle raster = Item_rasterize(art, (AIRasterizeType) type, resolution, antialiasing, width, height);
		if (raster != NULL) {
			// No need to pass document since we're activating document in getArtHandle
			return gEngine->wrapArtHandle(env, raster, NULL, true);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Raster nativeRasterize(com.scriptographer.ai.Item[] items, int type, float resolution, int antialiasing, float width, float height)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Item_nativeRasterize___3Lcom_scriptographer_ai_Item_2IFIFF(JNIEnv *env, jclass cls, jobjectArray items, jint type, jfloat resolution, jint antialiasing, jfloat width, jfloat height) {
	try {
		AIArtSet set = gEngine->convertItemSet(env, items, true);
		AIArtHandle raster = Item_rasterize(set, (AIRasterizeType) type, resolution, antialiasing, width, height);
		if (raster != NULL) {
			// It's ok not to not pass document here, since the method calling nativeRasterize makes sure the right one is active
			return gEngine->wrapArtHandle(env, raster, NULL, true);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void nativeDraw(com.scriptographer.ui.Image image, int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Item_nativeDraw(JNIEnv *env, jobject obj, jobject imageObj, jint width, jint height) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);

		Item_commit(env, art);
		AIRealRect rt;
		sAIArt->GetArtTransformBounds(art, NULL, kVisibleBounds | kNoStrokeBounds | kNoExtendedBounds | kExcludeGuideBounds, &rt);

		ADMImageRef image = gEngine->getImageHandle(env, imageObj);
		ADMDrawerRef drawer = sADMImage->BeginADMDrawer(image);
		ADMAGMPortPtr port = sADMDrawer->GetAGMPort(drawer);
		
		AIDrawArtFlags flags = kAIDrawArtPreviewMask;
		AIDrawArtData drawData;
		drawData.version = kAIDrawArtVersion;
		drawData.flags = flags;
		drawData.type = kAIDrawArtAGMPortOutputV6;
		drawData.origin.h = rt.left;
		drawData.origin.v = rt.top;
		sAIRealMath->AIRealMatrixSetIdentity(&drawData.matrix);
		drawData.art = art;
		AIRealRect &clip = drawData.destClipRect;
		clip.left = clip.top = 0;
		clip.right = width;
		clip.bottom = height;
//		clip.right = rt.right - rt.left;
//		clip.bottom = rt.top - rt.bottom;
		drawData.eraseDestClipRect = true;
		drawData.interruptedArt = NULL;
		drawData.greekThreshold = -1;
		drawData.output.port.port = port;
		AIRect &rect = drawData.output.port.portBounds;
		rect.left = rect.top = 0;
		rect.right = width;
		rect.bottom = height;
#if kPluginInterfaceVersion >= kAI12
		// TODO: implement for CS
		AIColorConvertOptions drawOptions;
		// sAIDrawArt->BeginDrawArt(&drawData, drawOptions, flags); 
		sAIDrawArt->DrawArt(&drawData, drawOptions); 
		// sAIDrawArt->EndDrawArt(&drawData, drawOptions); 
		sADMImage->EndADMDrawer(image);
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.Item nativeExpand(int flags, int steps)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Item_nativeExpand(JNIEnv *env, jobject obj, jint flags, jint steps) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		// commit pending changes first, before native expand is called!
		Item_commit(env, art);
		// store old selected items:
		AIArtHandle **selected = NULL;
		long numSelected = 0;
		sAIMatchingArt->GetSelectedArt(&selected, &numSelected);
		Item_deselectAll();
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
		Item_deselectAll();
		// select the previously selected objects:
		for (long i = 0; i < numSelected; i++)
			sAIArt->SetArtUserAttr((*selected)[i], kArtSelected, kArtSelected);
		sAIMDMemory->MdMemoryDisposeHandle((void **) selected);
		// No need to pass document since we're activating document in getArtHandle
		return gEngine->wrapArtHandle(env, res, NULL, true);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void activate()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Item_activate(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		sAIArt->SetInsertionPoint(art);
	} EXCEPTION_CONVERT(env);
}

/*
 * int nativeGetData()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Item_nativeGetData(JNIEnv *env, jobject obj) {
	AIDictionaryRef dictionary = NULL;
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		sAIArt->GetDictionary(art, &dictionary);
	} EXCEPTION_CONVERT(env);
	return (jint) dictionary;
}

/*
 * int getItemType()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Item_getItemType__(JNIEnv *env, jobject obj) {
	try {
		return Item_getType(gEngine->getArtHandle(env, obj));
	} EXCEPTION_CONVERT(env);
	return kUnknownArt;
}

/*
 * int getItemType(java.lang.Class item)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Item_getItemType__Ljava_lang_Class_2(JNIEnv *env, jclass cls, jclass item) {
	try {
		return Item_getType(env, item);
	} EXCEPTION_CONVERT(env);
	return kUnknownArt;
}

/*
 * com.scriptographer.ai.Item wrapHandle(int artHandle, int docHandle, boolean created, boolean checkWrapped)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Item_wrapHandle(JNIEnv *env, jclass cls, jint artHandle, jint docHandle, jboolean created, jboolean checkWrapped) {
	try {
		return gEngine->wrapArtHandle(env, (AIArtHandle) artHandle, (AIDocumentHandle) docHandle, created, -1, checkWrapped);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * boolean nativeAddEffect(com.scriptographer.ai.LiveEffect effect, com.scriptographer.ai.Dictionary parameters, int position)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Item_nativeAddEffect(JNIEnv *env, jobject obj, jobject effectObj, jobject parametersObj, jint position) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		AILiveEffectHandle liveEffect = gEngine->getLiveEffectHandle(env, effectObj);
		AIDictionaryRef liveDict = gEngine->getDictionaryHandle(env, parametersObj);
		AIArtStyleHandle style = NULL;
		sAIArtStyle->GetArtStyle(art, &style);
		if (style != NULL) {
			AIStyleParser parser = NULL;
			sAIArtStyleParser->NewParser(&parser);
			if (parser != NULL) {
				bool inserted = false;
				sAIArtStyleParser->ParseStyle(parser, style);
				AIParserLiveEffect effect = NULL;
				sAIArtStyleParser->NewParserLiveEffect(liveEffect, liveDict, &effect);
				if (effect != NULL) {
					if (position == kPreEffectFilter) {
						inserted = !sAIArtStyleParser->InsertNthPreEffect(parser, -1, effect);
					} else if (position == kPostEffectFilter) {
						inserted = !sAIArtStyleParser->InsertNthPreEffect(parser, -1, effect);
					} else {
						for (int i = sAIArtStyleParser->CountPaintFields(parser) - 1; !inserted && i >= 0; i--) {
							AIParserPaintField field = NULL;
							sAIArtStyleParser->GetNthPaintField(parser, i, &field);
							if (field != NULL) {
								if (position == kStrokeFilter && sAIArtStyleParser->IsStroke(field)
										|| position == kFillFilter && sAIArtStyleParser->IsFill(field)) {
									inserted = !sAIArtStyleParser->InsertNthEffectOfPaintField(parser, field, -1, effect);
								}
							}
						}
					}
					if (inserted) {
						sAIArtStyleParser->CreateNewStyle(parser, &style);
						sAIArtStyle->SetArtStyle(art, style);
					} else {
						sAIArtStyleParser->DisposeParserLiveEffect(effect);
					}
				}
				sAIArtStyleParser->DisposeParser(parser);
				return inserted;
			}
		}
	} EXCEPTION_CONVERT(env);
	return false;
}

struct LiveEffect_data {
	AILiveEffectHandle effect;
	jobject parameters;
	int position;
};

typedef bool (*EffectIterator)(JNIEnv *env, AIArtStyleHandle style, AIStyleParser parser,
		AIParserLiveEffect effect, AIParserPaintField field, int position, bool *cont,
		LiveEffect_data *data);

bool LiveEffect_callIterator(EffectIterator iterator, JNIEnv *env, AIArtStyleHandle style,
		AIStyleParser parser, AIParserLiveEffect effect, AIParserPaintField field, 
		int position, bool *cont, LiveEffect_data *data) {
	AILiveEffectHandle liveEffect = NULL;
	AILiveEffectParameters liveDict = NULL;
	sAIArtStyleParser->GetLiveEffectParams(effect, &liveDict);
	sAIArtStyleParser->GetLiveEffectHandle(effect, &liveEffect);
	if (liveEffect == data->effect && (data->parameters == NULL || gEngine->isEqual(env,
			gEngine->wrapLiveEffectParameters(env, liveDict), data->parameters))) {
		return iterator(env, style, parser, effect, field, kPreEffectFilter, cont, data);
	}
	return false;
}

bool LiveEffect_iterate(JNIEnv *env, AIArtHandle art, EffectIterator iterator, LiveEffect_data *data) {
	AIArtStyleHandle style = NULL;
	sAIArtStyle->GetArtStyle(art, &style);
	if (style != NULL) {
		AIStyleParser parser = NULL;
		sAIArtStyleParser->NewParser(&parser);
		if (parser != NULL) {
			sAIArtStyleParser->ParseStyle(parser, style);
			bool changed = false, cont = true;
			for (int i = sAIArtStyleParser->CountPreEffects(parser) - 1; cont && i >= 0; i--) {
				AIParserLiveEffect effect = NULL;
				sAIArtStyleParser->GetNthPreEffect(parser, i, &effect);
				if (effect != NULL)
					changed = LiveEffect_callIterator(iterator, env, style, parser, effect,
							NULL, kPreEffectFilter, &cont, data) || changed;
			}
			for (int i = sAIArtStyleParser->CountPostEffects(parser) - 1; cont && i >= 0; i--) {
				AIParserLiveEffect effect = NULL;
				sAIArtStyleParser->GetNthPostEffect(parser, i, &effect);
				if (effect != NULL)
					changed = LiveEffect_callIterator(iterator, env, style, parser, effect,
							NULL, kPreEffectFilter, &cont, data) || changed;
			}
			for (int i = sAIArtStyleParser->CountPaintFields(parser) - 1; cont && i >= 0; i--) {
				AIParserPaintField field = NULL;
				sAIArtStyleParser->GetNthPaintField(parser, i, &field);
				if (field != NULL) {
					for (int j = sAIArtStyleParser->CountEffectsOfPaintField(field) - 1; cont && j >= 0; j--) {
						AIParserLiveEffect effect = NULL;
						sAIArtStyleParser->GetNthEffectOfPaintField(field, j, &effect);
						if (effect != NULL) {
							changed = LiveEffect_callIterator(iterator, env, style, parser, effect, field,
							sAIArtStyleParser->IsStroke(field)
								? kStrokeFilter
								: sAIArtStyleParser->IsFill(field)
									? kFillFilter
									: 0,
							&cont, data) || changed;
						}
					}
				}
			}
			if (changed) {
				sAIArtStyleParser->CreateNewStyle(parser, &style);
				sAIArtStyle->SetArtStyle(art, style);
			}
			sAIArtStyleParser->DisposeParser(parser);
			return changed;
		}
	}
	return false;
}

bool LiveEffect_positionIterator(JNIEnv *env, AIArtStyleHandle style, AIStyleParser parser,
		AIParserLiveEffect effect, AIParserPaintField field, int position, bool *cont,
		LiveEffect_data *data) {
	data->position = position;
	*cont = false;
	return false;
}

/*
 * int nativeGetEffectPosition(com.scriptographer.ai.LiveEffect effect, java.util.Map parameters)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Item_nativeGetEffectPosition(JNIEnv *env, jobject obj, jobject effect, jobject parameters) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		LiveEffect_data effectData = {
			gEngine->getLiveEffectHandle(env, effect),
			parameters,
			0
		};
		LiveEffect_iterate(env, art, LiveEffect_positionIterator, &effectData);
		return effectData.position;
	} EXCEPTION_CONVERT(env);
	return 0;
}

bool LiveEffect_removeIterator(JNIEnv *env, AIArtStyleHandle style, AIStyleParser parser,
		AIParserLiveEffect effect, AIParserPaintField field, int position, bool *cont,
		LiveEffect_data *data) {
	bool removed;
	switch (position) {
	case kPreEffectFilter:
		removed = !sAIArtStyleParser->RemovePreEffect(parser, effect, true);
		break;
	case kPostEffectFilter:
		removed = !sAIArtStyleParser->RemovePostEffect(parser, effect, true);
		break;
	default:
		removed = !sAIArtStyleParser->RemoveEffectOfPaintField(parser, field, effect, true);
	}
	if (removed) {
		sAIArtStyleParser->DisposeParserLiveEffect(effect);
		return true;
	}
	return false;
}

/*
 * boolean removeEffect(com.scriptographer.ai.LiveEffect effect, java.util.Map parameters)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Item_removeEffect(JNIEnv *env, jobject obj, jobject effect, jobject parameters) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		LiveEffect_data effectData = {
			gEngine->getLiveEffectHandle(env, effect),
			parameters
		};
		return LiveEffect_iterate(env, art, LiveEffect_removeIterator, &effectData);
	} EXCEPTION_CONVERT(env);
	return false;
}

bool LiveEffect_editIterator(JNIEnv *env, AIArtStyleHandle style, AIStyleParser parser,
		AIParserLiveEffect effect, AIParserPaintField field, int position, bool *cont,
		LiveEffect_data *data) {
	sAIArtStyleParser->EditEffectParameters(style, effect);
	*cont = false;
	return false;
}

/*
 * boolean editEffect(com.scriptographer.ai.LiveEffect effect, java.util.Map parameters)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Item_editEffect(JNIEnv *env, jobject obj, jobject effect, jobject parameters) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		LiveEffect_data effectData = {
			gEngine->getLiveEffectHandle(env, effect),
			parameters
		};
		return LiveEffect_iterate(env, art, LiveEffect_editIterator, &effectData);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean isValid(int handle)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Item_isValid(JNIEnv *env, jclass cls, jint handle) {
	try {
#if kPluginInterfaceVersion < kAI12
		return sAIArt->ValidArt((AIArtHandle) handle);
#else // kPluginInterfaceVersion >= kAI12
		return sAIArt->ValidArt((AIArtHandle) handle, false);
#endif // kPluginInterfaceVersion >= kAI12
	} EXCEPTION_CONVERT(env);
	return false;
}
