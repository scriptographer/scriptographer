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
 * File created on 23.10.2005.
 * 
 * $Id:PromptItem.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer.ui;

import java.util.ArrayList;
import java.util.Map;

import com.scratchdisk.script.Callable;
import com.scratchdisk.script.ScriptEngine;
import com.scratchdisk.util.ConversionUtils;
import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 * 
 * @jshide
 */
public class PaletteItem {
	
	private String label;
	private String name; // for preferences
	private PaletteItemType type;
	private Object value;
	private Object options[] = null;
	private int width = -1;
	private int height = -1;
	private int rows = -1;
	private int columns = -1;
	private float min;
	private float max;
	private float increment = 0;
	private int precision = 3;
	private Item item;
	private Callable onChange = null;
	
	public PaletteItem(PaletteItemType type, String label, Object value) {
		this.label = label;
		this.type = type;
		this.value = value;
		this.setRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Creates a STRING Item
	 */
	public PaletteItem(String description, String value) {
		this(PaletteItemType.STRING, description, value);
	}

	/**
	 * Creates a NUMBER Item
	 */
	public PaletteItem(String description, Number value) {
		this(PaletteItemType.NUMBER, description, value);
	}

	public PaletteItem(String description, float value) {
		this(PaletteItemType.NUMBER, description, new Float(value));
	}

	/**
	 * Creates a BOOLEAN Item
	 */
	public PaletteItem(String description, Boolean value) {
		this(PaletteItemType.CHECKBOX, description, value);
	}

	public PaletteItem(String description, boolean value) {
		this(description, new Boolean(value));
	}
	/**
	 * Creates a RANGE Item
	 */
	public PaletteItem(String description, Number value, float min, float max,
			float step) {
		this(PaletteItemType.RANGE, description, value);
		this.setRange(min, max);
		this.increment = step;
	}
	
	/**
	 * Creates a LIST Item
	 */
	public PaletteItem(String description, Object value, Object[] options) {
		this(PaletteItemType.LIST, description, value);
		this.options = options;
	}
	
	// TODO: make constructor for UNIT
	
	/*
	 * Setters
	 */
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public float[] getRange() {
		return new float[] {
			min, max
		};
	}

	public void setRange(float min, float max) {
		this.min = min;
		this.max = max;
	}

	public void setRange(float[] range) {
		setRange(range[0], range[1]);
	}

	public float getIncrement() {
		return increment;
	}

	public void setIncrement(float increment) {
		this.increment = increment;
	}

	public Object[] getOptions() {
		return options;
	}

	public void setOptions(Object[] options) {
		this.options = options;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	

	public int getPrecision() {
		return precision;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Callable getOnChange() {
		return onChange;
	}

	public void setOnChange(Callable onChange) {
		this.onChange = onChange;
	}

	protected void onChange() {
		String name = getName();
		Object value = getResult();
		if (onChange != null)
			ScriptographerEngine.invoke(onChange, this, name, value);
		if (item.dialog instanceof Palette) {
			Palette palette = (Palette) item.dialog;
			palette.onChange(this, name, value);
		}
	}

	protected Item createItem(Dialog dialog, Border margin) {
		// Item:
		item = null;
		switch (type) {
		case RANGE:
			item = new Slider(dialog) {
				protected void onChange() throws Exception {
					PaletteItem.this.onChange();
				}
			};
			break;
		case CHECKBOX:
			item = new CheckBox(dialog) {
				protected void onClick() throws Exception {
					PaletteItem.this.onChange();
				}
			};
			break;
		case LIST:
			item = new PopupList(dialog) {
				protected void onChange() throws Exception {
					PaletteItem.this.onChange();
				}
			};
			break;
		case BUTTON:
			item = new Button(dialog) {
				protected void onClick() throws Exception {
					PaletteItem.this.onChange();
				}
			};
			break;
		default:
			TextOption[] options = type == PaletteItemType.TEXT
					? new TextOption[] { TextOption.MULTILINE }
					: null;
			item = new TextEdit(dialog, options) {
				protected void onChange() throws Exception {
					PaletteItem.this.onChange();
				}
			};
		}

		// Value:
		switch (type) {
		case STRING:
		case TEXT:
			((TextEditItem) item).setText(ConversionUtils.toString(value));
			break;
		case BUTTON:
			((Button) item).setText(ConversionUtils.toString(value));
			break;
		case NUMBER:
		case UNIT:
		case RANGE:
			if (item instanceof TextEditItem) {
				((TextEditItem) item).setAllowMath(true);
				((TextEditItem) item).setAllowUnits(true);
				((TextEditItem) item).setShowUnits(type == PaletteItemType.UNIT);
				((TextEditItem) item).setPrecision(precision);
			}
			if (type == PaletteItemType.RANGE) {
				((Slider) item).setIncrements(increment);
			}
			((ValueItem) item).setRange(min, max);
			((ValueItem) item).setValue(
					(float) ConversionUtils.toDouble(value));
			break;
		case CHECKBOX:
			((CheckBox) item).setChecked(ConversionUtils.toBoolean(value));
			break;
		case LIST: {
			PopupList list = (PopupList) item;
			for (int i = 0; i < options.length; i++) {
				Object option = options[i];
				ListEntry entry = null;
				if (option instanceof ListEntry) {
					entry = (ListEntry) option;
					entry = list.add(entry);
				} else {
					entry = new ListEntry(list);
					entry.setText(option.toString());
				}
				if (entry != null) {
					if (ConversionUtils.equals(value, option))
						entry.setSelected(true);
				}
			}
		}
			break;

		}
		// Margin needs to be defined before setting size, since getBestSize is
		// affected by margin
		item.setMargin(margin);
		Size size;
		if (type == PaletteItemType.TEXT) {
			size = item.getTextSize("H");
			size = new Size(size.width * columns, size.height * rows);
		} else {
			size = item.getBestSize();
			if (width >= 0)
				size.width = width;
			if (height >= 0)
				size.height = height;
		}
		item.setSize(size);
		return item;
	}

	protected Object getResult() {
		switch(type) {
			case STRING:
			case TEXT:
				return ((TextValueItem) item).getText();
			case BUTTON:
				return ((Button) item).getText();
			case NUMBER:
			case UNIT:
			case RANGE:
				return new Float(((ValueItem) item).getValue());
			case CHECKBOX:
				return new Boolean(((ToggleItem) item).isChecked());
			case LIST:
				ListEntry active = ((PopupList) item).getActiveEntry();
				if (active != null)
					return active.getText();
		}
		return null;
	}
/*
	private static double getDouble(Map map, String key, double defaultValue) {
		Object obj = map.get(key);
		return obj == null ? defaultValue : ConversionUtils.toDouble(obj, defaultValue);
	}

	private static double getDouble(Map map, String key) {
		return getDouble(map, key, Double.NaN);
	}
*/
	protected static PaletteItem getItem(Map map, String name, Object value) {
		if (map != null) {
			Object typeObj = map.get("type");
			Object valueObj = value != null ? value : map.get("value");
			float increment = 0;
			Object[] options = null;
			PaletteItemType type = null;
			if (typeObj != null) {
				if (typeObj instanceof String)
					type = PaletteItemType.get((String) typeObj);
			} else { // determine type from value and step:
				Object incrementObj = map.get("increment");
				if (incrementObj != null) {
					type = PaletteItemType.RANGE;
					increment = ConversionUtils.toFloat(incrementObj);
					if (Double.isNaN(increment))
						increment = 0;
				} else {
					Object optionsObj = map.get("options");
					if (optionsObj != null && optionsObj instanceof Object[])
						options = (Object[]) optionsObj;
					if (options != null)
						type = PaletteItemType.LIST;
					else if (valueObj instanceof Number)
						type = PaletteItemType.NUMBER;
					else if (valueObj instanceof Boolean)
						type = PaletteItemType.CHECKBOX;
					else if (valueObj instanceof String) 
						type = PaletteItemType.STRING;
				}
			}
			if (type != null) {
				String label = ConversionUtils.getString(map, "label");
				// Backward compatibility to description:
				if (label == null)
					label = ConversionUtils.getString(map, "description");
				// Produce a default label based on the name if it's not a button
				if (label == null && name != null
						&& type != PaletteItemType.BUTTON)
					label = Character.toUpperCase(name.charAt(0)) + name.substring(1);
				PaletteItem item = new PaletteItem(type, label, valueObj);
				item.setName(name != null ? name : ConversionUtils.getString(map, "name"));

				item.setWidth(ConversionUtils.getInt(map, "width", -1));
				item.setHeight(ConversionUtils.getInt(map, "height", -1));

				if (type == PaletteItemType.TEXT) {
					item.setRows((int) ConversionUtils.getDouble(map, "rows", 4));
					item.setColumns((int) ConversionUtils.getDouble(map, "columns", 32));
				}

				item.setPrecision(ConversionUtils.getInt(map, "precision", 3));

				float min = ConversionUtils.getFloat(map, "min");
				float max = ConversionUtils.getFloat(map, "max");
				if (!Double.isNaN(min) || !Double.isNaN(max))
					item.setRange(min, max);

				Object onChange = map.get("onChange");
				if (onChange == null)
					onChange = map.get("onClick");
				if (onChange != null && !(onChange instanceof Callable))
					onChange = ScriptEngine.convertToJava(onChange, Callable.class);
				if (onChange != null)
					item.setOnChange((Callable) onChange);
				
				if (options != null)
					item.setOptions(options);

				item.setIncrement(increment);
				
				return item;
			}
		}
		return null;
	}

	protected static PaletteItem[] getItems(Map[] items) {
		PaletteItem[] promptItems = new PaletteItem[items.length];
		for (int i = 0; i < items.length; i++)
			promptItems[i] = getItem(items[i], null, null);
		return promptItems;
	}

	protected static PaletteItem[] getItems(Map<String, Map> items,
			Map<String, Object> values) {
		ArrayList<PaletteItem> promptItems = new ArrayList<PaletteItem>();
		for (Map.Entry<String, Map> entry : items.entrySet()) {
			PaletteItem item = getItem(entry.getValue(), entry.getKey(),
					values != null ? values.get(entry.getKey()) : null);
			if (item != null)
				promptItems.add(item);
		}
		return promptItems.toArray(new PaletteItem[promptItems.size()]);
	}
}