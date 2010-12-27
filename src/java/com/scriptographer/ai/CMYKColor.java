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
 * File created on 22.01.2005.
 */

package com.scriptographer.ai;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.text.NumberFormat;

import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 */
public class CMYKColor extends Color {
	protected float cyan;
	protected float magenta;
	protected float yellow;
	protected float black;

	public CMYKColor(float c, float m, float y, float k) {
		this(c, m, y, k, -1f);
	}

	/**
	 * Creates a CMYKColor with the supplied color component values.
	 * The color components have values between 0 and 1.
	 * 
	 * @param c the amount of cyan
	 * @param m the amount of magenta
	 * @param y the amount of yellow
	 * @param k the amount of black
	 * @param a the alpha value {@default 1}
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
		// Workaround, as there seems to be a problem with the color profiles
		// and cmyk:
		Color color = convert(ColorType.RGB);
		if (color != null)
			return color.toAWTColor();
		return null;
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
	 * @param obj the CMYKColor to compare with
	 * @return {@true if the CMYKColor is the same}
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof CMYKColor) {
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
		NumberFormat format = ScriptographerEngine.numberFormat;
		StringBuffer buf = new StringBuffer(32);
		buf.append("{ cyan: ").append(format.format(cyan));
		buf.append(", magenta: ").append(format.format(magenta));
		buf.append(", yellow: ").append(format.format(yellow));
		buf.append(", black: ").append(format.format(black));
		if (alpha != -1f)
			buf.append(", alpha: ").append(format.format(alpha));
		buf.append(" }");
		return buf.toString();
	}
}
