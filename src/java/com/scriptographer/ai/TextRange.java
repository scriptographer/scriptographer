/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2004-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 28.10.2005.
 * 
 * $RCSfile: TextRange.java,v $
 * $Author: lehni $
 * $Revision: 1.7 $
 * $Date: 2005/11/08 14:02:15 $
 */

package com.scriptographer.ai;

import java.util.StringTokenizer;
import java.util.zip.Adler32;

import com.scriptographer.CommitManager;
import com.scriptographer.util.ExtendedArrayList;
import com.scriptographer.util.ExtendedList;
import com.scriptographer.util.Lists;
import com.scriptographer.util.ReadOnlyList;

public class TextRange extends AIObject {
	
	// CaseChangeType
	public static final int CASE_UPPER = 0;
	public static final int CASE_LOWER = 1;
	public static final int CASE_TITLE = 2;
	public static final int CASE_SENTENCE = 3;

	// ASCharType
	/** undefined character */
	public static final int CHAR_UNDEFINED = -1;
	/** space character */
	public static final int CHAR_SPACE = 0;
	/** punctuation character */
	public static final int CHAR_PUNCTUATION = 1;
	/** paragraph end character CR */
	public static final int CHAR_PARAGRAPH_END = 2;
	/** this character is anything but space, punctuation or paragraphend */
	public static final int CHAR_NORMAL = 3;
	
	// values for the native environment,
	// to cash glyph run refrences, once their
	// found. these values need to be cleared in
	// setStart, setEnd ,setRange and finalize
	private int glyphRunRef;
	private int glyphRunPos;
	
	private Document document;
	private TextStory story = null;
	
	protected TextRange(int handle, int documentHandle) {
		super(handle);
		document = Document.wrapHandle(documentHandle);
	}

	public void assignHandle(TextRange range) {
		finalize(); // release old handle
		handle = range.handle;
		range.handle = 0;
	}
	
	public Document getDocument() {
		return document;
	}
	
	private native int nativeGetStoryIndex();
	
	public TextStory getStory() {
		if (story == null)
			story = (TextStory) document.getStories().get(nativeGetStoryIndex());
		return story;
	}
	
	public native TextFrame getFirstFrame();
	
	public native TextFrame getLastFrame();
	
	public ReadOnlyList getFrames() {
		TextFrame frame = getFirstFrame();
		TextFrame lastFrame = getLastFrame();
		if (frame != null) {
			if (lastFrame == null)
				lastFrame = frame;
			ExtendedArrayList list = new ExtendedArrayList();
			do {
				list.add(frame);
				frame = frame.getNextFrame();
			} while (frame != null && frame != lastFrame);
			return list;
		}
		return null;
	}
	
	protected void updateRange() {
		// if content lenght has changed, update ranges in all associated text frames:
		TextFrame frame = getFirstFrame();
		while (frame != null) {
			frame.updateRange();
			frame = frame.getNextFrame();
		}
	}
	
	public native int getStart();
	
	private native void nativeSetStart(int handle, int glyphRunRef, int start);
	
	public void setStart(int start) {
		commitStyles();
		nativeSetStart(handle, glyphRunRef, start);
		glyphRunRef = 0;
	}
	
	public native int getEnd();
	
	private native void nativeSetEnd(int handle, int glyphRunRef, int end);
	
	public void setEnd(int end) {
		commitStyles();
		nativeSetEnd(handle, glyphRunRef, end);
		glyphRunRef = 0;
	}

	public native void nativeSetRange(int handle, int glyphRunRef, int start, int end);
	
	public void setRange(int start, int end) {
		commitStyles();
		nativeSetRange(handle, glyphRunRef, start, end);
		glyphRunRef = 0;
	}
	
	public native int getLength();
	
	private native void nativeInsertBefore(String text);
	
	public void insertBefore(String text) {
		nativeInsertBefore(text);
		updateRange();
	}
	
	private native void nativeInsertAfter(String text);
	
	public void insertAfter(String text) {
		nativeInsertAfter(text);
		updateRange();
	}
	
	private native void nativeInsertBefore(int handle);
	
	public void insertBefore(TextRange range) {
		nativeInsertBefore(range.handle);
		updateRange();
	}

	private native void nativeInsertAfter(int handle);
	
	public void insertAfter(TextRange range) {
		nativeInsertAfter(range.handle);
		updateRange();
	}
	
	/**
	 * Returns the kerning between two chars in thousands of em.
	 */
	public native int getKerning();
	
	/**
	 * Sets the kerning between two chars in thousands of em.
	 * @param kerning
	 */
	public native void setKerning(int kerning);
	
	private native void nativeRemove(int handle, int glyphRunRef);

	/**
	 *  This method will delete all the characters in that range.
	 */
	public void remove() {
		commitStyles();
		nativeRemove(handle, glyphRunRef);
		glyphRunRef = 0;
		updateRange();
	}
	
	public native String getContent();
	
	public void setContent(String text) {
		remove();
		insertAfter(text);
	}
	
	/**
	 * gets the character style handle and adds reference to it. 
	 * attention! this needs to be wrapped in CharacterStyle so
	 * the reference gets released in the end.
	 */
	private native int nativeGetCharacterStyle(int handle);
	private native int nativeGetParagraphStyle(int handle);
	
	CharacterStyle characterStyle = null;
	ParagraphStyle paragraphStyle = null;
	
	public CharacterStyle getCharacterStyle() {
		if (characterStyle == null) {
			int styleHandle = nativeGetCharacterStyle(handle);
			if (styleHandle != 0)
				characterStyle = new CharacterStyle(styleHandle, this);
		} else if (characterStyle.version != CommitManager.version) {
			characterStyle.changeHandle(nativeGetCharacterStyle(handle));
			characterStyle.version = CommitManager.version;
		}
		return characterStyle;
	}
	
	public void setCharacterStyle(CharacterStyle style) {
		if (style != null) {
			getCharacterStyle(); // make sure it's created
			// create a new handle and set it here
			style = (CharacterStyle) style.clone();
			characterStyle.changeHandle(style.handle);
			characterStyle.version = CommitManager.version;
			characterStyle.markSetStyle();
			style.handle = 0; // make sure finalize doesn't mess up things...
		}
	}
	
	public ParagraphStyle getParagraphStyle() {
		if (paragraphStyle == null) {
			int styleHandle = nativeGetParagraphStyle(handle);
			if (styleHandle != 0)
				paragraphStyle = new ParagraphStyle(styleHandle, this);
		} else if (paragraphStyle.version != CommitManager.version) {
			paragraphStyle.changeHandle(nativeGetParagraphStyle(handle));
			paragraphStyle.version = CommitManager.version;
		}
		return paragraphStyle;
	}
	
	public void setParagraphStyle(ParagraphStyle style) {
		if (style != null) {
			getParagraphStyle(); // make sure it's created
			// create a new handle and set it here
			style = (ParagraphStyle) style.clone();
			paragraphStyle.changeHandle(style.handle);
			paragraphStyle.version = CommitManager.version;
			paragraphStyle.markSetStyle();
			style.handle = 0; // make sure finalize doesn't mess up things...
		}
	}
	
	protected void commitStyles() {
		if (characterStyle != null && characterStyle.dirty)
			characterStyle.commit();
		if (paragraphStyle != null && paragraphStyle.dirty)
			paragraphStyle.commit();
	}
	
	// TODO: ...
	public native Point[] getOrigins();
	/*
	public native int[] getGlyphIds();
	public native int getGlyphId();
	public native int getCharCount();
	public native int getCount();
	*/
	
	public native String getGlyphRunContent();
	
	// TODO: needed?
	public native int getSingleGlyph();
	
	public native void select(boolean addToSelection);
	/// if addToSelection is true, it will add this range to the current document selection.
	/// if addToSelection is false, it will clear the selection from the document and only select this range.
	
	public void select() {
		select(false);
	}

	public native void deselect();
	/// This method will remove this range from the selection.
	/// Note, deselecting a range can cause defregmented selection, if this range is a sub range of the current selection.
	
	/**
	 * clones the range.
	 * TODO: When the original range is changed in size, the copied one seems to change as well.
	 * so maybe, use getRange(0, getLength()) internally instead of the native clone...
	 */
	public native Object clone();
	
	public TextRange getSubRange(int start, int end) {
		TextRange range = (TextRange) clone();
		range.setRange(start, end);
		return range;
	}

	/**
	 * 
	 * @param type TextRange.CASE_*
	 */
	public native void changeCase(int type);
	
	public native void fitHeadlines();
	
	/**
	 *  This Range has to be of size equal to 1, any other size will throw error (kBadParameter)
	 *  @return TextRange.CHAR_*
	 */
	public native int getCharacterType();
	
	// TODO:
	//	ATEErr (*SetStory) ( TextRangeRef textrange, const StoryRef story);
	//	ATEErr (*SetRange) ( TextRangeRef textrange, ASInt32 start, ASInt32 end);
	/// start and end of this range will change depending on direction
	/// if direction = CollapseEnd, then end = start
	/// if direction = CollapseStart, then start = end
	//	ATEErr (*Collapse) ( TextRangeRef textrange, CollapseDirection direction);
	
	public native boolean equals(Object obj);
	
	protected native void finalize();
	
	TokenizerList words = null;
	
	public ReadOnlyList getWords() {
		if (words == null)
			words = new TokenizerList(" \t\n\r\f");
		words.update();
		return words;
	}
	
	TokenizerList paragraphs = null;
	
	public ReadOnlyList getParagraphs() {
		if (paragraphs == null)
			paragraphs = new TokenizerList("\r");
		paragraphs.update();
		return paragraphs;
	}
	
	CharacterList characters = null;
	
	public ReadOnlyList getCharacters() {
		if (characters == null)
			characters = new CharacterList();
		characters.update();
		return characters;
	}
	
	/**
	 * The base class for all TextRangeList classes
	 */
	abstract class TextRangeList implements ReadOnlyList {
		ExtendedArrayList.List list;
		
		TextRangeList() {
			list = new ExtendedArrayList.List();
		}

		public int getLength() {
			return list.size();
		}

		public boolean isEmpty() {
			return list.isEmpty();
		}

		public ExtendedList subList(int fromIndex, int toIndex) {
			return Lists.createSubList(this, fromIndex, toIndex);
		}
	}

	/**
	 * A list that applies a StringTokenizer to the internal text and adds each
	 * token to the list as a TextRange. The Ranges are only created when needed,
	 * The delimiter chars are included in the tokens at the end.
	 */
	class TokenizerList extends TextRangeList {
		class Token {
			int start;
			int end;
			String text;
			TextRange range;
			
			Token(int start, int end, String text) {
				this.start = start;
				this.end = end;
				this.text = text;
				this.range = null;
			}
			
			TextRange getRange() {
				if (range == null)
					range = getSubRange(start, end);
				return range;
			}
		}
		
		String delimiter;
		int position;
		Adler32 checksum;
		
		TokenizerList(String delimiter) {
			super();
			this.delimiter = delimiter;
			this.checksum = new Adler32();
		}
		
		void update() {
			String content = getContent();
			// calculate string checksum and compare, only update token list
			// if something changed...
			// TODO: see how this performs!
			long oldChecksum = checksum.getValue();
			checksum.reset();
			checksum.update(content.getBytes());
			this.position = getStart();
			if (checksum.getValue() != oldChecksum) {
				StringTokenizer st = new StringTokenizer(content, delimiter, true);
				StringBuffer part = new StringBuffer();
				list.clear();
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					// delimiter char?
					if (part.length() > 0 && (token.length() > 1 || delimiter.indexOf(token.charAt(0)) == -1)) {
						addToken(part.toString());
						part.setLength(0);
					}
					part.append(token);
				}
				if (part.length() > 0)
					addToken(part.toString());
			}
		}
		
		void addToken(String str) {
			int len = str.length();
			int end = position + len;
			list.add(new Token(position, end, str));
			position = end;
		}

		public Object get(int index) {
			Token token = (Token) list.get(index);
			return token.getRange();
		}
	}

	/**
	 * A lis of TextRanges for each character in the TextRange
	 */
	class CharacterList extends TextRangeList {
		void update() {
			this.list.setSize(TextRange.this.getLength());
		}

		public Object get(int index) {
			TextRange range = (TextRange) list.get(index);
			if (range == null)
				range = getSubRange(index, index + 1);
			return range;
		}
	}
}
