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
 * File created on 14.01.2005.
 */

package com.scriptographer.ai;

/**
 * @author lehni
 * 
 * @jshide
 */
public class SegmentPoint extends Point {
	protected Segment segment;
	protected int index;

	protected SegmentPoint(Segment segment, int index) {
		this.segment = segment;
		this.index = index;
	}

	protected SegmentPoint(Segment segment, int index, Point pt) {
		super(pt);
		this.segment = segment;
		this.index = index;
	}

	protected SegmentPoint(Segment segment, int index, double x, double y) {
		super(x, y);
		this.segment = segment;
		this.index = index;
	}

	public void set(double x, double y) {
		if (segment != null) {
			segment.update(false);
			this.x = x;
			this.y = y;
			// Reset angle
			angle = null;
			segment.markDirty(Segment.DIRTY_POINTS);
		} else {
			super.set(x, y);
		}
	}

	public void setX(double x) {
		set(x, y);
	}

	public void setY(double y) {
		set(x, y);
	}
	
	public double getX() {
		segment.update(false);
		return x;
	}
	
	public double getY() {
		segment.update(false);
		return y;
	}

	/**
	 * This property is only present if the point is an anchor or control point
	 * of a {@link Segment} or a {@link Curve}. In this case, it returns
	 * {@true if it is selected by the user}
	 */
	public boolean isSelected() {
		return segment.isSelected(this);
	}
	
	public void setSelected(boolean selected) {
		segment.setSelected(this, selected);
	}
}
