/*
 * Scriptographer
 * AbstractFetchList.java
 * 
 * Created by  Lehni on 16.02.2005.
 * Copyright (c) 2004 http://www.scratchdisk.com. All rights reserved.
 * 
 */
package com.scriptographer.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * AbstractFetchList defines fetch and fetch(fromIndex, toIndex), which are called
 * everytime the a range of elements needs to be available immediatelly.
 * Subclassed by SegmentList and CurveList
 */
public abstract class AbstractFetchList extends AbstractList {
	protected abstract void fetch(int fromIndex, int toIndex);
	protected abstract void fetch();

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

	public Object[] toArray(Object[] array, int fromIndex, int toIndex) {
		fetch(fromIndex, toIndex);
		return super.toArray(array, fromIndex, toIndex);
	}

	public boolean retainAll(Collection elements) {
		fetch();
		return super.retainAll(elements);
	}

	public boolean removeAll(Collection elements) {
		fetch();
		return super.removeAll(elements);
	}

	public boolean containsAll(Collection elements) {
		fetch();
		return super.containsAll(elements);
	}

	public Iterator iterator() {
		fetch();
		return super.iterator();
	}
}
