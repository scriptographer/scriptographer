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
 * File created on Oct 18, 2006.
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
