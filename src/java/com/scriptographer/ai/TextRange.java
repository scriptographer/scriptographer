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
 * File created on 28.10.2005.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.zip.Adler32;

import com.scriptographer.CommitManager;
import com.scriptographer.Committable;
import com.scriptographer.ScriptographerException;
import com.scratchdisk.list.ExtendedArrayList;
import com.scratchdisk.list.ExtendedList;
import com.scratchdisk.list.ListIterator;
import com.scratchdisk.list.Lists;
import com.scratchdisk.list.ReadOnlyList;
import com.scratchdisk.util.ArrayList;
import com.scratchdisk.util.IntegerEnumUtils;

/**
 * @author lehni
 */
public class TextRange extends DocumentObject implements Committable,
		TextStoryProvider {
	
	// values for the native environment,
	// to cash glyph run references, once their
	// found. these values need to be cleared in
	// setStart, setEnd ,setRange and finalize
	private int glyphRuns;
	
	protected TextStory story = null;
	protected int version = CommitManager.version;
	protected boolean dirty = false;
	
	// Sub Range lists:
	TokenizerList words = null;
	TokenizerList paragraphs = null;
	CharacterList characters = null;

	protected TextRange(int handle, Document document) {
		super(handle, document);
	}

	protected TextRange(int handle, int docHandle) {
		this(handle, Document.wrapHandle(docHandle));
	}

	// Once a range object is created, always return the same reference
	// and swap handles instead. like this references in JS remain...
	protected void changeHandle(int newHandle) {
		nativeRelease(handle); // release old handle
		handle = newHandle;
		version = CommitManager.version;
		// Clear the sub ranges, as the items in them are not valid anymore
		words = null;
		paragraphs = null;
		characters = null;
		// Story needs to be updated too, otherwise the old TextItem
		// (e.g. before moving) keeps being referenced...
		if (story != null)
			document.getStories(this, true).changeStoryHandle(story,
					getStoryIndex());
	}

	/**
	 * markDirty is called when content is changed
	 */
	protected void markDirty() {
		if (!dirty) {
			CommitManager.markDirty(getStory(), this);
			dirty = true;
		}
	}

	public void commit(boolean endExecution) {
		// Committing changes for TextRange does not need more than
		// a reflow of the text layout in the document.
		// This is needed since otherwise, the TextItem's attributes
		// (bounds, etc) are invalid
		if (!endExecution)
			document.reflowText();
		dirty = false;
	}

	/**
	 * The text content of the text range.
	 */
	public native String getContent();
	
	public void setContent(String text) {
		remove();
		append(text);
	}
	
	/**
	 * The index of the first character of the text range inside the story in
	 * numbers of characters.
	 */
	public native int getStart();
	
	/**
	 * The index of the last character of the text range inside the story in
	 * numbers of characters.
	 */
	public native int getEnd();
	
	/**
	 * The amount of characters in the range.
	 */
	public native int getLength();

	private native int getStoryIndex();

	/*
	 * @see TextStoryProvider.
	 */
	public native int getStoryHandle();
	
	/**
	 * {@grouptitle Hierarchy}
	 * 
	 * The document that the text range belongs to.
	 */
	public Document getDocument() {
		// This is only here for the API document.
		// It does exactly the same as the definition in DocumentObject
		return document;
	}

	/** 
	 * The story that the text range belongs to.
	 */
	public TextStory getStory() {
		if (story == null) {
			TextStoryList stories = document.getStories(this, true);
			int index = getStoryIndex();
			if (index >= stories.size())
				throw new ScriptographerException("Cannot get text stories from document");
			story = stories.get(index);
		}
		return story;
	}
	
	/**
	 * The first text frame of the story that this text range belongs to.
	 * @jshide
	 */
	public native TextItem getFirstTextItem();

	/**
	 * The last text frame of the story that this text range belongs to.
	 * @jshide
	 */
	public native TextItem getLastTextItem();

	/**
	 * An array of linked text frames that the text range is contained
	 * within.
	 */
	public ReadOnlyList<TextItem> getTextItems() {
		ExtendedArrayList<TextItem> list = new ExtendedArrayList<TextItem>();
		TextItem item = getFirstTextItem();
		TextItem last = getLastTextItem();
		if (item != null) {
			if (last == null)
				last = item;
			do {
				list.add(item);
				item = item.getNextTextItem();
			} while (item != null && item != last);
		}
		return list;
	}

	protected native void setRange(int start, int end);
	
	private native int nativePrepend(int handle1, String text);
	
	private native int nativeAppend(int handle1, String text);
	
	private native int nativePrepend(int handle1, int handle2);

	private native int nativeAppend(int handle1, int handle2);

	private void adjustStart(int oldLength) {
		if (characters != null)
			characters.adjustStart(oldLength);
		if (words != null)
			words.adjustStart(oldLength);
		if (paragraphs != null)
			paragraphs.adjustStart(oldLength);
		this.markDirty();
	}

	private void adjustEnd(int oldLength) {
		if (characters != null)
			characters.adjustEnd(oldLength);
		if (words != null)
			words.adjustEnd(oldLength);
		if (paragraphs != null)
			paragraphs.adjustEnd(oldLength);
		this.markDirty();
	}

	/**
	 * Prepends the supplied text to the text range.
	 * @param text
	 */
	public void prepend(String text) {
		adjustStart(nativePrepend(handle, text));
	}
	
	/**
	 * Appends the supplied text to this text range.
	 * @param text
	 */
	public void append(String text) {
		adjustEnd(nativeAppend(handle, text));
	}

	/**
	 * Prepends the supplied text range to the text range.
	 */
	public void prepend(TextRange range) {
		adjustStart(nativePrepend(handle, range.handle));
	}

	/**
	 * Appends the supplied text range to this text range.
	 * @param range
	 */
	public void append(TextRange range) {
		adjustEnd(nativeAppend(handle, range.handle));
	}
	
	private native boolean nativeRemove(int handle);
	
	/**
	 *  Deletes all the characters in the text range.
	 */
	public boolean remove() {
		if (nativeRemove(handle)) {
			if (characters != null)
				characters.removeAll();
			if (words != null)
				words.removeAll();
			if (paragraphs != null)
				paragraphs.removeAll();
			markDirty();
			return true;
		}
		return false;
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

	/**
	 * {@grouptitle Style Properties}
	 * 
	 * The character style of the text range.
	 */
	public CharacterStyle getCharacterStyle() {
		if (characterStyle == null) {
			int styleHandle = nativeGetCharacterStyle(handle);
			if (styleHandle != 0)
				characterStyle = new CharacterStyle(styleHandle, this);
		} else if (characterStyle.version != CommitManager.version) {
			characterStyle.changeHandle(nativeGetCharacterStyle(handle));
		}
		return characterStyle;
	}

	public void setCharacterStyle(CharacterStyle style) {
		if (style != null) {
			getCharacterStyle(); // make sure it's created
			// create a new handle and set it here
			style = (CharacterStyle) style.clone();
			characterStyle.changeHandle(style.handle);
			characterStyle.markSetStyle();
			style.handle = 0; // make sure release doesn't mess up things...
		}
	}
	
	/**
	 * The paragraph style of the text range.
	 */
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
			style.handle = 0; // make sure release doesn't mess up things...
		}
	}
	
	protected void commitStyles() {
		if (characterStyle != null && characterStyle.dirty)
			characterStyle.commit(false);
		if (paragraphStyle != null && paragraphStyle.dirty)
			paragraphStyle.commit(false);
	}
	
	// TODO: move to CharacterStyle
	/**
	 * {@grouptitle Character Range Properties}
	 * 
	 * The origin points of the characters in the text range.
	 */
	public native Point[] getOrigins();
	
	/**
	 * The transformation matrices of the characters in the text range.
	 */
	// TODO: Rename to getMatrices() ?
	public native Matrix[] getTransformations();
	/*
	TODO: ...
	public native int[] getGlyphIds();
	public native int getGlyphId();
	public native int getCharCount();
	public native int getCount();
	*/
	
	// TODO: needed?
	/**
	 * @jshide
	 */
	public native int getSingleGlyph();
	
	/**
	 * Selects the text range.
	 * 
	 * @param addToSelection If set to {@code true}, the text range will
	 *        be added to the current selection in the document. If set to
	 *        false, it will clear the current selection in the document and
	 *        only select the text range.
	 */
	public native void select(boolean addToSelection);
	// if addToSelection is true, it will add this range to the current document
	// selection.
	// if addToSelection is false, it will clear the selection from the document
	// and only select this range.
	
	public void select() {
		select(false);
	}

	/**
	 * Deselects the text range in the document. Note that deselecting a text
	 * range can cause defragmented selection, if the text range is a subrange
	 * of the current selection.
	 */
	public native void deselect();
	// This method will remove this range from the selection.
	// Note, deselecting a range can cause defregmented selection, if this range
	// is a sub range of the current selection.
	
	/**
	 * Clones the text range.
	 */
	public Object clone() {
		/*
		 * Cannot use the native code, because when the original range is
		 * changed in size, the copied one seems to change as well. Use
		 * getRange(0, getLength()) instead of the native clone...
		 */
		return getSubRange(0, getLength());
	}
	
	/**
	 * Returns a text range from the story given in indices relative to the current
	 * text range's start index.
	 * 
	 * @param start
	 * @param end
	 * @return sub range
	 */
	public TextRange getSubRange(int start, int end) {
		int index = getStart();
		return getStory().getRange(index + start, index + end);
	}

	private native boolean nativeChangeCase(int change);

	/**
	 * Changes the case of the text in the text range.
	 * 
	 * Sample code:
	 * <code>
	 * var text = new PointText();
	 * text.content = 'the title of a book';
	 * text.range.changeCase('title');
	 * print(text.content); // 'The Title Of A Book'
	 * </code>
	 * 
	 * <code>
	 * var text = new PointText();
	 * text.content = 'one two three';
	 * text.range.words[1].changeCase('upper');
	 * print(text.content); // 'one TWO three'
	 * </code>
	 * 
	 * @param change
	 */
	public boolean changeCase(TextCase change) {
		if (change != null)
			return nativeChangeCase(change.value);
		return false;
	}
	
	/**
	 * Adjusts the tracking of the text in the text range to fit on one line
	 * spanning the width of the area text.
	 */
	public native void fitHeadlines();
	
	private native int nativeGetCharacterType();
	
	/**
	 * The character type of the text range.
	 * This range has to be of size equal to 1 character.
	 * 
	 * Sample code:
	 * <code>
	 * var text = new PointText(new Point(50, 100));
	 * text.content = 'Text.';
	 * 
	 * var firstCharacter = text.range.characters.first;
	 * print(firstCharacter.characterType) // 'normal'
	 * 
	 * var lastCharacter = text.range.characters.last;
	 * print(lastCharacter.characterType); // 'punctuation'
	 * </code>
	 */
	public CharacterType getCharacterType() {
		return IntegerEnumUtils.get(CharacterType.class,
				nativeGetCharacterType());
	}
	
	// TODO:
	//	ATEErr (*SetStory) ( TextRangeRef textrange, const StoryRef story);
	//	ATEErr (*SetRange) ( TextRangeRef textrange, ASInt32 start, ASInt32 end);
	/// start and end of this range will change depending on direction
	/// if direction = CollapseEnd, then end = start
	/// if direction = CollapseStart, then start = end
	//	ATEErr (*Collapse) ( TextRangeRef textrange, CollapseDirection direction);
	
	public native boolean equals(Object obj);
	
	private native void nativeRelease(int handle);
	
	protected void finalize() {
		nativeRelease(handle);
		handle = 0;
	}

	/**
	 * {@grouptitle Sub Ranges}
	 * 
	 * The text ranges of the words contained within the text range. Note that
	 * the returned text range includes the trailing whitespace characters of
	 * the words.
	 * 
	 * Sample code:
	 * <code>
	 * var text = new PointText(new Point(0,0));
	 * text.content = 'The contents of the point text.';
	 * var word = text.range.words[1];
	 * print(word.content) // 'contents ' - note the space after 'contents';
	 * </code>
	 */
	public ReadOnlyList<TextRange> getWords() {
		if (words == null)
			words = new TokenizerList(" \t\n\r\f");
		words.update();
		return words;
	}

	/**
	 * The text ranges of the paragraphs contained within the text range. Note
	 * that the returned text range includes the trailing paragraph (\r)
	 * characters of the paragraphs.
	 * 
	 * Sample code:
	 * <code>
	 * var text = new PointText(new Point(0,0));
	 * 
	 * // ('\r' is the escaped character that specifies a new paragraph)
	 * text.content = 'First paragraph\rSecond paragraph';
	 * var paragraph = text.range.paragraphs[1];
	 * print(paragraph.content) //returns 'Second paragraph';
	 * </code>
	 */
	public ReadOnlyList<TextRange> getParagraphs() {
		if (paragraphs == null)
			paragraphs = new TokenizerList("\r");
		paragraphs.update();
		return paragraphs;
	}

	/**
	 * The text ranges of the characters contained within the text range.
	 * 
	 * Sample code:
	 * <code>
	 * var text = new PointText(new Point(0,0));
	 * text.content = 'abc';
	 * var character = text.range.characters[1];
	 * print(character.content) //returns 'b';
	 * </code>
	 */	
	public ReadOnlyList<TextRange> getCharacters() {
		if (characters == null)
			characters = new CharacterList();
		characters.update();
		return characters;
	}
	
	/**
	 * The base class for all TextRangeList classes
	 */
	abstract class TextRangeList<E> implements ReadOnlyList<TextRange> {
		ArrayList<E> list;
		
		TextRangeList() {
			list = new ArrayList<E>();
		}

		public int size() {
			return list.size();
		}

		public boolean isEmpty() {
			return list.isEmpty();
		}
		
		public void removeAll() {
			list.clear();
		}

		public ExtendedList<TextRange> getSubList(int fromIndex, int toIndex) {
			return Lists.createSubList(this, fromIndex, toIndex);
		}

		public Iterator<TextRange> iterator() {
			return new ListIterator<TextRange>(this);
		}

		public TextRange getFirst() {
			return size() > 0 ? get(0) : null;
		}

		public TextRange getLast() {
			int size = size();
			return size > 0 ? get(size - 1) : null;
		}

		public Class<?> getComponentType() {
			return TextRange.class;
		}
	}

	/**
	 * A list that applies a StringTokenizer to the internal text and adds each
	 * token to the list as a TextRange. The Ranges are only created when
	 * needed, The delimiter chars are included in the tokens at the end.
	 */
	class TokenizerList extends TextRangeList<TokenizerList.Token> {

		class Token {
			int start;
			int end;
			String text;
			TextRange range = null;

			TextRange getRange() {
				if (range == null)
					range = getSubRange(start, end);
				return range;
			}

			void init(int start, int end, String text) {
				this.start = start;
				this.end = end;
				this.text = text;
				if (range != null)
					range.setRange(start, end);
			}
		}
		
		String delimiter;
		int tokenPos;
		int tokenIndex;
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
			// positions in tokens are relative to the start of the containing
			// range
			int position = 0;
			int index = 0;
			// this loop reuses tokens
			if (checksum.getValue() != oldChecksum) {
				StringTokenizer st = new StringTokenizer(content, delimiter, true);
				StringBuffer part = new StringBuffer();
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					// delimiter char?
					if (part.length() > 0 && (token.length() > 1 ||
							delimiter.indexOf(token.charAt(0)) == -1)) {
						position = setToken(index++, position, part.toString());
						part.setLength(0);
					}
					part.append(token);
				}
				if (part.length() > 0)
					position = setToken(index++, position, part.toString());
				list.setSize(index);
			}
		}
		
		public void adjustStart(int oldLength) {
			// get text of first token that has changed. split it again and
			// adjust list accordingly
			if (list.size() > 0) {
				TextRange range = get(0);
				String content = range.getContent();
				StringTokenizer st = new StringTokenizer(content, delimiter, false);
				int count = 0;
				while (st.hasMoreTokens()) {
					st.nextToken();
					count++;
				}
				// in case the added text causes a new split or even several,
				// add the amount of empty tokens so the offset is right again
				// (in case some ranges where already
				// referenced they need to shift properly. then update list
				// TODO: consider optimization: only the new tokens should be
				// parsed, the rest could be simply offset...
				if (count > 1) {
					for (int i = 1; i < count; i++)
						list.add(0, null);
					update();
				}
			}
		}

		public void adjustEnd(int oldLength) {
			// get text of last token that has changed. split it again and
			// adjust list accordingly
			int size = list.size();
			if (size > 0) {
				TextRange range = get(size - 1);
				String content = range.getContent();
				StringTokenizer st = new StringTokenizer(content, delimiter,
						false);
				int count = 0;
				while (st.hasMoreTokens()) {
					st.nextToken();
					count++;
				}
				// in case the added text causes a new split or even several,
				// add the amount of empty tokens so the offset is right again.
				// then fix range
				// TODO: consider optimization: only the new tokens should be
				// parsed, the rest could be simply offset...
				if (count > 1) {
					for (int i = 1; i < count; i++)
						list.add(null);
					update();
				}
			}
		}

		int setToken(int index, int position, String str) {
			int len = str.length();
			int end = position + len;
			Token token;
			if (index < list.size()) {
				token = list.get(index);
				if (token == null) {
					token = new Token();
					list.set(index, token);
				}
			} else {
				token = new Token();
				list.add(token);
			}
			token.init(position, end, str);
			return end;
		}

		public TextRange get(int index) {
			Token token = list.get(index);
			return token.getRange();
		}
	}

	/**
	 * A list of text ranges for each character in this text range.
	 */
	class CharacterList extends TextRangeList<TextRange> {
		void update() {
			list.setSize(TextRange.this.getLength());
		}
		
		/**
		 * adjustStart is called when TextRange.prepend changes the start point
		 * of the ranges update here accordingly
		 */
		void adjustStart(int oldLength) {
			TextRange range = list.get(0);
			// the starting point of the range needs to be moved and then removed from the
			// list so a new one is return for this index
			if (range != null) {
				// move the range by the amount of change in the range
				int start = getStart() + (size() - oldLength);
				range.setRange(start, start + 1);
				list.set(0, null);
			}
		}

		/**
		 * adjustEnd is called when TextRange.append changes the end point of
		 * the ranges update here accordingly
		 * 
		 * @param oldLength
		 */
		void adjustEnd(int oldLength) {
			int index = oldLength - 1;
			if (index >= 0 && index < list.size()) {
				TextRange range = list.get(index);
				// the end point of the range needs to be moved so it
				// has length 1 again. it can stay in the list
				if (range != null) {
					int end = getStart() + oldLength;
					range.setRange(end - 1, end);
				}
			}
		}

		public TextRange get(int index) {
			TextRange range = list.get(index);
			if (range == null) {
				range = getSubRange(index, index + 1);
				list.set(index, range);
			}
			return range;
		}
	}

	/**
	 * Whenever a style changes happens that affects other styles, the style
	 * objects need to get replaces with new references, as otherwise the
	 * updates only seem reflected in the next execution cycle. This method
	 * takes care of this for us. Currently this is only in use in 
	 * CharacterStyle#setLeading / #setAutoLeading.
	 */
	protected void updateStyle() {
		if (characterStyle != null) {
			characterStyle.commit(false);
			// Swap with new instance, so changes do get reflected.
			characterStyle.changeHandle(nativeGetCharacterStyle(handle));
		}
		if (paragraphStyle != null) {
			paragraphStyle.commit(false);
			// Swap with new instance, so changes do get reflected.
			paragraphStyle.changeHandle(nativeGetParagraphStyle(handle));
		}
	}
}
