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
 * File created on Feb 9, 2008.
 *
 * $Id$
 */

package com.scriptographer.adm;

import java.util.HashMap;
import java.util.Map;

import com.scratchdisk.list.AbstractExtendedList;
import com.scratchdisk.list.StringIndexList;
import com.scriptographer.adm.Component.AWTContainer;

/**
 * Content handles the interface between the ADM components and the
 * underlying AWT layouting mechanisms in a way that works well also
 * in scripting languages, by behaving both like an array and a hash.
 * For hashes, the keys are layout constraints. This works for
 * TableLayout and FlowLayout.
 * 
 * @author lehni
 */
class Content extends AbstractExtendedList<Component> implements StringIndexList<Component> {

	private Component component;

	protected Content(Component component) {
		this.component = component;
	}

	protected AWTContainer getAWTContainer() {
		return component.getAWTContainer();
	}

	protected java.awt.Component getAWTComponent(Object element) {
		return element instanceof Component
			? ((Component) element).getAWTComponent() : null;
	}

	protected Component getComponent(java.awt.Component component) {
		return ((ComponentWrapper) component).getComponent();
	}

	protected void addComponent(Component element) {
		component.addComponent(element);
	}

	protected void removeComponent(Component element) {
		component.removeComponent(element);
	}
	// Keep track of set constraints in an internal HashMap,
	// so they can be removed again as well.
	HashMap<String,Component> constraints = new HashMap<String,Component>();

	protected String capitalize(String constraint) {
		return constraint.length() > 0
				? constraint.substring(0, 1).toUpperCase()
						+ constraint.substring(1)
				: constraint;
	}

	public Component get(String name) {
		name = capitalize(name);
		return constraints.get(name);
	}

	public Component remove(String name) {
		name = capitalize(name);
		Component element = constraints.get(name);
		if (element != null) {
			java.awt.Component component = getAWTComponent(element);
			// Now search for this component:
			AWTContainer container = getAWTContainer();
			for (int i = container.getComponentCount(); i >= 0; i--) {
				java.awt.Component comp = container.getComponent(i);
				if (comp == component) {
					container.remove(i);
					constraints.remove(name);
					removeComponent((Component) element);
					return element;
				}
			}
		}
		return null;
	}

	public int size() {
		return getAWTContainer().getComponentCount();
	}

	public Component set(int index, Component element) {
		Component previous = this.remove(index);
		this.add(index, element);
		return previous;
	}

	public Component get(int index) {
		return getComponent(getAWTContainer().getComponent(index));
	}

	public Component add(Component element) {
		java.awt.Component component = getAWTComponent(element);
		if (component != null) {
			getAWTContainer().add(component);
			addComponent((Component) element);
			return element;
		}
		return null;
	}

	public Component add(int index, Component element) {
		java.awt.Component component = getAWTComponent(element);
		if (component != null) {
			getAWTContainer().add(component, index);
			addComponent((Component) element);
			return element;
		}
		return null;
	}

	public void addAll(Map<String,? extends Component> elements) {
		for (Map.Entry<String,? extends Component> entry : elements.entrySet())
			set(entry.getKey().toString(), entry.getValue());
	}

	public Component remove(int index) {
		AWTContainer container = getAWTContainer();
		Component component = getComponent(container.getComponent(index));
		container.remove(index);
		removeComponent(component);
		return component;
	}

	public void removeAll() {
		AWTContainer container = getAWTContainer();
		for (int i = container.getComponentCount() - 1; i >= 0; i--)
			removeComponent(getComponent(container.getComponent(i)));
		container.removeAll();
	}

	public Component set(String name, Component element) {
		name = capitalize(name);
		java.awt.Component component = getAWTComponent(element);
		if (component != null) {
			Component previous = this.get(name);
			getAWTContainer().add(component, name);
			addComponent((Component) element);
			constraints.put(name, element);
			return previous;
		}
		return null;
	}
}
