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

package com.scratchdisk.list;


/**
 * @author lehni
 */
public abstract class AbstractExtendedList<E> extends AbstractList<E>
	implements ExtendedList<E> {

	public E getFirst() {
		return size() > 0 ? get(0) : null;
	}
	
	public E getLast() {
		int size = size();
		return size > 0 ? get(size - 1) : null;
	}

	public E removeFirst() {
		return this.remove(0);		
	}
	
	public E removeLast() {
		return this.remove(size() - 1);
	}

	public int indexOf(Object element) {
		int size = size();
		for (int i = 0; i < size; i++) {
			E obj = get(i);
			if (obj == null && element == null || obj.equals(element))
				return i;
		}
		return -1;
	}

	public int lastIndexOf(Object element) {
		for (int i = size() - 1; i >= 0; i--) {
			E obj = get(i);
			if (obj == null && element == null || obj.equals(element))
				return i;
		}
		return -1;
	}

	public boolean contains(Object element) {
		return indexOf(element) != -1;
	}

	public E remove(E element) {
		int index = indexOf(element);
		if (index >= 0) {
			return remove(index);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public E[] toArray(E[] array) {
		if (array == null)
			array = (E[]) new Object[size()];
		for (int i = 0; i < array.length; i++)
			array[i] = get(i);
		return array;
	}

	public final E[] toArray() {
		return toArray(null);
	}

	/**
	 * @jshide
	 */
	public boolean retainAll(ExtendedList<?> elements) {
		boolean modified = false;
		for (int i = size() - 1; i >= 0; i--) {
			E obj = get(i);
			if(!elements.contains(obj) && remove(i) != null)
				modified = true;
		}
		return modified;
	}

	public final boolean retainAll(Object[] elements) {
		return retainAll(Lists.asList(elements));
	}

	/**
	 * @jshide
	 */
	public boolean removeAll(ExtendedList<?> elements) {
		boolean modified = false;
		for (int i = size() - 1; i >= 0; i--) {
			E obj = get(i);
			if(elements.contains(obj) && remove(i) != null)
				modified = true;
		}
		return modified;
	}

	public final boolean removeAll(Object[] elements) {
		return removeAll(Lists.asList(elements));
	}

	/**
	 * @jshide
	 */
	public boolean containsAll(ReadOnlyList<?> elements) {
		for (int i = elements.size() - 1; i >= 0; i--) {
			if (!contains(elements.get(i)))
				return false;
		}
		return true;
	}

	public final boolean containsAll(Object[] elements) {
		return containsAll(Lists.asList(elements));
	}

	public void setSize(int newSize) {
		int size = size();
		if (newSize > size) {
			// fill with null:
			for (int i = size; i < newSize; i++)
				add(i, null);
		} else if (newSize < size) {
			remove(newSize, size);
		}
	}
}
