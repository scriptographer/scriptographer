/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 04.01.2005.
 *
 * $Id$
 */

package com.scriptographer.adm;

import java.awt.*;

/**
 * ItemContainer acts as container of ADM Items that share a layout and can be
 * added to a parent container, e.g. another ItemContainer or a Dialog. This is
 * part of the AWT Layout wrapper code. ItemContainer itself wrapps a
 * AWTItemContainer that does the actuall layout work.
 * 
 * @author lehni
 */
public class ItemContainer {
	protected AWTItemContainer container;
	protected Frame frame;

	public ItemContainer(LayoutManager mgr, Item[] items, Frame frame) {
		this.container = new AWTItemContainer(mgr);
		this.frame = frame;
		if (items != null)
			for (int i = 0; i < items.length; i++)
				add(items[i]);
	}

	public ItemContainer(LayoutManager mgr, Item[] items) {
		this(mgr, items, null);
	}

	public ItemContainer(LayoutManager mgr) {
		this(mgr, null, null);
	}

	public ItemContainer() {
		this(null, null, null);
	}

	protected Component getComponent() {
		return container;
	}

	public void setFrame(Frame frame) {
		this.frame = frame;
	}

	public Frame getFrame() {
		return frame;
	}

	public void add(Item item, Object constraints) {
		container.add(item.getComponent(), constraints);
	}
	
	public void add(Item item) {
		add(item, null);
	}

	public void setSize(int x, int y) {
		container.setSize(x, y);
	}
	
	public void setSize(Dimension d) {
		container.setSize(d);
	}

	public void setSize(Point size) {
		setSize(size.x, size.y);
	}

	public Dimension getSize() {
		return container.getSize();
	}

	public void setInsets(int left, int top, int right, int bottom) {
		container.setInsets(left, top, right, bottom);
	}

	public void setInsets(int hor, int ver) {
		container.setInsets(hor, ver, hor, ver);
	}

	public Insets getInsets() {
		return container.getInsets();
	}

	public void doLayout() {
		container.doLayout();
	}

	/**
	 * The actualy AWT class for ItemContainer that does the work of collecting
	 * wrap items or other ItemContainers and redirecting doLayout calls to its
	 * children.
	 * 
	 * @author lehni
	 */
	class AWTItemContainer extends Container {
		Insets insets;

		public AWTItemContainer(LayoutManager mgr) {
			if (mgr != null)
				setLayout(mgr);
			setInsets(0, 0, 0, 0);
		}

		public void setInsets(int left, int top, int right, int bottom) {
			insets = new Insets(top, left, bottom, right);
		}

		public Insets getInsets() {
			return insets;
		}

		public void doLayout() {
			super.doLayout();
			// now walk through all the items do their layout as well:
			Component[] components = getComponents();
			for (int i = 0; i < components.length; i++)
				components[i].doLayout();
		}

		public void setBounds(int x, int y, int width, int height) {
			super.setBounds(x, y, width, height);
			if (frame != null) {
				java.awt.Point origin = getOrigin();
				frame.setBounds(x + origin.x, y + origin.y, width, height);
			}
		}

		public void setBounds(Rectangle r) {
			setBounds(r.x, r.y, r.width, r.height);
		}

		protected java.awt.Point getOrigin() {
			java.awt.Point delta = new java.awt.Point();
			Container parent = getParent();
			while (true) {
				Container next = parent.getParent();
				if (next == null)
					break;
				java.awt.Point loc = parent.getLocation();
				delta.x += loc.x;
				delta.y += loc.y;
				parent = next;
			}
			return delta;
		}

		public void setSize(int width, int height) {
			super.setSize(width, height);
			if (frame != null) {
				java.awt.Point loc = getLocation();
				frame.setBounds(loc.x, loc.y, width, height);
			}
		}

		public void setSize(Dimension d) {
			setSize(d.width, d.height);
		}

		public void setLocation(int x, int y) {
			super.setLocation(x, y);
			if (frame != null) {
				java.awt.Point origin = getOrigin();
				frame.setPosition(x + origin.x, y + origin.y);
			}
		}

		public void setLocation(Point p) {
			setLocation(p.x, p.y);
		}

		public boolean isVisible() {
			return true;
		}
	}
}
