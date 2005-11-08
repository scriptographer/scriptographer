package com.scriptographer.ai;

import com.scriptographer.CommitManager;
import com.scriptographer.util.ExtendedList;
import com.scriptographer.util.Lists;
import com.scriptographer.util.ReadOnlyList;

public class TextStory extends AIObject {
	
	private Document document;
	private TextRange range = null;
	
	protected TextStory(int handle, int documentHandle) {
		super(handle);
		document = Document.wrapHandle(documentHandle);
	}
	
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
	
	/**
	 * This is called from sub ranges whenever a change to the range's bounds 
	 * are executed. this assures that the parent range gets updated properly
	 * TODO: Benchmark. I hope this is no performance issue...
	 */
	protected void updateRange() {
		if (range != null)
			range.assignHandle(nativeGetRange());
	}

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

		public int getLength() {
			this.update();
			return length;
		}

		public Object get(int index) {
			return nativeGetTextFrame(handle, index);
		}

		public boolean isEmpty() {
			return getLength() == 0;
		}

		public ExtendedList subList(int fromIndex, int toIndex) {
			return Lists.createSubList(this, fromIndex, toIndex);
		}
	}
}
