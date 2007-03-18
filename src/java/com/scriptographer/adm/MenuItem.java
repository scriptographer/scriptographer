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
 * File created on 19.02.2005.
 * 
 * $Id$
 */

package com.scriptographer.adm;

import com.scriptographer.script.ScriptMethod;
import com.scriptographer.util.IntMap;

import java.util.Iterator;

/*
 * Theoretically MenuItem and MenuGroup would belong to the AI package, not ADM
 * But as these are the only classes dealing with the interface there, they were
 * moved here instead, where they make more sense.
 */

/**
 * @author lehni
 */
public class MenuItem extends ADMObject{
	public final static int
		OPTION_NONE 			= 0,
		OPTION_WANTS_UPDATE 	= 1 << 0,
		OPTION_ALWAYS_ENABLED	= 1 << 1;

	protected String name;
	protected String text;
	protected MenuGroup group;
	private MenuGroup subGroup;

	private static IntMap items = new IntMap();

	public MenuItem(MenuGroup group, String name, String text, int options) {
		this.name = name;
		this.text = text;
		this.group = group;

		synchronized(items) {
			for (Iterator iterator = items.values().iterator();
					iterator.hasNext();) {
				MenuItem item = (MenuItem) iterator.next();
				if (this.equals(item)) {
					// take over this item:
					handle = item.handle;
					item.handle = 0;
				}
			}
		}
		
		// if no item has been taken over, create a new one:
		if (handle == 0)
			handle = nativeCreate(name, text, group.name, options);

		if (handle == 0)
			throw new RuntimeException("Unable to create MenuItem");

		putItem(this);
	}

	public MenuItem(MenuGroup group, String name, String text) {
		this(group, name, text, OPTION_NONE);
	}

	/**
	 * Uses text for the item's name as well.
	 */
	public MenuItem(MenuGroup group, String text, int options) {
		this (group, text, text, options);
	}

	/**
	 * Uses text for the item's name as well.
	 */
	public MenuItem(MenuGroup group, String text) {
		this (group, text, text, OPTION_NONE);
	}

	/**
	 * Creates the MenuItem as a subitem of parentItem. If parentItem does not
	 * have a sub group already, one with default parameters is created. If a
	 * group with other options is needed, use createSubGroup and pass the
	 * resulting group to the constructor that takes a group as third parameter.
	 * 
	 * @param name
	 * @param text
	 * @param parentItem
	 * @param options
	 * 
	 * @see MenuItem(MenuGroup, String, String, int)
	 */
	public MenuItem(MenuItem parentItem, String name, String text, int options) {
		// if a subGroup as created earlier, createSubGroup does not create a
		// new one
		this(parentItem.createSubGroup(), name, text, options);
	}

	public MenuItem(MenuItem parentItem, String name, String text) {
		this(parentItem, name, text, OPTION_NONE);
	}

	/**
	 * Uses text for the item's name as well.
	 */
	public MenuItem(MenuItem parentItem, String text, int options) {
		this(parentItem, text, text, options);
	}

	/**
	 * Uses text for the item's name as well.
	 */
	public MenuItem(MenuItem parentItem, String text) {
		this(parentItem, text, text, OPTION_NONE);
	}

	public MenuGroup getSubGroup() {
		return subGroup;
	}

	/**
	 *
	 * @param options MenuGroup.OPTION_*
	 * @return
	 */
	public MenuGroup createSubGroup(int options) {
		if (subGroup == null)
			subGroup = new MenuGroup(name, this, options);
		return subGroup;
	}

	public MenuGroup createSubGroup() {
		return createSubGroup(MenuGroup.OPTION_NONE);
	}

	/**
	 * Used in wrapHandle
	 *
	 * @param handle
	 * @param name
	 * @param text
	 */
	protected MenuItem(int handle, String name, String text, MenuGroup group) {
		super(handle);
		this.name = name;
		this.text = text;
		this.group = group;
		putItem(this);
	}

	/**
	 * Called from the native environment to wrap a MenuItem:
	 *
	 * @param handle
	 * @param name
	 * @param text
	 * @param groupHandle
	 * @param groupName
	 * @return
	 */
	protected static MenuItem wrapHandle(int handle, String name, String text,
			int groupHandle, String groupName) {
		MenuItem item = getItem(handle);
		if (item == null)
			item = new MenuItem(handle, name, text, MenuGroup.wrapGroupHandle(
					groupHandle, groupName));
		return item;
	}

	public void remove() {
		nativeRemove(handle);
		if (items.get(handle) == this)
			items.remove(handle);
		handle = 0;
	}

	public static void removeAll() {
		// as remove() modifies the map, using an iterator is not possible here:
		Object[] items = MenuItem.items.values().toArray();
		for (int i = 0; i < items.length; i++) {
			((MenuItem) items[i]).remove();
		}
	}

	private static native int nativeCreate(String name, String text,
			String group, int options);

	private static native int nativeRemove(int handle);

	public boolean equals(Object obj) {
		if (obj instanceof MenuItem) {
			MenuItem item = (MenuItem) obj;
			return name.equals(item.name) &&
					text.equals(item.text) &&
					group.equals(item.group);
		}
		return false;
	}

	public String getName() {
		return name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		nativeSetText(text);
	}

	private native void nativeSetText(String text);

	public native void setOption(int options);
	public native int getOptions();

	public native void setEnabled(boolean enabled);
	public native boolean isEnabled();

	public native void setChecked(boolean checked);
	public native boolean isChecked();

	// TODO: add support for UpdateMenuItemAutomatically and all the parameters
	// needed for it

	// Callback functions:
	
	private ScriptMethod onSelect = null;

	public ScriptMethod getOnSelect() {
		return onSelect;
	}

	public void setOnSelect(ScriptMethod onSelect) {
		this.onSelect = onSelect;
	}

	protected void onSelect() throws Exception {
		if (onSelect != null)
			onSelect.execute(this);
	}
	
	private ScriptMethod onUpdate = null;

	public ScriptMethod getOnUpdate() {
		return onUpdate;
	}

	public void setOnUpdate(ScriptMethod onUpdate) {
		this.onUpdate = onUpdate;
	}
	
	protected void onUpdate(int inArtwork, int isSelected, int isTrue)
			throws Exception {
		if (onUpdate != null)
			onUpdate.execute(this);
	}

	/**
	 * To be called from the native environment:
	 */
	private static void onSelect(int handle) throws Exception {
		MenuItem item = getItem(handle);
		if (item != null)
			item.onSelect();
	}

	/**
	 * To be called from the native environment:
	 */
	private static void onUpdate(int handle, int inArtwork, int isSelected,
			int isTrue) throws Exception {
		MenuItem item = getItem(handle);
		if (item != null)
			item.onUpdate(inArtwork, isSelected, isTrue);
	}

	private static void putItem(MenuItem item) {
		items.put(item.handle, item);
	}

	private static MenuItem getItem(int handle) {
		return (MenuItem) items.get(handle);
	}
}
