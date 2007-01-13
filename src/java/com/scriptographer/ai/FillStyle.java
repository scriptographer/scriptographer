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
 * File created on 14.02.2005.
 *
 * $RCSfile$
 * $Author$
 * $Revision$
 * $Date$
 */

package com.scriptographer.ai;

import org.mozilla.javascript.Scriptable;

import com.scriptographer.js.NullToUndefinedWrapper;
import com.scriptographer.js.WrappableObject;
import com.scriptographer.js.WrapperCreator;

public class FillStyle extends WrappableObject implements WrapperCreator {
	protected Color color; 				/* Fill color */
	protected Boolean overprint;			/* Overprint */

	private PathStyle style = null;

	protected FillStyle(PathStyle style) {
		this.style = style;
	}

	protected FillStyle(FillStyle fill, PathStyle style) {
		init(fill.color, fill.overprint);
		this.style = style;
	}

	public FillStyle(FillStyle fill) {
		init(fill.color, fill.overprint);
	}

	public FillStyle(Color color, Boolean overprint) {
		init(color, overprint);
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

	/*
	 * For JDK 1.4
	 */
	public void setOverprint(boolean overprint) {
		setOverprint(new Boolean(overprint));
	}
	
	// wrappable interface


	// wrappable interface

	public Scriptable createWrapper(Scriptable scope, Class staticType) {
		wrapper = new NullToUndefinedWrapper(scope, this, staticType);
		return wrapper;
	}
}
