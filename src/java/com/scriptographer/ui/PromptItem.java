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
 * File created on 23.10.2005.
 * 
 * $Id:PromptItem.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer.ui;

import com.scratchdisk.util.ConversionUtils;

/**
 * @author lehni
 * 
 * @jshide
 */
public class PromptItem {
	
	private String description;
	private String name; // for preferences
	private PromptItemType type;
	private Object value;
	private Object values[];
	private float min;
	private float max;
	private float increment;
	private int precision;
	private Item item;
	private int width;
	
	public PromptItem(PromptItemType type, String description, Object value) {
		this.description = description;
		this.type = type;
		this.value = value;
		this.values = null;
		this.width = -1;
		this.setRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
		this.increment = 0;
		this.precision = 3;
	}

	/**
	 * Creates a STRING Item
	 */
	public PromptItem(String description, String value) {
		this(PromptItemType.STRING, description, value);
	}

	/**
	 * Creates a NUMBER Item
	 */
	public PromptItem(String description, Number value) {
		this(PromptItemType.NUMBER, description, value);
	}

	public PromptItem(String description, float value) {
		this(PromptItemType.NUMBER, description, new Float(value));
	}

	/**
	 * Creates a BOOLEAN Item
	 */
	public PromptItem(String description, Boolean value) {
		this(PromptItemType.CHECKBOX, description, value);
	}

	public PromptItem(String description, boolean value) {
		this(description, new Boolean(value));
	}
	/**
	 * Creates a RANGE Item
	 */
	public PromptItem(String description, Number value, float min, float max,
			float step) {
		this(PromptItemType.RANGE, description, value);
		this.setRange(min, max);
		this.increment = step;
	}
	
	/**
	 * Creates a LIST Item
	 */
	public PromptItem(String description, Object value, Object[] values) {
		this(PromptItemType.LIST, description, value);
		this.values = values;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public void setStep(float step) {
		this.increment = step;
	}

	public Object[] getValues() {
		return values;
	}

	public void setValues(Object[] values) {
		this.values = values;
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
	
	protected Item createItem(Dialog dialog, Border margin) {
		// Item:
		item = null;
		switch (type) {
			case RANGE:
				item = new Slider(dialog);
				break;
			case CHECKBOX:
				item = new CheckBox(dialog);
				break;
			case LIST:
				item = new PopupList(dialog);
				break;
			default:
				item = new TextEdit(dialog);
		}
		
		// Value:
		switch (type) {
			case STRING:
				((TextEditItem) item).setText(value.toString());
				break;
			case NUMBER:
			case UNIT:
			case RANGE:
				if (item instanceof TextEditItem) {
					((TextEditItem) item).setAllowMath(true);
					((TextEditItem) item).setAllowUnits(true);
					((TextEditItem) item).setShowUnits(type == PromptItemType.UNIT);
					((TextEditItem) item).setPrecision(precision);
				}
				if (type == PromptItemType.RANGE) {
					((Slider) item).setIncrements(increment, 8 * increment);
				}
				((ValueItem) item).setRange(min, max);
				((ValueItem) item).setValue((float) ConversionUtils.toDouble(value));
				break;
			case CHECKBOX:
				((CheckBox) item).setChecked(ConversionUtils.toBoolean(value));
				break;
			case LIST: {
					PopupList list = (PopupList) item;
					for (int i = 0; i < values.length; i++) {
						ListEntry entry = (ListEntry) list.add((ListEntry) values[i]);
						if (ConversionUtils.equals(value, values[i]))
							entry.setSelected(true);
					}
				}
				break;
				
		}
		// Margin needs to be defined before setting size, since getBestSize is affected by margin
		item.setMargin(margin);
		Size size = item.getBestSize();
		if (width >= 0)
			size.width = width;
		item.setSize(size);
		return item;
	}
	
	protected Object getResult() {
		switch(type) {
			case STRING:
				return ((TextValueItem) item).getText();
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
}