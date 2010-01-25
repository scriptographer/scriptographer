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
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.script.ArgumentReader;
import com.scratchdisk.script.ChangeListener;
import com.scriptographer.Commitable;
import com.scriptographer.CommitManager;

/**
 * The Segment object represents a part of a path which is described by the
 * {@link Path#getSegments()} array. Every segment of a path corresponds to
 * an anchor point (anchor points are the path handles that are visible when the
 * path is selected).
 * 
 * @author lehni
 */
public class Segment implements Commitable, ChangeListener {
	protected SegmentList segments;
	protected int index;
	// The internal points
	protected SegmentPoint point;
	protected SegmentPoint handleIn;
	protected SegmentPoint handleOut;
	// Corner (hidden to the API, but needed for AI)
	protected boolean corner;
	// The selection state is fetched the first time it's used
	protected short selectionState = SELECTION_FETCH;
	//
	protected int version = -1;
	protected int selectionVersion = -1;
	protected short dirty = DIRTY_NONE;

	// Dirty flags, to be combined bitwise
	protected final static short
		DIRTY_NONE = 0,
		DIRTY_POINTS = 1,
		DIRTY_SELECTION = 2;

	// For selectionState
	protected final static short
		SELECTION_FETCH = -1,
		SELECTION_NONE = 0,
		SELECTION_POINT = 1,
		SELECTION_HANDLE_IN = 2,
		SELECTION_HANDLE_OUT = 3,
		SELECTION_HANDLE_BOTH = 4;

	public Segment() {
		init(0, 0, 0, 0, 0, 0);
	}

	/**
	 * Creates a new Segment object.
	 * 
	 * Sample code:
	 * <code>
	 * var handleIn = new Point(-40, -50);
	 * var handleOut = new Point(40, 50);
	 *
	 * var firstPoint = new Point(100, 50);
	 * var firstSegment = new Segment(firstPoint, null, handleOut);
	 *
	 * var secondPoint = new Point(200, 50);
	 * var secondSegment = new Segment(secondPoint, handleIn, null);
	 *
	 * var path = new Path();
	 * path.segments = [firstSegment, secondSegment];
	 * </code>
	 * 
	 * @param pt the anchor point of the segment
	 * @param handleIn the handle point relative to the anchor point of the
	 *        segment that describes the in tangent of the segment. {@default x:
	 *        0, y: 0}
	 * @param handleOut the handle point relative to the anchor point of the
	 *        segment that describes the out tangent of the segment. {@default
	 *        x: 0, y: 0}
	 */
	public Segment(Point pt, Point handleIn, Point handleOut) {
		init(pt, handleIn, handleOut);
	}
	
	public Segment(Point pt) {
		init(pt, null, null);
	}

	/**
	 * Creates a new Segment object.
	 * 
	 * Sample code:
	 * <code>
	 * var handleIn = new Point(-40, -50);
	 * var handleOut = new Point(40, 50);
	 *
	 * var firstSegment = new Segment(100, 50, 0, 0, handleOut.x, handleOut.y);
	 * var secondSegment = new Segment(200, 50, handleIn.x, handleIn.y, 0, 0);
	 *
	 * var path = new Path();
	 * path.segments = [firstSegment, secondSegment];
	 * </code>
	 * 
	 * @param x the x coordinate of the anchor point of the segment
	 * @param y the y coordinate of the anchor point of the segment
	 * @param inX the x coordinate of the handle point relative to the anchor
	 *        point of the segment that describes the in tangent of the segment.
	 *        {@default 0}
	 * @param inY the y coordinate of the handle point relative to the anchor
	 *        point of the segment that describes the in tangent of the segment.
	 *        {@default 0}
	 * @param outX the x coordinate of the handle point relative to the anchor
	 *        point of the segment that describes the out tangent of the
	 *        segment. {@default 0}
	 * @param outY the y coordinate of the handle point relative to the anchor
	 *        point of the segment that describes the out tangent of the
	 *        segment. {@default 0}
	 */
	public Segment(double x, double y, double inX, double inY, double outX,
			double outY) {
		init(x, y, inX, inY, outX, outY);
	}

	public Segment(double x, double y) {
		init(x, y, 0, 0, 0, 0);
	}

	/**
	 * @jshide
	 */
	public Segment(ArgumentReader reader) {
		// First try reading a point, no matter if it is a hash or a array.
		// If that does not work, fall back to other scenarios:
		Point point = getPoint(reader, "point", true);
		if (point != null) {
			init(
					point,
					getPoint(reader, "handleIn", false),
					getPoint(reader, "handleOut", false)
				);
		} else {
			reader.revert();
			if (reader.isHash()) {
				if (reader.has("x")) {
					init(
						reader.readDouble("x", 0),
						reader.readDouble("y", 0),
						0, 0, 0, 0
					);
				} 
			} else {
				init(
					reader.readDouble(0),
					reader.readDouble(0),
					reader.readDouble(0),
					reader.readDouble(0),
					reader.readDouble(0),
					reader.readDouble(0)
				);
			}
		}
	}

	private static Point getPoint(ArgumentReader reader, String name, boolean allowNull) {
		Point point = (Point) reader.readObject(name, Point.class);
		return allowNull || point != null ? point : new Point();
	}

	protected Segment(SegmentList segments, int index) {
		this();
		this.segments = segments;
		this.index = index;
	}

	/**
	 * Creates a new Segment object from the specified Segment object.
	 * 
	 * @param segment
	 */
	public Segment(Segment segment) {
		point = new SegmentPoint(this, 0, segment.point);
		handleIn = new SegmentPoint(this, 2, segment.handleIn);
		handleOut = new SegmentPoint(this, 4, segment.handleOut);
		corner = segment.corner;
	}

	protected void init(double x, double y, double inX, double inY, double outX,
			double outY) {
		point = new SegmentPoint(this, 0, x, y);
		handleIn = new SegmentPoint(this, 2, inX, inY);
		handleOut = new SegmentPoint(this, 4, outX, outY);
		// Calculate corner accordingly
		corner = !handleIn.isParallel(handleOut);
	}

	protected void init(Point pt, Point in, Point out) {
		point = new SegmentPoint(this, 0, pt);
		handleIn = new SegmentPoint(this, 2, in);
		handleOut = new SegmentPoint(this, 4, out);
		// Calculate corner accordingly
		corner = !handleIn.isParallel(handleOut);
	}

	/**
	 * Read and write directly to native segment struct, which is represented
	 * here as a float array. Byte alignment wise this works since all fields
	 * are floats except the last one which is a boolean, but aligns like float
	 * too.
	 * We use double precision for all calculations but still have to store
	 * as floats, since that's what Illustrator uses. Calculations in SG
	 * will be much more precise though.
	 * 
	 * Warning: This does not call markDirty(). This needs to be taken care of
	 * after.
	 * 
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
		values[valueIndex] = (float) point.x;
		values[valueIndex + 1] = (float) point.y;
		values[valueIndex + 2] = (float) (handleIn.x + point.x);
		values[valueIndex + 3] = (float) (handleIn.y + point.y);
		values[valueIndex + 4] = (float) (handleOut.x + point.x);
		values[valueIndex + 5] = (float) (handleOut.y + point.y);
		// don't care about the exact value for true, as long as it's != 0 it
		// works:
		values[valueIndex + 6] = corner ? 1f : 0f;
	}

	public void commit() {
		if (dirty != DIRTY_NONE && segments != null && segments.path != null
				&& segments.path.isValid()) {
			Path path = segments.path;
			if ((dirty & DIRTY_POINTS) != 0) {
				SegmentList.nativeSet(path.handle, path.document.handle, index,
						(float) point.x,
						(float) point.y,
						(float) (handleIn.x + point.x),
						(float) (handleIn.y + point.y),
						(float) (handleOut.x + point.x),
						(float) (handleOut.y + point.y),
						corner);
			}
			if ((dirty & DIRTY_SELECTION) != 0) {
				SegmentList.nativeSetSelectionState(path.handle,
						path.document.handle, index, selectionState);
			}
			dirty = DIRTY_NONE;
			// Update to current path version after commit.
			version = segments.path.version;
			path.setModified();
		}
	}

	/**
	 * inserts this segment in the underlying AI path at position index
	 * Only call once, when adding this segment to the segmentList!
	 */
	protected void insert() {
		if (segments != null && segments.path != null
				&& segments.path.isValid()) {
			Path path = segments.path;
			SegmentList.nativeInsert(path.handle, path.document.handle, index,
					(float) point.x,
					(float) point.y,
					(float) (handleIn.x + point.x),
					(float) (handleIn.y + point.y),
					(float) (handleOut.x + point.x),
					(float) (handleOut.y + point.y),
					corner);
			dirty = DIRTY_NONE;
			// Update to current version after commit.
			version = segments.path.version;
			path.setModified();
		}
	}

	protected void markDirty(int dirty) {
		// only mark it as dirty if it's attached to a path already and
		// if the given dirty flag is not already set
		if ((this.dirty & dirty) != dirty &&
			segments != null && segments.path != null) {
			CommitManager.markDirty(segments.path, this);
			this.dirty |= dirty;
		}
	}
	
	protected void update() {
		if ((dirty & DIRTY_POINTS) == 0 && segments != null
				&& segments.path != null && segments.path.needsUpdate(version)) {
			// this handles all the updating automatically:
			segments.get(index);
			// Version has changed, force regetting of selection state:
			selectionState = SELECTION_FETCH;
		} else if (selectionVersion != CommitManager.version) {
			selectionState = SELECTION_FETCH;
		}
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(64);
		buf.append("{ point: ").append(point.toString());
		if (handleIn.x != 0 || handleIn.y != 0)
			buf.append(", handleIn: ").append(handleIn.toString());
		if (handleOut.x != 0 || handleOut.y != 0)
			buf.append(", handleOut: ").append(handleOut.toString());
		buf.append(" }");
		return buf.toString();
	}

	/**
	 * The anchor point of the segment.
	 */
	public SegmentPoint getPoint() {
		update();
		return point;
	}

	public void setPoint(Point pt) {
		point.set(pt);
	}

	/**
	 * @jshide
	 */
	public void setPoint(double x, double y) {
		point.set(x, y);
	}

	/**
	 * The handle point relative to the anchor point of the segment that
	 * describes the in tangent of the segment.
	 */
	public SegmentPoint getHandleIn() {
		update();
		return handleIn;
	}

	public void setHandleIn(Point pt) {
		handleIn.set(pt);
		// Update corner accordingly
		corner = !handleIn.isParallel(handleOut);
	}

	/**
	 * @jshide
	 */
	public void setHandleIn(double x, double y) {
		handleIn.set(x, y);
	}

	/**
	 * The handle point relative to the anchor point of the segment that
	 * describes the out tangent of the segment.
	 */
	public SegmentPoint getHandleOut() {
		update();
		return handleOut;
	}

	public void setHandleOut(Point pt) {
		handleOut.set(pt);
		// Update corner accordingly
		corner = !handleIn.isParallel(handleOut);
	}

	/**
	 * @jshide
	 */
	public void setHandleOut(double x, double y) {
		handleOut.set(x, y);
	}

	/**
	 * {@grouptitle Hierarchy}
	 * 
	 * The index of the segment in the {@link Path#getSegments()} array that the
	 * segment belongs to.
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * The path that the segment belongs to.
	 */
	public Path getPath() {
		return segments != null ? segments.path : null;
	}
	
	/**
	 * The curve that the segment belongs to.
	 */
	public Curve getCurve() {
		if (segments  != null && segments.path != null) {
			CurveList curves = segments.path.getCurves();
			// The curves list handles closing curves, so the curves.size
			// is adjusted accordingly. just check to be in the boundaries here: 
			return index < curves.size() ? (Curve) curves.get(index) : null;
		} else {
			return null;
		}
	}

	/**
	 * {@grouptitle Sibling Segments}
	 * 
	 * The next segment in the {@link Path#getSegments()} array that the segment
	 * belongs to.
	 */
	public Segment getNext() {
		return index < segments.size() - 1 ? segments.get(index + 1) : null;
	}
	
	/**
	 * The previous segment in the {@link Path#getSegments()} array that the
	 * segment belongs to.
	 */
	public Segment getPrevious() {
		return index > 0 ? segments.get(index - 1) : null;
	}

	protected boolean isSelected(SegmentPoint pt) {
		update();
		if (selectionState == SELECTION_FETCH) {
			if (segments != null && segments.path != null)
				selectionState = SegmentList.nativeGetSelectionState(
						segments.path.handle, index);
			else
				selectionState = SELECTION_NONE;
			// Selection uses its own version number as it might change regardless
			// of whether the path itself changes or not.
			selectionVersion = CommitManager.version;
		}
		if (pt == point) {
			return selectionState == SELECTION_POINT;
		} else if (pt == handleIn) {
			return selectionState == SELECTION_HANDLE_IN || 
				selectionState == SELECTION_HANDLE_BOTH;
		} else if (pt == handleOut) {
			return selectionState == SELECTION_HANDLE_OUT || 
				selectionState == SELECTION_HANDLE_BOTH;
		}
		return false;
	}

	protected void setSelected(SegmentPoint pt, boolean selected) {
		update();
		// find the right combination of selection states (SELECTION_*)
		boolean pointSelected = selectionState == SELECTION_POINT;
		boolean handleInSelected = selectionState == SELECTION_HANDLE_IN || 
			selectionState == SELECTION_HANDLE_BOTH;
		boolean handleOutSelected = selectionState == SELECTION_HANDLE_OUT || 
			selectionState == SELECTION_HANDLE_BOTH;
		if (pt == point) {
			pointSelected = selected;
		} else if (pt == handleIn) {
			pointSelected = false;
			handleInSelected = selected;
		} else if (pt == handleOut) {
			pointSelected = false;
			handleOutSelected = selected;
		}
		short state;
		if (pointSelected) {
			state = SELECTION_POINT;
		} else if (handleInSelected) {
			if (handleOutSelected) {
				state = SELECTION_HANDLE_BOTH;
			} else {
				state = SELECTION_HANDLE_IN;
			}
		} else if (handleOutSelected) {
			state = SELECTION_HANDLE_OUT;
		} else {
			state = SELECTION_NONE;
		}
		// only update if it changed
		if (selectionState != state) {
			selectionState = state;
			markDirty(DIRTY_SELECTION);
		}
	}

	public Object clone() {
		update();
		return new Segment(this);
	}
}