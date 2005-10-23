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
 * File created on 23.10.2005.
 * 
 * $RCSfile: PromptItem.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/10/23 00:33:04 $
 */

package com.scriptographer.adm;

import java.awt.Dimension;

import org.mozilla.javascript.ScriptRuntime;

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
	int type;
	Object value;
	float min;
	float max;
	float step;
	com.scriptographer.adm.Item item;
	int width;
	
	public PromptItem(int type, String description, Object value, int width, float min, float max, float step) {
		this.description = description;
		this.type = type;
		this.value = value;
		this.width = width;
		this.min = min;
		this.max = max;
		this.step = step;
	}

	public PromptItem(int type, String description, Object value) {
		this(type, description, value, -1, Float.MIN_VALUE, Float.MAX_VALUE, 0);
	}
	
	protected com.scriptographer.adm.Item createItem(Dialog dialog) {
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
	
	protected Object getValue() {
		switch(type) {
			case TYPE_STRING:
				return ((TextValueItem) item).getText();
			case TYPE_NUMBER:
			case TYPE_UNIT:
			case TYPE_RANGE:
				return new Float(((ValueItem) item).getValue());
			case TYPE_CHECKBOX:
				return new Boolean(((ToggleItem) item).isChecked());
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