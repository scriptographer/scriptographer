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

IGlyphRun textRangeGetGlyphRun(TextRangeRef rangeRef) {
	ITextRange range(rangeRef);
	ASInt32 start = range.GetStart();
	ITextFramesIterator frames = range.GetTextFramesIterator();
	while (frames.IsNotDone()) {
		ITextFrame frame = frames.Item();
		ITextLinesIterator lines = frame.GetTextLinesIterator();
		while(lines.IsNotDone()) {
			ITextLine line = lines.Item();
			ITextRange lineRange = line.GetTextRange();
			ASInt32 lineStart = lineRange.GetStart();
			ASInt32 lineSize = lineRange.GetSize();
			if (lineStart > start) {
				return IGlyphRun(); // too far already...
			} else if (lineStart + lineSize > start) { // almost there now!
				IGlyphRunsIterator glyphRuns = line.GetGlyphRunsIterator();
				ASInt32 glyphStart = lineStart;
				ASInt32 glyphSize;
				while (glyphRuns.IsNotDone()) {
					IGlyphRun glyphRun = glyphRuns.Item();
					glyphSize = glyphRun.GetCharacterCount();
					ASInt32 glyphEnd = glyphStart + glyphSize;
					if (glyphStart > start) {
						return IGlyphRun(); // too far already...
					} else if (glyphEnd > start) {
						// found it!
						return glyphRun;
					}
					// or do something else.
					glyphRuns.Next();
					glyphStart = glyphEnd;
				}
			}
			lines.Next();
		}
		frames.Next();
	}
	return IGlyphRun();
	
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


/*
 * com.scriptographer.ai.Point[] getOrigins()
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_ai_TextRange_getOrigins(JNIEnv *env, jobject obj) {
	try {
		using namespace ATE;
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		IGlyphRun glyphRun = textRangeGetGlyphRun(range);
		IArrayRealPoint points;
		if (!glyphRun.IsNull()) {
			IArrayRealPoint points = glyphRun.GetOrigins();
			ASInt32 size = points.GetSize();
			jobjectArray array = env->NewObjectArray(size, gEngine->cls_Point, NULL); 
			for (int i = 0; i < size; i++) {
				ASRealPoint pt = points.Item(i);
				env->SetObjectArrayElement(array, i, gEngine->convertPoint(env, &pt));
			}
			return array;
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * java.lang.String getGlyphRunContent()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_TextRange_getGlyphRunContent(JNIEnv *env, jobject obj) {
	try {
		using namespace ATE;
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		IGlyphRun glyphRun = textRangeGetGlyphRun(range);
		IArrayRealPoint points;
		if (!glyphRun.IsNull()) {
			ASInt32 size = glyphRun.GetSize();
			ASUnicode *text = new ASUnicode[size];
			size = glyphRun.GetContents(text, size);
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
		sTextRange->SetStart(range, start);
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
		sTextRange->SetEnd(range, end);
	} EXCEPTION_CONVERT(env)
}

/*
 * void setRange(int start, int end)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_setRange(JNIEnv *env, jobject obj, jint start, jint end) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		sTextRange->SetRange(range, start, end);
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
