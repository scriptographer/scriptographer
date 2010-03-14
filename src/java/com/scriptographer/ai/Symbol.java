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

	private static native int nativeCreate(int artHandle);

	/**
	 * Creates a Symbol item.
	 * 
	 * @param item the source item which is copied as the definition of the
	 *        symbol
	 */
	public Symbol(Item item) {
		super(nativeCreate(item != null ? item.handle : 0));
	}

	public Symbol() {
		this(null);
	}
	
	protected static Symbol wrapHandle(int handle, Document document) {
		return (Symbol) wrapHandle(Symbol.class, handle, document);
	}
	
	/**
	 * The name of the symbol which may not exceed 64 characters.
	 */
	public native String getName();
	
	public native void setName(String name);

	/**
	 * The symbol definition. The definition itself cannot be edited, just
	 * examined, replaced or cloned.
	 */
	public native Item getDefinition();

	public native void setDefinition(Item item);

	/**
 	 * Checks whether the symbol is valid, i.e. it hasn't been removed.
 	 * 
	 * @return {@true if the symbol is valid}
	 */
	public native boolean isValid();

	/**
	 * Specifies whether the item appears in the symbols palette. Listed symbols
	 * are saved when a document is closed, even if there are no instances of
	 * them within the document.
	 * 
	 * @return {@true if the item appears in the symbols palette}
	 */
	public native boolean isListed();

	public native void setListed(boolean listed);
	
	/**
	 * Checks whether the symbol is selected in the symbols palette.
	 * 
	 * @return {@true if the symbol is selected}
	 */
	public native boolean isSelected();
	
	/**
	 * Activates the symbol in the symbols palette. The current active symbol
	 * can be retrieved from {@link Document#getActiveSymbol()}.
	 */
	public native void activate();
	
	/**
	 * Set the index of the listed symbol in the symbols palette.
	 * 
	 * @param index -1 to move to end of list
	 */
	public native void setIndex(int index);
	
	protected native boolean nativeRemove();
	
	/**
	 * Removes the symbol from the symbols palette.
	 */
	public boolean remove() {
		// make it public
		return super.remove();
	}
}
