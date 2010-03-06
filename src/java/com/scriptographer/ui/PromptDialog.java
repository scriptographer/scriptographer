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
 * File created on 28.03.2005.
 * 
 * $Id:PromptDialog.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer.ui;

import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.Map;

import com.scriptographer.ui.layout.TableLayout;

/**
 * @author lehni
 * 
 * @jshide
 */
public class PromptDialog extends ModalDialog {

	private PaletteItem[] items = null;
	private Object[] values = null;

	public PromptDialog(String title, PaletteItem[] items) {
		setTitle(title);
		this.items = items;
		// Add one more row for the buttons.
		TableLayout layout = Palette.createLayout(this, items, true, 1);
		
		// Add buttons to the layout
		ItemGroup buttons = new ItemGroup(this);
		buttons.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttons.setTopMargin(8);

		Button cancelButton = new Button(this);
		cancelButton.setText("Cancel");
		cancelButton.setRightMargin(4);
		buttons.addToContent(cancelButton);

		Button okButton = new Button(this);
		okButton.setText("  OK  ");
		buttons.addToContent(okButton);

		int numRows = layout.getNumRow();
		addToContent(buttons, "0, " + (numRows - 1) + ", 2, " + (numRows - 1) + ", right, top");

		setDefaultItem(okButton);
		setCancelItem(cancelButton);
		setMargin(8);
	}
	
	public PromptDialog(String title, Map[] items) {
		this(title, PaletteItem.getItems(items));
	}

	public Object[] getValues() {
		if (values == null) {
			values = new Object[items.length];
			
			for (int i = 0; i < items.length; i++) {
				PaletteItem item = items[i];
				if (item != null)
					values[i] = item.getResult();
			}
		}
		return values;
	}

	public static Object[] prompt(String title, PaletteItem[] items) {
		/* TODO: Remove this code as soon as there is another nice way to store values in preferences.
		Preferences preferences = 
			new Preferences(ScriptographerEngine.getPreferences(true));
		String itemTitle = "item" + StringUtils.capitalize(title);
		for (int i = 0; i < items.length; i++) {
			PromptItem item = items[i];
			if (item != null) {
				if (item.getName() == null)
					item.setName(itemTitle + item.getDescription() + i);
				Object value = preferences.get(item.getName());
				if (value != null)
					item.setValue(value);
			}
		}
		*/
		PromptDialog dialog = new PromptDialog(title, items);
		if (dialog.doModal() == dialog.getDefaultItem()) {
			Object[] values = dialog.getValues();
			/*
			for (int i = 0; i < items.length; i++) {
				PromptItem item = items[i];
				if (item != null)
					preferences.put(item.getName(), values[i]);
			}
			*/
			return values;
		}
		return null;
	}

	public static Object[] prompt(String title, Map[] items) {
		return prompt(title, PaletteItem.getItems(items));
	}

	public static Map<String, Object> prompt(String title,
			Map<String, Map> items, Map<String, Object> values) {
		PaletteItem[] paletteItems = PaletteItem.getItems(items, values);
		Object[] results = prompt(title, paletteItems);
		if (results != null) {
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
			for (int i = 0; i < paletteItems.length; i++)
				map.put(paletteItems[i].getName(), results[i]);
			return map;
		}
		return values;
	}

	public static Map<String, Object> prompt(String title,
			Map<String, Map> items) {
		return prompt(title, items, null);
	}
}