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
 * File created on 30.12.2004.
 *
 * $RCSfile: Color.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:01:00 $
 */

package com.scriptographer.ai;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.IOException;

public abstract class Color {
	protected float alpha;

	/**
	 * Converts the color to a java.awt.Color equivalent.
	 *
	 * @return the converted Color
	 */
	public abstract java.awt.Color toAWTColor();

	public abstract boolean equals(Object obj);

	public abstract ColorSpace getColorSpace();

	public abstract float[] getComponents();

	/**
	 * Returns the color's alpha value.
	 * All colors of the different subclasses support alpha values.
	 * A value of 1f is sometimes treaded as a color without alpha channel.
	 *
	 * @return the color's alpha value
	 */
	public float getAlpha() {
		return alpha;
	}

	/**
	 * Sets the color's alpha value.
	 *
	 * @param alpha the color's new alpha value
	 */
	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	// AIColorConversionSpaceValue, used in convert();
	public final static int
		CONVERSION_MONO = 0,
		CONVERSION_GRAY = 1,
		CONVERSION_RGB = 2,
		CONVERSION_ARGB = 3,
		CONVERSION_CMYK	= 4,
		CONVERSION_ACMYK = 5,
		CONVERSION_AGRAY = 6;

	/**
	 * Converts the color into another color space.
	 *
	 * @param conversion the conversion color space, Color.CONVERSION_*
	 * @return the converted color.
	 */
	public native Color convert(int conversion);

	// AIWorkingColorSpace, AIColorModel,
	public final static int
		MODEL_GRAY = 0,
		MODEL_RGB = 1,
		MODEL_CMYK = 2;

	/**
	 * returns the native profile for the above MODEL_ constants, wrapped in an ICC_Profile
	 * this is pretty nice: the native ICC profile data from Adobe Illustrator really seems to be
	 * compatible with ICC_Profile, so the whole ColorSpaces from Illustrator can be used in Java
	 * as well.
	 *
	 * @param space the profile for Illustrator's ColorSpace, Color.MODEL_*
	 * @return the ICC_Profile that wraps Illustrator's ColorSpace profile
	 */
	private native ICC_Profile getWSProfile(int space);

	/**
	 * Call first getWSProfile in order to get the illustrator's profile, and if this doesn't work,
	 * it falls back to the scriptographer's internal profiles.
	 *
	 * @param space
	 * @return
	 */
	protected ICC_Profile getProfile(int space) {
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
						getClass().getClassLoader().getResourceAsStream("com/scriptographer/cmm/" + filename)
					);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return profile;
	}
}
