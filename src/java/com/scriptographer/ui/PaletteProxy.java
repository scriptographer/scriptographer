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
 * File created on Jun 2, 2010.
 */

package com.scriptographer.ui;

/**
 * @author lehni
 *
 */
public abstract class PaletteProxy {

	protected Palette palette;

	public PaletteProxy(Palette palette) {
		this.palette = palette;
	}

	public abstract void update(boolean sizeChanged);
}
