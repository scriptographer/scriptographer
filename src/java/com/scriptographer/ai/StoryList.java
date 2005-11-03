package com.scriptographer.ai;

import com.scriptographer.util.ExtendedArrayList;
import com.scriptographer.util.ExtendedList;
import com.scriptographer.util.Lists;
import com.scriptographer.util.ReadOnlyList;

class StoryList extends AIObject implements ReadOnlyList {
	ExtendedArrayList.List list;
	
	StoryList(int handle) {
		super(handle);
		list = new ExtendedArrayList.List();
	}

	private native int nativeGetLength(int handle);

	public int getLength() {
		return nativeGetLength(handle);
	}
	
	private native Story nativeGet(int handle, int index, Story curStory);

	public Object get(int index) {
		// update buffer length
		list.setSize(nativeGetLength(handle));
		// native get returns the old cached value in case it's
		// referencing the same object, otherwise it wraps the new
		// story and returns it
		Story curStory = (Story) list.get(index);
		Story story = nativeGet(handle, index, curStory);
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
