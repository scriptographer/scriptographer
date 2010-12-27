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
 * File created on 03.01.2005.
 */

package com.scriptographer.adm;

import java.util.EnumSet;

import com.scratchdisk.util.EnumUtils;
import com.scratchdisk.util.IntegerEnumUtils;

/**
 * @author lehni
 */
public class ListBox extends ListItem<ListEntry> {

	protected ListBox(Dialog dialog, ItemType type) {
		super(dialog, type);
	}

	public ListBox(Dialog dialog) {
		this(dialog, ItemType.LISTBOX);
	}
	
	protected ListEntry createEntry(int index) {
		return new ListEntry(this, index);
	}

	/**
	 * Empty constructor used for nested HierarchyListBoxes 
	 */
	protected ListBox() {
	}

	public EnumSet<ListStyle> getStyle() {
		return IntegerEnumUtils.getSet(ListStyle.class, nativeGetStyle());
	}

	public void setStyle(EnumSet<ListStyle> style) {
		nativeSetStyle(IntegerEnumUtils.getFlags(style));
	}

	public void setStyle(ListStyle[] style) {
		setStyle(EnumUtils.asSet(style));
	}
}
