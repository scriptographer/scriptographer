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
public abstract class AbstractList<E> extends AbstractReadOnlyList<E> implements List<E> {

	public E add(E element) {
		return add(size(), element);
	}

	public void remove(int fromIndex, int toIndex) {
		for (int i = toIndex - 1; i >= fromIndex; i--) {
			remove(i);
		}
	}

	public void removeAll() {
		remove(0, size());
	}

	/*
	 * Implement addAll with index too, although this is only
	 * required by ExtendedList. It is easier to implement all
	 * in one go like this though.
	 */
	public boolean addAll(int index, ReadOnlyList<? extends E> elements) {
		boolean modified = false;
		for (int i = 0, size = elements.size(); i < size; i++) {
			if (add(index, elements.get(i)) != null) {
				modified = true;
				index++;
			}
		}
		return modified;
	}

	/**
	 * @jshide
	 */
	public boolean addAll(ReadOnlyList<? extends E> elements) {
		return addAll(size(), elements);
	}

	public final boolean addAll(int index, E[] elements) {
		return addAll(index, Lists.asList(elements));
	}

	public final boolean addAll(E[] elements) {
		return addAll(size(), elements);
	}
}
