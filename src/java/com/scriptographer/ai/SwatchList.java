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

import com.scratchdisk.list.AbstractReadOnlyList;
import com.scratchdisk.list.ReadOnlyStringIndexList;

/**
 * The SwatchList object represents a list of swatches in an Illustrator
 * document as shown in the Swatches palette. SwatchLists are not created through
 * a constructor, they're always accessed through the {@link Document#swatches}
 * property.
 * 
 * @author lehni
 * 
 * @jshide
 */
public class SwatchList extends AbstractReadOnlyList<Swatch> implements ReadOnlyStringIndexList<Swatch> {
	Document document;

	protected SwatchList(Document document) {
		this.document = document;
	}
	
	private static native int nativeSize(int docHandle);

	public int size() {
		return nativeSize(document.handle);
	}

	private static native int nativeGet(int docHandle, int index);

	/**
	 * Retrieves a swatch 
	 * @param index the index of the swatch
	 */
	public Swatch get(int index) {
		return Swatch.wrapHandle(nativeGet(document.handle, index), document);
	}

	private static native int nativeGet(int docHandle, String name);

	/**
	 * Retrieves a swatch 
	 * @param name the name of the swatch
	 */
	public Swatch get(String name) {
		return Swatch.wrapHandle(nativeGet(document.handle, name), document);
	}

	public Class<?> getComponentType() {
		return Swatch.class;
	}
}
