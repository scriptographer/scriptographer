/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
 *
 * -- GPL LICENSE NOTICE --
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -- GPL LICENSE NOTICE --
 *
 * File created on 24.03.2005.
 *
 * $Id$
 */

package com.scriptographer.ui;

/**
 * @author lehni
 */
abstract class NativeObject {
	// used for storing the native handle for this object
	protected int handle;

	protected NativeObject() {
		handle = 0;
	}

	protected NativeObject(int handle) {
		this.handle = handle;
	}

	public int hashCode() {
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
	public String getId() {
		return "@" + Integer.toHexString(hashCode());
	}

	public String toString() {
		return getClass().getSimpleName() + " "
				+ (isValid() ? getId() : "<invalid>");
	}
}
