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

/**
 * Adds getting objects by name to Lists (an extension needed for some list
 * objects like LayerList)
 *
 * @author lehni 
 */
public interface ReadOnlyStringIndexList<E> {
	/**
	 * @jshide
	 */
	public E get(String name);
}
