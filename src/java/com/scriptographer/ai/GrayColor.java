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
 * File created on 23.01.2005.
 */

package com.scriptographer.ai;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.text.NumberFormat;

import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 */
public class GrayColor extends Color {
	protected float gray;

	/**
	 * Creates a GrayColor with the supplied color component values.
	 * The color components have values between 0 and 1.
	 * 
	 * @param g The amount of gray
	 * @param a The alpha value {@default 1}
	 */
	public GrayColor(float g, float a) {
		gray = g;
		alpha = a;
	}

	public GrayColor(float g) {
		this(g, 1f);
	}

	/**
	 * Creates a GrayColor using the color component values from the supplied array.
	 * The color components have values between 0 and 1.
	 * 
	 * Sample code:
	 * var components = [1];
	 * var color = new GrayColor(components);
	 * print(color); // { gray: 1.0 }
	 * 
	 * @param components
	 */
	public GrayColor(float components[]) {
		gray = components[0];
		alpha = (components.length > 1) ? components[1] : -1f;
	}

	public java.awt.Color toAWTColor() {
		return new java.awt.Color(getColorSpace(), new float[] { gray }, alpha);
	}

	public float[] getComponents() {
		return new float[] {
			gray,
			alpha
		};
	}

	protected static ColorSpace space = null;

	/**
	 * @jshide
	 */
	public static ColorSpace getColorSpace() {
		if (space == null)
			space = new ICC_ColorSpace(getProfile(ColorModel.GRAY));
		return space;
	}

	/**
	 * Checks if the component color values of the GrayColor are the
	 * same as those of the supplied one.
	 * 
	 * @param obj the GrayColor to compare with
	 * @return {@true if the GrayColor is the same}
	 */
	public boolean equals(Object obj) {
		if (obj instanceof GrayColor) {
			GrayColor col = (GrayColor) obj;
			return  gray == col.gray &&
					alpha == col.alpha;
		}
		return false;
	}

	/**
	 * A value between 0 and 1 that specifies the amount of gray in the gray color.
	 */
	public float getGray() {
		return gray;
	}

	public void setGray(float gray) {
		this.gray = gray;
	}

	public void set(Color color) {
		GrayColor other = (GrayColor) color.convert(getType());
		gray = other.gray;
	}

	public String toString() {
		NumberFormat format = ScriptographerEngine.numberFormat;
		StringBuffer buf = new StringBuffer(16);
		buf.append("{ gray: ").append(format.format(gray));
		if (alpha != -1f)
			buf.append(", alpha: ").append(format.format(alpha));
		buf.append(" }");
		return buf.toString();
	}
}
