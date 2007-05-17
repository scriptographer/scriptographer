/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id$
 */

package com.scriptographer.ai;

import java.awt.geom.Rectangle2D;
import java.util.Map;

import com.scratchdisk.script.ScriptEngine;

/**
 * A Rectangle specifies an area that is enclosed by it's top-left point (x, y),
 * its width, and its height. It should not be confused with a rectangular path,
 * it is not an art object.
 * 
 * @author lehni
 */
public class Rectangle {

	protected float x;
	protected float y;
	protected float width;
	protected float height;
	
	public Rectangle() {
		x = y = width = height = 0;
	}

	public Rectangle(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public Rectangle(double x, double y, double w, double h) {
		this((float) x, (float) y, (float) w, (float) h);
	}

	public Rectangle(Rectangle rt) {
		this(rt.x, rt.y, rt.width, rt.height);
	}

	public Rectangle(Rectangle2D rt) {
		this(rt.getX(), rt.getY(), rt.getWidth(), rt.getHeight());
	}
	
	public Rectangle(Point topLeft, Point bottomRight) {
		this(topLeft.x, bottomRight.y,
				bottomRight.x - topLeft.x, topLeft.y - bottomRight.y);
	}

	public Rectangle(Map map) {
		this(ScriptEngine.getDouble(map, "x"),
				ScriptEngine.getDouble(map, "y"),
				ScriptEngine.getDouble(map, "width"),
				ScriptEngine.getDouble(map, "height"));
	}

	public void set(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getWidth() {
		return width;
	}
	
	public void setWidth(float width) {
		this.width = width;
	}

	public float getHeight() {
		return height;
	}
	
	public void setHeight(float height) {
		this.height = height;
	}

	public float getLeft() {
		return x;
	}

	public void setLeft(float left) {
		// right should not move
		width -= left - x;
		x = left;
	}

	public float getRight() {
		return x + width;
	}

	public void setRight(float right) {
		width = right - x;
	}

	/**
	 * Returns the bottom coordinate of the rectangle, in the AI coordinate
	 * system, where the y axis has values that grow from bottom to top.
	 * 
	 * @return the bottom coordinate of the rectangle.
	 */
	public float getBottom() {
		return y;
	}
	
	public void setBottom(float bottom) {
		// top should not move
		height -= bottom - y;
		y = bottom;
	}

	/**
	 * Returns the top coordinate of the rectangle, in the AI coordinate
	 * system, where the y axis has values that grow from bottom to top.
	 * 
	 * @return the top coordinate of the rectangle.
	 */
	public float getTop() {
		return y + height;
	}
	
	public void setTop(float top) {
		height = top - y;
	}
	
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
	
	public String toString() {
		StringBuffer buf = new StringBuffer(128);
		buf.append("{ x: ").append(x);
		buf.append(", y: ").append(y);
		buf.append(", width: ").append(width);
		buf.append(", height: ").append(height);
		buf.append(" }");
		return buf.toString();
	}
	
	public Object clone() {
		return new Rectangle(this);
	}

	/**
	 * Determines whether or not this <code>Rectangle</code> is empty.
	 * 
	 * @return <code>true</code> if this <code>Rectangle</code> is empty, 
	 *         <code>false</code> otherwise.
	 */
	public boolean isEmpty() {
	    return width <= 0.0f || height <= 0.0f;
	}

	/**
	 * Tests if a specified coordinate is inside the boundary of this
	 * <code>Rectangle</code>.
	 * 
	 * @param x, y the coordinates to test
	 * @return <code>true</code> if the specified coordinates are inside the
	 *         boundary of this <code>Rectangle</code>; <code>false</code>
	 *         otherwise.
	 */
	public boolean contains(float x, float y) {
		return x >= this.x &&
			y >= this.y &&
			x < this.x + width &&
			y < this.y + height;
	}

    /**
	 * Tests if a specified <code>Point</code> is inside the boundary 
	 * of the <code>Shape</code>.
	 * @param p the specified <code>Point</code>
	 * @return <code>true</code> if the <code>Point</code> is inside the
	 * 			<code>Shape</code> object's boundary;
	 *			 <code>false</code> otherwise.
	 */
	public boolean contains(Point p) {
		return contains(p.x, p.y);
	}

	/**
	 * Tests if the interior of the<code>Shape</code> intersects the interior
	 * of a specified <code>Rectangle</code>.
	 * 
	 * @param r the specified <code>Rectangle</code>
	 * @return <code>true</code> if the <code>Shape</code> and the specified
	 *         <code>Rectangle</code> intersect each other;
	 *         <code>false</code> otherwise.
	 */
	public boolean intersects(Rectangle r) {
		return !isEmpty() && r.width > 0 && r.height > 0 &&
			r.x + r.width > this.x &&
			r.y + r.height > this.y &&
			r.x < this.x + this.width &&
			r.y < this.y + this.height;
	}
	
    /**
	 * Tests if the interior of the <code>Shape</code> entirely contains the
	 * specified <code>Rectangle</code>.
	 * 
	 * @param rect the specified <code>Rectangle</code>
	 * @return <code>true</code> if the <code>Shape</code> entirely contains
	 *         the specified <code>Rectangle</code>; <code>false</code>
	 *         otherwise.
	 */
	public boolean contains(Rectangle rect) {
	return !isEmpty() && rect.width > 0 && rect.height > 0 &&
		rect.x >= this.x &&
		rect.y >= this.y &&
		rect.x + rect.width <= this.x + this.width &&
		rect.y + rect.height <= this.y + this.height;
	}

	/**
	 * Returns a new <code>Rectangle</code> object representing the
	 * intersection of this <code>Rectangle</code> with the specified
	 * <code>Rectangle</code>.
	 * @param r the <code>Rectangle</code> to be intersected with
	 * this <code>Rectangle</code>
	 * @return the largest <code>Rectangle</code> contained in both 
	 * 		the specified <code>Rectangle</code> and in this
	 *		<code>Rectangle</code>.
	 */
	public Rectangle intersect(Rectangle r) {
		float x1 = Math.max(x, r.x);
		float y1 = Math.max(y, r.y);
		float x2 = Math.min(x + width, r.x + r.width);
		float y2 = Math.min(y + height, r.y + r.height);
		return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

	/**
	 * Returns a new <code>Rectangle</code> object representing the
	 * union of this <code>Rectangle</code> with the specified
	 * <code>Rectangle</code>.
	 * @param r the <code>Rectangle</code> to be combined with
	 * this <code>Rectangle</code>
	 * @return the smallest <code>Rectangle</code> containing both 
	 * the specified <code>Rectangle</code> and this 
	 * <code>Rectangle</code>.
	 */
	public Rectangle union(Rectangle r) {
		float x1 = Math.min(x, r.x);
		float y1 = Math.min(y, r.y);
		float x2 = Math.max(x + width, r.x + r.width);
		float y2 = Math.max(y + height, r.y + r.height);
		if (x2 < x1) {
			float t = x1;
		    x1 = x2;
		    x2 = t;
		}
		if (y2 < y1) {
		    float t = y1;
		    y1 = y2;
		    y2 = t;
		}
		return new Rectangle(x1, y1, x2 - x1, y2 - y1);
	}

    /**
	 * Adds a point, specified by the double precision arguments
	 * <code>newx</code> and <code>newy</code>, to this 
	 * <code>Rectangle</code>.  The resulting <code>Rectangle</code> 
	 * is the smallest <code>Rectangle</code> that
	 * contains both the original <code>Rectangle</code> and the
	 * specified point.
	 * <p>
	 * After adding a point, a call to <code>contains</code> with the 
	 * added point as an argument does not necessarily return 
	 * <code>true</code>. The <code>contains</code> method does not 
	 * return <code>true</code> for points on the right or bottom 
	 * edges of a rectangle. Therefore, if the added point falls on 
	 * the left or bottom edge of the enlarged rectangle, 
	 * <code>contains</code> returns <code>false</code> for that point.
	 * @param px,&nbsp;py the coordinates of the point
	 */
	public void include(float px, float py) {
		float nx = Math.min(x, px);
		float ny = Math.min(y, px);
		width = Math.max(x + width, px) - nx;
		height = Math.max(y + height, py) - ny;
		x = nx;
		y = ny;
	}

	public void include(Point pt) {
		include(pt.x, pt.y);
	}

	/**
	 * Adds a <code>Rectangle</code> object to this 
	 * <code>Rectangle</code>.  The resulting <code>Rectangle</code>
	 * is the union of the two <code>Rectangle</code> objects. 
	 * @param rect the <code>Rectangle</code> to add to this
	 * <code>Rectangle</code>.
	 */
	public void include(Rectangle rect) {
		float nx = Math.min(x, rect.x);
		float ny = Math.min(y, rect.y);
		width = Math.max(x + width, rect.x + rect.width) - nx;
		height = Math.max(y + width, rect.y + rect.width) - ny;
		x = nx;
		y = ny;
	}
}
