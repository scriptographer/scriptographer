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
 * File created on 19.02.2005.
 * 
 * $RCSfile: MenuItem.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:01:01 $
 */

package com.scriptographer.ai;

import com.scriptographer.js.WrappableObject;
import com.scriptographer.js.Unsealed;
import com.scriptographer.js.FunctionHelper;

import java.util.HashMap;
import java.util.Iterator;

import org.mozilla.javascript.Function;

public class MenuItem extends WrappableObject implements Unsealed {
	public final static int
		OPTION_NONE 			= 0,
		OPTION_WANTS_UPDATE 	= 1 << 0,
		OPTION_ALWAYS_ENABLED	= 1 << 1;

	protected int itemHandle;
	protected String name;
	protected String text;
	protected MenuGroup group;
	private MenuGroup subGroup;

	private static HashMap items = new HashMap();

	public MenuItem(String name, String text, MenuGroup group, int options) {
		this.name = name;
		this.text = text;
		this.group = group;

		itemHandle = 0;
		synchronized(items) {
			for (Iterator iterator = items.values().iterator(); iterator.hasNext();) {
				MenuItem item = (MenuItem) iterator.next();
				if (this.equals(item)) {
					// take over this item:
					itemHandle = item.itemHandle;
					item.itemHandle = 0;
				}
			}
		}

		// if no item has been taken over, create a new one:
		if (itemHandle == 0)
			itemHandle = nativeCreate(name, text, group.name, options);

		if (itemHandle == 0)
			throw new RuntimeException("Unable to create MenuItem");

		putItem(this);
	}

	public MenuItem(String name, String text, MenuGroup group) {
		this(name, text, group, OPTION_NONE);
	}

	/**
	 * Creates the MenuItem as a subitem of parentItem. If parentItem does not have a sub group already,
	 * one with default parameters is created. If a group with other options is needed, use createSubGroup
	 * and pass the resulting group to the constructor that takes a group as third parameter.
	 *
	 * @param name
	 * @param text
	 * @param parentItem
	 * @param options
	 *
	 * @see MenuItem(String, String, MenuGroup, int)
	 */
	public MenuItem(String name, String text, MenuItem parentItem, int options) {
		this(name, text,
				parentItem.subGroup == null ? parentItem.createSubGroup() : parentItem.subGroup,
				options
		);
	}

	public MenuItem(String name, String text, MenuItem parentItem) {
		this(name, text, parentItem, OPTION_NONE);
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
		if (subGroup == null) {
			subGroup = new MenuGroup(name, this, options);
			return subGroup;
		}
		return null;
	}

	public MenuGroup createSubGroup() {
		return createSubGroup(MenuGroup.OPTION_NONE);
	}

	/**
	 * Used in wrapItemHandle
	 *
	 * @param itemHandle
	 * @param name
	 * @param text
	 */
	protected MenuItem(int itemHandle, String name, String text, MenuGroup group) {
		this.itemHandle = itemHandle;
		this.name = name;
		this.text = text;
		this.group = group;
		putItem(this);
	}

	/**
	 * Called from the native environment to wrap a MenuItem:
	 *
	 * @param itemHandle
	 * @param name
	 * @param text
	 * @param groupHandle
	 * @param groupName
	 * @return
	 */
	protected static MenuItem wrapItemHandle(int itemHandle, String name, String text, int groupHandle, String groupName) {
		MenuItem item = getItem(itemHandle);
		if (item == null)
			item = new MenuItem(itemHandle, name, text, MenuGroup.wrapGroupHandle(groupHandle, groupName));
		return item;
	}

	public void remove() {
		nativeRemove(itemHandle);
		Integer key = new Integer(itemHandle);
		if (items.get(key) == this)
			items.remove(key);
		itemHandle = 0;
	}

	public static void removeAll() {
		// as remove() modifies the map, using an iterator is not possible here:
		Object[] items = MenuItem.items.values().toArray();
		for (int i = 0; i < items.length; i++) {
			((MenuItem) items[i]).remove();
		}
	}

	private native int nativeCreate(String name, String text, String group, int options);
	private native int nativeRemove(int itemHandle);

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

	// TODO: add support for UpdateMenuItemAutomatically and all the parameters needed for it

	// Callback stuff:

	private Function onExecute = null;
	private Function onUpdate = null;

	protected void onExecute() throws Exception {
		if (wrapper != null) {
			if (onExecute == null)
				onExecute = FunctionHelper.getFunction(wrapper, "onExecute");
			if (onExecute != null)
				FunctionHelper.callFunction(wrapper, onExecute);
		}
	}

	protected void onUpdate(int inArtwork, int isSelected, int isTrue) throws Exception {
		if (wrapper != null) {
			if (onUpdate == null)
				onUpdate = FunctionHelper.getFunction(wrapper, "onUpdate");
			if (onUpdate != null) {
				FunctionHelper.callFunction(wrapper, onUpdate, new Object[] {
					new Integer(inArtwork), new Integer(isSelected), new Integer(isTrue)
				});
			}
		}
	}

	/**
	 * To be called from the native environment:
	 */
	private static void onExecute(int itemHandle) throws Exception {
		MenuItem item = getItem(itemHandle);
		if (item != null)
			item.onExecute();
	}

	/**
	 * To be called from the native environment:
	 */
	private static void onUpdate(int itemHandle, int inArtwork, int isSelected, int isTrue) throws Exception {
		MenuItem item = getItem(itemHandle);
		if (item != null)
			item.onUpdate(inArtwork, isSelected, isTrue);
	}

	private static void putItem(MenuItem item) {
		items.put(new Integer(item.itemHandle), item);
	}

	private static MenuItem getItem(int itemHandle) {
		return (MenuItem) items.get(new Integer(itemHandle));
	}
}
