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
 * $Revision: 1.3 $
 * $Date: 2005/03/25 00:27:58 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_Art.h"

/*
 * com.scriptographer.ai.Art
 */
 
short artGetType(AIArtHandle handle) {
	short type = -1;
	sAIArt->GetArtType(handle, &type);
	return type;
}

short artGetType(JNIEnv *env, jclass cls) {
	if (env->IsInstanceOf(cls, gEngine->cls_Art)) {
		return kAnyArt;
	} else if (env->IsInstanceOf(cls, gEngine->cls_Path)) {
		return kPathArt;
	} else if (env->IsInstanceOf(cls, gEngine->cls_Raster)) {
		return kRasterArt;
	} else if (env->IsInstanceOf(cls, gEngine->cls_Layer)) {
		// special defined type for layers, needs handling!
		return com_scriptographer_ai_Art_TYPE_LAYER;
	} else if (env->IsInstanceOf(cls, gEngine->cls_Group)) {
		return kGroupArt;
	}
	return kUnknownArt;
	// TODO: make sure the above list contains all Art classes!
}

jboolean artHasChildren(AIArtHandle handle) {
	// don't show the children of textPaths and pointText 
	short type = artGetType(handle);
#ifdef OLD_TEXT_SUITES
	return (type == kTextArt && artGetTextType(handle) != kPointTextType) || (type != kTextPathArt);
#else
	return true;
#endif
}

jboolean artIsLayer(AIArtHandle handle) {
	ASBoolean isLayerGroup = false;
	sAIArt->IsArtLayerGroup(handle, &isLayerGroup);
	return isLayerGroup;
}

/*
 * int nativeCreate(int type, com.scriptographer.ai.Art relative)
 */
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
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Art_nativeCreate(JNIEnv *env, jclass cls, jint type) {
	AIArtHandle art = NULL;
	try {
		// if type is set to the self defined TYPE_LAYER, create a layer and return the wrapped art group object instead:
		if (type == com_scriptographer_ai_Art_TYPE_LAYER) { // create a layer
			// place it above all others:
			AILayerHandle layer = NULL;
			sAILayer->InsertLayer(NULL, kPlaceAboveAll, &layer);
			if (layer != NULL)
				sAIArt->GetFirstArtOfLayer(layer, &art);
			if (art == NULL)
				throw new StringException("Cannot create layer. Please make sure there is an open document.");
		} else { // create a normal art object
			sAIArt->NewArt(type, kPlaceAboveAll, NULL, &art);
			if (art == NULL)
				throw new StringException("Cannot create art object. Please make sure there is an open document.");
		}
	} EXCEPTION_CONVERT(env)
	return (jint)art;
}

/*
 * boolean nativeRemove(int handle)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_nativeRemove(JNIEnv *env, jobject obj, jint handle) {
	try {
		AIArtHandle art = (AIArtHandle) handle;
		if (artIsLayer(art)) {
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
	    AIArtHandle handle = gEngine->getArtHandle(env, obj);
		AIArtHandle newArt = NULL;
		sAIArt->DuplicateArt(handle, kPlaceAbove,handle, &newArt);
		if (newArt != NULL) {
			return gEngine->wrapArtHandle(env, newArt);
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * com.scriptographer.ai.Art getFirstChild()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Art_getFirstChild(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle handle = gEngine->getArtHandle(env, obj);
		if (artHasChildren(handle)) {
			AIArtHandle child = NULL;
			sAIArt->GetArtFirstChild(handle, &child);
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
	    AIArtHandle handle = gEngine->getArtHandle(env, obj);
		if (artHasChildren(handle)) {
			// there's no other way to do this:
			AIArtHandle child = NULL, curChild = NULL;
			sAIArt->GetArtFirstChild(handle, &curChild);
			if (curChild != NULL) {
				do {
					child = curChild;
					sAIArt->GetArtSibling(child, &curChild);
				} while (curChild != NULL);
				if (child != NULL) {
					return gEngine->wrapArtHandle(env, child);
				}
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
	    AIArtHandle handle = gEngine->getArtHandle(env, obj);
		AIArtHandle child = NULL;
		sAIArt->GetArtSibling(handle, &child);
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
	    AIArtHandle handle = gEngine->getArtHandle(env, obj);
		AIArtHandle child = NULL;
		sAIArt->GetArtPriorSibling(handle, &child);
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
	    AIArtHandle handle = gEngine->getArtHandle(env, obj);
		AIArtHandle child = NULL;
		sAIArt->GetArtParent(handle, &child);
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
	    AIArtHandle handle = gEngine->getArtHandle(env, obj);
	    sAIArt->GetArtBounds(handle, &rt);
	    return gEngine->convertRectangle(env, &rt);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * boolean isCenterVisible()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_isCenterVisible(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle handle = gEngine->getArtHandle(env, obj);
		AIBoolean visible;
		if (!sAIArt->GetArtCenterPointVisible(handle, &visible))
			return visible;
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void setCenterVisible(boolean visible)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_setCenterVisible(JNIEnv *env, jobject obj, jboolean visible) {
	try {
	    AIArtHandle handle = gEngine->getArtHandle(env, obj);
		sAIArt->SetArtCenterPointVisible(handle, visible);
	} EXCEPTION_CONVERT(env)
}

/*
 * void setUserAttributes(long flags, long values)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_setUserAttributes(JNIEnv *env, jobject obj, jint flags, jint values) {
	try {
	    AIArtHandle handle = gEngine->getArtHandle(env, obj);
		if (sAIArt->SetArtUserAttr(handle, flags, values))
			throw new StringException("Cannot set attributes for art object");
    } EXCEPTION_CONVERT(env)
}

/*
 * int getUserAttributes(long flags)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Art_getUserAttributes(JNIEnv *env, jobject obj, jint flags) {
	long values;
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		if (sAIArt->GetArtUserAttr(handle, flags, &values))
			throw new StringException("Cannot get attributes for art object");
    } EXCEPTION_CONVERT(env)
	return values;
}

/*
 * void setName(java.lang.String name)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_setName(JNIEnv *env, jobject obj, jstring name) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		char *str = gEngine->createCString(env, name);
		sAIArt->SetArtName(handle, str);
		delete str;
	} EXCEPTION_CONVERT(env)
}

/*
 * java.lang.String getName()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_Art_getName(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		char name[256];
		if (!sAIArt->GetArtName(handle, name, 256, NULL)) {
			return gEngine->createJString(env, name);
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * boolean append(com.scriptographer.ai.Art art)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_append(JNIEnv *env, jobject obj, jobject art) {
	try {
		AIArtHandle handle1 = gEngine->getArtHandle(env, obj);
		AIArtHandle handle2 = gEngine->getArtHandle(env, art);
		if (handle1 != NULL && handle2 != NULL && handle1 != handle2) {
			short type1 = artGetType(handle1);
#ifdef OLD_TEXT_SUITES
			if (type1 == kTextArt) {
				short type2 = artGetType(handle2);
				if (type2 == kPathArt) {
					// check that this path is not already in a textPath object:
					AIArtHandle parent;
					if (!sAIArt->GetArtParent(handle2, &parent) && artGetType(parent) != kTextPathArt) {
						AIArtHandle path = handle2;
						if (!sAITextPath->InsertTextPath(handle1, NULL, kPlaceInsideOnTop, &path)) {
							// assign the new art handle
							// TODO: artChangeArt(handle2, obj2, path);
							return JNI_TRUE;
						}
					}
				}
			} else
#endif
			if (type1 == kGroupArt || type1 == kCompoundPathArt || type1 == kMysteryPathArt) {
				// simply append it!
				if (!sAIArt->ReorderArt(handle2, kPlaceInsideOnTop, handle1))
					return JNI_TRUE;
			}
		}
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}


jboolean artMove(JNIEnv *env, jobject obj, jobject art, short paintOrder) {
	AIArtHandle handle1 = gEngine->getArtHandle(env, obj);
	AIArtHandle handle2 = gEngine->getArtHandle(env, art);
	if (handle1 != NULL && handle2 != NULL && handle1 != handle2) {
		AIArtHandle parent = NULL, path = NULL;
		short type1 = artGetType(handle1);
		short type2 = artGetType(handle2);
#ifdef OLD_TEXT_SUITES
		if (type2 == kTextPathArt) {
			if (type1 == kTextPathArt) {
				// get the path of it, remove the bellonging textPath, and add the new one...
				if (!sAIArt->GetArtParent(handle1, &parent) &&
					!sAITextPath->DeleteTextPath(parent, handle1, &path) &&
					!sAIArt->GetArtParent(handle2, &parent) &&
					!sAITextPath->InsertTextPath(parent, handle2, paintOrder, &path)) {
					artChangeArt(art1, obj, path);
					return JNI_TRUE;
					
				}
			} else if (type1 == kPathArt) {
				// insert it before handle2
				AIArtHandle parent;
				if (!sAIArt->GetArtParent(handle2, &parent) &&
					!sAITextPath->InsertTextPath(parent, handle2, paintOrder, &handle1))
					artChangeArt(art1, obj, handle1);
					return JNI_TRUE;
			}
		} else 
#endif
		{ // type2 != kTextPathArt
#ifdef OLD_TEXT_SUITES
			if (type1 == kTextPathArt) { // delete this textPath and get the path handle from it
				if (!sAIArt->GetArtParent(handle1, &parent) &&
					!sAITextPath->DeleteTextPath(parent, handle1, &path)) {
					handle1 = path;
					artChangeArt(art1, obj, handle1);
				} else handle1 = NULL;
			}
#endif
			// simply try to reorder it
			if (handle1 != NULL && handle2 != NULL && !sAIArt->ReorderArt(handle1, paintOrder, handle2))
			return JNI_TRUE;
		}
	}
	return JNI_FALSE;
}


/*
 * boolean moveAbove(com.scriptographer.ai.Art art)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_moveAbove(JNIEnv *env, jobject obj, jobject art) {
	try {
		return artMove(env, obj, art, kPlaceAbove);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * boolean moveBelow(com.scriptographer.ai.Art art)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Art_moveBelow(JNIEnv *env, jobject obj, jobject art) {
	try {
		return artMove(env, obj, art, kPlaceBelow);
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}

/*
 * void transform(java.awt.geom.AffineTransform at, int scaleFlags)
 */

// if 'deep' is set, artTransform traverses the children recursively and transforms them: 
void artTransform(JNIEnv *env, jobject obj, AIArtHandle art, AIRealMatrix *matrix, AIReal lineScale, long flags) {
	sAITransformArt->TransformArt(art, matrix, lineScale, flags);
	short type = artGetType(art);

	if (flags & com_scriptographer_ai_Art_TRANSFORM_DEEP) {
		AIArtHandle child;
		sAIArt->GetArtFirstChild(art, &child);
		while (child != NULL) {
			artTransform(env, NULL, child, matrix, lineScale, flags);
			sAIArt->GetArtSibling(child, &child);
		}
	}
}

JNIEXPORT void JNICALL Java_com_scriptographer_ai_Art_transform(JNIEnv *env, jobject obj, jobject at, jint flags) {
	try {
		AIArtHandle handle = gEngine->getArtHandle(env, obj);
		AIRealMatrix matrix;
		gEngine->convertMatrix(env, at, &matrix);
		// modify the matrix so that it 'acts' on the center of the selected object
		AIRealRect bounds;
		sAIArt->GetArtBounds(handle, &bounds);

		AIReal centerX = (bounds.left + bounds.right) * 0.5f;
		AIReal centerY = (bounds.top + bounds.bottom) * 0.5f;

		AIRealMatrix m;
		sAIRealMath->AIRealMatrixSetTranslate(&m, -centerX, -centerY);
		sAIRealMath->AIRealMatrixConcat(&m, &matrix, &m);
		sAIRealMath->AIRealMatrixConcatTranslate(&m, centerX, centerY);

		// according to adobe sdk manual: linescale = sqrt(scaleX) * sqrt(scaleY)
		AIReal sx, sy;
		sAIRealMath->AIRealMatrixGetScale(&m, &sx, &sy);
		AIReal lineScale = sAIRealMath->AIRealSqrt(sx) * sAIRealMath->AIRealSqrt(sy);

		artTransform(env, obj, handle, &m, lineScale, flags);
	} EXCEPTION_CONVERT(env)
}
