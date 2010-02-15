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
 * File created on 11.01.2005.
 *
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnumUtils;

/**
 * HitResult objects are returned by {@link Document#hitTest} and
 * {@link Path#hitTest}. They represent the result of a hit test,
 * which is reflected in the object's properties as described below.
 * 
 * @author lehni
 */
public class HitResult {
	protected static final float DEFAULT_TOLERANCE = 2.0f;
	
	private HitType type;
	private Curve curve;
	private Item item;
	private Point point;
	private double parameter;
	private Segment segment = null;

	/**
	 * 
	 * @param type HitTest.HIT_*
	 * @param curve
	 * @param parameter
	 * @param point
	 */
	protected HitResult(HitType type, Curve curve, double parameter, Point point) {
		this.type = type;
		this.curve = curve;
		this.item = curve.getPath();
		this.parameter = parameter;
		this.point = point;
	}

	protected HitResult(Curve curve, double parameter) {
		// passing null for point only calls curve.getPoint(t) if the point is
		// requested, see HitTest
		this(parameter > 0 && parameter < 1 ? HitType.CURVE : HitType.ANCHOR,
				curve, parameter, null);
	}
	
	/**
	 * To be called from the native environment
	 */
	protected HitResult(int type, Item item, int index, double parameter, Point point) {
		this.type = IntegerEnumUtils.get(HitType.class, type);
		curve = null;
		if (item instanceof Path && type < HitType.FILL.value) {
			Path path = (Path) item;
			CurveList curves = path.getCurves();
			// If we are between segments or click on the last right one,
			// calculate the curve index in the curve list according to the
			// segment index.
			if (parameter == -1 && index == curves.size()) {
				// Click on the last segment, decrease index and set paremter to the 2nd point.
				index--;
				parameter = 1.0;
			}
			if (parameter > 0.0 && parameter < 1.0) {
				// curve = segment - 1, or if segment = 0, curve = last curve, for closed paths.
				index = index == 0 ? curves.size() - 1 : index - 1;
			}
			if (index < curves.size()) {
				curve = (Curve) curves.get(index);
				// if parameter == -1 and index is valid, we're hitting
				// a segment point. just set parameter to 0 and the
				// curve / parameter pair is valid
				if (parameter == -1)
					parameter = 0;
			}
		}
		this.item = item;
		this.parameter = parameter;
		this.point = point;
	}

	/**
	 * The item which was hit.
	 */
	public Item getItem() {
		return item;
	}

	/**
	 * The curve which was hit, if any.
	 */
	public Curve getCurve() {
		return curve;
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
	 * The index of the curve which was hit, if any.
	 */
	public int getIndex() {
		if (curve != null)
			return curve.getIndex();
		else
			return -1;
	}
	
	public double getParameter() {
		return parameter;
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
	 * Describes the type of the hit result.
	 * For example, if you hit an anchor point, the type would be 'anchor'.
	 */
	public HitType getType() {
		return type;
	}
	
	public String toString() {
		return " { type: " + this.type + ", item: " + item + ", index: " + getIndex() + ", parameter: " + parameter + " }";
	}
}
