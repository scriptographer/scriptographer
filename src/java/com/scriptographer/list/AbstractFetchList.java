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

import com.scratchdisk.list.ExtendedList;
import com.scratchdisk.list.ReadOnlyList;

/**
 * AbstractFetchList defines fetch and fetch(fromIndex, toIndex), which are called
 * every time the a range of elements needs to be available immediately.
 * Subclassed by SegmentList and CurveList
 * 
 * @author lehni
 */
public abstract class AbstractFetchList<E> extends AbstractNativeList<E> {

	protected abstract void fetch(int fromIndex, int toIndex);

	protected void fetch() {
		if (size > 0)
			fetch(0, size);
	}

	public boolean contains(Object element) {
		fetch();
		return super.contains(element);
	}

	public int indexOf(Object element) {
		fetch();
		return super.indexOf(element);
	}

	public int lastIndexOf(Object element) {
		fetch();
		return super.lastIndexOf(element);
	}

	public E[] toArray(E[] array) {
		fetch();
		return super.toArray(array);
	}

	public boolean retainAll(ExtendedList<?> elements) {
		fetch();
		return super.retainAll(elements);
	}

	public boolean removeAll(ExtendedList<?> elements) {
		fetch();
		return super.removeAll(elements);
	}

	public boolean containsAll(ReadOnlyList<?> elements) {
		fetch();
		return super.containsAll(elements);
	}
	
	public ExtendedList<E> getSubList(int fromIndex, int toIndex) {
		fetch();
		return super.getSubList(fromIndex, toIndex);
	}

	public String toString() {
		fetch();
		return super.toString();
	}
}
