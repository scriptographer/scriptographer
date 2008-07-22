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
 * File created on 19.12.2004.
 *
 * $Id:Rectangle.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer.ai;

import java.awt.geom.Rectangle2D;

import com.scratchdisk.script.ArgumentReader;

/**
 * A Rectangle specifies an area that is enclosed by it's top-left point (x, y),
 * its width, and its height. It should not be confused with a rectangular path,
 * it is not an item.
 * 
 * @author lehni
 */
public class Rectangle {
	protected double x;
	protected double y;
	protected double width;
	protected double height;
	
	public Rectangle() {
		x = y = width = height = 0;
	}

	/**
	 * Creates a new rectangle.
	 * @param x	The left coordinate.
	 * @param y The top coordinate.
	 * @param width
	 * @param height
	 */
	public Rectangle(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public Rectangle(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/**
	 * Creates a new rectangle from the passed rectangle.
	 * @param rt
	 */
	public Rectangle(Rectangle rt) {
		this(rt.x, rt.y, rt.width, rt.height);
	}

	public Rectangle(Rectangle2D rt) {
		this(rt.getX(), rt.getY(), rt.getWidth(), rt.getHeight());
	}
	
	/**
	 * Creates a new rectangle from the passed points.
	 * @param bottomLeft The bottom left point.
	 * @param topRight The top right point.
	 */
	public Rectangle(Point bottomLeft, Point topRight) {
		this(bottomLeft.x, bottomLeft.y,
				topRight.x - bottomLeft.x, topRight.y - bottomLeft.y);
	}

	public Rectangle(Point point, Size size) {
		this(point.x, point.y, size.width, size.height);
	}

	/**
	 * Creates a new rectangle from the passed object literal.
	 * Sample code:
	 * <pre>
	 * var rect = new Rectangle({x:10,
	 *                           y:10,
	 *                           width:320,
	 *                           height:240});
	 * </pre>
	 * @param map <code>{x, y, width, height}</code>
	 */
	public Rectangle(ArgumentReader reader) {
		this(reader.readDouble("x", 0),
				reader.readDouble("y", 0),
				reader.readDouble("width", 0),
				reader.readDouble("height", 0));
	}

	/**
	 * Changes the boundary properties of the rectangle.
	 * @param x The left position.
	 * @param y The top position.
	 * @param width
	 * @param height
	 */
	public void set(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/**
	 * @jsbean The x position of the rectangle.
	 */
	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @jsbean The y position of the rectangle. In the AI coordinate
	 * @jsbean system, the y axis grows from bottom to top.
	 */
	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	/**
	 * @jsbean The width of the rectangle.
	 */
	public double getWidth() {
		return width;
	}
	
	public void setWidth(double width) {
		this.width = width;
	}

	/**
	 * @jsbean The height of the rectangle.
	 */
	public double getHeight() {
		return height;
	}
	
	public void setHeight(double height) {
		this.height = height;
	}

	/**
	 * @jsbean The position of the left hand side of the rectangle. Note that this
	 * @jsbean doesn't move the whole rectangle; the right hand side stays where it was.
	 */
	public double getLeft() {
		return x;
	}

	public void setLeft(double left) {
		// right should not move
		width -= left - x;
		x = left;
	}

	/**
	 * @jsbean The position of the right hand side of the rectangle. Note that this
	 * @jsbean doesn't move the whole rectangle; the left hand side stays where it was.
	 */
	public double getRight() {
		return x + width;
	}

	public void setRight(double right) {
		width = right - x;
	}

	/**
	 * @jsbean The bottom coordinate of the rectangle. In the AI coordinate
	 * @jsbean system, the y axis grows from bottom to top. Note that this doesn't move
	 * @jsbean the whole rectangle: the top won't move.
	 */
	public double getBottom() {
		return y;
	}
	
	public void setBottom(double bottom) {
		// top should not move
		height -= bottom - y;
		y = bottom;
	}

	/**
	 * @jsbean The top coordinate of the rectangle. In the AI coordinate
	 * @jsbean system, the y axis grows from bottom to top. Note that this
	 * @jsbean doesn't move the whole rectangle: the bottom won't move.
	 */
	public double getTop() {
		return y + height;
	}
	
	public void setTop(double top) {
		height = top - y;
	}
	
	/**
	 * @jsbean The center point of the rectangle.
	 */
	public Point getCenter() {
		return new Point(
			x + width * 0.5f,
			y + height * 0.5f
		);
	}

	public void setCenter(Point center) {
		x = center.x - width * 0.5f;
		y = center.y - height * 0.5f;
	}

	/**
	 * @jsbean The top left point of the rectangle.
	 */
	public Point getTopLeft() {
		return new Point(getLeft(), getTop());
	}

	public void setTopLeft(Point pt) {
		setLeft(pt.x);
		setTop(pt.y);
	}

	/**
	 * @jsbean The top right point of the rectangle.
	 */
	public Point getTopRight() {
		return new Point(getRight(), getTop());
	}

	public void setTopRight(Point pt) {
		setRight(pt.x);
		setTop(pt.y);
	}

	/**
	 * @jsbean The bottom left point of the rectangle.
	 */
	public Point getBottomLeft() {
		return new Point(getLeft(), getBottom());
	}

	public void setBottomLeft(Point pt) {
		setLeft(pt.x);
		setBottom(pt.y);
	}

	/**
	 * @jsbean The bottom right point of the rectangle.
	 */
	public Point getBottomRight() {
		return new Point(getRight(), getBottom());
	}

	public void setBottomRight(Point pt) {
		setRight(pt.x);
		setBottom(pt.y);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(128);
		buf.append("{ x: ").append(x);
		buf.append(", y: ").append(y);
		buf.append(", width: ").append(width);
		buf.append(", height: ").append(height);
		buf.append(" }");
		return buf.toString();
	}

	/**
	 * Clones the rectangle.
	 */
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

	public Rectangle add(Point pt) {
		return new Rectangle(x + pt.x, y + pt.y, width, height);
	}

	public Rectangle subtract(Point pt) {
		return new Rectangle(x - pt.x, y - pt.y, width, height);
	}

	public Rectangle multiply(double value) {
		return new Rectangle(x * value, y * value, width * value, height * value);
	}

	public Rectangle divide(double value) {
		return new Rectangle(x / value, y / value, width / value, height / value);
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
		double x1 = Math.max(x, r.x);
		double y1 = Math.max(y, r.y);
		double x2 = Math.min(x + width, r.x + r.width);
		double y2 = Math.min(y + height, r.y + r.height);
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
		double x1 = Math.min(x, r.x);
		double y1 = Math.min(y, r.y);
		double x2 = Math.max(x + width, r.x + r.width);
		double y2 = Math.max(y + height, r.y + r.height);
		if (x2 < x1) {
			double t = x1;
		    x1 = x2;
		    x2 = t;
		}
		if (y2 < y1) {
			double t = y1;
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
	public void include(double px, double py) {
		double nx = Math.min(x, px);
		double ny = Math.min(y, px);
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
		double nx = Math.min(x, rect.x);
		double ny = Math.min(y, rect.y);
		width = Math.max(x + width, rect.x + rect.width) - nx;
		height = Math.max(y + width, rect.y + rect.width) - ny;
		x = nx;
		y = ny;
	}

	/**
	 * @return
	 */
	protected Rectangle2D toRectangle2D() {
		return new Rectangle2D.Double(x, y, width, height);
	}
}
