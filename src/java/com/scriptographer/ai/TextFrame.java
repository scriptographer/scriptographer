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
 * File created on 23.10.2005.
 * 
 * $RCSfile: TextFrame.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/11/08 21:38:21 $
 */

package com.scriptographer.ai;

import com.scriptographer.util.ReadOnlyList;

public abstract class TextFrame extends Art {
	// AITextOrientation
	public static final int ORIENTATION_HORIZONTAL = 0;	
	public static final int ORIENTATION_VERTICAL = 1;

	// AITextType
	protected static final int 
		TEXTTYPE_UNKNOWN	= -1,
		TEXTTYPE_POINT	= 0,
		TEXTTYPE_AREA	= 1,
		TEXTTYPE_PATH	= 2;
	
	TextRange range = null;
	TextRange visibleRange = null;

	protected TextFrame(long handle) {
		super(handle);
	}
	
	// orientation
	public native int getOrientation();
	public native void setOrientation(int orientation);

	// TODO:
 	// AIAPI AIErr (*DoTextFrameHit)	( const AIHitRef hitRef, TextRangeRef*	textRange );
	
	public native Art createOutline();
	
	public native boolean link(TextFrame next);
	
	public native boolean unlinkBefore();
	
	public native boolean unlinkAfter();
	
	public native boolean isLinked();
	
	/**
	 * Returns the index of this text frame in the story's list of text frames
	 * @return
	 */
	public native int getIndex();
	
	/**
	 * Returns this text frame's story's index in the document's stories array
	 * 
	 * @return
	 */
	private native int getStoryIndex();
	
	public TextStory getStory() {
		// don't wrap directly. allways go through StoryList
		// to make sure we're not getting more than one reference
		// to the sam Story, so things can be cached there:
		return (TextStory) document.getStories().get(getStoryIndex());
	}
	
	private TextFrame getFrame(int index) {
		ReadOnlyList list = getStory().getTextFrames();
		if (index >= 0 && index < list.getLength()) {
			return (TextFrame) list.get(index);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the next text object in a story of various linked text frames
	 * @return
	 */
	public TextFrame getNextFrame() {
		return getFrame(getIndex() + 1);
	}
	
	/**
	 * Returns the previous text object in a story of various linked text frames
	 * @return
	 */
	public TextFrame getPreviousFrame() {
		return getFrame(getIndex() - 1);
	}

	// ATE
	
	// TODO: add cashing for getRange, updating with CommitManager.version
	public native TextRange nativeGetRange(boolean includeOverflow);
	
	/**
	 * In case there's an overflow in the text, this only returns a range
	 * over the visible characters, while getRange() returns one over the
	 * whole text
	 * @return
	 */
	public TextRange getVisibleRange() {
		// once a range object is created, allways return the same reference
		// and swap handles instead. like this references in JS remain...
		// as visible range or story flow might change, we need to refetch here..
		// TODO: check if its necessary
		TextRange newRange = nativeGetRange(false);
		if (visibleRange != null) {
			visibleRange.assignHandle(newRange);
		} else {
			visibleRange = newRange;
		}
		return visibleRange;
	}
	
	/**
	 * Returns a range for all the characters, even the invisble ones outside
	 * the container.
	 * 
	 * @return
	 */
	public TextRange getRange() {
		// once a range object is created, allways return the same reference
		// and swap handles instead. like this references in JS remain...
		// as story flow might change, we need to refetch here..
		// TODO: check if its necessary
		TextRange newRange = nativeGetRange(true);
		if (range != null) {
			range.assignHandle(newRange);
		} else {
			range = newRange;
		}
		return range;
	}
	
	/**
	 * return the same as getVisibleRange().getStart()
	 * @return
	 */
	public int getStart() {
		// don't create a cached version if it's not there already,
		// to avoid cache updating overhead
		if (visibleRange != null)
			return visibleRange.getStart();
		else
			return nativeGetRange(false).getStart();
	}
	
	/**
	 * return the same as getVisibleRange().getEnd()
	 * @return
	 */
	public int getEnd() {
		// don't create a cached version if it's not there already,
		// to avoid cache updating overhead
		if (visibleRange != null)
			return visibleRange.getEnd();
		else
			return nativeGetRange(false).getEnd();
	}
	
	public String getContent() {
		return getRange().getContent();
	}
	
	public void setContent(String text) {
		getRange().setContent(text);
	}
	
	public CharacterStyle getCharacterStyle() {
		return getRange().getCharacterStyle();
	}
	
	public void setCharacterStyle(CharacterStyle style) {
		getRange().setCharacterStyle(style);
	}
	
	public ParagraphStyle getParagraphStyle() {
		return getRange().getParagraphStyle();
	}
	
	public void setParagraphStyle(ParagraphStyle style) {
		getRange().setParagraphStyle(style);
	}

	public native TextRange getSelection();
	
	public native boolean equals(Object obj);
	
	// TODO:
	//	ATEErr (*GetTextLinesIterator) ( TextFrameRef textframe, TextLinesIteratorRef* ret);

	//	ATEErr (*GetLineOrientation) ( TextFrameRef textframe, LineOrientation* ret);
	//	ATEErr (*SetLineOrientation) ( TextFrameRef textframe, LineOrientation lineOrientation);
	
	/** Check if this frame is selected.  To set the selection, you have to use application specific
	API for that.  In Illustrator case, you can use AIArtSuite to set the selection.
	*/
	//	ATEErr (*GetSelected) ( TextFrameRef textframe, bool* ret);
	//	ATEErr (*GetMatrix) ( TextFrameRef textframe, ASRealMatrix* ret);

	public native float getSpacing();
	public native void setSpacing(float spacing);
	
	public native boolean getOpticalAlignment();
	public native void setOpticalAlignment(boolean active);
}
