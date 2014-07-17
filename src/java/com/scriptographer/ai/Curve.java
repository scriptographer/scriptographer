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
 * File created on 14.12.2004.
 */

package com.scriptographer.ai;

import java.util.ArrayList;

import com.scratchdisk.script.ArgumentReader;
import com.scratchdisk.script.ChangeReceiver;

/**
 * @author lehni
 */
public class Curve implements ChangeReceiver {
	private SegmentList segments = null;
	private int index1;
	private int index2;
	private Segment segment1;
	private Segment segment2;

	protected static final double EPSILON = 10e-5;

	public Curve() {
		segment1 = new Segment();
		segment2 = new Segment();
	}
	
	public Curve(Segment segment1, Segment segment2) {
		this.segment1 = new Segment(segment1);
		this.segment2 = new Segment(segment2);
	}

	public Curve(Curve curve) {
		this(curve.segment1, curve.segment2);
	}

	public Curve(Point point1, Point handle1, Point handle2, Point point2) {
		segment1 = new Segment(point1, null, handle1);
		segment2 = new Segment(point2, handle2, null);
	}

	public Curve(Point point1, Point point2) {
		this(point1, null, null, point2);
	}

	public Curve(Point point) {
		this(point, point);
	}

	/**
	 * @jshide
	 */
	public Curve(double p1x, double p1y, double h1x, double h1y,
			double h2x, double h2y, double p2x, double p2y) {
		segment1 = new Segment(p1x, p1y, 0, 0, h1x, h1y);
		segment2 = new Segment(p2x, p2y, h2x, h2y, 0, 0);
	}

	/**
	 * @jshide
	 */
	public Curve(ArgumentReader reader) {
		// First try reading a point, no matter if it is a hash or a array.
		// If that does not work, fall back to other scenarios:
		Point point1 = getPoint(reader, "point1", true);
		if (point1 != null) {
			init(
				point1,
				getPoint(reader, "handle1", false),
				getPoint(reader, "handle2", false),
				getPoint(reader, "point2", false)
			);
		} else if (reader.isArray()) {
			init(
				reader.readDouble(0),
				reader.readDouble(0),
				reader.readDouble(0),
				reader.readDouble(0),
				reader.readDouble(0),
				reader.readDouble(0),
				reader.readDouble(0),
				reader.readDouble(0)
			);		
		}
	}

	protected Curve(SegmentList segmentList, int index) {
		this.segments = segmentList;
		this.index1 = index;
		updateSegments();
	}

	private static Point getPoint(ArgumentReader reader, String name,
			boolean allowNull) {
		Point point = reader.readObject(name, Point.class);
		return allowNull || point != null ? point : new Point();
	}

	protected void init(Point pt1, Point h1, Point h2, Point pt2) {
		segment1 = new Segment(pt1, null, h1);
		segment2 = new Segment(pt2, h2, null);
	}

	protected void init(double p1x, double p1y, double h1x, double h1y,
			double h2x, double h2y, double p2x, double p2y) {
		segment1 = new Segment(p1x, p1y, 0, 0, h1x, h1y);
		segment2 = new Segment(p2x, p2y, h2x, h2y, 0, 0);
	}

	protected void updateSegments() {
		if (segments != null) {
			// Make sure the segments are up to date first. The size might have
			// changed in the meantime.
			segments.update();
			index2 = index1 + 1;
			// A closing curve?
			if (index2 >= segments.size())
				index2 = 0;
			
			// Check whether the segments were moved (others were deleted), the
			// path was updated or the segments even moved to another path.
			// Fetch again if they were:

			if (segment1 == null || segment1.index != index1 ||
					segments != segment1.segments || segments.path != null &&
					segments.path.needsUpdate(segment1.version))
				segment1 = segments.get(index1);

			if (segment2 == null || segment2.index != index2 ||
					segments != segment2.segments || segments.path != null &&
					segments.path.needsUpdate(segment2.version))
				segment2 = segments.get(index2);
		}
	}

	protected void setIndex(int i) {
		index1 = i;
		updateSegments();
	}

	/**
	 * The index of the curve in the {@link Path#getCurves()} array.
	 */
	public int getIndex() {
		return index1;
	}

	/**
	 * The path that the segment belongs to.
	 */
	public Path getPath() {
		return segments != null ? segments.path : null;
	}

	/**
	 * The first anchor point of the curve.
	 */
	public SegmentPoint getPoint1() {
		updateSegments();
		return segment1.point;
	}

	public void setPoint1(Point pt) {
		updateSegments();
		segment1.point.set(pt);
	}

	/**
	 * @jshide
	 */
	public void setPoint1(double x, double y) {
		updateSegments();
		segment1.point.set(x, y);
	}

	/**
	 * The second anchor point of the curve.
	 */
	public SegmentPoint getPoint2() {
		updateSegments();
		return segment2.point;
	}

	public void setPoint2(Point pt) {
		updateSegments();
		segment2.point.set(pt);
	}

	/**
	 * @jshide
	 */
	public void setPoint2(double x, double y) {
		updateSegments();
		segment2.point.set(x, y);
	}
	
	/**
	 * The handle point that describes the tangent in the first anchor point.
	 */
	public SegmentPoint getHandle1() {
		updateSegments();
		return segment1.handleOut;
	}

	public void setHandle1(Point pt) {
		updateSegments();
		segment1.handleOut.set(pt);
	}

	/**
	 * @jshide
	 */
	public void setHandle1(double x, double y) {
		updateSegments();
		segment1.handleOut.set(x, y);
	}

	/**
	 * The handle point that describes the tangent in the second anchor point.
	 */
	public SegmentPoint getHandle2() {
		updateSegments();
		return segment2.handleIn;
	}

	public void setHandle2(Point pt) {
		updateSegments();
		segment2.handleIn.set(pt);
	}

	/**
	 * @jshide
	 */
	public void setHandle2(double x, double y) {
		updateSegments();
		segment2.handleIn.set(x, y);
	}

	/**
	 * The first segment of the curve.
	 */
	public Segment getSegment1() {
		return segment1;
	}

	/**
	 * The second segment of the curve.
	 */
	public Segment getSegment2() {
		return segment2;
	}

	/**
	 * The next curve in the {@link Path#getCurves()} array.
	 */
	public Curve getNext() {
		if (segments != null) {
			if (index1 + 1 < segments.size()) {
				Curve next = segments.get(index1 + 1).getCurve();
				if (next != this)
					return next;
			} else {
				return segments.path != null && segments.path.isClosed()
						? segments.getFirst().getCurve() : null;
			}
		}
		return null;
	}

	/**
	 * The previous curve in the {@link Path#getCurves()} array.
	 */
	public Curve getPrevious() {
		if (segments != null) {
			if (index1 > 0) {
				Curve prev = segments.get(index1 - 1).getCurve();
				if (prev != this)
					return prev;
			} else {
				return segments.path != null && segments.path.isClosed()
						? segments.getLast().getCurve() : null;
			}
		}
		return null;
	}

	public void setSelected(boolean selected) {
		this.getHandle1().setSelected(selected);
		this.getHandle2().setSelected(selected);
	}

	public boolean isSelected() {
		return this.getHandle1().isSelected() && this.getHandle2().isSelected();
	}

	private static native double nativeGetLength(double p1x, double p1y,
			double h1x, double h1y, double h2x, double h2y, double p2x,
			double p2y);

	/**
	 * The approximated length of the curve in points.
	 */
	public double getLength() {
		updateSegments();
		Point point1 = segment1.point;
		Point handle1 = segment1.handleOut;
		Point handle2 = segment2.handleIn;
		Point point2 = segment2.point;
		return nativeGetLength(
				point1.x, point1.y,
				handle1.x + point1.x, handle1.y + point1.y,
				handle2.x + point2.x, handle2.y + point2.y,
				point2.x, point2.y
		);
	}

	public double getLength(double from, double to) {
		updateSegments();
		double[][] curve = getCurveValues();
		return getLength(curve, from, to, curve);
	}

	/**
	 * @deprecated
	 */
	public double getPartLength(double from, double to) {
		return getLength(from, to);
	}

	public Rectangle getControlBounds() {
		updateSegments();
		return getControlBounds(getCurveValues());
	}

	// Ported back from Paper.js in 2014
	private Point evaluate(double t, int type) {
		updateSegments();
		Point point1 = segment1.point;
		Point handle1 = segment1.handleOut;
		Point handle2 = segment2.handleIn;
		Point point2 = segment2.point;
		double p1x = point1.x,
			p1y = point1.y,
			c1x = p1x + handle1.x,
			c1y = p1y + handle1.y,
			p2x = point2.x,
			p2y = point2.y,
			c2x = p2x + handle2.x,
			c2y = p2y + handle2.y,
			tolerance = 10e-6,
			x, y;

		// Handle special case at beginning / end of curve
		if (type == 0 && (t < tolerance || t > 1 - tolerance)) {
			boolean isZero = t < tolerance;
			x = isZero ? p1x : p2x;
			y = isZero ? p1y : p2y;
		} else {
			// Calculate the polynomial coefficients.
			double cx = 3.0 * (c1x - p1x),
				bx = 3.0 * (c2x - c1x) - cx,
				ax = p2x - p1x - cx - bx,

				cy = 3.0 * (c1y - p1y),
				by = 3.0 * (c2y - c1y) - cy,
				ay = p2y - p1y - cy - by;
			if (type == 0) {
				// Calculate the curve point at parameter value t
				x = ((ax * t + bx) * t + cx) * t + p1x;
				y = ((ay * t + by) * t + cy) * t + p1y;
			} else {
				// 1: tangent, 1st derivative
				// 2: normal, 1st derivative
				// 3: curvature, 1st derivative & 2nd derivative
				// Prevent tangents and normals of length 0:
				// http://stackoverflow.com/questions/10506868/
				if (t < tolerance && c1x == p1x && c1y == p1y
						|| t > 1 - tolerance && c2x == p2x && c2y == p2y) {
					x = p2x - p1x;
					y = p2y - p1y;
				} else if (t < tolerance) {
					x = cx;
					y = cy;
				} else if (t > 1 - tolerance) {
					x = 3.0 * (p2x - c2x);
					y = 3.0* (p2y - c2y);
				} else {
					// Simply use the derivation of the bezier function for both
					// the x and y coordinates:
					x = (3.0 * ax * t + 2.0 * bx) * t + cx;
					y = (3.0 * ay * t + 2.0 * by) * t + cy;
				}
				if (type == 3) {
					// Calculate 2nd derivative, and curvature from there:
					// http://cagd.cs.byu.edu/~557/text/ch2.pdf page#31
					// k = |dx * d2y - dy * d2x| / (( dx^2 + dy^2 )^(3/2))
					double x2 = 6.0 * ax * t + 2.0 * bx,
						y2 = 6.0 * ay * t + 2.0 * by;
					// Return curvature as point with x value = curvature...
					return new Point((x * y2 - y * x2)
							/ Math.pow(x * x + y * y, 3.0 / 2.0), 0);
				}
			}
		}
		// The normal is simply the rotated tangent:
		return type == 2 ? new Point(y, -x) : new Point(x, y);
	}

	/*
	 * Instead of using the underlying AI functions and loose time for calling
	 * natives, let's do the dirty work ourselves:
	 */
	/**
	 * Returns the point on the curve at the specified position.
	 * 
	 * @param parameter the position at which to find the point as a value
	 *        between 0 and 1.
	 */
	public Point getPoint(double parameter) {
		return evaluate(parameter, 0);
	}
	
	public Point getTangent(double parameter) {
		return evaluate(parameter, 1);
	}

	public Point getNormal(double parameter) {
		return evaluate(parameter, 2);
	}

	public double getCurvature(double parameter) {
		return evaluate(parameter, 3).x;
	}

	public double getParameter(Point point, double precision) {
		updateSegments();
		return getParameter(getCurveValues(), point.x, point.y,
				precision);
	}

	public double getParameter(Point point) {
		return getParameter(point, EPSILON);
	}

	public double getParameter(double length) {
		updateSegments();
		return getParameter(getCurveValues(), length);
	}

	/**
	 * @deprecated
	 */
	public double getParameterWithLength(double length) {
		return getParameter(length);
	}

	public CurveLocation getLocation(Point point) {
		double param = getParameter(point);
		return param != -1 ? new CurveLocation(this, param, point) : null;
	}

	public CurveLocation getLocation(double length) {
		double param = getParameter(length);
		return param != -1 ? new CurveLocation(this, param, null) : null;
	}

	public CurveLocation[] getIntersections(Curve other) {
		ArrayList<CurveLocation> intersections = new ArrayList<CurveLocation>();
		getIntersections(this, getCurveValues(), other.getCurveValues(),
				intersections);
		return intersections.toArray(new CurveLocation[intersections.size()]);
	}

	/**
	 * Checks if this curve is linear, meaning it does not define any curve
	 * handle.

	 * @return {@true if the curve is linear}
	 */
	public boolean isLinear() {
		updateSegments();
		Point handle1 = segment1.handleOut;
		Point handle2 = segment2.handleIn;
		return handle1.x == 0 && handle1.y == 0
				&& handle2.y == 0 && handle2.y == 0;
	}

	private static native void nativeAdjustThroughPoint(float[] values,
			float x, float y, float parameter);

	public void adjustThroughPoint(Point pt, double parameter) {
		updateSegments();

		float[] values = new float[2 * SegmentList.VALUES_PER_SEGMENT];
		segment1.getValues(values, 0);
		segment2.getValues(values, SegmentList.VALUES_PER_SEGMENT);
		nativeAdjustThroughPoint(values, (float) pt.x, (float) pt.y,
				(float) parameter);
		segment1.setValues(values, 0);
		segment2.setValues(values, SegmentList.VALUES_PER_SEGMENT);
		// Don't mark dirty, commit immediately both as all the values have
		// been modified:
		Path path = getPath();
		if (path != null) {
			path.checkValid();
			SegmentList.nativeSet(path.handle, path.document.handle,
					index1, 2, values);
		}
	}

	/**
	 * Retruns the reversed the curve, without modifying the curve itself.
	 */
	public Curve reverse() {
		updateSegments();
		return new Curve(segment2.reverse(), segment1.reverse());
	}

	/**
	 * Divides the curve into two at the specified position. The curve itself is
	 * modified and becomes the first part, the second part is returned as a new
	 * curve. If the modified curve belongs to a path item, the second part is
	 * added to it.
	 * 
	 * @param parameter the position at which to split the curve as a value
	 *        between 0 and 1 {@default 0.5}
	 * @return the second part of the divided curve
	 * 
	 * @jsoperator none
	 */
	public Curve divide(double parameter) {
		Curve result = null;
		if (parameter > 0 && parameter < 1) {
			updateSegments();
			
			double left[][] = getCurveValues();
			double right[][] = new double[4][];
			// Use faster special algorithm for subdividing in the middle
			if (parameter == 0.5)
				subdivide(left, left, right);
			else
				subdivide(left, parameter, left, right);
	
			// Write back the results:
			segment1.handleOut.set(left[1][0] - segment1.point.x,
					left[1][1] - segment1.point.y);
			
			// segment2 is the end segment. By inserting newSegment
			// between segment1 and 2, 2 becomes the end segment.
			// absolute->relative
			segment2.handleIn.set(right[2][0] - segment2.point.x,
					right[2][1] - segment2.point.y);

			// Create the new segment, absolute -> relative:
			double x = left[3][0];
			double y = left[3][1];
			Segment newSegment = new Segment(x, y,
					left[2][0] - x, left[2][1] - y,
					right[1][0] - x, right[1][1] - y);
	
			// Insert it in the segments list, if needed:
			if (segments != null) {
				// Insert at the end if this curve is a closing curve
				// of a closed path, since otherwise it would be inserted
				// at 0
				if (index1 > 0 && index2 == 0) {
					segments.add(newSegment);
				} else {
					segments.add(index2, newSegment);
				}
				updateSegments();
				// if this curve is linked to a path, get the new curve there
			}
			// We need a path to be able to access curves
			if (segments != null && segments.path != null) {
				result = getNext();
				// If it's the last one in a closed path, return the first curve
				// instead
				if (result == null)
					result = segments.getFirst().getCurve();
			} else {
				// otherwise create it from the result of split
				Segment endSegment = segment2;
				segment2 = newSegment;
				result = new Curve(newSegment, endSegment);
			}
		}
		return result;
	}

	public Curve divide() {
		return divide(0.5f);
	}

	/**
	 * Splits the curve at the given parameter. If this curve is part of a path,
	 * it executes {@link Path#split(int, double)}, otherwise
	 * {@link #divide(double)}.
	 */
	public Curve split(double parameter) {
		Path path = getPath();
		if (path != null) {
			Path newPath = path.split(index1, parameter);
			return newPath.getCurves().getFirst();
		} else {
			return divide(parameter);
		}
	}

	public Object clone() {
		updateSegments();
		return new Curve(segment1, segment2);
	}

	public String toString() {
		updateSegments();
		StringBuffer buf = new StringBuffer(64);
		buf.append("{ point1: ").append(segment1.point.toString());
		if (segment1.handleOut.x != 0 || segment1.handleOut.y != 0)
			buf.append(", handle1: ").append(segment1.handleOut.toString());
		if (segment2.handleIn.x != 0 || segment2.handleIn.y != 0)
			buf.append(", handle2: ").append(segment2.handleIn.toString());
		buf.append(", point2: ").append(segment2.point.toString());
		buf.append(" }");
		return buf.toString();
	}
	
	/*
	 * Low Level Math functions for curve subdivision, calculation of roots, etc
	 */

	protected double[][] getCurveValues() {
		Point point1 = segment1.point;
		Point handle1 = segment1.handleOut;
		Point handle2 = segment2.handleIn;
		Point point2 = segment2.point;
		return new double[][] {
				{ point1.x, point1.y },
				{ point1.x + handle1.x, point1.y + handle1.y },
				{ point2.x + handle2.x, point2.y + handle2.y },
				{ point2.x, point2.y }
		};
	}

	/**
	 * Curve subdivision at t = 0.5.
	 */
	protected static void subdivide(double[][] curve, double[][] left,
			double[][] right) {
		double b0_x = curve[0][0];
		double b0_y = curve[0][1];
		double b1_x = curve[1][0];
		double b1_y = curve[1][1];
		double b2_x = curve[2][0];
		double b2_y = curve[2][1];
		double b3_x = curve[3][0];
		double b3_y = curve[3][1];
		double c_x = (b1_x + b2_x) / 2.0;
		double c_y = (b1_y + b2_y) / 2.0;
		b1_x = (b0_x + b1_x) / 2.0;
		b1_y = (b0_y + b1_y) / 2.0;
		b2_x = (b3_x + b2_x) / 2.0;
		b2_y = (b3_y + b2_y) / 2.0;
		double mb1c_x = (b1_x + c_x) / 2.0;
		double mb1c_y = (b1_y + c_y) / 2.0;
		double mb2c_x = (b2_x + c_x) / 2.0;
		double mb2c_y = (b2_y + c_y) / 2.0;
		c_x = (mb1c_x + mb2c_x) / 2.0;
		c_y = (mb1c_y + mb2c_y) / 2.0;
		if (left != null) {
			left[0] = new double[] { b0_x, b0_y };
			left[1] = new double[] { b1_x, b1_y };
			left[2] = new double[] { mb1c_x, mb1c_y };
			left[3] = new double[] { c_x, c_y };
		}
		if (right != null) {
			right[0] = new double[] { c_x, c_y };
			right[1] = new double[] { mb2c_x, mb2c_y };
			right[2] = new double[] { b2_x, b2_y };
			right[3] = new double[] { b3_x, b3_y };
		}
	}

	/**
	 * Curve subdivision at an arbitrary value for t.
	 */
	protected static void subdivide(double[][] curve, double t, double[][] left,
			double[][] right) {
		double temp[][][] = new double[4][][];

		// Copy control points
		temp[0] = curve;

		for (int i = 1; i < 4; i++) {
			temp[i] = new double[][] {
				{0, 0}, {0, 0}, {0, 0}, {0, 0}
			};
		}

		// Triangle computation
		double u = 1f - t;
		for (int i = 1; i < 4; i++) {
			double[][] row1 = temp[i];
			double[][] row2 = temp[i - 1];
			for (int j = 0 ; j < 4 - i; j++) {
				double[] pt1 = row1[j];
				double[] pt2 = row2[j];
				double[] pt3 = row2[j + 1];
				pt1[0] = u * pt2[0] + t * pt3[0];
				pt1[1] = u * pt2[1] + t * pt3[1];
			}
		}
		
		// Only write back left curve if it's not overwritten by right
		// afterwards
		if (left != null) {
			left[0] = temp[0][0];
			left[1] = temp[1][0];
			left[2] = temp[2][0];
			left[3] = temp[3][0];
		}

		// Curve automatically contains left result, through temp[0],
		// write right result into right:
		if (right != null) {
			right[0] = temp[3][0];
			right[1] = temp[2][1];
			right[2] = temp[1][2];
			right[3] = temp[0][3];
		}
	}

	protected static double getLength(double curve[][]) {
		return nativeGetLength(
				curve[0][0], curve[0][1],
				curve[1][0], curve[1][1],
				curve[2][0], curve[2][1],
				curve[3][0], curve[3][1]
		);
	}

	protected static void getIntersections(Curve curve, double[][] curve1,
			double[][] curve2, ArrayList<CurveLocation> intersections) {
		boolean debug = false;
		if (debug) {
			Path rect = Document.getActiveDocument().createRectangle(
					getControlBounds(curve1));
			rect.setStrokeColor(java.awt.Color.green);
			rect.setStrokeWidth(0.1f);

			rect = Document.getActiveDocument().createRectangle(
					getControlBounds(curve2));
			rect.setStrokeColor(java.awt.Color.red);
			rect.setStrokeWidth(0.1f);
		}
		Rectangle bounds1 = getControlBounds(curve1);
		Rectangle bounds2 = getControlBounds(curve2);
		// We are not using Rectangle#intersects() here, since in order to
		// detect intersections that lie on curve bounds, we need to consider
		// touching on one side of the tested rectangles as intersection as well
		// If touch is condired at both sides, solutions lying on the border of
		// bounds would turn up twice.
		if (bounds1.x + bounds1.width >= bounds2.x
				&& bounds1.y + bounds1.height >= bounds2.y
				&& bounds1.x < bounds2.x + bounds2.width
				&& bounds1.y < bounds2.y + bounds2.height) {
			if (isFlatEnough(curve1) && isFlatEnough(curve2)) {
				// Treat both curves as lines and see if their parametric
				// equations interesct.
				if (debug) {
					Path line = Document.getActiveDocument().createLine(
							new Point(curve1[0][0], curve1[0][1]),
							new Point(curve1[3][0], curve1[3][1]));
					line.setStrokeColor(java.awt.Color.green);
					line.setStrokeWidth(0.1f);

					line = Document.getActiveDocument().createLine(
							new Point(curve2[0][0], curve2[0][1]),
							new Point(curve2[3][0], curve2[3][1]));
					line.setStrokeColor(java.awt.Color.red);
					line.setStrokeWidth(0.1f);
				}
				Point point = Line.intersect(
						curve1[0][0], curve1[0][1],
						curve1[3][0], curve1[3][1], false,
						curve2[0][0], curve2[0][1],
						curve2[3][0], curve2[3][1], false);
				// We need to provide the original left curve reference to the
				// #getIntersections() calls as it is required for the returned
				// CurveLocation instances.
				// Passing -1 for parameter leads to lazy determination of
				// parameter values in CurveLocation#getParameter() only once
				// they are requested. This allows the use of CurveLocation
				// without slow-downs in comparisson to simply returning points
				// as long as only CurveLocation#getPoint() is called.
				if (point != null)
					intersections.add(new CurveLocation(curve, -1, point));
			} else {
				double curve1Left[][] = new double[4][];
				double curve1Right[][] = new double[4][];
				double curve2Left[][] = new double[4][];
				double curve2Right[][] = new double[4][];
				subdivide(curve1, curve1Left, curve1Right);
				subdivide(curve2, curve2Left, curve2Right);
				getIntersections(curve, curve1Left, curve2Left, intersections);
				getIntersections(curve, curve1Left, curve2Right, intersections);
				getIntersections(curve, curve1Right, curve2Left, intersections);
				getIntersections(curve, curve1Right, curve2Right, intersections);
			}
		}
	}

	private static boolean isFlatEnough(double[][] curve) {
		// Thanks to Kaspar Fischer for the following:
		// http://www.inf.ethz.ch/personal/fischerk/pubs/bez.pdf
		double p1x = curve[0][0];
		double p1y = curve[0][1];
		double c1x = curve[1][0];
		double c1y = curve[1][1];
		double c2x = curve[2][0];
		double c2y = curve[2][1];
		double p2x = curve[3][0];
		double p2y = curve[3][1];
		double ux = 3 * c1x - 2 * p1x - p2x;
		double uy = 3 * c1y - 2 * p1y - p2y;
		double vx = 3 * c2x - 2 * p2x - p1x;
		double vy = 3 * c2y - 2 * p2y - p1y;
		return Math.max(ux * ux, vx * vx) + Math.max(uy * uy, vy * vy) < 1;
	}

	private static Rectangle getControlBounds(double[][] curve) {
		double minX = curve[0][0], maxX = minX, minY = curve[0][1], maxY = minY;
		for (int i = 1; i < 4; i++) {
			double[] c = curve[i];
			double x = c[0], y = c[1];
			if (x < minX)
				minX = x;
			else if (x > maxX)
				maxX = x;
			if (y < minY)
				minY = y;
			else if (y > maxY)
				maxY = y;
		}
		return new Rectangle(minX, minY, maxX - minX, maxY - minY);
	}

	protected static double getParameter(double[][] curve, double length) {
		// TODO: Port root finding algorithm from Paper.js back
		if (length <= 0)
			return 0;
		double bezierLength = getLength(curve);
		if (length >= bezierLength)
			return 1;
		double[][] temp = new double[4][];
		// Let's use the False Position method to find the right length in 
		// few iterations. Generally only 4 - 7 are required.
		double left = 0;
		double right = 1;
		double error = 5e-15;
		double fLeft = getLeftLength(curve, left, temp) - length;
		double fRight = getLeftLength(curve, right, temp) - length;
		double res = 0;
		int n = 0, side = 0;
		do {
			res = (fLeft * right - fRight * left) / (fLeft - fRight);
			if (Math.abs(right - left) < error * Math.abs(right + left))
				break;
			double fRes = getLeftLength(curve, res, temp) - length;
			if (fRes * fRight > 0) {
				right = res; fRight = fRes;
				if (side == -1)
					fLeft /= 2;
				side = -1;
			} else if (fLeft * fRes > 0) {
				left = res;  fLeft = fRes;
				if (side == +1)
					fRight /= 2;
				side = +1;
			} else {
				break;
			}
		} while(n++ < 100);
//		System.out.println("n: " + n + "r: " + res + "f(r): "
//				+ getLeftLength(curve, res, temp) + ", len: " + length);
		return res;
	}

	private static double getLeftLength(double curve[][], double parameter,
			double tempCurve[][]) {
		if (parameter == 0)
			return 0;
		if (parameter < 1) {
			subdivide(curve, parameter, tempCurve, null);
			curve = tempCurve;
		}
		return getLength(curve);
	}

	/**
	 * Curve is only modified if it is passed as tempCurve as well. this is
	 * needed in #getLength() above...
	 * 
	 * TODO: Port Gauss-Legendre Numerical Integration method from Paper.js
	 * to Scriptographer and stop relying on internal Adobe code.
	 */
	private static double getLength(double curve[][], double from, double to,
			double tempCurve[][]) {
		if (from > to) {
			double temp = from;
			from = to;
			to = temp;
		} else if (from == to) {
			return 0;
		}

		if (from < 0)
			from = 0;

		if (to > 1)
			to = 1;

		return getLeftLength(curve, to, tempCurve)
			- getLeftLength(curve, from, tempCurve);
	}

	protected static double getParameter(double[][] curve, double x,
			double y, double epsilon) {
		double txs[] = { 0, 0, 0 }; 
		double tys[] = { 0, 0, 0 };

		// Handle beginnings and end seperately, as they are not detected
		// sometimes.
		if (Math.abs(curve[0][0] - x) < epsilon
				&& Math.abs(curve[0][1] - y) < epsilon)
			return 0;
		if (Math.abs(curve[3][0] - x) < epsilon
				&& Math.abs(curve[3][1] - y) < epsilon)
			return 1;

		int sx = solveCubicRoots(curve[0][0], curve[1][0], curve[2][0],
				curve[3][0], x, txs, epsilon);
		int sy = solveCubicRoots(curve[0][1], curve[1][1], curve[2][1],
				curve[3][1], y, tys, epsilon);

		int cx = 0;
		// sx, sy == -1 means infinite solutions:
		while (cx < sx || sx == -1) {
			double tx = txs[cx++];
			if (tx >= 0 && tx <= 1.0 || sx == -1) {
				int cy = 0;
				while (cy < sy || sy == -1) {
					double ty = tys[cy++];
					if (ty >= 0 && ty <= 1.0 || sy == -1) {
						if (sx == -1) tx = ty;
						else if (sy == -1) ty = tx;
						if (Math.abs(tx - ty) < epsilon) { // tolerance
							return (tx + ty) * 0.5;
						}
					}
				}
				// avoid endless loops here:
				// if sx is infinite and there was no fitting ty, there's no
				// solution for this bezier
				if (sx == -1)
					sx = 0; 
			}
		}
		return -1;
	}

	private static int solveQuadraticRoots(double a, double b, double c,
			double roots[], double tolerance) {
		// After Numerical Recipes in C, 2nd edition, Press et al.,
		// 5.6, Quadratic and Cubic Equations
		// If problem is actually linear, return 0 or 1 easy roots
		if (Math.abs(a) < tolerance) {
			if (Math.abs(b) >= tolerance) {
				roots[0] = -c / b;
				return 1;
			}
			// If all the coefficients are 0, infinite values are
			// possible!
			if (Math.abs(c) < tolerance)
				return -1; // Infinite solutions
			return 0; // 0 solutions
		}
		double q = b * b - 4 * a * c;
		if (q < 0)
			return 0; // 0 solutions
		q = Math.sqrt(q);
		if (b < 0)
			q = -q;
		q = (b + q) * -0.5;
		int n = 0;
		if (Math.abs(q) >= tolerance)
			roots[n++] = c / q;
		if (Math.abs(a) >= tolerance)
			roots[n++] = q / a;
		return n; // 0, 1 or 2 solutions
	}

	private static int solveCubicRoots(double a, double b, double c, double d,
			double roots[], double tolerance) {
		// After Numerical Recipes in C, 2nd edition, Press et al.,
		// 5.6, Quadratic and Cubic Equations
		if (Math.abs(a) < tolerance)
			return solveQuadraticRoots(b, c, d, roots, tolerance);
		// Normalize
		b /= a;
		c /= a;
		d /= a;
		// Compute discriminants
		double Q = (b * b - 3 * c) / 9,
			R = (2 * b * b * b - 9 * b * c + 27 * d) / 54,
			Q3 = Q * Q * Q,
			R2 = R * R;
		b /= 3; // Divide by 3 as that's required below
		if (R2 < Q3) { // Three real roots
			// This sqrt and division is safe, since R2 >= 0, so Q3 > R2,
			// so Q3 > 0.  The acos is also safe, since R2/Q3 < 1, and
			// thus R/sqrt(Q3) < 1.
			double theta = Math.acos(R / Math.sqrt(Q3)),
				// This sqrt is safe, since Q3 >= 0, and thus Q >= 0
				q = -2 * Math.sqrt(Q);
			roots[0] = q * Math.cos(theta / 3) - b;
			roots[1] = q * Math.cos((theta + 2 * Math.PI) / 3) - b;
			roots[2] = q * Math.cos((theta - 2 * Math.PI) / 3) - b;
			return 3;
		} else { // One real root
			double A = -Math.pow(Math.abs(R) + Math.sqrt(R2 - Q3), 1 / 3);
			if (R < 0) A = -A;
			double B = (Math.abs(A) < tolerance) ? 0 : Q / A;
			roots[0] = (A + B) - b;
			return 1;
		}
	}

	private static int solveCubicRoots(double v1, double v2, double v3,
			double v4, double v, double roots[], double epsilon) {
		// conversion from the point coordinates (v1 .. v4) to the polynomial
		// coefficients:
		double v1m3 = 3.0 * v1;
		double v2m3 = 3.0 * v2;
		double v3m3 = 3.0 * v3;

		double a = v4 - v3m3 + v2m3        - v1;
		double b =      v3m3 - v2m3 - v2m3 + v1m3;
		double c =             v2m3        - v1m3;
		double d =                           v1 - v;

		return solveCubicRoots(a, b, c, d, roots, epsilon);
	}
}
