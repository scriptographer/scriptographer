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
 * $Id$
 */

package com.scriptographer.ai;

import com.scriptographer.CommitManager;
import com.scratchdisk.util.ExtendedList;
import com.scratchdisk.util.Lists;
import com.scratchdisk.util.ReadOnlyList;

/**
 * @author lehni
 */
public class TextStory extends AIObject {
	
	private Document document;
	private TextRange range = null;
	
	protected TextStory(int handle, int docHandle) {
		super(handle);
		document = Document.wrapHandle(docHandle);
	}
	
	public native int getLength();
	
	public native TextRange nativeGetRange();

	public TextRange getRange() {
		// once a range object is created, allways return the same reference
		// and swap handles instead. like this references in JS remain...
		TextRange newRange = nativeGetRange();
		if (range != null) {
			range.assignHandle(newRange);
		} else {
			range = newRange;
		}
		return range;
	}
	
	public native TextRange getRange(int start, int end);

	public native TextRange getSelection();
	
	public native int getIndex();
	
	public Document getDocument() {
		return document;
	}
	
	public ReadOnlyList getStories() {
		return document.getStories();
	}

	public String getContent() {
		return getRange().getContent();
	}
	
	public void setContent(String text) {
		getRange().setContent(text);
	}
	
	private static native void nativeSuspendReflow(int handle);
	
	private static native void nativeResumeReflow(int handle);
	
	/**
	 * reflow is suspended during script execution.
	 * when reflow() is called, it's quickly turned on and off again
	 * immediatelly afterwards.
	 */
	public void reflow() {
		// TODO: test if this does the trick? does resumeTextReflow immediatelly
		// reflow the text? If so, make reflowText native and merge these
		// functions on the native side
		nativeResumeReflow(handle);
		nativeSuspendReflow(handle);
	}

	TextFrameList textFrames = null;
	
	public ReadOnlyList getTextFrames() {
		if (textFrames == null)
			textFrames = new TextFrameList();
		return textFrames;
	}
	
	public native boolean equals(Object obj);
	
	protected native void finailze();
	
	protected native int nativeGetTexListLength(int handle);
	
	protected native TextFrame nativeGetTextFrame(int handle, int index);
	
	class TextFrameList implements ReadOnlyList {
		int length = 0;
		int version = -1;
		
		void update() {
			if (CommitManager.version != version) {
				length = nativeGetTexListLength(handle);
				version = CommitManager.version;
			}
		}

		public int size() {
			this.update();
			return length;
		}

		public Object get(int index) {
			return nativeGetTextFrame(handle, index);
		}

		public boolean isEmpty() {
			return size() == 0;
		}

		public ExtendedList getSubList(int fromIndex, int toIndex) {
			return Lists.createSubList(this, fromIndex, toIndex);
		}
	}
}
