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
	protected Path(int handle, int docHandle, boolean created) {
		super(handle, docHandle, created);
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
	 * Inserts a point to the end of the list of this path's segments
	 * ({@link #getSegments()}, by converting it to a {@link Segment}.
	 * 
	 * @param point the point to be added.
	 * @return the added segment
	 */
	public Segment add(Point point) {
		return getSegments().add(point);
	}

	/**
	 * Inserts a segment to the end of the list of this path's segments.
	 * 
	 * @param segment the segment to be added.
	 * @return the added segment. This is not necessarily the same object, e.g.
	 *         if the segment to be added already belongs to another path.
	 */
	public Segment add(Segment segment) {
		return getSegments().add(segment);
	}

	/**
	 * Inserts a point at a given index in the list of this path's segments
	 * ({@link #getSegments()}, by converting it to a {@link Segment}.
	 * 
	 * @param index the index at which to insert the point.
	 * @param point the point to be added.
	 * @return the added segment.
	 */
	public Segment add(int index, Point point) {
		return getSegments().add(index, point);
	}

	/**
	 * Inserts a segment at a given index in the list of this path's segments.
	 * 
	 * @param index the index at which to insert the segment.
	 * @param segment the segment to be added.
	 * @return the added segment. This is not necessarily the same object, e.g.
	 *         if the segment to be added already belongs to another path.
	 */
	public Segment add(int index, Segment segment) {
		return getSegments().add(index, segment);
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
	
	private void updateSize(int size) {
		// Increase version as all segments have changed
		version++;
		if (segments != null)
			segments.updateSize(size);
		
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
		version++;
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
	 *        (such as the corner radius) are multiplied. {@default 1}
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
		if (parameter < 0.0) parameter = 0.0;
		else if (parameter >= 1.0) {
			// t = 1 is the same as t = 0 and index ++
			index++;
			parameter = 0.0;
		}
		SegmentList segments = getSegments();
		CurveList curves = getCurves();
		if (index >= 0 && index < curves.size()) {
			boolean hasTabletData = hasTabletData();
			// If there is tablet data, we need to measure the offset of
			// the split point, as a value between 0 and 1
			double length, partLength;
			if (hasTabletData) {
				length = getLength();
				// Add up length of the new path by getting the curves lengths
				partLength = 0;
				for (int i = 0; i < index; i++)
					partLength += curves.get(i).getLength();
			} else {
				length = partLength = 0;
			}
			// Only divide curves if we're not on an existing segment already
			if (parameter > 0.0) {
				// Divide the curve with the index at given parameter
				Curve curve = curves.get(index);
				curve.divide(parameter);
				if (hasTabletData)
					partLength += curve.getLength();
				// Dividing adds more segments to the path
				index++;
			}
			// Create the new path with the segments to the right of given parameter
			ExtendedList<Segment> newSegments = segments.getSubList(index, segments.size());
			// If the path was closed, make it an open one and move the segments around,
			// instead of creating a new path. Otherwise create two paths.
			if (isClosed()) {
				// Changing an item's segments also seems to change user attributes,
				// i.e. selection state, so save them and restore them again.
				int attributes = getAttributes();
				newSegments.addAll(segments.getSubList(0, index + 1));
				setSegments(newSegments);
				setClosed(false);
				setAttributes(attributes);
				if (hasTabletData)
					nativeSwapTabletData(partLength / length);
				return this;
			} else if (index > 0) {
				// Delete the segments from the current path, not including the divided point
				segments.remove(index + 1, segments.size());
				// TODO: Instead of cloning, find a way to copy all necessary attributes over?
				// AIArtSuite::TransferAttributes?
				// TODO: Split TabletData arrays as well! kTransferLivePaintPathTags?
				Path newPath = (Path) clone();
				newPath.setSegments(newSegments);
				nativeSplitTabletData(partLength / length, newPath);
				return newPath;
			}
		}
		return null;
	}

	public Path split(int index) {
		return split(index, 0);
	}

	public boolean join(Path path) {
		if (path != null) {
			SegmentList segments1 = getSegments();
			SegmentList segments2 = path.getSegments();
			Segment last1 = segments1.getLast();
			Segment last2 = segments2.getLast();
			if (last1.point.equals(last2.point)) {
				path.reverse();
			}
			Segment first2 = segments2.getFirst();
			if (last1.point.equals(first2.point)) {
				last1.handleOut.set(first2.handleOut);
				segments1.addAll(segments2.getSubList(1, segments2.size()));
			} else {
				Segment first1 = segments1.getFirst();
				if (first1.point.equals(first2.point)) {
					path.reverse();
				}
				last2 = segments2.getLast();
				if (first1.point.equals(last2.point)) {
					first1.handleIn.set(last2.handleIn);
					// Prepend all segments from segments2 except last one
					segments1.addAll(0, segments2.getSubList(0, segments2.size() - 1));
				} else {
					segments1.addAll(segments2);
				}
			}
			// TODO: Tablet data!
			path.remove();
			// Close if they touch in both places
			Segment first1 = segments1.getFirst();
			last1 = segments1.getLast();
			if (last1.point.equals(first1.point)) {
				first1.handleIn.set(last1.handleIn);
				segments1.remove(segments1.size() - 1);
				setClosed(true);
			}
			return true;
		}
		return false;
	}

	public void smooth() {
		getSegments().smooth(isClosed());
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
	 * Tablet Data Stuff
	 */
	
	private static final int
			/** Stylus pressure. */
			TABLET_PRESSURE = 0,
			/** Stylus wheel pressure, also called tangential or barrel pressure */
			TABLET_BARREL_PRESSURE = 1,
			/** Tilt, also called altitude. */
			TABLET_TILT = 2,
			/** Bearing, also called azimuth. */
			TABLET_BEARING = 3,
			/** Rotation. */
			TABLET_ROTATION = 4;

	private native float[][] nativeGetTabletData(int type);
	
	private native void nativeSetTabletData(int type, float[][] data);

	private native boolean nativeSplitTabletData(double offset, Path other);

	private native boolean nativeSwapTabletData(double offset);

	/**
	 * {@grouptitle Tablet Data}
	 */

	public native boolean hasTabletData();

	public float[][] getTabletPressure() {
		return nativeGetTabletData(TABLET_PRESSURE);
	}

	public void setTabletPressure(float[][] data) {
		nativeSetTabletData(TABLET_PRESSURE, data);
	}

	public float[][] getTabletBarrelPressure() {
		return nativeGetTabletData(TABLET_BARREL_PRESSURE);
	}

	public void setTabletBarrelPressure(float[][] data) {
		nativeSetTabletData(TABLET_BARREL_PRESSURE, data);
	}

	public float[][] getTabletTilt() {
		return nativeGetTabletData(TABLET_TILT);
	}

	public void setTabletTilt(float[][] data) {
		nativeSetTabletData(TABLET_TILT, data);
	}

	public float[][] getTabletBearing() {
		return nativeGetTabletData(TABLET_BEARING);
	}

	public void setTabletBearing(float[][] data) {
		nativeSetTabletData(TABLET_BEARING, data);
	}

	public float[][] getTabletRotation() {
		return nativeGetTabletData(TABLET_ROTATION);
	}

	public void setTabletRotation(float[][] data) {
		nativeSetTabletData(TABLET_ROTATION, data);
	}

	/**
	 * @deprecated Use {@link #getTabletPressure())} instead.
	 */
	public float[][] getTabletData() {
		return nativeGetTabletData(TABLET_PRESSURE);
	}

	/**
	 * @deprecated Use {@link #setTabletPressure())} instead.
	 */
	public void setTabletData(float[][] data) {
		nativeSetTabletData(TABLET_PRESSURE, data);
	}
	
	/**
	 * {@grouptitle PostScript-style drawing commands}
	 */
	public void moveTo(double x, double y) {
		getSegments().moveTo(x, y);
	}
	
	public void lineTo(double x, double y) {
		getSegments().lineTo(x, y);
	}
	
	public void curveTo(double handle1X, double handle1Y,
			double handle2X, double handle2Y,
			double endX, double endY) {
		getSegments().curveTo(handle1X, handle1Y, handle2X, handle2Y, endX, endY);
	}

	public void curveTo(double handleX, double handleY,
			double endX, double endY) {
		getSegments().curveTo(handleX, handleY, endX, endY);
	}

	public void arcTo(double endX, double endY) {
		getSegments().arcTo(endX, endY);
	}

	public void curveThrough(double middleX, double middleY,
			double endX, double endY, double t) {
		getSegments().curveThrough(middleX, middleY, endX, endY, t);
	}

	public void arcThrough(double middleX, double middleY,
			double endX, double endY) {
		getSegments().arcThrough(middleX, middleY, endX, endY);
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
					segments.curveTo(f[0], f[1], f[2], f[3]);
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

	private static void addSegment(GeneralPath path, Segment current, Segment next) {
		Point point1 = current.point;
		Point handle1 = current.handleOut;
		Point handle2 = next.handleIn;
		Point point2 = next.point;
		if (handle1.isZero() && handle2.isZero()) {
			path.lineTo(
					(float) point2.x,
					(float) point2.y
			);
		} else {
			// TODO: Is there an easy way to detect quads?
			path.curveTo(
					(float) (point1.x + handle1.x),
					(float) (point1.y + handle1.y),
					(float) (point2.x + handle2.x),
					(float) (point2.y + handle2.y),
					(float) point2.x,
					(float) point2.y
			);
		}
	}
	/**
	 * @jshide
	 */
	public GeneralPath toShape() {
		GeneralPath path = new GeneralPath();
		SegmentList segments = getSegments();
		Segment first = segments.getFirst();
		path.moveTo((float) first.point.x, (float) first.point.y);
		Segment seg = first;
		for (int i = 1, l = segments.size(); i < l; i++) {
			Segment next = segments.get(i);
			addSegment(path, seg, next);
			seg = next;
		}
		if (isClosed()) {
			addSegment(path, seg, first);
			path.closePath();
		}
		path.setWindingRule(getStyle().getWindingRule() == WindingRule.NON_ZERO
				? GeneralPath.WIND_NON_ZERO
				: GeneralPath.WIND_EVEN_ODD);
		return path;
	}
}