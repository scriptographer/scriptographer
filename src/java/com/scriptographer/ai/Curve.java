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
 * $Revision: 1.2 $
 * $Date: 2005/03/07 13:38:54 $
 */

package com.scriptographer.ai;

import com.scriptographer.js.ArgumentReader;

import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;

public class Curve {
	protected SegmentList segmentList = null;
	protected int index1;
	protected int index2;
	private Segment segment1;
	private Segment segment2;
	protected int fetchCount = -1;

	public Curve() {
		segment1 = new Segment();
		segment2 = new Segment();
	}

	public Curve(SegmentList segmentList, int index) {
		this();
		this.segmentList = segmentList;
		this.index1 = index;
		updateSegments();
	}

	public Curve(Point2D pt1, Point2D h1, Point2D h2, Point2D pt2) {
		segment1 = new Segment(pt1, pt1, h1, false);
		segment2 = new Segment(pt2, h2, pt2, false);
	}

	// TODO: instead of calling updateSegments(); everywhere, could there be a better way
	// to do this? e.g. calling from segments when needed?
	public String toString() {
		updateSegments();
		StringBuffer buf = new StringBuffer(64);
		buf.append("{ point1: ").append(segment1.point.toString());
		if (!segment1.handleOut.equals(segment1.point))
			buf.append(", handle1: ").append(segment1.handleOut.toString());
		if (!segment2.handleIn.equals(segment2.point))
			buf.append(", handle2: ").append(segment2.handleIn.toString());
		buf.append(", point2: ").append(segment2.point.toString());
		buf.append(" }");
		return buf.toString();
	}

	protected void updateSegments() {
		if (segmentList != null) {
			// a closing bezier?
			index2 = index1 + 1;
			if (index2 >= segmentList.length)
				index2 = 0;

			if (segment1 == null || segment1.index != index1)
				segment1 = (Segment) segmentList.get(index1);

			if (segment2 == null || segment2.index != index2)
				segment2 = (Segment) segmentList.get(index2);
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

	public void setPoint1(Object pt) {
		updateSegments();
		segment1.point.setLocation(new ArgumentReader().readPoint(pt));
	}

	public Point getHandle1() {
		updateSegments();
		return segment1.handleOut;
	}

	public void setHandle1(Point2D pt) {
		updateSegments();
		segment1.handleOut.setLocation(pt);
	}

	public void setHandle1(Object pt) {
		updateSegments();
		segment1.handleOut.setLocation(new ArgumentReader().readPoint(pt));
	}

	public Point getHandle2() {
		updateSegments();
		return segment2.handleIn;
	}

	public void setHandle2(Point2D pt) {
		updateSegments();
		segment2.handleIn.setLocation(pt);
	}

	public void setHandle2(Object pt) {
		updateSegments();
		segment2.handleIn.setLocation(new ArgumentReader().readPoint(pt));
	}

	public Point getPoint2() {
		updateSegments();
		return segment2.point;
	}

	public void setPoint2(Point2D pt) {
		updateSegments();
		segment2.point.setLocation(pt);
	}

	public void setPoint2(Object pt) {
		updateSegments();
		segment2.point.setLocation(new ArgumentReader().readPoint(pt));
	}

	/*
	 * Instead of using the underlying AI functions and loose time for calling natives,
	 * let's do the dirty work ourselves:
	 */
	public Point getPoint(float position) {
		updateSegments();
		// calculate the polynomial coefficients
		float cx = 3f * (segment1.handleOut.x - segment1.point.x);
		float bx = 3f * (segment2.handleIn.x - segment1.handleOut.x) - cx;
		float ax = segment2.point.x - segment1.point.x - cx - bx;

		float cy = 3f * (segment1.handleOut.y - segment1.point.y);
		float by = 3f * (segment2.handleIn.y - segment1.handleOut.y) - cy;
		float ay = segment2.point.y - segment1.point.y - cy - by;

		return new Point(
				( (ax * position + bx) * position + cx) * position + segment1.point.x,
				( (ay * position + by) * position + cy) * position + segment1.point.y
		);
	}

	public Point getTangent(float position) {
		updateSegments();
		// calculate the polynomial coefficients
		float cx = 3f * (segment1.handleOut.x - segment1.point.x);
		float bx = 3f * (segment2.handleIn.x - segment1.handleOut.x) - cx;
		float ax = segment2.point.x - segment1.point.x - cx - bx;

		float cy = 3f * (segment1.handleOut.y - segment1.point.y);
		float by = 3f * (segment2.handleIn.y - segment1.handleOut.y) - cy;
		float ay = segment2.point.y - segment1.point.y - cy - by;

		// simply use the derivation of the bezier function
		// for both the x and y coordinates:
		return new Point(
				( 3f * ax * position + 2f * bx) * position + cx,
				( 3f * ay * position + 2f * by) * position + cy
		);
	}

	public Point getNormal(float position) {
		updateSegments();
		// calculate the polynomial coefficients
		float cx = 3f * (segment1.handleOut.x - segment1.point.x);
		float bx = 3f * (segment2.handleIn.x - segment1.handleOut.x) - cx;
		float ax = segment2.point.x - segment1.point.x - cx - bx;

		float cy = 3f * (segment1.handleOut.y - segment1.point.y);
		float by = 3f * (segment2.handleIn.y - segment1.handleOut.y) - cy;
		float ay = segment2.point.y - segment1.point.y - cy - by;

		// the normal is simply the rotated tangent:
		return new Point(
				(-3f * ay * position - 2f * by) * position - cy,
				( 3f * ax * position + 2f * bx) * position + cx
		);
	}

	private native float nativeGetLength(float p1x, float p1y, float h1x, float h1y, float h2x, float h2y, float p2x, float p2y, float flatness);

	public float getLength(float flatness) {
		updateSegments();
		return nativeGetLength(
				segment1.point.x, segment1.point.y,
				segment1.handleOut.x, segment1.handleOut.y,
				segment2.handleIn.x, segment2.handleIn.y,
				segment2.point.x, segment2.point.y,
				flatness
		);
	}

	public float getLength() {
		return getLength(0.1f);
	}

	private native float nativeGetPartLength(float p1x, float p1y, float h1x, float h1y, float h2x, float h2y, float p2x, float p2y, float fromPosition, float toPosition, float flatness);

	public float getPartLength(float fromPosition, float toPosition, float flatness) {
		updateSegments();
		return nativeGetPartLength(
				segment1.point.x, segment1.point.y,
				segment1.handleOut.x, segment1.handleOut.y,
				segment2.handleIn.x, segment2.handleIn.y,
				segment2.point.x, segment2.point.y,
				fromPosition, toPosition, flatness
		);
	}

	public float getPartLength(float fromPosition, float toPosition) {
		return getPartLength(fromPosition, toPosition, 0.1f);
	}

	private native float nativeGetPositionWithLength(float p1x, float p1y, float h1x, float h1y, float h2x, float h2y, float p2x, float p2y, float length, float flatness);

	public float getPositionWithLength(float length, float flatness) {
		updateSegments();
		return nativeGetPositionWithLength(
				segment1.point.x, segment1.point.y,
				segment1.handleOut.x, segment1.handleOut.y,
				segment2.handleIn.x, segment2.handleIn.y,
				segment2.point.x, segment2.point.y,
				length, flatness
		);
	}

	public float getPositionWithLength(float length) {
		return getPositionWithLength(length, 0.1f);
	}

	private native void nativeAdjustThroughPoint(float[] values, float x, float y, float position);

	public void adjustThroughPoint(Point2D pt, float position) {
		updateSegments();

		float[] values = new float[2 * SegmentList.VALUES_PER_SEGMENT];
		segment1.getValues(values, 0);
		segment2.getValues(values, 1);
		nativeAdjustThroughPoint(values, (float)pt.getX(), (float)pt.getY(), position);
		segment1.setValues(values, 0);
		segment2.setValues(values, 1);
		// don't mark dirty, commit immediatelly both as all the values have been modified:
		if (segmentList.path != null)
			SegmentList.nativeCommit(segmentList.path.artHandle, index1, 2, values);
	}

	public Curve divide() {
		// TODO: implement;
		return null;
	}

	public void transform(AffineTransform at) {
		// TODO: implement ?
	}
}
