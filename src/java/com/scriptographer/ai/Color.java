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
 * File created on 30.12.2004.
 *
 * $Id$
 */

package com.scriptographer.ai;

import java.awt.color.ICC_Profile;
import java.io.IOException;

import com.scriptographer.ScriptographerException;

/**
 * @author lehni
 */
public abstract class Color {

	public static final Color NONE = new RGBColor(-1, -1, -1, -1);

	protected float alpha;

	/**
	 * Converts the color to a java.awt.Color equivalent.
	 *
	 * @return the converted Color
	 */
	public abstract java.awt.Color toAWTColor();

	public abstract boolean equals(Object obj);

	public abstract float[] getComponents();

	/**
	 * @jsbean A value between 0 and 1 that specifies the color's alpha value.
	 * @jsbean All colors of the different subclasses support alpha values.
	 * @jsbean A value of 1f is sometimes treated as a color without alpha channel.
	 */
	public float getAlpha() {
		// an alpha value of -1 means no alpha channel. return 1 here as no
		// alpha means 100% alpha
		return alpha == -1f ? 1f : alpha;
	}

	public boolean hasAlpha() {
		return alpha != -1f;
	}
	
	/**
	 * Sets the color's alpha value.
	 * Setting alpha to -1 deactivates the alpha channel.
	 *
	 * @param alpha the color's new alpha value
	 */
	public void setAlpha(float alpha) {
		if (alpha < 0f && alpha != -1f) this.alpha = 0f;
		else if (alpha > 1f) this.alpha = 1f;
		else this.alpha = alpha;
	}

	public native Color nativeConvert(int type);

	/**
	 * Converts the color into another color space.
	 *
	 * @param type the conversion color type
	 * @return the converted color.
	 */
	public Color convert(ColorType type) {
		return nativeConvert(type.value);
	}

	/**
	 * Returns the native profile for the given space, wrapped in an
	 * ICC_Profile this is pretty nice: the native ICC profile data from Adobe
	 * Illustrator really seems to be compatible with ICC_Profile, so the whole
	 * ColorSpaces from Illustrator can be used in Java as well.
	 */
	private static native ICC_Profile nativeGetProfile(int space);

	/**
	 * Call first nativeGetProfile in order to get the illustrator's profile,
	 * and if this doesn't work, it falls back to the scriptographer's internal
	 * profiles.
	 * 
	 * @param model
	 * @return
	 */
	protected static ICC_Profile getProfile(ColorModel model) {
		// first try the illustrator internal WS profiles:
		ICC_Profile profile = nativeGetProfile(model.value);
		if (profile == null) {
			// if this didn't work, use scriptographer's internal profiles:
			try {
				profile = ICC_Profile.getInstance(
						Color.class.getClassLoader().getResourceAsStream(
								"com/scriptographer/cmm/" + model.name().toLowerCase() + ".icc"));
			} catch (IOException e) {
				throw new ScriptographerException(e);
			}
		}
		return profile;
	}
	
	/*
	 * Used in GradientColor, but here to reduce amount of native cpp files
	 */
	protected static native void nativeSetGradient(int pointer, int handle,
			Point origin, double angle, double length, Matrix matrix,
			double hiliteAngle, double hiliteLength);
	
	protected static native void nativeSetPattern(int pointer, int handle, Matrix matrix);
}
