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

import com.scratchdisk.list.ArrayList;
import com.scratchdisk.list.ExtendedList;
import com.scratchdisk.list.Lists;
import com.scratchdisk.list.ReadOnlyList;

/**
 * @author lehni
 */
class TextStoryList extends NativeObject implements ReadOnlyList {
	ArrayList.List list;
	Document document;
	
	TextStoryList(int handle, Document document) {
		super(handle);
		this.document = document;
		list = new ArrayList.List();
	}

	private native int nativeSize(int handle);

	public int size() {
		return nativeSize(handle);
	}

	private native int nativeGet(int handle, int index, int curStoryHandle);

	public Object get(int index) {
		// update buffer length
		list.setSize(nativeSize(handle));
		// native get returns the old cached value in case it's
		// referencing the same object, otherwise it wraps the new
		// story and returns it
		TextStory story = (TextStory) list.get(index);
		int oldHandle = story != null ? story.handle : 0;
		int newHandle = nativeGet(handle, index, oldHandle);
		// update cache if story has changed
		if (newHandle != oldHandle) {
			story = new TextStory(newHandle, document);
			list.set(index, story);
		}
		return story;
	}

	public boolean isEmpty() {
		return nativeSize(handle) == 0;
	}

	public ExtendedList getSubList(int fromIndex, int toIndex) {
		return Lists.createSubList(this, fromIndex, toIndex);
	}

	protected void changeStoryHandle(TextStory story, int index) {
		int newHandle = nativeGet(handle, index, story.handle);
		if (story.handle != newHandle)
			story.changeHandle(newHandle);
	}
}
