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
 * File created on Dec 18, 2007.
 *
 * $Id$
 */

package com.scriptographer.adm;

import java.awt.LayoutManager;
import java.util.Iterator;
import java.util.Map;

/**
 * A helper class to add the same layout related functionality to 
 * Dialog and ItemContainer. It would be nicer to use polymorphism
 * or mix-ins but unfortunately we cannot.
 * 
 * @author lehni
 *
 */
class LayoutHelper {
	private ContainerProvider provider;

	LayoutHelper(ContainerProvider provider) {
		this.provider = provider;
	}
	
	void setLayout(LayoutManager mgr) {
		this.provider.getContainer().setLayout(mgr);
	}

	void setMargins(int left, int top, int right, int bottom) {
		this.provider.getContainer().setInsets(top, left, bottom, right);
	}

	Margins getMargins() {
		return new Margins(this.provider.getContainer().getInsets());
	}

	void setMargins(Margins margins) {
		setMargins(margins.left, margins.top, margins.right, margins.bottom);
	}

	void setMargins(int margins) {
		setMargins(margins, margins, margins, margins);
	}

	void setMargins(int[] margins) {
		setMargins(margins[0], margins[1], margins[2], margins[3]);
	}

	void setMargins(int hor, int ver) {
		setMargins(hor, ver, hor, ver);
	}

	int getLeftMargin() {
		return getMargins().left;
	}

	void setLeftMargin(int left) {
		Margins margins = getMargins();
		margins.left = left;
		setMargins(margins);
	}

	int getTopMargin() {
		return getMargins().top;
	}

	void setTopMargin(int top) {
		Margins margins = getMargins();
		margins.top = top;
		setMargins(margins);
	}

	int getRightMargin() {
		return getMargins().right;
	}

	void setRightMargin(int right) {
		Margins margins = getMargins();
		margins.right = right;
		setMargins(margins);
	}

	int getBottomMargin() {
		return getMargins().bottom;
	}

	void setBottomMargin(int bottom) {
		Margins margins = getMargins();
		margins.bottom = bottom;
		setMargins(margins);
	}

	void addToContent(Item item, Object constraints) {
		this.provider.getContainer().add(item.getComponent(), constraints);
	}

	void addToContent(Item item) {
		addToContent(item, null);
	}

	void addToContent(ItemContainer container, Object constraints) {
		this.provider.getContainer().add(container.getContainer(), constraints);
	}

	void addToContent(ItemContainer layout) {
		addToContent(layout, null);
	}

	void addToContent(Map items) {
		for (Iterator it = items.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			Object constraints = entry.getKey();
			if (constraints instanceof String) {
				// Capitalize
				String str = (String) constraints;
				if (str.length() > 0)
					constraints = str.substring(0, 1).toUpperCase()
							+ str.substring(1);
			}
			Object item = entry.getValue();
			if (item instanceof Item)
				addToContent((Item) item, constraints);
			else if (item instanceof ItemContainer)
				addToContent((ItemContainer) item, constraints);
			else throw new IllegalArgumentException(item.toString());
		}
	}

	// For the bean setter:
	void setContent(Map items) {
		addToContent(items);
	}
}
