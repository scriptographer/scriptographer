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

import com.scratchdisk.util.IntegerEnumUtils;

/**
 * @author lehni
 * 
 * @jshide
 */
public class StrokeStyle implements Style {
	/*
	 * Setting these fields to null means undefined.
	 * Setting color to Color.NONE means defined, but style is 
	 * deactivated
	 */
	protected Color color;				/* Stroke color */
	protected Boolean overprint;		/* Overprint - not meaningful if ColorTag is pattern */
	protected Float width;				/* Line width */
	protected Float dashOffset;		/* Dash dashOffset */
	protected float[] dashArray;		/* Dash array */
	protected StrokeCap cap;			/* Line cap */
	protected StrokeJoin join;			/* Line join */
	protected Float miterLimit;		/* Line miter limit */

	private PathStyle style = null;

	protected StrokeStyle(PathStyle style) {
		this.style = style;
	}

	protected StrokeStyle(StrokeStyle stroke, PathStyle style) {
		this(stroke);
		this.style = style;
	}

	public StrokeStyle(StrokeStyle stroke) {
		if (stroke != null) {
			init(stroke.color, stroke.overprint, stroke.width,
					stroke.cap, stroke.join, stroke.miterLimit, 
					stroke.dashOffset, stroke.dashArray);
		}
	}

	public StrokeStyle(Color color, Boolean overprint, Float width,
			StrokeCap cap, StrokeJoin join, Float miterLimit,
			Float dashOffset, float[] dashArray) {
		init(color, overprint, width, cap, join, miterLimit,
				dashOffset, dashArray);
	}

	/**
	 * called from the native environment
	 */
	protected StrokeStyle(Color color, boolean hasColor, short overprint,
			float width, int cap, int join, float miterLimit,
			float dashOffset, float[] dashArray) {
		init(color, hasColor, overprint, width, cap, join, miterLimit,
				dashOffset, dashArray);
	}

	protected void init(Color color, Boolean overprint, Float width,
			StrokeCap cap, StrokeJoin join, Float miterLimit,
			Float dashOffset, float[] dashArray) {
		this.color = color;
		this.overprint = overprint;
		this.width = width;
		this.cap = cap;
		this.join = join;
		this.miterLimit = miterLimit;
		this.dashOffset = dashOffset;
		this.setDashArray(dashArray, false);
	}

	protected void setStyle(PathStyle style) {
		this.style = style;
	}

	/**
	 * called from the native environment
	 */
	protected void init(Color color, boolean hasColor, short overprint,
			float width, int cap, int join, float miterLimit,
			float dashOffset, float[] dashArray) {
		this.color = hasColor && color == null ? Color.NONE : color;
		this.overprint = overprint >= 0 ? new Boolean(overprint != 0) : null;
		this.width = width >= 0 ? new Float(width) : null;
		this.cap = IntegerEnumUtils.get(StrokeCap.class, cap);
		this.join = IntegerEnumUtils.get(StrokeJoin.class, join);
		this.miterLimit = miterLimit >= 0 ? new Float(miterLimit) : null;
		this.dashOffset = dashOffset >= 0 ? new Float(dashOffset) : null;
		this.setDashArray(dashArray, false);
	}
	
	protected void initNative(int handle) {
		PathStyle.nativeInitStrokeStyle(handle, 
				color != null && color != Color.NONE ? color : null, color != null, 
				overprint != null ? (short) (overprint.booleanValue() ? 1 : 0) : -1,
				width != null ? width.floatValue() : -1,
				cap != null ? cap.value : -1,
				join != null ? join.value : -1,
				miterLimit != null ? miterLimit.floatValue() : -1,
				dashOffset != null ? dashOffset.floatValue() : -1,
				dashArray
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
		// Setting with to 0 or null equals to setting color to null!
		if (width == null || width.floatValue() == 0)
			this.color = null;
		this.width = width;
	}

	public StrokeCap getCap() {
		if (style != null)
			style.update();
		return cap;
	}

	public void setCap(StrokeCap cap) {
		if (style != null) {
			style.update();
			style.markDirty();
		}
		this.cap = cap;
	}

	public StrokeJoin getJoin() {
		if (style != null)
			style.update();
		return join;
	}

	public void setJoin(StrokeJoin join) {
		if (style != null) {
			style.update();
			style.markDirty();
		}
		this.join = join;
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
	
	public Float getMiterLimit() {
		if (style != null)
			style.update();
		return miterLimit;
	}

	public void setMiterLimit(Float limit) {
		if (style != null) {
			style.update();
			style.markDirty();
		}
		this.miterLimit = limit;
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
