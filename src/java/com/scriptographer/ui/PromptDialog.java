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
 * File created on 28.03.2005.
 * 
 * $Id:PromptDialog.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer.ui;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.scratchdisk.util.ConversionUtils;

/**
 * @author lehni
 * 
 * @jshide
 */
public class PromptDialog extends ModalDialog {

	private PromptItem[] items = null;
	private Object[] values = null;

	public PromptDialog(String title, PromptItem[] items) {
		this.setTitle(title);
		this.items = items;
		// Add two more rows, one as a filler in case there's less rows than the
		// height of the logo, and one for the buttons.
		double[] rows = new double[items.length + 2];
		for (int i = 0; i < rows.length; i++)
			rows[i] = TableLayout.PREFERRED;
		// Define the filler row, 2nd last
		rows[rows.length - 2] = TableLayout.FILL;
		double[][] sizes = {
			{ TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED },
			rows
		};

		TableLayout layout = new TableLayout(sizes);
		this.setLayout(layout);
		this.setMargin(8);
//		this.setFont(Dialog.FONT_PALETTE);

		ImagePane logo = new ImagePane(this);
		logo.setImage(getImage("logo.png"));
		logo.setMargin(-4, 4, -4, -4);
		// Logo uses all rows of items + filler row
		this.addToContent(logo, "0, 0, 0, " + (rows.length - 2) + ", left, top");

		for (int i = 0; i < items.length; i++) {
			PromptItem promptItem = items[i];
			if (promptItem != null) {
				String desc = promptItem.getDescription();
				if (desc != null) {
					TextPane descItem = new TextPane(this);
					descItem.setText(desc + ":");
					descItem.setMargin(0, 4, 0, 0);
					this.addToContent(descItem, "1, " + i + ", left, center");
				}
				
				com.scriptographer.ui.Item valueItem =
						promptItem.createItem(this, new Border(1, 0, 1, 0));
				this.addToContent(valueItem, "2, " + i + ", left, center");
			}
		}			
		
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

		this.addToContent(buttons, "0, " + (rows.length - 1) + ", 2, " + (rows.length - 1) + ", right, top");

		this.setDefaultItem(okButton);
		this.setCancelItem(cancelButton);
	}
	
	public PromptDialog(String title, Map[] items) {
		this(title, getItems(items));
	}
	
	public Object[] getValues() {
		if (values == null) {
			values = new Object[items.length];
			
			for (int i = 0; i < items.length; i++) {
				PromptItem item = items[i];
				if (item != null)
					values[i] = item.getResult();
			}
		}
		return values;
	}

	private static PromptItem getItem(Map map, String name, Object value) {
		if (map != null) {
			Object typeObj = map.get("type");
			Object valueObj = value != null ? value : map.get("value");
			float increment = 0;
			Object[] options = null;
			PromptItemType type = null;
			if (typeObj != null) {
				if (typeObj instanceof String)
					type = PromptItemType.get((String) typeObj);
			} else { // determine type from value and step:
				Object incrementObj = map.get("increment");
				if (incrementObj != null) {
					type = PromptItemType.RANGE;
					increment = ConversionUtils.toFloat(incrementObj);
					if (Double.isNaN(increment))
						increment = 0;
				} else {
					Object optionsObj = map.get("options");
					if (optionsObj != null && optionsObj instanceof Object[])
						options = (Object[]) optionsObj;
					if (options != null)
						type = PromptItemType.LIST;
					else if (valueObj instanceof Number)
						type = PromptItemType.NUMBER;
					else if (valueObj instanceof Boolean)
						type = PromptItemType.CHECKBOX;
					else if (valueObj instanceof String) 
						type = PromptItemType.STRING;
				}
			}
			if (type != null) {
				String description = ConversionUtils.getString(map, "description");
				if (description == null && name != null)
					description = Character.toUpperCase(name.charAt(0)) + name.substring(1);
				PromptItem item = new PromptItem(type, description, valueObj);
				item.setName(name != null ? name : ConversionUtils.getString(map, "name"));

				double width = getDouble(map, "width");
				if (!Double.isNaN(width))
					item.setWidth((int) width);

				double precision = getDouble(map, "precision");
				if (!Double.isNaN(precision))
					item.setPrecision((int) precision);

				double min = getDouble(map, "min");
				double max = getDouble(map, "max");
				if (!Double.isNaN(min) || !Double.isNaN(max))
					item.setRange((float) min, (float) max);
				
				if (options != null)
					item.setOptions(options);

				item.setIncrement(increment);
				
				return item;
			}
		}
		return null;
	}

	private static PromptItem[] getItems(Map[] items) {
		PromptItem[] promptItems = new PromptItem[items.length];
		for (int i = 0; i < items.length; i++)
			promptItems[i] = getItem(items[i], null, null);
		return promptItems;
	}

	private static PromptItem[] getItems(Map<String, Map> items,
			Map<String, Object> values) {
		ArrayList<PromptItem> promptItems = new ArrayList<PromptItem>();
		for (Map.Entry<String, Map> entry : items.entrySet()) {
			PromptItem item = getItem(entry.getValue(), entry.getKey(),
					values != null ? values.get(entry.getKey()) : null);
			if (item != null)
				promptItems.add(item);
		}
		return promptItems.toArray(new PromptItem[promptItems.size()]);
	}

	private static double getDouble(Map map, String key) {
		Object obj = map.get(key);
		return obj == null ? Double.NaN : ConversionUtils.toDouble(obj);
	}

	public static Object[] prompt(String title, PromptItem[] items) {
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
		return prompt(title, getItems(items));
	}

	public static Map<String, Object> prompt(String title,
			Map<String, Map> items, Map<String, Object> values) {
		PromptItem[] promptItems = getItems(items, values);
		Object[] results = prompt(title, promptItems);
		if (results != null) {
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
			for (int i = 0; i < promptItems.length; i++)
				map.put(promptItems[i].getName(), results[i]);
			return map;
		}
		return values;
	}

	public static Map<String, Object> prompt(String title,
			Map<String, Map> items) {
		return prompt(title, items, null);
	}
}