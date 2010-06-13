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
 * File created on Oct 18, 2006.
 * 
 * $Id$
 */

package com.scriptographer.ai;

/**
 * A Swatch represents a named color, tint, gradient or pattern contained within
 * an Illustrator document.
 * 
 * They can be retrieved from the document through {@link Document#getSwatches}.
 * 
 * @author lehni
 */
public class Swatch extends DocumentObject {
	
	/*
	 * Needed by wrapHandle mechanism
	 */
	protected Swatch(int handle, Document document) {
		super(handle, document);
	}

	private static native int nativeCreate();
	
	/**
	 * Creates a new Swatch object.
	 * 
	 * Sample code:
	 * <code>
	 * // create the swatch
	 * var swatch = new Swatch();
	 * swatch.color = new RGBColor(1, 0, 0);
	 * swatch.name = 'Red';
	 * 
	 * // add it to the document's swatch list
	 * document.swatches.push(swatch);
	 * </code>
	 */
	public Swatch() {
		super(nativeCreate());
	}
	
	protected static Swatch wrapHandle(int handle, Document document) {
		return (Swatch) wrapHandle(Swatch.class, handle, document);
	}
	
	/**
	 * The name of the swatch.
	 */
	public native String getName();
	
	public native void setName(String name);

	/**
	 * The color of the swatch.
	 */
	public native Color getColor();
	
	public native void setColor(Color color);
	
	protected native boolean nativeRemove();
	
	/**
	 * Removes the swatch from the document's swatch list.
	 */
	public boolean remove() {
		// make it public
		return super.remove();
	}
}
