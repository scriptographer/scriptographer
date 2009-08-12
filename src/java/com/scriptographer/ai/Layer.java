/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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

// TODO: subclass Group instead?
/**
 * The Layer item represents a layer in an Illustrator document.
 * 
 * The layer which is currently active can be accessed through {@link Document#getActiveLayer()}.
 * An array of all layers in a document can be accessed through {@link Document#getLayers()}.
 * 
 * Sample code:
 * <code>
 * print(document.activeLayer.name); // 'Layer 1'
 * var layer = new Layer();
 * layer.name = 'A new layer';
 * layer.activate();
 * print(document.activeLayer.name); // 'A new layer'
 * print(document.layers.length); // 2
 * </code>
 * @author lehni
 */
public class Layer extends Item {

	protected Layer(int handle, int docHandle) {
		super(handle, docHandle);
	}

	/**
	 * Creates a new Layer item.
	 * 
	 * Sample code:
	 * <code>
	 * var layer = new Layer();
	 * layer.name = 'the new layer';
	 * </code>
	 */
	public Layer() {
		super(TYPE_LAYER);
	}

	public native void setLocked(boolean locked);

	/**
	 * Specifies whether the layer is visible.
	 * 
	 * @return {@true if the layer is visible}
	 */
	public native boolean isVisible();

	public native void setVisible(boolean visible);

	/**
	 * Specifies whether the layer is set to preview ({@code true}) or outline
	 * mode ({@code false}). If a layer is set to outline mode, items in all
	 * it's child layers are rendered in outline mode, regardless of their
	 * preview settings.
	 * 
	 * @return {@code true} if the layer is set to preview, {@code false} if the
	 *         layer is set to outline
	 */
	public native boolean getPreview();

	public native void setPreview(boolean preview);

	/**
	 * Specifies whether the layer is considered printable
	 * when printing the document.
	 * @return {@true if the layer is considered printable}
	 */
	public native boolean isPrinted();

	public native void setPrinted(boolean printed);

	/**
	 * Specifies whether the layer is selected.
	 * {@true if the layer is selected}
	 */
	public native boolean isSelected();

	public native void setSelected(boolean selected);

	/**
	 * Specifies the color used for outlining items when they are selected.
	 */
	public native RGBColor getColor();

	public native void setColor(Color color);

	public void setColor(java.awt.Color color) {
		setColor(new RGBColor(color));
	}

	/**
	 * Returns all items contained within the layer, including indirect children
	 * of the layer.
	 * 
	 * Sample code:
	 * <code>
	 * var group = new Group();
	 * var path = new Path();
	 * group.appendTop(path);
	 * 
	 * // the direct children of the layer:
	 * print(document.activeLayer.children); // Group (@31fbd500)
	 * 
	 * // all items contained within the layer:
	 * print(document.activeLayer.items); // Group (@31fbd500), Path (@31fbbb00)
	 * </code>
	 */
	public native ItemList getItems();
	
	/**
	 * Checks whether the layer is active.
	 * 
	 * @return {@true if the layer is active}
	 */
	public native boolean isActive();
	
	/**
	 * Activates the layer.
	 * 
	 * Sample code:
	 * <code>
	 * var layer = new Layer();
	 * layer.name = 'new layer';
	 * layer.activate();
	 * print(document.activeLayer) // 'Layer (new layer)'
	 * </code>
	 */
	public native void activate();
}
