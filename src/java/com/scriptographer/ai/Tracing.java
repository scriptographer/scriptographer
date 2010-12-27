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
 * File created on 21.06.2006.
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnumUtils;
import com.scriptographer.CommitManager;
import com.scriptographer.Committable;

/**
 * @author lehni
 */
public class Tracing extends Item implements Committable {
		
	protected int optionsHandle = 0;
	protected int statisticsHandle = 0;

	protected Tracing(int handle, int docHandle, boolean created) {
		super(handle, docHandle, created);
	}

	private static native int nativeCreate(int docHandle, int artHandle);

	protected Tracing(Item item) {
		super(nativeCreate(item.document.handle, item.handle), item.document, true, false);
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

	public void commit(boolean endExecution) {
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
