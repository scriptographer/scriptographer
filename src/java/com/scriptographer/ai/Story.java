package com.scriptographer.ai;

import com.scriptographer.CommitManager;
import com.scriptographer.util.ExtendedArrayList;
import com.scriptographer.util.ExtendedList;
import com.scriptographer.util.Lists;
import com.scriptographer.util.ReadOnlyList;

public class Story extends AIObject {
	
	protected Story(int handle) {
		super(handle);
	}
	
	// TODO: add cashing for getRange, updating with CommitManager.version
	public native TextRange getRange();
	
	public native TextRange getSelection();

	public String getText() {
		return getRange().getText();
	}
	
	public void setText(String text) {
		getRange().setText(text);
	}

	TextList texts = null;
	
	public ReadOnlyList getTexts() {
		if (texts == null)
			texts = new TextList();
		return texts;
	}
	
	public native boolean equals(Object obj);
	
	protected native void finailze();
	
	private native int nativeGetTexListLength(int handle);
	
	private native Text nativeGetText(int handle, int index, Text curText);
	
	class TextList implements ReadOnlyList {
		ExtendedArrayList.List list;
		int version = -1;
		
		TextList() {
			list = new ExtendedArrayList.List();
		}
		
		void update() {
			if (CommitManager.version != version) {
				list.setSize(nativeGetTexListLength(handle));
				version = CommitManager.version;
			}
		}

		public int getLength() {
			this.update();
			return list.size();
		}

		public Object get(int index) {
			this.update();
			// native get returns the old cached value in case it's
			// referencing the same object, otherwise it wraps the new
			// story and returns it
			Text curText = (Text) list.get(index);
			Text text = nativeGetText(handle, index, curText);
			// update cache if story has changed
			if (text != curText)
				list.set(index, text);
			return text;
		}

		public boolean isEmpty() {
			return getLength() == 0;
		}

		public ExtendedList subList(int fromIndex, int toIndex) {
			return Lists.createSubList(this, fromIndex, toIndex);
		}
	}
}
