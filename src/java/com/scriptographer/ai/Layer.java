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
 * File created on 11.01.2005.
 */

package com.scriptographer.ai;

import java.awt.Graphics2D;

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

	protected Layer(int handle, int docHandle, boolean created) {
		super(handle, docHandle, created);
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
	 * Specifies whether the layer is active.
	 * 
	 * @return {@true if the layer is active}
	 */
	public native boolean isActive();

	public void setActive(boolean active) {
		if (active) {
			activate();
		} else {
			// TODO: Activate another layer?
		}
	}

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

	public Item copyTo(Document document) {
		Item copy = super.copyTo(document);
		copy.moveAbove(document.getActiveLayer());
		return copy;
	}

	/*
	 * getPreviousSibling / getNextSibling works for nested layers but not
	 * for top level layers. Top level layers require nativeGetNextLayer / 
	 * nativeGetPreviousLayer, which work with the layer list.
	 * We are deciding which one is required by checking the parent.
	 */
	private native Item nativeGetNextLayer();

	private native Item nativeGetPreviousLayer();

	public Item getNextSibling() {
		if (getParent() == null)
			return nativeGetNextLayer();
		return super.getNextSibling();
	}

	public Item getPreviousSibling() {
		if (getParent() == null)
			return nativeGetPreviousLayer();
		return super.getPreviousSibling();
	}

	/**
	 * Draws the layer's content into a Graphics2D object. Useful for
	 * conversions.
	 * 
	 * @jshide
	 */
	public void paint(Graphics2D graphics) {
		ItemList children = getChildren();
		for (int i = children.size() - 1; i >= 0; i--) {
			children.get(i).paint(graphics);
		}
	}
}
