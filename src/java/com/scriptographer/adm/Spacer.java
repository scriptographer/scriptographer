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
 * File created on 06.03.2005.
 */

package com.scriptographer.adm;

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
