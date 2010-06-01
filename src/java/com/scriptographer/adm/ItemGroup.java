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
 */

package com.scriptographer.adm;

import com.scriptographer.ScriptographerEngine;

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
		if (component.isValid() && component instanceof Item)
			nativeAdd((Item) component);
	}

	protected void removeComponent(Component component) {
		// Remove natively only if it's not a fake item such as Spacer
		if (component.isValid() && component instanceof Item)
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

	/*
	 * Override bounds handling so that ItemGroups always have a native size of
	 * 0. This is required on Windows where ItemGroups otherwise sometimes seem
	 * to intersect mouse events.
	 */
	protected Rectangle nativeGetBounds() {
		if (ScriptographerEngine.isWindows()) {
			// If nativeGetBounds returns bounds with a size != 0, we have not
			// set its size to 0 yet and need to report the real native size
			// back so initBounds() forces a call of nativeSetBounds, in which
			// size is then set to 0.
			Rectangle bounds = super.nativeGetBounds();
			if (nativeBounds == null || bounds.width > 0 || bounds.height > 0)
				return bounds;
			// In any other case, do not use the native bounds but the
			// internally reflected value.
			return (Rectangle) nativeBounds.clone();
		} else {
			return super.nativeGetBounds();
		}
	}

	protected void nativeSetBounds(int x, int y, int width, int height) {
		if (ScriptographerEngine.isWindows()) {
			super.nativeSetBounds(x, y, 0, 0);
			// It seems that on CS2 we need to call setSize to really force it
			// to 0. Always use both to be on the safe side.
			super.nativeSetSize(0, 0);
		} else {
			super.nativeSetBounds(x, y, width, height);
		}
	}

	protected void nativeSetSize(int width, int height) {
		if (ScriptographerEngine.isWindows()) {
			super.nativeSetSize(0, 0);
		} else {
			super.nativeSetSize(width, height);
		}
	}
}