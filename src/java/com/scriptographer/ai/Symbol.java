/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id$
 */

package com.scriptographer.ai;

/**
 * @author lehni
 */
public class Symbol extends DocumentObject {

	/*
	 * Needed by wrapHandle mechanism
	 */
	protected Symbol(int handle, Document document) {
		super(handle, document);
	}

	private static native int nativeCreate(int artHandle, boolean listed);
	
	public Symbol(Item item, boolean listed) {
		super(nativeCreate(item != null ? item.handle : 0, listed));
	}
	
	public Symbol(Item item) {
		this(item, true);
	}
	
	public Symbol() {
		this(null, true);
	}
	
	protected static Symbol wrapHandle(int handle, Document document) {
		return (Symbol) wrapHandle(Symbol.class, handle, document);
	}
	
	public native String getName();
	
	public native void setName(String name);

	/**
	 * Returns the symbol definition. The item returned cannot be edited, just
	 * examined or duplicated
	 * @return
	 */
	public native Item getDefinition();

	/**
	 * Copy the item as the new symbol definition. Any existing
	 * instances of the symbol will be updated
	 * @param item
	 */
	public native void setDefinition(Item item);

	public native boolean isValid();

	public native boolean isListed();
	
	public native void setListed(boolean listed);
	
	public native boolean isSelected();
	
	public native void activate();
	
	/**
	 * Set the index of the listed symbol
	 * @param index -1 to move to end of list.
	 */
	public native void setIndex(int index);
	
	protected native boolean nativeRemove();
	
	public boolean remove() {
		// make it public
		return super.remove();
	}
}
