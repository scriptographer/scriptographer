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
 * $RCSfile: Segment.java,v $
 * $Author: lehni $
 * $Revision: 1.5 $
 * $Date: 2005/04/07 20:12:55 $
 */

package com.scriptographer.ai;

import com.scriptographer.js.ArgumentReader;
import com.scriptographer.Commitable;
import com.scriptographer.CommitManager;

import java.awt.geom.Point2D;

public class Segment implements Commitable {
	protected SegmentList segments;
	protected int index;
	// the internal points
	protected SegmentPoint point;
	protected SegmentPoint handleIn;
	protected SegmentPoint handleOut;
	// corner
	protected boolean corner;
	//
	protected int version = -1;
	protected boolean dirty = false;

	public Segment() {
		point = new SegmentPoint(this, 0);
		handleIn = new SegmentPoint(this, 2);
		handleOut = new SegmentPoint(this, 4);
	}

	public Segment(Point2D pt, Point2D in, Point2D out, boolean corner) {
		point = new SegmentPoint(this, 0, pt);
		handleIn = new SegmentPoint(this, 2, in != null ? in : pt);
		handleOut = new SegmentPoint(this, 4, out != null ? out : pt);
		this.corner = corner;
	}

	public Segment(float x, float y, float inX, float inY, float outX, float outY, boolean corner) {
		point = new SegmentPoint(this, 0, x, y);
		handleIn = new SegmentPoint(this, 2, inX, inY);
		handleOut = new SegmentPoint(this, 4, outX, outY);
		this.corner = corner;
	}

	public Segment(float x, float y) {
		this(x, y, x, y, x, y, true);
	}
	
	public Segment(Point2D pt) {
		this((float) pt.getX(), (float) pt.getY());
	}
	
	public Segment(Segment segment) {
		this(segment.point, segment.handleIn, segment.handleOut, segment.corner);
	}
	
	protected Segment(SegmentList segments, int index) {
		this();
		this.segments = segments;
		this.index = index;
	}

	/**
	 * Warning: This does not call markDirty(). This needs to be taken care of after.
	 * @param values
	 * @param valueIndex
	 */
	protected void setValues(float[] values, int valueIndex) {
		point.x = values[valueIndex];
		point.y = values[valueIndex + 1];
		handleIn.x = values[valueIndex + 2];
		handleIn.y = values[valueIndex + 3];
		handleOut.x = values[valueIndex + 4];
		handleOut.y = values[valueIndex + 5];
		corner = values[valueIndex + 6] != 0;
	}

	protected void getValues(float[] values, int valueIndex) {
		values[valueIndex] = point.x;
		values[valueIndex + 1] = point.y;
		values[valueIndex + 2] = handleIn.x;
		values[valueIndex + 3] = handleIn.y;
		values[valueIndex + 4] = handleOut.x;
		values[valueIndex + 5] = handleOut.y;
		// don't care about the exact value for true, as long as it's != 0 it works:
		values[valueIndex + 6] = corner ? 1f : 0f;
	}

	public void commit() {
		if (segments != null && segments.path != null) {
			SegmentList.nativeCommit(segments.path.handle, index, point.x, point.y, handleIn.x, handleIn.y, handleOut.x, handleOut.y, corner);
			System.out.println("nativeCommit " + index + " " + 1);
			// update to current maxVersion after commit.
			version = segments.path.version;
			dirty = false;
		}
	}

	/**
	 * inserts this segment in the underlying AI path at position index
	 * Only call once, when adding this segment to the segmentList!
	 */
	protected void insert() {
		if (segments != null && segments.path != null) {
			SegmentList.nativeInsert(segments.path.handle, index, point.x, point.y, handleIn.x, handleIn.y, handleOut.x, handleOut.y, corner);
			// update to current maxVersion after commit.
			version = segments.path.version;
			dirty = false;
		}
	}

	protected void markDirty() {
		// only mark it as dirty if it's attached to a path already:
		if (!dirty && segments != null && segments.path != null) {
			CommitManager.markDirty(this);
			dirty = true;
		}
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(64);
		buf.append("{ point: ").append(point.toString());
		if (!handleIn.equals(point))
			buf.append(", handleIn: ").append(handleIn.toString());
		if (!handleOut.equals(point))
			buf.append(", handleOut: ").append(handleOut.toString());
		if (corner)
			buf.append(", corner: ").append(Boolean.toString(corner));
		buf.append(" }");
		return buf.toString();
	}

	public int getIndex() {
		return index;
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point2D pt) {
		point.setLocation(pt);
	}

	// TODO: get rid of ArgumentReader calls and implement all versions!!!
	public void setPoint(Object pt) {
		point.setLocation(new ArgumentReader().readPoint(pt));
	}

	public Point getHandleIn() {
		return handleIn;
	}

	public void setHandleIn(Point2D pt) {
		handleIn.setLocation(pt);
	}

	public void setHandleIn(Object pt) {
		handleIn.setLocation(new ArgumentReader().readPoint(pt));
	}

	public Point getHandleOut() {
		return handleOut;
	}

	public void setHandleOut(Point2D pt) {
		handleOut.setLocation(pt);
	}

	public void setHandleOut(Object pt) {
		handleOut.setLocation(new ArgumentReader().readPoint(pt));
	}

	public boolean getCorner() {
		return corner;
	}

	public void setCorner(boolean corner) {
		this.corner = corner;
		markDirty();
	}

	public Segment divide(float t) {
		Curve newCurve = getCurve().divide(t);
		if (newCurve != null)
			return newCurve.getSegment1();
		else
			return null;
	}
	
	public Segment divide() {
		return divide(0.5f);
	}
	
	public Curve getCurve() {
		return (Curve) segments.path.getCurves().get(index);
	}
}