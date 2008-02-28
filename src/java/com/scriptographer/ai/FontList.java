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
 * File created on 04.11.2005.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import java.util.WeakHashMap;

import com.scratchdisk.list.ExtendedList;
import com.scratchdisk.list.Lists;
import com.scratchdisk.list.ReadOnlyList;
import com.scratchdisk.list.StringIndexReadOnlyList;

/**
 * The FontList object represents a list of {@link FontFamily} objects. Font
 * lists are not created through a constructor, they're always accessed through
 * the Application.fonts property.
 * 
 * @author lehni
 */
public class FontList implements ReadOnlyList, StringIndexReadOnlyList {

	/**
     * Don't let anyone instantiate this class.
     */
	private FontList() {
	}

	/**
	 * The amount of font families in the font list.
	 */
	public native int size();
	
	private static native int nativeGet(int index);

	/**
	 * Gets a font family based on it's index in the font list.
	 */
	public Object get(int index) {
		return FontFamily.wrapHandle(nativeGet(index));
	}

	public WeakHashMap fontsByName = new WeakHashMap();	
	
	/**
	 * Gets a font family based on it's name in the font list.
	 */
	public Object get(String name) {
		// fontsByName is a steadily growing lookup table
		FontFamily family = null;
		if (name != null) {
			name = name.toLowerCase();
			family = (FontFamily) fontsByName.get(name);
			if (family != null && !family.isValid()) {
				fontsByName.remove(name);
				family = null;
			}
			// not found, scan:
			if (family == null) {
				int len = size();
				for (int i = 0; i < len; i++) {
					FontFamily f = (FontFamily) get(i);
					String n = f.getName().toLowerCase();
					fontsByName.put(n, f);
					if (f != null && n.equals(name)) {
						family = f;
						break;
					}
				}
			}
		}
		return family;
	}

	/**
	 * Boolean value that specifies wether the font list is empty.
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	public ExtendedList getSubList(int fromIndex, int toIndex) {
		return Lists.createSubList(this, fromIndex, toIndex);
	}

	private static FontList fonts;

	public static FontList getInstance() {
		if (fonts == null)
			fonts = new FontList();

		return fonts;
	}
}
