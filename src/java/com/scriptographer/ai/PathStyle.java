/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 14.02.2005.
 *
 * $Id$
 */

package com.scriptographer.ai;

import com.scriptographer.Commitable;
import com.scriptographer.CommitManager;

/*
 * PathStyle, FillStyle and StrokeStyle are used for Art, CharacterAttributes,
 * and others In some places, not all of the values may be defined.
 * Setting any value to null means the value is not defined.
 * 
 * Setting fillColor or StrokeColor to Color.NONE means no fill / stroke
 * Setting it to null undefines the value, it doesn't have the same effect as
 * seting it to Color.NONE
 * 
 * Since Java 1.5 comes with auto boxing / unboxing, I don't think it's a big
 * deal that we're not returning native values but boxed ones here (or null,
 * in case the value isn't defined)
 * 
 * PathStyle derives AIObject so CharacterStyle has a handle.
 */

/**
 * @author lehni
 */
public class PathStyle extends NativeObject implements Style, Commitable {
	protected FillStyle fill;

	protected StrokeStyle stroke;
	
	// Whether or not to use this as a clipping path
	protected Boolean clip;
	
	// Whether or not to lock the clipping path
	protected Boolean lockClip;

	// Whether or not to use the even-odd rule to determine path insideness
	protected Boolean evenOdd;
	
	// Path's resolution
	protected Float resolution;
	
	private Art art = null;

	protected boolean dirty = false;
	protected int version = -1;
	
	// don't fetch immediatelly.
	// only fetch once values are requested
	protected boolean fetched = false;
	
	/**
	 * for CharacterStyle
	 */
	protected PathStyle(int handle) {
		super(handle);
		fill = new FillStyle(this);
		stroke = new StrokeStyle(this);
	}

	protected PathStyle(Art art) {
		this(0); // PathStyle doesn't use the handle, but CharacterStyle does
		this.art = art;
	}

	protected PathStyle(PathStyle style) {
		this(0); // PathStyle doesn't use the handle, but CharacterStyle does
		init(style);
	}

	public PathStyle(FillStyle fill, StrokeStyle stroke) {
		this.fill = new FillStyle(fill, this);
		this.stroke = new StrokeStyle(stroke, this);
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof PathStyle) {
			// TODO: Implement!
		}
		return false;
	}
	
	public Object clone() {
		return new PathStyle(this);
	}
	
	protected void update() {
		// only update if it didn't change in the meantime:
		if (!fetched || (!dirty && art != null && version != art.version))
			fetch();
	}

	/*
	 * This is complicated: for undefined values:
	 * - color needs an additional boolean value
	 * - boolean values are passed as short: -1 = undefined, 0 = false,
	 *   1 = true
	 * - float are passed as float: < 0 = undefined, >= 0 = defined
	 */
	protected void init(
			Color fillColor, boolean hasFillColor, short fillOverprint,
			Color strokeColor, boolean hasStrokeColor, short strokeOverprint,
			float strokeWidth,
			float dashOffset, float[] dashArray,
			short cap, short join, float miterLimit,
			short clip, short lockClip, short evenOdd, float resolution) {
		//  dashArray doesn't need the boolean, as it's {} when set but empty
		
		fill.init(fillColor, hasFillColor, fillOverprint);
		stroke.init(strokeColor, hasStrokeColor, strokeOverprint, strokeWidth,
			dashOffset, dashArray, cap, join, miterLimit);

		this.clip = clip >= 0 ? new Boolean(clip != 0) : null;
		this.lockClip = lockClip >= 0 ? new Boolean(lockClip != 0) : null;
		this.evenOdd = evenOdd >= 0 ? new Boolean(evenOdd != 0) : null;
		this.resolution = resolution >= 0 ? new Float(resolution) : null;
	}
	
	protected void init(PathStyle style) {
		FillStyle fillStyle = style.fill;
		StrokeStyle strokeStyle = style.stroke;
		fill.init(fillStyle.color, fillStyle.overprint);
		stroke.init(strokeStyle.color, strokeStyle.overprint, strokeStyle.width,
			strokeStyle.dashOffset, strokeStyle.dashArray, strokeStyle.cap,
			strokeStyle.join, strokeStyle.miterLimit);
		this.clip = style.clip;
		this.lockClip = style.lockClip;
		this.evenOdd = style.evenOdd;
		this.resolution = style.resolution;
	}

	protected native void nativeGet(int handle);
	
	protected native void nativeSet(int handle, int docHandle, 
			Color fillColor, boolean hasFillColor,
			short fillOverprint,
			Color strokeColor, boolean hasStrokeColor,
			short strokeOverprint, float strokeWidth,
			float dashOffset, float[] dashArray,
			short cap, short join, float miterLimit,
			short clip, short lockClip, short evenOdd, float resolution);

	// These would belong to FillStyle and StrokeStyle, but in order to safe 4
	// new native files, they're here:
	protected static native void nativeInitStrokeStyle(int handle, Color color,
		boolean hasColor, short overprint, float width, float dashOffset,
		float[] dashArray, short cap, short join, float miterLimit);

	protected static native void nativeInitFillStyle(int handle, Color color,
		boolean hasColor, short overprint);
	
	/**
	 * just a wrapper around nativeCommit, which can be used in CharacterStyle
	 * as well (CharacterStyle has an own implementation of nativeCommit, but
	 * the calling is the same...)
	 */
	protected void commit(int handle, int docHandle) {
		nativeSet(handle, docHandle,
			fill.color != null && fill.color != Color.NONE ? fill.color : null,
			fill.color != null, 
			fill.overprint != null ? (short) (fill.overprint.booleanValue() ? 1 : 0) : -1,
			stroke.color != null && stroke.color != Color.NONE ? stroke.color : null,
			stroke.color != null,
			stroke.overprint != null ? (short) (stroke.overprint.booleanValue() ? 1 : 0) : -1,
			stroke.width != null ? stroke.width.floatValue() : -1,
			stroke.dashOffset != null ? stroke.dashOffset.floatValue() : -1,
			stroke.dashArray,
			stroke.cap != null ? stroke.cap.shortValue() : -1,
			stroke.join != null ? stroke.join.shortValue() : -1,
			stroke.miterLimit != null ? stroke.miterLimit.floatValue() : -1,
			clip != null ? (short) (clip.booleanValue() ? 1 : 0) : -1,
			lockClip != null ? (short) (lockClip.booleanValue() ? 1 : 0) : -1,
			evenOdd != null ? (short) (evenOdd.booleanValue() ? 1 : 0) : -1,
			resolution != null ? resolution.floatValue() : -1
		);
	}

	protected void fetch() {
		nativeGet(art.handle);
		version = art.version;
		fetched = true;
	}

	public void commit() {
		if (dirty && art != null) {
			commit(art.handle, art.document.handle);
			version = art.version;
			dirty = false;
		}
	}

	protected void markDirty() {
		// only mark it as dirty if it's attached to a path already:
		if (!dirty && art != null) {
			CommitManager.markDirty(art, this);
			dirty = true;
		}
	}

	public FillStyle getFill() {
		return fill;
	}

	public void setFill(FillStyle fill) {
		update();
		this.fill = new FillStyle(fill, this);
		markDirty();
	}

	public StrokeStyle getStroke() {
 		return stroke;
	}

	public void setStroke(StrokeStyle stroke) {
		update();
		this.stroke = new StrokeStyle(stroke, this);
		markDirty();
	}

	public Boolean getClip() {
		update();
		return clip;
	}

	public void setClip(Boolean clip) {
		update();
		this.clip = clip;
		markDirty();
	}

	public Boolean getLockClip() {
		update();
		return lockClip;
	}

	public void setLockClip(Boolean lockClip) {
		update();
		this.lockClip = lockClip;
		markDirty();
	}
	
	public Boolean getEvenOdd() {
		update();
		return evenOdd;
	}

	public void setEvenOdd(Boolean evenOdd) {
		update();
		this.evenOdd = evenOdd;
		markDirty();
	}

	public Float getResolution() {
		update();
		return resolution;
	}

	public void setResolution(Float resolution) {
		update();
		this.resolution = resolution;
		markDirty();
	}
	
	/*
	 * For JDK 1.4
	 */
	public void setClip(boolean clip) {
		setClip(new Boolean(clip));
	}

	public void setLockClip(boolean lockClip) {
		setLockClip(new Boolean(lockClip));
	}

	public void setEvenOdd(boolean evenOdd) {
		setEvenOdd(new Boolean(evenOdd));
	}

	public void setResolution(float resolution) {
		setResolution(new Float(resolution));
	}
}
