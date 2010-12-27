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
 * File created on 04.11.2005.
 */

package com.scriptographer.ai;

/**
 * @author lehni
 */
public class FontWeight extends NativeObject {

	/**
	 * @jshide
	 */
	public static final FontWeight NONE = new FontWeight(0);

	protected FontWeight(int handle) {
		super(handle);
	}

	private native String nativeGetName(int handle);

	/**
	 * The name of the font weight.
	 */
	public String getName() {
		return handle == 0 ? "None" : nativeGetName(handle);
	}

	private native int nativeGetFamily(int handle);

	/**
	 * The font family array that the font weight belongs to.
	 */
	public FontFamily getFamily() {
		return FontFamily.wrapHandle(nativeGetFamily(handle));
	}

	private native int nativeGetIndex(int handle);

	/**
	 * The index of the font weight in it's font family array.
	 */
	public int getIndex() {
		return handle == 0 ? -1 : nativeGetIndex(handle);
	}

	protected static FontWeight wrapHandle(int handle) {
		return (FontWeight) (handle == 0 ? null : wrapHandle(FontWeight.class, handle));
	}

	private native boolean nativeIsValid(int handle);

	public boolean isValid() {
		return handle == 0 ? false : nativeIsValid(handle);
	}

	public String toString() {
		return getFamily() + " " + getName();
	}
}
