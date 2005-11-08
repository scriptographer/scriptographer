#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_TextRange.h"

/*
 * com.scriptographer.ai.TextRange
 */

using namespace ATE;

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

GlyphRunRef TextRange_getGlyphRun(JNIEnv *env, jobject obj, TextRangeRef rangeRef, int *glyphRunPos) {
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

/*
 * com.scriptographer.ai.Point[] getOrigins()
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_ai_TextRange_getOrigins(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int pos;
		GlyphRunRef glyphRun = TextRange_getGlyphRun(env, obj, range, &pos);
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
 * int[] getGlyphIds()
 */
JNIEXPORT jintArray JNICALL Java_com_scriptographer_ai_TextRange_getGlyphIds(JNIEnv *env, jobject obj) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int pos;
		GlyphRunRef glyphRun = TextRange_getGlyphRun(env, obj, range, &pos);
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
		GlyphRunRef glyphRun = TextRange_getGlyphRun(env, obj, range, &pos);
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
		GlyphRunRef glyphRun = TextRange_getGlyphRun(env, obj, range, &pos);
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
		GlyphRunRef glyphRun = TextRange_getGlyphRun(env, obj, range, &pos);
		ASInt32 count;
		sGlyphRun->GetSize(glyphRun, &count);
		return count;
	} EXCEPTION_CONVERT(env)
	return 0;
}

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
	} EXCEPTION_CONVERT(env)
	return -1;
}

/*
 * java.lang.String getGlyphRunContent()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ai_TextRange_getGlyphRunContent(JNIEnv *env, jobject obj) {
	try {
		using namespace ATE;
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int pos;
		GlyphRunRef glyphRun = TextRange_getGlyphRun(env, obj, range, &pos);
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
 * void nativeSetStart(int handle, int glyphRunRef, int start)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_nativeSetStart(JNIEnv *env, jobject obj, jint handle, jint glyphRunRef, jint start) {
	try {
		if (!sTextRange->SetStart((TextRangeRef) handle, start))
		if (glyphRunRef != 0)
			sGlyphRun->Release((GlyphRunRef) glyphRunRef);
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
 * void nativeSetEnd(int handle, int glyphRunRef, int end)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_nativeSetEnd(JNIEnv *env, jobject obj, jint handle, jint glyphRunRef, jint end) {
	try {
		if (!sTextRange->SetEnd((TextRangeRef) handle, end))
		if (glyphRunRef != 0)
			sGlyphRun->Release((GlyphRunRef) glyphRunRef);
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeSetRange(int handle, int glyphRunRef, int start, int end)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_nativeSetRange(JNIEnv *env, jobject obj, jint handle, jint glyphRunRef, jint start, jint end) {
	try {
		if (!sTextRange->SetRange((TextRangeRef) handle, start, end))
		if (glyphRunRef != 0)
			sGlyphRun->Release((GlyphRunRef) glyphRunRef);
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
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void nativeInsertBefore(java.lang.String text)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_nativeInsertBefore__Ljava_lang_String_2(JNIEnv *env, jobject obj, jstring text) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int size = env->GetStringLength(text);
		const jchar *chars = env->GetStringCritical(text, NULL);
		sTextRange->InsertBefore_AsUnicode(range, chars, size);
		env->ReleaseStringCritical(text, chars); 
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeInsertAfter(java.lang.String text)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_nativeInsertAfter__Ljava_lang_String_2(JNIEnv *env, jobject obj, jstring text) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		int size = env->GetStringLength(text);
		const jchar *chars = env->GetStringCritical(text, NULL);
		sTextRange->InsertAfter_AsUnicode(range, chars, size);
		env->ReleaseStringCritical(text, chars); 
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeInsertBefore(com.scriptographer.ai.TextRange range)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_nativeInsertBefore__I(JNIEnv *env, jobject obj, jint handle) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		if (handle != 0)
			sTextRange->InsertBefore_AsTextRange(range, (TextRangeRef) range);
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeInsertAfter(com.scriptographer.ai.TextRange range)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_nativeInsertAfter__I(JNIEnv *env, jobject obj, jint handle) {
	try {
		TextRangeRef range = gEngine->getTextRangeRef(env, obj);
		if (handle != 0)
			sTextRange->InsertAfter_AsTextRange(range, (TextRangeRef) range);
	} EXCEPTION_CONVERT(env)
}

/*
 * void nativeRemove(int handle, int glyphRunRef)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_TextRange_nativeRemove(JNIEnv *env, jobject obj, jint handle, jint glyphRunRef) {
	try {
		sTextRange->Remove((TextRangeRef) handle);
		if (glyphRunRef != 0)
			sGlyphRun->Release((GlyphRunRef) glyphRunRef);
	} EXCEPTION_CONVERT(env)
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
	} EXCEPTION_CONVERT(env)
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
	} EXCEPTION_CONVERT(env)
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
		TextRangeRef range = (TextRangeRef) gEngine->getIntField(env, obj, gEngine->fid_AIObject_handle);
		if (range != NULL)
			sTextRange->Release(range);
		GlyphRunRef glyphRunRef = (GlyphRunRef) gEngine->getIntField(env, obj, gEngine->fid_TextRange_glyphRunRef);
		if (glyphRunRef != NULL)
			sGlyphRun->Release(glyphRunRef);
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
	} EXCEPTION_CONVERT(env)
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
	} EXCEPTION_CONVERT(env)
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
	} EXCEPTION_CONVERT(env)
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
	} EXCEPTION_CONVERT(env)
	return 0;
}
