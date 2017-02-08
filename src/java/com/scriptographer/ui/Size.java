/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 * 
 * File created on May 16, 2007.
 */

package com.scriptographer.ui;

import java.awt.Dimension;

import com.scratchdisk.script.ArgumentReader;

/**
 * @author lehni
 * 
 * @jshide
 */
public class Size {
	public int width;
	public int height;

	public Size() {
		width = height = 0;
	}

	public Size(int width, int height) {
		set(width, height);
	}

	public Size(double width, double height) {
		set(width, height);
	}

	public Size(Size size) {
		set(size.width, size.height);
	}

	public Size(Point point) {
		width = point.x;
		height = point.y;
	}

	/**
	 * @jshide
	 */
	public Size(Dimension size) {
		this.width = size.width;
		this.height = size.height;
	}

	/**
	 * @jshide
	 */
	public Size(ArgumentReader reader) {
		this(reader.readInteger("width", 0),
				reader.readInteger("height", 0));
	}

	/**
	 * @jshide
	 */
	public void set(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * @jshide
	 */
	public void set(double width, double height) {
		this.width = (int) Math.round(width);
		this.height = (int) Math.round(height);
	}

	public int getWidth() {
		return width;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}

	public void setWidth(double width) {
		this.width = (int) Math.round(width);
	}

	public int getHeight() {
		return height;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}

	public void setHeight(double height) {
		this.height = (int) Math.round(height);
	}

	public Size add(double w, double h) {
		return new Size(width + w, height + h);
	}

	public Size add(Size size) {
		return add(size.width, size.height);
	}

	public Size add(Border border) {
		return add(border.left + border.right, border.top + border.bottom);
	}

	public Size add(double value) {
		return add(value, value);
	}

	public Size subtract(double w, double h) {
		return new Size(width - w, height - h);
	}

	public Size subtract(Size size) {
		return subtract(size.width, size.height);
	}

	public Size subtract(Border border) {
		return subtract(border.left + border.right, border.top + border.bottom);
	}

	public Size subtract(double value) {
		return subtract(value, value);
	}

	public Size multiply(double w, double h) {
		return new Size(width * w, height * h);
	}

	public Size multiply(Size size) {
		return multiply(size.width, size.height);
	}

	public Size multiply(double value) {
		return multiply(value, value);
	}

	public Size divide(double w, double h) {
		return new Size(width / w, height / h);
	}

	public Size divide(Size size) {
		return divide(size.width, size.height);
	}

	public Size divide(double value) {
		return divide(value, value);
	}

	public Size modulo(int w, int h) {
		return new Size(this.width % w, this.height % h);
	}

	public Size modulo(Point point) {
		return modulo(point.x, point.y);
	}

	public Size modulo(int value) {
		return modulo(value, value);
	}

	public Size negate() {
		return new Size(-width, -height);
	}

	public Object clone() {
		return new Size(this);
	}

	public boolean equals(Object object) {
		if (object instanceof Size) {
			Size size = (Size) object;
			return size.width == width && size.height == height;
		}
		// TODO: support other point types?
		return false;
	}

	public String toString() {
	   	return "{ width: " + width + ", height: " + height + " }";
	}
}
