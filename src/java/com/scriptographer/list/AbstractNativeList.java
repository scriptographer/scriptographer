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
 * File created on 16.02.2005.
 */

package com.scriptographer.list;

import com.scratchdisk.list.AbstractExtendedList;
import com.scratchdisk.util.ArrayList;

/**
 * AbstractFetchList defines fetch and fetch(fromIndex, toIndex), which are called
 * every time the a range of elements needs to be available immediately.
 * Subclassed by SegmentList and CurveList
 * 
 * @author lehni
 */
public abstract class AbstractNativeList<E> extends AbstractExtendedList<E> {
	protected int size;
	protected ArrayList<E> list;

	protected AbstractNativeList() {
		list = new ArrayList<E>();
		size = 0;
	}

	public int size() {
		return size;
	}

	/**
	 * Checks whether the list is empty
	 * 
	 * @return {@true if it's empty}
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	public E remove(int index) {
		E element = get(index);
		remove(index, index + 1);
		return element;
	}
}
