/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
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
		TextStory story = list.get(index);
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
