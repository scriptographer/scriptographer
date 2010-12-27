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
 * File created on 11.01.2005.
 */

package com.scriptographer.ai;

import com.scratchdisk.list.AbstractReadOnlyList;
import com.scratchdisk.list.ReadOnlyStringIndexList;

/**
 * The LayerList object represents a list of layers in an Illustrator document.
 * LayerLists are not created through a constructor, they're always accessed
 * through the {@link Document#layers} property.
 * 
 * @author lehni
 * 
 * @jshide
 */
public class LayerList extends AbstractReadOnlyList<Layer> implements ReadOnlyStringIndexList<Layer> {
	Document document;

	protected LayerList(Document document) {
		this.document = document;
	}
	
	private static native int nativeSize(int docHandle);

	public int size() {
		return nativeSize(document.handle);
	}

	private static native Layer nativeGet(int docHandle, int index);

	/**
	 * Retrieves a layer 
	 * @param index the index of the layer
	 */
	public Layer get(int index) {
		return nativeGet(document.handle, index);
	}

	private static native Layer nativeGet(int docHandle, String name);

	/**
	 * Retrieves a layer 
	 * @param name the name of the layer
	 */
	public Layer get(String name) {
		return nativeGet(document.handle, name);
	}

	public Class<?> getComponentType() {
		return Layer.class;
	}
}
