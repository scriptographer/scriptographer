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

import com.scriptographer.CommitManager;
import com.scriptographer.Commitable;

/**
 * @author lehni
 */
public class Tracing extends Art implements Commitable {
	
	// AITracingMode
	public final static int
		/** Color.  Either RGB or CMYK depending on source image.  */
		MODE_COLOR = 0,
		
		/** Gray. */
		MODE_GRAY = 1,

		/** Bitmap. */
		MODE_BITMAP = 2;
	
	// AITracingVisualizeVectorType
	public final static int
		/** No Artwork.  */
		VECTOR_NONE = 0,
	
		/** Artwork.  The full result, paths and fills, of the tracing.  */
		VECTOR_ARTWORK = 1,
	
		/** Paths.  A paths only version of the "Artwork" mode with black stroked paths.  */
		VECTOR_OUTLINES = 2,
	
		/** Paths and Transparency.  A "transparent" version of the "Artwork" mode with black stroked paths overlayed.  */
		VECTOR_ARTWORK_AND_OUTLINES = 3;
		
	// AITracingVisualizeRasterType
	public final static int
		/** None.  No raster is included in the visualization.  */
		RASTER_NONE = 0,
	
		/** Original.  The original raster input (before preprocessing).  */
		RASTER_ORIGINAL = 1,
	
		/** Preprocessed.  The preprocessed image.  */
		RASTER_ADJUSTED = 2,
	
		/** Transparency.  A "transparent" version of the "Original" mode.  */
		RASTER_TRANSPARENT = 3;
	
	protected int optionsHandle = 0;
	protected int statisticsHandle = 0;

	protected Tracing(int handle) {
		super(handle);
	}

	private native static int nativeCreate(int docHandle, int artHandle);

	protected Tracing(Art art) {
		super(nativeCreate(art.document.handle, art.handle));
		markDirty(); // force a first update
	}
	
	public Tracing(Raster raster) {
		this((Art) raster);
	}
	
	public Tracing(PlacedItem raster) {
		this((Art) raster);
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
	 * Default: false
	 * @return
	 */
	public native boolean getResample();
	
	/**
	 * @param resample 
	 */
	public native void setResample(boolean resample);
	
	/**
	 * Default: 72.0
	 * @return
	 */
	public native float getResampleResolution();
	
	/**
	 * 
	 * @param resolution 0 = dynamic, 1 <= resolution <= 600
	 */
	public native void setResampleResolution(float resolution);
	
	/**
	 * Default: {@link #MODE_BITMAP}
	 * @return
	 */
	public native int getMode();
	
	/**
	 * 
	 * @param mode MODE_*
	 */
	public native void setMode(int mode);
	
	/**
	 * Default: 0.0
	 * @return
	 */
	public native float getBlur();
	
	/**
	 * 
	 * @param blur 0.0 < blur < 20.0
	 */
	public native void setBlur(float blur);
	
	/**
	 * Default: 128
	 * @return
	 */
	public native int getThreshold();
	
	/**
	 * 
	 * @param threshold 1 < threshold < 255
	 */
	public native void setThreshold(int threshold);
	
	/**
	 * Default: 6
	 * @return
	 */
	public native int getMaxColors();
	
	/**
	 * 
	 * @param maxColors 2 < maxColors < 256
	 */
	public native void setMaxColors(int maxColors);
	
	/**
	 * Default: true
	 * @return
	 */
	public native boolean getFills();
	
	/**
	 * 
	 * @param fills
	 */
	public native void setFills(boolean fills);
	
	/**
	 * Default: false
	 * @return
	 */
	public native boolean getStrokes();
	
	/**
	 * 
	 * @param strokes
	 */
	public native void setStrokes(boolean strokes);
	
	/**
	 * Default: 10.0
	 * @return
	 */
	public native float getMaxStrokeWeight();
	
	/**
	 * 
	 * @param maxWeight 0.01 < maxWeight < 100.0
	 */
	public native void setMaxStrokeWeight(float maxWeight);
	
	/**
	 * Default: 20.0
	 * @return
	 */
	public native float getMinStrokeLength();
	
	/**
	 * 
	 * @param minLength 0.0 < minLength < 200.0
	 */
	public native void setMinStrokeLength(float minLength);
	
	/**
	 * Default: 2.0
	 * @return
	 */
	public native float getPathTightness();
	
	/**
	 * 
	 * @param tightness 0.0 < tightness < 10.0
	 */
	public native void setPathTightness(float tightness);
	
	/**
	 * Default: 20.0
	 * @return
	 */
	public native float getCornerAngle();
	
	/**
	 * 
	 * @param angle 0.0 < angle < 180.0
	 */
	public native void setCornerAngle(float angle);
	
	/**
	 * Default: 10
	 * @return
	 */
	public native int getMinArea();
	
	/**
	 * 
	 * @param area 0 < minArea < 3000
	 */
	public native void setMinArea(int area);
	
	/**
	 * Default: {@link #VECTOR_ARTWORK}
	 * @return
	 */
	public native int getVectorDisplay();
	
	/**
	 * 
	 * @param display #VECTOR_*
	 */
	public native void setVectorDisplay(int display);
	
	/**
	 * Default: {@link #RASTER_NONE}
	 * @return
	 */
	public native int getRasterDisplay();
	
	/**
	 * 
	 * @param display {@link #RASTER_*}
	 */
	public native void setRasterDisplay(int display);
	
	/**
	 * Default: false
	 * @return
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
