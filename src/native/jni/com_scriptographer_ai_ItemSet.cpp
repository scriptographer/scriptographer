/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id: com_scriptographer_ai_ItemSet.cpp 460 2008-02-28 17:06:09Z lehni $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Item.h"
#include "com_scriptographer_ai_ItemSet.h"

/*
 * com.scriptographer.ai.ItemSet
 */

void ItemSet_filter(AIArtSet set, bool layerOnly) {
	// takes out all kUnknownArt, kTextRunArt, ... objs
	// removes layergroups as well
	long count;
	sAIArtSet->CountArtSet(set, &count);
	for (long i = count - 1; i >= 0; i--) {
		AIArtHandle art = NULL;
		if (!sAIArtSet->IndexArtSet(set, i, &art)) {
			short type = Item_getType(art);
			bool isLayer = Item_isLayer(art);
			if (type == kUnknownArt ||
#if kPluginInterfaceVersion < kAI11
				type == kTextRunArt ||
#endif
				(layerOnly && !isLayer || !layerOnly && isLayer)) {
					sAIArtSet->RemoveArtFromArtSet(set, art);
			}
		}
	}
}

jobject ItemSet_getSelected(JNIEnv *env) {
	AIArtSet set = NULL;
	if (!sAIArtSet->NewArtSet(&set)) {
		if (!sAIArtSet->SelectedArtSet(set)) {
			// now filter out objects of which the parents are selected too
			long count;
			sAIArtSet->CountArtSet(set, &count);
			for (long i = count - 1; i >= 0; i--) {
				AIArtHandle art;
				if (!sAIArtSet->IndexArtSet(set, i, &art)) {
					AIArtHandle parent = NULL;
					long values;
					if (!sAIArt->GetArtUserAttr(art, kArtFullySelected, &values) && !(values & kArtFullySelected)) {
						sAIArtSet->RemoveArtFromArtSet(set, art);
					} else {
						sAIArt->GetArtParent(art, &parent);
						if (!Item_isLayer(parent)) {
							if (!sAIArt->GetArtUserAttr(parent, kArtFullySelected, &values) && (values & kArtFullySelected))
								sAIArtSet->RemoveArtFromArtSet(set, art);
						}
					}
				}
			}
			jobject	itemSet = gEngine->convertArtSet(env, set);
			sAIArtSet->DisposeArtSet(&set);
			return itemSet;
		}
	}
	return NULL;
}

AIArtHandle ItemSet_rasterize(AIArtSet artSet, AIRasterizeType type, float resolution, int antialiasing, float width, float height) {
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
	settings.options = kRasterizeOptionsNone;
	AIRealRect artBounds;
	sAIRasterize->ComputeArtBounds(artSet, &artBounds, false);
	if (width >= 0)
		artBounds.right = artBounds.left + width;
	if (height >= 0)
		artBounds.bottom = artBounds.top + height;
	AIArtHandle raster = NULL;
	// walk through artSet and find the art that is blaced above all others:
	AIArtHandle top = NULL;
	long count;
	sAIArtSet->CountArtSet(artSet, &count);
	for (long i = count - 1; i >= 0; i--) {
		AIArtHandle art;
		if (!sAIArtSet->IndexArtSet(artSet, i, &art)) {
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
	sAIRasterize->Rasterize(artSet, &settings, &artBounds, kPlaceAbove, top, &raster, NULL);
	return raster;
}

/*
 * com.scriptographer.ai.ItemSet invert()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_ItemSet_invert(JNIEnv *env, jobject obj) {
	try {
		AIArtSet setFrom = gEngine->convertArtSet(env, obj), setTo;
		if (setFrom != NULL && !sAIArtSet->NewArtSet(&setTo) && !sAIArtSet->NotArtSet(setFrom, setTo)) {
				jobject itemSet = gEngine->convertArtSet(env, setTo);
				sAIArtSet->DisposeArtSet(&setFrom);
				sAIArtSet->DisposeArtSet(&setTo);
				return itemSet;
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Raster rasterize(int type, float resolution, int antialiasing, float width, float height)
 */

JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_ItemSet_rasterize(JNIEnv *env, jobject obj, jint type, jfloat resolution, jint antialiasing, jfloat width, jfloat height) {
	try {
		AIArtSet set = gEngine->convertArtSet(env, obj);
		AIArtHandle raster = ItemSet_rasterize(set, (AIRasterizeType) type, resolution, antialiasing, width, height);
		if (raster != NULL)
			return gEngine->wrapArtHandle(env, raster);
	} EXCEPTION_CONVERT(env);
	return NULL;
}
