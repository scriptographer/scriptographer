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
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnumUtils;
import com.scriptographer.script.EnumUtils;

/**
 * HitResult objects are returned by
 * {@link Document#hitTest(Point, HitRequest, float)} and
 * {@link Path#hitTest(Point, HitRequest, float)}. They represent the result of
 * a hit test, which is reflected in the object's properties as described below.
 * 
 * @author lehni
 */
public class HitResult extends CurveLocation {
	protected static final float DEFAULT_TOLERANCE = 2.0f;
	
	private HitType type;
	private Item item;
	private TextRange textRange;

	protected HitResult(HitType type, Curve curve, double parameter,
			Point point) {
		super(curve, parameter, point);
		this.type = type;
		this.item = curve.getPath();
	}

	/**
	 * To be called from the native environment
	 */
	protected HitResult(int docHandle, int type, Item item, int index,
			double parameter, Point point, int textRangeHandle) {
		this.type = IntegerEnumUtils.get(HitType.class, type);
		Curve curve = null;
		if (item instanceof Path && type < HitType.FILL.value) {
			Path path = (Path) item;
			CurveList curves = path.getCurves();
			// If we are between segments or click on the last right one,
			// calculate the curve index in the curve list according to the
			// segment index.
			if (parameter == -1 && index == curves.size()) {
				// Click on the last segment, decrease index and set parameter
				// to the 2nd point.
				index--;
				parameter = 1.0;
			}
			if (parameter > 0.0 && parameter < 1.0) {
				// curve = segment - 1, or if segment = 0, curve = last curve,
				// for closed paths.
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
		init(curve, parameter, point);
		this.item = item;
		// Always wrap textRange even if the user does not request it, so
		// reference gets released in the end through GC.
		if (textRangeHandle != 0) {
			textRange = new TextRange(textRangeHandle,
					Document.wrapHandle(docHandle));
		}
	}

	/**
	 * Describes the type of the hit result.
	 * For example, if you hit an anchor point, the type would be 'anchor'.
	 */
	public HitType getType() {
		return type;
	}

	/**
	 * The item which was hit.
	 */
	public Item getItem() {
		return item;
	}

	/**
	 * The text range which was hit, if any.
	 */
	public TextRange getTextRange() {
		return textRange;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(32);
		buf.append("{ type: ").append(EnumUtils.getScriptName(type)); 
		buf.append(", item: ").append(item);
		Point point = getPoint();
		if (point != null)
			buf.append(", point: ").append(getPoint());
		Curve curve = getCurve();
		if (curve != null)
			buf.append(", index: ").append(getIndex());
		Double parameter = getParameter();
		if (parameter != null)
			buf.append(", parameter: ").append(parameter);
		buf.append(" }");
		return buf.toString();
	}
}
