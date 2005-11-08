/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2004-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile: FontList.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/11/08 21:38:21 $
 */

package com.scriptographer.ai;

import java.util.WeakHashMap;

import com.scriptographer.util.ExtendedList;
import com.scriptographer.util.Lists;
import com.scriptographer.util.ReadOnlyList;
import com.scriptographer.util.StringIndexList;

public class FontList implements ReadOnlyList, StringIndexList {

	/**
     * Don't let anyone instantiate this class.
     */
	private FontList() {
	}

	public native int getLength();
	
	private static native int nativeGet(int index);

	public Object get(int index) {
		return FontFamily.wrapHandle(nativeGet(index));
	}

	public WeakHashMap fontsByName = new WeakHashMap();	
	
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
				int len = getLength();
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

	public boolean isEmpty() {
		return getLength() == 0;
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
