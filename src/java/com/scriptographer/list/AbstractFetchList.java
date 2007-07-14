/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id$
 */

package com.scriptographer.list;

import com.scratchdisk.list.AbstractExtendedList;
import com.scratchdisk.list.ExtendedList;

/**
 * AbstractFetchList defines fetch and fetch(fromIndex, toIndex), which are called
 * everytime the a range of elements needs to be available immediatelly.
 * Subclassed by SegmentList and CurveList
 * 
 * @author lehni
 */
public abstract class AbstractFetchList extends AbstractExtendedList {
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

	public Object[] toArray(Object[] array) {
		fetch();
		return super.toArray(array);
	}

	public boolean retainAll(ExtendedList elements) {
		fetch();
		return super.retainAll(elements);
	}

	public boolean removeAll(ExtendedList elements) {
		fetch();
		return super.removeAll(elements);
	}

	public boolean containsAll(ExtendedList elements) {
		fetch();
		return super.containsAll(elements);
	}
	
	public ExtendedList getSubList(int fromIndex, int toIndex) {
		fetch();
		return super.getSubList(fromIndex, toIndex);
	}

	public String toString() {
		fetch();
		return super.toString();
	}
}
