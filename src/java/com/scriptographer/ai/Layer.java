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
 * File created on 11.01.2005.
 * 
 * $Id$
 */

package com.scriptographer.ai;

// TODO: subclass Group instead!
/**
 * The Layer item represents a layer in an Illustrator document.
 * @author lehni
 */
public class Layer extends Art {

	protected Layer(int handle) {
		super(handle);
	}

	public Layer() {
		super(TYPE_LAYER);
	}

	public native void setLocked(boolean locked);

	public native void setVisible(boolean visible);
	public native boolean isVisible();

	public native void setPreview(boolean preview);
	public native boolean getPreview();

	public native void setPrinted(boolean printed);
	public native boolean isPrinted();

	public native void setSelected(boolean selected);
	public native boolean isSelected();

	public native void setColor(Color color);
	public native RGBColor getColor();

	public void setColor(java.awt.Color color) {
		setColor(new RGBColor(color));
	}

	public native ArtSet getItems();
	
	/**
	 * Checks wether the layer is active.
	 * 
	 * @return <code>true</code> if the layer is active, <code>false</code>
	 *         otherwise.
	 */
	public native boolean isActive();
	
	/**
	 * Activates the layer
	 */
	public native void activate();
}
