/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id$
 */

package com.scriptographer.ai;

import java.awt.geom.Point2D;

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;

import com.scriptographer.js.WrapperCreator;

/**
 * @author lehni
 */
class SegmentPoint extends Point implements WrapperCreator {
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
		segment.markDirty(Segment.DIRTY_POINTS);
	}

	public void setLocation(double x, double y) {
		segment.update();
		this.x = (float) x;
		this.y = (float) y;
		segment.markDirty(Segment.DIRTY_POINTS);
	}

	public void setLocation(Point pt) {
		segment.update();
		this.x = pt.x;
		this.y = pt.y;
		segment.markDirty(Segment.DIRTY_POINTS);
	}

	public void setLocation(Point2D pt) {
		segment.update();
		this.x = (float) pt.getX();
		this.y = (float) pt.getY();
		segment.markDirty(Segment.DIRTY_POINTS);
	}

	public void setX(float x) {
		segment.update();
		this.x = x;
		segment.markDirty(Segment.DIRTY_POINTS);
	}

	public void setY(float y) {
		segment.update();
		this.y = y;
		segment.markDirty(Segment.DIRTY_POINTS);
	}
	
	public double getX() {
		segment.update();
		return x;
	}
	
	public double getY() {
		segment.update();
		return y;
	}
	
	public boolean isSelected() {
		return segment.isSelected(this);
	}
	
	public void setSelected(boolean selected) {
		segment.setSelected(this, selected);
	}
	
	// wrappable interface

	/**
	 * Wrapper is neeeded so the public fields Point2D.Float.x and
	 * Point2D.Float.y are overriden with the setX and setY setters, in order to
	 * reflect the changes in the underlying AI points
	 */
	class Wrapper extends NativeJavaObject {
		Wrapper(Scriptable scope, SegmentPoint point, Class staticType) {
			super(scope, point, staticType);
		}

		public void put(String name, Scriptable start, Object value) {
			if (javaObject != null) {
				if (name.equals("x"))
					SegmentPoint.this.setX((float) ScriptRuntime.toNumber(value));
				else if (name.equals("y"))
					SegmentPoint.this.setY((float) ScriptRuntime.toNumber(value));
				else
					super.put(name, start, value);
			}
		}
	}

	public Scriptable createWrapper(Scriptable scope, Class staticType) {
		wrapper = new Wrapper(scope, this, staticType);
		return wrapper;
	}
}
