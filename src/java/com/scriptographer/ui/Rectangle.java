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
 * File created on May 14, 2007.
 *
 * $Id$
 */

package com.scriptographer.ui;

import com.scratchdisk.script.ArgumentReader;

/**
 * @author lehni
 *
 */
public class Rectangle {
	public int x;
	public int y;
	public int width;
	public int height;

	public Rectangle() {
		x = y = width = height = 0;
	}

	public Rectangle(int x, int y, int width, int height) {
		set(x, y, width, height);
	}

	public Rectangle(Rectangle rect) {
		set(rect.x, rect.y, rect.width, rect.height);
	}

	public Rectangle(Point topLeft, Point bottomRight) {
		this(topLeft.x, topLeft.y,
				bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
	}

	public Rectangle(Point point, Size size) {
		this(point.x, point.y, size.width, size.height);
	}

	/**
	 * @jshide
	 */
	public Rectangle(ArgumentReader reader) {
		this(reader.readInteger("x", 0),
				reader.readInteger("y", 0),
				reader.readInteger("width", 0),
				reader.readInteger("height", 0));
	}

	public void set(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/*
	 * TODO: Consider renaming to getPosition
	 * But that might be weird, since it would have to be like this in ai.Rectangle too,
	 * and Item#getPosition does something else... (center).
	 * How to resolve this? Dialog#getPosition returns upper left...
	 */
	public Point getPoint() {
		return new Point(x, y);
	}

	public void setPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setPoint(Point point) {
		this.x = point.x;
		this.y = point.y;
	}

	public Size getSize() {
		return new Size(width, height);
	}

	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void setSize(Size size) {
		this.width = size.width;
		this.height = size.height;
	}

	public int getLeft() {
		return x;
	}

	public void setLeft(int left) {
		// right should not move
		width -= left - x;
		x = left;
	}

	public int getRight() {
		return x + width;
	}

	public void setRight(int right) {
		width = right - x;
	}

	public int getBottom() {
		return y + height;
	}
	
	public void setBottom(int bottom) {
		height = bottom - y;
	}

	public int getTop() {
		return y;
	}
	
	public void setTop(int top) {
		// bottom should not move
		height -= top - y;
		y = top;
	}
	
	public Point getCenter() {
		return new Point(
			x + width / 2,
			y + height / 2
		);
	}

	public void setCenter(Point center) {
		x = center.x - width / 2;
		y = center.y - height / 2;
	}

	public Point getTopLeft() {
		return new Point(getLeft(), getTop());
	}

	public void setTopLeft(Point pt) {
		setLeft(pt.x);
		setTop(pt.y);
	}

	public Point getTopRight() {
		return new Point(getRight(), getTop());
	}

	public void setTopRight(Point pt) {
		setRight(pt.x);
		setTop(pt.y);
	}

	public Point getBottomLeft() {
		return new Point(getLeft(), getBottom());
	}

	public void setBottomLeft(Point pt) {
		setLeft(pt.x);
		setBottom(pt.y);
	}

	public Point getBottomRight() {
		return new Point(getRight(), getBottom());
	}

	public void setBottomRight(Point pt) {
		setRight(x);
		setBottom(y);
	}

	/**
	 * Adds the padding to the given rectangle and returns the modified rectangle
	 * @param border
	 */
	public Rectangle add(Border border) {
		x -= border.left;
		y -= border.top;
		width += border.left + border.right;
		height += border.top + border.bottom;
		return this;
	}

	/**
	 * @param border
	 */
	public Rectangle subtract(Border border) {
		x += border.left;
		y += border.top;
		width -= border.left + border.right;
		height -= border.top + border.bottom;
		return this;
	}

	/**
	 * @jsbean Returns <code>true</code> if the rectangle is empty,
	 *         <code>false</code> otherwise.
	 */
	public boolean isEmpty() {
	    return width <= 0.0f || height <= 0.0f;
	}

	/**
	 * Tests if specified coordinates are inside the boundary of the rectangle.
	 * 
	 * @param x, y the coordinates to test
	 * @return <code>true</code> if the specified coordinates are inside the
	 *         boundary of the rectangle; <code>false</code> otherwise.
	 */
	public boolean contains(double x, double y) {
		return x >= this.x &&
			y >= this.y &&
			x < this.x + width &&
			y < this.y + height;
	}

    /**
	 * Tests if the specified point is inside the boundary of the rectangle.
	 * 
	 * @param p the specified point
	 * @return <code>true</code> if the point is inside the rectangle's
	 *         boundary; <code>false</code> otherwise.
	 */
	public boolean contains(Point p) {
		return contains(p.x, p.y);
	}

	/**
	 * Tests if the interior of this rectangle intersects the interior of
	 * another rectangle.
	 * 
	 * @param r the specified rectangle
	 * @return <code>true</code> if the rectangle and the specified rectangle
	 *         intersect each other; <code>false</code> otherwise.
	 */
	public boolean intersects(Rectangle r) {
		return !isEmpty() && r.width > 0 && r.height > 0 &&
			r.x + r.width > this.x &&
			r.y + r.height > this.y &&
			r.x < this.x + this.width &&
			r.y < this.y + this.height;
	}
	
    /**
	 * Tests if the interior of the rectangle entirely contains the specified
	 * rectangle.
	 * 
	 * @param rect The specified rectangle
	 * @return <code>true</code> if the rectangle entirely contains the
	 *         specified rectangle; <code>false</code> otherwise.
	 */
	public boolean contains(Rectangle rect) {
	return !isEmpty() && rect.width > 0 && rect.height > 0 &&
		rect.x >= this.x &&
		rect.y >= this.y &&
		rect.x + rect.width <= this.x + this.width &&
		rect.y + rect.height <= this.y + this.height;
	}

	/**
	 * Returns a new rectangle representing the intersection of this rectangle
	 * with the specified rectangle.
	 * 
	 * @param r The rectangle to be intersected with this rectangle.
	 * @return The largest rectangle contained in both the specified rectangle
	 *         and in this rectangle.
	 */
	public Rectangle intersect(Rectangle r) {
		int x1 = Math.max(x, r.x);
		int y1 = Math.max(y, r.y);
		int x2 = Math.min(x + width, r.x + r.width);
		int y2 = Math.min(y + height, r.y + r.height);
		return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

	/**
	 * Returns a new rectangle representing the union of this rectangle with the
	 * specified rectangle.
	 * 
	 * @param r the rectangle to be combined with this rectangle
	 * @return the smallest rectangle containing both the specified rectangle
	 *         and this rectangle.
	 */
	public Rectangle union(Rectangle r) {
		int x1 = Math.min(x, r.x);
		int y1 = Math.min(y, r.y);
		int x2 = Math.max(x + width, r.x + r.width);
		int y2 = Math.max(y + height, r.y + r.height);
		if (x2 < x1) {
			int t = x1;
		    x1 = x2;
		    x2 = t;
		}
		if (y2 < y1) {
			int t = y1;
		    y1 = y2;
		    y2 = t;
		}
		return new Rectangle(x1, y1, x2 - x1, y2 - y1);
	}

    /**
	 * Adds a point, specified by the double precision arguments <code>px</code>
	 * and <code>py</code>, to the rectangle. The resulting rectangle is the
	 * smallest rectangle that contains both the original rectangle and the
	 * specified point.
	 * 
	 * After adding a point, a call to <code>contains</code> with the added
	 * point as an argument does not necessarily return <code>true</code>.
	 * The <code>contains</code> method does not return <code>true</code>
	 * for points on the right or bottom edges of a rectangle. Therefore, if the
	 * added point falls on the left or bottom edge of the enlarged rectangle,
	 * <code>contains</code> returns <code>false</code> for that point.
	 * 
	 * @param px, py The coordinates of the point.
	 */
	public void include(int px, int py) {
		int nx = Math.min(x, px);
		int ny = Math.min(y, px);
		width = Math.max(x + width, px) - nx;
		height = Math.max(y + height, py) - ny;
		x = nx;
		y = ny;
	}

    /**
	 * Adds a point to this rectangle. The resulting rectangle is the
	 * smallest rectangle that contains both the original rectangle and the
	 * specified point.
	 * 
	 * After adding a point, a call to <code>contains</code> with the added
	 * point as an argument does not necessarily return <code>true</code>.
	 * The <code>contains</code> method does not return <code>true</code>
	 * for points on the right or bottom edges of a rectangle. Therefore, if the
	 * added point falls on the left or bottom edge of the enlarged rectangle,
	 * <code>contains</code> returns <code>false</code> for that point.
	 * 
	 * @param pt
	 */
	public void include(Point pt) {
		include(pt.x, pt.y);
	}

	/**
	 * Adds a rectangle to this rectangle. The resulting rectangle is the union
	 * of the two rectangles.
	 * 
	 * @param rect the rectangle to add to this rectangle.
	 */
	public void include(Rectangle rect) {
		int nx = Math.min(x, rect.x);
		int ny = Math.min(y, rect.y);
		width = Math.max(x + width, rect.x + rect.width) - nx;
		height = Math.max(y + width, rect.y + rect.width) - ny;
		x = nx;
		y = ny;
	}

	public Object clone() {
		return new Rectangle(this);
	}

	public boolean equals(Object object) {
		if (object instanceof Rectangle) {
			Rectangle rt = (Rectangle) object;
			return rt.x == x && rt.y == y &&
					rt.width == width && rt.height == height;
		} else {
			// TODO: support other rect types?
			return false;
		}
	}

	public String toString() {
	   	return "{ x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + " }";
	}
}
