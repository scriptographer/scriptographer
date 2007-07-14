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
 * File created on 23.10.2005.
 * 
 * $Id$
 */

package com.scriptographer.adm;

import com.scratchdisk.util.ConversionHelper;

/**
 * @author lehni
 */
public class PromptItem {
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
	String name; // for preferences
	int type;
	Object value;
	Object values[];
	float min;
	float max;
	float increment;
	int precision;
	Item item;
	int width;
	
	public PromptItem(int type, String description, Object value) {
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
	 * Creates a TYPE_STRING Item
	 */
	public PromptItem(String description, String value) {
		this(TYPE_STRING, description, value);
	}

	/**
	 * Creates a TYPE_NUMBER Item
	 */
	public PromptItem(String description, Number value) {
		this(TYPE_NUMBER, description, value);
	}

	public PromptItem(String description, float value) {
		this(TYPE_NUMBER, description, new Float(value));
	}

	/**
	 * Creates a TYPE_BOOLEAN Item
	 */
	public PromptItem(String description, Boolean value) {
		this(TYPE_CHECKBOX, description, value);
	}

	public PromptItem(String description, boolean value) {
		this(description, new Boolean(value));
	}
	/**
	 * Creates a TYPE_RANGE Item
	 */
	public PromptItem(String description, Number value, float min, float max,
			float step) {
		this(TYPE_RANGE, description, value);
		this.setRange(min, max);
		this.increment = step;
	}
	
	/**
	 * Creates a TYPE_LIST Item
	 */
	public PromptItem(String description, Object value, Object[] values) {
		this(TYPE_LIST, description, value);
		this.values = values;
	}
	
	// TODO: make constructor for TYPE_UNIT
	
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
	
	protected Item createItem(Dialog dialog) {
		// Item:
		item = null;
		switch (type) {
			case TYPE_RANGE:
				item = new Slider(dialog);
				break;
			case TYPE_CHECKBOX:
				item = new CheckBox(dialog);
				break;
			case TYPE_LIST:
				item = new PopupList(dialog);
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
					((TextEdit) item).setPrecision(precision);
				}
				if (type == TYPE_RANGE) {
					((Slider) item).setIncrements(increment, 8 * increment);
				}
				((ValueItem) item).setRange(min, max);
				((ValueItem) item).setValue((float) ConversionHelper.toDouble(value));
				break;
			case TYPE_CHECKBOX:
				((CheckBox) item).setChecked(ConversionHelper.toBoolean(value));
				break;
			case TYPE_LIST: {
					PopupList list = (PopupList) item;
					for (int i = 0; i < values.length; i++) {
						ListEntry entry = (ListEntry) list.add(values[i]);
						if (value != null && value.equals(values[i]))
							entry.setSelected(true);
					}
				}
				break;
				
		}
		item.setFont(Dialog.FONT_PALETTE);
		Size size = item.getBestSize();
		if (width >= 0)
			size.width = width;
		item.setSize(size);
		return item;
	}
	
	protected Object getResult() {
		switch(type) {
			case TYPE_STRING:
				return ((TextValueItem) item).getText();
			case TYPE_NUMBER:
			case TYPE_UNIT:
			case TYPE_RANGE:
				return new Float(((ValueItem) item).getValue());
			case TYPE_CHECKBOX:
				return new Boolean(((ToggleItem) item).isChecked());
			case TYPE_LIST:
				ListEntry active = ((PopupList) item).getActiveEntry();
				if (active != null)
					return active.getText();
		}
		return null;
	}
	
	protected static int getType(String type) {
		for (int i = 0; i < typeNames.length; i++) {
			if (typeNames[i].equals(type))
				return i;
		}
		return -1;
	}
}