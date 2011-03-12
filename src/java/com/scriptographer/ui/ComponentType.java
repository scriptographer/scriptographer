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
 * File created on Apr 15, 2008.
 */

package com.scriptographer.ui;

import java.util.HashMap;

/**
 * @author lehni
 *
 */
public enum ComponentType {
	STRING("string"),
	NUMBER("number"),
	TEXT("text"),
	BOOLEAN("boolean"),
	/**
	* @deprecated
	*/
	CHECKBOX("checkbox"),
	LIST("list"),
	BUTTON("button"),
	SLIDER("slider"),
	/**
	* @deprecated
	*/
	RANGE("range"),
	COLOR("color"),
	FONT("font"),
	RULER("ruler"),
	MENU_ENTRY("menu-entry"),
	MENU_SEPARATOR("menu-separator");

	protected String name;

	private ComponentType(String name) {
		this.name = name;
	}

	/**
	 * A hash-map for case insensitive retrieval of type objects based on their
	 * name.
	 */
	private static HashMap<String, ComponentType> types =
		new HashMap<String, ComponentType>();

	static {
		for (ComponentType type : values())
			types.put(type.name.toLowerCase(), type);
	}
	
	public static ComponentType get(String name) {
		return types.get(name.toLowerCase());
	}
}
