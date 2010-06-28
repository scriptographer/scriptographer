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

	public Line(double x1, double y1, double x2, double y2, boolean extend) {
		this.point1 = new Point(x1, y1);
		this.point2 = new Point(x2, y2);
		this.extend = extend;
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