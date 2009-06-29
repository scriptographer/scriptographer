/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 23.10.2005.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.list.ReadOnlyList;
import com.scratchdisk.util.IntegerEnumUtils;
import com.scriptographer.CommitManager;

/**
 * @author lehni
 */
public abstract class TextItem extends Item {

	// AITextType
	protected static final short
		TEXTTYPE_UNKNOWN = -1,
		TEXTTYPE_POINT = 0,
		TEXTTYPE_AREA = 1,
		TEXTTYPE_PATH = 2;

	TextRange range = null;
	TextRange visibleRange = null;

	protected TextItem(int handle, boolean created) {
		super(handle);
		if (created)
			document = Document.getWorkingDocument();
	}

	protected void commit(boolean invalidate) {
		CommitManager.commit(this.getStory());
		super.commit(invalidate);
	}

	private native int nativeGetOrientation();
	private native void nativeSetOrientation(int orientation);

	/**
	 * The orientation of the text in the text frame.
	 */
	public TextOrientation getOrientation() {
		return (TextOrientation) IntegerEnumUtils.get(TextOrientation.class,
				nativeGetOrientation());
	}

	public void setOrientation(TextOrientation orientation) {
		nativeSetOrientation(orientation.value);
	}

	// TODO:
	 // AIAPI AIErr (*DoTextFrameHit)	( const AIHitRef hitRef, TextRangeRef*	textRange );

	private native Item nativeCreateOutline();

	/**
	 * Converts the text in the text frame to outlines. Unlike the Illustrator
	 * 'Create Outlines' action, this won't remove the text frame.
	 * 
	 * @return A new item containing the outlined text.
	 */
	public Item createOutline() {
		// Apply changes and reflow the layout before creating outlines
		// All styles regarding this story need to be committed, as
		// CharacterStyle uses Story as the commit key.
		CommitManager.commit(this.getStory());
		// This should not be needed since TextRange takes care of it
		// when committing already:
		// document.reflowText();
		return nativeCreateOutline();
	}

	/**
	 * Links the supplied text frame to this one.
	 * @param next The text frame that will be linked.
	 * @return {@true if the text frame was linked}
	 */
	public native boolean link(TextItem next);

	private native boolean nativeUnlink(boolean before, boolean after);
	
	/**
	 * Unlinks the text frame from its current story.
	 * 
	 * @return {@true if the operation as successful}
	 */
	public boolean unlink() {
		return nativeUnlink(true, true);
	}

	/**
	 * Unlinks the text frame from its current story and breaks up the story
	 * into two parts before the text frame.
	 * 
	 * @return {@true if the operation as successful}
	 */
	public boolean unlinkBefore() {
		return nativeUnlink(true, false);
	}

	/**
	 * Unlinks the text frame from its current story and breaks up the story
	 * into two parts after the text frame.
	 * 
	 * @return {@true if the operation as successful}
	 */
	public boolean unlinkAfter() {
		return nativeUnlink(false, true);
	}

	/**
	 * Returns {@true if the text frame is linked}
	 */
	public native boolean isLinked();

	/**
	 * Returns the index of this text frame in the {@link TextStory#getTextFrames()} array.
	 */
	public native int getIndex();

	/**
	 * Returns this text frame's story's index in the document's stories array.
	 */
	private native int getStoryIndex();

	/**
	 * Returns the Story that the text frame belongs to.
	 */
	public TextStory getStory() {
		// don't wrap directly. always go through StoryList
		// to make sure we're not getting more than one reference
		// to the same Story, so things can be cached there:
		int index = getStoryIndex();
		ReadOnlyList list = document.getStories();
		if (index >= 0 && index < list.size())
			return (TextStory) list.get(index);
		return null;
	}

	private TextItem getFrame(int index) {
		TextStory story = getStory();
		if (story != null) {
			ReadOnlyList list = story.getTextFrames();
			if (index >= 0 && index < list.size())
				return (TextItem) list.get(index);
		}
		return null;
	}

	/**
	 * Returns the next text frame in a story of various linked text frames.
	 */
	public TextItem getNextFrame() {
		return getFrame(getIndex() + 1);
	}

	/**
	 * Returns the previous text frame in a story of various linked text frames.
	 */
	public TextItem getPreviousFrame() {
		return getFrame(getIndex() - 1);
	}

	// ATE
	/**
	 * @jshide
	 */
	public native int nativeGetRange(boolean includeOverflow);

	/**
	 * In case there's an overflow in the text, this only returns a range
	 * over the visible characters, while {@link TextItem#getRange()} returns one over the
	 * whole text.
	 */
	public TextRange getVisibleRange() {
		// once a range object is created, always return the same reference
		// and swap handles instead. like this references in JS remain...
		if (visibleRange == null) {
			visibleRange = new TextRange(nativeGetRange(false), document);
		} else if (visibleRange.version != CommitManager.version) {
			visibleRange.changeHandle(nativeGetRange(false));
		}
		return visibleRange;
	}

	/**
	 * Returns a text range for all the characters, even the invisible ones outside
	 * the container.
	 */
	public TextRange getRange() {
		// once a range object is created, always return the same reference
		// and swap handles instead. like this references in JS remain...
		if (range == null) {
			range = new TextRange(nativeGetRange(true), document);
		} else if (range.version != CommitManager.version) {
			range.changeHandle(nativeGetRange(true));
		}
		return range;
	}

	/**
	 * Returns the index of the first visible character of the text frame.
	 * (this is the equivalent of calling TextFrame.visibleRange.start)
	 */
	public int getStart() {
		return getVisibleRange().getStart();
	}

	/**
	 * Returns the index of the last visible character of the text frame.
	 * (this is the equivalent of calling TextFrame.visibleRange.end)
	 */
	public int getEnd() {
		return getVisibleRange().getEnd();
	}

	/**
	 * The text contents of the text frame.
	 */
	public String getContent() {
		return getRange().getContent();
	}

	public void setContent(String text) {
		getRange().setContent(text);
	}

	/**
	 * The character style of the text frame.
	 */
	public CharacterStyle getCharacterStyle() {
		return getRange().getCharacterStyle();
	}

	public void setCharacterStyle(CharacterStyle style) {
		getRange().setCharacterStyle(style);
	}

	/**
	 * The paragraph style of the text frame.
	 */
	public ParagraphStyle getParagraphStyle() {
		return getRange().getParagraphStyle();
	}

	public void setParagraphStyle(ParagraphStyle style) {
		getRange().setParagraphStyle(style);
	}

	/**
	 * Returns the selected text of the text frame as a text range.
	 */
	public native TextRange getSelection();

	public native boolean equals(Object obj);

	// TODO:
	//	ATEErr (*GetTextLinesIterator) ( TextFrameRef textframe, TextLinesIteratorRef* ret);

	//	ATEErr (*GetLineOrientation) ( TextFrameRef textframe, LineOrientation* ret);
	//	ATEErr (*SetLineOrientation) ( TextFrameRef textframe, LineOrientation lineOrientation);

	/* Check if this frame is selected.  To set the selection, you have to use application specific
	API for that.  In Illustrator case, you can use AIArtSuite to set the selection.
	*/
	//	ATEErr (*GetSelected) ( TextFrameRef textframe, bool* ret);

	/**
	 * The line spacing value for the text frame in points.
	 */
	public native float getSpacing();
	public native void setSpacing(float spacing);

	/**
	 * Specifies whether to use optical alignment within the text frame. Optical
	 * alignment hangs punctuation outside the edges of a text frame.
	 * 
	 * @return {@true if the text frame uses optical alignment}
	 */
	public native boolean getOpticalAlignment();
	public native void setOpticalAlignment(boolean active);
}
