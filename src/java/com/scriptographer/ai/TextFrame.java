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
 * File created on 23.10.2005.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.list.ReadOnlyList;
import com.scriptographer.CommitManager;

/**
 * @author lehni
 */
public abstract class TextFrame extends Art {
	// AITextOrientation
	public static final short
		ORIENTATION_HORIZONTAL = 0,
		ORIENTATION_VERTICAL = 1;

	// AITextType
	protected static final short
		TEXTTYPE_UNKNOWN = -1,
		TEXTTYPE_POINT = 0,
		TEXTTYPE_AREA = 1,
		TEXTTYPE_PATH = 2;

	TextRange range = null;
	TextRange visibleRange = null;

	protected TextFrame(int handle) {
		super(handle);
	}

	/**
	 * @jsbean The orientation of the text in the text frame as
	 * @jsbean specified by the TextFrame.ORIENTATION_* static properties.
	 * @return TextFrame.ORIENTATION_*
	 */
	public native short getOrientation();
	public native void setOrientation(short orientation);

	// TODO:
	 // AIAPI AIErr (*DoTextFrameHit)	( const AIHitRef hitRef, TextRangeRef*	textRange );

	private native Art nativeCreateOutline();

	/**
	 * Converts the text in the text frame to outlines. Unlike the Illustrator
	 * 'Create Outlines' action, this won't remove the text frame.
	 * 
	 * @return An Art item containing the outlined text.
	 */
	public Art createOutline() {
		// apply changes and reflow the layout before creating outlines
		// All styles regarding this story need to be commited, as
		// CharacterStyle uses Story as the commit key.
		CommitManager.commit(this.getStory());
		document.reflowText();
		return nativeCreateOutline();
	}

	/**
	 * Links the supplied text frame to this one.
	 * @param next The text frame that will be linked.
	 * @return True if the text frame was linked, false otherwise
	 */
	public native boolean link(TextFrame next);

	private native boolean nativeUnlink(boolean before, boolean after);
	
	/**
	 * Unlinks the text frame from its current story.
	 * 
	 * @return True if the operation was successful, false otherwise
	 */
	public boolean unlink() {
		return nativeUnlink(true, true);
	}

	/**
	 * Unlinks the text frame from its current story and breaks up the story
	 * into two parts before the text frame.
	 * 
	 * @return True if the operation as successful, false otherwise
	 */
	public boolean unlinkBefore() {
		return nativeUnlink(true, false);
	}

	/**
	 * Unlinks the text frame from its current story and breaks up the story
	 * into two parts after the text frame.
	 * 
	 * @return True if the operation as successful, false otherwise
	 */
	public boolean unlinkAfter() {
		return nativeUnlink(false, true);
	}

	/**
	 * @jsbean Returns <code>true</code> if the text frame is
	 *         linked, false otherwise.
	 */
	public native boolean isLinked();

	/**
	 * @jsbean Returns the index of this text frame in the story's list of text frames.
	 */
	public native int getIndex();

	/**
	 * @jsbean Returns this text frame's story's index in the document's stories array.
	 */
	private native int getStoryIndex();

	/**
	 * @jsbean Returns the Story that the text frame belongs to.
	 */
	public TextStory getStory() {
		// don't wrap directly. allways go through StoryList
		// to make sure we're not getting more than one reference
		// to the sam Story, so things can be cached there:
		int index = getStoryIndex();
		ReadOnlyList list = document.getStories();
		if (index >= 0 && index < list.size())
			return (TextStory) list.get(index);
		return null;
	}

	private TextFrame getFrame(int index) {
		TextStory story = getStory();
		if (story != null) {
			ReadOnlyList list = story.getTextFrames();
			if (index >= 0 && index < list.size())
				return (TextFrame) list.get(index);
		}
		return null;
	}

	/**
	 * @jsbean Returns the next text frame in a story of various linked text frames
	 */
	public TextFrame getNextFrame() {
		return getFrame(getIndex() + 1);
	}

	/**
	 * @jsbean Returns the previous text frame in a story of various linked text frames
	 */
	public TextFrame getPreviousFrame() {
		return getFrame(getIndex() - 1);
	}

	// ATE
	/**
	 * @jshide true
	 * @jshide bean
	 */
	public native int nativeGetRange(boolean includeOverflow);

	/**
	 * @jsbean In case there's an overflow in the text, this only returns a range
	 * @jsbean over the visible characters, while getRange() returns one over the
	 * @jsbean whole text.
	 */
	public TextRange getVisibleRange() {
		// once a range object is created, allways return the same reference
		// and swap handles instead. like this references in JS remain...
		if (visibleRange == null) {
			visibleRange = new TextRange(nativeGetRange(false), document);
		} else if (visibleRange.version != CommitManager.version) {
			visibleRange.changeHandle(nativeGetRange(false));
		}
		return visibleRange;
	}

	/**
	 * @jsbean Returns a text range for all the characters, even the invisible ones outside
	 * @jsbean the container.
	 */
	public TextRange getRange() {
		// once a range object is created, allways return the same reference
		// and swap handles instead. like this references in JS remain...
		if (range == null) {
			range = new TextRange(nativeGetRange(true), document);
		} else if (range.version != CommitManager.version) {
			range.changeHandle(nativeGetRange(true));
		}
		return range;
	}

	/**
	 * @jsbean Returns the index of the first visible character of the text frame.
	 * @jsbean (this is the equivalent of calling TextFrame.visibleRange.start)
	 */
	public int getStart() {
		return getVisibleRange().getStart();
	}

	/**
	 * @jsbean Returns the index of the last visible character of the text frame.
	 * @jsbean (this is the equivalent of calling TextFrame.visibleRange.end)
	 */
	public int getEnd() {
		return getVisibleRange().getEnd();
	}

	/**
	 * @jsbean The text contents of the text frame.
	 */
	public String getContent() {
		return getRange().getContent();
	}

	public void setContent(String text) {
		getRange().setContent(text);
	}

	/**
	 * @jsbean The character style of the text frame.
	 */
	public CharacterStyle getCharacterStyle() {
		return getRange().getCharacterStyle();
	}

	public void setCharacterStyle(CharacterStyle style) {
		getRange().setCharacterStyle(style);
	}

	/**
	 * @jsbean The paragraph style of the text frame.
	 */
	public ParagraphStyle getParagraphStyle() {
		return getRange().getParagraphStyle();
	}

	public void setParagraphStyle(ParagraphStyle style) {
		getRange().setParagraphStyle(style);
	}

	/**
	 * @jsbean Returns the selected text of the text frame as a text range
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
	 * @jsbean The line spacing value for the text frame in points.
	 */
	public native float getSpacing();
	public native void setSpacing(float spacing);

	/**
	 * @jsbean Specifies wether to use optical alignment within the text frame.
	 *         Optical aligment hangs punctuation outside the edges of a text
	 *         frame.
	 */
	public native boolean getOpticalAlignment();
	public native void setOpticalAlignment(boolean active);
}
