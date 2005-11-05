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
 * $RCSfile: Text.java,v $
 * $Author: lehni $
 * $Revision: 1.5 $
 * $Date: 2005/11/05 00:50:41 $
 */

package com.scriptographer.ai;

public abstract class Text extends Art {
	// AITextOrientation
	public static final int ORIENTATION_HORIZONTAL = 0;	
	public static final int ORIENTATION_VERTICAL = 1;

	// AITextType
	protected static final int 
		TEXTTYPE_UNKNOWN	= -1,
		TEXTTYPE_POINT	= 0,
		TEXTTYPE_AREA	= 1,
		TEXTTYPE_PATH	= 2;

	protected Text(long handle) {
		super(handle);
	}
	
	// orientation
	public native int getOrientation();
	public native void setOrientation(int orientation);

	// TODO:
 	// AIAPI AIErr (*DoTextFrameHit)	( const AIHitRef hitRef, TextRangeRef*	textRange );
	
	public native Art createOutline();
	
	public native boolean link(Text next);
	
	public native boolean unlinkBefore();
	
	public native boolean unlinkAfter();
	
	public native boolean isLinked();
	
	public native int getStoryIndex();
	
	public native int getTextIndex();
	
	public Story getStory() {
		// don't wrap directly. allways go through StoryList
		// to make sure we're not getting more than one reference
		// to the sam Story, so things can be cached there:
		return (Story) document.getStories().get(getStoryIndex());
	}

	// ATE
	
	// TODO: add cashing for getRange, updating with CommitManager.version
	public native TextRange getRange(boolean includeOverflow);
	
	public TextRange getRange() {
		return getRange(true);
	}
	
	public String getContent() {
		return getRange(true).getContent();
	}
	
	public void setContent(String text) {
		getRange(true).setContent(text);
	}
	
	public CharacterStyle getCharacterStyle() {
		return getRange(true).getCharacterStyle();
	}
	
	public void setCharacterStyle(CharacterStyle style) {
		getRange(true).setCharacterStyle(style);
	}
	
	public ParagraphStyle getParagraphStyle() {
		return getRange(true).getParagraphStyle();
	}
	
	public void setParagraphStyle(ParagraphStyle style) {
		getRange(true).setParagraphStyle(style);
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
