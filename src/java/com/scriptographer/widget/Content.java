/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 * 
 * File created on Feb 9, 2008.
 */

package com.scriptographer.widget;

import java.util.HashMap;
import java.util.Map;

import com.scratchdisk.list.AbstractExtendedList;
import com.scratchdisk.list.StringIndexList;
import com.scriptographer.widget.Component;
import com.scriptographer.widget.Component.AWTContainer;

/**
 * Content handles the interface between the UI components and the
 * underlying AWT layouting mechanisms in a way that works well also
 * in scripting languages, by behaving both like an array and a hash.
 * For hashes, the keys are layout constraints. This works for
 * TableLayout and FlowLayout.
 * 
 * @author lehni
 * 
 * @jshide
 */
public class Content extends AbstractExtendedList<Component> implements
		StringIndexList<Component> {

	private Component component;

	protected Content(Component component) {
		this.component = component;
	}

	public Class<?> getComponentType() {
		return Component.class;
	}

	protected AWTContainer getAWTContainer() {
		return component.getAWTContainer(true);
	}

	protected java.awt.Component getAWTComponent(Object element) {
		return element instanceof Component
			? ((Component) element).getAWTComponent(true) : null;
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
					removeComponent(element);
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
			addComponent(element);
			return element;
		}
		return null;
	}

	public Component add(int index, Component element) {
		java.awt.Component component = getAWTComponent(element);
		if (component != null) {
			getAWTContainer().add(component, index);
			addComponent(element);
			return element;
		}
		return null;
	}

	public void addAll(Map<String,? extends Component> elements) {
		for (Map.Entry<String,? extends Component> entry : elements.entrySet())
			put(entry.getKey().toString(), entry.getValue());
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

	public Component put(String name, Component element) {
		name = capitalize(name);
		java.awt.Component component = getAWTComponent(element);
		if (component != null) {
			Component previous = this.get(name);
			getAWTContainer().add(component, name);
			addComponent(element);
			constraints.put(name, element);
			return previous;
		}
		return null;
	}
}
