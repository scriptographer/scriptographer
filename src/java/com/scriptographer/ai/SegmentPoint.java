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
 * File created on 14.01.2005.
 *
 * $RCSfile: SegmentPoint.java,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/10/19 02:48:17 $
 */

package com.scriptographer.ai;

import java.awt.geom.Point2D;

public class SegmentPoint extends Point {
	protected Segment segment;
	protected int index;

	protected SegmentPoint(Segment listener, int index) {
		this.segment = listener;
		this.index = index;
	}

	protected SegmentPoint(Segment listener, int index, Point2D pt) {
		super(pt);
		this.segment = listener;
		this.index = index;
	}

	protected SegmentPoint(Segment listener, int index, float x, float y) {
		super(x, y);
		this.segment = listener;
		this.index = index;
	}

	public void setLocation(float x, float y) {
		segment.update();
		this.x = x;
		this.y = y;
		segment.markDirty();
	}

	public void setLocation(double x, double y) {
		segment.update();
		this.x = (float) x;
		this.y = (float) y;
		segment.markDirty();
	}

	public void setLocation(Point pt) {
		segment.update();
		this.x = pt.x;
		this.y = pt.y;
		segment.markDirty();
	}

	public void setLocation(Point2D pt) {
		segment.update();
		this.x = (float) pt.getX();
		this.y = (float) pt.getY();
		segment.markDirty();
	}

	public void setX(float x) {
		segment.update();
		this.x = x;
		segment.markDirty();
	}

	public void setY(float y) {
		segment.update();
		this.y = y;
		segment.markDirty();
	}
	
	public double getX() {
		segment.update();
		return x;
	}
	
	public double getY() {
		segment.update();
		return y;
	}
}
