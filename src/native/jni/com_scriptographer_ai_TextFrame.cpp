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

#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_TextFrame.h"

/*
 * com.scriptographer.ai.TextFrame
 */
 
using namespace ATE;

// TextFrame AIDocumenHandle activation:
// TextFrames seemd to need the document be active for getting and setting story related states.
// Everything else seems to be not depending on the documents at all (ATE related)

/*
 * short getOrientation()
 */
JNIEXPORT jshort JNICALL Java_com_scriptographer_ai_TextFrame_getOrientation(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle text = gEngine->getArtHandle(env, obj);
		AITextOrientation orient;
		if (!sAITextFrame->GetOrientation(text, &orient))
			return (jint) orient;
	} EXCEPTION_CONVERT(env);
	return -1;
}

/*
 * void setOrientation(short orient)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextFrame_setOrientation(JNIEnv *env, jobject obj, jshort orient) {
	try {
		AIArtHandle text = gEngine->getArtHandle(env, obj, true);
		sAITextFrame->SetOrientation(text, (AITextOrientation) orient);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.Art nativeCreateOutline()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_TextFrame_nativeCreateOutline(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle text = gEngine->getArtHandle(env, obj, true);
		AIArtHandle outline;
		if (!sAITextFrame->CreateOutline(text, &outline))
			return gEngine->wrapArtHandle(env, outline);
	} EXCEPTION_CONVERT(env);
	return NULL;
}


/*
 * boolean link(com.scriptographer.ai.TextFrame text)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_TextFrame_link(JNIEnv *env, jobject obj, jobject text) {
	try {
		AIArtHandle text1 = gEngine->getArtHandle(env, obj, true);
		AIArtHandle text2 = gEngine->getArtHandle(env, text);
		if (text2 != NULL && !sAITextFrame->Link(text1, text2))
			return true;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean nativeUnlink(boolean before, boolean after)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_TextFrame_nativeUnlink(JNIEnv *env, jobject obj, jboolean before, jboolean after) {
	try {
		AIArtHandle text = gEngine->getArtHandle(env, obj, true);
		if (!sAITextFrame->Unlink(text, before, after))
			return true;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean isLinked()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_TextFrame_isLinked(JNIEnv *env, jobject obj) {
	try {
		// TextFrames need document be active for getting story related states too
		AIArtHandle text = gEngine->getArtHandle(env, obj, true);
		ATEBool8 linked;
		if (!sAITextFrame->PartOfLinkedText(text, &linked))
			return linked;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * int getStoryIndex()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextFrame_getStoryIndex(JNIEnv *env, jobject obj) {
	try {
		// TextFrames need document be active for getting story related states too
		AIArtHandle text = gEngine->getArtHandle(env, obj, true);
		long index;
		if (!sAITextFrame->GetStoryIndex(text, &index))
			return index;
	} EXCEPTION_CONVERT(env);
	return -1;
}

/*
 * int getIndex()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextFrame_getIndex(JNIEnv *env, jobject obj) {
	try {
		// TextFrames need document be active for getting too
		AIArtHandle text = gEngine->getArtHandle(env, obj, true);
		long index;
		if (!sAITextFrame->GetFrameIndex(text, &index))
			return index;
	} EXCEPTION_CONVERT(env);
	return -1;
}

/*
 * com.scriptographer.ai.TextRange getSelection()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_TextFrame_getSelection(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle text = gEngine->getArtHandle(env, obj);
		TextRangesRef ranges;
		if (!sAITextFrame->GetATETextSelection(text, &ranges))
			return TextRange_convertTextRanges(env, ranges);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * int nativeGetRange(boolean bIncludeOverflow)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextFrame_nativeGetRange(JNIEnv *env, jobject obj, jboolean bIncludeOverflow) {
	try {
		// activate document so that text flow gets suspended as soon as the first range is accessed
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj, true);
		TextRangeRef range = NULL;
		sTextFrame->GetTextRange(frame, bIncludeOverflow, &range);
		sTextFrame->Release(frame);
		return (jint) range;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * float getSpacing()
 */
JNIEXPORT jfloat JNICALL Java_com_scriptographer_ai_TextFrame_getSpacing(JNIEnv *env, jobject obj) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		ASReal spacing = 0;
		sTextFrame->GetSpacing(frame, &spacing);
		sTextFrame->Release(frame);
		return spacing;
	} EXCEPTION_CONVERT(env);
}

/*
 * void setSpacing(float spacing)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextFrame_setSpacing(JNIEnv *env, jobject obj, jfloat spacing) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj, true);
		sTextFrame->SetSpacing(frame, spacing);
		sTextFrame->Release(frame);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean getOpticalAlignment()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_TextFrame_getOpticalAlignment(JNIEnv *env, jobject obj) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj);
		ATEBool8 active = false;
		sTextFrame->GetOpticalAlignment(frame, &active);
		sTextFrame->Release(frame);
		return active;
	} EXCEPTION_CONVERT(env);
}

/*
 * void setOpticalAlignment(boolean active)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextFrame_setOpticalAlignment(JNIEnv *env, jobject obj, jboolean active) {
	try {
		TextFrameRef frame = gEngine->getTextFrameRef(env, obj, true);
		sTextFrame->SetOpticalAlignment(frame, active);
		sTextFrame->Release(frame);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean equals(java.lang.Object text)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_TextFrame_equals(JNIEnv *env, jobject obj, jobject text) {
	try {
		ATEBool8 ret = false;
		if (env->IsInstanceOf(text, gEngine->cls_ai_TextFrame)) {
			TextFrameRef frame1 = gEngine->getTextFrameRef(env, obj);
			TextFrameRef frame2 = gEngine->getTextFrameRef(env, text);
			if (frame2 != NULL) {
				sTextFrame->IsEqual(frame1, frame2, &ret);
				sTextFrame->Release(frame2);
			}
			sTextFrame->Release(frame1);
		}
		return ret;
	} EXCEPTION_CONVERT(env);
}
