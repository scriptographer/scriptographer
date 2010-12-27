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
		Object[] results = prompt(title, comps);
		Component.restoreComponentDefinitions(components);
		if (results != null) {
			if (values == null)
				values = new LinkedHashMap<String, Object>();
			for (int i = 0; i < comps.length; i++)
				values.put(comps[i].getName(), results[i]);
			return values;
		}
		return null;
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
