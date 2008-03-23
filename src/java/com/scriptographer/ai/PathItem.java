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
 */
abstract class PathItem extends Item {
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
	
	public abstract GeneralPath toShape();

	public abstract void append(PathIterator iter, boolean connect);

	/*
	 *  PostScript-like interface: moveTo, lineTo, curveTo, arcTo
	 */

	public abstract void moveTo(float x, float y);	

	public abstract void lineTo(float x, float y);

	public abstract void curveTo(float c1x, float c1y, float c2x, float c2y,
			float x, float y);
	
	public abstract void quadTo(float cx, float cy, float x, float y);

	public abstract void arcTo(float centerX, float centerY, float endX,
			float endY, int ccw);
	
	public abstract void closePath();

	public void moveTo(Point pt) {
		moveTo(pt.x, pt.y);
	}

	public void lineTo(Point pt) {
		lineTo(pt.x, pt.y);
	}

	public void curveTo(Point c1, Point c2, Point pt) {
		curveTo(c1, c2, pt);
	}

	public void quadTo(Point c, Point pt) {
		quadTo(c, pt);
	}

	public void arcTo(Point center, Point endPoint, int ccw) {
		arcTo(center, endPoint, ccw);
	}

	public void append(PathIterator iter) {
		append(iter, false);
	}

	/**
	 * Appends the segments of a Shape to the path. If <code>connect</code> is
	 * true, the new path segments are connected to the existing one with a
	 * line. The winding rule of the Shape is ignored.
	 */
	public void append(Shape shape, boolean connect) {
		append(shape.getPathIterator(null), connect);
	}

	public void append(Shape shape) {
		append(shape.getPathIterator(null), false);
	}

	public boolean intersects(PathItem item) {
		Area area = new Area(this.toShape());
		area.intersect(new Area(item.toShape()));
		return area.isEmpty();
	}

	public boolean contains(PathItem item) {
		Area area = new Area(item.toShape());
		area.subtract(new Area(this.toShape()));
		return area.isEmpty();
	}

	public boolean contains(Point point) {
		return new Area(this.toShape()).contains(point.toPoint2D());
	}

	public PathItem intersect(PathItem item) {
		Area area = new Area(this.toShape());
		area.intersect(new Area(item.toShape()));
		CompoundPath compoundPath = new CompoundPath(area);
		compoundPath.setStyle(this.getStyle());
		return compoundPath.simplify();
	}

	public PathItem unite(PathItem item) {
		Area area = new Area(this.toShape());
		area.add(new Area(item.toShape()));
		CompoundPath compoundPath = new CompoundPath(area);
		compoundPath.setStyle(this.getStyle());
		return compoundPath.simplify();
	}

	public PathItem exclude(PathItem item) {
		Area area = new Area(this.toShape());
		area.subtract(new Area(item.toShape()));
		CompoundPath compoundPath = new CompoundPath(area);
		compoundPath.setStyle(this.getStyle());
		return compoundPath.simplify();
	}
}
