/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 18.01.2005.
 * 
 * $RCSfile: ArtSet.java,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/03/25 00:27:57 $
 */

package com.scriptographer.ai;

import java.util.*;

import com.scriptographer.util.ArrayList;
import com.scriptographer.js.FunctionHelper;
import org.mozilla.javascript.NativeObject;

public class ArtSet extends ArrayList {
	// TODO: move getSelected, getMatching to document, getLayer to layer.getAll()!

	public final static Integer
			ATTR_SELECTED = new Integer(Art.ATTR_SELECTED),
			ATTR_LOCKED = new Integer(Art.ATTR_LOCKED),
			ATTR_HIDDEN = new Integer(Art.ATTR_HIDDEN),
			ATTR_FULLY_SELECTED = new Integer(Art.ATTR_FULLY_SELECTED),
			ATTR_EXPANDED = new Integer(Art.ATTR_EXPANDED),
			ATTR_TARGETED = new Integer(Art.ATTR_TARGETED),
			ATTR_IS_CLIPMASK = new Integer(Art.ATTR_IS_CLIPMASK),
			ATTR_IS_TEXTWRAP = new Integer(Art.ATTR_IS_TEXTWRAP),
			ATTR_SELECTED_TOPLEVEL_GROUPS = new Integer(Art.ATTR_SELECTED_TOPLEVEL_GROUPS),
			ATTR_SELECTED_LAYERS = new Integer(Art.ATTR_SELECTED_LAYERS),
			ATTR_SELECTED_TOPLEVEL_WITH_PAINT = new Integer(Art.ATTR_SELECTED_TOPLEVEL_WITH_PAINT),
			ATTR_HAS_SIMPLE_STYLE = new Integer(Art.ATTR_HAS_SIMPLE_STYLE),
			ATTR_HAS_ACTIVE_STYLE = new Integer(Art.ATTR_HAS_ACTIVE_STYLE),
			ATTR_PART_OF_COMPOUND = new Integer(Art.ATTR_PART_OF_COMPOUND),
			ATTR_STYLE_IS_DIRTY = new Integer(Art.ATTR_STYLE_IS_DIRTY);

	// use a map to keep track of already added art objects:
	HashMap map;

	public ArtSet() {
		map = new HashMap();
	}

	public ArtSet(Collection artObjects) {
		this();
		addAll(artObjects);
	}

	public ArtSet(Object[] artObjects) {
		this(Arrays.asList(artObjects));
	}

	public Art getArt(int index) {
		return (Art) get(index);
	}

	/**
	 * Adds the art to the ArtSet, only if it does not already exist in it.
	 * @param index
	 * @param art
	 * @return true if the art was added to the set.
	 */
	public boolean add(int index, Object art) {
		if (art instanceof Art) {
			if (map.get(art) == null) {
				if (super.add(index, art)) {
					map.put(art, art);
					return true;
				}
			}
		}
		return false;
	}

	public boolean addAll(int index, Collection c) {
		// get around ArrayList's addAll that does not rely on add() but the much faster version that adds all elemnts at once:
		boolean modified = false;
		Iterator e = c.iterator();
		while (e.hasNext()) {
			if (add(index++, e.next()))
				modified = true;
		}
		return modified;
	}

	public Object remove(int index) {
		Object obj = super.remove(index);
		if (obj != null)
			map.remove(obj);
		return obj;
	}

	public boolean contains(Object element) {
		return map.get(element) != null;
	}

	private static native Object[] nativeGetSelected();
	private static native Object[] nativeGetMatching(Class type, Map attributes);
	private static native Object[] nativeGetLayer(int artHandle);
	private static native Object[] nativeInvert(Object[] artObjects);

	public static ArtSet getSelected() {
		return new ArtSet(nativeGetSelected());
	}

	public static ArtSet getMatching(Class type, Map attributes) {
		return new ArtSet(nativeGetMatching(type, attributes));
	}

	public static ArtSet getMatching(Class type, NativeObject attributes) {
		return getMatching(type, FunctionHelper.convertToMap(attributes));
	}

	public static ArtSet getLayer(Layer layer) {
		return new ArtSet(nativeGetLayer(layer.handle));
	}

	public ArtSet invert() {
		return new ArtSet(nativeInvert(toArray()));
	}
}
