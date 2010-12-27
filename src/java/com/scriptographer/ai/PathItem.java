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
 * File created on Mar 21, 2008.
 */

package com.scriptographer.ai;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

import com.scratchdisk.list.List;

/**
 * @author lehni
 */
public abstract class PathItem extends Item {
	/**
	 * Wraps an AIArtHandle in a Path object
	 */
	protected PathItem(int handle, int docHandle, boolean created) {
		super(handle, docHandle, created);
	}

	/**
	 * Creates a path object of the given type. Used by CompoundPath
	 */
	protected PathItem(short type) {
		super(type);
	}

	/*
	 * Convert to and from Java2D (java.awt.geom)
	 */
	
	/**
	 * @jshide
	 */
	public abstract GeneralPath toShape();

	/**
	 * @jshide
	 */
	public abstract void append(PathIterator iter, boolean connect);

	/*
	 *  PostScript-like interface: moveTo, lineTo, curveTo, arcTo
	 */

	/**
	 * {@grouptitle PostScript-style drawing commands}
	 */

	public void moveTo(Point point) {
		if (point == null) moveTo(0, 0);
		else moveTo(point.x, point.y);
	}

	public abstract void moveTo(double x, double y);	

	public void lineTo(Point point) {
		if (point == null) lineTo(0, 0);
		else lineTo(point.x, point.y);
	}

	public abstract void lineTo(double x, double y);

	public void cubicCurveTo(Point handle1, Point handle2, Point to) {
		cubicCurveTo(handle1 != null ? handle1.x : 0,
				handle1 != null ? handle1.y : 0,
				handle2 != null ? handle2.x : 0,
				handle2 != null ? handle2.y : 0,
				to != null ? to.x : 0,
				to != null ? to.y : 0);
	}

	public abstract void cubicCurveTo(double handle1X, double handle1Y,
			double handle2X, double handle2Y, double toX, double toY);

	/**
	 * @deprecated
	 */
	public void curveTo(Point handle1, Point handle2, Point to) {
		cubicCurveTo(handle1, handle2, to);
	}

	/**
	 * @deprecated
	 */
	public void curveTo(double handle1X, double handle1Y,
			double handle2X, double handle2Y, double toX, double toY) {
		cubicCurveTo(handle1X, handle1Y, handle2X, handle2Y, toX, toY);
	}

	public void quadraticCurveTo(Point handle, Point to) {
		quadraticCurveTo(handle != null ? handle.x : 0,
				handle != null ? handle.y : 0,
				to != null ? to.x : 0,
				to != null ? to.y : 0);
	}

	public abstract void quadraticCurveTo(double handleX, double handleY,
			double toX, double toY);

	/**
	 * @deprecated
	 */
	public void quadTo(Point handle, Point to) {
		quadraticCurveTo(handle, to);
	}

	/**
	 * @deprecated
	 */
	public void quadTo(double handleX, double handleY, double toX, double toY) {
		quadraticCurveTo(handleX, handleY, toX, toY);
	}

	public void curveTo(Point through, Point to, double parameter) {
		curveTo(through != null ? through.x : 0,
				through != null ? through.y : 0,
				to != null ? to.x : 0,
				to != null ? to.y : 0,
				parameter);
	}

	public void curveTo(Point through, Point to) {
		curveTo(through, to, 0.5);
	}

	public abstract void curveTo(double throughX, double throughY,
			double toX, double toY, double t);

	public void curveTo(double throughX, double throughY,
			double toX, double toY) {
		curveTo(throughX, throughY, toX, toY, 0.5);
	}

	/**
	 * @deprecated
	 */
	public void curveThrough(Point through, Point to, double parameter) {
		curveTo(through, to, parameter);
	}

	/**
	 * @deprecated
	 */
	public void curveThrough(double throughX, double throughY,
			double toX, double toY, double parameter) {
		curveTo(throughX, throughY, toX, toY, parameter);
	}

	/**
	 * @deprecated
	 */
	public void curveThrough(Point through, Point to) {
		curveTo(through, to);
	}

	/**
	 * @deprecated
	 */
	public void curveThrough(double throughX, double throughY,
			double toX, double toY) {
		curveTo(throughX, throughY, toX, toY);
	}

	public void arcTo(Point point, boolean clockwise) {
		arcTo(point != null ? point.x : 0,
				point != null ? point.y : 0,
				clockwise);
	}

	public void arcTo(Point point) {
		arcTo(point, true);
	}

	public abstract void arcTo(double x, double y, boolean clockwise);

	public void arcTo(double x, double y) {
		arcTo(x, y, true);
	}

	public void arcTo(Point through, Point to) {
		arcTo(through != null ? through.x : 0,
				through != null ? through.y : 0,
				to != null ? to.x : 0,
				to != null ? to.y : 0);
	}

	public abstract void arcTo(double throughX, double throughY,
			double toX, double toY);

	/**
	 * @deprecated
	 */
	public void arcThrough(Point through, Point to) {
		arcTo(through.x, through.y, to.x, to.y);
	}
	
	/**
	 * @deprecated
	 */
	public void arcThrough(double throughX, double throughY, double toX,
			double toY) {
		arcTo(throughX, throughY, toX, toY);
	}

	public void lineBy(Point vector) {
		if (vector != null)
			lineBy(vector.x, vector.y);
	}

	public abstract void lineBy(double x, double y);	

	public void curveBy(Point throughVector, Point toVector, double parameter) {
		curveBy(throughVector != null ? throughVector.x : 0,
				throughVector != null ? throughVector.y : 0,
				toVector != null ? toVector.x : 0,
				toVector != null ? toVector.y : 0,
				parameter);
	}

	public void curveBy(Point throughVector, Point toVector) {
		curveBy(throughVector, toVector, 0.5);
	}

	public abstract void curveBy(double throughX, double throughY,
			double toX, double toY, double parameter);

	public void curveBy(double throughX, double throughY,
			double toX, double toY) {
		curveBy(throughX, throughY, toX, toY, 0.5);
	}

	public void arcBy(Point vector, boolean clockwise) {
		arcBy(vector != null ? vector.x : 0,
				vector != null ? vector.y : 0,
				clockwise);
	}

	public void arcBy(Point vector) {
		arcBy(vector, true);
	}

	public abstract void arcBy(double x, double y, boolean clockwise);

	public void arcBy(double x, double y) {
		arcBy(x, y, true);
	}
	public void arcBy(Point throughVector, Point toVector) {
		arcBy(throughVector != null ? throughVector.x : 0,
				throughVector != null ? throughVector.y : 0,
				toVector != null ? toVector.x : 0,
				toVector != null ? toVector.y : 0);
	}

	public abstract void arcBy(double throughX, double throughY,
			double toX, double toY);

	/**
	 * Closes the path. If it is closed, Illustrator connects the first and last
	 * segments.
	 */
	public abstract void closePath();


	/**
	 * @jshide
	 */
	public void append(PathIterator iter) {
		append(iter, false);
	}

	/**
	 * Appends the segments of a Shape to the path. If {@code connect} is
	 * true, the new path segments are connected to the existing one with a
	 * line. The winding rule of the Shape is ignored.
	 * @jshide
	 */
	public void append(Shape shape, boolean connect) {
		append(shape.getPathIterator(null), connect);
	}

	/**
	 * @jshide
	 */
	public void append(Shape shape) {
		append(shape.getPathIterator(null), false);
	}

	/**
	 * {@grouptitle Geometric Tests}
	 * 
	 * Checks if the interior of the path intersects with the interior of the
	 * specified path.
	 * 
	 * @param item
	 * @return {@true if the paths intersect}
	 */
	public boolean intersects(PathItem item) {
		Area area = new Area(this.toShape());
		area.intersect(new Area(item.toShape()));
		return !area.isEmpty();
	}

	/**
	 * Checks if the interior of the path contains the interior of the specified
	 * path.
	 * 
	 * @param item
	 * @return {@true if the path contains the specified path}
	 */
	public boolean contains(PathItem item) {
		Area area = new Area(item.toShape());
		area.subtract(new Area(this.toShape()));
		return area.isEmpty();
	}

	/**
	 * Checks if the specified point is contained within the interior of the path.
	 * 
	 * @param point
	 * @return {@true if the point is contained within the path}
	 */
	public boolean contains(Point point) {
		return new Area(this.toShape()).contains(point.toPoint2D());
	}

	/**
	 * {@grouptitle Boolean Operations}
	 * 
	 * Returns the intersection of the paths as a new path
	 * 
	 * @param item
	 */
	public PathItem intersect(PathItem item) {
		Area area = new Area(this.toShape());
		area.intersect(new Area(item.toShape()));
		CompoundPath compoundPath = new CompoundPath(area);
		compoundPath.setStyle(this.getStyle());
		return compoundPath.simplify();
	}

	/**
	 * Adds the shape of the specified path to the path and returns it as a new
	 * path.
	 * 
	 * @param item
	 */
	public PathItem unite(PathItem item) {
		Area area = new Area(this.toShape());
		area.add(new Area(item.toShape()));
		CompoundPath compoundPath = new CompoundPath(area);
		compoundPath.setStyle(this.getStyle());
		return compoundPath.simplify();
	}

	/**
	 * Subtracts the shape of the specified path from the path and returns it as
	 * a new path.
	 * 
	 * @param item
	 */
	public PathItem exclude(PathItem item) {
		Area area = new Area(this.toShape());
		area.subtract(new Area(item.toShape()));
		CompoundPath compoundPath = new CompoundPath(area);
		compoundPath.setStyle(this.getStyle());
		return compoundPath.simplify();
	}

	/**
	 * Returns all curves contained in the Item. For {@link Path} items this is
	 * the same as {@link Path#getCurves}, for {@link CompoundPath} items it
	 * returns the curves of all the {@link Path} items contained inside.
	 */
	protected abstract List<Curve> getAllCurves();

	/**
	 * Returns all interesections between two {@link Path} items in an array of
	 * {@link CurveLocation} objects. {@link CompoundPath} items are support
	 * too.
	 */
	public CurveLocation[] getIntersections(PathItem path) {
		// First check the bounds of the two paths. If they don't intersect,
		// we don't need to iterate through the whole path.
		if (!getBounds().intersects(path.getBounds()))
			return new CurveLocation[0];
		ArrayList<CurveLocation> locations = new ArrayList<CurveLocation>();
		List<Curve> curves1 = getAllCurves(), curves2 = path.getAllCurves();
		int length1 = curves1.size(), length2 = curves2.size();
		// Convert curves2 to curve values, as we're looping through them for
		// each curve in curves1.
		double[][][] curvesValues2 = new double[length2][][];
		for (int i = 0; i < length2; i++)
			curvesValues2[i] = curves2.get(i).getCurveValues();
		// Now loop through each curve in curves1 and get intersections with
		// the curves in curves2.
		for (int i = 0; i < length1; i++) {
			Curve curve = curves1.get(i);
			double[][] curveValues = curve.getCurveValues();
			for (int j = 0; j < length2; j++)
				Curve.getIntersections(curve, curveValues, curvesValues2[j],
						locations);
		}
		return locations.toArray(new CurveLocation[locations.size()]);
	}
}
