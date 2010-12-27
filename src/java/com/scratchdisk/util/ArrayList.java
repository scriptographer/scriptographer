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
 * 
 * File created on Aug 13, 2009.
 */

package com.scratchdisk.util;

import java.util.Collection;

/**
 * com.scratchdisk.util.ArrayList extends java.util.ArrayList and adds some
 * useful public methods, such as {@link #setSize}, {@link #remove}. 
 * 
 * @author lehni
 */
public class ArrayList<E> extends java.util.ArrayList<E> {
	public ArrayList() {
		super();
	}

	public ArrayList(int initialCapacity) {
		super(initialCapacity);
	}

	public ArrayList(Collection<? extends E> c) {
		super(c);
	}

	public void setSize(int newSize) {
		int size = size();
		if (newSize > size) {
			// fill with null:
			ensureCapacity(newSize);
			for (int i = size; i < newSize; i++)
				add(i, null);
		} else if (newSize < size) {
			removeRange(newSize, size);
		}
	}

	public void remove(int fromIndex, int toIndex) {
		removeRange(fromIndex, toIndex);
	}
}