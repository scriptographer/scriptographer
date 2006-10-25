/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.com/ for updates and contact.
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
 * File created on Oct 17, 2006.
 * 
 * $RCSfile: Pattern.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2006/10/25 02:12:51 $
 */

package com.scriptographer.ai;

public class Pattern extends AIWrapper {
	
	protected Pattern(int handle) {
		super(handle);
	}
	
	private static native int nativeCreate();
	
	public Pattern() {
		super(nativeCreate());
	}
	
	public Pattern(Art item) {
		this();
		setDefinition(item);
	}

	protected static Pattern wrapHandle(int handle, Document document) {
		return (Pattern) wrapHandle(Pattern.class, handle, document, true);
	}
	
	public native String getName();
	
	public native void setName(String name);

	/**
	 * Returns the pattern definition.
	 * @return
	 */
	public native Art getDefinition();

	/**
	 * Copy the item as the new symbol definition
	 * @param item
	 */
	public native void setDefinition(Art item);

	public native boolean isValid();
}
