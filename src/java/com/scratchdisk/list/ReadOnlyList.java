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
 * @author lehni 
 */
public interface ReadOnlyList<E> extends Iterable<E> {
	/**
	 * The amount of elements contained in the array.
	 * 
	 * @jshide
	 */
	int size();

	/**
	 * @jshide
	 */
	boolean isEmpty();

	/**
	 * @jshide
	 */
	E get(int index);

	/**
	 * @jshide
	 */
	ExtendedList<E> getSubList(int fromIndex, int toIndex);

	/**
	 * @jshide
	 */
	E getFirst();

	/**
	 * @jshide
	 */
	E getLast();

	/**
	 * @jshide
	 */
	Class<?> getComponentType();
}
