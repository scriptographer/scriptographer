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
 * File created on 24.03.2005.
 */

package com.scriptographer.ui;

/**
 * @author lehni
 * 
 * @jshide
 */
public abstract class NativeObject {
	// used for storing the native handle for this object
	protected int handle;

	protected NativeObject() {
		handle = 0;
	}

	protected NativeObject(int handle) {
		this.handle = handle;
	}

	public int hashCode() {
		// Return the native handle here as hashCode, as we use equals() in
		// quite a few places to see if wrapper objects are actually
		// representing the same native object. For example this is used when
		// reusing live effects and menu groups / items after the plug-in was
		// reloaded.
		return handle != 0 ? handle : super.hashCode();
	}

	public boolean isValid() {
		return handle != 0;
	}

	public boolean equals(Object obj) {
		if (obj instanceof NativeObject) {
			return handle == ((NativeObject) obj).handle;
		}
		return false;
	}

	/**
	 * @jshide
	 */
	public Object getId() {
		return "@" + Integer.toHexString(hashCode());
	}

	public String toString() {
		return getClass().getSimpleName() + " "
				+ (isValid() ? getId() : "<invalid>");
	}
}
