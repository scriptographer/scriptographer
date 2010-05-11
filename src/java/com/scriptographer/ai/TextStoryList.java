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

import com.scratchdisk.list.ExtendedList;
import com.scratchdisk.list.ListIterator;
import com.scratchdisk.list.Lists;
import com.scratchdisk.list.ReadOnlyList;
import com.scratchdisk.util.ArrayList;
import com.scriptographer.CommitManager;

/**
 * @author lehni
 * 
 * @jshide
 */
class TextStoryList extends DocumentObject implements ReadOnlyList<TextStory> {
	ArrayList<TextStory> list;
	protected int version = CommitManager.version;

	TextStoryList(int handle, Document document) {
		super(handle, document);
		list = new ArrayList<TextStory>();
	}

	private native int nativeSize(int handle);

	public int size() {
		return handle != 0 ? nativeSize(handle) : 0;
	}

	private native int nativeGet(int handle, int index, int curStoryHandle);

	public TextStory get(int index) {
		// update buffer length
		list.setSize(size());
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
		return size() == 0;
	}

	public ExtendedList<TextStory> getSubList(int fromIndex, int toIndex) {
		return Lists.createSubList(this, fromIndex, toIndex);
	}

	public Iterator<TextStory> iterator() {
		return new ListIterator<TextStory>(this);
	}

	protected void changeStoryHandle(TextStory story, int index) {
		int newHandle = nativeGet(handle, index, story.handle);
		if (story.handle != newHandle)
			story.changeHandle(newHandle);
	}

	protected native void nativeRelease(int handle);

	protected void finalize() {
		if (handle != 0) {
			nativeRelease(handle);
			handle = 0;
		}
	}

	protected void changeHandle(int newHandle) {
		if (handle != 0)
			nativeRelease(handle); // Release old handle
		handle = newHandle;
		version = CommitManager.version;
	}

	public TextStory getFirst() {
		return size() > 0 ? get(0) : null;
	}

	public TextStory getLast() {
		int size = size();
		return size > 0 ? get(size - 1) : null;
	}

	public Class<?> getComponentType() {
		return TextStory.class;
	}
}
