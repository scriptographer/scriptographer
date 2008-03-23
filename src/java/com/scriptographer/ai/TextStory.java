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
 * $Id$
 */

package com.scriptographer.ai;

import com.scriptographer.CommitManager;
import com.scratchdisk.list.ExtendedList;
import com.scratchdisk.list.Lists;
import com.scratchdisk.list.ReadOnlyList;

/**
 * @author lehni
 */
public class TextStory extends NativeObject {
	
	private Document document;
	private TextRange range = null;
	
	protected TextStory(int handle, Document document) {
		super(handle);
		this.document = document;
	}
	
	public native int getLength();
	
	private native int nativeGetRange();

	public TextRange getRange() {
		// once a range object is created, always return the same reference
		// and swap handles instead. like this references in JS remain...
		if (range == null) {
			range = new TextRange(nativeGetRange(), document);
		} else if (range.version != CommitManager.version) {
			range.changeHandle(nativeGetRange());
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
	
	/**
	 * Text reflow is suspended during script execution.
	 * reflow forces the text story's layout to be reflown.
	 */
	public native void reflow();

	TextFrameList textFrames = null;
	
	public ReadOnlyList getTextFrames() {
		if (textFrames == null)
			textFrames = new TextFrameList();
		return textFrames;
	}
	
	public native boolean equals(Object obj);
	
	protected native void release();
	
	protected void finalize() {
		release();
	}
	
	protected void changeHandle(int newHandle) {
		release(); // release old handle
		handle = newHandle;
	}
	
	protected native int nativeGetTexListLength(int handle);
	
	protected native TextFrame nativeGetTextFrame(int handle, int index);
	
	class TextFrameList implements ReadOnlyList {
		int length = 0;
		int version = -1;
		
		void update() {
			if (version != CommitManager.version) {
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
