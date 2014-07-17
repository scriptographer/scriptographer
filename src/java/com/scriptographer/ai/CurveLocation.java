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
 * File created on Jun 11, 2010.
 */

package com.scriptographer.ai;

/**
 * CurveLocation objects describe a location on {@Curve} objects, as
 * defined by the curve {@link #getParameter()}, a value between {@code 0}
 * (beginning of the curve) and {@code 1} (end of the curve). If the curve is
 * part of a {@link Path} item, its {@link #getIndex()} inside the
 * {@link Path#getCurves()} list is also provided.
 * 
 * The class is in use in many places, such as {@link Path#getLocation(double)},
 * {@link Path#getPoint(double)}, {@link Path#split(CurveLocation)},
 * {@link PathItem#getIntersections(PathItem)}, etc.
 * 
 * @author lehni
 */
public class CurveLocation {
	private Curve curve;
	private double parameter;
	private Point point;
	private Segment segment;

	protected CurveLocation() {
	}

	protected CurveLocation(Curve curve, double parameter, Point point) {
		init(curve, parameter, point);
	}

	public CurveLocation(Curve curve, double parameter) {
		init(curve, parameter, null);
	}

	public CurveLocation(Path path, int index, double parameter) {
		init(path.getCurves().get(index), parameter, null);
	}

	protected void init(Curve curve, double parameter, Point point) {
		this.curve = curve;
		this.parameter = parameter;
		this.point = point;
	}

	/**
	 * The segment of the curve which is closer to the described location.
	 */
	public Segment getSegment() {
		if (segment == null) {
			// Determine the segment closest to the hit point
			Double parameter = getParameter();
			if (parameter == null) {
				return null;
			} else if (parameter == 0) {
				segment = curve.getSegment1();
			} else if (parameter == 1) {
				segment = curve.getSegment2();
			} else {
				// Determine the closest segment by comparing curve lengths
				segment = curve.getLength(0, parameter) 
					< curve.getLength(parameter, 1)
						? curve.getSegment1()
						: curve.getSegment2();
			}
		}
		return segment;
	}

	/**
	 * The curve by which the location is defined.
	 */
	public Curve getCurve() {
		return curve;
	}

	/**
	 * The item this curve belongs to, if any.
	 */
	public Item getItem() {
		return curve != null ? curve.getPath() : null;
	}

	/**
	 * The index of the curve within the {@link Path#getCurves()} list, if the
	 * curve is part of a {@link Path} item.
	 */
	public Integer getIndex() {
		return curve != null ? curve.getIndex() : null;
	}

	/**
	 * The length of the path from its beginning up to the location described
	 * by this object.
	 */
	public Double getOffset() {
		if (curve != null) {
			Path path = curve.getPath();
			if (path != null)
				return path.getOffset(this);
		}
		return null;
	}

	/**
	 * @deprecated
	 */
	public Double getLength() {
		return getOffset();
	}

	/**
	 * The length of the curve from its beginning up to the location described
	 * by this object.
	 */
	public Double getCurveOffset() {
		if (curve != null) {
			Double parameter = getParameter();
			if (parameter != null)
				return curve.getLength(0, parameter);
		}
		return null;
	}

	/**
	 * @deprecated
	 */
	public Double getCurveLength() {
		return getCurveOffset();
	}
	
	/**
	 * The curve parameter, as used by various bezier curve calculations. It is
	 * value between {@code 0} (beginning of the curve) and {@code 1} (end of
	 * the curve).
	 */
	public Double getParameter() {
		if (parameter == -1 && point != null && curve != null)
			parameter = curve.getParameter(point);
		return parameter != -1 ? parameter : null;
	}
	
	/**
	 * The point which is defined by the {@link #getCurve()} and
	 * {@link #getParameter()}.
	 */
	public Point getPoint() {
		if (point == null && curve != null) {
			Double parameter = getParameter();
			if (parameter != null)
				point = curve.getPoint(parameter);
		}
		return point;
	}

	/**
	 * The tangential vector to the {@link #getCurve()} at the given location.
	 */
	public Point getTangent() {
		Double parameter = getParameter();
		return parameter != null && curve != null
				? curve.getTangent(parameter)
				: null;
	}

	/**
	 * The normal vector to the {@link #getCurve()} at the given location.
	 */
	public Point getNormal() {
		Double parameter = getParameter();
		return parameter != null && curve != null
				? curve.getNormal(parameter)
				: null;
	}

	public double getCurvature() {
		Double parameter = getParameter();
		return parameter != null && curve != null
				? curve.getCurvature(parameter)
				: null;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(32);
		Point point = getPoint();
		if (point != null)
			buf.append(", point: ").append(getPoint());
		Integer index = getIndex();
		if (index != null)
			buf.append(", index: ").append(index);
		Double parameter = getParameter();
		if (parameter != null)
			buf.append(", parameter: ").append(parameter);
		// Replace the first ',' with a '{', no matter which one came first.
		buf.setCharAt(0, '{');
		buf.append(" }");
		return buf.toString();
	}

	public boolean equals(Object object) {
		if (object instanceof CurveLocation) {
			CurveLocation loc = (CurveLocation) object;
			return getPoint().equals(loc.getPoint());
		}
		return false;
	}
}
