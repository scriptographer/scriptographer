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
 * File created on 06.03.2005.
 * 
 * $Id$
 */

package com.scriptographer.ui;

/**
 * @author lehni
 */
public class Spacer extends Item {

	private boolean visible;

	public Spacer(int width, int height) {
		// Don't call super prototype, since we're not actually creating a new item
		type = ItemType.SPACER;
		bounds = new Rectangle(0, 0, width, height);
		visible = true;
	}

	public Spacer(Size size) {
		this(size.width, size.height);
	}

	public Size getPreferredSize() {
		return bounds.getSize();
	}

	protected void initBounds() {
		// Do nothing here
	}
	
	protected void updateNativeBounds(int x, int y, int width, int height) {
		// override this as we are not using all the native stuff internally in
		// spacers.
		// setBounds does not need to be touched, as it relies on updateBounds
		// for the heavy lifting.
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	
}
