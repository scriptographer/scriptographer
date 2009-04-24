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
import java.util.Map;

import com.scratchdisk.util.ConversionUtils;
import com.scratchdisk.util.StringUtils;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.sg.Preferences;

/**
 * @author lehni
 */
public class PromptDialog extends ModalDialog {

	private PromptItem[] items = null;
	private Object[] values = null;

	public PromptDialog(String title, PromptItem[] items) {
		this.setTitle(title);
		this.items = items;
		
		double[] rows = new double[items.length + 1];
		for (int i = 0; i < rows.length; i++)
			rows[i] = TableLayout.PREFERRED;

		double[][] sizes = {
			{ TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED },
			rows
		};

		TableLayout layout = new TableLayout(sizes);
		this.setLayout(layout);
		this.setMargin(6);
//		this.setFont(Dialog.FONT_PALETTE);

		ImageStatic logo = new ImageStatic(this);
		logo.setImage(getImage("logo.png"));
		logo.setMargin(-4, 4, -4, -4);
		this.addToContent(logo, "0, 0, 0, " + items.length + ", left, top");

		for (int i = 0; i < items.length; i++) {
			PromptItem promptItem = items[i];
			if (promptItem != null) {
				if (promptItem.description != null) {
					Static descItem = new Static(this);
					descItem.setText(promptItem.description + ":");
					descItem.setMargin(0, 4, 0, 0);
					this.addToContent(descItem, "1, " + i + ", left, center");
				}
				
				com.scriptographer.ui.Item valueItem =
						promptItem.createItem(this);
				this.addToContent(valueItem, "2, " + i + ", left, center");
			}
		}			
		
		ItemGroup buttons = new ItemGroup(this);
		buttons.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttons.setTopMargin(4);

		Button cancelButton = new Button(this);
		cancelButton.setText("Cancel");
		cancelButton.setRightMargin(4);
		buttons.addToContent(cancelButton);

		Button okButton = new Button(this);
		okButton.setText("  OK  ");
		buttons.addToContent(okButton);

		this.addToContent(buttons, "0, " + items.length + ", 2, " + items.length + ", right, top");

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

	private static PromptItem[] getItems(Map[] items) {
		PromptItem[] promptItems = new PromptItem[items.length];
		for (int i = 0; i < items.length; i++) {
			Map map = items[i];
			if (map != null) {
				Object typeObj = map.get("type");
				Object valueObj = map.get("value");
				double increment = 0;
				Object[] values = null;
				PromptItemType type = null;
				if (typeObj != null) {
					if (typeObj instanceof String)
						type = PromptItemType.get((String) typeObj);
				} else { // determine type from value and step:
					Object incrementObj = map.get("increment");
					if (incrementObj != null) {
						type = PromptItemType.RANGE;
						increment = ConversionUtils.toDouble(incrementObj);
						if (Double.isNaN(increment))
							increment = 0;
					} else {
						Object valuesObj = map.get("values");
						if (valuesObj != null && valuesObj instanceof Object[])
							values = (Object[]) valuesObj;
						if (values != null)
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
					PromptItem item = new PromptItem(type, ConversionUtils.getString(map, "description"), valueObj);
					item.setName(ConversionUtils.getString(map, "name"));

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
					
					if (values != null)
						item.setValues(values);

					promptItems[i] = item;
				} else {
					promptItems[i] = null;
				}
			}
		}
		return promptItems;
	}

	private static double getDouble(Map map, String key) {
		Object obj = map.get(key);
		return obj == null ? Double.NaN : ConversionUtils.toDouble(obj);
	}

	public static Object[] prompt(String title, PromptItem[] items) {
		Preferences preferences = 
			new Preferences(ScriptographerEngine.getPreferences(true));
		String itemTitle = "item" + StringUtils.capitalize(title);
		for (int i = 0; i < items.length; i++) {
			PromptItem item = items[i];
			if (item != null) {
				if (item.name == null)
					item.name = itemTitle + item.description + i;
				Object value = preferences.get(item.name);
				if (value != null)
					item.value = value;
			}
		}
		PromptDialog dialog = new PromptDialog(title, items);
		if (dialog.doModal() == dialog.getDefaultItem()) {
			Object[] values = dialog.getValues();
			for (int i = 0; i < items.length; i++) {
				PromptItem item = items[i];
				if (item != null)
					preferences.put(item.name, values[i]);
			}
			return values;
		}
		return null;
	}

	public static Object[] prompt(String title, Map[] items) {
		return prompt(title, getItems(items));
	}
}