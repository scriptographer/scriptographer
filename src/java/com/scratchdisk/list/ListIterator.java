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
 * File created on Apr 21, 2008.
 */

package com.scratchdisk.list;

import java.util.Iterator;

/**
 * @author lehni
 *
 */
public class ListIterator<E> implements Iterator<E> {
	private ReadOnlyList<E> list;
	private int index;

	public ListIterator(ReadOnlyList<E> list) {
		this.list = list;
		this.index = 0;
	}

	public boolean hasNext() {
		return index < list.size();
	}

	public E next() {
		return list.get(index++);
	}

	public void remove() {
		if (list instanceof List) {
			((List) list).remove(index++);
		} else {
			throw new UnsupportedOperationException("Cannot remove on ReadOnlyLists");
		}
	}
}
