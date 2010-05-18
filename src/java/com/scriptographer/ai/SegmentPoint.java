/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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
 * File created on 14.01.2005.
 *
 * $Id$
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
		segment.update();
		this.x = x;
		this.y = y;
		// Reset angle
		angle = null;
		segment.markDirty(Segment.DIRTY_POINTS);
	}

	public void setX(double x) {
		set(x, y);
	}

	public void setY(double y) {
		set(x, y);
	}
	
	public double getX() {
		segment.update();
		return x;
	}
	
	public double getY() {
		segment.update();
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
