/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id$
 */

package com.scriptographer.adm;

import java.awt.FlowLayout;
import java.util.Map;

import com.scratchdisk.util.ConversionHelper;

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
			{ TableLayout.PREFERRED, TableLayout.PREFERRED },
			rows
		};

		TableLayout layout = new TableLayout(sizes, 4, 4);
		this.setLayout(layout);
		this.setMargins(4, 4, 4, 4);
		
		for (int i = 0; i < items.length; i++) {
			PromptItem promptItem = items[i];
			if (promptItem != null) {
				if (promptItem.description != null) {
					Static descItem = new Static(this);
					descItem.setFont(Dialog.FONT_PALETTE);
					descItem.setText(promptItem.description + ":");
					descItem.setMargins(0, 2, 0, 0);
					this.addToContent(descItem, "0, " + i);
				}
				
				com.scriptographer.adm.Item valueItem =
						promptItem.createItem(this);
				this.addToContent(valueItem, "1, " + i);
			}
		}			
		
		ItemContainer buttons = new ItemContainer(
				new FlowLayout(FlowLayout.RIGHT, 0, 0));
		
		Button cancelButton = new Button(this);
		cancelButton.setFont(Dialog.FONT_PALETTE);
		cancelButton.setText("Cancel");
		cancelButton.setMargins(0, 0, 4, 0);
		buttons.add(cancelButton);
		
		Button okButton = new Button(this);
		okButton.setFont(Dialog.FONT_PALETTE);
		okButton.setText("OK");
		buttons.add(okButton);

		this.addToContent(buttons, "0, " + items.length + ", 1, " + items.length);
		
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
	
	private static double getValue(Map map, String name) {
		Object obj = map.get(name);
		if (obj != null)
			return ConversionHelper.toDouble(obj);
		else
			return Double.NaN;
	}

	private static PromptItem[] getItems(Map[] items) {
		PromptItem[] promptItems = new PromptItem[items.length];
		for (int i = 0; i < items.length; i++) {
			Map map = items[i];
			Object typeObj = map.get("type");
			Object valueObj = map.get("value");
			double increment = 0;
			Object[] values = null;
			int type = -1;
			if (typeObj != null) {
				if (typeObj instanceof String) {
					type = PromptItem.getType((String) typeObj);
				} else if (typeObj instanceof Number) {
					type = ((Number) typeObj).intValue();
				}
			} else { // determine type from value and step:
				Object incrementObj = map.get("increment");
				if (incrementObj != null) {
					type = PromptItem.TYPE_RANGE;
					increment = ConversionHelper.toDouble(incrementObj);
					if (Double.isNaN(increment))
						increment = 0;
				} else {
					Object valuesObj = map.get("values");
					if (valuesObj != null && valuesObj instanceof Object[])
						values = (Object[]) valuesObj;
					if (values != null)
						type = PromptItem.TYPE_LIST;
					else if (valueObj instanceof Number)
						type = PromptItem.TYPE_NUMBER;
					else if (valueObj instanceof Boolean)
						type = PromptItem.TYPE_CHECKBOX;
					else if (valueObj instanceof String) 
						type = PromptItem.TYPE_STRING;
				}
			}
			
			if (type != -1) {
				Object obj = map.get("description");
				String desc = obj != null ? obj.toString() : null;
				PromptItem item = new PromptItem(type, desc, valueObj);

				double width = getValue(map, "width");
				if (!Double.isNaN(width))
					item.setWidth((int) width);

				double precision = getValue(map, "precision");
				if (!Double.isNaN(precision))
					item.setPrecision((int) precision);

				double min = getValue(map, "min");
				double max = getValue(map, "max");
				if (!Double.isNaN(min) || !Double.isNaN(max))
					item.setRange((float) min, (float) max);
				
				if (values != null)
					item.setValues(values);

				promptItems[i] = item;
			} else {
				promptItems[i] = null;
			}
		}
		return promptItems;
	}

	public static Object[] prompt(String title, Map[] items) {
		PromptDialog dialog = new PromptDialog(title, items);
		return dialog.doModal() == dialog.getDefaultItem()
			? dialog.getValues()
			: null;
	}
}