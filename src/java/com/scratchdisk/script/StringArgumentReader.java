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
 * File created on Feb 12, 2008.
 */

package com.scratchdisk.script;

/**
 * A StringArgumentReader can read from passed strings. It extends
 * ArrayArgumentReader, therefore also return true for isArray.
 * So make sure you check for isString first if that special case
 * needs handling.
 * 
 * @author lehni
 *
 */
public class StringArgumentReader extends ArrayArgumentReader {

	private String[] parts;

	public StringArgumentReader(Converter converter, String string) {
		super(converter);
		parts = string.split("\\s");
	}

	protected Object readNext(String name) {
		return index < parts.length ? parts[index++] : null;
	}

	public boolean isString() {
		return true;
	}
}
