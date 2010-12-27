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
 * File created on 03.12.2004.
 */

package com.scriptographer.ai;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

import com.scratchdisk.list.ExtendedArrayList;
import com.scratchdisk.list.ExtendedList;
import com.scratchdisk.list.List;
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
 * @jsreference {@type constructor} {@name Path.Arc} {@reference Document#createArc} {@after Path}
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
	 * Adds one segment to the end of the segment list of this path.
	 * 
	 * @param segment the segment or point to be added.
	 * @return the added segment. This is not necessarily the same object, e.g.
	 *         if the segment to be added already belongs to another path.
	 */
	public Segment add(Segment segment) {
		return getSegments().add(segment);
	}

	/**
	 * @jshide for now. TODO: Implement varargs in doclet Adds a variable amount
	 *         of segments at the end of the segment list of this path.
	 * 
	 * @return the added segments. These are not necessarily the same objects,
	 *         e.g. if the segments to be added already belongs to another path.
	 */
	public ReadOnlyList<? extends Segment> add(Segment... segments) {
		SegmentList segs = getSegments();
		int start = segs.size();
		segs.addAll(Lists.asList(segments));
		return segs.getSubList(start, segs.size());
	}

	/**
	 * Inserts a segment at a given index in the list of this path's segments.
	 * 
	 * @param index the index at which to insert the segment.
	 * @param segment the segment or point to be inserted.
	 * @return the added segment. This is not necessarily the same object, e.g.
	 *         if the segment to be added already belongs to another path.
	 */
	public Segment insert(int index, Segment segment) {
		return getSegments().add(index, segment);
	}

	/**
	 * @jshide for now. TODO: Implement varargs in doclet Inserts a variable
	 *         amount of segment at a given index in the segment list of this
	 *         path.
	 * 
	 * @param index the index at which to insert the segments.
	 * 
	 * @return the added segments. These is not necessarily the same objects,
	 *         e.g. if the segments to be added already belongs to another path.
	 */
	public ReadOnlyList<? extends Segment> insert(int index, Segment... segments) {
		SegmentList segs = getSegments();
		// Remember previous size so we can find out how many were really added
		int before = segs.size();
		segs.addAll(index, Lists.asList(segments));
		return segs.getSubList(index, index + segs.size() - before);
	}

	public Segment remove(int index) {
		return getSegments().remove(index);
	}

	public Segment remove(Segment segment) {
		return getSegments().remove(segment);
	}

	/**
	 * @jshide for now. TODO: Implement varargs in doclet Adds a variable amount
	 *         of segments at the end of the segment list of this path.
	 * 
	 * @return the added segments. These are not necessarily the same objects,
	 *         e.g. if the segments to be added already belongs to another path.
	 */
	public ReadOnlyList<? extends Segment> remove(Segment... segments) {
		SegmentList segs = getSegments();
		ExtendedArrayList<Segment> removed = new ExtendedArrayList<Segment>(segments);
		removed.retainAll(segs);
		if (segs.removeAll(Lists.asList(segments)))
			return removed;
		return null;
	}

	public ReadOnlyList<? extends Segment> remove(int fromIndex, int toIndex) {
		SegmentList segs = getSegments();
		ReadOnlyList<? extends Segment> removed = segs.getSubList(fromIndex, toIndex);
		segs.remove(fromIndex, toIndex);
		return removed;
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
		return split(getLocation(length));
	}

	public Path split(CurveLocation location) {
		return split(location.getIndex(), location.getParameter());
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

	/**
	 * Smooth bezier curves without changing the amount of segments or their
	 * points, by only smoothing and adjusting their handle points, for both
	 * open ended and closed paths.
	 * 
	 * @author Oleg V. Polikarpotchkin
	 */
	public void smooth() {
		getSegments().smooth(isClosed());
	}

	public CurveLocation getLocation(Point point, double precision) {
		CurveList curves = getCurves();
		int length = curves.size();
		
		for (int i = 0; i < length; i++) {
			Curve curve = curves.get(i);
			double t = curve.getParameter(point, precision);
			if (t >= 0)
				return new CurveLocation(curve, t);
		}
		return null;
	}

	public CurveLocation getLocation(Point point) {
		return getLocation(point, Curve.EPSILON);
	}

	// TODO: move to CurveList, to make accessible when not using
	// paths directly too?
	public CurveLocation getLocation(double length) {
		CurveList curves = getCurves();
		double currentLength = 0;
		for (int i = 0, l = curves.size(); i < l; i++) {
			double startLength = currentLength;
			Curve curve = curves.get(i);
			currentLength += curve.getLength();
			if (currentLength >= length) {
				// found the segment within which the length lies
				double t = curve.getParameter(length - startLength);
				return new CurveLocation(curve, t);
			}
		}
		// it may be that through impreciseness of getLength, that the end of
		// the curves was missed:
		if (length <= getLength()) {
			Curve curve = curves.getLast();
			return new CurveLocation(curve, 1);
		}
		return null;
	}

	/**
	 * @deprecated
	 */
	public CurveLocation getPositionWithLength(double length) {
		return getLocation(length);
	}

	public double getLength(CurveLocation location) {
		int index = location.getIndex();
		if (index != -1) {
			double length = 0;
			CurveList curves = getCurves();
			for (int i = 0; i < index; i++)
				length += curves.get(i).getLength();
			// Clone the curve as we're going to divide it to get the length.
			// Without cloning it, this would modify the path.
			Curve curve = (Curve) curves.get(index).clone();
			curve.divide(location.getParameter());
			return length + curve.getLength();
		}
		return -1;
	}

	/**
	 * @deprecated
	 */
	public double getLengthOfPosition(CurveLocation location) {
		return getLength(location);
	}

	/**
	 * Returns the point of the path at the given length.
	 */
	public Point getPoint(double length) {
		CurveLocation loc = getLocation(length);
		if (loc != null)
			return loc.getPoint();
		return null;
	}

	/**
	 * Returns the tangent to the path at the given length as a vector point.
	 */
	public Point getTangent(double length) {
		CurveLocation loc = getLocation(length);
		if (loc != null)
			return loc.getCurve().getTangent(loc.getParameter());
		return null;
	}

	/**
	 * Returns the normal to the path at the given length as a vector point.
	 */
	public Point getNormal(double length) {
		CurveLocation loc = getLocation(length);
		if (loc != null)
			return loc.getCurve().getNormal(loc.getParameter());
		return null;
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

	public native boolean hasTabletData();

	/**
	 * {@grouptitle Tablet Data}
	 */
	public float[][] getTabletPressure() {
		return nativeGetTabletData(TABLET_PRESSURE);
	}

	public void setTabletPressure(float[][] data) {
		nativeSetTabletData(TABLET_PRESSURE, data);
	}

	public float[][] getTabletWheel() {
		return nativeGetTabletData(TABLET_BARREL_PRESSURE);
	}

	public void setTabletWheel(float[][] data) {
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
	
	@Override
	public void moveTo(double x, double y) {
		getSegments().moveTo(x, y);
	}
	
	@Override
	public void lineTo(double x, double y) {
		getSegments().lineTo(x, y);
	}
	
	@Override
	public void cubicCurveTo(double handle1X, double handle1Y, double handle2X,
			double handle2Y, double toX, double toY) {
		getSegments().cubicCurveTo(handle1X, handle1Y, handle2X, handle2Y,
				toX, toY);
	}

	@Override
	public void quadraticCurveTo(double handleX, double handleY,
			double toX, double toY) {
		getSegments().quadraticCurveTo(handleX, handleY, toX, toY);
	}

	@Override
	public void curveTo(double throughX, double throughY,
			double toX, double toY, double parameter) {
		getSegments().curveTo(throughX, throughY, toX, toY, parameter);
	}

	@Override
	public void arcTo(double x, double y, boolean clockwise) {
		getSegments().arcTo(x, y, clockwise);
	}

	@Override
	public void arcTo(double throughX, double throughY, double toX, double toY) {
		getSegments().arcTo(throughX, throughY, toX, toY);
	}

	@Override
	public void lineBy(double x, double y) {
		getSegments().lineBy(x, y);
	}

	@Override
	public void curveBy(double throughX, double throughY,
			double toX, double toY, double parameter) {
		getSegments().curveBy(throughX, throughY, toX, toY, parameter);
	}

	@Override
	public void arcBy(double x, double y, boolean clockwise) {
		getSegments().arcBy(x, y, clockwise);
	}

	@Override
	public void arcBy(double throughX, double throughY, double toX, double toY) {
		getSegments().arcBy(throughX, throughY, toX, toY);
	}

	@Override
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
	 *        {@link PathIterator#SEG_MOVETO}segment by a
	 *        {@link PathIterator#SEG_LINETO}, or {@code false} for not
	 *        performing any substitution. If this GeneralPath is currently
	 *        empty, {@code connect} is assumed to be {@code false}, thus
	 *        leaving the initial {@link PathIterator#SEG_MOVETO}unchanged.
	 * @jshide
	 */
	@Override
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
						Point pt = segments.getLast().point;
						if (pt.x == f[0] && pt.y == f[1])
							break;
					}
					// Fall through to lineto for connect!
				case PathIterator.SEG_LINETO:
					segments.lineTo(f[0], f[1]);
					break;
				case PathIterator.SEG_QUADTO:
					segments.quadraticCurveTo(f[0], f[1], f[2], f[3]);
					break;
				case PathIterator.SEG_CUBICTO:
					segments.cubicCurveTo(f[0], f[1], f[2], f[3], f[4], f[5]);
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

	private static void addSegment(GeneralPath path, Segment current,
			Segment next) {
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

	protected List<Curve> getAllCurves() {
		return getCurves();
	}
}