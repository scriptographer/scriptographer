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
 * $Id: com_scriptographer_ai_ItemList.cpp 578 2008-07-22 21:15:16Z lehni $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Item.h"
#include "com_scriptographer_ai_ItemList.h"

/*
 * com.scriptographer.ai.ItemList
 */

void ItemList_filter(AIArtSet set, bool layerOnly) {
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

AIArtSet ItemList_getSelected(JNIEnv *env) {
	AIArtSet set = NULL;
	if (!sAIArtSet->NewArtSet(&set)) {
		if (!sAIArtSet->SelectedArtSet(set)) {
			// Now filter out objects of which the parents are selected too
			long count;
			sAIArtSet->CountArtSet(set, &count);
			for (long i = count - 1; i >= 0; i--) {
				AIArtHandle art;
				if (!sAIArtSet->IndexArtSet(set, i, &art)) {
					long values;
					if (!sAIArt->GetArtUserAttr(art, kArtFullySelected, &values) && !(values & kArtFullySelected)) {
						sAIArtSet->RemoveArtFromArtSet(set, art);
					} else {
						AIArtHandle parent = NULL;
						sAIArt->GetArtParent(art, &parent);
						if (parent != NULL && !Item_isLayer(parent)) {
							if (!sAIArt->GetArtUserAttr(parent, kArtFullySelected, &values) && (values & kArtFullySelected))
								sAIArtSet->RemoveArtFromArtSet(set, art);
						}
					}
				}
			}
		}
	}
	return set;
}

AIArtHandle ItemList_rasterize(AIArtSet artSet, AIRasterizeType type, float resolution, int antialiasing, float width, float height) {
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
 * com.scriptographer.ai.Raster nativeRasterize(int type, float resolution, int antialiasing, float width, float height)
 */

JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_ItemList_nativeRasterize(JNIEnv *env, jobject obj, jint type, jfloat resolution, jint antialiasing, jfloat width, jfloat height) {
	try {
		AIArtSet set = gEngine->convertArtSet(env, obj);
		AIArtHandle raster = ItemList_rasterize(set, (AIRasterizeType) type, resolution, antialiasing, width, height);
		if (raster != NULL) {
			// It's ok not to not pass document here, since the method calling nativeRasterize makes sure the right one is active
			return gEngine->wrapArtHandle(env, raster);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}
