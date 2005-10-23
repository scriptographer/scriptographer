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
 * File created on 22.01.2005.
 * 
 * $RCSfile: CMYKColor.java,v $
 * $Author: lehni $
 * $Revision: 1.4 $
 * $Date: 2005/10/23 00:33:04 $
 */

package com.scriptographer.ai;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;

public class CMYKColor extends Color {
	protected float cyan;
	protected float magenta;
	protected float yellow;
	protected float black;

	public CMYKColor(float c, float m, float y, float k) {
		this(c, m, y, k, -1f);
	}

	public CMYKColor(float c, float m, float y, float k, float a) {
		cyan = c;
		magenta = m;
		yellow = y;
		black = k;
		alpha = a;
	}

	public java.awt.Color toAWTColor() {
		// workaround, as there seems to be a problem with the color profiles and cmyk:
		return convert(Color.TYPE_ARGB).toAWTColor();
		// this doesn't seem to work:
		// return new java.awt.Color(getColorSpace(), new float[] { cyan, yellow, magenta, black }, alpha);
	}

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

	public static ColorSpace getColorSpace() {
		if (space == null)
			space = new ICC_ColorSpace(getProfile(MODEL_CMYK));
		return space;
	}

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

	public float getCyan() {
		return cyan;
	}

	public void setCyan(float cyan) {
		this.cyan = cyan;
	}

	public float getMagenta() {
		return magenta;
	}

	public void setMagenta(float magenta) {
		this.magenta = magenta;
	}

	public float getYellow() {
		return yellow;
	}

	public void setYellow(float yellow) {
		this.yellow = yellow;
	}

	public float getBlack() {
		return black;
	}

	public void setBlack(float black) {
		this.black = black;
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
