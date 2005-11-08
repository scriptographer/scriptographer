package com.scriptographer.ai;

import com.scriptographer.util.ExtendedArrayList;
import com.scriptographer.util.ExtendedList;
import com.scriptographer.util.Lists;
import com.scriptographer.util.ReadOnlyList;

class TextStoryList extends AIObject implements ReadOnlyList {
	ExtendedArrayList.List list;
	
	TextStoryList(int handle) {
		super(handle);
		list = new ExtendedArrayList.List();
	}

	private native int nativeGetLength(int handle);

	public int getLength() {
		return nativeGetLength(handle);
	}
	
	private native TextStory nativeGet(int handle, int index, TextStory curStory);

	public Object get(int index) {
		// update buffer length
		list.setSize(nativeGetLength(handle));
		// native get returns the old cached value in case it's
		// referencing the same object, otherwise it wraps the new
		// story and returns it
		TextStory curStory = (TextStory) list.get(index);
		TextStory story = nativeGet(handle, index, curStory);
		// update cache if story has changed
		if (story != curStory)
			list.set(index, story);
		return story;
	}

	public boolean isEmpty() {
		return nativeGetLength(handle) == 0;
	}

	public ExtendedList subList(int fromIndex, int toIndex) {
		return Lists.createSubList(this, fromIndex, toIndex);
	}
}
