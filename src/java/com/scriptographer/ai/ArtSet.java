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
 * $Revision: 1.6 $
 * $Date: 2005/04/07 20:12:55 $
 */

package com.scriptographer.ai;

import java.util.*;

import com.scriptographer.util.ArrayList;

public class ArtSet extends ArrayList {
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

	public native ArtSet invert();
	
	public native Raster rasterize(int type, float resolution, int antialiasing, float width, float height);
	
	public Raster rasterize(int type, float resolution, int antialiasing) {
		return rasterize(type, resolution, antialiasing, -1, -1);
	}
	
	public Raster rasterize(int type) {
		return rasterize(type, 0, 4, -1, -1);
	}
	
	public Raster rasterize() {
		return rasterize(-1, 0, 4, -1, -1);
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[");
		for (int i = 0; i < size(); i++) {
			if (i > 0)
				buffer.append(", ");
			buffer.append(get(i).toString());
		}
		buffer.append("]");
		return buffer.toString();
	}
}
