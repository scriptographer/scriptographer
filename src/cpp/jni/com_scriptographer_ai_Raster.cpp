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
 * $RCSfile: com_scriptographer_ai_Raster.cpp,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:59 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_Raster.h"

/*
 * com.scriptographer.ai.Raster
 */

struct RasterData {
	AIRasterRecord info;
	AISlice slice;
	AITile tile;
	unsigned char pixel[5]; // for get / set pixel
};

RasterData *rasterGetData(JNIEnv *env, jobject raster, AIArtHandle art) {
	RasterData *data = (RasterData *)gEngine->getIntField(env, raster, gEngine->fid_Raster_rasterData);
	if (data == NULL) {
		// init a new data struct now:
		data = new RasterData;
		sAIRaster->GetRasterInfo(art, &data->info);
		
		if (data->info.bitsPerPixel == 0) { 
			// not initialized. create an empty raster with the colorModel from the document:		
			short colorModel;
			sAIDocument->GetDocumentColorModel(&colorModel);
			switch (colorModel) {
			case kDocGrayColor:
				data->info.colorSpace = kGrayColorSpace;
				data->info.bitsPerPixel = 8;
				break;
			case kDocRGBColor:
				data->info.colorSpace = kRGBColorSpace;
				data->info.bitsPerPixel = 24;
				break;
			case kDocCMYKColor:
				data->info.colorSpace = kCMYKColorSpace;
				data->info.bitsPerPixel = 32;
				break;
			}
			data->info.byteWidth = 0;
			sAIRaster->SetRasterInfo(art, &data->info);
		}
		// prepare the slices.
		// use allways 5 color components, for the maximum of 5 components in the case of 
		// a acmyk color:
		AISlice *slice = &data->slice;
		// set the slice for get/set pixel:
		slice->left = 0;
		slice->top = 0;
		slice->right = 1;
		slice->bottom = 1;
		slice->front = 0;
		slice->back = 5;

		AITile *tile = &data->tile;
		for (int i = 0; i < 5; i++) {
			tile->channelInterleave[i]=i;
		}
		tile->colBytes = 5;
		tile->rowBytes = 5;
		tile->planeBytes = 0;
		tile->bounds = *slice;
		tile->data = data->pixel;

		gEngine->setIntField(env, raster, gEngine->fid_Raster_rasterData, (jint) data);
	}
	return data;
}

/*
 * int getWidth()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Raster_getWidth(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		RasterData *data = rasterGetData(env, obj, art);
		return data->info.bounds.right - data->info.bounds.left;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void setWidth(int width)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Raster_setWidth(JNIEnv *env, jobject obj, jint width) {
	try {
		// TODO: define setWidth
	} EXCEPTION_CONVERT(env)
}

/*
 * int getHeight()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Raster_getHeight(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		RasterData *data = rasterGetData(env, obj, art);
		return data->info.bounds.bottom - data->info.bounds.top;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void setHeight(int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Raster_setHeight(JNIEnv *env, jobject obj, jint height) {
	try {
		// TODO: define setHeight
	} EXCEPTION_CONVERT(env)
}

/*
 * int getType()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Raster_getType(JNIEnv *env, jobject obj) {
	try {
		// TODO: define getType
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void setType(int type)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Raster_setType(JNIEnv *env, jobject obj, jint type) {
	try {
		// TODO: define setType
	} EXCEPTION_CONVERT(env)
}

/*
 * com.scriptographer.ai.Color getPixel(int x, int y)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Raster_getPixel(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		RasterData *data = rasterGetData(env, obj, art);

		// just get a 1 pixel big tile from the raster (this may be slow...???)
		memset(data->pixel, 0, 5);

		AISlice slice;
		slice.left = data->info.bounds.left + x;
		slice.top = data->info.bounds.top + y;
		slice.right = slice.left + 1;
		slice.bottom = slice.top + 1;
		slice.front = 0;
		slice.back = 5;

		sAIRaster->GetRasterTile(art, &slice, &data->tile, &data->slice);
		long colorSpace = data->info.colorSpace;
		unsigned char *pixel = data->pixel;
		AIReal alpha;
		if (colorSpace & kColorSpaceHasAlpha) {
			colorSpace &= ~kColorSpaceHasAlpha;
			alpha = *(pixel++) / 255.0;
		} else alpha = -1;
		AIColor col;
		switch (colorSpace) {
		case kGrayColorSpace:
			col.kind = kGrayColor;
			col.c.g.gray = (data->info.bitsPerPixel == 1) ? 
				(*(pixel++) == 0) : (255 - *(pixel++)) / 255.0;
			break;
		case kRGBColorSpace:
			col.kind = kThreeColor;
			col.c.rgb.red = *(pixel++) / 255.0;
			col.c.rgb.green = *(pixel++) / 255.0;
			col.c.rgb.blue = *(pixel++) / 255.0;
			break;
		case kCMYKColorSpace:
			col.kind = kFourColor;
			col.c.f.cyan = *(pixel++) / 255.0;
			col.c.f.magenta = *(pixel++) / 255.0;
			col.c.f.yellow = *(pixel++) / 255.0;
			col.c.f.black = *(pixel++) / 255.0;
			break;
		}
		return gEngine->convertColor(env, &col, alpha);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void setPixel(int arg1, int arg2, com.scriptographer.ai.Color arg3)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Raster_setPixel(JNIEnv *env, jobject obj, jint arg1, jint arg2, jobject arg3) {
	try {
		// TODO: define setPixel
	} EXCEPTION_CONVERT(env)
}

/*
 * void finalize()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Raster_finalize(JNIEnv *env, jobject obj) {
	try {
		RasterData *data = (RasterData *) gEngine->getIntField(env, obj, gEngine->fid_Raster_rasterData);
		if (data != NULL)
			delete data;
	} EXCEPTION_CONVERT(env)
}
