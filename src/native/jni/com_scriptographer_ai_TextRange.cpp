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
	int m_glyphSize;
	int m_charSize;
	ASRealMatrix m_matrix;
	ITextFrame m_textFrame;
	ASRealPoint **m_origins;

public:
	GlyphRun(IGlyphRun run, int start, int end, Array<int> *glyphLengths, int glyphIndex) {
		m_matrix = run.GetMatrix();
		// Determine TextFrame through TextLine:
		m_textFrame = run.GetTextLine().GetTextFrame();
		IArrayRealPoint origins = run.GetOrigins();
		m_glyphSize = end - start;
		// Determine char size from glyph lengths:
		m_charSize = 0;
		for (int i = glyphIndex, length = glyphIndex + m_glyphSize; i < length; i++) {
			int glyphLength = glyphLengths->get(i);
			if (glyphLength < 0) {
				// Negative lenghts indicate paragraph end chars at the beginning. All other
				// paragraph breaks are added to previous values, so glyphIndex can remain
				// linked to glyphLengths entries
				m_charSize -= glyphLength;
				// Increase length, since these characters are not included in glyph indices
				length++;
			} else {
				m_charSize += glyphLength;
			}
		}
		m_origins = new ASRealPoint *[m_charSize];
		int charIndex = 0;
		for (int i = start; i < end; i++) {
			int glyphLength = glyphLengths->get(glyphIndex);
			// If it's a negative value, add null origins before the origin
			// (see explanation in GlyphRuns::getIndex)
			if (glyphLength < 0) {
				// Remove this entry now, so the glyph length indices remain link to glyph indices
				glyphLengths->remove(glyphIndex);
				for (int j = -glyphLength; j > 0; j--)
					m_origins[charIndex++] = NULL;
			} else {
				// A normal glyph, we can increase as usual
				glyphIndex++;
			}
			ASRealPoint *point = new ASRealPoint;
			*point = origins.Item(i);
			m_origins[charIndex++] = point;
			// Otherwise, add them now
			for (int j = 1; j < glyphLength; j++)
				m_origins[charIndex++] = NULL;
		}
	}

	~GlyphRun() {
		for (int i = 0; i < m_charSize; i++) {
			ASRealPoint *point = m_origins[i];
			if (point != NULL)
				delete point;
		}
		delete m_origins;
	}

	inline int glyphSize() {
		return m_glyphSize;
	}

	inline int charSize() {
		return m_charSize;
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

	inline ASRealPoint **getOrigins() {
		return m_origins;
	}
};

class GlyphRuns {

private:
	Array<GlyphRun *> m_runs;
	int m_glyphSize;
	int m_glyphStart;
	int m_glyphEnd;
	int m_glyphIndex;
	int m_charStart;
	int m_charSize;

	GlyphRuns(int glyphStart, int glyphEnd, int charStart, int charEnd) {
		m_glyphStart = glyphStart;
		m_glyphEnd = glyphEnd;
		m_glyphIndex = glyphStart;
		m_glyphSize = glyphEnd - glyphStart;
		m_charStart = charStart;
		m_charSize = charEnd - charStart;
	}

	bool add(IGlyphRun run, int glyphSize, Array<int> *glyphLengths) {
		bool first = m_glyphIndex == m_glyphStart;
		int nextGlyphIndex = first ? glyphSize : m_glyphIndex + glyphSize;
		bool last = nextGlyphIndex >= m_glyphEnd;
		int glyphStart = first ? m_glyphStart : 0;
		int glyphEnd = last ? m_glyphEnd - m_glyphIndex : glyphSize;
		m_runs.add(new GlyphRun(run, glyphStart, glyphEnd, glyphLengths, m_glyphIndex));
		m_glyphIndex = nextGlyphIndex;
		return !last;
	}

	inline GlyphRun *get(int index) {
		return m_runs.get(index);
	}

public:
	jobjectArray getOrigins(JNIEnv *env) {
		int index = 0;
		jobjectArray array = env->NewObjectArray(m_charSize, gEngine->cls_ai_Point, NULL);
		for (int i = 0, l = m_runs.size(); i < l; i++) {
			GlyphRun *run = get(i);
			ASRealPoint **origins = run->getOrigins();
			ASRealMatrix matrix = run->getMatrix();
			for (int j = 0, m = run->charSize(); j < m; j++) {
				ASRealPoint *origin = origins[j];
				jobject point;
				if (origin != NULL) {
					AIRealPoint pt;
					sAIRealMath->AIRealMatrixXformPoint(&matrix, origin, &pt);
					point = gEngine->convertPoint(env, &pt);
				} else {
					point = NULL;
				}
				env->SetObjectArrayElement(array, index++, point);
			}
		}
		return array;
	}

	jobjectArray getTransformations(JNIEnv *env) {
		int index = 0;
		jobjectArray array = env->NewObjectArray(m_charSize, gEngine->cls_ai_Matrix, NULL);
		for (int i = 0; i < m_runs.size(); i++) {
			GlyphRun *run = get(i);
			ASRealPoint **origins = run->getOrigins();
			ASRealMatrix runMatrix = run->getMatrix();
			for (int j = 0; j < run->charSize(); j++) {
				ASRealPoint *origin = origins[j];
				jobject matrix;
				if (origin != NULL) {
					ASRealMatrix glyphMatrix = runMatrix;
					sAIRealMath->AIRealMatrixConcatTranslate(&glyphMatrix, origin->h, origin->v);
					matrix = gEngine->convertMatrix(env, &glyphMatrix);
				} else {
					matrix = NULL;
				}
				env->SetObjectArrayElement(array, index++, matrix);
			}
		}
		return array;
	}

	static int getIndex(ITextRange range, int charIndex, Array<int> *glyphLengths = NULL) {
		// Count glyphs until pos. There is a bug in glyphRun.GetCharacterSize()
		// and glyphRun.GetContents(), so we cannot count on these.
		// They sometimes contain chars that are in the next run or contain chars
		// from the previous ones....
		// So let's do it the hard way and count only on GetSingleGlyphInRange
		// TODO: cash these results in an int table!
		// IDEA: cash it in the Story of the range, as a lookup table
		// char-index -> glyph-index
		int glyphPos = 0;
		int start = range.GetStart();
		int end = range.GetEnd();
		int size = range.GetSize();
		int scanPos = start;
		while (scanPos < charIndex) {
			// There is a way to discover ligatures: the TextRange's GetSingleGlyphInRange
			// only returns if the length is set to the amount of chars that produce a ligature
			// otherwise it fails. So we can test....
			// TODO: determine maximum ligature size.
			// Assumption is 16 for now.
			// In most cases, 1 will return a result, so there won't be too much iteration here...
			ATEGlyphID id;
			int length = 1;
			int max = MIN(16, size - scanPos);
			for (; length <= max; length++) {
				// First set the text range of the glpyhrun to test GetSingleGlyphInRange on
				range.SetRange(scanPos, scanPos + length);
				// ASCharType type = range.GetCharacterType();
				if (range.GetSingleGlyphInRange(&id)) // Found a full glyph?
					break;
			}
			// If the length goes all the way to the end, we are likely to have encountered a hyphen glyph, as forced by the
			// AI layout engine's auto hyphenation.
			// There seems to be no way to detect this otherwise, and GetSingleGlyphInRange does not return an id
			// for the situation where the range only describes the one letter before the hyphen. So let's assume
			// this situation is only encountered for hyphens, and adjust glyphLengths, scanPos and glyphPos accordingly
			// further bellow.
			if (length < max || max == 1) {
				scanPos += length;
				// Glyph runs do not count paragraph end chars, so don't count them here either.
				if (range.GetCharacterType() != kParagraphEndChar) {
					glyphPos++;
					if (glyphLengths != NULL)
						glyphLengths->add(length);
				} else if (glyphLengths != NULL) {
					int last = glyphLengths->size() - 1;
					if (last >= 0) {
						int value = glyphLengths->get(last);
						// Add length to last one. If it's negative, subtract it, since the range starts with paragraph
						// end chars (see bellow)
						if (value < 0) value -= length;
						else value += length;
						glyphLengths->set(last, value);
					} else {
						// Add a negative value, to indicate that range starts with paragraph end chars
						glyphLengths->add(-length);
					}
				}
			} else {
				// Increase glyph pos both for the actual char and the hyphen. This is guessing. There might be situations where
				// this is wrong, e.g. ligatures before hypenation, etc. TODO: Test!
				glyphPos += 2;
				scanPos++;
				if (glyphLengths != NULL) {
					// Normal glyph (what if it's a ligature?)
					glyphLengths->add(1);
					// The hyphen, to be ignored as a glyph
					glyphLengths->add(0);
				}
			}
		}
		if (scanPos > charIndex)
			glyphPos--;
		range.SetRange(start, end);
		return glyphPos;
	}

	static GlyphRuns *get(JNIEnv *env, jobject obj, TextRangeRef rangeRef) {
		GlyphRuns *glyphRuns = (GlyphRuns *) gEngine->getIntField(env, obj, gEngine->fid_ai_TextRange_glyphRuns);
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
			
			// Keep track of each glyph's length in characters, so we can track ligatures and paragraph end chars,
			// which will produce null entries in the origin arrays. (e.g. if 3 chars lead to one glyph, only the
			// first char has an origin, the others will be null, in order to link the indices between glyphs and
			// content).
			Array<int> glyphLengths;
			int charStart = range.GetStart();
			int charEnd = range.GetEnd();
			int charPos = charStart;
			int glyphStart = GlyphRuns::getIndex(frameRange, charStart);
			int glyphEnd = GlyphRuns::getIndex(frameRange, charEnd, &glyphLengths);
			
			ASInt32 runStart = 0;
			ITextLinesIterator lines = frame.GetTextLinesIterator();
			while (lines.IsNotDone()) {
				// Get current line's glyph runs
				IGlyphRunsIterator runs = lines.Item().GetGlyphRunsIterator();
				while (runs.IsNotDone()) {
					IGlyphRun run = runs.Item();
					ASInt32 runSize = run.GetSize();
					ASInt32 runEnd = runStart + runSize;
					if (runStart >= glyphEnd) {
						// Found it already
						return glyphRuns;
					} else if (runEnd > glyphStart) {
						// Found it!
						// Cache the value and return
						// Make values relative to current glyphRun:
						if (glyphRuns == NULL) {
							glyphRuns = new GlyphRuns(glyphStart - runStart, glyphEnd - runStart, charStart, charEnd);
							gEngine->setIntField(env, obj, gEngine->fid_ai_TextRange_glyphRuns, (jint) glyphRuns);
						}
						glyphRuns->add(run, runSize, &glyphLengths);
						runs.Next();
						while (runs.IsNotDone()) {
							run = runs.Item();
							runSize = run.GetSize();
							runEnd = runEnd + runSize;
							if (!glyphRuns->add(run, runSize, &glyphLengths))
								break;
							runs.Next();
						}
					}
					if (runs.IsNotDone())
						runs.Next();
					runStart = runEnd;
				}
				lines.Next();
			}
		}
		return glyphRuns;
	}
	
	static void release(JNIEnv *env, jobject obj) {
		GlyphRuns * glyphRuns = (GlyphRuns *) gEngine->getIntField(env, obj, gEngine->fid_ai_TextRange_glyphRuns);
		if (glyphRuns != NULL) {
			delete glyphRuns;
			gEngine->setIntField(env, obj, gEngine->fid_ai_TextRange_glyphRuns, 0);	
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
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj, true);
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
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj, true);
		GlyphRuns *glyphRuns = GlyphRuns::get(env, obj, range);
		if (glyphRuns != NULL)
			return glyphRuns->getTransformations(env);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
JNIEXPORT jintArray JNICALL Java_com_scriptographer_ai_TextRange_getGlyphIds(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
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
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
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
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
		int pos;
		IGlyphRun glyphRun = TextRange_getGlyphRun(env, obj, range, &pos);
		return glyphRun.GetCharacterCount();
	} EXCEPTION_CONVERT(env);
	return 0;
}

JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getCount(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
		int pos;
		IGlyphRun glyphRun = TextRange_getGlyphRun(env, obj, range, &pos);
		return glyphRun.GetSize();
	} EXCEPTION_CONVERT(env);
	return 0;
}
*/

/*
 * int getStoryIndex()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getStoryIndex(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
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
 * int getStoryHandle()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getStoryHandle(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
		StoryRef story = NULL;
		sTextRange->GetStory(range, &story);
		return (jint) story;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int getStart()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getStart(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
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
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
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
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
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
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
		sTextRange->SetRange(range, start, end);
	} EXCEPTION_CONVERT(env);
}

/*
 * java.lang.String getContent()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_TextRange_getContent(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
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
 * boolean nativeRemove(int handle)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_TextRange_nativeRemove(JNIEnv *env, jobject obj, jint handle) {
	try {
		sTextRange->Remove((TextRangeRef) handle);
		GlyphRuns::release(env, obj);
		return true;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * com.scriptographer.ai.TextItem getFirstTextItem()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_TextRange_getFirstTextItem(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
		TextFramesIteratorRef framesRef;
		if (!sTextRange->GetTextFramesIterator(range, &framesRef)) {
			ITextFramesIterator frames(framesRef);
			if (!frames.IsEmpty()) {
				AIArtHandle art;
				if (!sAITextFrame->GetAITextFrame( frames.Item().GetRef(), &art))
					return gEngine->wrapArtHandle(env, art, gEngine->getDocumentHandle(env, obj));	
			}
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.TextItem getLastTextItem()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_TextRange_getLastTextItem(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
		TextFramesIteratorRef framesRef;
		if (!sTextRange->GetTextFramesIterator(range, &framesRef)) {
			ITextFramesIterator frames(framesRef);
			if (!frames.IsEmpty()) {
				// Walk to the last item
				while(frames.IsNotDone())
					frames.Next();
				AIArtHandle art;
				if (!sAITextFrame->GetAITextFrame(frames.Item().GetRef(), &art))
					return gEngine->wrapArtHandle(env, art, gEngine->getDocumentHandle(env, obj));	
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
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
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
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
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
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
		sTextRange->Select(range, addToSelection);
	} EXCEPTION_CONVERT(env);
}

/*
 * void deselect()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_deselect(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
		sTextRange->DeSelect(range);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean nativeChangeCase(int type)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_TextRange_nativeChangeCase(JNIEnv *env, jobject obj, jint type) {
	try {
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
		return !sTextRange->ChangeCase(range, (CaseChangeType) type);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void fitHeadlines()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_fitHeadlines(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
		sTextRange->FitHeadlines(range);
	} EXCEPTION_CONVERT(env);
}

/*
 * int nativeGetCharacterType()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_nativeGetCharacterType(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
		ASCharType type;
		if (!sTextRange->GetCharacterType(range, &type)) {
			return (jint) type;
		}
	} EXCEPTION_CONVERT(env);
	return -1;
}

/*
 * void nativeRelease()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_nativeRelease(JNIEnv *env, jobject obj, jint handle) {
	try {
		GlyphRuns::release(env, obj);
		if (handle) {
			sTextRange->Release((TextRangeRef) handle);
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean equals(java.lang.Object range)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_TextRange_equals(JNIEnv *env, jobject obj, jobject range) {
	try {
		if (env->IsInstanceOf(range, gEngine->cls_ai_TextRange)) {
			TextRangeRef range1 = gEngine->getTextRangeHandle(env, obj);
			TextRangeRef range2 = gEngine->getTextRangeHandle(env, range);
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
	ASInt32 kerning = 0;
	try {
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
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
 * void setKerning(int kerning)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_setKerning(JNIEnv *env, jobject obj, jint kerning) {
	try {
		TextRangeRef range = gEngine->getTextRangeHandle(env, obj);
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
