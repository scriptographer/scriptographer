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
 * File created on 03.12.2004.
 */

package com.scriptographer.ai;

import com.scratchdisk.list.List;
import com.scratchdisk.list.Lists;

/**
 * A Group is a collection of items. When you transform a Group, it's
 * children are treated as a single unit without changing their relative
 * positions. The Group's style or transparency blending attributes affect the
 * rendering of it's children without changing their style/attributes.
 * 
 * @author lehni
 */
public class Group extends Item {

	protected Group(int handle, int docHandle, boolean created) {
		super(handle, docHandle, created);
	}

	/**
	 * Creates a group item
	 */
	public Group() {
		super(TYPE_GROUP);
	}
	
	/**
	 * Creates a group item.
	 * 
	 * Sample code:
	 * <code>
	 * // Create an empty group:
	 * var group = new Group();
	 * // Append a path to the group:
	 * var line = new Path.Line(new Point(10, 10), new Point(50, 50));
	 * group.appendTop(line);
	 * 
	 * // Create a group containing a path:
	 * var circle = new Path.Circle(new Point(10, 10), 100);
	 * var circleGroup = new Group([circle]);
	 * </code>
	 * 
	 * @param children the children to be added to the newly created group
	 */
	public Group(List<? extends Item> children) {
		this();
		for (Item child : children)
			this.appendBottom(child);
	}

	public Group(Item[] children) {
		this(Lists.asList(children));
	}
	
	/**
	 * Specifies whether the group item is to be clipped.
	 * When setting to true, the first child in the group is automatically
	 * defined as the clipping mask.
	 *
	 * Sample code:
	 * <code>
	 * var group = new Group();
	 * group.appendChild(path);
	 * group.clipped = true;
	 * </code>
	 * @return {@true if the group item is to be clipped}
	 */
	public native boolean isClipped();

	private native void nativeSetClipped(boolean clipped);

	public void setClipped(boolean clipped) {
		nativeSetClipped(clipped);
		Item child = getFirstChild();
		if (child != null)
			child.setClipMask(clipped);
	}

	/*
	 * Setting selected attribute on Groups and CompoundPaths only works when
	 * also explicitly applying the same attributes to all the children, even
	 * when using setFully selected. So override both here.
	 */
	public void setSelected(boolean selected) {
		Item child = getFirstChild();
		while (child != null) {
			child.setSelected(selected);
			child = child.getNextSibling();
		}
		super.setSelected(selected);
	}

	public void setFullySelected(boolean selected) {
		Item child = getFirstChild();
		while (child != null) {
			child.setFullySelected(selected);
			child = child.getNextSibling();
		}
		super.setFullySelected(selected);
	}

	public Rectangle getBounds() {
		// Delegate to the clipped item...
		Item item = isClipped() ? getFirstChild() : null;
		if (item != null)
			return item.getBounds();
		item = getFirstChild();
		if (item != null) {
			Rectangle bounds = item.getBounds();
			item = item.getNextSibling();
			while (item != null) {
				bounds = bounds.unite(item.getBounds());
				item = item.getNextSibling();
			}
			return bounds;
		}
		return super.getBounds();
	}

	public Rectangle getStrokeBounds() {
		Item item = isClipped() ? getFirstChild() : null;
		if (item != null)
			return item.getStrokeBounds();
		item = getFirstChild();
		if (item != null) {
			Rectangle bounds = item.getStrokeBounds();
			item = item.getNextSibling();
			while (item != null) {
				bounds = bounds.unite(item.getStrokeBounds());
				item = item.getNextSibling();
			}
			return bounds;
		}
		return super.getStrokeBounds();
	}

	public Rectangle getControlBounds() {
		Item item = isClipped() ? getFirstChild() : null;
		if (item != null)
			return item.getControlBounds();
		item = getFirstChild();
		if (item != null) {
			Rectangle bounds = item.getControlBounds();
			item = item.getNextSibling();
			while (item != null) {
				bounds = bounds.unite(item.getControlBounds());
				item = item.getNextSibling();
			}
			return bounds;
		}
		return super.getControlBounds();
	}
}