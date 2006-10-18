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
 * $RCSfile: StrokeStyle.java,v $
 * $Author: lehni $
 * $Revision: 1.6 $
 * $Date: 2006/10/18 14:17:44 $
 */

package com.scriptographer.ai;

import org.mozilla.javascript.Scriptable;

import com.scriptographer.js.NullToUndefinedWrapper;
import com.scriptographer.js.WrappableObject;
import com.scriptographer.js.WrapperCreator;

public class StrokeStyle extends WrappableObject implements WrapperCreator {
	protected Color color;				/* Stroke color */
	protected Boolean overprint;			/* Overprint - not meaningful if ColorTag is pattern */
	protected Float width;				/* Line width */
	protected Float dashOffset;			/* Dash dashOffset */
	protected float[] dashArray;			/* Dash array */
	protected Integer cap;				/* Line cap */
	protected Integer join;				/* Line join */
	protected Float miterLimit;			/* Line miter limit */

	public static final int
		CAP_BUTT = 0,
		CAP_ROUND = 1,
		CAP_SQUARE = 2;

	public static final int
		JOIN_MITER = 0,
		JOIN_ROUND = 1,
		JOIN_BEVEL = 2;

	private PathStyle style = null;

	protected StrokeStyle(PathStyle style) {
		this.style = style;
	}

	protected StrokeStyle(StrokeStyle stroke, PathStyle style) {
		this(stroke);
		this.style = style;
	}

	public StrokeStyle(StrokeStyle stroke) {
		init(stroke.color, stroke.overprint, stroke.width, stroke.dashOffset, stroke.dashArray, stroke.cap, stroke.join, stroke.miterLimit);
	}

	public StrokeStyle(Color color, Boolean overprint, Float width, Float dashOffset, float[] dashArray, Integer cap, Integer join, Float miterLimit) {
		init(color, overprint, width, dashOffset, dashArray, cap, join, miterLimit);
	}
	
	/**
	 * called from the native environment
	 */
	protected StrokeStyle(Color color, boolean hasColor, short overprint, float width, float dashOffset, float[] dashArray, short cap, short join, float miterLimit) {
		init(color, hasColor, overprint, width, dashOffset, dashArray, cap, join, miterLimit);
	}


	protected void init(Color color, Boolean overprint, Float width, Float dashOffset, float[] dashArray, Integer cap, Integer join, Float miterLimit) {
		this.color = color;
		this.overprint = overprint;
		this.width = width;
		this.dashOffset = dashOffset;
		this.setDashArray(dashArray, false);
		this.cap = cap;
		this.join = join;
		this.miterLimit = miterLimit;
	}

	/**
	 * called from the native environment
	 */
	protected void init(Color color, boolean hasColor, short overprint, float width, float dashOffset, float[] dashArray, short cap, short join, float miterLimit) {
		this.color = hasColor && color == null ? Color.NONE : color;
		this.overprint = overprint >= 0 ? new Boolean(overprint != 0) : null;
		this.width = width >= 0 ? new Float(width) : null;
		this.dashOffset = dashOffset >= 0 ? new Float(dashOffset) : null;
		this.setDashArray(dashArray, false);
		this.cap = cap >= 0 ? new Integer(cap) : null;
		this.join = join >= 0 ? new Integer(join) : null;
		this.miterLimit = miterLimit >= 0 ? new Float(miterLimit) : null;
	}

	protected void setStyle(PathStyle style) {
		this.style = style;
	}
	
	protected void initNative(int handle) {
		PathStyle.nativeInitStrokeStyle(handle, 
				color != null && color != Color.NONE ? color.getComponents() : null, color != null, 
				overprint != null ? (short) (overprint.booleanValue() ? 1 : 0) : -1,
				width != null ? width.floatValue() : -1,
				dashOffset != null ? dashOffset.floatValue() : -1,
				dashArray,
				cap != null ? cap.shortValue() : -1,
				join != null ? join.shortValue() : -1,
				miterLimit != null ? miterLimit.floatValue() : -1
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

	public Float getWidth() {
		if (style != null)
			style.update();
		return width;
	}

	public void setWidth(Float width) {
		if (style != null) {
			style.update();
			style.markDirty();
		}
		// setting with to 0 equals to setting color to null!
		if (width.floatValue() == 0)
			this.color = null;
		this.width = width;
	}
	
	public Float getDashOffset() {
		if (style != null)
			style.update();
		return dashOffset;
	}

	public void setDashOffset(Float offset) {
		if (style != null) {
			style.update();
			style.markDirty();
		}
		this.dashOffset = offset;
	}
	
	public float[] getDashArray() {
		if (style != null)
			style.update();
		return dashArray;
	}

	private void setDashArray(float[] array, boolean sync) {
		if (style != null && sync) {
			style.update();
			style.markDirty();
		}
		if (array == null)
			this.dashArray = null;
		else {
			int count = array.length;
			if (count > 6)
				count = 6;
			this.dashArray = new float[count];
			for (int i = 0; i < count; i++)
				this.dashArray[i] = array[i];
		}
	}

	public void setDashArray(float[] array) {
		setDashArray(array, true);
	}

	public void setDash(float offset, float[] array) {
		setDashOffset(new Float(offset));
		setDashArray(array, false);
	}

	public Integer getCap() {
		if (style != null)
			style.update();
		return cap;
	}

	public void setCap(Integer cap) {
		if (style != null) {
			style.update();
			style.markDirty();
		}
		this.cap = cap;
	}

	public Integer getJoin() {
		if (style != null)
			style.update();
		return join;
	}

	public void setJoin(Integer join) {
		if (style != null) {
			style.update();
			style.markDirty();
		}
		this.join = join;
	}
	
	public Float getMiterLimit() {
		if (style != null)
			style.update();
		return miterLimit;
	}

	public void setMiterLimit(Float miterLimit) {
		if (style != null) {
			style.update();
			style.markDirty();
		}
		this.miterLimit = miterLimit;
	}

	/*
	 * For JDK 1.4
	 */
	public void setOverprint(boolean overprint) {
		setOverprint(new Boolean(overprint));
	}
	
	public void setWidth(float width) {
		setWidth(new Float(width));
	}
	
	public void setDashOffset(float offset) {
		setDashOffset(new Float(offset));
	}
	
	public void setCap(int cap) {
		setCap(new Integer(cap));
	}

	public void setJoin(int join) {
		setJoin(new Integer(join));
	}

	public void setMiterLimit(float miterLimit) {
		setMiterLimit(new Float(miterLimit));
	}

	// wrappable interface

	public Scriptable createWrapper(Scriptable scope, Class staticType) {
		wrapper = new NullToUndefinedWrapper(scope, this, staticType);
		return wrapper;
	}
}
