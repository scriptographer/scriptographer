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
 * File created on Feb 11, 2008.
 */

package com.scratchdisk.script.rhino;

import org.mozilla.javascript.NativeArray;

import com.scratchdisk.script.Converter;

/**
 * @author lehni
 *
 */
public class ArrayArgumentReader extends com.scratchdisk.script.ArrayArgumentReader {

	protected NativeArray array;

	public ArrayArgumentReader(Converter converter, NativeArray array) {
		super(converter);
		this.array = array;
	}

	protected Object readNext(String name) {
		return index < array.getLength() ? array.get(index++, array) : null;
	}

	public int size() {
		return (int) array.getLength();
	}
}
