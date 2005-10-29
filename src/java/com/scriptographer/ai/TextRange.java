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
 * $Revision: 1.1 $
 * $Date: 2005/10/29 10:18:38 $
 */

package com.scriptographer.ai;

public class TextRange extends AIObject {
	
	public TextRange(int handle) {
		super(handle);
	}
	
	public native int getStart();
	public native void setStart(int start);
	
	public native int getEnd();
	public native void setEnd(int end);
	
	public native int getSize();
	
	public native void insertBefore(String text);
	public native void insertAfter(String text);
	
	public native void insertBefore(TextRange range);
	public native void insertAfter(TextRange range);
	
	/**
	 *  This method will delete all the characters in that range.
	 */
	public native void remove();
	
	public native String getContent();
	
	public void setContent(String content) {
		remove();
		insertAfter(content);
	}
	
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
	
	public native Object clone();
	
	// CaseChangeType
	public static final int CASE_UPPER = 0;
	public static final int CASE_LOWER = 1;
	public static final int CASE_TITLE = 2;
	public static final int CASE_SENTENCE = 3;

	/**
	 * 
	 * @param type TextRange.CASE_*
	 */
	public native void changeCase(int type);
	
	public native void fitHeadlines();

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
}
