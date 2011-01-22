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
 * File created on 14.02.2005.
 */

package com.scriptographer.ai;


/**
 * @author lehni
 * 
 * @jshide
 */
public class FillStyle implements Style {
	/*
	 * Setting these fields to null means undefined.
	 * Setting color to Color.NONE means defined, but style is 
	 * deactivated
	 */

	/**
	 *  Fill color
	 */
	protected Color color;

	/**
	 * Overprint 
	 */
	protected Boolean overprint;

	private PathStyle style = null;

	protected FillStyle(PathStyle style) {
		this.style = style;
	}

	protected FillStyle(FillStyle fill, PathStyle style) {
		this(fill);
		this.style = style;
	}

	public FillStyle() {
		color = null;
		overprint = null;
	}

	public FillStyle(FillStyle fill) {
		if (fill != null) {
			init(fill.color, fill.overprint);
		}
	}

	public FillStyle(Color color, Boolean overprint) {
		init(color, overprint);
	}

	public FillStyle(Color color) {
		init(color, false);
	}

	/**
	 * called from the native environment
	 */
	protected FillStyle(Color color, boolean hasColor, short overprint) {
		init(color, hasColor, overprint);
	}

	protected void init(Color color, Boolean overprint) {
		this.color = color;
		this.overprint = overprint;
	}

	/**
	 * called from the native environment
	 */
	protected void init(Color color, boolean hasColor, short overprint) {
		this.color = hasColor && color == null ? Color.NONE : color;
		this.overprint = overprint < 0 ? null : new Boolean(overprint != 0);
	}
	
	protected void setStyle(PathStyle style) {
		this.style = style;
	}

	protected void initNative(int handle) {
		PathStyle.nativeInitFillStyle(handle, 
				color != null && color != Color.NONE ? color : null, color != null, 
				overprint != null ? (short) (overprint.booleanValue() ? 1 : 0) : -1
		);
	}

	public Color getColor() {
		if (style != null)
			style.update();
		return color;
	}

	public void setColor(Color color) {
		if (style != null) {
			style.update();
			style.markDirty();
		}
		this.color = color;
	}

	// TODO: convert through getColorComponents instead!
	public void setColor(java.awt.Color color) {
		setColor(new RGBColor(color));
	}

	public Boolean getOverprint() {
		if (style != null)
			style.update();
		return overprint;
	}

	public void setOverprint(Boolean overprint) {
		if (style != null) {
			style.update();
			style.markDirty();
		}
		this.overprint = overprint;
	}
}
