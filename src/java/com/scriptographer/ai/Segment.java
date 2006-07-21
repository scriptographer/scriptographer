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
 * $Revision: 1.13 $
 * $Date: 2006/07/21 16:49:15 $
 */

package com.scriptographer.ai;

import com.scriptographer.Commitable;
import com.scriptographer.CommitManager;
import com.scriptographer.js.WrappableObject;

import java.awt.geom.Point2D;

public class Segment extends WrappableObject implements Commitable {
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
		handleIn = in != null ?
			new SegmentPoint(this, 2, in) :
			new SegmentPoint(this, 2);
		handleOut = out != null ?
			new SegmentPoint(this, 4, out) :
			new SegmentPoint(this, 4);
		this.corner = corner;
	}

	public Segment(Point2D pt, Point2D in, Point2D out) {
		this(pt, in, out, false);
	}

	public Segment(float x, float y, float inX, float inY, float outX, float outY, boolean corner) {
		point = new SegmentPoint(this, 0, x, y);
		handleIn = new SegmentPoint(this, 2, inX, inY);
		handleOut = new SegmentPoint(this, 4, outX, outY);
		this.corner = corner;
	}

	public Segment(float x, float y, float inX, float inY, float outX, float outY) {
		this(x, y, inX, inY, outX, outY, false);
	}

	public Segment(float x, float y) {
		this(x, y, 0, 0, 0, 0, false);
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
		handleIn.x = values[valueIndex + 2] - point.x;
		handleIn.y = values[valueIndex + 3] - point.y;
		handleOut.x = values[valueIndex + 4] - point.x;
		handleOut.y = values[valueIndex + 5] - point.y;
		corner = values[valueIndex + 6] != 0;
	}

	protected void getValues(float[] values, int valueIndex) {
		values[valueIndex] = point.x;
		values[valueIndex + 1] = point.y;
		values[valueIndex + 2] = handleIn.x + point.x;
		values[valueIndex + 3] = handleIn.y + point.y;
		values[valueIndex + 4] = handleOut.x + point.x;
		values[valueIndex + 5] = handleOut.y + point.y;
		// don't care about the exact value for true, as long as it's != 0 it works:
		values[valueIndex + 6] = corner ? 1f : 0f;
	}

	public void commit() {
		if (dirty && segments != null && segments.path != null) {
			SegmentList.nativeCommit(segments.path.handle, index, point.x, point.y, handleIn.x + point.x, handleIn.y + point.y, handleOut.x + point.x, handleOut.y + point.y, corner);
			// System.out.println("nativeCommit " + index + " " + 1);
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
			SegmentList.nativeInsert(segments.path.handle, index, point.x, point.y, handleIn.x + point.x, handleIn.y + point.y, handleOut.x + point.x, handleOut.y + point.y, corner);
			// update to current maxVersion after commit.
			version = segments.path.version;
			dirty = false;
		}
	}

	protected void markDirty() {
		// only mark it as dirty if it's attached to a path already:
		if (!dirty && segments != null && segments.path != null) {
			CommitManager.markDirty(segments.path, this);
			dirty = true;
		}
	}
	
	protected void update() {
		if (!dirty && segments != null && segments.path != null && version != segments.path.version) {
			// this handles all the updating automatically:
			segments.get(index);
		}
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(64);
		buf.append("{ point: ").append(point.toString());
		if (handleIn.x != 0 || handleIn.y != 0)
			buf.append(", handleIn: ").append(handleIn.toString());
		if (handleOut.x != 0 || handleOut.y != 0)
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
		update();
		return point;
	}

	public void setPoint(Point2D pt) {
		point.setLocation(pt);
	}

	public void setPoint(float x, float y) {
		point.setLocation(x, y);
	}

	public void setPoint(double x, double y) {
		point.setLocation(x, y);
	}

	public Point getHandleIn() {
		update();
		return handleIn;
	}

	public void setHandleIn(Point2D pt) {
		handleIn.setLocation(pt);
	}

	public void setHandleIn(float x, float y) {
		handleIn.setLocation(x, y);
	}

	public void setHandleIn(double x, double y) {
		handleIn.setLocation(x, y);
	}

	public Point getHandleOut() {
		update();
		return handleOut;
	}

	public void setHandleOut(Point2D pt) {
		handleOut.setLocation(pt);
	}

	public void setHandleOut(float x, float y) {
		handleOut.setLocation(x, y);
	}

	public void setHandleOut(double x, double y) {
		handleOut.setLocation(x, y);
	}

	public boolean getCorner() {
		update();
		return corner;
	}

	public void setCorner(boolean corner) {
		update();
		this.corner = corner;
		markDirty();
	}

	public Segment divide(float parameter) {
		Curve curve = getCurve();
		if (curve == null)
			return null;
		Curve newCurve = curve.divide(parameter);
		if (newCurve != null)
			return newCurve.getSegment1();
		else
			return null;
	}
	
	public Segment divide() {
		return divide(0.5f);
	}
	
	public Curve getCurve() {
		if (segments  != null) {
			CurveList curves = segments.path.getCurves();
			// the curves list handles closing curves, so the curves.size
			// is adjusted accordingly. just check to be in the boundaries here: 
			return index < curves.size ? (Curve) curves.get(index) : null;
		} else {
			return null;
		}
	}
	
	public Object clone() {
		update();
		return new Segment(point, handleIn, handleOut, corner);
	}
}