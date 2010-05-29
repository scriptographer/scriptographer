/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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
 */

package com.scriptographer.ui;

import com.scratchdisk.script.ArgumentReader;

/**
 * @author lehni
 * 
 * @jshide
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

	public int getTop() {
		return y;
	}
	
	public void setTop(int top) {
		// bottom should not move
		height -= top - y;
		y = top;
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
	
	private int getCenterX() {
		return x + width / 2;
	}
	
	private int getCenterY() {
		return y + height / 2;
	}

	private void setCenterX(int x) {
		this.x = x - width / 2;
	}

	private void setCenterY(int y) {
		this.y = y - height / 2;
	}

	public Point getCenter() {
		return new Point(getCenterX(), getCenterY());
	}

	public void setCenter(Point center) {
		setCenterX(center.x);
		setCenterY(center.y);
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

	public Point getLeftCenter() {
		return new Point(getLeft(), getCenterY());
	}
	
	public void setLeftCenter(Point pt) {
		setLeft(pt.x);
		setCenterY(pt.y);
	}

	public Point getTopCenter() {
		return new Point(getCenterX(), getTop());
	}
	
	public void setTopCenter(Point pt) {
		setCenterX(pt.x);
		setTop(pt.y);
	}

	public Point getRightCenter() {
		return new Point(getRight(), getCenterY());
	}
	
	public void setRightCenter(Point pt) {
		setRight(pt.x);
		setCenterY(pt.y);
	}

	public Point getBottomCenter() {
		return new Point(getCenterX(), getBottom());
	}
	
	public void setBottomCenter(Point pt) {
		setCenterX(pt.x);
		setBottom(pt.y);
	}

	public boolean isEmpty() {
		return width <= 0 || height <= 0;
	}

	public boolean contains(Point point) {
		return contains(point.x, point.y);
	}

	public boolean contains(double x, double y) {
		return x >= this.x &&
			y >= this.y &&
			x < this.x + width &&
			y < this.y + height;
	}
	
 	public boolean contains(Rectangle rect) {
 		return !isEmpty() 
			&& rect.width > 0 && rect.height > 0
			&& rect.x >= x && rect.y >= y
			&& rect.x + rect.width <= x + width
			&& rect.y + rect.height <= y + height;
	}

	public boolean intersects(Rectangle rect) {
		return !isEmpty() && rect.width > 0 && rect.height > 0 &&
			rect.x + rect.width > this.x &&
			rect.y + rect.height > this.y &&
			rect.x < this.x + this.width &&
			rect.y < this.y + this.height;
	}

	public Rectangle intersect(Rectangle rect) {
		int x1 = Math.max(x, rect.x);
		int y1 = Math.max(y, rect.y);
		int x2 = Math.min(x + width, rect.x + rect.width);
		int y2 = Math.min(y + height, rect.y + rect.height);
		return new Rectangle(x1, y1, x2 - x1, y2 - y1);
	}

	public Rectangle unite(Rectangle rect) {
		int x1 = Math.min(x, rect.x);
		int y1 = Math.min(y, rect.y);
		int x2 = Math.max(x + width, rect.x + rect.width);
		int y2 = Math.max(y + height, rect.y + rect.height);
		return new Rectangle(x1, y1, x2 - x1, y2 - y1);
	}

	public Rectangle unite(int px, int py) {
		int x1 = Math.min(x, px);
		int y1 = Math.min(y, py);
		int x2 = Math.max(x + width, px);
		int y2 = Math.max(y + height, py);
		return new Rectangle(x1, y1, x2 - x1, y2 - y1);
	}

	public Rectangle unite(Point point) {
		return unite(point.x, point.y);
	}

	/**
	 * Adds the padding to the given rectangle and returns the modified rectangle
	 * @param border
	 */
	public Rectangle add(Border border) {
		return new Rectangle(
				x - border.left,
				y - border.top,
				width + border.left + border.right,
				height + border.top + border.bottom);
	}

	/**
	 * @param border
	 */
	public Rectangle subtract(Border border) {
		return new Rectangle(
				x + border.left,
				y + border.top,
				width - border.left - border.right,
				height - border.top - border.bottom);
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
