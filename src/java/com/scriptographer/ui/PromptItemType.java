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
 * File created on Apr 15, 2008.
 *
 * $Id$
 */

package com.scriptographer.ui;

import java.util.HashMap;

/**
 * @author lehni
 *
 */
public enum PromptItemType {
	STRING("String"),
	NUMBER("Number"),
	UNIT("Unit"),
	RANGE("Range"),
	CHECKBOX("CheckBox"),
	LIST("List");

	protected String name;

	private PromptItemType(String name) {
		this.name = name;
	}

	/**
	 * A hash-map for case insensitive retrieval of type objects based on their
	 * name.
	 */
	private static HashMap<String, PromptItemType> types =
		new HashMap<String, PromptItemType>();

	static {
		for (PromptItemType type : values())
			types.put(type.name.toLowerCase(), type);
	}
	
	public static PromptItemType get(String name) {
		return types.get(name.toLowerCase());
	}
}
