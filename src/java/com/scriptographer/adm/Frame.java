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
 * File created on 03.01.2005.
 */

package com.scriptographer.adm;

import com.scratchdisk.util.IntegerEnumUtils;

/**
 * @author lehni
 */
public class Frame extends TextItem implements ComponentGroup {

	public Frame(Dialog dialog) {
		super(dialog,ItemType.FRAME);
		setPadding(0, 0, 0, 0);
	}

	protected void updateAWTMargin(Border margin) {
		if (padding != null) {
			// Add padding to AWT margin
			super.updateAWTMargin(margin.add(padding));
			// Remove padding from AWT bounds
			updateAWTBounds(bounds.subtract(padding));
		}
	}

	public boolean hasText() {
		return this.getText().length() > 0;
	}

	public void setText(String text) {
		// Make sure we update padding if text changes
		Border padding = getPadding();
		super.setText(text);
		setPadding(padding);
	}

	public FrameStyle getStyle() {
		return IntegerEnumUtils.get(FrameStyle.class, nativeGetStyle());
	}

	public void setStyle(FrameStyle style) {
		if (style != null)
			nativeSetStyle(style.value);
	}

	private Border padding = null;

	protected static final Border PADDING_TEXT = new Border(10, 4, 5, 4);

	public Border getPadding() {
		Border border = new Border(padding);
		if (hasText())
			border = border.subtract(PADDING_TEXT);
		return border;
	}

	public void setPadding(int top, int right, int bottom, int left) {
		padding = new Border(top, right, bottom, left);
		if (hasText())
			padding = padding.add(PADDING_TEXT);
		// Update AWT margin, but only if it's used
		if (component != null)
			updateAWTMargin(margin);
	}

	public void setPadding(Border padding) {
		setPadding(padding.top, padding.right, padding.bottom, padding.left);
	}

	public void setPadding(int margin) {
		setPadding(margin, margin, margin, margin);
	}

	public void setPadding(int ver, int hor) {
		setPadding(ver, hor, ver, hor);
	}

	public int getPaddingLeft() {
		return padding.left;
	}

	public void setPaddingLeft(int left) {
		padding.left = left;
	}

	public int getPaddingTop() {
		return padding.top;
	}

	public void setPaddingTop(int top) {
		padding.top = top;
	}

	public int getPaddingRight() {
		return padding.right;
	}

	public void setPaddingRight(int right) {
		padding.right = right;
	}

	public int getPaddingBottom() {
		return padding.bottom;
	}

	public void setPaddingBottom(int bottom) {
		padding.bottom = bottom;
	}
}
