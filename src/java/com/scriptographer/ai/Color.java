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
 * File created on 30.12.2004.
 *
 * $RCSfile$
 * $Author$
 * $Revision$
 * $Date$
 */

package com.scriptographer.ai;

import java.awt.color.ICC_Profile;
import java.io.IOException;

import com.scriptographer.js.WrappableObject;

public abstract class Color extends WrappableObject {

	// AIRasterizeType, AIColorConversionSpaceValue
	// Used in Color.convert() and Raster
	// the conversion to the right AIColorConversionSpaceValue values is done
	// in native code. 
	public final static short
		TYPE_RGB = 0, // RGB no alpha
		TYPE_CMYK = 1, // CMYK no alpha
		TYPE_GRAY = 2, // Grayscale no alpha
		TYPE_BITMAP = 3, // opaque bitmap
		TYPE_ARGB = 4, // RGB with alpha
		TYPE_ACMYK = 5, // CMYK with alpha
		TYPE_AGRAY = 6, // Grayscale with alpha
		TYPE_ABITMAP = 8; // bitmap with transparent 0-pixels

	// AIWorkingColorSpace, AIColorModel,
	public final static short
		MODEL_GRAY = 0,
		MODEL_RGB = 1,
		MODEL_CMYK = 2;
	
	public final static Color NONE = new RGBColor(-1, -1, -1, -1);

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
	 * Returns the color's alpha value.
	 * All colors of the different subclasses support alpha values.
	 * A value of 1f is sometimes treaded as a color without alpha channel.
	 *
	 * @return the color's alpha value
	 */
	public float getAlpha() {
		// an alpha value of -1 means no alpha channel. return 1 here as no alpha means
		// 100% alpha
		return alpha == -1f ? 1f : alpha;
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

	/**
	 * Converts the color into another color space.
	 *
	 * @param type the conversion color space, Color.TYPE_*
	 * @return the converted color.
	 */
	public native Color convert(short type);

	/**
	 * returns the native profile for the above MODEL_ constants, wrapped in an ICC_Profile
	 * this is pretty nice: the native ICC profile data from Adobe Illustrator really seems to be
	 * compatible with ICC_Profile, so the whole ColorSpaces from Illustrator can be used in Java
	 * as well.
	 *
	 * @param space the profile for Illustrator's ColorSpace, Color.MODEL_*
	 * @return the ICC_Profile that wraps Illustrator's ColorSpace profile
	 */
	private static native ICC_Profile getWSProfile(short space);

	/**
	 * Call first getWSProfile in order to get the illustrator's profile, and if this doesn't work,
	 * it falls back to the scriptographer's internal profiles.
	 *
	 * @param space
	 * @return
	 */
	protected static ICC_Profile getProfile(short space) {
		// first try the illustrator internal WS profiles:
		ICC_Profile profile = getWSProfile(space);
		if (profile == null) {
			// if this didn't work, use scriptographer's internal profiles:
			String filename = null;
			switch (space) {
				case MODEL_GRAY: filename = "gray.icc"; break;
				case MODEL_RGB:	 filename = "rgb.icc";  break;
				case MODEL_CMYK: filename = "cmyk.icc"; break;
			}
			if (filename != null) {
				try {
					profile = ICC_Profile.getInstance(
						Color.class.getClassLoader().getResourceAsStream("com/scriptographer/cmm/" + filename)
					);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return profile;
	}
	
	/*
	 * Used in GradientColor, but here to reduce amount of native cpp files
	 */
	protected static native void nativeSetGradient(int pointer, int gradientHandle,
		Point origin, float angle, float length, Matrix matrix,
		float hiliteAngle, float hiliteLength);
	
	protected static native void nativeSetPattern(int pointer, int patternHandle,
		float shiftDistance, float shiftAngle, Point scaleFactor,
		float rotationAngle, boolean reflect, float reflectAngle,
		float shearAngle, float shearAxis, Matrix matrix);
}
