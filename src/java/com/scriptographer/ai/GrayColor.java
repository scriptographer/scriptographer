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
 * File created on 23.01.2005.
 *
 * $RCSfile$
 * $Author$
 * $Revision$
 * $Date$
 */

package com.scriptographer.ai;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;

public class GrayColor extends Color {
	protected float gray;

	/**
	 * The color components have values between 0 and 1
	 * @param g Gray
	 * @param a Alpha
	 */
	public GrayColor(float g, float a) {
		gray = g;
		alpha = a;
	}

	public GrayColor(float g) {
		this(g, 1f);
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

	public static ColorSpace getColorSpace() {
		if (space == null)
			space = new ICC_ColorSpace(getProfile(MODEL_GRAY));
		return space;
	}

	public boolean equals(Object obj) {
		if (obj instanceof GrayColor) {
			GrayColor col = (GrayColor) obj;
			return  gray == col.gray &&
					alpha == col.alpha;
		}
		return false;
	}

	public float getGray() {
		return gray;
	}

	public void setGray(float gray) {
		this.gray = gray;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(16);
		buf.append("{ gray: ").append(gray);
		if (alpha != -1f)
			buf.append(", alpha: ").append(alpha);
		buf.append(" }");
		return buf.toString();
	}
}
