/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
 *
 * -- GPL LICENSE NOTICE --
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -- GPL LICENSE NOTICE --
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
