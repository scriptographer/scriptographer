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
#include "com_scriptographer_ai_TextRange.h"

/*
 * com.scriptographer.ai.TextRange
 */

using namespace ATE;

class GlyphRun {

private:
	int m_size;
	ASRealMatrix m_matrix;
	ITextFrame m_textFrame;
	ASRealPoint *m_origins;
	ASUnicode *m_content;

public:
	GlyphRun(IGlyphRun run, int start, int end) {
		m_matrix = run.GetMatrix();
		// Determine TextFrame through TextLine:
		m_textFrame = run.GetTextLine().GetTextFrame();
		IArrayRealPoint origins = run.GetOrigins();
		m_size = end - start;
		m_origins = new ASRealPoint[m_size];
		for (int i = start; i < end; i++) {
			m_origins[i - start] = origins.Item(i);
		}
		// content
		ASInt32 size = run.GetCharacterCount();
		ASUnicode *text = new ASUnicode[size];
		size = run.GetContents(text, size);
		m_content = new ASUnicode[m_size];
		memcpy(m_content, &text[start], m_size * sizeof(ASUnicode));
		delete text;
	}

	~GlyphRun() {
		delete m_origins;
		delete m_content;
	}

	inline int size() {
		return m_size;
	}

	ASRealMatrix getMatrix() {
		// From the SDK:
		// The matrix returned specify the full transformation of the given run.
		// You need to transform the origin by IGlyphRun::GetOrigins() and
		// concat with ITextFrame::GetMatrix() in order to get the location of the glyphs.
		ASRealMatrix textMatrix = m_textFrame.GetMatrix();
		ASRealMatrix matrix;
		sAIRealMath->AIRealMatrixConcat(&m_matrix, &textMatrix, &matrix);
		sAIHardSoft->AIRealMatrixSoften(&matrix);
		// Weird... Is ATE upside down? This seems to help, but why?
		matrix.ty *= -1;
		sAIRealMath->AIRealMatrixConcatScale(&matrix, 1, -1);
		return matrix;
	}

	inline ASRealPoint *getOrigins() {
		return m_origins;
	}

	inline ASUnicode *getContent() {
		return m_content;
	}
};

class GlyphRuns {

private:

	Array<GlyphRun *> m_runs;
	int m_size;
	int m_start;
	int m_end;
	int m_index;

	GlyphRuns(int start, int end) : m_start(start), m_end(end), m_index(0) {
		m_size = end - start;
	}

	bool add(IGlyphRun run) {
		int size = run.GetSize();
		int start = m_index == 0 ? m_start : 0;
		bool last = m_index + size >= m_end;
		int end = last ? m_end - m_start - m_index : size;
		m_runs.add(new GlyphRun(run, start, end));
		m_index += size;
		return !last;
	}

	inline GlyphRun *get(int index) {
		return m_runs.get(index);
	}

public:

	jobjectArray getOrigins(JNIEnv *env) {
		int count = m_end - m_start, index = 0;
		jobjectArray array = env->NewObjectArray(count, gEngine->cls_Point, NULL);
		for (int i = 0; i < m_runs.size(); i++) {
			GlyphRun *run = get(i);
			ASRealPoint *origins = run->getOrigins();
			ASRealMatrix matrix = run->getMatrix();
			for (int j = 0; j < run->size(); j++) {
				ASRealPoint pt = origins[j];
				sAIRealMath->AIRealMatrixXformPoint(&matrix, &pt, &pt);
				env->SetObjectArrayElement(array, index++, gEngine->convertPoint(env, &pt));
			}
		}
		return array;
	}

	jobjectArray getTransformations(JNIEnv *env) {
		int count = m_end - m_start, index = 0;
		jobjectArray array = env->NewObjectArray(count, gEngine->cls_Matrix, NULL);
		for (int i = 0; i < m_runs.size(); i++) {
			GlyphRun *run = get(i);
			ASRealPoint *origins = run->getOrigins();
			ASRealMatrix matrix = run->getMatrix();
			for (int j = 0; j < run->size(); j++) {
				ASRealPoint pt = origins[j];
				ASRealMatrix glyphMatrix = matrix;
				sAIRealMath->AIRealMatrixConcatTranslate(&glyphMatrix, pt.h, pt.v);
				env->SetObjectArrayElement(array, index++, gEngine->convertMatrix(env, &glyphMatrix));
			}
		}
		return array;
	}

	jstring getContent(JNIEnv *env) {
		ASUnicode *text = new ASUnicode[m_size];
		int index = 0;
		for (int i = 0; i < m_runs.size(); i++) {
			GlyphRun *run = get(i);
			memcpy(&text[index], run->getContent(), run->size() * sizeof(ASUnicode));
			index += run->size();
		}
		return env->NewString(text, index);
	}

	static int getIndex(ITextRange range, int charIndex) {
		// Count glyphs until pos. There is a bug in glyphRun.GetCharacterSize()
		// and glyphRun.GetContents(), so we cannot count on these.
		// They sometimes contain chars that are in the next run or contain chars
		// from the previous ones....
		// So let's do it the hard way and count only on GetSingleGlyphInRange
		// TODO: cash these results in an int table!
		// IDEA: cash it in the Story of the range, as a lookup table
		// char-index -> glyph-index
		int glyphPos = 0;
		int scanPos = range.GetStart();
		while (scanPos < charIndex) {
			int step = 1;
			// Now step through the glyphrun and find the position.
			// There is a way to discover ligatures: the TextRange's GetSingleGlyphInRange
			// only returns if the length is set to the amount of chars that produce a ligature
			// otherwise it fails. So we can test....
			// TODO: determine maximum ligature size. assumption is 3 for now...
			ATEGlyphID id;
			for (; step <= 3; step++) {
				// First set the text range of the glpyhrun to test GetSingleGlyphInRange on
				range.SetRange(scanPos, scanPos + step);
				if (range.GetSingleGlyphInRange(&id)) // found a full glyph
					break;
			}
			scanPos += step;
			// Glyph runs do not count paragraph end chars, so don't count them here either.
			if (range.GetCharacterType() != kParagraphEndChar)
				glyphPos++;
			
		}
		if (scanPos > charIndex)
			glyphPos--;
		return glyphPos;
	}

	static GlyphRuns *get(JNIEnv *env, jobject obj, TextRangeRef rangeRef) {
		GlyphRuns *glyphRuns = (GlyphRuns *) gEngine->getIntField(env, obj, gEngine->fid_TextRange_glyphRuns);
		// Use the cached result from last glyphRuns search:
		if (glyphRuns != NULL)
			return glyphRuns;
		// long t = gEngine->getNanoTime();
		// Use the C++ wrappers here for easier handling of ref madness
		ITextRange range(rangeRef);
		// Do not release rangeRef!
		sTextRange->AddRef(rangeRef);
		ITextFramesIterator frames = range.GetTextFramesIterator();
		if (frames.IsNotDone()) {
			// Only use the first frame in the range, as we look at the glyph at start
			ITextFrame frame = frames.Item();
			ITextRange frameRange = frame.GetTextRange();
			// Now see where the frame range starts. that's where glyph index 0 of the frame is
			
			int glyphStart = GlyphRuns::getIndex(frameRange, range.GetStart());
			int glyphEnd = glyphStart + GlyphRuns::getIndex(range, range.GetEnd());
			
			ASInt32 runStart = 0;
			ITextLinesIterator lines = frame.GetTextLinesIterator();
			while (lines.IsNotDone()) {
				IGlyphRunsIterator runs = lines.Item().GetGlyphRunsIterator();
				while (runs.IsNotDone()) {
					IGlyphRun run = runs.Item();
					ASInt32 runEnd = runStart + run.GetSize();
					if (runStart > glyphStart) {
						return NULL; // Too far already...
					} else if (runEnd > glyphStart) {
						// Found it!
						// Cache the value and return
						// Make values relative to current glyphRun:
						glyphRuns = new GlyphRuns(glyphStart - runStart, glyphEnd - runStart);
						glyphRuns->add(run);
						runs.Next();
						while (runs.IsNotDone() && glyphRuns->add(runs.Item()))
							runs.Next();
						gEngine->setIntField(env, obj, gEngine->fid_TextRange_glyphRuns, (jint) glyphRuns);
						// gEngine->println(env, "%i", gEngine->getNanoTime() - t);
						// increase ref counter for returned IGlyphRun
						return glyphRuns;
					}
					runs.Next();
					runStart = runEnd;
				}
				lines.Next();
			}
		}
		return NULL;
	}
	
	static void release(JNIEnv *env, jobject obj) {
		GlyphRuns * glyphRuns = (GlyphRuns *) gEngine->getIntField(env, obj, gEngine->fid_TextRange_glyphRuns);
		if (glyphRuns != NULL) {
			delete glyphRuns;
			gEngine->setIntField(env, obj, gEngine->fid_TextRange_glyphRuns, 0);	
		}
	}
};

jobject TextRange_convertTextRanges(JNIEnv *env, TextRangesRef ranges) {
    // assume that this allways returns either 0 or 1. an exception is thrown if not so we know about it.
    // in this case, the assumption was wrong and scripto probably needs a few changes
	ASInt32 size;
	if (!sTextRanges->GetSize(ranges, &size)) {
		if (size > 1) {
			throw new StringException("TextRanges has more than one element!");
		} else if (size == 1) {
			TextRangeRef range;
			if (!sTextRanges->Item(ranges, 0, &range)) {
				return gEngine->wrapTextRangeRef(env, range);
			}
		}
	}
	return NULL;
}

/*
 * com.scriptographer.ai.Point[] getOrigins()
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_ai_TextRange_getOrigins(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		GlyphRuns *glyphRuns = GlyphRuns::get(env, obj, range);
		if (glyphRuns != NULL)
			return glyphRuns->getOrigins(env);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Matrix[] getTransformations()
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_ai_TextRange_getTransformations(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		GlyphRuns *glyphRuns = GlyphRuns::get(env, obj, range);
		if (glyphRuns != NULL)
			return glyphRuns->getTransformations(env);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * java.lang.String getGlyphRunContent()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_TextRange_getGlyphRunContent(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		GlyphRuns *glyphRuns = GlyphRuns::get(env, obj, range);
		if (glyphRuns != NULL)
			return glyphRuns->getContent(env);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
JNIEXPORT jintArray JNICALL Java_com_scriptographer_ai_TextRange_getGlyphIds(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int pos;
		IGlyphRun glyphRun = TextRange_getGlyphRun(env, obj, range, &pos);
		if (!glyphRun.IsNull()) {
			IArrayGlyphID glyphs = glyphRun.GetGlyphIDs();
			ASInt32 size = glyphs.GetSize();
			jintArray array = env->NewIntArray(size);
			jint *values = new jint[size];
			for (int i = 0; i < size; i++)
				values[i] = glyphs.Item(i);
			env->SetIntArrayRegion(array, 0, size, values);
			return array;
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getGlyphId(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int pos;
		IGlyphRun glyphRun = TextRange_getGlyphRun(env, obj, range, &pos);
		if (!glyphRun.IsNull()) {
			IArrayGlyphID glyphs = glyphRun.GetGlyphIDs();
			return glyphs.Item(pos);
		}
	} EXCEPTION_CONVERT(env);
	return 0;
}

JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getCharCount(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int pos;
		IGlyphRun glyphRun = TextRange_getGlyphRun(env, obj, range, &pos);
		return glyphRun.GetCharacterCount();
	} EXCEPTION_CONVERT(env);
	return 0;
}

JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getCount(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int pos;
		IGlyphRun glyphRun = TextRange_getGlyphRun(env, obj, range, &pos);
		return glyphRun.GetSize();
	} EXCEPTION_CONVERT(env);
	return 0;
}
*/

/*
 * int nativeGetStoryIndex()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_nativeGetStoryIndex(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		StoryRef story;
		ASInt32 index;
		// we can't wrap the story here and return it because for stories it's important to only have one reference per story (caching values...)
		// so return index here and grab it from the document based list.
		if (!sTextRange->GetStory(range, &story) && !sStory->GetIndex(story, &index))
			return index;
	} EXCEPTION_CONVERT(env);
	return -1;
}

/*
 * int getStart()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getStart(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ASInt32 start;
		if (!sTextRange->GetStart(range, &start))
			return start;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int getEnd()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getEnd(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ASInt32 end;
		if (!sTextRange->GetEnd(range, &end))
			return end;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int getLength()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getLength(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ASInt32 size;
		if (!sTextRange->GetSize(range, &size))
			return size;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void setRange(int start, int end)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_setRange(JNIEnv *env, jobject obj, jint start, jint end) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		sTextRange->SetRange(range, start, end);
	} EXCEPTION_CONVERT(env);
}

/*
 * java.lang.String getContent()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_TextRange_getContent(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ASInt32 size;
		if (!sTextRange->GetSize(range, &size)) {
			ASUnicode *text = new ASUnicode[size];
			if (!sTextRange->GetContents_AsUnicode(range, text, size, &size)) {
	 			return env->NewString(text, size);
			}
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * int nativePrepend(int handle, java.lang.String text)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_nativePrepend__ILjava_lang_String_2(JNIEnv *env, jobject obj, jint handle, jstring text) {
	ASInt32 size = 0;
	try {
		sTextRange->GetSize((TextRangeRef) handle, &size);
		int len = env->GetStringLength(text);
		const jchar *chars = env->GetStringCritical(text, NULL);
		sTextRange->InsertBefore_AsUnicode((TextRangeRef) handle, chars, len);
		env->ReleaseStringCritical(text, chars);
	} EXCEPTION_CONVERT(env);
	return size;
}

/*
 * int nativeAppend(int handle, java.lang.String text)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_nativeAppend__ILjava_lang_String_2(JNIEnv *env, jobject obj, jint handle, jstring text) {
	ASInt32 size = 0;
	try {
		sTextRange->GetSize((TextRangeRef) handle, &size);
		int len = env->GetStringLength(text);
		const jchar *chars = env->GetStringCritical(text, NULL);
		sTextRange->InsertAfter_AsUnicode((TextRangeRef) handle, chars, len);
		env->ReleaseStringCritical(text, chars); 
	} EXCEPTION_CONVERT(env);
	return size;
}

/*
 * int nativePrepend(int handle1, int handle2)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_nativePrepend__II(JNIEnv *env, jobject obj, jint handle1, jint handle2) {
	ASInt32 size = 0;
	try {
		sTextRange->GetSize((TextRangeRef) handle1, &size);
		sTextRange->InsertBefore_AsTextRange((TextRangeRef) handle1, (TextRangeRef) handle2);
	} EXCEPTION_CONVERT(env);
	return size;
}

/*
 * int nativeAppend(int handle1, int handle2)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_nativeAppend__II(JNIEnv *env, jobject obj, jint handle1, jint handle2) {
	ASInt32 size = 0;
	try {
		sTextRange->GetSize((TextRangeRef) handle1, &size);
		sTextRange->InsertAfter_AsTextRange((TextRangeRef) handle1, (TextRangeRef) handle2);
	} EXCEPTION_CONVERT(env);
	return size;
}

/*
 * void nativeRemove(int handle)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_nativeRemove(JNIEnv *env, jobject obj, jint handle) {
	try {
		sTextRange->Remove((TextRangeRef) handle);
		GlyphRuns::release(env, obj);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.TextFrame getFirstFrame()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_TextRange_getFirstFrame(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		TextFramesIteratorRef framesRef;
		if (!sTextRange->GetTextFramesIterator(range, &framesRef)) {
			ITextFramesIterator frames(framesRef);
			if (!frames.IsEmpty()) {
				AIArtHandle art;
				if (!sAITextFrame->GetAITextFrame( frames.Item().GetRef(), &art)) {
					return gEngine->wrapArtHandle(env, art);	
				}
			}
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.TextFrame getLastFrame()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_TextRange_getLastFrame(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		TextFramesIteratorRef framesRef;
		if (!sTextRange->GetTextFramesIterator(range, &framesRef)) {
			ITextFramesIterator frames(framesRef);
			if (!frames.IsEmpty()) {
				// walk to the last item
				while(frames.IsNotDone())
					frames.Next();
				AIArtHandle art;
				if (!sAITextFrame->GetAITextFrame(frames.Item().GetRef(), &art)) {
					return gEngine->wrapArtHandle(env, art);	
				}
			}
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * java.lang.Object clone()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_TextRange_clone(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		TextRangeRef clone;
		if (!sTextRange->Clone(range, &clone))
			return gEngine->wrapTextRangeRef(env, clone);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * int getSingleGlyph()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getSingleGlyph(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ATEGlyphID id;
		ATEBool8 ret;
		if (!sTextRange->GetSingleGlyphInRange(range, &id, &ret) && ret) {
			return id;
		}
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void select(boolean addToSelection)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_select(JNIEnv *env, jobject obj, jboolean addToSelection) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		sTextRange->Select(range, addToSelection);
	} EXCEPTION_CONVERT(env);
}

/*
 * void deselect()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_deselect(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		sTextRange->DeSelect(range);
	} EXCEPTION_CONVERT(env);
}

/*
 * void changeCase(int type)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_changeCase(JNIEnv *env, jobject obj, jint type) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		sTextRange->ChangeCase(range, (CaseChangeType) type);
	} EXCEPTION_CONVERT(env);
}

/*
 * void fitHeadlines()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_fitHeadlines(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		sTextRange->FitHeadlines(range);
	} EXCEPTION_CONVERT(env);
}

/*
 * int getCharacterType()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getCharacterType(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ASCharType type;
		if (!sTextRange->GetCharacterType(range, &type)) {
			return (jint) type;
		}
	} EXCEPTION_CONVERT(env);
	return -1;
}

/*
 * void release()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_release(JNIEnv *env, jobject obj) {
	try {
		GlyphRuns::release(env, obj);
		TextRangeRef range = (TextRangeRef) gEngine->getIntField(env, obj, gEngine->fid_AIObject_handle);
		if (range != NULL) {
			sTextRange->Release(range);
			gEngine->setIntField(env, obj, gEngine->fid_AIObject_handle, 0);
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean equals(java.lang.Object range)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_TextRange_equals(JNIEnv *env, jobject obj, jobject range) {
	try {
		if (env->IsInstanceOf(range, gEngine->cls_TextRange)) {
			TextRangeRef range1 = gEngine->getTextRangeRef(env, obj);
			TextRangeRef range2 = gEngine->getTextRangeRef(env, range);
			if (range2 != NULL) {
				ATEBool8 ret;
				if (!sTextRange->IsEqual(range1, range2, &ret))
					return ret;
			}
		}
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * int getKerning()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getKerning(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		StoryRef story;
		ASInt32 kerning;
		AutoKernType type;
		if (!sTextRange->GetStory(range, &story) && !sStory->GetKern(story, range, &type, &kerning))
			return (jint) kerning;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void setKerning(int kerning)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_setKerning(JNIEnv *env, jobject obj, jint kerning) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		StoryRef story;
		ASInt32 start;
		if (!sTextRange->GetStory(range, &story) && !sTextRange->GetStart(range, &start))
			sStory->SetKernAtChar(story, start, kerning);
	} EXCEPTION_CONVERT(env);
}

/*
 * int nativeGetCharacterStyle(int handle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_nativeGetCharacterStyle(JNIEnv *env, jobject obj, jint handle) {
	try {
		CharFeaturesRef features;
		if (!sTextRange->GetUniqueCharFeatures((TextRangeRef) handle, &features)) {
			// add reference to the handle, which will be released in CharacterStyle.finalize
			sCharFeatures->AddRef(features);
			return (jint) features;
		}
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeGetParagraphStyle(int handle)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_nativeGetParagraphStyle(JNIEnv *env, jobject obj, jint handle) {
	try {
		ParaFeaturesRef features;
		if (!sTextRange->GetUniqueParaFeatures((TextRangeRef) handle, &features)) {
			// add reference to the handle, which will be released in ParagraphStyle.finalize
			sParaFeatures->AddRef(features);
			return (jint) features;
		}
	} EXCEPTION_CONVERT(env);
	return 0;
}
