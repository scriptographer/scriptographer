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
 * $RCSfile: com_scriptographer_ai_Art.cpp,v $
 * $Author: lehni $
 * $Revision: 1.20 $
 * $Date: 2006/09/29 22:37:12 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Art.h"

/*
 * com.scriptographer.ai.Art
 */
 
short Art_getType(AIArtHandle art) {
	short type = -1;
	sAIArt->GetArtType(art, &type);
	return type;
}

short Art_getType(JNIEnv *env, jclass cls) {
	if (env->IsSameObject(cls, gEngine->cls_Art)) {
		return kAnyArt;
	} else if (env->IsSameObject(cls, gEngine->cls_Path)) {
		return kPathArt;
	} else if (env->IsSameObject(cls, gEngine->cls_CompoundPath)) {
		return kCompoundPathArt;
	} else if (env->IsSameObject(cls, gEngine->cls_Raster)) {
		return kRasterArt;
	} else if (env->IsAssignableFrom(cls, gEngine->cls_TextFrame)) {
		return kTextFrameArt;
	} else if (env->IsSameObject(cls, gEngine->cls_Layer)) {
		// special defined type for layers, needs handling!
		return com_scriptographer_ai_Art_TYPE_LAYER;
	} else if (env->IsSameObject(cls, gEngine->cls_Group)) {
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

/*
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Art_nativeCreate(JNIEnv *env, jclass cls, jint type, jobject relative) {
	AIArtHandle art = NULL;
	try {
		AIArtHandle relativeArt = NULL;
		if (relative != NULL) {
			relativeArt = gEngine->getArtHandle(env, relative);
		}
		// if type is set to the self defined TYPE_LAYER, create a layer and return the wrapped art group object instead:
		if (type == com_scriptographer_ai_Art_TYPE_LAYER) { // create a layer
			// place it above all others:
			AILayerHandle layer = NULL;
			if (relativeArt != NULL) {
				jboolean isLayer = artIsLayer(relativeArt);
				if (!isLayer) {
					// see wether the parent of relativeArt is a layer:
					AIArtHandle parentArt = NULL;
					sAIArt->GetArtParent(relativeArt, &parentArt);
					if (!artIsLayer(parentArt))
						throw new StringException("Layers can only be created within other layers.");
				}
				sAILayer->InsertLayerAtArt(relativeArt, isLayer ? kPlaceInsideOnTop : kPlaceAbove, &layer);
			} else {
				sAILayer->InsertLayer(NULL, kPlaceAboveAll, &layer);
			}
			if (layer != NULL)
				sAIArt->GetFirstArtOfLayer(layer, &art);
			
			if (art == NULL)
				throw new StringException("Cannot create layer. Please make sure there is an open document.");
		} else { // create a normal art object
			if (relativeArt != NULL) {
				short artType = artGetType(relativeArt);
				sAIArt->NewArt(type, (artType == kGroupArt || artType == kCompoundPathArt) ? kPlaceInsideOnTop : kPlaceAbove, relativeArt, &art);
			} else {
				sAIArt->NewArt(type, kPlaceAboveAll, NULL, &art);
			}
			if (art == NULL)
				throw new StringException("Cannot create art object. Please make sure there is an open document.");
		}
	} EXCEPTION_CONVERT(env)
	return (jint)art;
}
*/

/*
 * long nativeCreate(int docHandle, int type)
 */
JNIEXPORT jlong JNICALL Java_com_scriptographer_ai_Art_nativeCreate(JNIEnv *env, jclass cls, jint docHandle, jint type) {
	AIArtHandle art = NULL;

	CREATEART_BEGIN

	AIArtHandle artLayer = Layer_beginCreateArt();
	// if type is set to the self defined TYPE_LAYER, create a layer and return the wrapped art group object instead:
	if (type == com_scriptographer_ai_Art_TYPE_LAYER) { // create a layer
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
		// try to create in the active layer
		sAIArt->NewArt(type, artLayer != NULL ? kPlaceInsideOnTop : kPlaceAboveAll, artLayer, &art);
		if (art == NULL)
			throw new StringException("Cannot create art object. Please make sure there is an open document.");
	}

	CREATEART_END

	return (jlong) art;
}

/*
 * void finalize()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_finalize(JNIEnv *env, jobject obj) {
	try {
		AIDictionaryRef dictionary = gEngine->getArtDictionaryRef(env, obj);
		if (dictionary != NULL) {
			sAIDictionary->Release(dictionary);
		}
	} EXCEPTION_CONVERT(env)
}

/*
 * Walks through the given dictionary and finds the key for art :)
 */
AIDictKey artGetDictionaryKey(AIDictionaryRef dictionary, AIArtHandle art) {
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

/*
 * boolean nativeRemove(int handle, jint dictionaryRef)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_nativeRemove(JNIEnv *env, jobject obj, jint handle, jint dictionaryRef) {
	try {
		AIArtHandle art = (AIArtHandle) handle;
		// treat the object differently if it's in a dictionary than in the
		// normal artwork tree of the document:
		AIDictionaryRef dictionary = (AIDictionaryRef) dictionaryRef;
		if (dictionary != NULL) {
			AIDictKey key = artGetDictionaryKey(dictionary, art);
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
	} EXCEPTION_CONVERT(env)
	return false;
}

/*
 * java.lang.Object clone()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Art_clone(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle newArt = NULL;
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		// treat the object differently if it's in a dictionary than in the
		// normal artwork tree of the document:
		AIDictionaryRef dictionary = gEngine->getArtDictionaryRef(env, obj);
		if (dictionary != NULL) {
			AIDictKey key = artGetDictionaryKey(dictionary, art);
			if (key != NULL)
				sAIDictionary->CopyEntryToArt(dictionary, key, kPlaceAboveAll, NULL, &newArt);
		}

		if (newArt == NULL)
			sAIArt->DuplicateArt(art, kPlaceAbove, art, &newArt);

		if (newArt != NULL)
			return gEngine->wrapArtHandle(env, newArt);
	} EXCEPTION_CONVERT(env)
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
	} EXCEPTION_CONVERT(env)
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
			// there's no other way to do this on < CS 1
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
	} EXCEPTION_CONVERT(env)
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
	} EXCEPTION_CONVERT(env)
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
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.ai.Art getParent()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Art_getParent(JNIEnv *env, jobject obj) {
	jobject res = NULL;
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIArtHandle child = NULL;
		sAIArt->GetArtParent(art, &child);
		if (child != NULL) {
			res = gEngine->wrapArtHandle(env, child);
		}
	} EXCEPTION_CONVERT(env)
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
	} EXCEPTION_CONVERT(env)
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
	} EXCEPTION_CONVERT(env)
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
	} EXCEPTION_CONVERT(env)
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
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setCenterVisible(boolean visible)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_setCenterVisible(JNIEnv *env, jobject obj, jboolean visible) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		sAIArt->SetArtCenterPointVisible(art, visible);
	} EXCEPTION_CONVERT(env)
}

/*
 * void setAttribute(long attribute, boolean value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_setAttribute(JNIEnv *env, jobject obj, jint attribute, jboolean value) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		if (sAIArt->SetArtUserAttr(art, attribute, value ? attribute : 0))
			throw new StringException("Cannot set attributes for art object");
    } EXCEPTION_CONVERT(env)
}

/*
 * boolean getAttribute(long attribute)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_getAttribute(JNIEnv *env, jobject obj, jint attribute) {
	jboolean value = false;
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		long values;
		if (sAIArt->GetArtUserAttr(art, attribute, &values))
			throw new StringException("Cannot get attributes for art object");
		value = values & attribute;
    } EXCEPTION_CONVERT(env)
	return value;
}

/*
 * int getBlendMode()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Art_getBlendMode(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		return sAIBlendStyle->GetBlendingMode(art);
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void setBlendMode(int mode)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_setBlendMode(JNIEnv *env, jobject obj, jint mode) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		sAIBlendStyle->SetBlendingMode(art, (AIBlendingMode) mode);
	} EXCEPTION_CONVERT(env)
}

/*
 * float getOpacity()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_Art_getOpacity(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		return sAIBlendStyle->GetOpacity(art);
	} EXCEPTION_CONVERT(env)
	return 0.0;
}

/*
 * void setOpacity(float opacity)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_setOpacity(JNIEnv *env, jobject obj, jfloat opacity) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		sAIBlendStyle->SetOpacity(art, opacity);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean getIsolated()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_getIsolated(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		return sAIBlendStyle->GetIsolated(art);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setIsolated(boolean isolated)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_setIsolated(JNIEnv *env, jobject obj, jboolean isolated) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		sAIBlendStyle->SetIsolated(art, isolated);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean getKnockout()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_getKnockout(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		return sAIBlendStyle->GetKnockout(art);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setKnockout(int knockout)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_setKnockout(JNIEnv *env, jobject obj, jint knockout) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		sAIBlendStyle->SetKnockout(art, (AIKnockout) knockout);
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean getKnockout()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_getKnockoutInherited(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		return sAIBlendStyle->GetInheritedKnockout(art);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * boolean getAlphaIsShape()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_getAlphaIsShape(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		return sAIBlendStyle->GetAlphaIsShape(art);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setAlphaIsShape(boolean isAlpha)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_setAlphaIsShape(JNIEnv *env, jobject obj, jboolean isAlpha) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		sAIBlendStyle->SetAlphaIsShape(art, isAlpha);
	} EXCEPTION_CONVERT(env)
}

/*
 * java.lang.String getName()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_Art_getName(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
#if kPluginInterfaceVersion < kAI12
		char name[256];
		if (!sAIArt->GetArtName(art, name, 256, NULL)) {
#else
		ai::UnicodeString name;
		if (!sAIArt->GetArtName(art, name, NULL)) {
#endif
			return gEngine->convertString(env, name);
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void setName(java.lang.String name)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_setName(JNIEnv *env, jobject obj, jstring name) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
#if kPluginInterfaceVersion < kAI12
		char *str = gEngine->convertString(env, name);
		sAIArt->SetArtName(art, str);
		delete str;
#else
		ai::UnicodeString str = gEngine->convertString_UnicodeString(env, name);
		sAIArt->SetArtName(art, str);
#endif
	} EXCEPTION_CONVERT(env)
}

/*
 * boolean hasDefaultName()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_hasDefaultName(JNIEnv *env, jobject obj) {
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
	} EXCEPTION_CONVERT(env)
	return isDefaultName;
}

/*
 * boolean appendChild(com.scriptographer.ai.Art art)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_appendChild(JNIEnv *env, jobject obj, jobject art) {
	try {
		if (art != NULL) {
			AIArtHandle art1 = gEngine->getArtHandle(env, obj);
			AIArtHandle art2 = gEngine->getArtHandle(env, art);
			if (art1 != NULL && art2 != NULL && art1 != art2) {
				short type1 = Art_getType(art1);
#if kPluginInterfaceVersion < kAI11
				if (type1 == kTextArt) {
					short type2 = Art_getType(art2);
					if (type2 == kPathArt) {
						// check that this path is not already in a textPath object:
						AIArtHandle parent;
						if (!sAIArt->GetArtParent(art2, &parent) && artGetType(parent) != kTextPathArt) {
							AIArtHandle path = art2;
							if (!sAITextPath->InsertTextPath(art1, NULL, kPlaceInsideOnTop, &path)) {
								// assign the new art handle
								artChangeArt(art2, obj2, path);
								return true;
							}
						}
					}
				} else
#endif
				if (type1 == kGroupArt || type1 == kCompoundPathArt || type1 == kMysteryPathArt) {
					// if art belongs to a dictionary, treat it differently
					AIDictionaryRef dict2 = gEngine->getArtDictionaryRef(env, art);
					if (dict2 != NULL) {
						AIDictKey key = artGetDictionaryKey(dict2, art2);
						if (key != NULL) {
							if (!sAIDictionary->MoveEntryToArt(dict2, key, kPlaceInsideOnTop, art1, &art2)) {
								gEngine->changeArtHandle(env, art, art2, NULL);
								return true;
							}
						}

					}
					// simply append it
					if (!sAIArt->ReorderArt(art2, kPlaceInsideOnTop, art1))
						return true;
				}
			}
		}
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

jboolean artMove(JNIEnv *env, jobject obj, jobject art, short paintOrder) {
	try {
		if (art != NULL) {
			AIArtHandle art1 = gEngine->getArtHandle(env, obj);
			AIArtHandle art2 = gEngine->getArtHandle(env, art);
			if (art1 != NULL && art2 != NULL && art1 != art2) {
#if kPluginInterfaceVersion < kAI11
				AIArtHandle parent = NULL, path = NULL;
				short type1 = artGetType(art1);
				short type2 = artGetType(art2);
				if (type2 == kTextPathArt) {
					if (type1 == kTextPathArt) {
						// get the path of it, remove the bellonging textPath, and add the new one...
						if (!sAIArt->GetArtParent(art1, &parent) &&
							!sAITextPath->DeleteTextPath(parent, art1, &path) &&
							!sAIArt->GetArtParent(art2, &parent) &&
							!sAITextPath->InsertTextPath(parent, art2, paintOrder, &path)) {
							artChangeArt(art1, obj, path);
							return true;
							
						}
					} else if (type1 == kPathArt) {
						// insert it before art2
						AIArtHandle parent;
						if (!sAIArt->GetArtParent(art2, &parent) &&
							!sAITextPath->InsertTextPath(parent, art2, paintOrder, &art1))
							artChangeArt(art1, obj, art1);
							return true;
					}
				} else 
#endif
				{ // type2 != kTextPathArt
#if kPluginInterfaceVersion < kAI11
					if (type1 == kTextPathArt) { // delete this textPath and get the path handle from it
						if (!sAIArt->GetArtParent(art1, &parent) &&
							!sAITextPath->DeleteTextPath(parent, art1, &path)) {
							art1 = path;
							artChangeArt(art1, obj, art1);
						} else art1 = NULL;
					}
#endif
					// simply try to reorder it
					if (art1 != NULL && art2 != NULL) {
						// if art belongs to a dictionary, treat it differently
						AIDictionaryRef dict1 = gEngine->getArtDictionaryRef(env, obj);
						if (dict1 != NULL) {
							AIDictKey key = artGetDictionaryKey(dict1, art1);
							if (key != NULL) {
								if (!sAIDictionary->MoveEntryToArt(dict1, key, paintOrder, art2, &art1)) {
									gEngine->changeArtHandle(env, obj, art1, NULL);
									return true;
								}
							}

						}

						if (!sAIArt->ReorderArt(art1, paintOrder, art2))
							return true;
					}
				}
			}
		}
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * boolean moveAbove(com.scriptographer.ai.Art art)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_moveAbove(JNIEnv *env, jobject obj, jobject art) {
	return artMove(env, obj, art, kPlaceAbove);
}

/*
 * boolean moveBelow(com.scriptographer.ai.Art art)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_moveBelow(JNIEnv *env, jobject obj, jobject art) {
	return artMove(env, obj, art, kPlaceBelow);
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
		gEngine->setIntField(env, obj, gEngine->fid_Art_version, gEngine->getIntField(env, obj, gEngine->fid_Art_version) + 1);
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
 * void transform(java.awt.geom.AffineTransform at, int scaleFlags)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_transform(JNIEnv *env, jobject obj, jobject at, jint flags) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIRealMatrix matrix;
		gEngine->convertMatrix(env, at, &matrix);
		// according to adobe sdk manual: linescale = sqrt(scaleX) * sqrt(scaleY)
		AIReal sx, sy;
		sAIRealMath->AIRealMatrixGetScale(&matrix, &sx, &sy);
		AIReal lineScale = sAIRealMath->AIRealSqrt(sx) * sAIRealMath->AIRealSqrt(sy);
		artTransform(env, art, &matrix, lineScale, flags);
	} EXCEPTION_CONVERT(env)
}

/*
 * com.scriptographer.ai.Raster rasterize(int type, float resolution, int antialiasing, float width, float height)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Art_rasterize(JNIEnv *env, jobject obj, jint type, jfloat resolution, jint antialiasing, jfloat width, jfloat height) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIArtHandle raster = Art_rasterize(art, (AIRasterizeType) type, resolution, antialiasing, width, height);
		if (raster != NULL)
			return gEngine->wrapArtHandle(env, raster);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void expand(int flags, int steps)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_expand(JNIEnv *env, jobject obj, jint flags, jint steps) {
	try {
		// commit pending changes first, before native expand is called!
		gEngine->callStaticObjectMethod(env, gEngine->cls_CommitManager, gEngine->mid_CommitManager_commit, obj);
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		sAIExpand->Expand(art, flags, steps);
	} EXCEPTION_CONVERT(env)
}

/*
 * int getOrder(com.scriptographer.ai.Art art)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Art_getOrder(JNIEnv *env, jobject obj, jobject art) {
	try {
		AIArtHandle handle1 = gEngine->getArtHandle(env, obj);
		AIArtHandle handle2 = gEngine->getArtHandle(env, art);
		short order;
		sAIArt->GetArtOrder(handle1, handle2, &order);
		return order;
	} EXCEPTION_CONVERT(env)
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
	} EXCEPTION_CONVERT(env)
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
	} EXCEPTION_CONVERT(env)
	if (dictionary != NULL)
		sAIDictionary->Release(dictionary);
}

/*
 * boolean isValid()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_isValid(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
#if kPluginInterfaceVersion < kAI12
		return sAIArt->ValidArt(art);
#else
		// TODO: If searchAllLayerLists is true, then this does a search through all layers in
		// all layer lists. Otherwise, it only does a search on the current layer list.
		return sAIArt->ValidArt(art, false);
#endif
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}
