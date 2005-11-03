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
	
	private native Text nativeGetText(int handle, int index);
	
	class TextList implements ReadOnlyList {
		int length = 0;
		int version = -1;
		
		void update() {
			if (CommitManager.version != version) {
				length = nativeGetTexListLength(handle);
				version = CommitManager.version;
			}
		}

		public int getLength() {
			this.update();
			return length;
		}

		public Object get(int index) {
			return nativeGetText(handle, index);
		}

		public boolean isEmpty() {
			return getLength() == 0;
		}

		public ExtendedList subList(int fromIndex, int toIndex) {
			return Lists.createSubList(this, fromIndex, toIndex);
		}
	}
}
