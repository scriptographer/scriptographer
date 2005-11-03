#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_TextRange.h"

/*
 * com.scriptographer.ai.TextRange
 */

using namespace ATE;

jobject textRangeConvertTextRanges(JNIEnv *env, TextRangesRef ranges) {
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

GlyphRunRef textRangeGetGlyphRun(JNIEnv *env, jobject obj, TextRangeRef rangeRef, int *glyphRunPos) {
	GlyphRunRef glyphRunRef = (GlyphRunRef) gEngine->getIntField(env, obj, gEngine->fid_TextRange_glyphRunRef);
	// use the cached result from last glyph search:
	if (glyphRunRef != NULL) {
		*glyphRunPos = gEngine->getIntField(env, obj, gEngine->fid_TextRange_glyphRunPos);
		return glyphRunRef;
	}

	long t = gEngine->getNanoTime();
	// use the C++ wrappers here for easier handling of ref madness
	// TODO: move away from wrappers for speed improve and size decrease!
	ITextRange range(rangeRef);
	ITextFramesIterator frames = range.GetTextFramesIterator();
	if (frames.IsNotDone()) {
		// only use the first frame in the range, as we look at the glyph at start
		ITextFrame frame = frames.Item();
		ITextRange frameRange = frame.GetTextRange();
		// now see where the frame range starts. that's where glyph index 0 of the frame is

		// count glyphs until start. There is a bug in glyphRun.GetCharacterSize() and glyphRun.GetContents(), so
		// we cannot count on these. They sometimes contain chars that are in the next run or contain chars from the prvious
		// ones....
		// so let's do it the hard way and count only on GetSingleGlyphInRange
		// TODO: cash these results in an int table!
		// IDEA: cash it in the Story of the range, as a lookup table char-index -> glyph-index
		int glyphPos = 0;
		int charPos = range.GetStart();
		int scanPos = frameRange.GetStart();
		while (scanPos < charPos) {
			int step = 1;
			// TODO: determine maximum ligature size. assumption is 3 for now...
			for (; step <= 3; step++) {
				frameRange.SetRange(scanPos, scanPos + step);
				ATEGlyphID id;
				if (frameRange.GetSingleGlyphInRange(&id)) // found a full glyph
					break;
			}
			scanPos += step;
			glyphPos++;

		}
		if (scanPos > charPos)
			glyphPos--;

		ASInt32 glyphStart = 0;
		ITextLinesIterator lines = frame.GetTextLinesIterator();
		while(lines.IsNotDone()) {
			IGlyphRunsIterator glyphRuns = lines.Item().GetGlyphRunsIterator();
			while (glyphRuns.IsNotDone()) {
				IGlyphRun glyphRun = glyphRuns.Item();
				ASInt32 glyphSize = glyphRun.GetSize();
				ASInt32 glyphEnd = glyphStart + glyphSize;
				if (glyphStart > glyphPos) {
					return NULL; // too far already...
				} else if (glyphEnd > glyphPos) {
					// found it!
					// now step through the glyphrun and find the right position.
					// there is a way to discover ligatures: the textrange's GetSingleGlyphInRange
					// only returns if the length is set to the amount of chars that produce a ligature
					// otherwise it files. so we can test....
					// first produce a text range over the glpyhrun:

					// cache the value and return
					glyphPos -=  glyphStart;
					glyphRunRef = glyphRun.GetRef();
					sGlyphRun->AddRef(glyphRunRef); // increase ref counter
					gEngine->setIntField(env, obj, gEngine->fid_TextRange_glyphRunRef, (jint) glyphRunRef);
					gEngine->setIntField(env, obj, gEngine->fid_TextRange_glyphRunPos, glyphPos);
					*glyphRunPos = glyphPos;
					gEngine->println(env, "%i", gEngine->getNanoTime() - t);
					return glyphRunRef;
					
				}
				glyphRuns.Next();
				glyphStart = glyphEnd;
			}
			lines.Next();
		}
	}
	return NULL;
	
	/*
	TextFramesIteratorRef frames;
	ASInt32 start;
	if (!sTextRange->GetStart(range, &start) && !sTextRange->GetTextFramesIterator(range, &frames)) {
		bool framesDone;
		TextFrameRef frame;
		while (true) {
			if (sTextFramesIterator->IsDone(frames, &framesDone) || framesDone ||
				sTextFramesIterator->Item(frames, &frame) ||
				sTextFramesIterator->Next(frames))
				break;
			
			TextLinesIteratorRef lines;
			if (!sTextFrame->GetTextLinesIterator(frame, &lines)) {
				bool linesDone;
				TextLineRef line;
				TextRangeRef lineRange;
				ASInt32 lineStart;
				ASInt32 lineSize;
				// find the line that contains the start of range:
				while (true) {
					if (sTextLinesIterator->IsDone(lines, &linesDone) || linesDone ||
						sTextLinesIterator->Item(lines, &line) ||
						sTextLinesIterator->Next(lines) ||
						sTextLine->GetTextRange(line, &lineRange) ||
						sTextRange->GetStart(lineRange, &lineStart) ||
						sTextRange->GetSize(lineRange, &lineSize))
						break;
					if (lineStart > start) {
						return NULL; // too far already...
					} else if (lineStart + lineSize > start) { // almost there now!
						GlyphRunsIteratorRef glyphRuns;
						if (!sTextLine->GetGlyphRunsIterator(line, &glyphRuns)) {
							bool glyphRunsDone;
							GlyphRunRef glyphRun;
							ASInt32 glyphStart = lineStart;
							ASInt32 glyphSize;
							while (true) {
								if (sGlyphRunsIterator->IsNotDone(glyphRuns, &glyphRunsDone) || !glyphRunsDone ||
									sGlyphRunsIterator->Item(glyphRuns, &glyphRun) ||
									sGlyphRunsIterator->Next(glyphRuns) ||
									sGlyphRun->GetSize(glyphRun, &glyphSize))
									break;
								ASInt32 glyphEnd = glyphStart + glyphSize;
								if (glyphStart > start) {
									return NULL; // too far already...
								} else if (glyphEnd > start) {
									// found it!
									return glyphRun;
								}
								glyphStart = glyphEnd;
							}
						}
					}
				}
			}
		}
	}
	return NULL;
	*/
}

// cleans the cached glyph run
void textRangeCleanGlyphRun(JNIEnv *env, jobject obj) {
	GlyphRunRef glyphRunRef = (GlyphRunRef) gEngine->getIntField(env, obj, gEngine->fid_TextRange_glyphRunRef);
	if (glyphRunRef != NULL) {
		gEngine->setIntField(env, obj, gEngine->fid_TextRange_glyphRunRef, 0);
		sGlyphRun->Release(glyphRunRef);
	}
}

/*
 * com.scriptographer.ai.Point[] getOrigins()
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_ai_TextRange_getOrigins(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int pos;
		GlyphRunRef glyphRun = textRangeGetGlyphRun(env, obj, range, &pos);
		ArrayRealPointRef points;
		ASInt32 size;
		if (glyphRun != NULL && !sGlyphRun->GetOrigins(glyphRun, &points) && !sArrayRealPoint->GetSize(points, &size)) {
			jobjectArray array = env->NewObjectArray(size, gEngine->cls_Point, NULL); 
			for (int i = 0; i < size; i++) {
				ASRealPoint pt;
				if (!sArrayRealPoint->Item(points, i, &pt)) {
					env->SetObjectArrayElement(array, i, gEngine->convertPoint(env, &pt));
				}
			}
			return array;
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * int getGlyphCount()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getGlyphCount(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int pos;
		GlyphRunRef glyphRun = textRangeGetGlyphRun(env, obj, range, &pos);
		ArrayGlyphIDRef glyphs;
		ASInt32 size;
		if (glyphRun != NULL && !sGlyphRun->GetGlyphIDs(glyphRun, &glyphs) && !sArrayGlyphID->GetSize(glyphs, &size)) {
			return size;
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * int[] getGlyphIds()
 */
JNIEXPORT jintArray JNICALL Java_com_scriptographer_ai_TextRange_getGlyphIds(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int pos;
		GlyphRunRef glyphRun = textRangeGetGlyphRun(env, obj, range, &pos);
		ArrayGlyphIDRef glyphs;
		ASInt32 size;
		if (glyphRun != NULL && !sGlyphRun->GetGlyphIDs(glyphRun, &glyphs) && !sArrayGlyphID->GetSize(glyphs, &size)) {
			jintArray array = env->NewIntArray(size);
			jint *values = new jint[size];
			for (int i = 0; i < size; i++) {
				ATEGlyphID id;
				if (!sArrayGlyphID->Item(glyphs, i, &id))
					values[i] = id;
			}
			env->SetIntArrayRegion(array, 0, size, values);
			return array;
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * int getGlyphId()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getGlyphId(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int pos;
		GlyphRunRef glyphRun = textRangeGetGlyphRun(env, obj, range, &pos);
		ArrayGlyphIDRef glyphs;
		ASInt32 size;
		if (glyphRun != NULL && !sGlyphRun->GetGlyphIDs(glyphRun, &glyphs) && !sArrayGlyphID->GetSize(glyphs, &size)) {
			ATEGlyphID id;
			if (!sArrayGlyphID->Item(glyphs, pos, &id))
				return id;
		}
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * int getCharCount()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getCharCount(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int pos;
		GlyphRunRef glyphRun = textRangeGetGlyphRun(env, obj, range, &pos);
		ASInt32 count;
		sGlyphRun->GetCharacterCount(glyphRun, &count);
		return count;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * int getCount()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getCount(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int pos;
		GlyphRunRef glyphRun = textRangeGetGlyphRun(env, obj, range, &pos);
		ASInt32 count;
		sGlyphRun->GetSize(glyphRun, &count);
		return count;
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * java.lang.String getGlyphRunContent()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_TextRange_getGlyphRunContent(JNIEnv *env, jobject obj) {
	try {
		using namespace ATE;
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int pos;
		GlyphRunRef glyphRun = textRangeGetGlyphRun(env, obj, range, &pos);
		ASInt32 size;
		if (glyphRun != NULL && !sGlyphRun->GetCharacterCount(glyphRun, &size)) {
			ASUnicode *text = new ASUnicode[size];
			if (!sGlyphRun->GetContents_AsUnicode(glyphRun, text, size, &size))
	 			return env->NewString(text, size);
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
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
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void setStart(int start)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_setStart(JNIEnv *env, jobject obj, jint start) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		if (!sTextRange->SetStart(range, start))
			textRangeCleanGlyphRun(env, obj);
	} EXCEPTION_CONVERT(env)
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
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void setEnd(int end)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_setEnd(JNIEnv *env, jobject obj, jint end) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		if (!sTextRange->SetEnd(range, end))
			textRangeCleanGlyphRun(env, obj);
	} EXCEPTION_CONVERT(env)
}

/*
 * void setRange(int start, int end)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_setRange(JNIEnv *env, jobject obj, jint start, jint end) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		if (!sTextRange->SetRange(range, start, end))
			textRangeCleanGlyphRun(env, obj);
	} EXCEPTION_CONVERT(env)
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
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * java.lang.String getText()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_TextRange_getText(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ASInt32 size;
		if (!sTextRange->GetSize(range, &size)) {
			ASUnicode *text = new ASUnicode[size];
			if (!sTextRange->GetContents_AsUnicode(range, text, size, &size)) {
	 			return env->NewString(text, size);
			}
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void insertBefore(java.lang.String text)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_insertBefore__Ljava_lang_String_2(JNIEnv *env, jobject obj, jstring text) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int size = env->GetStringLength(text);
		const jchar *chars = env->GetStringCritical(text, NULL);
		sTextRange->InsertBefore_AsUnicode(range, chars, size);
		env->ReleaseStringCritical(text, chars); 
	} EXCEPTION_CONVERT(env)
}

/*
 * void insertAfter(java.lang.String text)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_insertAfter__Ljava_lang_String_2(JNIEnv *env, jobject obj, jstring text) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int size = env->GetStringLength(text);
		const jchar *chars = env->GetStringCritical(text, NULL);
		sTextRange->InsertAfter_AsUnicode(range, chars, size);
		env->ReleaseStringCritical(text, chars); 
	} EXCEPTION_CONVERT(env)
}

/*
 * void insertBefore(com.scriptographer.ai.TextRange range)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_insertBefore__Lcom_scriptographer_ai_TextRange_2(JNIEnv *env, jobject obj, jobject range) {
	try {
		TextRangeRef range1 = gEngine->getTextRangeRef(env, obj);
		TextRangeRef range2 = gEngine->getTextRangeRef(env, range);
		if (range2 != NULL)
			sTextRange->InsertBefore_AsTextRange(range1, range2);
	} EXCEPTION_CONVERT(env)
}

/*
 * void insertAfter(com.scriptographer.ai.TextRange range)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_insertAfter__Lcom_scriptographer_ai_TextRange_2(JNIEnv *env, jobject obj, jobject range) {
	try {
		TextRangeRef range1 = gEngine->getTextRangeRef(env, obj);
		TextRangeRef range2 = gEngine->getTextRangeRef(env, range);
		if (range2 != NULL)
			sTextRange->InsertAfter_AsTextRange(range1, range2);
	} EXCEPTION_CONVERT(env)
}

/*
 * void remove()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_remove(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		sTextRange->Remove(range);
	} EXCEPTION_CONVERT(env)
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
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * int getSingleGlyph()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_TextRange_getSingleGlyph(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		ATEGlyphID id;
		bool ret;
		if (!sTextRange->GetSingleGlyphInRange(range, &id, &ret) && ret) {
			return id;
		}
	} EXCEPTION_CONVERT(env)
	return 0;
}

/*
 * void select(boolean addToSelection)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_select(JNIEnv *env, jobject obj, jboolean addToSelection) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		sTextRange->Select(range, addToSelection);
	} EXCEPTION_CONVERT(env)
}

/*
 * void deselect()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_deselect(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		sTextRange->DeSelect(range);
	} EXCEPTION_CONVERT(env)
}

/*
 * void changeCase(int type)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_changeCase(JNIEnv *env, jobject obj, jint type) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		sTextRange->ChangeCase(range, (CaseChangeType) type);
	} EXCEPTION_CONVERT(env)
}

/*
 * void fitHeadlines()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_fitHeadlines(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		sTextRange->FitHeadlines(range);
	} EXCEPTION_CONVERT(env)
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
	} EXCEPTION_CONVERT(env)
	return -1;
}

/*
 * void finalize()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_finalize(JNIEnv *env, jobject obj) {
	try {
		sTextRange->Release(gEngine->getTextRangeRef(env, obj));
		textRangeCleanGlyphRun(env, obj);
	} EXCEPTION_CONVERT(env)
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
				bool ret;
				if (!sTextRange->IsEqual(range1, range2, &ret))
					return ret;
			}
		}
	} EXCEPTION_CONVERT(env)
	return JNI_FALSE;
}
