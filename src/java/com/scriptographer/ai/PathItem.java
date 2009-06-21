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
 * File created on Mar 21, 2008.
 *
 * $Id$
 */

package com.scriptographer.ai;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

/**
 * @author lehni
 *
 * @jshide
 */
public abstract class PathItem extends Item {
	/**
	 * Wraps an AIArtHandle in a Path object
	 */
	protected PathItem(int handle) {
		super(handle);
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

	public abstract void moveTo(double x, double y);	

	public abstract void lineTo(double x, double y);

	public abstract void curveTo(double c1x, double c1y, double c2x, double c2y,
			double x, double y);
	
	public abstract void quadTo(double cx, double cy, double x, double y);

	public abstract void arcTo(double middleX, double middleY, double endX, double endY);
	
	public abstract void arcTo(double endX, double endY);

	public abstract void closePath();

	public void moveTo(Point pt) {
		if (pt == null) moveTo(0, 0);
		else moveTo(pt.x, pt.y);
	}

	public void lineTo(Point pt) {
		if (pt == null) lineTo(0, 0);
		else lineTo(pt.x, pt.y);
	}

	public void curveTo(Point c1, Point c2, Point pt) {
		curveTo(c1 != null ? c1.x : 0, c1 != null ? c1.y : 0,
				c2 != null ? c2.x : 0, c2 != null ? c2.y : 0,
				pt != null ? pt.x : 0, pt != null ? pt.y : 0);
	}

	public void quadTo(Point c, Point pt) {
		quadTo(c != null ? c.x : 0, c != null ? c.y : 0,
				pt != null ? pt.x : 0, pt != null ? pt.y : 0);
	}

	public void arcTo(Point center, Point endPoint) {
		arcTo(center != null ? center.x : 0, center != null ? center.y : 0,
				endPoint != null ? endPoint.x : 0, endPoint != null ? endPoint.y : 0);
	}

	public void arcTo(Point endPoint) {
		arcTo(endPoint != null ? endPoint.x : 0, endPoint != null ? endPoint.y : 0);
	}

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
	 * Checks if the interior of the path intersects with the interior of the
	 * specified path.
	 * 
	 * @param item
	 * @return {@true if the paths intersect}
	 */
	public boolean intersects(PathItem item) {
		Area area = new Area(this.toShape());
		area.intersect(new Area(item.toShape()));
		return area.isEmpty();
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
	 * Returns the intersection of the paths as a new path
	 * 
	 * @param item
	 * @return
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
	 * @return
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
	 * @return
	 */
	public PathItem exclude(PathItem item) {
		Area area = new Area(this.toShape());
		area.subtract(new Area(item.toShape()));
		CompoundPath compoundPath = new CompoundPath(area);
		compoundPath.setStyle(this.getStyle());
		return compoundPath.simplify();
	}
}
