/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.com/ for updates and contact.
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
 *
 * $Id:AbstractFetchList.java 363 2007-07-14 15:54:06Z lehni $
 */

package com.scriptographer.list;

import com.scratchdisk.list.AbstractExtendedList;
import com.scratchdisk.list.ArrayList;

/**
 * AbstractFetchList defines fetch and fetch(fromIndex, toIndex), which are called
 * every time the a range of elements needs to be available immediately.
 * Subclassed by SegmentList and CurveList
 * 
 * @author lehni
 */
public abstract class AbstractNativeList<E> extends AbstractExtendedList<E> {
	protected int size;
	protected ArrayList.List<E> list;

	protected AbstractNativeList() {
		list = new ArrayList.List<E>();
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
