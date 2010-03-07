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
 * File created on Mar 7, 2010.
 *
 * $Id$
 */

package com.scriptographer.ui;

import java.awt.Color;

/**
 * @author lehni
 *
 */
public class ColorButton extends ImageButton {

	private Color color;

	public ColorButton(Dialog dialog) {
		super(dialog);
		// TODO: Fix this in item instead! Size defaults to 100 / 100...
		setSize(getBestSize());
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;

		if(color != null) {
			Rectangle rect = new Rectangle(new Point(),
					getSize().subtract(getMargin()).subtract(6));
			Image image = new Image(rect.width, rect.height, ImageType.RGB);
			Drawer drawer = image.getDrawer();
			drawer.setColor(color);
			drawer.fillRect(rect);
			drawer.dispose();
			setImage(image);
		}
	}

	protected void updateBounds(int x, int y, int width, int height,
			boolean sizeChanged) {
		super.updateBounds(x, y, width, height, sizeChanged);
		setColor(color);
	}

	protected void onClick() throws Exception {
		Color color = Dialog.chooseColor(this.color);
		if(color != null) {
			setColor(color);
			super.onClick();
		}
	}
}
