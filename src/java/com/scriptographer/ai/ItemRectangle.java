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
 * File created on Feb 27, 2008.
 *
 * $Id$
 */

package com.scriptographer.ai;

/**
 * A Rectangle class to wrap an item and control its bounds.
 * 
 * @author lehni
 */
public class ItemRectangle extends Rectangle {
	protected Item item;
	protected int version = -1;

	protected ItemRectangle(Item item) {
		this.item = item;
		update();
	}

	protected void update() {
		if (version != item.version) {
			Rectangle bounds = item.nativeGetBounds();
			x = bounds.x;
			y = bounds.y;
			width = bounds.width;
			height = bounds.height;
			version = item.version;
		}
	}

	public void set(float x, float y, float width, float height) {
		// This updates the rectangle object itself too:
		item.setBounds(x, y, width, height);
	}

	public float getX() {
		update();
		return x;
	}

	public void setX(float x) {
		update();
		set(x, y, width, height);
	}

	public float getY() {
		update();
		return y;
	}

	public void setY(float y) {
		update();
		set(x, y, width, height);
	}

	public float getWidth() {
		update();
		return width;
	}
	
	public void setWidth(float width) {
		update();
		set(x, y, width, height);
	}

	public float getHeight() {
		update();
		return height;
	}

	public void setHeight(float height) {
		update();
		set(x, y, width, height);
	}

	public float getLeft() {
		update();
		return x;
	}

	public void setLeft(float left) {
		update();
		set(left, y, width - left + x, height);
	}

	public float getRight() {
		update();
		return x + width;
	}

	public void setRight(float right) {
		update();
		set(x, y, right - x, height);
	}

	public float getBottom() {
		update();
		return y;
	}
	
	public void setBottom(float bottom) {
		update();
		// top should not move
		set(x, bottom, width, height - bottom + y);
	}

	public float getTop() {
		update();
		return y + height;
	}
	
	public void setTop(float top) {
		update();
		set(x, y, width, top - y);
	}
	
	public Point getCenter() {
		update();
		return new Point(
			x + width * 0.5f,
			y + height * 0.5f
		);
	}

	public void setCenter(Point center) {
		update();
		set(center.x - width * 0.5f, center.y - height * 0.5f, width, height);
	}

	public void setTopLeft(Point pt) {
		update();
		set(pt.x, y, width - pt.x + x, pt.y - y);
	}

	public void setTopRight(Point pt) {
		update();
		set(x, y, pt.x - x, pt.y - y);
	}

	public void setBottomLeft(Point pt) {
		update();
		// top should not move
		set(pt.x, pt.y, width - pt.x + x, height - pt.y + y);
	}

	public void setBottomRight(Point pt) {
		update();
		// top should not move
		set(x, pt.y, pt.x - x, height - pt.y + y);
	}
}