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
public interface ExtendedList<E> extends List<E> {

	/**
	 * @jshide
	 */
	int indexOf(Object element);

	/**
	 * @jshide
	 */
	int lastIndexOf(Object element);

	/**
	 * @jshide
	 */
	boolean contains(Object o);

	/**
	 * @jshide
	 */
	boolean addAll(int index, ReadOnlyList<? extends E> elements);

	/**
	 * @jshide
	 */
	boolean addAll(int index, E[] elements);

	/**
	 * @jshide
	 */
	boolean containsAll(ReadOnlyList<?> elementsc);

	/**
	 * @jshide
	 */
	boolean containsAll(Object[] elements);

	/**
	 * @jshide
	 */
	boolean removeAll(ExtendedList<?> elements);

	/**
	 * @jshide
	 */
	boolean removeAll(Object[] elements);

	/**
	 * @jshide
	 */
	boolean retainAll(ExtendedList<?> elements);

	/**
	 * @jshide
	 */
	boolean retainAll(Object[] elements);

	/**
	 * @jshide
	 */
	void setSize(int size);

	/**
	 * @jshide
	 */
	E[] toArray();

	/**
	 * @jshide
	 */
	E[] toArray(E elements[]);

	/**
	 * @jshide
	 */
	E removeFirst();

	/**
	 * @jshide
	 */
	E removeLast();

	/**
	 * @jshide
	 */

	E remove(E element);
}
