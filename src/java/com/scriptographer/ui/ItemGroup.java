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
 * File created on 03.01.2005.
 *
 * $Id$
 */

package com.scriptographer.ui;

/**
 * A container that groups items logically, so that they can be
 * enabled or disabled together. The group object does not have a
 * visible representation, and does not affect the appearance or
 * position of its members, if the AWT layouting layer is not used.
 * It can however set a layout and add items to its content, in 
 * which case the positioning is taken care of.
 *
 * @author lehni
 */
public class ItemGroup extends Item implements ComponentGroup {

	public ItemGroup(Dialog dialog) {
		super(dialog, ItemType.ITEMGROUP);
	}

	private native void nativeAdd(Item item);
	private native void nativeRemove(Item item);

	protected void addComponent(Component component) {
		// Add natively only if it's not a fake item such as Spacer
		if (component.handle != 0 && component instanceof Item)
			nativeAdd((Item) component);
	}

	protected void removeComponent(Component component) {
		// Remove natively only if it's not a fake item such as Spacer
		if (component.handle != 0 && component instanceof Item)
			nativeRemove((Item) component);
	}

	public void add(Item item, String constraints) {
		if (constraints != null)
			getContent().put(constraints, item);
		else if (component != null)
			getContent().add(item);
		else
			addComponent(item);
	}

	public void add(Item item) {
		add(item, null);
	}

	public void remove(Item item) {
		if (component != null)
			getContent().remove(item);
		else
			removeComponent(item);
	}
	
	protected void initBounds() {
		// Do nothing here, since changing ItemGroup layout messes up things...?
	}

	protected void updateNativeBounds(int x, int y, int width, int height) {
		// Just like in Spacer, override this as apparently we should not change ItemGroup size.
	}
}