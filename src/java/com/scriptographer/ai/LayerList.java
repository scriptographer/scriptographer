/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 11.01.2005.
 *
 * $Id$
 */

package com.scriptographer.ai;

import com.scriptographer.util.StringIndexList;
import com.scriptographer.util.AbstractReadOnlyList;

/**
 * The LayerList object represents a list of layers in an Illustrator document.
 * LayerLists are not created through a constructor, they're always accessed
 * through the {@link Document#layers} property.
 * 
 * @author lehni
 */
public class LayerList extends AbstractReadOnlyList implements StringIndexList {
	Document document;

	protected LayerList(Document document) {
		this.document = document;
	}
	
	private static native int nativeGetLength(int docHandle);

	public int getLength() {
		return nativeGetLength(document.handle);
	}

	private static native Object nativeGet(int docHandle, int index);

	public Object get(int index) {
		return nativeGet(document.handle, index);
	}

	private static native Object nativeGet(int docHandle, String name);

	public Object get(String name) {
		return nativeGet(document.handle, name);
	}

	/**
	 * Retrieves a layer 
	 * @param index the index of the layer
	 * @return
	 */
	public Layer getLayer(int index) {
		return (Layer) get(index);
	}

	/**
	 * Retrieves a layer 
	 * @param name the name of the layer
	 * @return
	 */
	public Layer getLayer(String name) {
		return (Layer) get(name);
	}
}
