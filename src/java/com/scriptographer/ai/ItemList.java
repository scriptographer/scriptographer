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
 * File created on 18.01.2005.
 */

package com.scriptographer.ai;

import java.util.HashMap;

import com.scratchdisk.list.ExtendedArrayList;
import com.scratchdisk.list.Lists;
import com.scratchdisk.list.ReadOnlyList;
import com.scratchdisk.list.ReadOnlyStringIndexList;

/**
 * @author lehni
 * 
 * @jshide
 */
public class ItemList extends ExtendedArrayList<Item> implements ReadOnlyStringIndexList<Item> {
	HashMap<Item, Item> map;

	public ItemList() {
		map = new HashMap<Item, Item>();
	}

	/**
	 * @jshide
	 */
	public ItemList(ReadOnlyList<Item> items) {
		this();
		addAll(items);
	}

	public ItemList(Item... items) {
		this(Lists.asList(items));
	}

	public Class<Item> getComponentType() {
		return Item.class;
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
		if (name != null)
			for (Item item : this)
				if (item.isValid() && name.equals(item.getName()))
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
