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
import com.scratchdisk.util.ConversionUtils;
import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 */
public class Component implements ChangeReceiver {

	private String label;
	private String name; // for preferences
	private ComponentType type;
	private Object defaultValue;
	private Object options[];
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

	protected ComponentProxy proxy;
	protected Palette palette;
	private boolean initialized;

	/**
	 * @jshide
	 */
	public Component(ArgumentReader reader)
			throws IllegalArgumentException {
		if (reader.isMap()) {
			type = reader.readEnum("type", ComponentType.class);
			defaultValue = reader.readObject("value");
			if (type == null) {
				// Determine type form options and value
				if (reader.has("options"))
					type = ComponentType.LIST;
				else if (reader.has("onClick"))
					type = ComponentType.BUTTON;
				else if (reader.has("onSelect"))
					type = ComponentType.MENU_ENTRY;
				else if (defaultValue instanceof Number)
					type = ComponentType.NUMBER;
				else if (defaultValue instanceof Boolean)
					type = ComponentType.CHECKBOX;
				else if (defaultValue instanceof String) 
					type = ComponentType.STRING;
			}
			if (type != null) {
				setType(type);
				// Call setMultiline to set default value for length
				setMultiline(false);
				// Tell the framework to set the properties from the map on the
				// object after creating through ArgumentReader
				reader.setProperties(this);
				// Turn on steppers for number components with units by default
				if (steppers == null && type == ComponentType.NUMBER
						&& units != null && units != TextUnits.NONE)
					setSteppers(true);
			}
		}
		if (type == null)
			throw new IllegalArgumentException();
	}

	protected void initialize() {
		// Now set all the values again, so the item reflects them:
		if (!visible)
			proxy.setVisible(false);
		if (!enabled)
			proxy.setEnabled(false);
		// Calculate a default range for sliders of none was defined
		if (min == null && max == null && type == ComponentType.SLIDER) {
			min = 0d;
			max = defaultValue == null ? 1d
					: ConversionUtils.toDouble(defaultValue);
		}
		// Set these values not straight through proxy but through the public
		// methods here, since they perform type filtering for us.
		setRange(min, max);
		setFractionDigits(fractionDigits);
		// Setting range internally updates increments, so no need to set it
		// again here.
		setMaxLength(maxLength);
		setOptions(options);
		setValue(defaultValue);
		setUnits(units);
		// Now update the size of the item.
		updateSize();
		initialized = true;
	}

	/**
	 * @jshide
	 */
	public Component(ComponentType type, String label,
			Object value) {
		this.label = label;
		setType(type);
		this.defaultValue = value;
	}

	/**
	 * Creates a STRING Item
	 * 
	 * @jshide
	 */
	public Component(String description, String value) {
		this(ComponentType.STRING, description, value);
	}

	/**
	 * Creates a NUMBER Item
	 * 
	 * @jshide
	 */
	public Component(String description, double value) {
		this(ComponentType.NUMBER, description, value);
	}

	/**
	 * Creates a BOOLEAN Item
	 * 
	 * @jshide
	 */
	public Component(String description, boolean value) {
		this(ComponentType.CHECKBOX, description, value);
	}

	/**
	 * Creates a RANGE Item
	 * 
	 * @jshide
	 */
	public Component(String description, Number value, double min,
			double max, double step) {
		this(ComponentType.SLIDER, description, value);
		this.setRange(min, max);
		this.increment = step;
	}
	
	/**
	 * Creates a LIST Item
	 * 
	 * @jshide
	 */
	public Component(String description, Object value, Object[] options) {
		this(ComponentType.LIST, description, value);
		this.options = options;
	}
	
	// TODO: make constructors for other types

	/**
	 * Resets the value of the component to {@link #defaultValue}.
	 */
	public void reset() {
		setValue(defaultValue);
	}

	protected void updateSize() {
		if (proxy != null)
			proxy.updateSize();
	}

	protected void onSizeChanged() {
		if (initialized && palette != null)
			palette.onSizeChanged();
	}

	/*
	 * Beans
	 */

	/**
	 * The component type.
	 */
	public ComponentType getType() {
		return type;
	}

	@SuppressWarnings("deprecation")
	public void setType(ComponentType type) {
		if (proxy != null)
			throw new UnsupportedOperationException(
					"The component type cannot be changed once it is created");
		// Convert legacy type RANGE to SLIDER:
		if (type == ComponentType.RANGE)
			type = ComponentType.SLIDER;
		this.type = type;
	}

	/**
	 * The value of the component.
	 */
	public Object getValue() {
		return proxy != null ? proxy.getValue() : defaultValue;
	}

	public void setValue(Object value) {
		if (proxy == null || !proxy.setValue(value))
			defaultValue = value;
	}

	/**
	 * The first {@link #getValue()} that was set.
	 * 
	 * Sample code:
	 * <code>
	 * var values = {
	 * 	number: 30
	 * };
	 * var components = { 
	 * 	number: {
	 * 		type: 'number'
	 * 	}
	 * }; 
	 * var palette = new Palette('Text', components, values);
	 * values.number = 60;
	 * print(components.number.value) // 60
	 * print(components.number.defaultValue) // 30
	 * </code>
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * The name of the component as it is referenced in
	 * {@link Palette#getComponents}.
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Text label that appears on the left hand side of the component in the
	 * palette.
	 */
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
	 * Specifies whether the component is visible.
	 * 
	 * Sample code:
	 * <code>
	 * var values = {
	 * 	number: 10
	 * };
	 * var components = {
	 * 	showNumber: {
	 * 		type: 'checkbox', label: 'Show',
	 * 		onChange: function(value) {
	 * 			components.number.visible = value;
	 * 		}
	 * 	},
	 * 	number: {
	 * 		type: 'number', label: 'Number',
	 * 		visible: false
	 * 	}
	 * };
	 * var palette = new Palette('Show / Hide', components, values);
	 * </code>
	 * @return {@true if the component is visible}
	 */
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
		if (proxy != null)
			proxy.setVisible(visible);
	}

	/**
	 * Specifies whether the component is enabled. When set to {@code false},
	 * the component is grayed out and does not allow user interaction.
	 * 
	 * Sample code:
	 * <code>
	 * var values = {
	 * 	canEdit: false,
	 * 	text: 'Can you edit me?'
	 * }
	 * var components = { 
	 * 	canEdit: {
	 * 		type: 'checkbox', label: 'Allow Editing',
	 * 		onChange: function(value) {
	 * 			components.text.enabled = value;
	 * 		}
	 * 	},
	 * 	text: { 
	 * 		type: 'string',
	 * 		enabled: false
	 * 	}
	 * }; 
	 * var palette = new Palette('Text', components, values);
	 * </code>
	 * 
	 * @return {@true if the component is enabled}
	 */
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (proxy != null)
			proxy.setEnabled(enabled);
	}

	/**
	 * {@grouptitle Size}
	 * 
	 * When set to {@code true}, the component is stretched over the width of
	 * the palette. When no {@link #getLabel()} is set, it also eliminates the
	 * margin on the left hand side.
	 */
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

	/**
	 * The width of the input field in pixels.
	 */
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

	/**
	 * The height of the input field in pixels.
	 */
	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
		// Clear rows when setting height and vice versa.
		rows = null;
		updateSize();
	}
	
	/**
	 * {@grouptitle Number / Slider Properties}
	 * 
	 * The range for the numeric value as an array in the form: [min, max]. The
	 * first element in the array defines the allowed minimum amount, the second
	 * the maximum amount, both are included in the range.
	 * 
	 * Sample code:
	 * <code>
	 * var values = {
	 * 	percentage: 50,
	 * 	angle: 180
	 * };
	 * var components = { 
	 * 	percentage: { 
	 * 		type: 'slider', label: 'Percentage', 
	 * 		range: [0, 100]
	 * 	}, 
	 * 	angle: { 
	 * 		type: 'number', label: 'Angle', 
	 * 		range: [0, 360]
	 * 	}, 
	 * };
	 * 
	 * var palette = new Palette('Range Examples', components, values);
	 * </code>
	 */
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
		if (type == ComponentType.NUMBER
				|| type == ComponentType.SLIDER) {
			this.min = min;
			this.max = max;
			if (proxy != null && proxy.setRange(min, max)) {
				// Setting range sets increment again as well, as it will
				// be dynamically calculated based on range in case it was
				// not set on a fixed value.
				setIncrement(increment);
			}
		}
	}

	/**
	 * @jshide
	 */
	public boolean hasRange() {
		return min != null && max != null;
	}

	/**
	 * The minimum amount allowed.
	 * 
	 * Sample code:
	 * <code>
	 * var values = {
	 * 	size: 5
	 * };
	 * var components = {
	 * 	size: {
	 * 		type: 'number', label: 'Size',
	 * 		min: 0, steppers: true
	 * 	}
	 * };
	 * var palette = new Palette('Minimum Value', components, values);
	 * </code>
	 */
	public Double getMin() {
		return min;
	}

	public void setMin(Double min) {
		setRange(min, max);
	}

	/**
	 * The maximum amount allowed.
	 * 
	 * Sample code:
	 * <code>
	 * var values = {
	 * 	size: 5
	 * };
	 * var components = {
	 * 	size: {
	 * 		type: 'number', label: 'Size',
	 * 		max: 10, steppers: true
	 * 	}
	 * };
	 * var palette = new Palette('Maximum Value', components, values);
	 * </code>
	 */
	public Double getMax() {
		return max;
	}

	public void setMax(Double max) {
		setRange(min, max);
	}

	/**
	 * The amount the steppers increase / decrease the value. Even when steppers
	 * are not activated, the user can still use the up/down arrow keys to step
	 * by the amount defined by increment.
	 * 
	 * Sample code:
	 * <code>
	 * var values = {
	 * 	percentage: 50
	 * };
	 * var components = {
	 * 	percentage: {
	 * 		type: 'number',
	 * 		range: [0, 100],
	 * 		steppers: true, increment: 10
	 * 	}
	 * };
	 * var palette = new Palette('Increment', components, values);
	 * </code>
	 */
	public Double getIncrement() {
		if (type == ComponentType.NUMBER
				|| type == ComponentType.SLIDER) {
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
		if (type == ComponentType.NUMBER
				|| type == ComponentType.SLIDER) {
			this.increment = increment;
			// If no increment is defined, use a default value, as calculated
			// by getIncrement.
			if (proxy != null)
				proxy.setIncrement(getIncrement());
		}
	}

	/**
	 * The amount of digits after the decimal point. If finer grained values are
	 * entered, they are rounded to the next allowed number. The default is 3.
	 */
	public Integer getFractionDigits() {
		return fractionDigits;
	}

	public void setFractionDigits(Integer fractionDigits) {
		if (type == ComponentType.NUMBER) {
			if (fractionDigits == null)
				fractionDigits = 3;
			this.fractionDigits = fractionDigits;
			if (proxy != null)
				proxy.setFractionDigits(fractionDigits);
		}
	}
	
	/**
	 * The units to be displayed behind the value.
	 * 
	 * Sample code:
	 * <code>
	 * var values = {
	 * 	width: 10,
	 * 	percentage: 50,
	 * 	angle: 180
	 * }
	 * 
	 * var components = {
	 * 	width: {
	 * 		type: 'number', label: 'Width',
	 * 		units: 'point'
	 * 	},
	 * 	percentage: {
	 * 		type: 'number', label: 'Percentage',
	 * 		units: 'percent'
	 * 	},
	 * 	angle: {
	 * 		type: 'number', label: 'Angle',
	 * 		units: 'degree'
	 * 	}
	 * }
	 * 
	 * var palette = new Palette('Units Examples', components, values);
	 * </code>
	 */
	public TextUnits getUnits() {
		return units;
	}

	public void setUnits(TextUnits units) {
		if (type == ComponentType.NUMBER) {
			if (units == null)
				units = TextUnits.NONE;
			this.units = units;
			if (proxy != null)
				proxy.setUnits(units);
		}
	}

	/**
	 * Activates little stepping arrows on the side of the input field when set
	 * to true.
	 */
	public Boolean getSteppers() {
		return steppers;
	}

	public void setSteppers(Boolean steppers) {
		// No support to change this at runtime for now
		if (type == ComponentType.NUMBER) {
			this.steppers = steppers;
			if (proxy != null)
				proxy.setSteppers(steppers);
		}
	}

	/**
	 * OptionList is a normal ExtendedArrayList that implements ChangeEmitter so
	 * changes on it get propagated to the Palette object which is a
	 * ChangeReceiver automatically through mechanisms in the scripting engine.
	 */
	private static class OptionList extends ExtendedArrayList<Object> implements
			ChangeEmitter {

		public OptionList(Object[] options) {
			super(options);
		}
	}

	/**
	 * {@grouptitle List Properties}
	 * 
	 * The options that the user can choose from in the list component.
	 * 
	 * Sample code:
	 * <code>
	 * var values = {
	 * 	fruit: 'Orange'
	 * };
	 * var components = { 
	 * 	fruit: { 
	 * 		type: 'list', label: 'Fruit',
	 * 		options: ['Orange', 'Apple', 'Banana', 'Kiwi']
	 * 	} 
	 * };
	 * var palette = new Palette('List Example', components, values);
	 * </code>
	 */
	public com.scratchdisk.list.List<Object> getOptions() {
		return new OptionList(options);
	}

	public void setOptions(ReadOnlyList<Object> options) {
		setOptions(options != null ? Lists.toArray(options) : new Object[0]);
	}

	public void setOptions(Object[] options) {
		if (type == ComponentType.LIST) {
			// Retrieve current value before setting new options,
			// as options is used inside getValue().
			Object current = getValue();
			this.options = options;
			if (proxy != null)
				proxy.setOptions(options, current);
		}
	}

	/**
	 * @jshide
	 */
	public Object getOption(int index) {
		if (options != null && index >= 0 && index < options.length)
			return options[index];
		return null;
	}

	/**
	 * The index of the selected value in the {@link #getOptions} array.
	 * 
	 * Sample code:
	 * <code>
	 * var values = {
	 * 	fruit: 'Apple'
	 * };
	 * var components = { 
	 * 	fruit: { 
	 * 		type: 'list', label: 'Fruit',
	 * 		options: ['Orange', 'Apple', 'Banana', 'Kiwi']
	 * 	} 
	 * };
	 * var palette = new Palette('List Example', components, values);
	 * print(components.fruit.selectedIndex) // 1
	 * </code>
	 */
	public Integer getSelectedIndex() {
		return proxy != null ? proxy.getSelectedIndex() : null;
	}

	public void setSelectedIndex(Integer index) {
		// We're changing index, not value, so cause onChange callback for value
		setSelectedIndex(index, true);
	}

	protected void setSelectedIndex(Integer index, boolean callback) {
		if (type == ComponentType.LIST && index != null && index >= 0
				&& (options == null || index < options.length)) {
			if (proxy != null && proxy.setSelectedIndex(index, callback))
				onChange(callback);
		}
	}

	/**
	 * {@grouptitle Text and String Properties}
	 * 
	 * The width of the text field in average amount of characters.
	 */
	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		if (type == ComponentType.STRING ||
				type == ComponentType.NUMBER) {
			this.multiline = false;
			this.length = length;
			// Clear width and columns when setting length and vice versa.
			fullSize = false;
			width = null;
			columns = null;
			updateSize();
		}
	}

	/**
	 * The maximum amount of characters that the text field may contain.
	 * 
	 * Sample code:
	 * <code>
	 * var values = {
	 * 	name: ''
	 * };
	 * var components = { 
	 * 	name: { 
	 * 		type: 'string', label: 'Name',
	 * 		editable: true, maxLength: 3
	 * 	} 
	 * };
	 * var palette = new Palette('Max Length', components, values);
	 * values.name = '123456';
	 * print(values.name) // '123'
	 * </code>
	 */
	public Integer getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(Integer maxLength) {
		if (type == ComponentType.STRING ||
				type == ComponentType.NUMBER) {
			this.maxLength = maxLength;
			if (proxy != null)
				proxy.setMaxLength(maxLength);
		}
	}

	/**
	 * Specifies whether the field shows multiple lines of text.
	 * 
	 * Sample code:
	 * <code>
	 * var components = { 
	 *     text: { 
	 *         type: 'text', label: 'Text', 
	 *         value: 'This is a text\nwith multiple lines', 
	 *         multiline: true
	 *     } 
	 * }; 
	 * var palette = new Palette('Text', components);
	 * </code>
	 */
	public Boolean getMultiline() {
		return multiline;
	}

	public void setMultiline(Boolean multiline) {
		if (type == ComponentType.STRING ||
				type == ComponentType.NUMBER) {
			multiline = multiline && type == ComponentType.STRING;
			// Set default values for length / columns, rows
			if (multiline) {
				if (rows == null)
					rows = 6;
				if (columns == null)
					columns = 32;
				length = null;
			} else {
				if (length == null)
					length = type == ComponentType.STRING ? 16 : 8;
				columns = null;
				rows = null;
			}
			this.multiline = multiline;
		}
	}

	/**
	 * The average amount of characters per line visible in the text area.
	 * 
	 * Sample code:
	 * <code>
	 * var components = { 
	 *     text: { 
	 *         type: 'string',
	 *         value: 'This is a string component\nwith 6 rows and 30 columns', 
	 *         rows: 6, columns: 30
	 *     } 
	 * }; 
	 * var palette = new Palette('Text', components);
	 * </code>
	 */
	public Integer getColumns() {
		return columns;
	}

	public void setColumns(Integer columns) {
		if (type == ComponentType.STRING) {
			this.multiline = true;
			this.columns = columns;
			// Clear width and length when setting columns and vice versa.
			fullSize = false;
			width = null;
			length = null;
			updateSize();
		}
	}

	/**
	 * The amount of visible lines of text in the text area. Due to a bug in
	 * Illustrator's GUI, values below 6 cause problems with scrollbars on
	 * Macintosh. The default is 6.
	 * 
	 * Sample code:
	 * <code>
	 * var components = { 
	 *     text: { 
	 *         type: 'string',
	 *         value: 'This is a string component\nwith 6 rows and 30 columns', 
	 *         rows: 6, columns: 30
	 *     } 
	 * }; 
	 * var palette = new Palette('Text', components);
	 * </code>
	 */
	public Integer getRows() {
		return rows;
	}

	public void setRows(Integer rows) {
		if (type == ComponentType.STRING) {
			this.multiline = rows > 1;
			this.rows = rows;
			// Clear height when setting rows and vice versa.
			height = null;
			updateSize();
		}
	}

	private Callable onChange;

	/**
	 * {@grouptitle Callback handlers}
	 * 
	 * The function that is called whenever the value of the component changes.
	 * The function receives the new value as an argument.
	 * 
	 * Sample code:
	 * <code>
	 * var components = { 
	 * 	threshold: { 
	 * 		type: 'number', label: 'Distance Threshold', 
	 * 		units: 'point', 
	 * 		onChange: function(value) { 
	 * 			print('Threshold was changed to', value); 
	 * 			tool.distanceThreshold = value; 
	 * 		} 
	 * 	} 
	 * };
	 * 
	 * var palette = new Palette('title', components);
	 * </code>
	 */
	public Callable getOnChange() {
		return onChange;
	}

	public void setOnChange(Callable onChange) {
		this.onChange = onChange;
	}

	protected void onChange(boolean callback) {
		Object value = getValue();
		// First call onChange on Palette, so values get updated
		if (palette != null)
			palette.onChange(this, name, value, callback);
		// And now call onChange on the item. values will contain the same
		// new value now too.
		if (callback && onChange != null)
			ScriptographerEngine.invoke(onChange, this, value);
	}

	private Callable onClick;

	/**
	 * The function that is called when a button component is clicked.
	 * 
	 * Sample code:
	 * <code>
	 * var components = {
	 * 	button: { 
	 * 		type: 'button',
	 * 		value:'Click Me', label: 'Button',
	 * 		onClick: function() {
	 * 			print('You clicked me!');
	 * 		}
	 * 	}
	 * };
	 * var palette = new Palette('Button Component', components);
	 * </code>
	 */
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

	/**
	 * The function that is called when a popup menu entry is selected.
	 * 
	 * Sample code:
	 * <code>
	 * var components = {
	 * 	menuEntry: { 
	 * 		type: 'menu-entry',
	 * 		value:'Select Me',
	 * 		onSelect: function() {
	 * 			print('You selected me!');
	 * 		}
	 * 	}
	 * };
	 * var values = new Palette('Menu Entry', components);
	 * </code>
	 */
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

	/**
	 * @jshide
	 */
	public static Component[] getComponents(
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