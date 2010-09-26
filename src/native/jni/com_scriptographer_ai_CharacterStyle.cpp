/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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
 */

#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_CharacterStyle.h"

/*
 * com.scriptographer.ai.CharacterStyle
 */

using namespace ATE;

#define CHARACTERSTYLE_GET(NAME, TYPE, CLASS, JTYPE) \
	try { \
		CharFeaturesRef features = gEngine->getCharFeaturesHandle(env, obj); \
		ATEBool8 isAssigned; \
		TYPE value; \
		if (!sCharFeatures->Get##NAME(features, &isAssigned, &value) && isAssigned) \
			return gEngine->newObject(env, gEngine->cls_##CLASS, gEngine->cid_##CLASS, (JTYPE) value); \
	} EXCEPTION_CONVERT(env); \
	return NULL;

#define CHARACTERSTYLE_SET(NAME, TYPE, METHOD_TYPE, METHOD_NAME) \
	try { \
		CharFeaturesRef features = gEngine->getCharFeaturesHandle(env, obj); \
		ASErr err; \
		if (value == NULL) \
			err = sCharFeatures->Clear##NAME(features); \
		else \
			err = sCharFeatures->Set##NAME(features, (TYPE) gEngine->call##METHOD_TYPE##Method(env, value, gEngine->METHOD_NAME)); \
		if (!err) \
			gEngine->callVoidMethod(env, obj, gEngine->mid_ai_CharacterStyle_markSetStyle); \
	} EXCEPTION_CONVERT(env);

#define	CHARACTERSTYLE_GET_FLOAT(NAME) \
	CHARACTERSTYLE_GET(NAME, ASReal, Float, jfloat)

#define CHARACTERSTYLE_SET_FLOAT(NAME) \
	CHARACTERSTYLE_SET(NAME, ASReal, Float, mid_Number_floatValue)

#define CHARACTERSTYLE_GET_BOOLEAN(NAME) \
	CHARACTERSTYLE_GET(NAME, ATEBool8, Boolean, jboolean)

#define CHARACTERSTYLE_SET_BOOLEAN(NAME) \
	CHARACTERSTYLE_SET(NAME, ATEBool8, Boolean, mid_Boolean_booleanValue)

#define CHARACTERSTYLE_GET_INTEGER(NAME) \
	CHARACTERSTYLE_GET(NAME, ASInt32, Integer, jint)

#define CHARACTERSTYLE_SET_INTEGER(NAME) \
	CHARACTERSTYLE_SET(NAME, ASInt32, Int, mid_Number_intValue)

#define CHARACTERSTYLE_GET_ENUM(NAME) \
	CHARACTERSTYLE_GET(NAME, NAME, Integer, jint)

#define CHARACTERSTYLE_SET_ENUM(NAME) \
	CHARACTERSTYLE_SET(NAME, NAME, Int, mid_Number_intValue)

/*
 * int nativeCreate()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeCreate(JNIEnv *env, jclass cls) {
	try {
		CharFeaturesRef features;
//		if (!sCharFeatures->Initialize(&features)) {
		if (!sAIATECurrentTextFeatures->GetCurrentCharFeature(&features)) {
			// add reference to the handle, which will be released in CharacterStyle.finalize
			sCharFeatures->AddRef(features);
			return (jint) features;
		}
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeClone()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeClone(JNIEnv *env, jobject obj) {
	try {
		CharFeaturesRef features = gEngine->getCharFeaturesHandle(env, obj);
		CharFeaturesRef clone;
		if (!sCharFeatures->Clone(features, &clone)) {
			// add reference to the handle, which will be released in CharacterStyle.finalize
			sCharFeatures->AddRef(clone);
			return (jint) clone;
		}
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void nativeGet(int handle, int docHandle)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeGet(JNIEnv *env, jobject obj, jint handle, jint docHandle) {
	try {
		AIPathStyle style;
		AIPathStyleMap map;

		if (!sAIATEPaint->GetAIPathStyleAndMap((CharFeaturesRef) handle, &style, &map))
			PathStyle_init(env, obj, &style, &map);
		
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSet(int handle, int docHandle,
				com.scriptographer.ai.Color fillColor, boolean hasFillColor, short fillOverprint,
				com.scriptographer.ai.Color strokeColor, boolean hasStrokeColor, short strokeOverprint, float strokeWidth,
				int strokeCap, int strokeJoin, float miterLimit,
				float dashOffset, float[] dashArray,
				short clip, short lockClip, int windingRule, float resolution)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeSet(JNIEnv *env, jobject obj, jint handle, jint docHandle, jobject fillColor, jboolean hasFillColor, jshort fillOverprint, jobject strokeColor, jboolean hasStrokeColor, jshort strokeOverprint, jfloat strokeWidth, jint strokeCap, jint strokeJoin, jfloat miterLimit, jfloat dashOffset, jfloatArray dashArray, jshort clip, jshort lockClip, jint windingRule, jfloat resolution) {
	try {
		AIPathStyle style;
		AIPathStyleMap map;
		PathStyle_convertPathStyle(env, &style, &map,
				fillColor, hasFillColor, fillOverprint,
				strokeColor, hasStrokeColor, strokeOverprint, strokeWidth,
				strokeCap, strokeJoin, miterLimit,
				dashOffset, dashArray,
				clip, lockClip, windingRule, resolution);
		sAIATEPaint->GetCharFeatures(&style, &map, (CharFeaturesRef) handle);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetStyle(int handle, int docHandle, int rangeHandle)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeSetStyle(JNIEnv *env, jobject obj, jint handle, jint docHandle, jint rangeHandle) {
	try {
		Document_activate((AIDocumentHandle) docHandle);
		sTextRange->SetLocalCharFeatures((TextRangeRef) rangeHandle, (CharFeaturesRef) handle);
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeRelease()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeRelease(JNIEnv *env, jobject obj, jint handle) {
	try {
		if (handle)
			sCharFeatures->Release((CharFeaturesRef) handle);
	} EXCEPTION_CONVERT(env);
}

/*
 * int nativeGetFont()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeGetFont(JNIEnv *env, jobject obj) {
	try {
		CharFeaturesRef features = gEngine->getCharFeaturesHandle(env, obj);
		ATEBool8 isAssigned;
		FontRef value;
		AIFontKey font;
		if (!sCharFeatures->GetFont(features, &isAssigned, &value) && isAssigned &&
			!sAIFont->FontKeyFromFont(value, &font)) {
			return (jint) font;
		}
	} EXCEPTION_CONVERT(env);
	return -1;
}

/*
 * void nativeSetFont(int value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeSetFont(JNIEnv *env, jobject obj, jint value) {
	try {
		CharFeaturesRef features = gEngine->getCharFeaturesHandle(env, obj);
		ASErr err;
		FontRef font;
		if (value == -1)
			err = sCharFeatures->ClearFont(features);
		else if (!sAIFont->FontFromFontKey((AIFontKey) value, &font))
			err = sCharFeatures->SetFont(features, font);
		if (!err)
			gEngine->callVoidMethod(env, obj, gEngine->mid_ai_CharacterStyle_markSetStyle);
	} EXCEPTION_CONVERT(env);
}

/*
 * java.lang.Float getFontSize()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getFontSize(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_FLOAT(FontSize)
}

/*
 * void setFontSize(java.lang.Float value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setFontSize(JNIEnv *env, jobject obj, jobject value) {
	//	CHARACTERSTYLE_SET_FLOAT(FontSize), with added check of size for 0
	try {
		CharFeaturesRef features = gEngine->getCharFeaturesHandle(env, obj);
		ASErr err;
		if (value == NULL) {
			err = sCharFeatures->ClearFontSize(features);
		} else {
			ASReal size = (ASReal) gEngine->callFloatMethod(env, value, gEngine->mid_Number_floatValue);
			if (size <= 0)
				size = 0.1;
			err = sCharFeatures->SetFontSize(features, size);
		}
		if (!err)
			gEngine->callVoidMethod(env, obj, gEngine->mid_ai_CharacterStyle_markSetStyle);
	} EXCEPTION_CONVERT(env);
}

/*
 * java.lang.Float getHorizontalScale()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getHorizontalScale(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_FLOAT(HorizontalScale)
}

/*
 * void setHorizontalScale(java.lang.Float value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setHorizontalScale(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_FLOAT(HorizontalScale)
}

/*
 * java.lang.Float getVerticalScale()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getVerticalScale(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_FLOAT(VerticalScale)
}

/*
 * void setVerticalScale(java.lang.Float value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setVerticalScale(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_FLOAT(VerticalScale)
}

/*
 * java.lang.Boolean getAutoLeading()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getAutoLeading(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_BOOLEAN(AutoLeading)
}

/*
 * void nativeSetAutoLeading(java.lang.Boolean value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeSetAutoLeading(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_BOOLEAN(AutoLeading)
}

/*
 * java.lang.Float getLeading()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getLeading(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_FLOAT(Leading)
}

/*
 * void nativeSetLeading(java.lang.Float value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeSetLeading(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_FLOAT(Leading)
}

/*
 * java.lang.Integer getTracking()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getTracking(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_INTEGER(Tracking)
}

/*
 * void setTracking(java.lang.Integer value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setTracking(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_INTEGER(Tracking)
}

/*
 * java.lang.Float getBaselineShift()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getBaselineShift(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_FLOAT(BaselineShift)
}

/*
 * void setBaselineShift(java.lang.Float value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setBaselineShift(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_FLOAT(BaselineShift)
}

/*
 * java.lang.Float getRotation()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getRotation(JNIEnv *env, jobject obj) {
//	CHARACTERSTYLE_GET_FLOAT(CharacterRotation)
	try {
		CharFeaturesRef features = gEngine->getCharFeaturesHandle(env, obj);
		ATEBool8 isAssigned;
		AIReal value;
		if (!sCharFeatures->GetCharacterRotation(features, &isAssigned, &value) && isAssigned) {
			// Convert degrees to radians
			value = value * PI / 180.0;
			return gEngine->newObject(env, gEngine->cls_Float, gEngine->cid_Float, (jfloat) value);
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setRotation(java.lang.Float value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setRotation(JNIEnv *env, jobject obj, jobject value) {
	try {
		CharFeaturesRef features = gEngine->getCharFeaturesHandle(env, obj);
		ASErr err;
		if (value == NULL) {
			err = sCharFeatures->ClearCharacterRotation(features);
		} else {
			jfloat val = gEngine->callFloatMethod(env, value, gEngine->mid_Number_floatValue);
			// Convert radians to degrees
			val = val / 180.0 * PI;
			err = sCharFeatures->SetCharacterRotation(features, (ASReal) val);
		}
		if (!err)
			gEngine->callVoidMethod(env, obj, gEngine->mid_ai_CharacterStyle_markSetStyle);
	} EXCEPTION_CONVERT(env);

}

/*
 * java.lang.Integer nativeGetKerning(int rangeHandle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeGetKerning(JNIEnv *env, jobject obj, jint rangeHandle) {
	ASInt32 kerning = 0;
	try {
		TextRangeRef range = (TextRangeRef) rangeHandle;
		StoryRef story;
		if (!sTextRange->GetStory(range, &story)) {
			ASInt32 start;
			if (!sTextRange->GetStart(range, &start)) {
				AutoKernType type;
				sStory->GetModelKernAtChar(story, start, &kerning, &type);
			}
			sStory->Release(story);
		}
	} EXCEPTION_CONVERT(env);
	return kerning;
}

/*
 * void nativeSetKerning(int rangeHandle, int kerning)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeSetKerning(JNIEnv *env, jobject obj, jint rangeHandle, jint kerning) {
	try {
		TextRangeRef range = (TextRangeRef) rangeHandle;
		StoryRef story;
		if (!sTextRange->GetStory(range, &story)) {
			ASInt32 start, end;
			if (!sTextRange->GetStart(range, &start) && !sTextRange->GetEnd(range, &end)) {
				for (int i = start; i < end; i++)
					sStory->SetKernAtChar(story, i, kerning);
			}
			sStory->Release(story);
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * java.lang.Integer nativeGetKerningType()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeGetKerningType(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_ENUM(AutoKernType)
}

/*
 * void nativeSetKerningType(java.lang.Integer value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeSetKerningType(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_ENUM(AutoKernType)
}

/*
 * java.lang.Integer nativeGetCapitalization()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeGetCapitalization(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_ENUM(FontCapsOption)
}

/*
 * void nativeSetCapitalization(java.lang.Integer value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeSetCapitalization(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_ENUM(FontCapsOption)
}

/*
 * java.lang.Integer nativeGetBaselineOption()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeGetBaselineOption(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_ENUM(FontBaselineOption)
}

/*
 * void nativeSetBaselineOption(java.lang.Integer value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeSetBaselineOption(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_ENUM(FontBaselineOption)
}

/*
 * java.lang.Integer nativeGetOpenTypePosition()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeGetOpenTypePosition(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_ENUM(FontOpenTypePositionOption)
}

/*
 * void nativeSetOpenTypePosition(java.lang.Integer value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeSetOpenTypePosition(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_ENUM(FontOpenTypePositionOption)
}

/*
 * java.lang.Integer nativeGetStrikethroughPosition()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeGetStrikethroughPosition(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_ENUM(StrikethroughPosition)
}

/*
 * void nativeSetStrikethroughPosition(java.lang.Integer value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeSetStrikethroughPosition(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_ENUM(StrikethroughPosition)
}

/*
 * java.lang.Integer nativeGetUnderlinePosition()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeGetUnderlinePosition(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_ENUM(UnderlinePosition)
}

/*
 * void nativeSetUnderlinePosition(java.lang.Integer value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeSetUnderlinePosition(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_ENUM(UnderlinePosition)
}

/*
 * java.lang.Float getUnderlineOffset()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getUnderlineOffset(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_FLOAT(UnderlineOffset)
}

/*
 * void setUnderlineOffset(java.lang.Float value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setUnderlineOffset(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_FLOAT(UnderlineOffset)
}

/*
 * java.lang.Boolean getLigature()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getLigature(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_BOOLEAN(Ligature)
}

/*
 * void setLigature(java.lang.Boolean value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setLigature(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_BOOLEAN(Ligature)
}

/*
 * java.lang.Boolean getDiscretionaryLigature()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getDiscretionaryLigature(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_BOOLEAN(DiscretionaryLigatures)
}

/*
 * void setDiscretionaryLigature(java.lang.Boolean value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setDiscretionaryLigature(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_BOOLEAN(DiscretionaryLigatures)
}

/*
 * java.lang.Boolean getContextualLigature()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getContextualLigature(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_BOOLEAN(ContextualLigatures)
}

/*
 * void setContextualLigature(java.lang.Boolean value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setContextualLigature(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_BOOLEAN(ContextualLigatures)
}

/*
 * java.lang.Boolean getAlternateLigatures()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getAlternateLigatures(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_BOOLEAN(AlternateLigatures)
}

/*
 * void setAlternateLigatures(java.lang.Boolean value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setAlternateLigatures(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_BOOLEAN(AlternateLigatures)
}

/*
 * java.lang.Boolean getOldStyle()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getOldStyle(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_BOOLEAN(OldStyle)
}

/*
 * void setOldStyle(java.lang.Boolean value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setOldStyle(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_BOOLEAN(OldStyle)
}

/*
 * java.lang.Boolean getFractions()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getFractions(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_BOOLEAN(Fractions)
}

/*
 * void setFractions(java.lang.Boolean value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setFractions(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_BOOLEAN(Fractions)
}

/*
 * java.lang.Boolean getOrdinals()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getOrdinals(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_BOOLEAN(Ordinals)
}

/*
 * void setOrdinals(java.lang.Boolean value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setOrdinals(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_BOOLEAN(Ordinals)
}

/*
 * java.lang.Boolean GetSwash()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getSwash(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_BOOLEAN(Swash)
}

/*
 * void setSwash(java.lang.Boolean value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setSwash(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_BOOLEAN(Swash)
}

/*
 * java.lang.Boolean getTitling()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getTitling(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_BOOLEAN(Titling)
}

/*
 * void setTitling(java.lang.Boolean value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setTitling(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_BOOLEAN(Titling)
}

/*
 * java.lang.Boolean getConnectionForms()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getConnectionForms(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_BOOLEAN(ConnectionForms)
}

/*
 * void setConnectionForms(java.lang.Boolean value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setConnectionForms(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_BOOLEAN(ConnectionForms)
}

/*
 * java.lang.Boolean getStylisticAlternates()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getStylisticAlternates(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_BOOLEAN(StylisticAlternates)
}

/*
 * void setStylisticAlternates(java.lang.Boolean value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setStylisticAlternates(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_BOOLEAN(StylisticAlternates)
}

/*
 * java.lang.Boolean getOrnaments()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getOrnaments(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_BOOLEAN(Ornaments)
}

/*
 * void setOrnaments(java.lang.Boolean value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setOrnaments(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_BOOLEAN(Ornaments)
}

/*
 * java.lang.Integer nativeGetFigureStyle()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeGetFigureStyle(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_ENUM(FigureStyle)
}

/*
 * void nativeSetFigureStyle(java.lang.Integer value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_nativeSetFigureStyle(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_ENUM(FigureStyle)
}

/*
 * java.lang.Boolean getNoBreak()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_CharacterStyle_getNoBreak(JNIEnv *env, jobject obj) {
	CHARACTERSTYLE_GET_BOOLEAN(NoBreak)
}

/*
 * void setNoBreak(java.lang.Boolean value)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_CharacterStyle_setNoBreak(JNIEnv *env, jobject obj, jobject value) {
	CHARACTERSTYLE_SET_BOOLEAN(NoBreak)
}
