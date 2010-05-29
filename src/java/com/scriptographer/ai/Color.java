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
 * File created on 30.12.2004.
 */

package com.scriptographer.ai;

import java.awt.color.ICC_Profile;
import java.io.IOException;

import com.scriptographer.ScriptographerException;

/**
 * @author lehni
 */
public abstract class Color {

	/**
	 * @jshide
	 */
	public static final Color NONE = new RGBColor(-1, -1, -1, -1);

	protected float alpha;

	/**
	 * Converts the color to a java.awt.Color equivalent.
	 *
	 * @return the converted Color
	 * 
	 * @jshide
	 */
	public abstract java.awt.Color toAWTColor();

	public abstract boolean equals(Object obj);

	/**
	 * @jshide
	 */
	public abstract float[] getComponents();

	/**
	 * @jshide
	 */
	public abstract void set(Color color);

	/**
	 * A value between 0 and 1 that specifies the color's alpha value.
	 * All colors of the different subclasses support alpha values.
	 */
	public Float getAlpha() {
		// an alpha value of -1 means no alpha channel.
		return alpha == -1f ? null : alpha;
	}

	/**
	 * Checks if the color has an alpha value.
	 * 
	 * @return {@true if the color has an alpha value}
	 */
	public boolean hasAlpha() {
		return alpha != -1f;
	}
	
	/**
	 * Sets the color's alpha value.
	 * Setting alpha to null deactivates the alpha channel.
	 *
	 * @param alpha the color's new alpha value
	 */
	public void setAlpha(Float alpha) {
		if (alpha == null || alpha == -1) this.alpha = -1f;
		else if (alpha < 0f) this.alpha = 0f;
		else if (alpha > 1f) this.alpha = 1f;
		else this.alpha = alpha;
	}

	private native Color nativeConvert(int type);

	/**
	 * Converts the color into another color space.
	 *
	 * @param type the conversion color type
	 * @return the converted color.
	 */
	public Color convert(ColorType type) {
		return type == getType() ? this : nativeConvert(type.value);
	}

	/**
	 * Converts the color into another type.
	 * 
	 * @param type the type of the color to convert to, e.g. {@code RGBColor},
	 *        {@code CMYKColor}, {@code GrayColor}.
	 * @return the converted color.
	 * 
	 * @jshide
	 */
	public Color convert(Class type) {
		return convert(getType(type, hasAlpha()));
	}

	/**
	 * Converts the color into a color model, as returned by
	 * {@link Document#getColorModel}.
	 * 
	 * @param model the conversion color type
	 * @return the converted color.
	 * 
	 * @jshide
	 */
	public Color convert(ColorModel model) {
		return convert(getType(model, hasAlpha()));
	}

	/**
	 * @jshide
	 */
	public static ColorType getType(Class type, boolean alpha) {
		if (CMYKColor.class.isAssignableFrom(type)) {
			return alpha ? ColorType.ACMYK : ColorType.CMYK;
		} else if (RGBColor.class.isAssignableFrom(type)) { 
			return alpha ? ColorType.ARGB : ColorType.RGB;
		} else if (GrayColor.class.isAssignableFrom(type)) { 
			return alpha ? ColorType.AGRAY : ColorType.GRAY;
		}
		return null;
	}

	/**
	 * @jshide
	 */
	public static ColorType getType(ColorModel model, boolean alpha) {
		switch (model) {
		case CMYK:
			return alpha ? ColorType.ACMYK : ColorType.CMYK;
		case RGB:
			return alpha ? ColorType.ARGB : ColorType.RGB;
		case GRAY:
			return alpha ? ColorType.AGRAY : ColorType.GRAY;
		}
		return null;
	}

	/**
	 * Returns the type of the color as a string.
	 * 
	 * Sample code:
	 * <code>
	 * var color = new RGBColor(1, 0, 0);
	 * print(color.type); // 'rgb'
	 * print(color.type == 'cmyk'); // false
	 * 
	 * color = color.convert('cmyk') // convert the color to a CMYKColor
	 * print(color.type); // 'cmyk'
	 * 
	 * color.alpha = 0.5; // give the color an alpha value
	 * print(color.type); // 'acmyk'
	 * </code>
	 * 
	 * @return the color type
	 */
	public ColorType getType() {
		return getType(getClass(), hasAlpha());
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
