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
 * File created on 06.03.2005.
 * 
 * $Id$
 */

package com.scriptographer.adm;

/**
 * @author lehni
 */
public class Spacer extends Item {

	private boolean visible;

	public Spacer(int width, int height) {
		bounds = new Rectangle(0, 0, width, height);
		visible = true;
	}

	public Spacer(Size size) {
		this(size.width, size.height);
	}

	public Size getPreferredSize() {
		return bounds.getSize();
	}
	
	protected void updateBounds(int x, int y, int width, int height) {
		// override this as we are not using all the native stuff internally in
		// spacers.
		// setBounds does not need to be touched, as it relies on updateBounds
		// for the heavy lifting.
		bounds.set(x, y, width, height);
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	
}
