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
 * File created on 22.01.2005.
 *
 * $Id$
 */

package com.scriptographer.ai;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;

/**
 * @author lehni
 */
public class RGBColor extends Color {
	protected float red;
	protected float green;
	protected float blue;

	public RGBColor(float r, float g, float b) {
		this(r, g, b, -1f);
	}

	/**
	 * Creates an RGBColor with the supplied color component values.
	 * The color components have values between 0 and 1.
	 * 
	 * @param r the amount of red
	 * @param g the amount of green
	 * @param b the amount of blue
	 * @param a tha alpha value {@default 1}
	 */
	public RGBColor(float r, float g, float b, float a) {
		red = r;
		green = g;
		blue = b;
		alpha = a;
	}

	/**
	 * Creates an RGBColor using the values from the supplied array.
	 * The color components have values between 0 and 1.
	 * 
	 * Sample code:
	 * <code>
	 * var components = [1, 0, 0];
	 * var color = new RGBColor(components);
	 * print(color); // { red: 1.0, green: 0.0, blue: 0.0 }
	 * </code>
	 * 
	 * @param component
	 */
	public RGBColor(float components[]) {
		red = components[0];
		green = components[1];
		blue = components[2];
		alpha = (components.length > 3) ? components[3] : -1f;
	}

	public RGBColor(java.awt.Color col) {
		this(col.getRed() / 255.0f, col.getGreen() / 255.0f,
				col.getBlue() / 255.0f, col.getAlpha() / 255.0f);
	}

	public java.awt.Color toAWTColor() {
		return new java.awt.Color(getColorSpace(),
				new float[] { red, green, blue }, hasAlpha() ? alpha : 1f);
	}

	public float[] getComponents() {
		return new float[] {
			red,
			green,
			blue,
			alpha
		};
	}

	protected static ColorSpace space = null;

	/**
	 * @jshide
	 */
	public static ColorSpace getColorSpace() {
		if (space == null)
			space = new ICC_ColorSpace(getProfile(ColorModel.RGB));
		return space;
	}

	/**
	 * Checks if the component color values of the RGBColor are the
	 * same as those of the supplied one.
	 * 
	 * @param obj the RGBColor to compare with
	 * @return {@true if the RGBColor is the same}
	 */
	public boolean equals(Object obj) {
		if (obj instanceof RGBColor) {
			RGBColor col = (RGBColor) obj;
			return  red == col.red &&
					green == col.green &&
					blue == col.blue &&
					alpha == col.alpha;
		}
		return false;
	}

	/**
	 * A value between 0 and 1 that specifies the amount of red in the RGB color.
	 */
	public float getRed() {
		return red;
	}

	public void setRed(float red) {
		this.red = red;
	}

	/**
	 * A value between 0 and 1 that specifies the amount of green in the RGB color.
	 */
	public float getGreen() {
		return green;
	}

	public void setGreen(float green) {
		this.green = green;
	}

	/**
	 * A value between 0 and 1 that specifies the amount of blue in the RGB color.
	 */
	public float getBlue() {
		return blue;
	}

	public void setBlue(float blue) {
		this.blue = blue;
	}

	public void set(Color color) {
		RGBColor other = (RGBColor) color.convert(getType());
		red = other.red;
		green = other.green;
		blue = other.blue;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(32);
		buf.append("{ red: ").append(red);
		buf.append(", green: ").append(green);
		buf.append(", blue: ").append(blue);
		if (alpha != -1f)
			buf.append(", alpha: ").append(alpha);
		buf.append(" }");
		return buf.toString();
	}
}
