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
 * File created on 03.12.2004.
 *
 * $Id$
 */

package com.scriptographer.ai;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

import com.scratchdisk.list.ExtendedList;
import com.scratchdisk.list.Lists;
import com.scratchdisk.list.ReadOnlyList;
import com.scriptographer.CommitManager;

/**
 * The Path item represents a path in an Illustrator document.
 * 
 * @author lehni
 * 
 * @jsreference {@type constructor} {@name Path.Line} {@reference Document#createLine} {@after Path}
 * @jsreference {@type constructor} {@name Path.Rectangle} {@reference Document#createRectangle} {@after Path}
 * @jsreference {@type constructor} {@name Path.RoundRectangle} {@reference Document#createRoundRectangle} {@after Path}
 * @jsreference {@type constructor} {@name Path.RegularPolygon} {@reference Document#createRegularPolygon} {@after Path}
 * @jsreference {@type constructor} {@name Path.Star} {@reference Document#createStar} {@after Path}
 * @jsreference {@type constructor} {@name Path.Spiral} {@reference Document#createSpiral} {@after Path}
 * @jsreference {@type constructor} {@name Path.Oval} {@reference Document#createOval} {@after Path}
 * @jsreference {@type constructor} {@name Path.Circle} {@reference Document#createCircle} {@after Path}
 */

public class Path extends PathItem {

	private SegmentList segments = null;
	private CurveList curves = null;

	/**
	 * Wraps an AIArtHandle in a Path object
	 */
	protected Path(int handle) {
		super(handle);
	}

	/**
	 * Creates a path object of the given type. Used by CompoundPath
	 */
	protected Path(short type) {
		super(type);
	}

	public Path() {
		super(TYPE_PATH);
	}

	/**
	 * Creates a new Path Item.
	 * 
	 * Sample code:
	 * <code>
	 * var firstSegment = new Segment(30, 30);
	 * var secondSegment = new Segment(100, 100);
	 * var path = new Path([firstSegment, secondSegment]);
	 * </code>
	 * 
	 * <code>
	 * var path = new Path();
	 * path.moveTo(30, 30);
	 * path.lineTo(100, 100);
	 * </code>
	 * 
	 * @param segments the segments to be added to the {@link #getSegments()} array
	 * @return the newly created path
	 */
	public Path(ReadOnlyList<? extends Segment> segments) {
		this();
		setSegments(segments);
	}

	public Path(Segment[] segments) {
		this(Lists.asList(segments));
	}
	
	/**
	 * @jshide
	 */
	public Path(Shape shape) {
		this();
		append(shape);
	}

	/**
	 * Removes the path item from the document.
	 */
	public boolean remove() {
        boolean ret = super.remove();
        // Dereference from path if they're used somewhere else!
        if (segments != null)
            segments.path = null;
        return ret;
    }
    
	public Object clone() {
		CommitManager.commit(this);
		return super.clone();
	}

	/**
	 * The segments contained within the path.
	 */
	public SegmentList getSegments() {
		if (segments == null)
			segments = new SegmentList(this);
		else
			segments.update();
		return segments;
	}

	public void setSegments(ReadOnlyList<? extends Segment> segments) {
		SegmentList segs = getSegments();
		// TODO: Implement SegmentList.setAll so removeAll is not necessary and
		// nativeCommit is used instead of nativeInsert removeRange would still
		// be needed in cases the new list is smaller than the old one...
		segs.removeAll();
		segs.addAll(segments);
	}

	public void setSegments(Segment[] segments) {
		setSegments(Lists.asList(segments));
	}

	/**
	 * The curves contained within the path.
	 */
	public CurveList getCurves() {
		if (curves == null)
			curves = new CurveList(this, getSegments());
		return curves;
	}

	/**
	 * Specifies whether the path is closed. If it is closed, Illustrator
	 * connects the first and last segments.
	 */
	public native boolean isClosed();
	
	private native void nativeSetClosed(boolean closed);
	
	public void setClosed(boolean closed) {
		// Amount of curves may change when closed is modified
		nativeSetClosed(closed);
		if (curves != null)
			curves.updateSize();
	}
	
	/**
	 * Specifies whether the path is used as a guide.
	 */
	public native boolean isGuide();
	
	public native void setGuide(boolean guide);
	
	public native TabletValue[] getTabletData();
	
	public native void setTabletData(TabletValue[] data);
	
	public void setTabletData(float[][] data) {
		// Convert to a TabletValue[] data array:
		ArrayList<TabletValue> values = new ArrayList<TabletValue>();
		for (int i = 0; i < data.length; i++) {
			float[] pair = data[i];
			if (pair != null && pair.length >= 2)
				values.add(new TabletValue(pair[0], pair[1]));
		}
		setTabletData(values.toArray(new TabletValue[values.size()]));
	}

	/**
	 * The length of the perimeter of the path.
	 */
	public native double getLength();

	/**
	 * The area of the path in square points. Self-intersecting paths can
	 * contain sub-areas that cancel each other out.
	 */
	public native float getArea();

	private native void nativeReverse();

	/**
	 * Reverses the segments of the path.
	 */
	public void reverse() {
		// First save all changes:
		CommitManager.commit(this);
		// Reverse underlying AI structures:
		nativeReverse();
		// Increase version as all segments have changed
		this.version++;
	}
	
	private void updateSize(int size) {
		// increase version as all segments have changed
		this.version++;
		if (segments != null)
			segments.updateSize(size);
		
	}

	private native int nativePointsToCurves(float tolerance, float threshold,
			int cornerRadius, float scale);

	/**
	 * Approximates the path by converting the points in the path to curves. It
	 * only uses the {@link Segment#getPoint()} property of each segment and
	 * ignores the {@link Segment#getHandleIn()} and
	 * {@link Segment#getHandleOut()} properties.
	 * 
	 * @param tolerance a smaller tolerance gives a more exact fit and more
	 *        segments, a larger tolerance gives a less exact fit and fewer
	 *        segments. {@default 2.5}
	 * @param threshold {@default 1}
	 * @param cornerRadius if, at any point in the fitted curve, the radius of
	 *        an inscribed circle that has the same tangent and curvature is
	 *        less than the cornerRadius, a corner point is generated there;
	 *        otherwise the path is smooth at that point. {@default 1}
	 * @param scale the scale factor by which the points and other input units
	 *        (such as the corner radius) are multiplied {@default 1}
	 */
	public void pointsToCurves(float tolerance, float threshold,
			int cornerRadius, float scale) {
		updateSize(nativePointsToCurves(tolerance, threshold, cornerRadius,
				scale));
	}

	public void pointsToCurves(float tolerance, float threshold,
			int cornerRadius) {
		pointsToCurves(tolerance, threshold, cornerRadius, 1f);
	}

	public void pointsToCurves(float tolerance, float threshold) {
		pointsToCurves(tolerance, threshold, 1, 1f);
	}

	public void pointsToCurves(float tolerance) {
		pointsToCurves(tolerance, 1f, 1, 1f);
	}

	public void pointsToCurves() {
		pointsToCurves(2.5f, 1f, 1, 1f);
	}

	private native int nativeCurvesToPoints(float maxPointDistance,
			float flatness);

	/**
	 * Converts the curves in the path to points.
	 * 
	 * @param maxPointDistance the maximum distance between the generated points
	 *        {@default 1000}
	 * @param flatness a value which controls the exactness of the algorithm
	 *        {@default 0.1}
	 */
	public void curvesToPoints(double maxPointDistance, double flatness) {
		int size = nativeCurvesToPoints((float) maxPointDistance, (float) flatness);
		updateSize(size);
	}

	public void curvesToPoints(double maxPointDistance) {
		curvesToPoints(maxPointDistance, 0.1f);
	}

	public void curvesToPoints() {
		curvesToPoints(1000f, 0.1f);
	}

	private native void nativeReduceSegments(float flatness);

	/**
	 * Reduces the amount of segments in the path.
	 * 
	 * @param flatness a value which controls the exactness of the algorithm
	 *        {@default 0.1}
	 */
	public void reduceSegments(double flatness) {
		nativeReduceSegments((float) flatness);
		updateSize(-1);
	}

	public void reduceSegments() {
		reduceSegments(0.1f);
	}
	
	public Path split(double length) {
		return split(getPositionWithLength(length));
	}

	public Path split(HitResult position) {
		return split(position.getIndex(), position.getParameter());
	}

	public Path split(int index, double parameter) {
		SegmentList segments = getSegments();
		ExtendedList<Segment> newSegments = null;

		if (parameter < 0.0f) parameter = 0.0f;
		else if (parameter >= 1.0f) {
			// t = 1 is the same as t = 0 and index ++
			index++;
			parameter = 0.0f;
		}
		int numSegments = segments.size();
		if (index >= 0 && index < numSegments - 1) {
			if (parameter == 0.0) { // special case
				if (index > 0) {
					// split at index
					newSegments = segments.getSubList(index, numSegments);
					segments.remove(index + 1, numSegments);
				}
			} else {
				// divide the segment at index at parameter
				Segment segment = (Segment) segments.get(index);
				if (segment != null) {
					segment.split(parameter);
					// create the new path with the segments to the right of t
					newSegments = segments.getSubList(index + 1, numSegments);
					// and delete these segments from the current path, not
					// including the divided point
					segments.remove(index + 2, numSegments);
				}
			}
		}
		if (newSegments != null)
			return new Path(newSegments);
		else
			return null;
	}

	public Path split(int index) {
		return split(index, 0);
	}

	// No need to expose these since they override the native one.
	// TODO: Decide if maybe useful under different name?
	/*
	public HitResult hitTest(Point point, double precision) {
		CurveList curves = getCurves();
		int length = curves.size();
		
		for (int i = 0; i < length; i++) {
			Curve curve = (Curve) curves.get(i);
			double t = curve.hitTest(point, precision);
			if (t >= 0)
				return new HitResult(curve, t);
		}
		return null;
	}

	public HitResult hitTest(Point point) {
		return hitTest(point, Curve.EPSILON);
	}
	*/

	// TODO: move to CurveList, to make accessible when not using
	// paths directly too?
	public HitResult getPositionWithLength(double length) {
		CurveList curves = getCurves();
		double currentLength = 0;
		for (int i = 0, l = curves.size(); i < l; i++) {
			double startLength = currentLength;
			Curve curve = (Curve) curves.get(i);
			currentLength += curve.getLength();
			if (currentLength >= length) {
				// found the segment within which the length lies
				double t = curve.getParameterWithLength(length - startLength);
				return new HitResult(curve, t);
			}
		}
		// it may be that through impreciseness of getLength, that the end of
		// the curves was missed:
		if (length <= getLength()) {
			Curve curve = (Curve) curves.getLast();
			return new HitResult(curve, 1);
		} else {
			return null;
		}
	}

	/*
	 *  PostScript-like interface: moveTo, lineTo, curveTo, arcTo
	 */

	public void moveTo(double x, double y) {
		getSegments().moveTo(x, y);
	}
	
	public void lineTo(double x, double y) {
		getSegments().lineTo(x, y);
	}
	
	public void curveTo(double c1x, double c1y, double c2x, double c2y,
			double x, double y) {
		getSegments().curveTo(c1x, c1y, c2x, c2y, x, y);
	}
	
	public void quadTo(double cx, double cy, double x, double y) {
		getSegments().quadTo(cx, cy, x, y);
	}
	
	public void arcTo(double middleX, double middleY, double endX, double endY) {
		getSegments().arcTo(middleX, middleY, endX, endY);
	}

	public void arcTo(double endX, double endY) {
		getSegments().arcTo(endX, endY);
	}

	/**
	 * Closes the path. If it is closed, Illustrator connects the first and last
	 * segments.
	 */
	public void closePath() {
		setClosed(true);
	}

	/*
	 * Convert to and from Java2D (java.awt.geom)
	 */

	/**
	 * Appends the segments of a PathIterator to this Path. Optionally, the
	 * initial {@link PathIterator#SEG_MOVETO}segment of the appended path is
	 * changed into a {@link PathIterator#SEG_LINETO}segment.
	 * 
	 * @param iter the PathIterator specifying which segments shall be appended.
	 * @param connect {@code true} for substituting the initial
	 *        {@link PathIterator#SEG_MOVETO}segment by a {@link
	 *        PathIterator#SEG_LINETO}, or {@code false} for not
	 *        performing any substitution. If this GeneralPath is currently
	 *        empty, {@code connect} is assumed to be {@code false},
	 *        thus leaving the initial {@link PathIterator#SEG_MOVETO}unchanged.
	 * @jshide
	 */
	public void append(PathIterator iter, boolean connect) {
		float[] f = new float[6];
		SegmentList segments = getSegments();
		int size = segments.size();
		boolean open = true;
		while (!iter.isDone() && open) {
			switch (iter.currentSegment(f)) {
				case PathIterator.SEG_MOVETO:
					if (!connect || (size == 0)) {
						moveTo(f[0], f[1]);
						break;
					}
					if (size >= 1) {
						Point pt = ((Segment) segments.getLast()).point;
						if (pt.x == f[0] && pt.y == f[1])
							break;
					}
					// Fall through to lineto for connect!
				case PathIterator.SEG_LINETO:
					segments.lineTo(f[0], f[1]);
					break;
				case PathIterator.SEG_QUADTO:
					quadTo(f[0], f[1], f[2], f[3]);
					break;
				case PathIterator.SEG_CUBICTO:
					segments.curveTo(f[0], f[1], f[2], f[3], f[4], f[5]);
					break;
				case PathIterator.SEG_CLOSE:
					setClosed(true);
					open = false;
					break;
			}

			// connect = false;
			iter.next();
		}
	}

	/**
	 * @jshide
	 */
	public GeneralPath toShape() {
		GeneralPath path = new GeneralPath();
		SegmentList segments = getSegments();
		Segment first = (Segment) segments.getFirst();
		path.moveTo((float) first.point.x, (float) first.point.y);
		Segment seg = first;
		for (int i = 1, l = segments.size(); i < l; i++) {
			Segment next = (Segment) segments.get(i);
			path.curveTo((float) (seg.point.x + seg.handleOut.x),
					(float) (seg.point.y + seg.handleOut.y),
					(float) (next.point.x + next.handleIn.x),
					(float) (next.point.y + next.handleIn.y),
					(float) next.point.x,
					(float) next.point.y);
			seg = next;
		}
		if (isClosed()) {
			path.curveTo((float) (seg.point.x + seg.handleOut.x),
					(float) (seg.point.y + seg.handleOut.y),
					(float) (first.point.x + first.handleIn.x),
					(float) (first.point.y + first.handleIn.y),
					(float) first.point.x,
					(float) first.point.y);
			path.closePath();
		}
		path.setWindingRule(getStyle().getWindingRule() == WindingRule.NON_ZERO
				? GeneralPath.WIND_NON_ZERO
				: GeneralPath.WIND_EVEN_ODD);
		return path;
	}
}