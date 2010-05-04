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
 * File created on Feb 23, 2010.
 *
 * $Id$
 */

package com.scriptographer.ui;

import java.util.LinkedHashMap;
import java.util.Map;

import com.scratchdisk.script.Callable;
import com.scratchdisk.script.ChangeObserver;
import com.scratchdisk.script.ScriptEngine;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.ui.layout.TableLayout;

/**
 * @author lehni
 * 
 */
public class Palette extends FloatingDialog implements ChangeObserver {
	private Map<String, Object> values;
	private Map<String, Object> components;
	private boolean hasLabels;

	public Palette(String title, Map<String, Object> components,
			Map<String, Object> values) {
		super(new DialogOption[] {
				DialogOption.TABBED,
				DialogOption.SHOW_CYCLE,
				DialogOption.REMEMBER_PLACING
		});

		if (values != null) {
			// Observer all existing properties for changes
			for (Object key : values.keySet())
				ScriptEngine.observeChanges(values, key, this);	
		} else {
			values = new LinkedHashMap<String, Object>();
		}
		if (components == null)
			components = new LinkedHashMap<String, Object>();
		this.values = values;
		this.components = components;

		double version = ScriptographerEngine.getApplicationVersion();
		boolean upperCase = false;
		int extraWidth = 32;
		double factor = 1;
		if (version >= 14) { // CS4 and above
			upperCase = true;
			extraWidth = 64;
		} else if (version >= 13) { // CS3
			factor = 1;
			extraWidth = 82;
		} else {
			// TODO: Test / Implement
		}
		// Calculate title size. Temporarily set bold font
		setFont(DialogFont.PALETTE_BOLD);
		int width = (int) Math.round(getTextSize(upperCase 
				? title.toUpperCase() : title).width * factor);
		setFont(DialogFont.PALETTE);
		// UI Requires 64px more to show title fully in palette windows.
		setMinimumSize(width + extraWidth, -1);
		setTitle(title);
		PaletteComponent[] paletteItems =
				PaletteComponent.getComponents(components, values);
		createLayout(this, paletteItems, false, 0);
		hasLabels = false;
		for (PaletteComponent item : paletteItems) {
			if (item != null) {
				this.components.put(item.getName(), item);
				String label = item.getLabel();
				if (label != null && !"".equals(label))
					hasLabels = true;
			}
		}

		if (hasLabels)
			setMargin(2, 2, 0, 4);
		else
			setMargin(2, -1, 0, -1);
	}

	public Palette(String title, Map<String, Object> components) {
		this(title, components, null);
	}

	protected void onInitialize() {
		// Since palettes remember placing, we need to explicitly set them
		// visible when they are created.
		setVisible(true);
		super.onInitialize();
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public Map<String, Object> getComponents() {
		return components;
	}

	public PaletteComponent getComponent(String name) {
		// components only contains PaletteComponent after initialization,
		// but is not declared in this way as the passed components object
		// is reused and PaletteComponent are put pack into it. This gives
		// easy access to them on the Scripting side.
		Object component = components.get(name);
		return component instanceof PaletteComponent
				? (PaletteComponent) component : null;
	}

	private Callable onChange = null;
	private boolean isChanging = false;

	public Callable getOnChange() {
		return onChange;
	}

	public void setOnChange(Callable onChange) {
		this.onChange = onChange;
	}

	protected void onChange(PaletteComponent item, String name, Object value,
			boolean callback) {
		if (!values.containsKey(name)) {
			// Make sure we observe new fields too. This has to do with the
			// nature of change observing on JavaScript, where observers can
			// only be installed for existing properties.
			// So add a null property under that name now, then register the 
			// observer to make sure it can register. The change after then
			// trigers straight away.
			values.put(name, null);
			ScriptEngine.observeChanges(values, name, this);	
		}
		isChanging  = true;
		values.put(name, value);
		isChanging = false;
		if (callback && onChange != null)
			ScriptographerEngine.invoke(onChange, this, item);
	}

	public void onChange(Map object, Object key, Object value) {
		if (!isChanging) {
			// System.out.println("Changed " + key + " = " + value);
			PaletteComponent component = getComponent(key.toString());
			if (component != null)
				component.setValue(value);
		}
	}

	protected static TableLayout createLayout(Dialog dialog,
			PaletteComponent[] components, boolean hasLogo, int extraRows) {
		// Add one more row as a filler in case there's less rows than the
		// height of the logo.
		if (hasLogo)
			extraRows++;
		double[] rows = new double[components.length + extraRows];
		for (int i = 0; i < rows.length; i++)
			rows[i] = TableLayout.PREFERRED;
		// Define the filler row, 2nd last
		if (hasLogo)
			rows[rows.length - extraRows] = TableLayout.FILL;
		else if (rows.length > 0)
			rows[rows.length - 1] = TableLayout.FILL;

		double[][] sizes = {
			hasLogo
				? new double[] { TableLayout.PREFERRED, TableLayout.FILL,
					TableLayout.PREFERRED }
				: new double[] { TableLayout.FILL, TableLayout.PREFERRED },
			rows
		};
		TableLayout layout = new TableLayout(sizes);
		dialog.setLayout(layout);

		if (hasLogo) {
			ImagePane logo = new ImagePane(dialog);
			logo.setImage(Dialog.getImage("logo.png"));
			logo.setMargin(-4, 4, -4, -4);
			// Logo uses all rows of components + filler row
			dialog.addToContent(logo, "0, 0, 0, " + (rows.length - extraRows)
					+ ", left, top");
		}

		int columnIndex = hasLogo ? 1 : 0;
		for (int i = 0; i < components.length; i++) {
			PaletteComponent item = components[i];
			if (item != null) {
				Item valueItem = item.createItem(dialog,
						new Border(1, 0, 1, 0));
				String label = item.getLabel();
				if (label != null && !"".equals(label)) {
					TextPane labelItem = new TextPane(dialog);
					labelItem.setText(label + ":");
					// Adjust top margin of label to reflect the native margin
					// in the value item.
					Item marginItem = valueItem;
					// If this is an item group, use the first item in it instead
					// This is only needed for FontPopupList so far.
					if (marginItem instanceof ItemGroup)
						marginItem = (Item) ((ItemGroup) marginItem).getContent().get(0);
					labelItem.setMargin(marginItem.getNativeMargin().top + 4, 4, 0, 0);
					dialog.addToContent(labelItem, columnIndex + ", " + i
							+ ", left, top");
				}
				dialog.addToContent(valueItem, (columnIndex + 1) + ", " + i
						+ ", left, center");
			}
		}
		return layout;
	}
}
