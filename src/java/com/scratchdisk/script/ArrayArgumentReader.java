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
 * File created on Mar 23, 2008.
 */

package com.scratchdisk.script;

/**
 * @author lehni
 *
 */
public abstract class ArrayArgumentReader extends ArgumentReader {

	protected int index;

	public ArrayArgumentReader(Converter converter) {
		super(converter);
		index = 0;
	}

	public void revert() {
		if (index > 0)
			index--;
	}

	public boolean isArray() {
		return true;
	}
}
