/*
 * Scriptographer
 * ExtendedJavaList.java
 * 
 * Created by  Lehni on 11.02.2005.
 * Copyright (c) 2004 http://www.scratchdisk.com. All rights reserved.
 * 
 */
package com.scriptographer.util;

/*
 * Adds setSize and the public removeRange to java.util.ArrayList:
 */
public class ExtendedJavaList extends java.util.ArrayList {
	public ExtendedJavaList() {
		super();
	}

	public ExtendedJavaList(int initialCapacity) {
		super(initialCapacity);
	}

	public void setSize(int newSize) {
		int size = size();
		if (newSize > size) {
			// fill with null:
			ensureCapacity(newSize);
			for (int i = size; i < newSize; i++)
				add(i, null);
		} else if (newSize < size) {
			// remove the unneeded beziers:
			removeRange(newSize, size);
		}
	}

	public void removeRange(int fromIndex, int toIndex) {
		super.removeRange(fromIndex, toIndex);
	}
}
