/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2004-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile: PromptDialog.java,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/04/20 13:49:37 $
 */

package com.scriptographer.adm;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Map;

import org.mozilla.javascript.ScriptRuntime;

public class PromptDialog extends ModalDialog {

	private Object[] values = null;
	
	public PromptDialog(String title, Item[] items) {
		this.setTitle(title);
		
		double[] columns = { TableLayout.PREFERRED, TableLayout.PREFERRED };
		double[] rows = new double[items.length + 1];
		for (int i = 0; i < rows.length; i++)
			rows[i] = TableLayout.PREFERRED;
		
		TableLayout layout = new TableLayout(columns, rows, 4, 4);
		this.setLayout(layout);
		this.setInsets(4, 4, 4, 4);
		
		for (int i = 0; i < items.length; i++) {
			Item promptItem = items[i];
			if (promptItem != null) {
				Static descItem = new Static(this);
				descItem.setFont(Dialog.FONT_PALETTE);
				descItem.setText(promptItem.description + ":");
				this.addToLayout(descItem, "0, " + i);
				
				com.scriptographer.adm.Item valueItem = promptItem.createItem(this);
				this.addToLayout(valueItem, "1, " + i);
			}
		}			
		
		ItemContainer buttons = new ItemContainer(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		buttons.setInsets(-8, 2, -6, 4);
		
		Button okButton = new Button(this);
		okButton.setFont(Dialog.FONT_PALETTE);
		okButton.setText("OK");
		buttons.add(okButton);
		
		Button cancelButton = new Button(this);
		cancelButton.setFont(Dialog.FONT_PALETTE);
		cancelButton.setText("Cancel");
		buttons.add(cancelButton);

		this.addToLayout(buttons, "0, " + items.length + ", 1, " + items.length);
		
		this.setDefaultItem(okButton);
		this.setCancelItem(cancelButton);
		
		this.autoLayout();
		
		values = new Object[items.length];
		
		if (doModal() == okButton) {
			for (int i = 0; i < items.length; i++) {
				values[i] = items[i].getValue();
			}
		} else {
			for (int i = 0; i < items.length; i++) {
				values[i] = items[i].value;
			}
		}
	}
	
	public PromptDialog(String title, Object[] items) {
		this(title, getItems(items));
	}
	
	public Object[] getValues() {
		return values;
	}

	private static Item[] getItems(Object[] items) {
		Item[] promptItems = new Item[items.length];
		for (int i = 0; i < items.length; i++) {
			Object itemObj = items[i];
			if (itemObj instanceof Item) {
				promptItems[i] = (Item) itemObj;
			} else if (itemObj instanceof Map) {
				Map map = (Map) itemObj;
				Object valueObj = map.get("value");
				Object typeObj = map.get("type");
				Object stepObj = map.get("step");
				int type = -1;
				if (typeObj != null) {
					if (typeObj instanceof String) {
						type = Item.getType((String) typeObj);
					} else if (typeObj instanceof Number) {
						type = ((Number) typeObj).intValue();
					}
				} else { // determine type from value and step:
					if (stepObj != null) {
						type = Item.TYPE_RANGE;
					} else {
						if (valueObj instanceof Number)
							type = Item.TYPE_NUMBER;
						else if (valueObj instanceof String) 
							type = Item.TYPE_STRING;
						else if (valueObj instanceof Object[])
							type = Item.TYPE_LIST;
					}
				}
				
				if (type != -1) {
					Object descObj = map.get("description");
					String desc = descObj instanceof String ? (String) descObj : "";
					
					Object widthObj = map.get("width");
					double width = ScriptRuntime.toNumber(widthObj);
					if (widthObj == null || width == ScriptRuntime.NaN)
						width = -1;
	
					Object minObj = map.get("min");
					double min = ScriptRuntime.toNumber(minObj);
					if (minObj == null || min == ScriptRuntime.NaN)
						min = Float.MIN_VALUE;
	
					Object maxObj = map.get("max");
					double max = ScriptRuntime.toNumber(minObj);
					if (maxObj == null || max == ScriptRuntime.NaN)
						max = Float.MAX_VALUE;
	
					double step = ScriptRuntime.toNumber(stepObj);
					if (step == ScriptRuntime.NaN)
						step = 0;
	
					promptItems[i] = new Item(type, desc, valueObj, (int) width, (float) min, (float) max, (float) step);				
				} else {
					promptItems[i] = null;
				}
			} else {
				promptItems[i] = null;
			}
		}
		return promptItems;
	}

	public static Object[] prompt(String title, Item[] items) {
		return new PromptDialog(title, items).values;
	}

	public static Object[] prompt(String title, Object[] items) {
		return new PromptDialog(title, items).values;
	}

	public static class Item {
		public static final int
			TYPE_STRING = 0,
			TYPE_NUMBER = 1,
			TYPE_UNIT = 2,
			TYPE_RANGE = 3,
			TYPE_CHECKBOX = 4,
			TYPE_LIST = 5;
		
		protected static final String[] typeNames = {
			"String",
			"Number",
			"Unit",
			"Range",
			"CheckBox",
			"List"
		};
		
		String description;
		int type;
		Object value;
		float min;
		float max;
		float step;
		com.scriptographer.adm.Item item;
		int width;
		
		public Item(int type, String description, Object value, int width, float min, float max, float step) {
			this.description = description;
			this.type = type;
			this.value = value;
			this.width = width;
			this.min = min;
			this.max = max;
			this.step = step;
		}
	
		public Item(int type, String description, Object value) {
			this(type, description, value, -1, Float.MIN_VALUE, Float.MAX_VALUE, 0);
		}
		
		private com.scriptographer.adm.Item createItem(Dialog dialog) {
			// Item:
			item = null;
			switch (type) {
				case TYPE_RANGE:
					item = new Slider(dialog);
					break;
				case TYPE_CHECKBOX:
					item = new CheckBox(dialog);
					break;
				default:
					item = new TextEdit(dialog);
			}
			
			// Value:
			switch (type) {
				case TYPE_STRING:
					((TextEdit) item).setText(value.toString());
					break;
				case TYPE_NUMBER:
				case TYPE_UNIT:
				case TYPE_RANGE:
					if (item instanceof TextEdit) {
						((TextEdit) item).setAllowMath(true);
						((TextEdit) item).setAllowUnits(true);
						((TextEdit) item).setShowUnits(type == TYPE_UNIT);
					}
					if (type == TYPE_RANGE) {
						((Slider) item).setIncrements(step, 8 * step);
					}
					((ValueItem) item).setRange(min, max);
					((ValueItem) item).setValue((float) ScriptRuntime.toNumber(value));
					break;
				case TYPE_CHECKBOX:
					((CheckBox) item).setChecked(ScriptRuntime.toBoolean(value));
					break;
					
			}
			item.setFont(Dialog.FONT_PALETTE);
			Dimension size = item.getBestSize();
			if (width >= 0)
				size.width = width;
			item.setSize(size);
			return item;
		}
		
		private Object getValue() {
			switch(type) {
				case TYPE_STRING:
					return ((TextItem) item).getText();
				case TYPE_NUMBER:
				case TYPE_UNIT:
				case TYPE_RANGE:
					return new Float(((ValueItem) item).getValue());
				case TYPE_CHECKBOX:
					return new Boolean(((ToggleItem) item).isChecked());
			}
			return null;
		}
		
		private static int getType(String type) {
			for (int i = 0; i < typeNames.length; i++) {
				if (typeNames[i].equals(type))
					return i;
			}
			return -1;
		}
	}
}