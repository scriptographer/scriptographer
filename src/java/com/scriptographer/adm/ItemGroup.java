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
 * File created on 03.01.2005.
 */

package com.scriptographer.adm;


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
		super.addComponent(component);
		// Add natively only if it's not a fake item such as Spacer
		if (component.isValid() && component instanceof Item)
			nativeAdd((Item) component);
	}

	protected void removeComponent(Component component) {
		super.removeComponent(component);
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
	 * to intersect mouse events. It is also necessary on Mac, where setting
	 * ItemGroup bounds to a size seems to also resize all items it contains to
	 * the same size.
	 */
	protected Rectangle nativeGetBounds() {
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
	}

	protected void nativeSetBounds(int x, int y, int width, int height) {
		// It seems that on CS2 we need to call setSize to really force it
		// to 0. Always use both to be on the safe side.
		// I am not sure why both calls are needed, but not calling
		// nativeSetSize on Mac seems to hide all grouped items in some
		// situations, e.g. in the repository editor.
		super.nativeSetBounds(x, y, 0, 0);
		super.nativeSetSize(0, 0);
	}

	protected void nativeSetSize(int width, int height) {
		super.nativeSetSize(0, 0);
	}
}