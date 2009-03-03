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
 * File created on 18.01.2005.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import java.util.HashMap;

import com.scratchdisk.list.ArrayList;
import com.scratchdisk.list.ExtendedList;
import com.scratchdisk.list.Lists;
import com.scratchdisk.list.StringIndexReadOnlyList;

/**
 * @author lehni
 */
public class ItemList extends ArrayList<Item> implements StringIndexReadOnlyList<Item> {
	HashMap<Item, Item> map;

	public ItemList() {
		map = new HashMap<Item, Item>();
	}

	public ItemList(ExtendedList<Item> items) {
		this();
		addAll(items);
	}

	public ItemList(Item[] items) {
		this(Lists.asList(items));
	}

	/**
	 * Adds the item to the ItemSet, only if it does not already exist in it.
	 * @param index
	 * @param item
	 * @return true if the item was added to the set.
	 */
	public Item add(int index, Item item) {
		if (map.get(item) == null) {
			if (super.add(index, item) != null) {
				map.put(item, item);
				return item;
			}
		}
		return null;
	}

	public Item get(String name) {
		for (Item item : this)
			if (!item.isDefaultName() && item.getName().equals(name))
				return item;
		return null;
	}

	public Item remove(int index) {
		Item obj = super.remove(index);
		if (obj != null)
			map.remove(obj);
		return obj;
	}

	public boolean contains(Object element) {
		return map.get(element) != null;
	}

	private native Raster nativeRasterize(int type, float resolution,
			int antialiasing, float width, float height);
	
	/**
	 * @param type
	 * @param resolution
	 * @param antialiasing
	 * @param width
	 * @param height
	 * @return
	 */
	public Raster rasterize(ColorType type, float resolution, int antialiasing,
			float width, float height) {
		// Make sure the document is activated.
		activateDocument(false);
		return nativeRasterize(type != null ? type.value : -1, resolution, antialiasing, width, height);
	}

	private void activateDocument(boolean focus) {
		if (size() > 0) {
			Document doc = get(0).getDocument();
			doc.activate(focus);
		}
	}

	public Raster rasterize(ColorType type, float resolution, int antialiasing) {
		return rasterize(type, resolution, antialiasing, -1, -1);
	}
	
	public Raster rasterize(ColorType type) {
		return rasterize(type, 0, 4, -1, -1);
	}
	
	public Raster rasterize() {
		return rasterize(null, 0, 4, -1, -1);
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
