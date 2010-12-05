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
 * File created on Jun 1, 2010.
 */

package com.scriptographer.ui;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import com.scriptographer.ai.Color;

/**
 * @author lehni
 *
 */
public class Dialog {
	public static void alert(String title, String message) {
		UiFactory.getInstance().alert(title, message);
	}

	public static void alert(String message) {
		alert("Scriptographer", message);
	}

	public static boolean confirm(String title, String message) {
		return UiFactory.getInstance().confirm(title, message);
	}

	public static boolean confirm(String message) {
		return confirm("Scriptographer", message);
	}

	public static Map<String, Object> prompt(String title,
			Map<String, Object> components, Map<String, Object> values) {
		// Similar code as in Palette
		if (components == null)
			components = new LinkedHashMap<String, Object>();
		Component[] comps =
				Component.getComponents(components, values);
		// Make sure we're putting the produced components back into the 
		// passed comonents Map, so they can be accessed from code (e.g. to
		// toggle the enabled flag).
		for (Component component : comps) {
			if (component != null)
				components.put(component.getName(), component);
		}
		Object[] results = prompt(title, comps);
		if (results != null) {
			if (values == null)
				values = new LinkedHashMap<String, Object>();
			for (int i = 0; i < comps.length; i++)
				values.put(comps[i].getName(), results[i]);
		}
		return values;
	}

	public static Map<String, Object> prompt(String title,
			Map<String, Object> items) {
		return prompt(title, items, null);
	}

	/**
	 * @jshide
	 */
	public static Object[] prompt(String title, Component[] components) {
		return UiFactory.getInstance().prompt(title, components);
	}

	public static File fileOpen(String message, String[] filters,
			File selectedFile) {
		return UiFactory.getInstance().fileOpen(message, filters, selectedFile);
	}

	public static File fileOpen(String message, String[] filters) {
		return fileOpen(message, filters, null);
	}

	public static File fileOpen(String message) {
		return fileOpen(message, null, null);
	}

	public static File fileOpen() {
		return fileOpen(null, null, null);
	}

	public static File fileSave(String message, String[] filters,
			File selectedFile) {
		return UiFactory.getInstance().fileSave(message, filters, selectedFile);
	}

	public static File fileSave(String message, String[] filters) {
		return fileSave(message, filters, null);
	}

	public static File fileSave(String message) {
		return fileSave(message, null, null);
	}

	public static File fileSave() {
		return fileSave(null, null, null);
	}

	public static File chooseDirectory(String message, File selectedDir) {
		return UiFactory.getInstance().chooseDirectory(message, selectedDir);
	}

	public static File chooseDirectory(String message) {
		return chooseDirectory(message, null);
	}

	public static File chooseDirectory() {
		return chooseDirectory(null, null);
	}

	public static Color chooseColor(Color color) {
		return UiFactory.getInstance().chooseColor(color);
	}

	public static Color chooseColor() {
		return chooseColor(null);
	}
}
