/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:01:01 $
 */

package com.scriptographer.ai;

public class StrokeStyle {
	protected Color color;					/* Stroke color */
	protected boolean overprint;			/* Overprint - not meaningful if ColorTag is pattern */
	protected float width;					/* Line width */
	protected float dashOffset;				/* Dash dashOffset */
	protected float[] dashArray;			/* Dash array */
	protected short cap;					/* Line cap */
	protected short join;					/* Line join */
	protected float miterLimit;				/* Line miter limit */

	public static final short
		CAP_BUTT = 0,
		CAP_ROUND = 1,
		CAP_SQUARE = 2;

	public static final short
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

	public StrokeStyle(Color color, boolean overprint, float width, float dashOffset, float[] dashArray, short cap, short join, float miterLimit) {
		init(color, overprint, width, dashOffset, dashArray, cap, join, miterLimit);
	}

	protected void setStyle(PathStyle style) {
		this.style = style;
	}

	protected void init(Color color, boolean overprint, float width, float dashOffset, float[] dashArray, short cap, short join, float miterLimit) {
		this.color = color;
		this.overprint = overprint;
		this.width = width;
		this.dashOffset = dashOffset;
		this.setDashArray(dashArray, false);
		this.cap = cap;
		this.join = join;
		this.miterLimit = miterLimit;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
		if (style != null)
			style.markDirty();
	}

	public boolean getOverprint() {
		return overprint;
	}

	public void setOverprint(boolean overprint) {
		this.overprint = overprint;
		if (style != null)
			style.markDirty();
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
		if (style != null)
			style.markDirty();
	}

	public float getDashOffset() {
		return dashOffset;
	}

	public void setDashOffset(float offset) {
		this.dashOffset = offset;
		if (style != null)
			style.markDirty();
	}

	public float[] getDashArray() {
		return dashArray;
	}

	private void setDashArray(float[] array, boolean mark) {
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
		if (mark && style != null)
			style.markDirty();
	}

	public void setDashArray(float[] array) {
		setDashArray(array, true);
	}

	public void setDash(float offset, float[] array) {
		setDashOffset(offset);
		setDashArray(array);
	}

	public short getCap() {
		return cap;
	}

	public void setCap(short cap) {
		this.cap = cap;
		if (style != null)
			style.markDirty();
	}

	public short getJoin() {
		return join;
	}

	public void setJoin(short join) {
		this.join = join;
		if (style != null)
			style.markDirty();
	}

	public float getMiterLimit() {
		return miterLimit;
	}

	public void setMiterLimit(float miterLimit) {
		this.miterLimit = miterLimit;
		if (style != null)
			style.markDirty();
	}
}
