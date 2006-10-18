/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile: Spacer.java,v $
 * $Author: lehni $
 * $Revision: 1.5 $
 * $Date: 2006/10/18 14:08:28 $
 */

package com.scriptographer.adm;

import java.awt.*;

import com.scriptographer.js.Unsealed;

public class Spacer extends Item implements Unsealed {

	public Spacer(int width, int height) {
		size = new Dimension(width, height);
		bounds = new Rectangle(0, 0, width, height);
	}

	public Spacer(Dimension size) {
		this(size.width, size.height);
	}

	public Dimension getPreferredSize() {
		return size;
	}

	public Dimension getSize() {
		return size;
	}

	public void setSize(int width, int height) {
		size.setSize(width, height);
	}

	public Rectangle getBounds() {
		return bounds;
	}

	public void setBounds(int x, int y, int width, int height) {
		bounds.setBounds(x, y, width, height);
	}

	public void setLocation(int x, int y) {
		bounds.setBounds(x, y, size.width, size.height);
	}

	public Point getLocation() {
		return new Point(bounds.x, bounds.y);
	}
}
