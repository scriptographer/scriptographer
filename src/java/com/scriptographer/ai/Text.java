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
 * $Revision: 1.1 $
 * $Date: 2005/10/29 10:18:38 $
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

	/*
	// gets you ATE::TextRangeRef given AIArtHandle.  If this frame is the last one in the chain AND 
	// it has overflow text, then ITextRange(textRange).getEnd() will be include the hidden text.
	AIAPI AIErr (*GetATETextRange)			( AIArtHandle textFrame, TextRangeRef*	textRange );
	// gets you ATE::TextFrameRef given AIArtHandle.
	AIAPI AIErr (*GetATETextFrame)			( AIArtHandle textFrame, TextFrameRef*	ATE_textFrame );
	AIAPI AIErr (*GetAITextFrame)			( TextFrameRef	ATE_textFrame, AIArtHandle* textFrame );
	AIAPI AIErr (*GetATETextSelection)		( AIArtHandle textFrame, TextRangesRef*	Selection );
	AIAPI AIErr (*DoTextFrameHit)	( const AIHitRef hitRef, TextRangeRef*	textRange );
	
	 */
	
	public native Art createOutline();
	
	/*
	
	// you can link two in path text objects, after link they will share the same story
	AIAPI AIErr (*Link)				( AIArtHandle baseFrame, AIArtHandle nextFrame );

	// you can unlink a text object from its current story, 
	// if before is true, after is false, frames in the story will be broken into two groups right before this frame;
	// if before is false, after is true, frames in the story will be broken into two groups right after this frame;
	// if before is true, after is true, frame will be removed from the story and be itself;
	// if before is false, after is false, nothing happens.
	AIAPI AIErr (*Unlink)			( AIArtHandle frame, bool before, bool after );

	/** Create a new text object based on ATE (Adobe Text Engine) version 2 blob data, 
		specifying paint order, and prep object as they are in NewArt() in AIArtSuite. 
		data and size refer to binary blob. index is the index of text object in the 
		SLO TextObject list. List is zero base. */
//	AIAPI AIErr (*CreateATEV2Text)	( short paintOrder, AIArtHandle prep, void* data, long size, long index, AIArtHandle *newTextFrame );

	/** return ATE (Adobe Text Engine) version 2 blob data for the current arwork.
		client is in charge of free memory for data. Please call SPFreeBlock(*data). */
//	AIAPI AIErr (*GetATEV2Data)		( void** data, long* size );

	/** return the story index of a given frame. index will be set to -1 
		if the frame does not belong to the current artwork or the frame is part of 
		result art of plugin group or styled art. */
//	AIAPI AIErr (*GetStoryIndex)	( AIArtHandle frame, long* index );

	/** return the frame index of a given frame in its story. index will be set to -1 
		if the frame does not belong to the current artwork or the frame is part of 
		result art of plugin group or styled art. */
//	AIAPI AIErr (*GetFrameIndex)	( AIArtHandle frame, long* index );

	/** check whether a give frame is part of a linked text */
//	AIAPI AIErr (*PartOfLinkedText)	( AIArtHandle frame, bool* linked );
	
	// ATE
	
	public native TextRange getRange(boolean includeOverflow);
	
	public native TextRanges getSelection();
	
	//	ATEErr (*GetTextRange) ( TextFrameRef textframe, bool bIncludeOverflow, TextRangeRef* ret);
	//	ATEErr (*GetTextLinesIterator) ( TextFrameRef textframe, TextLinesIteratorRef* ret);
	//	ATEErr (*IsEqual) ( TextFrameRef textframe, const TextFrameRef anotherFrame, bool* ret);
	//	ATEErr (*GetType) ( TextFrameRef textframe, FrameType* ret);

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
