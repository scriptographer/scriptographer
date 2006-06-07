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
 * $RCSfile: com_scriptographer_adm_Image.cpp,v $
 * $Author: lehni $
 * $Revision: 1.5 $
 * $Date: 2006/06/07 16:44:18 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_adm_Image.h"

/*
 * com.scriptographer.adm.Image
 */

/*
 * int nativeCreate(int width, int height, int type)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Image_nativeCreate(JNIEnv *env, jobject obj, jint width, jint height, jint type) {
	try {
		ADMImageRef image = NULL;
		switch(type) {
			case com_scriptographer_adm_Image_TYPE_RGB:
				image = sADMImage->Create(width, height, 0);
				break;
			case com_scriptographer_adm_Image_TYPE_ARGB:
				image = sADMImage->Create(width, height, kADMImageHasAlphaChannelOption);
				break;
			case com_scriptographer_adm_Image_TYPE_SCREEN:
				image = sADMImage->CreateOffscreen(width, height, 0);
				break;
			case com_scriptographer_adm_Image_TYPE_ASCREEN:
				image = sADMImage->CreateOffscreen(width, height, kADMImageHasAlphaChannelOption);
				break;
		}
		gEngine->setIntField(env, obj, gEngine->fid_Image_byteWidth, sADMImage->GetByteWidth(image));
		gEngine->setIntField(env, obj, gEngine->fid_Image_bitsPerPixel, sADMImage->GetBitsPerPixel(image));
		return (jint)image;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void nativeDestroy(int handle, int iconHandle)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Image_nativeDestroy(JNIEnv *env, jobject obj, jint handle, jint iconHandle) {
	try {
		if (handle != 0) sADMImage->Destroy((ADMImageRef) handle);
		if (iconHandle != 0) sADMIcon->Destroy((ADMIconRef) iconHandle);
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeSetPixels(int[] data, int width, int height, int byteWidth)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Image_nativeSetPixels___3IIII(JNIEnv *env, jobject obj, jintArray data, jint width, jint height, jint byteWidth) {
	try {
		ADMImageRef image = gEngine->getImageRef(env, obj);
		jint len = env->GetArrayLength(data);
		char *src = (char *)env->GetPrimitiveArrayCritical(data, 0);
		if (data == NULL) EXCEPTION_CHECK(env);
		char *dst = (char *)sADMImage->BeginBaseAddressAccess(image); 
		
		// we're copying int rgb(a) values, so *4:
		width *= 4;
		for (int y = 0; y < height; y++) {
			memcpy(dst, src, width);
			src += width;
			dst += byteWidth;
		}
		
		env->ReleasePrimitiveArrayCritical(data, src, 0);
		sADMImage->EndBaseAddressAccess(image); 
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeSetPixels(int handle, int numBytes)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Image_nativeSetPixels__II(JNIEnv *env, jobject obj, jint handle, jint numBytes) {
	try {
		ADMImageRef dstImage = gEngine->getImageRef(env, obj);
		ADMImageRef srcImage = (ADMImageRef) handle;
		char *src = (char *)sADMImage->BeginBaseAddressAccess(srcImage); 
		char *dst = (char *)sADMImage->BeginBaseAddressAccess(dstImage); 
		
		memcpy(dst, src, numBytes);
		
		sADMImage->EndBaseAddressAccess(srcImage); 
		sADMImage->EndBaseAddressAccess(dstImage); 
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeGetPixels(int[] data, int width, int height, int byteWidth)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Image_nativeGetPixels(JNIEnv *env, jobject obj, jintArray data, jint width, jint height, jint byteWidth) {
	try {
		ADMImageRef image = gEngine->getImageRef(env, obj);
		jint len = env->GetArrayLength(data);
		char *dst = (char *)env->GetPrimitiveArrayCritical(data, 0);
		if (data == NULL) EXCEPTION_CHECK(env);
		char *src = (char *)sADMImage->BeginBaseAddressAccess(image); 
		
		// we're copying int rgb(a) values, so *4:
		width *= 4;
		for (int y = 0; y < height; y++) {
			memcpy(dst, src, width);
			src += byteWidth;
			dst += width;
		}
		
		env->ReleasePrimitiveArrayCritical(data, dst, 0);
		sADMImage->EndBaseAddressAccess(image); 
	} EXCEPTION_CONVERT(env)
}

/*
 * int nativeCreateIcon()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Image_nativeCreateIcon(JNIEnv *env, jobject obj) {
	try {
		ADMImageRef image = gEngine->getImageRef(env, obj);
		return (jint)sADMIcon->CreateFromImage(image);
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * int nativeBeginDrawer()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_adm_Image_nativeBeginDrawer(JNIEnv *env, jobject obj) {
	try {
		ADMImageRef image = gEngine->getImageRef(env, obj);
		return (jint)sADMImage->BeginADMDrawer(image);
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void nativeEndDrawer()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_adm_Image_nativeEndDrawer(JNIEnv *env, jobject obj) {
	try {
		ADMImageRef image = gEngine->getImageRef(env, obj);
		sADMImage->EndADMDrawer(image);
	} EXCEPTION_CONVERT(env)
}
