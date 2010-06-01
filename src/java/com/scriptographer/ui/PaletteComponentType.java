/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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
 */

package com.scriptographer.ui;

import java.util.HashMap;

/**
 * @author lehni
 *
 */
public enum PaletteComponentType {
	STRING("string"),
	NUMBER("number"),
	TEXT("text"),
	RULER("ruler"),
	CHECKBOX("checkbox"),
	LIST("list"),
	BUTTON("button"),
	SLIDER("slider"),
	COLOR("color"),
	FONT("font"),
	MENU_ENTRY("menu-entry"),
	MENU_SEPARATOR("menu-separator");

	protected String name;

	private PaletteComponentType(String name) {
		this.name = name;
	}

	/**
	 * A hash-map for case insensitive retrieval of type objects based on their
	 * name.
	 */
	private static HashMap<String, PaletteComponentType> types =
		new HashMap<String, PaletteComponentType>();

	static {
		for (PaletteComponentType type : values())
			types.put(type.name.toLowerCase(), type);
	}
	
	public static PaletteComponentType get(String name) {
		return types.get(name.toLowerCase());
	}
}
