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

import java.util.Collection;
import java.util.Arrays;

import com.scratchdisk.util.ArrayList;

/**
 * Wraps an com.scratchdisk.util.ArrayList (which is an extended
 * java.util.ArrayList) in an ExtendedList interface.
 * 
 * @author lehni
 */
public class ExtendedArrayList<E> extends AbstractExtendedList<E> {
	ArrayList<E> list;

	public ExtendedArrayList() {
		list = new ArrayList<E>();
	}

	public ExtendedArrayList(int initialCapacity) {
		list = new ArrayList<E>(initialCapacity);
	}

	public ExtendedArrayList(E[] objects) {
		list = new ArrayList<E>(Arrays.asList(objects));
	}

	public ExtendedArrayList(ArrayList<E> list) {
		list = new ArrayList<E>(list);
	}

	public int size() {
		return list.size();
	}

	public E get(int index) {
		return list.get(index);
	}

	public E add(int index, E element) {
		list.add(index, element);
		return element;
	}

	public E set(int index, E element) {
		return list.set(index, element);
	}

	public E remove(int index) {
		return list.remove(index);
	}

	public void remove(int fromIndex, int toIndex) {
		list.remove(fromIndex, toIndex);
	}

	public E add(E element) {
		if (list.add(element))
			return element;
		return null;
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		return list.addAll(index, c);
	}

	public Class<?> getComponentType() {
		return Object.class;
	}
}
