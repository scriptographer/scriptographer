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
 * File created on Jun 9, 2010.
 */

package com.scriptographer.ai;

/**
 * @author lehni
 *
 * @jshide
 */
public class Line {
	private Point point1;
	private Point point2;
	/**
	 * extend controls wether the line extends beyond the defining points,
	 * meaning point results outside the line segment are allowed.
	 */
	private boolean extend;

	public Line(Point point1, Point point2, boolean extend) {
		this.point1 = point1;
		this.point2 = point2;
		this.extend = extend;
	}

	public Line(Point point1, Point point2) {
		this(point1, point2, false);
	}

	public Line(double x1, double y1, double x2, double y2, boolean extend) {
		this.point1 = new Point(x1, y1);
		this.point2 = new Point(x2, y2);
		this.extend = extend;
	}

	public Line(double x1, double y1, double x2, double y2) {
		this(x1, y1, x2, y2, false);
	}

	public Point intersect(Line line) {
		return intersect(point1.x, point1.y, point2.x, point2.y, extend,
				line.point1.x, line.point1.y, line.point2.x, line.point2.y,
				line.extend);
	}
	
	public double getSide(Point p) {
		Point v1 = point2.subtract(point1);
		Point v2 = p.subtract(point1);
		double ccw = v2.cross(v1);
		if (ccw == 0.0) {
			ccw = v2.dot(v1);
			if (ccw > 0.0) {
				ccw = v2.subtract(v1).dot(v1);
				if (ccw < 0.0)
				    ccw = 0.0;
			}
		}
		return ccw < 0.0 ? -1 : ccw > 0.0 ? 1 : 0;
	}

	public Point getPoint1() {
		return point1;
	}

	public Point getPoint2() {
		return point2;
	}

	public Point getVector() {
		return point2.subtract(point1);
	}

	/**
	 * @jshide
	 */
	public static Point intersect(
			double l1p1x, double l1p1y,
			double l1p2x, double l1p2y, boolean extend1,
			double l2p1x, double l2p1y,
			double l2p2x, double l2p2y, boolean extend2) {
		/*
		// The code below performs the same as this but without using objects
		// for increased performance.
		Point v1 = point2.subtract(point1);
		Point v2 = line.point2.subtract(line.point1);
		double cross = v1.cross(v2);
		if (Math.abs(cross) <= 10e-6)
			return null;
		Point v = line.point1.subtract(point1);
		double t1 = v.cross(v2) / cross;
		double t2 = v.cross(v1) / cross;
		// Check the ranges of t parameters if the line is not allowed to
		// extend beyond the definition points.
		if ((extend || 0 <= t1 && t1 <= 1)
				&& (line.extend || 0 <= t2 && t2 <= 1))
			return point1.add(v1.multiply(t1));
		return null;
		*/
		double v1x = l1p2x - l1p1x;
		double v1y = l1p2y - l1p1y;
		double v2x = l2p2x - l2p1x;
		double v2y = l2p2y - l2p1y;
		double cross = v1x * v2y - v1y * v2x;
		if (Math.abs(cross) > 10e-6) {
			double vx = l2p1x - l1p1x;
			double vy = l2p1y - l1p1y;
			double t1 = (vx * v2y - vy * v2x) / cross;
			double t2 = (vx * v1y - vy * v1x) / cross;
			// Check the ranges of t parameters if the line is not
			// allowed to extend beyond the definition points.
			if ((extend1 || 0 <= t1 && t1 <= 1)
					&& (extend2 || 0 <= t2 && t2 <= 1))
				return new Point(l1p1x + v1x * t1, l1p1y + v1y * t1);
		}
		return null;
	}

	public static Point intersect(Point l1p1, Point l1p2, boolean extend1,
			Point l2p1, Point l2p2, boolean extend2) {
		return intersect(l1p1.x, l1p1.y, l1p2.x, l1p2.y, extend1,
				l2p1.x, l2p1.y, l2p2.x, l2p2.y, extend2);
	}
}