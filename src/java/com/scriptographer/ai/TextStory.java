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
 * $Id$
 */

package com.scriptographer.ai;

import java.util.Iterator;

import com.scriptographer.CommitManager;
import com.scratchdisk.list.ExtendedList;
import com.scratchdisk.list.ListIterator;
import com.scratchdisk.list.Lists;
import com.scratchdisk.list.ReadOnlyList;

/**
 * @author lehni
 */
public class TextStory extends DocumentObject implements TextStoryProvider {
	
	private TextRange range = null;
	
	protected TextStory(int handle, Document document) {
		super(handle, document);
	}
	
	/**
	 * The amount of characters in the story.
	 */
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

	public native TextRange getSelectedRange();
	
	public native int getIndex();
	
	public TextStoryList getStories() {
		return document.getStories(this, false);
	}

	/*
	 * @see TextStoryProvider.
	 */
	public int getStoryHandle() {
		return handle;
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

	TextItemList textItems = null;

	public ReadOnlyList<TextItem> getTextItems() {
		if (textItems == null)
			textItems = new TextItemList();
		return textItems;
	}
	
	public native boolean equals(Object obj);
	
	private native void nativeRelease(int handle);
	
	protected void finalize() {
		nativeRelease(handle);
		handle = 0;
	}
	
	protected void changeHandle(int newHandle) {
		nativeRelease(handle); // release old handle
		handle = newHandle;
	}
	
	protected native int nativeGetTexListLength(int handle);
	
	protected native TextItem nativeGetTextItem(int storyHandle, int docHandle, int index);
	
	class TextItemList implements ReadOnlyList<TextItem> {
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

		public TextItem get(int index) {
			return nativeGetTextItem(handle, document.handle, index);
		}

		public boolean isEmpty() {
			return size() == 0;
		}

		public ExtendedList<TextItem> getSubList(int fromIndex, int toIndex) {
			return Lists.createSubList(this, fromIndex, toIndex);
		}

		public Iterator<TextItem> iterator() {
			return new ListIterator<TextItem>(this);
		}

		public TextItem getFirst() {
			return size() > 0 ? get(0) : null;
		}

		public TextItem getLast() {
			int size = size();
			return size > 0 ? get(size - 1) : null;
		}

		public Class<?> getComponentType() {
			return TextItem.class;
		}
	}
}
