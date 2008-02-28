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
 * File created on 21.10.2005.
 * 
 * $Id$
 */

package com.scratchdisk.list;


/**
 * This is similar to java.util.List, but has a few changes that make it much
 * easier to use in Scirptographer. SegmentList, CurveList, LayerList and ArtSet
 * subclass the AbstractList that implements this interface.
 * 
 * Reason to implement this:
 * - add(element) returns the final element. this might be different from the
 *   parameter that was passed. usefull for segment lists, where add(point)
 *   create a segment and returns it or lists, where add(String) creates a list
 *   entry and returns it.
 * - add(int, element) returns a element as well, which makes it easier to
 *   implement both adds.
 * - defines less functions and makes it therefore easier to implement
 *   (no ListIterator, no SubLists)
 * - defines other often needed functions, e.g. remove(fromIndex, toIndex)
 * - gives complete control over behavior of lists
 * - allows read only lists
 * - avoids wrapping of standard java.util.lists in the JS ListObject
 * - ...
 * 
 * @author lehni
 */
public interface List extends ReadOnlyList {
	Object set(int index, Object element);
	Object add(Object element);
	Object add(int index, Object element);
	boolean addAll(List list);
	boolean addAll(Object[] elements);
	Object remove(int index);
	void remove(int fromIndex, int toIndex);
	void removeAll();
}
