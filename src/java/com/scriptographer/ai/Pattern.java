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
 * File created on Oct 17, 2006.
 */

package com.scriptographer.ai;

/**
 * @author lehni
 */
public class Pattern extends DocumentObject {

	protected Pattern(int handle, Document document) {
		super(handle, document);
	}
	
	private static native int nativeCreate();

	public Pattern() {
		super(nativeCreate());
	}
	
	public Pattern(Item item) {
		this();
		setDefinition(item);
	}

	protected static Pattern wrapHandle(int handle, Document document) {
		return (Pattern) wrapHandle(Pattern.class, handle, document);
	}

	/**
	 * Returns the pattern definition.
	 */
	public native Item getDefinition();

	/**
	 * Copy the item as the new symbol definition
	 * @param item
	 */
	public native void setDefinition(Item item);

	public native boolean isValid();
}
