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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A Rectangle specifies an area that is enclosed by it's top-left point (x, y),
 * its width, and its height. It should not be confused with a rectangular path,
 * it is not an art object.
 * 
 * @author lehni
 */
public class Rectangle extends Rectangle2D.Float {

	public Rectangle() {
	}

	public Rectangle(java.awt.Rectangle rt) {
		super(rt.x, rt.y, rt.width, rt.height);
	}

	public Rectangle(Rectangle2D rt) {
		super((float)rt.getX(), (float)rt.getY(), (float)rt.getWidth(),
				(float)rt.getHeight());
	}
	
	public Rectangle(float x, float y, float w, float h) {
		super(x, y, w, h);
	}
	
	public Rectangle(double x, double y, double w, double h) {
		super((float)x, (float)y, (float)w, (float)h);
	}
	
	public Rectangle(Point2D bottomLeft, Point2D topRight) {
		super((float)bottomLeft.getX(), (float)bottomLeft.getY(),
				(float)topRight.getX() - (float)bottomLeft.getX(),
				(float)topRight.getY() - (float)bottomLeft.getY());
	}
	
	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}
	
	public void setWidth(float width) {
		this.width = width;
	}
	
	public void setHeight(float height) {
		this.height = height;
	}
	
	public Point getCenter() {
		return new Point(
			x + width * 0.5f,
			y + height * 0.5f
		);
	}
	
	public void setCenter(float x, float y) {
		this.x = x - width * 0.5f;
		this.y = y - height * 0.5f;
	}
	
	public void setCenter(Point center) {
		setCenter((float) center.getX(), (float) center.getY());
	}
	
	public Point getTopLeft() {
		return new Point(x, y);
	}
	
	public void setTopLeft(Point pt) {
		this.x = pt.x;
		this.y = pt.y;
	}
	
	public Point getTopRight() {
		return new Point(x + width, y);
	}
	
	public void setTopRight(Point pt) {
		this.x = pt.x - width;
		this.y = pt.y;
	}
	
	public Point getBottomLeft() {
		return new Point(x, y + height);
	}
	
	public void setBottomLeft(Point pt) {
		this.x = pt.x;
		this.y = pt.y - height;
	}
	
	public Point getBottomRight() {
		return new Point(x + width, y + height);
	}
	
	public void setBottomRight(Point pt) {
		this.x = pt.x - width;
		this.y = pt.y - height;
	}
	
	public float getLeft() {
		return x;
	}
	
	public void setLeft(float left) {
		x = left;
	}
	
	public float getRight() {
		return x + width;
	}
	
	public void setRight(float right) {
		width = right - x;
	}
	
	public float getBottom() {
		return y;
	}
	
	public void setBottom(float bottom) {
		y = bottom;
	}
	
	public float getTop() {
		return y + height;
	}
	
	public void setTop(float top) {
		height = top - y;
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
}
