/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile: Rectangle.java,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/04/07 20:12:55 $
 */

package com.scriptographer.ai;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.mozilla.javascript.Scriptable;

import com.scriptographer.js.Wrappable;

public class Rectangle extends Rectangle2D.Float implements Wrappable {

	public Rectangle() {
	}

	public Rectangle(java.awt.Rectangle rt) {
		super(rt.x, rt.y, rt.width, rt.height);
	}

	public Rectangle(Rectangle2D rt) {
		super((float)rt.getX(), (float)rt.getY(), (float)rt.getWidth(), (float)rt.getHeight());
	}
	
	public Rectangle(float x, float y, float w, float h) {
		super(x, y, w, h);
	}
	
	public Rectangle(double x, double y, double w, double h) {
		super((float)x, (float)y, (float)w, (float)h);
	}
	
	public Rectangle(Point2D bottomLeft, Point2D topRight) {
		super((float)bottomLeft.getX(), (float)bottomLeft.getY(), (float)topRight.getX() - (float)bottomLeft.getX(), (float)topRight.getY() - (float)bottomLeft.getY());
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

	public Point getCenter() {
		return new Point(x + width * 0.5f, y + height * 0.5f);
	}

	// wrappable interface:

	private Scriptable wrapper;

	public void setWrapper(Scriptable wrapper) {
		this.wrapper = wrapper;
	}

	public Scriptable getWrapper() {
		return wrapper;
	}
}
