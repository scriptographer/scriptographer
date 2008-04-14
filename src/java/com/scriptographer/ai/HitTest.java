/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 11.01.2005.
 *
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnumUtils;

/**
 * HitTest objects are returned by {@link Document#hitTest} and
 * {@link Path#hitTest}. They represent the result of a hit test, which is
 * reflected in the object's properties as described bellow.
 * 
 * @author lehni
 */
public class HitTest {
	protected static final float DEFAULT_TOLERANCE = 2.0f;
	
	private HitType type;
	private Curve curve;
	private Item item;
	private Point point;
	private float parameter;

	/**
	 * 
	 * @param type HitTest.HIT_*
	 * @param curve
	 * @param parameter
	 * @param point
	 */
	protected HitTest(HitType type, Curve curve, float parameter, Point point) {
		this.type = type;
		this.curve = curve;
		this.item = curve.getPath();
		this.parameter = parameter;
		this.point = point;
	}

	protected HitTest(Curve curve, float parameter) {
		// passing null for point only calls curve.getPoint(t) if the point is requested, see HitTest
		this(
			parameter > 0 && parameter < 1 ? HitType.CURVE : HitType.ANCHOR,
			curve,
			parameter,
			null
		);
	}
	
	/**
	 * To be called from the native environment
	 */
	protected HitTest(int type, Item item, int index, float parameter, Point point) {
		this.type = IntegerEnumUtils.get(HitType.class, type);
		this.item = item;
		this.parameter = parameter;
		this.point = point;
		this.curve = null;
		if (item instanceof Path && type < HitType.FILL.value) {
			Path path = (Path) item;
			CurveList curves = path.getCurves();
			// calculate the curve index in the curve list according to the segment index:
			// curve = segment - 1, if curve < 0, curve += segmentCount
			index--;
			if (index < 0)
				index += curves.size();
			if (index < curves.size()) {
				this.curve = (Curve) curves.get(index);
				// if parameter == -1 and index is valid, we're hitting
				// a segment point. just set parameter to 0 and the
				// curve / parameter pair is valid
				if (parameter == -1)
					this.parameter = 0;
			}
		}
	}

	/**
	 * Returns the item which was hit.
	 * @return
	 */
	public Item getItem() {
		return item;
	}

	public Curve getCurve() {
		return curve;
	}
	
	public int getCurveIndex() {
		if (curve != null)
			return curve.getIndex();
		else
			return -1;
	}
	
	public float getParameter() {
		return parameter;
	}
	
	public Point getPoint() {
		if (point == null && curve != null)
			point = curve.getPoint(parameter);
		return point;
	}
	
	public HitType getType() {
		return type;
	}
	
	public String toString() {
		return " { type: " + this.type + ", item: " + item + ", index: " + getCurveIndex() + ", parameter: " + parameter + " }";
	}
}
