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

/**
 * @author lehni
 * 
 */
public class Palette implements PropertyObserver, Committable {

	private String title;
	private Map<String, Object> values;
	private Map<String, Object> components;
	private boolean sizeChanged;
	private boolean hasLabels;

	private PaletteProxy proxy;

	public Palette(String title, Map<String, Object> components,
			Map<String, Object> values) {
		if (values != null) {
			// Observer all existing properties for changes
			for (Object key : values.keySet())
				ScriptEngine.observeChanges(values, key, this);	
		} else {
			values = new LinkedHashMap<String, Object>();
		}
		if (components == null)
			components = new LinkedHashMap<String, Object>();
		this.title = title;
		this.values = values;
		this.components = components;
		Component[] paletteComponents =
			Component.getComponents(components, values);
		hasLabels = false;
		for (Component component : paletteComponents) {
			if (component != null) {
				component.palette = this;
				this.components.put(component.getName(), component);
				String label = component.getLabel();
				if (label != null && !"".equals(label))
					hasLabels = true;
			}
		}
		proxy = UiFactory.getInstance().createPalette(this, paletteComponents);
	}

	public Palette(String title, Map<String, Object> components) {
		this(title, components, null);
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public Map<String, Object> getComponents() {
		return components;
	}

	public String getTitle() {
		return title;
	}

	public boolean hasLabels() {
		return hasLabels;
	}

	public void reset() {
		for (Object component : components.values()) {
			if (component instanceof Component)
				((Component) component).reset();
		}
	}

	/**
	 * @jshide
	 */
	public Component getComponent(String name) {
		// components only contains PaletteComponent after initialization,
		// but is not declared in this way as the passed components object
		// is reused and PaletteComponent are put pack into it. This gives
		// easy access to them on the Scripting side.
		Object component = components.get(name);
		return component instanceof Component ? (Component) component : null;
	}

	private Callable onChange = null;
	private boolean isChanging = false;

	public Callable getOnChange() {
		return onChange;
	}

	public void setOnChange(Callable onChange) {
		this.onChange = onChange;
	}

	protected void onChange(Component component, String name, Object value,
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

	protected void onSizeChanged() {
		sizeChanged = true;
		// Use CommitManager functionality to update this dialog once
		// after all value changes.
		CommitManager.markDirty(this, this);
	}

	public void commit() {
		if (sizeChanged) {
			proxy.doLayout();
			sizeChanged = false;
		}
	}

	/**
	 * @jshide
	 */
	public void onChangeProperty(Map object, Object key, Object value) {
		if (!isChanging) {
			Component component = getComponent(key.toString());
			if (component != null)
				component.setValue(value);
		}
	}
}
