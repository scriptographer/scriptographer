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
 * File created on 14.12.2004.
 *
 * $Id:Curve.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer.ai;

import com.scratchdisk.script.ArgumentReader;
import com.scratchdisk.script.ChangeListener;

/**
 * @author lehni
 */
public class Curve implements ChangeListener {
	protected SegmentList segments = null;
	protected int index1;
	protected int index2;
	private Segment segment1;
	private Segment segment2;
	protected int fetchCount = -1;
	
	protected static final double EPSILON = 0.00001;

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

	private static Point getPoint(ArgumentReader reader, String name, boolean allowNull) {
		Point point = (Point) reader.readObject(name, Point.class);
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
	
	public Object clone() {
		updateSegments();
		return new Curve(segment1, segment2);
	}
	
	/**
	 * The path that the curve belongs to.
	 */
	public Path getPath() {
		return segments.path;
	}

	protected void updateSegments() {
		if (segments != null) {
			index2 = index1 + 1;
			// a closing bezier?
			if (index2 >= segments.size())
				index2 = 0;
			
			// Check whether the segments were moved (others were deleted), the path was updated or the segments even moved to
			// another path. fetch again if they were:

			if (segment1 == null || segment1.index != index1 ||
					segments != segment1.segments || segments.path != null &&
					segments.path.version != segment1.version)
				segment1 = (Segment) segments.get(index1);

			if (segment2 == null || segment2.index != index2 ||
					segments != segment2.segments || segments.path != null &&
					segments.path.version != segment2.version)
				segment2 = (Segment) segments.get(index2);
		}
	}

	/**
	 * The index of the curve in the {@link Path#getCurves()} array.
	 */
	public int getIndex() {
		return index1;
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
		return index1 < segments.size() ? segments.get(index1 + 1).getCurve() : null;
	}
	
	/**
	 * The previous curve in the {@link Path#getCurves()} array.
	 */
	public Curve getPrevious() {
		return index1 > 0 ? segments.get(index1 - 1).getCurve() : null;
	}

	// TODO: return reversed curve as new instance instead of modifiying this curve?
	/**
	 * Reverses the curve.
	 */
	public void reverse() {
		Segment tmp = (Segment) segment1.clone();
		segment1.setHandleIn(segment2.getHandleOut());
		segment1.setHandleOut(segment2.getHandleIn());
		segment1.setPoint(segment2.getPoint());
		segment2.setHandleIn(tmp.getHandleOut());
		segment2.setHandleOut(tmp.getHandleIn());
		segment2.setPoint(tmp.getPoint());
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
		updateSegments();
		// calculate the polynomial coefficients. caution: handles are relative
		// to points
		double dx = segment2.point.x - segment1.point.x;
		double cx = 3f * segment1.handleOut.x;
		double bx = 3f * (dx + segment2.handleIn.x - segment1.handleOut.x) - cx;
		double ax = dx - cx - bx;

		double dy = segment2.point.y - segment1.point.y;
		double cy = 3f * segment1.handleOut.y;
		double by = 3f * (dy + segment2.handleIn.y - segment1.handleOut.y) - cy;
		double ay = dy - cy - by;
		
		return new Point(
			((ax * parameter + bx) * parameter + cx) * parameter + segment1.point.x,
			((ay * parameter + by) * parameter + cy) * parameter + segment1.point.y
		);
	}

	public Point getTangent(double parameter) {
		updateSegments();

		double t = parameter;
		// prevent normals of length 0:
		if (t == 0 && segment1.handleOut.x == 0 && segment1.handleOut.y == 0)
			t = 0.000000000001;
		else if (t == 1 && segment2.handleIn.x == 0 && segment2.handleIn.y == 0)
			t = 0.999999999999;

		// calculate the polynomial coefficients. caution: handles are relative
		// to points
		double dx = segment2.point.x - segment1.point.x;
		double cx = 3.0 * segment1.handleOut.x;
		double bx = 3.0 * (dx + segment2.handleIn.x - segment1.handleOut.x) - cx;
		double ax = dx - cx - bx;

		double dy = segment2.point.y - segment1.point.y;
		double cy = 3.0 * segment1.handleOut.y;
		double by = 3.0 * (dy + segment2.handleIn.y - segment1.handleOut.y) - cy;
		double ay = dy - cy - by;

		// simply use the derivation of the bezier function
		// for both the x and y coordinates:
		return new Point(
				(3.0 * ax * t + 2.0 * bx) * t + cx,
				(3.0 * ay * t + 2.0 * by) * t + cy
		);
	}

	public Point getNormal(double parameter) {
		updateSegments();

		double t = parameter;
		// prevent normals of length 0:
		if (t == 0 && segment1.handleOut.x == 0 && segment1.handleOut.y == 0)
			t = 0.000000000001;
		else if (t == 1 && segment2.handleIn.x == 0 && segment2.handleIn.y == 0)
			t = 0.999999999999;

		// calculate the polynomial coefficients. caution: handles are relative
		// to points
		double dx = segment2.point.x - segment1.point.x;
		double cx = 3.0 * segment1.handleOut.x;
		double bx = 3.0 * (dx + segment2.handleIn.x - segment1.handleOut.x) - cx;
		double ax = dx - cx - bx;

		double dy = segment2.point.y - segment1.point.y;
		double cy = 3.0 * segment1.handleOut.y;
		double by = 3.0 * (dy + segment2.handleIn.y - segment1.handleOut.y) - cy;
		double ay = dy - cy - by;

		// the normal is simply the rotated tangent:
		return new Point(
			(-3.0 * ay * t - 2.0 * by) * t - cy,
			( 3.0 * ax * t + 2.0 * bx) * t + cx
		);
	}

	private static native double nativeGetLength(double p1x, double p1y,
			double h1x, double h1y, double h2x, double h2y, double p2x, double p2y);

	/**
	 * The approximated length of the curve in points.
	 */
	public double getLength() {
		updateSegments();
		return nativeGetLength(
				segment1.point.x,
				segment1.point.y,
				(segment1.handleOut.x + segment1.point.x),
				(segment1.handleOut.y + segment1.point.y),
				(segment2.handleIn.x + segment2.point.x),
				(segment2.handleIn.y + segment2.point.y),
				segment2.point.x,
				segment2.point.y
		);
	}

	public boolean isLinear() {
		return segment1.handleOut.x == 0 && segment1.handleOut.y == 0
			&& segment2.handleIn.y == 0 && segment2.handleIn.y == 0;
	}

	private static native void nativeAdjustThroughPoint(float[] values,
			float x, float y, float parameter);

	public void adjustThroughPoint(Point pt, double parameter) {
		updateSegments();

		float[] values = new float[2 * SegmentList.VALUES_PER_SEGMENT];
		segment1.getValues(values, 0);
		segment2.getValues(values, 1);
		nativeAdjustThroughPoint(values, (float) pt.x, (float) pt.y, (float) parameter);
		segment1.setValues(values, 0);
		segment2.setValues(values, 1);
		// don't mark dirty, commit immediately both as all the values have
		// been modified:
		if (segments.path != null) {
			Path path = segments.path;
			SegmentList.nativeSet(path.handle, path.document.handle,
					index1, 2, values);
		}
	}

	public Curve transform(Matrix matrix) {
		updateSegments();
		return new Curve(
				matrix.transform(segment1.point),
				matrix.transform(segment1.handleOut),
				matrix.transform(segment2.handleIn),
				matrix.transform(segment2.point));
	}

	/**
	 * Splits the curve into two at the specified position. The curve itself is
	 * modified and becomes the first part, the second part is returned as a new
	 * curve. If the modified curve belongs to a path item, the second part is
	 * added to it.
	 * 
	 * @param parameter the position at which to split the curve as a value
	 *        between 0 and 1 {@default 0.5}
	 * @return the second part of the splitted curve
	 */
	public Curve split(double parameter) {
		if (parameter > 0 && parameter < 1) {
			updateSegments();
			
			double left[][] = getCurveArray();
			double right[][] = new double[4][];
			split(left, parameter, left, right);
		
			// write back the results:
			segment1.handleOut.set(left[1][0] - segment1.point.x,
					left[1][1] - segment1.point.y);
	
			// create the new segment, absolute -> relative:
			double x = left[3][0];
			double y = left[3][1];
			Segment newSegment = new Segment(x, y,
					left[2][0] - x, left[2][1] - y,
					right[1][0] - x, right[1][1] - y);
	
			// and insert it, if needed:
			if (segments != null)
				segments.add(index2, newSegment);
	
			// absolute->relative
			segment2.handleIn.set(right[2][0] - segment2.point.x,
					right[2][1] - segment2.point.y);
	
			if (segments != null && segments.path != null) {
				// if this curve is linked to a path, get the new curve there
				return (Curve) segments.path.getCurves().get(index2);
			} else {
				// otherwise create it from the result of split
				return new Curve(newSegment, segment2);
			}
		}
		return null;
	}

	public Curve split() {
		return split(0.5f);
	}

	/**
	 * @param point
	 * @param precision
	 */
	public double hitTest(Point point, double precision) {
		updateSegments();
		
		return hitTest(getCurveArray(), point.x, point.y, precision);
	}

	public double hitTest(Point point) {
		return hitTest(point, EPSILON);
	}

	public double getPartLength(double fromParameter, double toParameter) {
		updateSegments();
		double[][] curve = getCurveArray();
		return getPartLength(curve, fromParameter, toParameter, curve);
	}

	public double getParameterWithLength(double length) {
		if (length <= 0)
			return 0;
		// updateSegments is not necessary here, as it is called in getLength!
		double bezierLength = getLength();
		if (length >= bezierLength)
			return 1;
		double[][] curve = getCurveArray();
		double[][] temp = new double[4][];
		// Use length / bezierLength as a first guess, then iterate closer
		double t = length / bezierLength, prevCloseness = 1;
		// TODO: Find a better approach for this:
		// Make sure we're not iterating endlessly...
		for (int i = 0; i < 100; i++) {
			double partLength = getPartLength(curve, 0, t, temp);
			double distance = (length - partLength) / bezierLength;
			// closeness: value for the 'exactness' of the current guess
			double closeness = Math.abs(distance);
			// If it's exact enough or even getting worse again,
			// break the loop...
			if (closeness < 0.00001 || closeness >= prevCloseness)
				break;
			t += distance * 0.5;
			if (t < 0) t = 0;
			else if (t > 1) t = 1;
			prevCloseness = closeness;
		}
		return t;
	}
	
	private double[][] getCurveArray() {
		return new double[][] {
			{ segment1.point.x, segment1.point.y },
			{ segment1.handleOut.x + segment1.point.x,
					segment1.handleOut.y + segment1.point.y },
			{ segment2.handleIn.x + segment2.point.x,
					segment2.handleIn.y + segment2.point.y },
			{ segment2.point.x, segment2.point.y } };
	}
	
	/*
	 * Low Level Math functions for division and calculation of roots:
	 */
	
	private static void split(double[][] curve, double t, double[][] left,
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
		
		// only write back left curve if it's not overwritten by right
		// afterwards
		if (left != null) {
			left[0] = temp[0][0];
			left[1] = temp[1][0];
			left[2] = temp[2][0];
			left[3] = temp[3][0];
		}

		// curve automatically contains left result, through temp[0],
		// write right result into right:
		if (right != null) {
			right[0] = temp[3][0];
			right[1] = temp[2][1];
			right[2] = temp[1][2];
			right[3] = temp[0][3];
		}
	}

	/*
	 * curve is only modified if it is passed as tempCurve as well. this is
	 * needed in getParameterWithLength above...
	 */
	private static double getPartLength(double curve[][], double fromParameter,
			double toParameter, double tempCurve[][]) {
		if (fromParameter > toParameter) {
			double temp = fromParameter;
			fromParameter = toParameter;
			toParameter = temp;
		} else if (fromParameter == toParameter) {
			return 0;
		}

		if (fromParameter < 0)
			fromParameter = 0;

		if (toParameter > 1)
			toParameter = 1;

		// get the point in order to calculate the new fromParameter for the
		// divided curve afterwards (TODO: there must be a simpler solution for
		// getting that value)
		if (toParameter < 1) {
			double fromX = 0;
			double fromY = 0;
			if (fromParameter > 0) {
				// calculate the point of fromParameter (see getPoint)
				double cx = 3f * (curve[1][0] - curve[0][0]);
				double bx = 3f * (curve[2][0] - curve[1][0]) - cx;
				double ax = curve[3][0] - curve[0][0] - cx - bx;

				double cy = 3f * (curve[1][1] - curve[0][1]);
				double by = 3f * (curve[2][1] - curve[1][1]) - cy;
				double ay = curve[3][1] - curve[0][1] - cy - by;

				fromX = ((ax * fromParameter + bx) * fromParameter + cx)
								* fromParameter + curve[0][0];
				fromY = ((ay * fromParameter + by) * fromParameter + cy)
								* fromParameter + curve[0][1];
			}
			// cut away the second part:
			split(curve, toParameter, tempCurve, null);
			curve = tempCurve;
			// now adjust fromParameter, by calculating the parameter of
			// fromX,fromY
			if (fromParameter > 0) {
				fromParameter = hitTest(curve, fromX, fromY, EPSILON);
				if (fromParameter == -1)
					return -1;
			}
		}
		if (fromParameter > 0) {
			split(curve, fromParameter, null, tempCurve);
			curve = tempCurve;
		}
		return nativeGetLength(
				curve[0][0], curve[0][1],
				curve[1][0], curve[1][1],
				curve[2][0], curve[2][1],
				curve[3][0], curve[3][1]
		);
	}

	private static int solveQuadraticRoots(double a, double b, double c,
			double roots[], double epsilon) {
		// Solve, using closed form methods, the quadratic polynomial:	
		//		a*x^2 + b*x + c = 0				
		// for 2 real roots returned in root[0..1].  If error we return 0.
		// We also return 0 or 1 real roots as appropriate, such as when
		// the problem is actually linear.					
		// After _Numerical Recipes in C_, 2nd edition, Press et al.,	
		// page 183, although with some added case testing and forwarding.
		// This is better than the _Graphics Gems_ technique, which admits
		// the possibility of numerical errors cited in Press.		
		int solutions = 0;
		// If problem is actually linear, return 0 or 1 easy roots		
		if (Math.abs(a) < epsilon) {
			if (Math.abs(b) >= epsilon) {
				roots[solutions++] = -c / b;
			} else if (Math.abs(c) < epsilon) { 
				// if all the coefficients are 0, infinite values are possible!
				solutions = -1; // -1 indicates infinite solutions
			}
			return solutions;
		}
		double bb = b*b;
		double q = bb-4.0*a*c;
		if (q < 0.0) return solutions;
		q = Math.sqrt(q);
		if (b < 0.0) q = -q;
		q = -0.5 * (b + q);
		if (Math.abs(q) >= epsilon) roots[solutions++] = c / q;
		if (Math.abs(a) >= epsilon) roots[solutions++] = q / a;
		return solutions;
	}

	private static int solveCubicRoots(double a, double b, double c, double d,
			double roots[], double epsilon) {
		// Solve, using closed form methods, the cubic polynomial:		
		//		a*x^3 + b*x^2 + c*x + d = 0			
		// for 1 real root returned in root[0], or 3 real roots returned
		// in root[0..2].  If error we return 0.  Note: we alter c[].	
		// If the polynomial is actually quadratic or linear (because	
		// coefficients a or b are zero), we forward the problem to
		// the quadratic/linear solver and return the appropriate 1 or 2
		// roots.								
		// After _Numerical Recipes in C_, 2nd edition, Press et al.,	
		// page 184, although with some added case testing and forwarding.
		// This is better than the _Graphics Gems_ technique, which admits
		// the possibility of numerical errors cited in Press.		
		// Test for a quadratic or linear degeneracy			
		if (Math.abs(a) < epsilon) {
			return solveQuadraticRoots(b, c, d, roots, epsilon);
		}
		// Normalize							
		b /= a; c /= a; d /= a; a = 1.0;
		// Compute discriminants						
		double Q = (b * b - 3.0 * c) / 9.0;
		double QQQ = Q * Q * Q;
		double R = (2.0 * b * b * b - 9.0 * b * c + 27.0 * d) / 54.0;
		double RR = R * R;
		if (RR <= QQQ) { // Three real roots
			// This sqrt and division is safe, since RR >= 0, so QQQ > RR,	
			// so QQQ > 0.  The acos is also safe, since RR/QQQ < 1, and	
			// thus R/sqrt(QQQ) < 1.					
			double theta = Math.acos(R / Math.sqrt(QQQ));
			// This sqrt is safe, since QQQ >= 0, and thus Q >= 0
			double v1 = -2.0 * Math.sqrt(Q);
			double v2 = b / 3.0;
			roots[0] = v1 * Math.cos(theta / 3.0) - v2;
			roots[1] = v1 * Math.cos((theta + 2 * Math.PI) / 3.0) - v2;
			roots[2] = v1 * Math.cos((theta - 2 * Math.PI) / 3.0) - v2;
			return 3;
		} else { // One real root							
			double A = -Math.pow(Math.abs(R)+ Math.sqrt(RR - QQQ), 1.0 / 3.0);
			if (A != 0.0) {
				if (R < 0.0) A = -A;
				roots[0] = A + Q / A - b / 3.0;
				return 1;
			}
		}
		return 0;
	}

	private static int solveCubicRoots(double v1, double v2, double v3,
			double v4, double v, double roots[], double epsilon) {
		// conversion from the point coordinates (v1 .. v4) to the polynomal
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
	
	private static double hitTest(double[][] curve, double x, double y,
			double epsilon) {
		double txs[] = { 0, 0, 0 }; 
		double tys[] = { 0, 0, 0 };
		
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
}
