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
 * File created on May 11, 2007.
 */

package com.scriptographer.ui;

import java.awt.Insets;

import com.scratchdisk.script.ArgumentReader;

/**
 * @author lehni
 *
 */
public class Border {
	public int top;
	public int right;
	public int bottom;
	public int left;

	public Border() {
		top = right = bottom = left = 0;
	}

	public Border(int top, int right, int bottom, int left) {
		set(top, right, bottom, left);
	}
	
	public Border(int ver, int hor) {
		set(ver, hor, ver, hor);
	}

	public Border(Border margins) {
		set(margins.top, margins.right, margins.bottom, margins.left);
	}

	public Border(Insets insets) {
		set(insets.top, insets.right, insets.bottom, insets.left);
	}

	/**
	 * @jshide
	 */
	public Border(ArgumentReader reader) {
		this(reader.readInteger("top", 0),
				reader.readInteger("right", 0),
				reader.readInteger("bottom", 0),
				reader.readInteger("left", 0));
	}

	public void set(int top, int right, int bottom, int left) {
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		this.left = left;
	}

	public Border add(Border border) {
		return new Border(top + border.top,
				right + border.right,
				bottom + border.bottom,
				left + border.left);
	}

	public Border subtract(Border border) {
		return new Border(top - border.top,
				right - border.right,
				bottom - border.bottom,
				left - border.left);
	}

	public Insets toInsets() {
		return new Insets(top, left, bottom, right);
	}

	public Object clone() {
		return new Border(this);
	}

	public boolean equals(Object object) {
		if (object instanceof Border) {
			Border border = (Border) object;
			return border.top == top && border.right == right
				&& border.bottom == bottom && border.left == left;
		}
		// TODO: support other margin types?
		return false;
	}
}
