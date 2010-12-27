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
 * File created on 21.10.2005.
 */

package com.scratchdisk.list;


/**
 * This is similar to java.util.List, but has a few changes that make it much
 * easier to use in Scirptographer. SegmentList, CurveList, LayerList and ItemSet
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
public interface List<E> extends ReadOnlyList<E> {
	/**
	 * @jshide
	 */
	E set(int index, E element);

	/**
	 * @jshide
	 */
	E add(E element);

	/**
	 * @jshide
	 */
	E add(int index, E element);

	/**
	 * @jshide
	 */
	boolean addAll(ReadOnlyList<? extends E> list);

	/**
	 * @jshide
	 */
	boolean addAll(E[] elements);

	/**
	 * @jshide
	 */
	E remove(int index);

	/**
	 * @jshide
	 */
	void remove(int fromIndex, int toIndex);

	/**
	 * @jshide
	 */
	void removeAll();
}
