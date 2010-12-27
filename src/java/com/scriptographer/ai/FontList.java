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
 * File created on 04.11.2005.
 */

package com.scriptographer.ai;

import java.util.Iterator;
import java.util.WeakHashMap;

import com.scratchdisk.list.ExtendedList;
import com.scratchdisk.list.ListIterator;
import com.scratchdisk.list.Lists;
import com.scratchdisk.list.ReadOnlyList;
import com.scratchdisk.list.ReadOnlyStringIndexList;

/**
 * The FontList object represents a list of {@link FontFamily} objects. Font
 * lists are not created through a constructor, they're always accessed through
 * the Application.fonts property.
 * 
 * @author lehni
 * 
 * @jshide
 */
public class FontList implements ReadOnlyList<FontFamily>, ReadOnlyStringIndexList<FontFamily> {

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
	public FontFamily get(int index) {
		return FontFamily.wrapHandle(nativeGet(index));
	}

	public WeakHashMap<String, FontFamily> fontsByName =
		new WeakHashMap<String, FontFamily>();	
	
	/**
	 * Gets a font family based on it's name in the font list.
	 */
	public FontFamily get(String name) {
		// fontsByName is a steadily growing lookup table
		FontFamily family = null;
		if (name != null) {
			name = name.toLowerCase();
			family = fontsByName.get(name);
			if (family != null && !family.isValid()) {
				fontsByName.remove(name);
				family = null;
			}
			// not found, scan:
			if (family == null) {
				int len = size();
				for (int i = 0; i < len; i++) {
					FontFamily f = get(i);
					if (f != null) {
						String n = f.getName().toLowerCase();
						fontsByName.put(n, f);
						if (n.equals(name)) {
							family = f;
							break;
						}
					}
				}
			}
		}
		return family;
	}

	public FontWeight getWeight(String fullName) {
		// First find family:
		FontFamily family = get(fullName);
		if (family != null)
			return family.get(0);
		int pos = fullName.lastIndexOf(' ');
		// Now split name into familName weightName and try again:
		if (pos > 0) {
			family = get(fullName.substring(0, pos));
			if (family != null)
				return family.get(fullName.substring(pos + 1));
		}
		return null;
	}
	
	
	/**
	 * Boolean value that specifies whether the font list is empty.
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	public ExtendedList<FontFamily> getSubList(int fromIndex, int toIndex) {
		return Lists.createSubList(this, fromIndex, toIndex);
	}

	public Iterator<FontFamily> iterator() {
		return new ListIterator<FontFamily>(this);
	}

	private static FontList fonts;

	public static FontList getInstance() {
		if (fonts == null)
			fonts = new FontList();

		return fonts;
	}

	public FontFamily getFirst() {
		return size() > 0 ? get(0) : null;
	}

	public FontFamily getLast() {
		int size = size();
		return size > 0 ? get(size - 1) : null;
	}

	public Class<?> getComponentType() {
		return FontFamily.class;
	}
}
