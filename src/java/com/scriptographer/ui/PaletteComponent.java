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
import java.util.LinkedHashMap;
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
import com.scriptographer.ai.FontWeight;

/**
 * @author lehni
 */
public class PaletteComponent implements ChangeReceiver {

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
	private Float min;
	private Float max;
	private Float increment;
	private Integer fractionDigits;
	private Item item;
	private Callable onChange;
	private TextUnits units;
	private Boolean steppers;

	/**
	 * @jshide
	 */
	public PaletteComponent(ArgumentReader reader)
			throws IllegalArgumentException {
		if (reader.isMap()) {
			type = reader.readEnum("type", PaletteComponentType.class);
			defaultValue = reader.readObject("value");
			if (type == null) {
				// Determine type form options and value
				if (reader.has("options"))
					type = PaletteComponentType.LIST;
				else if (defaultValue instanceof Number)
					type = PaletteComponentType.NUMBER;
				else if (defaultValue instanceof Boolean)
					type = PaletteComponentType.CHECKBOX;
				else if (defaultValue instanceof String) 
					type = PaletteComponentType.STRING;
			}
			if (type != null) {
				// Call setMultiline to set default value for length
				setMultiline(false);
				// Tell the framework to set the properties from the map
				// on the object after creating through ArgumentReader.
				reader.setProperties(this);
			}
		}
		if (type == null)
			throw new IllegalArgumentException();
	}

	/**
	 * @jshide
	 */
	public PaletteComponent(PaletteComponentType type, String label,
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
	public PaletteComponent(String description, String value) {
		this(PaletteComponentType.STRING, description, value);
	}

	/**
	 * Creates a NUMBER Item
	 * 
	 * @jshide
	 */
	public PaletteComponent(String description, Number value) {
		this(PaletteComponentType.NUMBER, description, value);
	}

	/**
	 * @jshide
	 */
	public PaletteComponent(String description, float value) {
		this(PaletteComponentType.NUMBER, description, new Float(value));
	}

	/**
	 * Creates a BOOLEAN Item
	 * 
	 * @jshide
	 */
	public PaletteComponent(String description, Boolean value) {
		this(PaletteComponentType.CHECKBOX, description, value);
	}

	/**
	 * @jshide
	 */
	public PaletteComponent(String description, boolean value) {
		this(description, new Boolean(value));
	}

	/**
	 * Creates a RANGE Item
	 * 
	 * @jshide
	 */
	public PaletteComponent(String description, Number value, float min,
			float max, float step) {
		this(PaletteComponentType.SLIDER, description, value);
		this.setRange(min, max);
		this.increment = step;
	}
	
	/**
	 * Creates a LIST Item
	 * 
	 * @jshide
	 */
	public PaletteComponent(String description, Object value, Object[] options) {
		this(PaletteComponentType.LIST, description, value);
		this.options = options;
	}
	
	// TODO: make constructors for other types


	protected Item createItem(Dialog dialog, Border margin) {
		// Item:
		item = null;
		switch (type) {
		case NUMBER:
			if (steppers != null && steppers) {
				item = new SpinEdit(dialog) {
					protected void onChange() {
						super.onChange();
						PaletteComponent.this.onChange(true);
					}
				};
			} else {
				TextEditItem textItem = new TextEdit(dialog) {
					protected void onChange() {
						PaletteComponent.this.onChange(true);
					}
				};
				textItem.setAllowMath(true);
				textItem.setAllowUnits(true);
				item = textItem;
			}
			break;
		case STRING:
			TextOption[] options = multiline != null && multiline
					? new TextOption[] { TextOption.MULTILINE }
					: null;
			item = new TextEdit(dialog, options) {
				protected void onChange() {
					PaletteComponent.this.onChange(true);
				}
			};
			break;
		case TEXT:
			item = new TextPane(dialog);
			// Space a bit more to the top, to compensate the descender space.
			margin = margin.add(new Border(1, 0, 0, 0));
			break;
		case RULER:
			Frame frame = new Frame(dialog);
			frame.setStyle(FrameStyle.SUNKEN);
			// Margin needs to be set before changing size...
			// TODO: Fix this in UI package?
			int top = label != null ? 2 : 4, bottom = 4;
			frame.setMargin(top, 0, bottom, 0);
			// Make sure we're not setting default margin later on.
			margin = null;
			// Margin is included inside size, not added. This is different
			// to how things works with CSS...
			// TODO: Fix this in UI package?
			frame.setHeight(2 + top + bottom);
			item = frame;
			break;
		case SLIDER:
			item = new Slider(dialog) {
				protected void onChange() {
					super.onChange();
					PaletteComponent.this.onChange(true);
				}
			};
			break;
		case CHECKBOX:
			item = new CheckBox(dialog) {
				protected void onClick() {
					super.onClick();
					PaletteComponent.this.onChange(true);
				}
			};
			break;
		case LIST:
			item = new PopupList(dialog, true) {
				protected void onChange() {
					super.onChange();
					selectedIndex = this.getSelectedEntry().getIndex();
					PaletteComponent.this.onChange(true);
				}
			};
			break;
		case BUTTON:
			item = new Button(dialog) {
				protected void onClick() {
					super.onClick();
					PaletteComponent.this.onChange(true);
				}
			};
			break;
		case COLOR:
			item = new ColorButton(dialog) {
				protected void onClick() {
					super.onClick();
					PaletteComponent.this.onChange(true);
				}
			};
			break;
		case FONT:
			item = new FontPopupList(dialog, new FontPopupListOption[] {
					FontPopupListOption.EDITABLE,
					FontPopupListOption.VERTICAL
			}) {
				protected void onChange() {
					PaletteComponent.this.onChange(true);
				}
			};
			break;
		}

		// Now set all the values again, so the item reflects them:
		if (!visible)
			item.setVisible(false);
		if (!enabled)
			item.setEnabled(false);
		setRange(min, max);
		setIncrement(increment);
		setUnits(units);
		setFractionDigits(fractionDigits);
		setMaxLength(maxLength);
		setOptions(options);
		setValue(defaultValue);
		
		// Margin needs to be defined before setting size, since getBestSize is
		// affected by margin
		if (margin != null)
			item.setMargin(margin);
		updateSize();

		return item;
	}

	protected void updateSize() {
		if (item != null) {
			Size size;
			if (multiline != null && multiline && columns != null && rows != null) {
				// Base width on an average wide character, such as H
				size = item.getTextSize("H");
				size = new Size(
						size.width * columns + 8,
						size.height * rows + 8
				);
			} else {
				// Use preferred size instead of best size, as we want to take
				// into account items of which the size was already set before,
				// e.g. rulers.
				size = item.getPreferredSize();
				if (length != null)
					size.width = item.getTextSize("H").width * length;
			}
			if (width != null)
				size.width = width;
			if (height != null)
				size.height = height;
			item.setSize(size);
		}
	}

	protected int addToContent(Dialog dialog,
			LinkedHashMap<String, Component> content, int column, int row) {
		Item valueItem = createItem(dialog, new Border(1, 0, 1, 0));
		String label = getLabel();
		boolean isRuler = type == PaletteComponentType.RULER;
		if (label != null && !"".equals(label)) {
			TextPane labelItem = new TextPane(dialog);
			if (isRuler) {
				// Add label above ruler in its own row.
				labelItem.setText(label);
				labelItem.setMargin(4, 0, 0, 4);
				content.put(column + ", " + row + ", " + (column + 1) + ", "
						+ row + ", left, top", labelItem);
				row++;
			} else {
				labelItem.setText(label + ":");
				// Adjust top margin of label to reflect the native margin
				// in the value item.
				Item marginItem = valueItem;
				// If this is an item group, use the first item in it instead
				// This is only needed for FontPopupList so far.
				if (marginItem instanceof ItemGroup)
					marginItem = (Item) ((ItemGroup) marginItem).getContent().get(0);
				labelItem.setMargin(marginItem.getNativeMargin().top + 4, 4, 0, 0);
				content.put(column + ", " + row + ", right, top", labelItem);
			}
		}
		String justification = isRuler || fullSize 
				? "full, center" : "left, center";
		content.put(isRuler
				? column + ", " + row  + ", " + (column + 1) + ", " + row + ", "
						+ justification
				: (column + 1) + ", " + row + ", " + justification,
				valueItem);
		return row + 1;
	}

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
				ListEntry selected = ((PopupList) item).getSelectedEntry();
				if (selected != null)
					return options[selected.getIndex()];
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
			boolean resize = false;
			switch (type) {
			case STRING:
			case TEXT:
				((TextValueItem) item).setText(ConversionUtils.toString(value));
				resize = type == PaletteComponentType.TEXT;
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
				int index = selectedIndex != null ? selectedIndex : 0;
				for (int i = 0, l = list.size(); i < l && selected == null; i++) {
					Object option = options[i];
					if (ConversionUtils.equals(value, option))
						index = i;
				}
				setSelectedIndex(index);
				// No need to call onChange, as setSelectionIndex already does so.
				return;
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
			if (resize) {
				// TODO: Make this work!
				Size size = item.getBestSize();
				item.setMinimumSize(size);
				item.setSize(size);
				item.getDialog().doLayout();
			}
			// Update palette's value object too
			onChange(false);
		}
	}

	/**
	 * @jshide
	 */
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

	/**
	 * @deprecated
	 */
	public Float getMin() {
		return min;
	}

	/**
	 * @deprecated
	 */
	public void setMin(Float min) {
		setRange(min, max);
	}

	/**
	 * @deprecated
	 */
	public Float getMax() {
		return max;
	}

	/**
	 * @deprecated
	 */
	public void setMax(Float max) {
		setRange(min, max);
	}

	public float[] getRange() {
		return hasRange() ? new float[] {
			min, max
		} : null;
	}

	public void setRange(float[] range) {
		if (range == null)
			setRange(null, null);
		else
			setRange(range[0], range[1]);
	}

	/**
	 * @jshide
	 */
	public void setRange(Float min, Float max) {
		if (type == PaletteComponentType.NUMBER
				|| type == PaletteComponentType.SLIDER) {
			this.min = min;
			this.max = max;
			if (item != null) {
				((ValueItem) item).setRange(
						min != null ? min : Integer.MIN_VALUE, 
						max != null ? max : Integer.MAX_VALUE);
			}
		}
	}

	public boolean hasRange() {
		return min != null && max != null;
	}


	public Float getIncrement() {
		return increment;
		
	}

	public void setIncrement(Float increment) {
		if (type == PaletteComponentType.NUMBER
				|| type == PaletteComponentType.SLIDER) {
			if (increment == null)
				increment = 0f;
			this.increment = increment;
			if (item != null)
				((ValueItem) item).setIncrements(increment);
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
			if (item != null)
				((TextEditItem) item).setFractionDigits(fractionDigits);
		}
	}

	/**
	 * @deprecated
	 */
	public Integer getPrecision() {
		return getFractionDigits();
	}

	/**
	 * @deprecated
	 */
	public void setPrecision(Integer precision) {
		setFractionDigits(precision);
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public TextUnits getUnits() {
		return units;
	}

	public void setUnits(TextUnits units) {
		if (type == PaletteComponentType.NUMBER) {
			if (units == null)
				units = TextUnits.NONE;
			this.units = units;
			if (item != null) {
				TextEditItem textItem = (TextEditItem) item;
				textItem.setUnits(units);
				textItem.setShowUnits(units != TextUnits.NONE);
			}
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
		if (item != null)
			item.setVisible(visible);
	}

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (item != null)
			item.setEnabled(enabled);
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
			if (item != null) {
				PopupList list = (PopupList) item;
				list.removeAll();
				if (options != null && options.length > 0) {
					int index = selectedIndex != null ? selectedIndex : 0;
					for (int i = 0; i < options.length; i++) {
						Object option = options[i];
						if (option.equals(current))
							index = i;
						ListEntry entry = null;
						if (option instanceof ListEntry) {
							entry = (ListEntry) option;
							entry = list.add(entry);
						} else {
							entry = new ListEntry(list);
							entry.setText(option.toString());
						}
					}
					if (index < 0)
						index = 0;
					else if (index >= options.length)
						index = options.length - 1;
					setSelectedIndex(index);
				}
			}
		}
	}

	public Integer getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelectedIndex(Integer index) {
		if (type == PaletteComponentType.LIST && index != null && index >= 0
				&& (options == null || index < options.length)) {
			selectedIndex = index;
			if (item != null) {
				PopupList list = (PopupList) item;
				list.setSelectedEntry(list.get(index));
				onChange(false);
			}
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
			if (item != null)
				((TextEditItem) item).setMaxLength(maxLength != null ? maxLength : -1);
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

	protected void onChange(boolean callback) {
		String name = getName();
		Object value = getValue();
		// First call onChange on Palette, so values get updated
		Dialog dialog = item.getDialog();
		if (dialog instanceof Palette) {
			Palette palette = (Palette) dialog;
			palette.onChange(this, name, value, callback);
		}
		// And now call onChange on the item. values will contain the same
		// new value now too.
		if (callback && onChange != null)
			ScriptographerEngine.invoke(onChange, this, value);
	}

	@SuppressWarnings("unchecked")
	private static PaletteComponent getItem(Object object, String name,
			Object value) {
		if (object != null) {
			if (object instanceof PaletteComponent) {
				return (PaletteComponent) object;
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
					return new PaletteComponent(reader);
				} catch (IllegalArgumentException e) {
				}
			}
		}
		return null;
	}

	protected static PaletteComponent[] getComponents(Map<String,
			Object>[] components) {
		PaletteComponent[] promptItems = new PaletteComponent[components.length];
		for (int i = 0; i < components.length; i++)
			promptItems[i] = getItem(components[i], null, null);
		return promptItems;
	}

	protected static PaletteComponent[] getComponents(
			Map<String, Object> components, Map<String, Object> values) {
		ArrayList<PaletteComponent> promptItems = new ArrayList<PaletteComponent>();
		for (Map.Entry<String, Object> entry : components.entrySet()) {
			String name = entry.getKey();
			Object map = entry.getValue();
			Object value = values != null ? values.get(entry.getKey()) : null;
			PaletteComponent component = getItem(map, name, value);
			if (component != null)
				promptItems.add(component);
		}
		return promptItems.toArray(new PaletteComponent[promptItems.size()]);
	}
}