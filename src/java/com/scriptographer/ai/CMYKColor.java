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
public class CMYKColor extends Color {
	protected float cyan;
	protected float magenta;
	protected float yellow;
	protected float black;

	/**
	 * Creates a CMYKColor with the supplied color component values.
	 * The color components have values between 0 and 1
	 * 
	 * @param c The amount of cyan
	 * @param m The amount of magenta
	 * @param y The amount of yellow
	 * @param k The amount of black
	 */
	public CMYKColor(float c, float m, float y, float k) {
		this(c, m, y, k, -1f);
	}

	/**
	 * Creates a CMYKColor with the supplied color component values.
	 * The color components have values between 0 and 1.
	 * 
	 * @param c The amount of cyan
	 * @param m The amount of magenta
	 * @param y The amount of yellow
	 * @param k The amount of black
	 * @param a The alpha value
	 */
	public CMYKColor(float c, float m, float y, float k, float a) {
		cyan = c;
		magenta = m;
		yellow = y;
		black = k;
		alpha = a;
	}

	/**
	 * Creates a CMYKColor using the values from the supplied array.
	 * The color components have values between 0 and 1.
	 * 
	 * Sample code:
	 * <code>
	 * var components = [1, 1, 0, 0.5];
	 * var color = new CMYKColor(components);
	 * print(color); // { cyan: 1.0, magenta: 1.0, yellow: 0.0, black: 0.5 }
	 * </code>
	 * 
	 * @param component
	 */
	public CMYKColor(float components[]) {
		cyan = components[0];
		magenta = components[1];
		yellow = components[2];
		black = components[3];
		alpha = (components.length > 4) ? components[4] : -1f;
	}

	public java.awt.Color toAWTColor() {
		// workaround, as there seems to be a problem with the color profiles
		// and cmyk:
		return convert(ColorType.ARGB).toAWTColor();
		// this doesn't seem to work:
		// return new java.awt.Color(getColorSpace(), new float[] { cyan,
		// yellow, magenta, black }, alpha);
	}

	/**
	 * Returns the color component values as an array.
	 * 
	 * Sample code:
	 * <code>
	 * var color = new CMYKColor(1, 1, 0, 0.5);
	 * print(color.components) // 1.0, 1.0, 0.0, 0.5, -1.0
	 * </code>
	 */
	public float[] getComponents() {
		return new float[] {
			cyan,
			magenta,
			yellow,
			black,
			alpha
		};
	}

	protected static ColorSpace space = null;

	/**
	 * @jshide
	 */
	public static ColorSpace getColorSpace() {
		if (space == null)
			space = new ICC_ColorSpace(getProfile(ColorModel.CMYK));
		return space;
	}

	/**
	 * Checks if the component color values of the CMYKColor are the
	 * same as those of the supplied one.
	 * 
	 * @param obj The CMYKColor to compare with
	 * @return <code>true</code> if the CMYKColor is the same, <code>false</code> otherwise
	 */
	public boolean equals(Object obj) {
		if (obj instanceof CMYKColor) {
			CMYKColor col = (CMYKColor) obj;
			return  cyan == col.cyan &&
					magenta == col.magenta &&
					yellow == col.yellow &&
					black == col.black &&
					alpha == col.alpha;
		}
		return false;
	}

	/**
	 * A value between 0 and 1 that specifies the amount of cyan in the CMYK color.
	 */
	public float getCyan() {
		return cyan;
	}

	public void setCyan(float cyan) {
		this.cyan = cyan;
	}

	/**
	 * A value between 0 and 1 that specifies the amount of magenta in the CMYK color.
	 */
	public float getMagenta() {
		return magenta;
	}

	public void setMagenta(float magenta) {
		this.magenta = magenta;
	}

	/**
	 * A value between 0 and 1 that specifies the amount of yellow in the CMYK color.
	 */
	public float getYellow() {
		return yellow;
	}

	public void setYellow(float yellow) {
		this.yellow = yellow;
	}

	/**
	 * A value between 0 and 1 that specifies the amount of black in the CMYK color.
	 */
	public float getBlack() {
		return black;
	}

	public void setBlack(float black) {
		this.black = black;
	}

	/**
	 * @jshide
	 */
	public void set(Color color) {
		CMYKColor other = (CMYKColor) color.convert(getType());
		cyan = other.cyan;
		magenta = other.magenta;
		yellow = other.yellow;
		black = other.black;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(32);
		buf.append("{ cyan: ").append(cyan);
		buf.append(", magenta: ").append(magenta);
		buf.append(", yellow: ").append(yellow);
		buf.append(", black: ").append(black);
		if (alpha != -1f)
			buf.append(", alpha: ").append(alpha);
		buf.append(" }");
		return buf.toString();
	}
}
