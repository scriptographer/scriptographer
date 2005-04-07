/*
 * Scriptographer
 * AbstractList.java
 * 
 * Created by  Lehni on 16.02.2005.
 * Copyright (c) 2004 http://www.scratchdisk.com. All rights reserved.
 * 
 */
package com.scriptographer.util;

import com.scriptographer.js.WrappableObject;

import java.util.Iterator;
import java.util.Collection;
import java.util.Arrays;

public abstract class AbstractList extends WrappableObject implements List {
	public abstract int size();
	public abstract Object get(int index);
	public abstract boolean add(int index, Object element);
	public abstract Object set(int index, Object element);
	public abstract Object remove(int index);

	public int indexOf(Object element) {
		for (int i = 0; i < size(); i++) {
			Object obj = get(i);
			if (obj == null && element == null || obj.equals(element))
				return i;
		}
		return -1;
	}

	public int lastIndexOf(Object element) {
		for (int i = size() - 1; i >= 0; i--) {
			Object obj = get(i);
			if (obj == null && element == null || obj.equals(element))
				return i;
		}
		return -1;
	}

	public boolean contains(Object element) {
		return indexOf(element) != -1;
	}

	public boolean remove(Object element) {
		int index = indexOf(element);
		if (index >= 0) {
			return remove(index) != null;
		}
		return false;
	}

	public void remove(int fromIndex, int toIndex) {
		for (int i = toIndex - 1; i >= fromIndex; i--) {
			remove(i);
		}
	}

	public final void clear() {
		remove(0, size());
	}

	public boolean add(Object element) {
		return add(size(), element);
	}

	public boolean addAll(int index, Collection elements) {
		boolean modified = false;
		Iterator e = elements.iterator();
		while (e.hasNext()) {
			if (add(index++, e.next()))
				modified = true;
		}
		return modified;
	}

	public final boolean addAll(Collection elements) {
		return addAll(size(), elements);
	}

	public final boolean addAll(int index, Object[] elements) {
		return addAll(index, Arrays.asList(elements));
	}

	public final boolean addAll(Object[] elements) {
		return addAll(size(), elements);
	}

	public Object[] toArray(Object[] array, int fromIndex, int toIndex) {
		if (array == null)
			array = new Object[toIndex - fromIndex];
		for (int i = fromIndex; i < toIndex; i++) {
			array[i] = get(i);
		}
		return array;
	}

	public final Object[] toArray() {
		return toArray(null, 0, size());
	}

	public final Object[] toArray(Object[] array) {
		return toArray(array, 0, size());
	}

	public final Object[] toArray(int fromIndex, int toIndex) {
		return toArray(null, fromIndex, toIndex);
	}

	public List toList(int fromIndex, int toIndex) {
		return new ArrayList(toArray(fromIndex, toIndex));
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public boolean retainAll(Collection elements) {
		boolean modified = false;
		for (int i = size() - 1; i >= 0; i--) {
			Object obj = get(i);
			if(!elements.contains(obj) && remove(i) != null)
				modified = true;
		}
		return modified;
	}

	public boolean removeAll(Collection elements) {
		boolean modified = false;
		for (int i = size() - 1; i >= 0; i--) {
			Object obj = get(i);
			if(elements.contains(obj) && remove(i) != null)
				modified = true;
		}
		return modified;
	}

	public boolean containsAll(Collection elements) {
		Iterator e = elements.iterator();
		while (e.hasNext()) {
			if(!contains(e.next()))
				return false;
		}
		return true;
	}

	public Iterator iterator() {
		return new ListIterator();
	}

	private class ListIterator implements Iterator {
		int index = 0;
		int lastIndex = -1;

		public boolean hasNext() {
			return index != size();
		}

		public Object next() {
			Object next = get(index);
			lastIndex = index++;
			return next;
		}

		public void remove() {
			if (lastIndex == -1)
				throw new IllegalStateException();

			AbstractList.this.remove(lastIndex);
			if (lastIndex < index)
				index--;
			lastIndex = -1;
		}
	}
}
