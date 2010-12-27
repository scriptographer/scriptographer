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
 * File created on 12.02.2005.
 */

package com.scratchdisk.list;

import java.util.Map;

/**
 * Adds setting objects by name to Lists.
 *
 * @author lehni 
 */
public interface StringIndexList<E> extends ReadOnlyStringIndexList<E> {
	/**
	 * @jshide
	 */
	public E put(String name, E element);

	/**
	 * @jshide
	 */
	public E remove(String name);

	/**
	 * @jshide
	 */
	public void addAll(Map<String,? extends E> elements);
}
