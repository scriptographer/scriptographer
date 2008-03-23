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
 * File created on 19.02.2005.
 * 
 * $Id$
 */

package com.scriptographer.adm;

import com.scriptographer.ScriptographerEngine; 
import com.scriptographer.ScriptographerException;
import com.scratchdisk.script.Callable;
import com.scratchdisk.util.IntMap;

import java.util.Iterator;

/*
 * Theoretically MenuItem and MenuGroup would belong to the AI package, not ADM
 * But as these are the only classes dealing with the interface there, they were
 * moved here instead, where they make more sense.
 */

/**
 * @author lehni
 */
public class MenuItem extends NativeObject{
	public final static int
		OPTION_NONE 			= 0,
		OPTION_WANTS_UPDATE 	= 1 << 0,
		OPTION_ALWAYS_ENABLED	= 1 << 1,
		OPTION_SEPARATOR		= 1 << 2;
	
	protected String name;
	protected MenuGroup group;
	private MenuGroup subGroup;

	private static IntMap items = new IntMap();

	private static int uniqueId = 0;

	public MenuItem(MenuGroup group, int options) {
		this.name = "Scriptographer MenuItem " + (++uniqueId);

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
			handle = nativeCreate(this.name, this.name, group.name, options);

		if (handle == 0)
			throw new ScriptographerException("Unable to create MenuItem");

		putItem(this);
	}

	public MenuItem(MenuGroup group) {
		this (group, OPTION_NONE);
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
	public MenuItem(MenuItem parentItem, int options) {
		// If a subGroup as created earlier, createSubGroup does not create a
		// new one
		this(parentItem.createSubGroup(), options);
	}

	public MenuItem(MenuItem parentItem) {
		this(parentItem, OPTION_NONE);
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
			subGroup = new MenuGroup(this, options);
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
	protected MenuItem(int handle, String name, MenuGroup group) {
		super(handle);
		this.name = name;
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
	protected static MenuItem wrapHandle(int handle, String name,
			int groupHandle, String groupName) {
		MenuItem item = getItem(handle);
		if (item == null)
			item = new MenuItem(handle, name, MenuGroup.wrapGroupHandle(
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
		// As remove() modifies the map, using an iterator is not possible here:
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
					group.equals(item.group);
		}
		return false;
	}

	public native String getText();
	public native void setText(String text);

	public native void setOptions(int options);
	public native int getOptions();

	public native void setEnabled(boolean enabled);
	public native boolean isEnabled();

	public native void setChecked(boolean checked);
	public native boolean isChecked();

	public void setSeparator(boolean separator) {
		int options = getOptions();
		if (separator) options |= OPTION_SEPARATOR;
		else options &= ~OPTION_SEPARATOR;
		setOptions(options);
		// This seems to do the trick really:
		setText(separator ? "-" : "");
	}
	
	public boolean getSeparator() {
		return (getOptions() & OPTION_SEPARATOR) != 0;
	}

	// TODO: add support for UpdateMenuItemAutomatically and all the parameters
	// needed for it

	// Callback functions:
	
	private Callable onSelect = null;

	public Callable getOnSelect() {
		return onSelect;
	}

	public void setOnSelect(Callable onSelect) {
		this.onSelect = onSelect;
	}

	protected void onSelect() throws Exception {
		if (onSelect != null)
			ScriptographerEngine.invoke(onSelect, this);
	}
	
	private Callable onUpdate = null;

	public Callable getOnUpdate() {
		return onUpdate;
	}

	public void setOnUpdate(Callable onUpdate) {
		this.onUpdate = onUpdate;
		int options = this.getOptions();
		if (onUpdate != null) options |= OPTION_WANTS_UPDATE;
		else options &= ~OPTION_WANTS_UPDATE;
		this.setOptions(options);
	}
	
	protected void onUpdate(int inArtwork, int isSelected, int isTrue)
			throws Exception {
		if (onUpdate != null)
			ScriptographerEngine.invoke(onUpdate, this);
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
