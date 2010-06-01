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
 */

package com.scriptographer.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.scratchdisk.list.ExtendedArrayList;
import com.scratchdisk.list.Lists;
import com.scratchdisk.list.ReadOnlyList;
import com.scratchdisk.script.ArgumentReader;
import com.scratchdisk.script.Callable;
import com.scratchdisk.script.ChangeEmitter;
import com.scratchdisk.script.ChangeReceiver;
import com.scratchdisk.script.MapArgumentReader;
import com.scratchdisk.script.ScriptEngine;
import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 */
public class Component implements ChangeReceiver {

	private String label;
	private String name; // for preferences
	private PaletteComponentType type;
	private Object defaultValue;
	private Object options[];
	private Integer selectedIndex;
	private boolean visible = true;
	private boolean enabled = true;
	private boolean fullSize = false;
	private Integer width;
	private Integer height;
	private Integer length;
	private Integer maxLength;
	private Boolean multiline;
	private Integer rows;
	private Integer columns;
	private Double min;
	private Double max;
	private Double increment;
	private Integer fractionDigits;
	private TextUnits units;
	private Boolean steppers;

	// Used for scaling slider values
	// TODO: Move factor to ADM
	private double factor = 1;

	/**
	 * @jshide
	 */
	public Component(ArgumentReader reader)
			throws IllegalArgumentException {
		if (reader.isMap()) {
			type = reader.readEnum("type", PaletteComponentType.class);
			defaultValue = reader.readObject("value");
			if (type == null) {
				// Determine type form options and value
				if (reader.has("options"))
					type = PaletteComponentType.LIST;
				else if (reader.has("onClick"))
					type = PaletteComponentType.BUTTON;
				else if (reader.has("onSelect"))
					type = PaletteComponentType.MENU_ENTRY;
				else if (defaultValue instanceof Number)
					type = PaletteComponentType.NUMBER;
				else if (defaultValue instanceof Boolean)
					type = PaletteComponentType.CHECKBOX;
				else if (defaultValue instanceof String) 
					type = PaletteComponentType.STRING;
			}
			if (type != null) {
				// Set scaling factor for Slider to allow fractional digits
				// TODO: Move factor to ADM
				if (type == PaletteComponentType.SLIDER)
					factor = 1000;
				// Call setMultiline to set default value for length
				setMultiline(false);
				// Tell the framework to set the properties from the map
				// on the object after creating through ArgumentReader
				reader.setProperties(this);
				// Turn on steppers for number components with units by
				// default
				if (steppers == null && type == PaletteComponentType.NUMBER
						&& units != null && units != TextUnits.NONE)
					setSteppers(true);
			}
		}
		if (type == null)
			throw new IllegalArgumentException();
	}

	/**
	 * @jshide
	 */
	public Component(PaletteComponentType type, String label,
			Object value) {
		this.label = label;
		this.type = type;
		this.defaultValue = value;
	}

	/**
	 * Creates a STRING Item
	 * 
	 * @jshide
	 */
	public Component(String description, String value) {
		this(PaletteComponentType.STRING, description, value);
	}

	/**
	 * Creates a NUMBER Item
	 * 
	 * @jshide
	 */
	public Component(String description, double value) {
		this(PaletteComponentType.NUMBER, description, value);
	}

	/**
	 * Creates a BOOLEAN Item
	 * 
	 * @jshide
	 */
	public Component(String description, boolean value) {
		this(PaletteComponentType.CHECKBOX, description, value);
	}

	/**
	 * Creates a RANGE Item
	 * 
	 * @jshide
	 */
	public Component(String description, Number value, double min,
			double max, double step) {
		this(PaletteComponentType.SLIDER, description, value);
		this.setRange(min, max);
		this.increment = step;
	}
	
	/**
	 * Creates a LIST Item
	 * 
	 * @jshide
	 */
	public Component(String description, Object value, Object[] options) {
		this(PaletteComponentType.LIST, description, value);
		this.options = options;
	}
	
	// TODO: make constructors for other types

	public void reset() {
		setValue(defaultValue);
	}

	protected void updateSize() {
		// TODO: Implement and call palette.onLayoutChanged() if size changes
	}

	/*
	 * Beans
	 */

	public PaletteComponentType getType() {
		return type;
	}

	public Object getValue() {
		// TODO: Implement
		return null;
	}

	public void setValue(Object value) {
		// TODO: Implement
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public double[] getRange() {
		return hasRange() ? new double[] {
			min, max
		} : null;
	}

	public void setRange(double[] range) {
		if (range == null)
			setRange(null, null);
		else
			setRange(range[0], range[1]);
	}

	/**
	 * @jshide
	 */
	public void setRange(Double min, Double max) {
		if (type == PaletteComponentType.NUMBER
				|| type == PaletteComponentType.SLIDER) {
			this.min = min;
			this.max = max;
			// TODO: Implement
		}
	}

	public boolean hasRange() {
		return min != null && max != null;
	}

	public Double getMin() {
		return min;
	}

	public void setMin(Double min) {
		setRange(min, max);
	}

	public Double getMax() {
		return max;
	}

	public void setMax(Double max) {
		setRange(min, max);
	}

	public Double getIncrement() {
		if (type == PaletteComponentType.NUMBER
				|| type == PaletteComponentType.SLIDER) {
			// If no increment is defined, calculate a default value, based on
			// the defined range.
			if (increment == null) {
				if (min != null && max != null) {
					double range = max - min;
					if (range == 0)
						range = 1.0;
					int numDigits = Math.max(0, 2 - (int) Math.ceil(Math.log10(range)));
					double inc = 1.0 / Math.pow(10, numDigits);
					return inc > 1.0 ? 1.0 : inc;
				} else {
					return 1.0;
				}
			}
			return increment;
		}
		return null;
	}

	public void setIncrement(Double increment) {
		if (type == PaletteComponentType.NUMBER
				|| type == PaletteComponentType.SLIDER) {
			this.increment = increment;
			// TODO: Implement
		}
	}

	public Integer getFractionDigits() {
		return fractionDigits;
	}

	public void setFractionDigits(Integer fractionDigits) {
		if (type == PaletteComponentType.NUMBER) {
			if (fractionDigits == null)
				fractionDigits = 3;
			this.fractionDigits = fractionDigits;
			// TODO: Implement
		}
	}
	
	public TextUnits getUnits() {
		return units;
	}

	public void setUnits(TextUnits units) {
		if (type == PaletteComponentType.NUMBER) {
			if (units == null)
				units = TextUnits.NONE;
			this.units = units;
			// TODO: Implement
		}
	}

	public Boolean getSteppers() {
		return steppers;
	}

	public void setSteppers(Boolean steppers) {
		// No support to change this at runtime for now
		if (type == PaletteComponentType.NUMBER)
			this.steppers = steppers;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
		// TODO: Implement
	}

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		// TODO: Implement
	}

	private static class OptionList extends ExtendedArrayList<Object> implements
			ChangeEmitter {

		public OptionList(Object[] options) {
			super(options);
		}
	}

	public com.scratchdisk.list.List<Object> getOptions() {
		return new OptionList(options);
	}

	public void setOptions(ReadOnlyList<Object> options) {
		setOptions(options != null ? Lists.toArray(options) : new Object[0]);
	}

	public void setOptions(Object[] options) {
		if (type == PaletteComponentType.LIST) {
			// Retrieve current value before setting new options,
			// as options is used inside getValue().
			Object current = getValue();
			this.options = options;
			// TODO: Implement
		}
	}

	public Integer getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelectedIndex(Integer index) {
		// We're changing index, not value, so cause onChange callback for value
		setSelectedIndex(index, true);
	}

	protected void setSelectedIndex(Integer index, boolean callback) {
		if (type == PaletteComponentType.LIST && index != null && index >= 0
				&& (options == null || index < options.length)) {
			selectedIndex = index;
			// TODO: Implement
		}
	}

	public boolean getFullSize() {
		return fullSize;
	}

	public void setFullSize(boolean fit) {
		this.fullSize = fit;
		if (fit) {
			width = null;
			length = null;
			columns = null;
		}
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
		// Clear columns and length when setting width and vice versa.
		fullSize = false;
		columns = null;
		length = null;
		updateSize();
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
		// Clear rows when setting height and vice versa.
		rows = null;
		updateSize();
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		if (type == PaletteComponentType.STRING ||
				type == PaletteComponentType.NUMBER) {
			this.multiline = false;
			this.length = length;
			// Clear width and columns when setting length and vice versa.
			fullSize = false;
			width = null;
			columns = null;
			updateSize();
		}
	}

	
	public Integer getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(Integer maxLength) {
		if (type == PaletteComponentType.STRING ||
				type == PaletteComponentType.NUMBER) {
			this.maxLength = maxLength;
			// TODO: Implement
		}
	}

	public Boolean getMultiline() {
		return multiline;
	}

	public void setMultiline(Boolean multiline) {
		if (type == PaletteComponentType.STRING ||
				type == PaletteComponentType.NUMBER) {
			multiline = multiline && type == PaletteComponentType.STRING;
			// Set default values for length / columns, rows
			if (multiline) {
				if (rows == null)
					rows = 6;
				if (columns == null)
					columns = 32;
				length = null;
			} else {
				if (length == null)
					length = type == PaletteComponentType.STRING ? 16 : 8;
				columns = null;
				rows = null;
			}
			this.multiline = multiline;
		}
	}

	public Integer getColumns() {
		return columns;
	}

	public void setColumns(Integer columns) {
		if (type == PaletteComponentType.STRING) {
			this.multiline = true;
			this.columns = columns;
			// Clear width and length when setting columns and vice versa.
			fullSize = false;
			width = null;
			length = null;
			updateSize();
		}
	}

	public Integer getRows() {
		return rows;
	}

	public void setRows(Integer rows) {
		if (type == PaletteComponentType.STRING) {
			this.multiline = true;
			this.rows = rows;
			// Clear height when setting rows and vice versa.
			height = null;
			updateSize();
		}
	}

	private Callable onChange;

	public Callable getOnChange() {
		return onChange;
	}

	public void setOnChange(Callable onChange) {
		this.onChange = onChange;
	}

	protected void onChange(boolean callback) {
		// TODO: Implement
	}

	private Callable onClick;

	public Callable getOnClick() {
		return onClick;
	}

	public void setOnClick(Callable onClick) {
		this.onClick = onClick;
	}

	protected void onClick() {
		if (onClick != null)
			ScriptographerEngine.invoke(onClick, this);
	}

	private Callable onSelect;

	public Callable getOnSelect() {
		return onSelect;
	}

	public void setOnSelect(Callable onSelect) {
		this.onSelect = onSelect;
	}

	protected void onSelect() {
		if (onSelect != null)
			ScriptographerEngine.invoke(onSelect, this);
	}

	@SuppressWarnings("unchecked")
	private static Component getItem(Object object, String name,
			Object value) {
		if (object != null) {
			if (object instanceof Component) {
				return (Component) object;
			} else if (object instanceof Map) {
				try {
					ArgumentReader reader =
							ScriptEngine.convertToArgumentReader(object);
					if (name != null || value != null) {
						// We need to create a new map that contains all the
						// values from map, but also name and value, so the
						// ArgumentReader constructor can read these from there.
						// Just clone it.
						Map<Object, Object> clone =
								new HashMap<Object, Object>((Map) object);
						if (name != null)
							clone.put("name", name);
						if (value != null)
							clone.put("value", value);
						// Make a new ArgumentReader that inherits its converter
						// from the current reader, which was returned by
						// ScriptEngine.convertToArgumentReader. So for example
						// if map actually is a NativeObject, our new reader will
						// inherit all converter functionality from it.
						reader = new MapArgumentReader(reader, clone);
					}
					return new Component(reader);
				} catch (IllegalArgumentException e) {
				}
			}
		}
		return null;
	}

	protected static Component[] getComponents(Map<String,
			Object>[] components) {
		Component[] promptItems = new Component[components.length];
		for (int i = 0; i < components.length; i++)
			promptItems[i] = getItem(components[i], null, null);
		return promptItems;
	}

	protected static Component[] getComponents(
			Map<String, Object> components, Map<String, Object> values) {
		ArrayList<Component> promptItems =
				new ArrayList<Component>();
		for (Map.Entry<String, Object> entry : components.entrySet()) {
			String name = entry.getKey();
			Object map = entry.getValue();
			Object value = values != null ? values.get(entry.getKey()) : null;
			Component component = getItem(map, name, value);
			if (component != null)
				promptItems.add(component);
		}
		return promptItems.toArray(new Component[promptItems.size()]);
	}
}