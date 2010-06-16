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
 * File created on Jun 11, 2010.
 */

package com.scriptographer.ai;


/**
 * @author lehni
 *
 */
public class CurveLocation {
	protected Curve curve;
	private double parameter;
	private Point point;
	private Segment segment;

	protected CurveLocation(Curve curve, double parameter, Point point) {
		this.curve = curve;
		this.parameter = parameter;
		this.point = point;
	}

	public CurveLocation(Curve curve, double parameter) {
		this(curve, parameter, null);
	}

	public CurveLocation(Path path, int index, double parameter) {
		this(path.getCurves().get(index), parameter, null);
	}

	/**
	 * The segment of the curve that was hit and that is closer to the hit
	 * point.
	 */
	public Segment getSegment() {
		if (segment == null) {
			// Determine the segment closest to the hit point
			if (parameter == 0) {
				segment = curve.getSegment1();
			} else if (parameter == 1) {
				segment = curve.getSegment2();
			} else if (parameter == -1) {
				return null;
			} else {
				// Determine the closest segment by comparing curve lengths
				Curve rightCurve = ((Curve) curve.clone()).divide(parameter);
				segment = rightCurve.getLength() > curve.getLength() / 2
						? curve.getSegment1()
						: curve.getSegment2();
			}
		}
		return segment;
	}

	/**
	 * The curve which was hit, if any.
	 */
	public Curve getCurve() {
		return curve;
	}

	/**
	 * The index of the curve which was hit, if any.
	 */
	public int getIndex() {
		if (curve != null)
			return curve.getIndex();
		else
			return -1;
	}
	
	public Double getParameter() {
		if (parameter == -1 && point != null) {
			parameter = curve.getParameter(point);
		}
		return parameter != -1 ? parameter : null;
	}
	
	/**
	 * The point which was hit.
	 */
	public Point getPoint() {
		if (point == null && curve != null)
			point = curve.getPoint(parameter);
		return point;
	}

	/**
	 * The item which was hit.
	 */
	public Item getItem() {
		return curve.getPath();
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(32);
		Point point = getPoint();
		if (point != null)
			buf.append(", point: ").append(getPoint());
		int index = getIndex();
		if (index >= 0)
			buf.append(", index: ").append(index);
		if (parameter != -1)
			buf.append(", parameter: ").append(parameter);
		// Replace the first ',' with a '{', no matter which one came first.
		buf.setCharAt(0, '{');
		buf.append(" }");
		return buf.toString();
	}
}
