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
 * File created on 14.12.2004.
 *
 * $RCSfile: Curve.java,v $
 * $Author: lehni $
 * $Revision: 1.6 $
 * $Date: 2005/04/22 11:14:24 $
 */

package com.scriptographer.ai;

import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;

public class Curve {
	protected SegmentList segments = null;
	protected int index1;
	protected int index2;
	private Segment segment1;
	private Segment segment2;
	protected int fetchCount = -1;
	
	protected static final float EPSILON = 0.00001f;
	protected static final float FLATNESS = 0.1f;

	public Curve() {
		segment1 = new Segment();
		segment2 = new Segment();
	}

	public Curve(SegmentList segmentList, int index) {
		this.segments = segmentList;
		this.index1 = index;
		updateSegments();
	}
	
	public Curve(Segment segment1, Segment segment2) {
		segment1 = new Segment(segment1);
		segment2 = new Segment(segment2);
	}
	
	public Curve(Curve curve) {
		this(curve.segment1, curve.segment2);
	}

	public Curve(Point2D pt1, Point2D h1, Point2D h2, Point2D pt2) {
		segment1 = new Segment(pt1, null, h1, false);
		segment2 = new Segment(pt2, h2, null, false);
	}
	
	public Curve(float p1x, float p1y, float h1x, float h1y, float h2x, float h2y, float p2x, float p2y) {
		segment1 = new Segment(p1x, p1y, 0, 0, h1x, h1y, false);
		segment2 = new Segment(p2x, p2y, h2x, h2y, 0, 0, false);
	}

	// TODO: instead of calling updateSegments(); everywhere, could there be a better way
	// to do this? e.g. calling from segments when needed?
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

	protected void updateSegments() {
		if (segments != null) {
			// a closing bezier?
			index2 = index1 + 1;
			if (index2 >= segments.size)
				index2 = 0;

			if (segment1 == null || segment1.index != index1)
				segment1 = (Segment) segments.get(index1);

			if (segment2 == null || segment2.index != index2)
				segment2 = (Segment) segments.get(index2);
		}
	}

	public int getIndex() {
		return index1;
	}

	public Point getPoint1() {
		updateSegments();
		return segment1.point;
	}

	public void setPoint1(Point2D pt) {
		updateSegments();
		segment1.point.setLocation(pt);
	}

	public void setPoint1(float x, float y) {
		updateSegments();
		segment1.point.setLocation(x, y);
	}

	public Point getHandle1() {
		updateSegments();
		return segment1.handleOut;
	}

	public void setHandle1(Point2D pt) {
		updateSegments();
		segment1.handleOut.setLocation(pt);
	}

	public void setHandle1(float x, float y) {
		updateSegments();
		segment1.handleOut.setLocation(x, y);
	}

	public Point getHandle2() {
		updateSegments();
		return segment2.handleIn;
	}

	public void setHandle2(Point2D pt) {
		updateSegments();
		segment2.handleIn.setLocation(pt);
	}

	public void setHandle2(float x, float y) {
		updateSegments();
		segment2.handleIn.setLocation(x, y);
	}

	public Point getPoint2() {
		updateSegments();
		return segment2.point;
	}

	public void setPoint2(Point2D pt) {
		updateSegments();
		segment2.point.setLocation(pt);
	}

	public void setPoint2(float x, float y) {
		updateSegments();
		segment2.point.setLocation(x, y);
	}
	
	public Segment getSegment1() {
		return segment1;
	}
	
	public Segment getSegment2() {
		return segment2;
	}

	/*
	 * Instead of using the underlying AI functions and loose time for calling natives,
	 * let's do the dirty work ourselves:
	 */
	public Point getPoint(float position) {
		updateSegments();
		// calculate the polynomial coefficients. caution: handles are relative to points
		float dx = segment2.point.x - segment1.point.x;
		float cx = 3f * segment1.handleOut.x;
		float bx = 3f * (dx + segment2.handleIn.x - segment1.handleOut.x) - cx;
		float ax = dx - cx - bx;

		float dy = segment2.point.y - segment1.point.y;
		float cy = 3f * segment1.handleOut.y;
		float by = 3f * (dy + segment2.handleIn.y - segment1.handleOut.y) - cy;
		float ay = dy - cy - by;

		return new Point(
				((ax * position + bx) * position + cx) * position + segment1.point.x,
				((ay * position + by) * position + cy) * position + segment1.point.y
		);
	}

	public Point getTangent(float position) {
		updateSegments();
		// calculate the polynomial coefficients. caution: handles are relative to points
		float dx = segment2.point.x - segment1.point.x;
		float cx = 3f * segment1.handleOut.x;
		float bx = 3f * (dx + segment2.handleIn.x - segment1.handleOut.x) - cx;
		float ax = dx - cx - bx;

		float dy = segment2.point.y - segment1.point.y;
		float cy = 3f * segment1.handleOut.y;
		float by = 3f * (dy + segment2.handleIn.y - segment1.handleOut.y) - cy;
		float ay = dy - cy - by;

		// simply use the derivation of the bezier function
		// for both the x and y coordinates:
		return new Point(
				(3f * ax * position + 2f * bx) * position + cx,
				(3f * ay * position + 2f * by) * position + cy
		);
	}

	public Point getNormal(float position) {
		updateSegments();
		// calculate the polynomial coefficients. caution: handles are relative to points
		float dx = segment2.point.x - segment1.point.x;
		float cx = 3f * segment1.handleOut.x;
		float bx = 3f * (dx + segment2.handleIn.x - segment1.handleOut.x) - cx;
		float ax = dx - cx - bx;

		float dy = segment2.point.y - segment1.point.y;
		float cy = 3f * segment1.handleOut.y;
		float by = 3f * (dy + segment2.handleIn.y - segment1.handleOut.y) - cy;
		float ay = dy - cy - by;

		// the normal is simply the rotated tangent:
		return new Point(
				(-3f * ay * position - 2f * by) * position - cy,
				( 3f * ax * position + 2f * bx) * position + cx
		);
	}

	private native static float nativeGetLength(float p1x, float p1y, float h1x, float h1y, float h2x, float h2y, float p2x, float p2y, float flatness);

	public float getLength(float flatness) {
		updateSegments();
		return nativeGetLength(
				segment1.point.x, segment1.point.y,
				segment1.handleOut.x + segment1.point.x, segment1.handleOut.y + segment1.point.y,
				segment2.handleIn.x + segment2.point.x, segment2.handleIn.y + segment2.point.y,
				segment2.point.x, segment2.point.y,
				flatness
		);
	}

	public float getLength() {
		return getLength(FLATNESS);
	}

	private native static void nativeAdjustThroughPoint(float[] values, float x, float y, float position);

	public void adjustThroughPoint(Point2D pt, float position) {
		updateSegments();

		float[] values = new float[2 * SegmentList.VALUES_PER_SEGMENT];
		segment1.getValues(values, 0);
		segment2.getValues(values, 1);
		nativeAdjustThroughPoint(values, (float)pt.getX(), (float)pt.getY(), position);
		segment1.setValues(values, 0);
		segment2.setValues(values, 1);
		// don't mark dirty, commit immediatelly both as all the values have been modified:
		if (segments.path != null)
			SegmentList.nativeCommit(segments.path.handle, index1, 2, values);
	}

	public void transform(AffineTransform at) {
		// TODO: implement ?
	}
	
	public Curve divide(double t) {
		if (t > 0 && t < 1f) {
			updateSegments();
			
			double left[][] = getCurveArray();
			double right[][] = new double[4][];
			divide(left, t, left, right);
		
			// write back the results:
			segment1.handleOut.setLocation(left[1][0] - segment1.point.x, left[1][1] - segment1.point.y);
	
			// create the new segment, absolute -> relative:
			float x = (float) left[3][0];
			float y = (float) left[3][1];
			Segment newSegment = new Segment(x, y, (float) left[2][0] - x, (float) left[2][1] - y, (float) right[1][0] - x, (float) right[1][1] - y, false);
	
			// and insert it, if needed:
			if (segments != null)
				segments.add(index2, newSegment);
	
			// absolute->relative
			segment2.handleIn.setLocation(right[2][0] - segment2.point.x, right[2][1] - segment2.point.y);
	
			if (segments != null && segments.path != null) {
				// if this curve is linked to a path, get the new curve there
				return (Curve) segments.path.getCurves().get(index2);
			} else {
				// otherwise create it from the result of divide
				return new Curve(newSegment, segment2);
			}
		}
		return null;
	}

	public Curve divide() {
		return divide(0.5f);
	}

	/**
	 * @param point
	 * @param epsilon
	 * @return
	 */
	public float hitTest(Point point, float epsilon) {
		updateSegments();
		
		return hitTest(getCurveArray(), point.x, point.y, epsilon);
	}

	public float hitTest(Point point) {
		return hitTest(point, EPSILON);
	}

	public float getPartLength(float fromPosition, float toPosition, float flatness) {
		updateSegments();
		double[][] curve = getCurveArray();
		return getPartLength(curve, fromPosition, toPosition, flatness, curve);
	}

	public float getPartLength(float fromPosition, float toPosition) {
		return getPartLength(fromPosition, toPosition, FLATNESS);
	}

	public float getParameterWithLength(float length, float flatness) {
		if (length <= 0)
			return 0;
		// updateSegments is not necessary here, as it is called in getLength!
		float bezierLength = getLength(flatness);
		if (length >= bezierLength)
			return 1;
		double[][] curve = getCurveArray();
		double[][] temp = new double[4][];
		double param = length / bezierLength, oldF = 1;
		for (int i = 0; i < 100; i++) { // prevent too many iterations...
			double stepLength = getPartLength(curve, 0, param, flatness, temp);
			double step = (length - stepLength) / bezierLength;
			double f = Math.abs(step); // f: value for exactness
			if (f < 0.00001 || f >= oldF) break; // if it's exact enough or even getting worse with iteration, break the loop...
			param += step; // (1 + f) * step
			// if pos < 0 then pos = 0
			oldF = f;
		}
		return (float) param;
	}

	public float getParameterWithLength(float length) {
		return getParameterWithLength(length, FLATNESS);
	}
	
	private double[][] getCurveArray() {
		return new double[][] {
			{segment1.point.x, segment1.point.y},
			{segment1.handleOut.x + segment1.point.x, segment1.handleOut.y + segment1.point.y},
			{segment2.handleIn.x + segment2.point.x, segment2.handleIn.y + segment2.point.y},
			{segment2.point.x, segment2.point.y}
		};
	}
	
	/*
	 * Low Level Math functions for division and calculation of roots:
	 */
	
	private static void divide(double[][] curve, double t, double[][] left, double[][] right) {
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
		
		// only write back left curve if it's not overwritten by right afterwards
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
	 * curve is only modified if it is passed as tempCurve as well. this is needed in getParameterWithLength above...
	 */
	private static float getPartLength(double curve[][], double fromPosition, double toPosition, double flatness, double tempCurve[][]) {
		if (fromPosition > toPosition) {
			double temp = fromPosition;
			fromPosition = toPosition;
			toPosition = temp;
		} else if (fromPosition == toPosition) {
			return 0;
		}

		if (fromPosition < 0)
			fromPosition = 0;

		if (toPosition > 1)
			toPosition = 1;

		// get the point in order to calculate the new fromPosition for the divided curve
		// afterwards (TODO: ther must be a simpler solution for getting that value)
		if (toPosition < 1) {
			double fromX = 0;
			double fromY = 0;
			if (fromPosition > 0) {
				// calculate the point of fromPosition (see getPoint)
				double cx = 3f * (curve[1][0] - curve[0][0]);
				double bx = 3f * (curve[2][0] - curve[1][0]) - cx;
				double ax = curve[3][0] - curve[0][0] - cx - bx;

				double cy = 3f * (curve[1][1] - curve[0][1]);
				double by = 3f * (curve[2][1] - curve[1][1]) - cy;
				double ay = curve[3][1] - curve[0][1] - cy - by;

				fromX = ((ax * fromPosition + bx) * fromPosition + cx) * fromPosition + curve[0][0];
				fromY = ((ay * fromPosition + by) * fromPosition + cy) * fromPosition + curve[0][1];
			}
			// cut away the second part:
			divide(curve, toPosition, tempCurve, null);
			curve = tempCurve;
			// now adjust fromPosition, by calculating the position of fromX,fromY
			if (fromPosition > 0) {
				fromPosition = hitTest(curve, fromX, fromY, EPSILON);
				if (fromPosition == -1)
					return -1;
			}
		}
		if (fromPosition > 0) {
			divide(curve, fromPosition, null, tempCurve);
			curve = tempCurve;
		}
		return nativeGetLength(
				(float) curve[0][0], (float) curve[0][1],
				(float) curve[1][0], (float) curve[1][1],
				(float) curve[2][0], (float) curve[2][1],
				(float) curve[3][0], (float) curve[3][1],
				(float) flatness
		);
	}

	private static int solveQuadraticRoots(double a, double b, double c, double roots[], double epsilon) {
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
			} else if (Math.abs(c) < epsilon) { // if all the coefficients are 0, infinite values are possible!
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

	private static int solveCubicRoots(double a, double b, double c, double d, double roots[], double epsilon) {
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

	private static int solveCubicRoots(double v1, double v2, double v3, double v4, double v, double roots[], double epsilon) {
		// conversion from the point coordinates (v1 .. v4) to the polynomal coefficients:
		double v1m3 = 3.0 * v1;
		double v2m3 = 3.0 * v2;
		double v3m3 = 3.0 * v3;

		double a = v4 - v3m3 + v2m3        - v1;
		double b =      v3m3 - v2m3 - v2m3 + v1m3;
		double c =             v2m3        - v1m3;
		double d =                           v1 - v;

		return solveCubicRoots(a, b, c, d, roots, epsilon);
	}
	
	private static float hitTest(double[][] curve, double x, double y, double epsilon) {
		double txs[] = { 0, 0, 0 }; 
		double tys[] = { 0, 0, 0 };
		
		int sx = solveCubicRoots(curve[0][0], curve[1][0], curve[2][0], curve[3][0], x, txs, epsilon);
		int sy = solveCubicRoots(curve[0][1], curve[1][1], curve[2][1], curve[3][1], y, tys, epsilon);

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
							return (float) ((tx + ty) * 0.5);
						}
					}
				}
				// avoid endless loops here:
				// if sx is infinite and there was no fitting ty, there's no solution for this bezier
				if (sx == -1)
					sx = 0; 
			}
		}
		return -1;
	}
}
