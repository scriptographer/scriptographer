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
import com.scratchdisk.script.PropertyObserver;
import com.scratchdisk.script.ScriptEngine;
import com.scriptographer.CommitManager;
import com.scriptographer.Committable;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.ui.layout.TableLayout;

/**
 * @author lehni
 * 
 */
public class Palette extends FloatingDialog implements PropertyObserver,
		Committable {
	private Map<String, Object> values;
	private Map<String, Object> components;
	private boolean hasLabels;
	private boolean layoutChanged;

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
		if (version >= 14) { // CS4 and above
			upperCase = true;
			extraWidth = 64;
		} else if (version >= 13) { // CS3
			extraWidth = 82;
		} else {
			// TODO: Test / Implement
		}
		// Calculate title size. Temporarily set bold font
		setFont(DialogFont.PALETTE_BOLD);
		int width = (int) Math.round(getTextSize(upperCase 
				? title.toUpperCase() : title).width);
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

	public void reset() {
		for (Object component : components.values()) {
			if (component instanceof PaletteComponent)
				((PaletteComponent) component).reset();
		}
	}

	/**
	 * @jshide
	 */
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

	protected void onChange(PaletteComponent component, String name, Object value,
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
		// Use CommitManager functionality to update this dialog once
		// after all value changes.
		CommitManager.markDirty(this, this);
		if (callback && onChange != null)
			ScriptographerEngine.invoke(onChange, this, component);
	}

	protected void onLayoutChanged() {
		layoutChanged = true;
		// Use CommitManager functionality to update this dialog once
		// after all value changes.
		CommitManager.markDirty(this, this);
	}

	public void commit() {
		if (layoutChanged) {
			// Make sure size changes are taken into account and palette is
			// resized accordingly.
			Size size = getPreferredSize();
			if (!getSize().equals(size)) {
				// setSize internally causes doLayout to be called, no need
				// to call here too.
				setSize(getPreferredSize());
			} else {
				// Just call doLayout to realign things, as the total dialog
				// size has not changed.
				doLayout();
			}
			layoutChanged = false;
		}
		update();
	}

	/**
	 * @jshide
	 */
	public void onChangeProperty(Map object, Object key, Object value) {
		if (!isChanging) {
			// System.out.println("Changed " + key + " = " + value);
			PaletteComponent component = getComponent(key.toString());
			if (component != null)
				component.setValue(value);
		}
	}

	protected static TableLayout createLayout(Dialog dialog,
			PaletteComponent[] components, boolean hasLogo, int extraRows) {
		// First collect all content in a LinkedHashMap, then create the layout
		// at the end, and add the items to it. This allows flexibility
		// regarding amount of rows, as needed by the ruler element that uses
		// two rows when it has a title.

		LinkedHashMap<String, Component> content =
				new LinkedHashMap<String, Component>();

		int column = hasLogo ? 1 : 0, row = 0;
		for (int i = 0; i < components.length; i++) {
			PaletteComponent item = components[i];
			if (item != null)
				row = item.addToContent(dialog, content, column, row);
		}

		if (hasLogo) {
			ImagePane logo = new ImagePane(dialog);
			logo.setImage(Dialog.getImage("logo.png"));
			logo.setMargin(-4, 4, -4, -4);
			// Logo uses all rows of components + filler row
			content.put("0, 0, 0, " + row + ", left, top",
					logo);
			row++;
		}
	
		double[] rows = new double[row + extraRows];
		for (int i = 0; i < rows.length; i++)
			rows[i] = TableLayout.PREFERRED;

		// Define the filler row, 2nd last
		if (hasLogo)
			rows[rows.length - extraRows - 1] = TableLayout.FILL;
		else if (rows.length > 0)
			rows[rows.length - 1] = TableLayout.FILL;

		double[][] sizes = {
			hasLogo
				? new double[] { TableLayout.PREFERRED, TableLayout.PREFERRED,
					TableLayout.FILL }
				: new double[] { TableLayout.PREFERRED, TableLayout.FILL },
			rows
		};
		TableLayout layout = new TableLayout(sizes, 0, 3);
		dialog.setLayout(layout);
		dialog.setContent(content);

		return layout;
	}
}
