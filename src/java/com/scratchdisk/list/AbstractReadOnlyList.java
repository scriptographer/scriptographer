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

import java.util.Iterator;


/**
 * @author lehni
 */
public abstract class AbstractReadOnlyList<E> implements ReadOnlyList<E> {

	public boolean isEmpty() {
		return size() == 0;
	}

	public ExtendedList<E> getSubList(int fromIndex, int toIndex) {
		return Lists.createSubList(this, fromIndex, toIndex);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(256);
		buf.append("[ ");
		int size = size();
		for (int i = 0; i < size; i++) {
			Object obj = get(i);
			if (i > 0) buf.append(", ");
			buf.append(obj.toString());
		}
		buf.append(" ]");
		return buf.toString();
	}

	public Iterator<E> iterator() {
		return new ListIterator<E>(this);
	}

	public E getFirst() {
		return size() > 0 ? get(0) : null;
	}
	
	public E getLast() {
		int size = size();
		return size > 0 ? get(size - 1) : null;
	}
}
