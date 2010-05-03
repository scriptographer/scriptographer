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

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.scratchdisk.script.ArgumentReader;
import com.scratchdisk.script.Callable;
import com.scratchdisk.script.MapArgumentReader;
import com.scratchdisk.script.ScriptEngine;
import com.scratchdisk.util.ConversionUtils;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.ai.FontWeight;

/**
 * @author lehni
 * 
 * @jshide
 */
public class PaletteComponent {

	private String label;
	private String name; // for preferences
	private PaletteComponentType type;
	private Object defaultValue;
	private Object options[] = null;
	private int width = -1;
	private int height = -1;
	private int rows = -1;
	private int length = -1;
	private int columns = -1;
	private float min = Float.NaN;
	private float max = Float.NaN;
	private float increment = 0;
	private int fractionDigits = 3;
	private Item item;
	private Callable onChange = null;
	private TextUnits units = TextUnits.NONE;
	private boolean steppers = false;

	/**
	 * @jshide
	 */
	public PaletteComponent(ArgumentReader reader) throws IllegalArgumentException {
		if (reader.isMap()) {
			type = reader.readEnum("type", PaletteComponentType.class);
			defaultValue = reader.readObject("value");
			options = reader.readObject("options", Object[].class);
			if (type == null) {
				// Determine type form options and value
				if (options != null)
					type = PaletteComponentType.LIST;
				else if (defaultValue instanceof Number)
					type = PaletteComponentType.NUMBER;
				else if (defaultValue instanceof Boolean)
					type = PaletteComponentType.CHECKBOX;
				else if (defaultValue instanceof String) 
					type = PaletteComponentType.STRING;
			}
			if (type != null) {
				// Set default values for rows / columns / length
				if (type == PaletteComponentType.TEXT) {
					rows = 6;
					columns = 32;
				} else if (type == PaletteComponentType.STRING
						|| type == PaletteComponentType.NUMBER) {
					length = type == PaletteComponentType.STRING ? 16 : 8;
				}
				// Tell the framework to set the properties from the map
				// on the object after creating through ArgumentReader.
				reader.setProperties(this);
			}
		} else {
			throw new IllegalArgumentException();
		}
	}

	public PaletteComponent(PaletteComponentType type, String label, Object value) {
		this.label = label;
		this.type = type;
		this.defaultValue = value;
	}

	/**
	 * Creates a STRING Item
	 */
	public PaletteComponent(String description, String value) {
		this(PaletteComponentType.STRING, description, value);
	}

	/**
	 * Creates a NUMBER Item
	 */
	public PaletteComponent(String description, Number value) {
		this(PaletteComponentType.NUMBER, description, value);
	}

	public PaletteComponent(String description, float value) {
		this(PaletteComponentType.NUMBER, description, new Float(value));
	}

	/**
	 * Creates a BOOLEAN Item
	 */
	public PaletteComponent(String description, Boolean value) {
		this(PaletteComponentType.CHECKBOX, description, value);
	}

	public PaletteComponent(String description, boolean value) {
		this(description, new Boolean(value));
	}

	/**
	 * Creates a RANGE Item
	 */
	public PaletteComponent(String description, Number value, float min, float max,
			float step) {
		this(PaletteComponentType.SLIDER, description, value);
		this.setRange(min, max);
		this.increment = step;
	}
	
	/**
	 * Creates a LIST Item
	 */
	public PaletteComponent(String description, Object value, Object[] options) {
		this(PaletteComponentType.LIST, description, value);
		this.options = options;
	}
	
	// TODO: make constructors for other types
	
	/*
	 * Beans
	 */

	public Object getValue() {
		if (item == null) {
			return defaultValue;
		} else {
			switch (type) {
			case STRING:
			case TEXT:
				return ((TextValueItem) item).getText();
			case BUTTON:
				return ((Button) item).getText();
			case NUMBER:
			case SLIDER:
				return new Float(((ValueItem) item).getValue());
			case CHECKBOX:
				return new Boolean(((ToggleItem) item).isChecked());
			case LIST:
				ListEntry active = ((PopupList) item).getSelectedEntry();
				if (active != null)
					return active.getText();
				break;
			case COLOR:
				return ((ColorButton) item).getColor();
			case FONT:
				return ((FontPopupList) item).getFontWeight();
			}
			return null;
		}
	}

	public void setValue(Object value) {
		if (item == null) {
			defaultValue = value;
		} else {
			switch (type) {
			case STRING:
			case TEXT:
				((TextEditItem) item).setText(ConversionUtils.toString(value));
				break;
			case BUTTON:
				((Button) item).setText(ConversionUtils.toString(value));
				break;
			case NUMBER:
			case SLIDER:
				((ValueItem) item).setValue(
						(float) ConversionUtils.toDouble(value));
				break;
			case CHECKBOX:
				((CheckBox) item).setChecked(ConversionUtils.toBoolean(value));
				break;
			case LIST:
				PopupList list = (PopupList) item;
				ListEntry selected = null;
				for (int i = 0, l = list.size(); i < l && selected == null; i++) {
					Object option = options[i];
					ListEntry entry = list.get(i);
					if (ConversionUtils.equals(value, option))
						selected = entry;
				}
				if (selected == null)
					selected = list.getFirst();
				if (selected != null)
					selected.setSelected(true);
				break;
			case COLOR:
				Color color = ScriptEngine.convertToJava(value, Color.class);
				if (color == null)
					color = Color.BLACK;
				((ColorButton) item).setColor(color);
				break;
			case FONT:
				FontWeight weight = ScriptEngine.convertToJava(value,
						FontWeight.class);
				if (weight != null)
					((FontPopupList) item).setFontWeight(weight);
				break;
			}
		}
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
		// Clear columns and length when setting width and vice versa.
		columns = -1;
		length = -1;
		updateSize();
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
		// Clear rows when setting height and vice versa.
		rows = -1;
		updateSize();
	}

	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
		// Clear width when setting columns and vice versa.
		width = -1;
		updateSize();
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
		// Clear height when setting rows and vice versa.
		height = -1;
		updateSize();
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
		// Clear width when setting length and vice versa.
		width = -1;
		updateSize();
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @deprecated
	 */
	public String getDescription() {
		return getLabel();
	}

	/**
	 * @deprecated
	 */
	public void setDescription(String description) {
		setLabel(description);
	}

	/**
	 * @deprecated
	 */
	public float getMin() {
		return min;
	}

	/**
	 * @deprecated
	 */
	public void setMin(float min) {
		setRange(min, max);
	}

	/**
	 * @deprecated
	 */
	public float getMax() {
		return max;
	}

	/**
	 * @deprecated
	 */
	public void setMax(float max) {
		setRange(min, max);
	}

	public float[] getRange() {
		return new float[] {
			min, max
		};
	}

	/**
	 * @jshide
	 */
	public void setRange(float min, float max) {
		this.min = min;
		this.max = max;
		if (item instanceof ValueItem)
			((ValueItem) item).setRange(min, max);
	}

	public void setRange(float[] range) {
		setRange(range[0], range[1]);
	}

	public boolean hasRange() {
		return !Float.isNaN(min) && !Float.isNaN(max);
	}


	public float getIncrement() {
		return increment;
	}

	public void setIncrement(float increment) {
		this.increment = increment;
		if (item instanceof ValueItem)
			((ValueItem) item).setIncrements(increment);
	}

	public Object[] getOptions() {
		return options;
	}

	public void setOptions(Object[] options) {
		this.options = options;
		if (item instanceof PopupList) {
			PopupList list = (PopupList) item;
			list.removeAll();
			if (options != null) {
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
					if (i == 0)
						entry.setSelected(true);
				}
			}
		}
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public int getFractionDigits() {
		return fractionDigits;
	}

	public void setFractionDigits(int fractionDigits) {
		this.fractionDigits = fractionDigits;
		if (item instanceof TextEditItem)
			((TextEditItem) item).setFractionDigits(fractionDigits);
	}

	/**
	 * @deprecated
	 */
	public int getPrecision() {
		return getFractionDigits();
	}

	/**
	 * @deprecated
	 */
	public void setPrecision(int precision) {
		setFractionDigits(precision);
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

	public Callable getOnClick() {
		return onChange;
	}

	public void setOnClick(Callable onClick) {
		this.onChange = onClick;
	}

	public TextUnits getUnits() {
		return units;
	}

	public void setUnits(TextUnits units) {
		if (units == null)
			units = TextUnits.NONE;
		this.units = units;
		if (item instanceof TextEditItem) {
			TextEditItem textItem = (TextEditItem) item;
			textItem.setUnits(units);
			textItem.setShowUnits(units != TextUnits.NONE);
		}
	}

	public boolean isSteppers() {
		return steppers;
	}

	public void setSteppers(boolean steppers) {
		this.steppers = steppers;
		// We cannot and do not support to change this at runtime
	}

	protected void onChange() {
		String name = getName();
		Object value = getValue();
		// First call onChange on Palette, so values get updated
		if (item.dialog instanceof Palette) {
			Palette palette = (Palette) item.dialog;
			palette.onChange(this, name, value);
		}
		// And now call onChange on the item. values will contain the same
		// new value now too.
		if (onChange != null)
			ScriptographerEngine.invoke(onChange, this, value);
	}

	protected Item createItem(Dialog dialog, Border margin) {
		// Item:
		item = null;
		switch (type) {
		case LABEL:
			break;
		case SLIDER:
			item = new Slider(dialog) {
				protected void onChange() throws Exception {
					super.onChange();
					PaletteComponent.this.onChange();
				}
			};
			break;
		case CHECKBOX:
			item = new CheckBox(dialog) {
				protected void onClick() throws Exception {
					super.onClick();
					PaletteComponent.this.onChange();
				}
			};
			break;
		case LIST:
			item = new PopupList(dialog) {
				protected void onChange() throws Exception {
					super.onChange();
					PaletteComponent.this.onChange();
				}
			};
			break;
		case BUTTON:
			item = new Button(dialog) {
				protected void onClick() throws Exception {
					super.onClick();
					PaletteComponent.this.onChange();
				}
			};
			break;
		case BUTTONS:
			break;
		case COLOR:
			item = new ColorButton(dialog) {
				protected void onClick() throws Exception {
					super.onClick();
					PaletteComponent.this.onChange();
				}
			};
			break;
		case FONT:
			item = new FontPopupList(dialog, new FontPopupListOption[] {
					FontPopupListOption.EDITABLE,
					FontPopupListOption.VERTICAL
			}) {
				protected void onChange() throws Exception {
					PaletteComponent.this.onChange();
				}
			};
			break;
		case NUMBER:
			if (steppers) {
				item = new SpinEdit(dialog) {
					protected void onChange() throws Exception {
						super.onChange();
						PaletteComponent.this.onChange();
					}
				};
				break;
			}
			// No break, as we're moving on to default
		default:
			TextOption[] options = type == PaletteComponentType.TEXT
					? new TextOption[] { TextOption.MULTILINE }
					: null;
			TextEditItem textItem = new TextEdit(dialog, options) {
				protected void onChange() throws Exception {
					PaletteComponent.this.onChange();
				}
			};
			item = textItem;
			if (type == PaletteComponentType.NUMBER) {
				textItem.setAllowMath(true);
				textItem.setAllowUnits(true);
			}
		}

		// Now set all the values again, so the item reflects them:
		setOptions(options);
		setValue(defaultValue);
		if (hasRange())
			setRange(min, max);
		if (increment != 0)
			setIncrement(increment);
		setUnits(units);
		setFractionDigits(fractionDigits);
		
		// Margin needs to be defined before setting size, since getBestSize is
		// affected by margin
		item.setMargin(margin);
		updateSize();

		return item;
	}

	protected void updateSize() {
		if (item != null) {
			Size size;
			if (type == PaletteComponentType.TEXT) {
				// Base width on an average wide character, such as H
				size = item.getTextSize("H");
				size = new Size(
						size.width * columns + 8,
						size.height * rows + 8
				);
			} else {
				size = item.getBestSize();
				if (length != -1)
					size.width = item.getTextSize("H").width * length;
			}
			if (width >= 0)
				size.width = width;
			if (height >= 0)
				size.height = height;
			item.setSize(size);
		}
	}

	private static PaletteComponent getItem(Map<String, Object> map, String name,
			Object value) {
		if (map != null) {
			try {
				ArgumentReader reader =
						ScriptEngine.convertToArgumentReader(map);
				if (name != null || value != null) {
					// We need to create a new map that contains all the values
					// from map, but also name and value, so the ArgumentReader
					// constructor can read these from there. Just clone it.
					Map<String, Object> clone =
							new HashMap<String, Object>(map);
					if (name != null)
						clone.put("name", name);
					if (value != null)
						clone.put("value", value);
					// Make a new ArgumentReader that inherits its converter
					// from the current reader, which was returned by
					// ScriptEngine.convertToArgumentReader.
					// So for example if map actually is a NativeObject, our new
					// reader will inherit all converter functionality from it.
					reader = new MapArgumentReader(reader, clone);
				}
				return new PaletteComponent(reader);
			} catch (IllegalArgumentException e) {
			}
		}
		return null;
	}

	protected static PaletteComponent[] getItems(Map<String, Object>[] items) {
		PaletteComponent[] promptItems = new PaletteComponent[items.length];
		for (int i = 0; i < items.length; i++)
			promptItems[i] = getItem(items[i], null, null);
		return promptItems;
	}

	protected static PaletteComponent[] getItems(
			Map<String, Map<String, Object>> items, Map<String, Object> values) {
		ArrayList<PaletteComponent> promptItems = new ArrayList<PaletteComponent>();
		for (Map.Entry<String, Map<String, Object>> entry : items.entrySet()) {
			String name = entry.getKey();
			Map<String, Object> map = entry.getValue();
			Object value = values != null ? values.get(entry.getKey()) : null;
			PaletteComponent item = getItem(map, name, value);
			if (item != null)
				promptItems.add(item);
		}
		return promptItems.toArray(new PaletteComponent[promptItems.size()]);
	}
}