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
 * $Revision: 1.2 $
 * $Date: 2005/03/30 08:15:38 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Raster.h"

/*
 * com.scriptographer.ai.Raster
 */

struct RasterData {
	AIRasterRecord info;
	int numComponents;
	int version; // the version of the raster data, if it changed, fetch it again.
	 // for geting / seting pixels
	AISlice pixelSlice;
	AITile pixelTile;
	unsigned char pixelValues[5];
};

RasterData *rasterGetData(JNIEnv *env, jobject raster, AIArtHandle art) {
	RasterData *data = (RasterData *) gEngine->getIntField(env, raster, gEngine->fid_Raster_rasterData);
	// match against version
	int version = gEngine->getIntField(env, raster, gEngine->fid_Art_version);
	if ( data == NULL || data->version != version) {
		// init a new data struct now:
		if ( data == NULL) {
			data = new RasterData;
			
			memset(&data->info, 0, sizeof(AIRasterRecord));

			// prepare the slices.
			// use allways 5 color components, for the maximum of 5 components in the case of 
			// a acmyk color:
			AISlice *pixelSlice = &data->pixelSlice;
			// set the slice for get/set pixelValues:
			pixelSlice->left = 0;
			pixelSlice->top = 0;
			pixelSlice->right = 1;
			pixelSlice->bottom = 1;
			pixelSlice->front = 0;

			AITile *pixelTile = &data->pixelTile;
			// maximum for numComponents is 5
			for (int i = 0; i < 5; i++) {
				pixelTile->channelInterleave[i]=i;
			}
			pixelTile->planeBytes = 0;
			pixelTile->bounds = *pixelSlice;
			pixelTile->data = data->pixelValues;
		}
		
		data->version = version;

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
			sAIRaster->SetRasterInfo(art, &data->info);
		}
		
		// determine the number of components from bitsPerPixel:
		int numComponents = data->info.bitsPerPixel >= 8 ? data->info.bitsPerPixel >> 3 : 1;
		
		data->numComponents = numComponents;
		data->pixelSlice.back = numComponents;
		data->pixelTile.colBytes = numComponents;
		data->pixelTile.rowBytes = numComponents;

		gEngine->setIntField(env, raster, gEngine->fid_Raster_rasterData, (jint) data);
	}
	return data;
}

void rasterFinalize(JNIEnv *env, jobject obj) {
	RasterData *data = (RasterData *) gEngine->getIntField(env, obj, gEngine->fid_Raster_rasterData);
	if (data != NULL) {
		delete data;
		// set to null:
		gEngine->setIntField(env, obj, gEngine->fid_Raster_rasterData, (jint) NULL);
	}
}

void rasterCopyPixels(JNIEnv *env, jobject obj, jbyteArray data, jint numComponents, jint x, jint y, jint width, jint height, bool get) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		RasterData *rasterData = rasterGetData(env, obj, art);
		AISlice sliceFrom, sliceTo;
		sliceFrom.left = rasterData->info.bounds.left + x;
		sliceFrom.top = rasterData->info.bounds.top + y;
		sliceFrom.right = x + width;
		sliceFrom.bottom = y + height;
		sliceFrom.front = 0;
		sliceFrom.back = rasterData->numComponents;

		sliceTo.left = 0;
		sliceTo.top = 0;
		sliceTo.right = width;
		sliceTo.bottom = height;
		sliceTo.front = 0;
		sliceTo.back = numComponents;
		
		AITile tile;

		// TODO: handle the case where data->numComponents != numComponents? (should never be the case, maybe throw an exception if it is?)
		for (int i = 0; i < numComponents; i++)
			tile.channelInterleave[i] = i;

		tile.colBytes = numComponents;
		tile.rowBytes = width * numComponents; // rasterData->info.byteWidth
		tile.planeBytes = 0;
		tile.bounds = sliceTo;
		
		char *dst = (char *)env->GetPrimitiveArrayCritical(data, 0);
		if (dst == NULL) EXCEPTION_CHECK(env)
		tile.data = dst;
		
		if (get) sAIRaster->GetRasterTile(art, &sliceFrom, &tile, &sliceTo);
		else sAIRaster->SetRasterTile(art, &sliceFrom, &tile, &sliceTo);

		env->ReleasePrimitiveArrayCritical(data, dst, 0);
	} EXCEPTION_CONVERT(env)
}

/*
 * int nativeConvert(int type, int width, int height)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Raster_nativeConvert(JNIEnv *env, jobject obj, jint type, jint width, jint height) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		RasterData *data = rasterGetData(env, obj, art);

		if (type == -1) {
			// this is used when width and height is set...
			long colorSpace = data->info.colorSpace;
			ASBoolean alpha = (colorSpace & kColorSpaceHasAlpha || (data->info.flags & kRasterMaskImageType));
			if (alpha) colorSpace &= ~kColorSpaceHasAlpha;
			switch (colorSpace) {
			case kGrayColorSpace: 
				if (data->info.bitsPerPixel == 1) {
					if (alpha) type = kRasterizeABitmap;
					else type = kRasterizeBitmap;
				} else {
					if (alpha) type = kRasterizeAGrayscale;
					else type = kRasterizeGrayscale;
				}
				break;
			case kRGBColorSpace:
				if (alpha) type = kRasterizeARGB;
				else type = kRasterizeRGB;
				break;
			case kCMYKColorSpace:
				if (alpha) type = kRasterizeACMYK;
				else type = kRasterizeCMYK;
				break;
			}
		}
		// change the matrix during rasterizing so that no pixels get lost...
		AIRealMatrix prevMatrix;
		sAIRaster->GetRasterMatrix(art, &prevMatrix);
		AIRealMatrix matrix;
		double scale = 72.0 / 300.0;
		sAIRealMath->AIRealMatrixSetScale(&matrix, scale, scale);
		sAIRaster->SetRasterMatrix(art, &matrix);

		float scaledWidth = width, scaledHeight = height;
		if (width >= 0) scaledWidth *= scale;
		if (height >= 0) scaledHeight *= scale;

		// see wether the raster contains some data:
		if (data->info.byteWidth > 0) {
			// convert the raster by rasterizing it again and then exchange the
			// old art by the new one:
			// TODO: check wether the old art needs to be removed?
			art = artRasterize(art, (AIRasterizeType) type, 0, 0, scaledWidth, scaledHeight);
			// remove the raster info because it has changed now...
			rasterFinalize(env, obj);
		} else {
			// just set the raster info:
			AIRasterRecord *info = &data->info;
			if (type != NULL) {
				switch (type) {
				case kRasterizeBitmap:
					info->bitsPerPixel = 1;
					info->colorSpace = kGrayColorSpace;
					break;
				case kRasterizeABitmap:
					info->bitsPerPixel = 1;
					info->colorSpace = kAlphaGrayColorSpace;
					break;
				case kRasterizeGrayscale:
					info->bitsPerPixel = 8;
					info->colorSpace = kGrayColorSpace;
					break;
				case kRasterizeAGrayscale:
					info->bitsPerPixel = 16;
					info->colorSpace = kAlphaGrayColorSpace;
					break;
				case kRasterizeRGB:
					info->bitsPerPixel = 24;
					info->colorSpace = kRGBColorSpace;
					break;
				case kRasterizeARGB:
					info->bitsPerPixel = 32;
					info->colorSpace = kAlphaRGBColorSpace;
					break;
				case kRasterizeCMYK:
					info->bitsPerPixel = 32;
					info->colorSpace = kCMYKColorSpace;
					break;
				case kRasterizeACMYK:
					info->bitsPerPixel = 48;
					info->colorSpace = kAlphaCMYKColorSpace;
					break;
				}
			}
			if (width > 0) {
				info->bounds.right = info->bounds.left + width;
				if (info->bitsPerPixel == 1) 
					info->byteWidth = info->bounds.right / 8 + 1;
				else 
					info->byteWidth = info->bounds.right * (info->bitsPerPixel / 8);
			}
			if (height > 0) info->bounds.bottom = info->bounds.top + height;
			sAIRaster->SetRasterInfo(art, info);
		}
		// set the old matrix after rendering:
		sAIRaster->SetRasterMatrix(art, &prevMatrix);
		return (jint) art;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void finalize()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Raster_finalize(JNIEnv *env, jobject obj) {
	try {
		rasterFinalize(env, obj);
	} EXCEPTION_CONVERT(env)
}

/*
 * java.awt.Dimension getSize()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Raster_getSize(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		RasterData *data = rasterGetData(env, obj, art);
		return gEngine->convertDimension(env, data->info.bounds.right - data->info.bounds.left, data->info.bounds.bottom - data->info.bounds.top);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

int rasterGetType(AIRasterRecord *info) {
	long colorSpace = info->colorSpace;
	ASBoolean alpha = (colorSpace & kColorSpaceHasAlpha || (info->flags & kRasterMaskImageType));
	if (alpha) colorSpace &= ~kColorSpaceHasAlpha;
	switch (colorSpace) {
	case kGrayColorSpace: 
		if (info->bitsPerPixel == 1) {
			if (alpha) return kRasterizeABitmap;
			else return kRasterizeBitmap;
		} else {
			if (alpha) return kRasterizeAGrayscale;
			else return kRasterizeGrayscale;
		}
	case kRGBColorSpace:
		if (alpha) return kRasterizeARGB;
		else return kRasterizeRGB;
	case kCMYKColorSpace:
		if (alpha) return kRasterizeACMYK;
		else return kRasterizeCMYK;
	}
	return kRasterizeRGB;
}

/*
 * int getType()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Raster_getType(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		RasterData *data = rasterGetData(env, obj, art);
		return rasterGetType(&data->info);
	} EXCEPTION_CONVERT(env)
	return kRasterizeRGB;
}

/*
 * com.scriptographer.ai.Color getPixel(int x, int y)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Raster_getPixel(JNIEnv *env, jobject obj, jint x, jint y) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		RasterData *data = rasterGetData(env, obj, art);

		// just get a 1 pixelValues big tile from the raster

		memset(data->pixelValues, 0, data->numComponents);

		AISlice slice;
		slice.left = data->info.bounds.left + x;
		slice.top = data->info.bounds.top + y;
		slice.right = slice.left + 1;
		slice.bottom = slice.top + 1;
		slice.front = 0;
		slice.back = data->numComponents;

		sAIRaster->GetRasterTile(art, &slice, &data->pixelTile, &data->pixelSlice);
		
		long colorSpace = data->info.colorSpace;
		unsigned char *pixelValues = data->pixelValues;
		AIReal alpha;
		if (colorSpace & kColorSpaceHasAlpha) {
			colorSpace &= ~kColorSpaceHasAlpha;
			alpha = *(pixelValues++) / 255.0;
		} else alpha = -1;
		AIColor col;
		switch (colorSpace) {
		case kGrayColorSpace:
			col.kind = kGrayColor;
			col.c.g.gray = (data->info.bitsPerPixel == 1) ? 
				*pixelValues == 0 : 
				(255 - *pixelValues) / 255.0; // flip the gray values, in order to simulate AIColor gray
			break;
		case kRGBColorSpace:
			col.kind = kThreeColor;
			col.c.rgb.red = *(pixelValues++) / 255.0;
			col.c.rgb.green = *(pixelValues++) / 255.0;
			col.c.rgb.blue = *pixelValues / 255.0;
			break;
		case kCMYKColorSpace:
			col.kind = kFourColor;
			col.c.f.cyan = *(pixelValues++) / 255.0;
			col.c.f.magenta = *(pixelValues++) / 255.0;
			col.c.f.yellow = *(pixelValues++) / 255.0;
			col.c.f.black = *pixelValues / 255.0;
			break;
		}
		return gEngine->convertColor(env, &col, alpha);
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void setPixel(int x, int y, com.scriptographer.ai.Color color)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Raster_setPixel(JNIEnv *env, jobject obj, jint x, jint y, jobject color) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		RasterData *data = rasterGetData(env, obj, art);
		AIColor col;
		AIReal alpha;
		gEngine->convertColor(env, color, &col, &alpha);

		memset(data->pixelValues, 0, data->numComponents);
		unsigned char *pixelValues = data->pixelValues;

		long colorSpace = data->info.colorSpace;

		if (colorSpace & kColorSpaceHasAlpha) {
			if (alpha < 0) alpha = 1;
			colorSpace &= ~kColorSpaceHasAlpha;
			*(pixelValues++) = int(alpha * 255 + 0.5);
		}
		switch (colorSpace) {
		case kGrayColorSpace:
			if (col.kind != kGrayColor)
				gEngine->convertColor(&col, kAIGrayColorSpace, &col);
				*pixelValues = (data->info.bitsPerPixel == 1) ?
					((col.c.g.gray >= 0.5) ? 0 : 128) :
					255 - int(col.c.g.gray * 255 + 0.5); // flip the gray values, in order to simulate AIColor gray
			break;
		case kRGBColorSpace:
			if (col.kind != kThreeColor)
				gEngine->convertColor(&col, kAIRGBColorSpace, &col);
			*(pixelValues++) = int(col.c.rgb.red * 255 + 0.5);
			*(pixelValues++) = int(col.c.rgb.green * 255 + 0.5);
			*pixelValues = int(col.c.rgb.blue * 255 + 0.5);
			break;
		case kCMYKColorSpace:
			if (col.kind != kFourColor)
				gEngine->convertColor(&col, kAICMYKColorSpace, &col);
			*(pixelValues++) = int(col.c.f.cyan * 255 + 0.5);
			*(pixelValues++) = int(col.c.f.magenta * 255 + 0.5);
			*(pixelValues++) = int(col.c.f.yellow * 255 + 0.5);
			*pixelValues = int(col.c.f.black * 255 + 0.5);
			break;
		}
		
		AISlice slice;
		slice.left = data->info.bounds.left + x;
		slice.top = data->info.bounds.top + y;
		slice.right = slice.left + 1;
		slice.bottom = slice.top + 1;
		slice.front = 0;
		slice.back = data->numComponents;

		sAIRaster->SetRasterTile(art, &slice, &data->pixelTile, &data->pixelSlice);
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeGetPixels(byte[] data, int numComponents, int x, int y, int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Raster_nativeGetPixels(JNIEnv *env, jobject obj, jbyteArray data, jint numComponents, jint x, jint y, jint width, jint height) {
	rasterCopyPixels(env, obj, data, numComponents, x, y, width, height, true);
}

/*
 * void nativeSetPixels(byte[] data, int numComponents, int x, int y, int width, int height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Raster_nativeSetPixels(JNIEnv *env, jobject obj, jbyteArray data, jint numComponents, jint x, jint y, jint width, jint height) {
	rasterCopyPixels(env, obj, data, numComponents, x, y, width, height, false);
}
