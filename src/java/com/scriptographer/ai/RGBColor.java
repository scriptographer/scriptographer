/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile$
 * $Author$
 * $Revision$
 * $Date$
 */

package com.scriptographer.ai;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;

public class RGBColor extends Color {
	protected float red;
	protected float green;
	protected float blue;

	public RGBColor(float r, float g, float b) {
		this(r, g, b, -1f);
	}

	public RGBColor(float r, float g, float b, float a) {
		red = r;
		green = g;
		blue = b;
		alpha = a;
	}

	public RGBColor(java.awt.Color col) {
		this(col.getRed() / 255.0f, col.getGreen() / 255.0f, col.getBlue() / 255.0f, col.getAlpha() / 255.0f);
	}

	public java.awt.Color toAWTColor() {
		return new java.awt.Color(getColorSpace(), new float[] { red, green, blue }, alpha);
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

	public static ColorSpace getColorSpace() {
		if (space == null)
			space = new ICC_ColorSpace(getProfile(MODEL_RGB));
		return space;
	}

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

	public float getRed() {
		return red;
	}

	public void setRed(float red) {
		this.red = red;
	}

	public float getGreen() {
		return green;
	}

	public void setGreen(float green) {
		this.green = green;
	}

	public float getBlue() {
		return blue;
	}

	public void setBlue(float blue) {
		this.blue = blue;
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
