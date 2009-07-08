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
 * File created on 21.06.2006.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnumUtils;
import com.scriptographer.CommitManager;
import com.scriptographer.Commitable;

/**
 * @author lehni
 */
public class Tracing extends Item implements Commitable {
		
	protected int optionsHandle = 0;
	protected int statisticsHandle = 0;

	protected Tracing(int handle) {
		super(handle);
	}

	private static native int nativeCreate(int docHandle, int artHandle);

	protected Tracing(Item item) {
		super(nativeCreate(item.document.handle, item.handle));
		markDirty(); // force a first update
	}
	
	public Tracing(Raster raster) {
		this((Item) raster);
	}
	
	public Tracing(PlacedFile raster) {
		this((Item) raster);
	}
	
	private boolean dirty = false;

	protected void markDirty() {
		if (!dirty) {
			CommitManager.markDirty(this, this);
			dirty = true;
		}
	}

	public void commit() {
		if (dirty) {
			update();
			dirty = false;
		}
	}
	
	private native void update();
	
	/**
	 * Default: {@code false}
	 */
	public native boolean getResample();
	
	/**
	 * @param resample 
	 */
	public native void setResample(boolean resample);
	
	/**
	 * Default: 72.0
	 */
	public native float getResampleResolution();
	
	/**
	 * 
	 * @param resolution 0 = dynamic, 1 <= resolution <= 600
	 */
	public native void setResampleResolution(float resolution);
	
	private native int nativeGetMode();
	private native void nativeSetMode(int mode);

	/**
	 * Default: "bitmap"
	 */
	public TracingMode getMode() {
		return IntegerEnumUtils.get(TracingMode.class, nativeGetMode());
	}

	public void setMode(TracingMode mode) {
		nativeSetMode((mode != null ? mode : TracingMode.BITMAP).value);
	}

	/**
	 * Default: 0.0
	 */
	public native float getBlur();
	
	/**
	 * 
	 * @param blur 0.0 < blur < 20.0
	 */
	public native void setBlur(float blur);
	
	/**
	 * Default: 128
	 */
	public native int getThreshold();
	
	/**
	 * 
	 * @param threshold 1 < threshold < 255
	 */
	public native void setThreshold(int threshold);
	
	/**
	 * Default: 6
	 */
	public native int getMaxColors();
	
	/**
	 * 
	 * @param maxColors 2 < maxColors < 256
	 */
	public native void setMaxColors(int maxColors);
	
	/**
	 * Default: {@code true}
	 */
	public native boolean getFills();
	
	/**
	 * 
	 * @param fills
	 */
	public native void setFills(boolean fills);
	
	/**
	 * Default: {@code false}
	 */
	public native boolean getStrokes();
	
	/**
	 * 
	 * @param strokes
	 */
	public native void setStrokes(boolean strokes);
	
	/**
	 * Default: 10.0
	 */
	public native float getMaxStrokeWeight();
	
	/**
	 * 
	 * @param maxWeight 0.01 < maxWeight < 100.0
	 */
	public native void setMaxStrokeWeight(float maxWeight);
	
	/**
	 * Default: 20.0
	 */
	public native float getMinStrokeLength();
	
	/**
	 * 
	 * @param minLength 0.0 < minLength < 200.0
	 */
	public native void setMinStrokeLength(float minLength);
	
	/**
	 * Default: 2.0
	 */
	public native float getPathTightness();
	
	/**
	 * 
	 * @param tightness 0.0 < tightness < 10.0
	 */
	public native void setPathTightness(float tightness);
	
	/**
	 * Default: 20.0
	 */
	public native float getCornerAngle();
	
	/**
	 * 
	 * @param angle 0.0 < angle < 180.0
	 */
	public native void setCornerAngle(float angle);
	
	/**
	 * Default: 10
	 */
	public native int getMinArea();
	
	/**
	 * 
	 * @param area 0 < minArea < 3000
	 */
	public native void setMinArea(int area);
	
	private native int nativeGetVectorDisplay();
	private native void nativeSetVectorDisplay(int display);

	/**
	 * Default: "artwork"
	 */
	public TracingVectorDisplay getVectorDisplay() {
		return IntegerEnumUtils.get(TracingVectorDisplay.class,
				nativeGetVectorDisplay());
	}
	
	public void setVectorDisplay(TracingVectorDisplay vectorDisplay) {
		nativeSetVectorDisplay((vectorDisplay != null ? vectorDisplay
				: TracingVectorDisplay.ARTWORK).value);
	}

	public native int nativeGetRasterDisplay();
	public native void nativeSetRasterDisplay(int display);

	/**
	 * Default: "none"
	 */
	public TracingRasterDisplay getRasterDisplay() {
		return IntegerEnumUtils.get(TracingRasterDisplay.class,
				nativeGetRasterDisplay());
	}
	
	public void setRasterDisplay(TracingRasterDisplay rasterDisplay) {
		nativeSetRasterDisplay((rasterDisplay != null ? rasterDisplay
				: TracingRasterDisplay.NONE).value);
	}
	
	/**
	 * Default: {@code false}
	 */
	public native boolean getLivePaint();
	
	/**
	 * 
	 * @param livePaint
	 */
	public native void setLivePaint(boolean livePaint);

	/* TODO:
	#define kTracingPaletteKey					"adobe/tracing/ip/palette"				// string
	#define kTracingEmbeddedPaletteKey			"adobe/tracing/ip/embedded/palette"		// dictionary (hidden, use EmbedSwatches instead)
	#define kTracingCustomColorsKey				"adobe/tracing/ip/custom/colors"		// dictionary (hidden, do not access, housekeeping of referenced custom colors)
	#define kTracingSwatchNameKey				"adobe/tracing/swatch/name"				// string
	#define kTracingSwatchColorKey				"adobe/tracing/swatch/color"			// fill style (used to store embedded custom colors (as fill styles))

	#define kTracingOutputToSwatchesKey			"adobe/tracing/output/outputtoswatches"	// bool
	*/
}
