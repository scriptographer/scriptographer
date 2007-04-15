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
 * File created on Oct 18, 2006.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.util.AbstractReadOnlyList;
import com.scratchdisk.util.StringIndexList;

/**
 * The SwatchList object represents a list of swatches in an Illustrator
 * document as shown in the Swatches palette. SwatchLists are not created through
 * a constructor, they're always accessed through the {@link Document#swatches}
 * property.
 * 
 * @author lehni
 */
public class SwatchList extends AbstractReadOnlyList implements StringIndexList {
	Document document;

	protected SwatchList(Document document) {
		this.document = document;
	}
	
	private static native int nativeSize(int docHandle);

	public int size() {
		return nativeSize(document.handle);
	}

	private static native int nativeGet(int docHandle, int index);

	public Object get(int index) {
		return Swatch.wrapHandle(nativeGet(document.handle, index), document);
	}

	private static native int nativeGet(int docHandle, String name);

	public Object get(String name) {
		return Swatch.wrapHandle(nativeGet(document.handle, name), document);
	}

	/**
	 * Retrieves a swatch 
	 * @param index the index of the swatch
	 * @return
	 */
	public Swatch getSwatch(int index) {
		return (Swatch) get(index);
	}

	/**
	 * Retrieves a swatch 
	 * @param name the name of the swatch
	 * @return
	 */
	public Swatch getSwatch(String name) {
		return (Swatch) get(name);
	}
}
