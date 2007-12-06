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
 * File created on May 14, 2007.
 *
 * $Id: $
 */

package com.scriptographer.adm;

import java.util.Map;

import com.scratchdisk.util.ConversionUtils;

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

	public Rectangle(Map map) {
		this(ConversionUtils.getInt(map, "x"),
				ConversionUtils.getInt(map, "y"),
				ConversionUtils.getInt(map, "width"),
				ConversionUtils.getInt(map, "height"));
	}

	public void set(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

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
}
