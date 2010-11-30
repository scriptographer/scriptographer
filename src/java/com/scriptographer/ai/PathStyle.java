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
 * File created on 14.02.2005.
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnumUtils;
import com.scriptographer.CommitManager;
import com.scriptographer.Committable;

/*
 * PathStyle, FillStyle and StrokeStyle are used for Item, CharacterAttributes,
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
 * PathStyle is used for changing the visual styles of items contained within an
 * Illustrator document and is returned by {@link Item#getStyle()} and
 * {@link Document#getCurrentStyle}.
 * 
 * All properties of PathStyle are also reflected directly in {@link Item},
 * i.e.: {@link Item#getFillColor()}.
 * 
 * To set multiple style properties in one go, you can pass an object to
 * {@link Item#getStyle()}. This is a convenient way to define a style once and
 * apply it to a series of items:
 * 
 * <code>
 * var circleStyle = {
 * 	fillColor: new RGBColor(1, 0, 0),
 * 	strokeColor: new GrayColor(1),
 * 	strokeWidth: 5
 * };
 * 
 * var path = new Path.Circle(new Point(50, 50), 50);
 * path.style = circleStyle;
 * </code>
 * 
 * @author lehni
 */
public class PathStyle extends NativeObject implements Style, Committable {
	protected FillStyle fill;

	protected StrokeStyle stroke;
	
	/**
	 *  Whether or not to use this as a clipping path.
	 *  @deprecated in Illustrator, but we still need to keep it around to
	 *  reflect the state
	 */
	protected Boolean clip;
	
	/**
	 *  Whether or not to lock the clipping path.
	 *  @deprecated in Illustrator, but we still need to keep it around to
	 *  reflect the state
	 */
	protected Boolean lockClip;

	// Whether or not to use the even-odd rule to determine path insideness
	protected WindingRule windingRule;
	
	// Path's resolution
	protected Float resolution;
	
	private Item item = null;

	protected boolean dirty = false;
	protected int version = -1;
	
	// Don't fetch immediately. Only fetch once values are requested
	protected boolean fetched = false;
	
	/*
	 * for CharacterStyle
	 */
	protected PathStyle(int handle) {
		super(handle);
		fill = new FillStyle(this);
		stroke = new StrokeStyle(this);
	}

	/*
	 * For Item#getStyle
	 */
	protected PathStyle(Item item) {
		this(0); // PathStyle doesn't use the handle, but CharacterStyle does
		this.item = item;
	}

	protected PathStyle(PathStyle style) {
		this(0); // PathStyle doesn't use the handle, but CharacterStyle does
		init(style);
	}

	public PathStyle() {
		super();
		this.fill = new FillStyle(this);
		this.stroke = new StrokeStyle(this);
	}

	/**
	 * @jshide
	 */
	public PathStyle(FillStyle fill, StrokeStyle stroke) {
		super();
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
		// Only update if it didn't change in the meantime:
		if (item != null && (!fetched || (!dirty && item.needsUpdate(version))))
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
			short strokeCap, short strokeJoin, float miterLimit,
			float dashOffset, float[] dashArray,
			short clip, short lockClip, int windingRule, float resolution) {
		//  dashArray doesn't need the boolean, as it's {} when set but empty
		
		fill.init(fillColor, hasFillColor, fillOverprint);
		stroke.init(strokeColor, hasStrokeColor, strokeOverprint, strokeWidth,
				strokeCap, strokeJoin, miterLimit,
				dashOffset, dashArray);

		this.clip = clip >= 0 ? new Boolean(clip != 0) : null;
		this.lockClip = lockClip >= 0 ? new Boolean(lockClip != 0) : null;
		this.windingRule = IntegerEnumUtils.get(WindingRule.class, windingRule);
		this.resolution = resolution >= 0 ? new Float(resolution) : null;
	}
	
	protected void init(PathStyle style) {
		FillStyle fillStyle = style.fill;
		StrokeStyle strokeStyle = style.stroke;
		fill.init(fillStyle.color, fillStyle.overprint);
		stroke.init(strokeStyle.color, strokeStyle.overprint, strokeStyle.width,
				strokeStyle.cap, strokeStyle.join, strokeStyle.miterLimit,
				strokeStyle.dashOffset, strokeStyle.dashArray);
		this.clip = style.clip;
		this.lockClip = style.lockClip;
		this.windingRule = style.windingRule;
		this.resolution = style.resolution;
	}

	protected native void nativeGet(int handle, int docHandle);
	
	protected native void nativeSet(int handle, int docHandle, 
			Color fillColor, boolean hasFillColor,
			short fillOverprint,
			Color strokeColor, boolean hasStrokeColor,
			short strokeOverprint, float strokeWidth,
			int strokeCap, int strokeJoin, float miterLimit,
			float dashOffset, float[] dashArray,
			short clip, short lockClip, int windingRule, float resolution);

	// These would belong to FillStyle and StrokeStyle, but in order to safe 4
	// new native files, they're here:
	protected static native void nativeInitStrokeStyle(int handle, Color color,
		boolean hasColor, short overprint, float width,
		int strokeCap, int strokeJoin, float miterLimit,
		float dashOffset, float[] dashArray);

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
			fill.overprint != null 
					? (short) (fill.overprint.booleanValue() ? 1 : 0) : -1,
			stroke.color != null && stroke.color != Color.NONE 
					? stroke.color : null,
			stroke.color != null,
			stroke.overprint != null 
					? (short) (stroke.overprint.booleanValue() ? 1 : 0) : -1,
			stroke.width != null ? stroke.width.floatValue() : -1,
			stroke.cap != null ? stroke.cap.value : -1,
			stroke.join != null ? stroke.join.value : -1,
			stroke.miterLimit != null ? stroke.miterLimit.floatValue() : -1,
			stroke.dashOffset != null ? stroke.dashOffset.floatValue() : -1,
			stroke.dashArray,
			clip != null ? (short) (clip.booleanValue() ? 1 : 0) : -1,
			lockClip != null ? (short) (lockClip.booleanValue() ? 1 : 0) : -1,
			windingRule != null ? windingRule.value() : -1,
			resolution != null ? resolution.floatValue() : -1
		);
	}

	protected void fetch() {
		nativeGet(item.handle, item.document.handle);
		version = item.version;
		fetched = true;
	}

	public void commit(boolean endExecution) {
		if (dirty && item != null && item.isValid()) {
			commit(item.handle, item.document.handle);
			version = item.version;
			item.setModified();
			dirty = false;
		}
	}

	protected void markDirty() {
		// Only mark it as dirty if it's attached to a path already:
		if (!dirty && item != null) {
			CommitManager.markDirty(item, this);
			dirty = true;
		}
	}

	/**
	 * @jshide
	 */
	public FillStyle getFill() {
		return fill;
	}

	/**
	 * @jshide
	 */
	public void setFill(FillStyle fill) {
		update();
		this.fill = new FillStyle(fill, this);
		markDirty();
	}

	protected FillStyle getFill(boolean create) {
		if (fill == null && create)
			fill = new FillStyle(this);
 		return fill;
	}

	/**
	 * @jshide
	 */
	public StrokeStyle getStroke() {
 		return stroke;
	}

	protected StrokeStyle getStroke(boolean create) {
		if (stroke == null && create)
			stroke = new StrokeStyle(this);
 		return stroke;
	}

	/**
	 * @jshide
	 */
	public void setStroke(StrokeStyle stroke) {
		update();
		this.stroke = new StrokeStyle(stroke, this);
		markDirty();
	}

	/*
	 * Path Style
	 */
	public WindingRule getWindingRule() {
		update();
		return windingRule;
	}

	public void setWindingRule(WindingRule rule) {
		update();
		this.windingRule = rule;
		markDirty();
	}

	/**
	 * The output resolution for the path.
	 */
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
	 * Stroke Styles
	 */

	/**
	 * {@grouptitle Stroke Style}
	 * 
	 * The color of the stroke.
	 * 
	 * Sample code:
	 * <code>
	 * // Create a circle shaped path at { x: 50, y: 50 } with a radius of 10:
	 * var circle = new Path.Circle(new Point(50, 50), 10);
	 * 
	 * // Set the stroke color of the circle to CMYK red:
	 * circle.strokeColor = new CMYKColor(1, 1, 0, 0);
	 * </code>
	 */
	public Color getStrokeColor() {
		// TODO: Return Color.NONE instead of null?
		return stroke != null ? stroke.getColor() : null;
	}

	public void setStrokeColor(Color color) {
		getStroke(true).setColor(color);
	}

	public void setStrokeColor(java.awt.Color color) {
		getStroke(true).setColor(color);
	}

	/**
	 * The width of the stroke.
	 * 
	 * Sample code:
	 * <code>
	 * // Create a circle shaped path at { x: 50, y: 50 } with a radius of 10:
	 * var circle = new Path.Circle(new Point(50, 50), 10);
	 * 
	 * // Set the stroke width of the circle to 3pt:
	 * circle.strokeWidth = 3;
	 * </code>
	 */
	public Float getStrokeWidth() {
		return stroke != null ? stroke.getWidth() : null;
	}

	public void setStrokeWidth(Float width) {
		getStroke(true).setWidth(width);
	}

	/**
	 * The cap of the stroke.
	 * 
	 * Sample code:
	 * <code>
	 * // Create a line from { x: 0, y: 50 } to { x: 50, y: 50 };
	 * var line = new Path.Line(new Point(0, 50), new Point(50, 50));
	 * 
	 * // Set the stroke cap of the line to be round:
	 * line.strokeCap = 'round';
	 * </code>
	 */
	public StrokeCap getStrokeCap() {
		return stroke != null ? stroke.getCap() : null;
	}

	public void setStrokeCap(StrokeCap cap) {
		getStroke(true).setCap(cap);
	}

	/**
	 * The join of the stroke.
	 */
	public StrokeJoin getStrokeJoin() {
		return stroke != null ? stroke.getJoin() : null;
	}

	public void setStrokeJoin(StrokeJoin join) {
		getStroke(true).setJoin(join);
	}

	/**
	 * The dash offset of the stroke.
	 */
	public Float getDashOffset() {
		return stroke != null ? stroke.getDashOffset() : null;
	}

	public void setDashOffset(Float offset) {
		getStroke(true).setDashOffset(offset);
	}
	
	/**
	 * Specifies an array containing the dash and gap lengths of the stroke.
	 * 
	 * Sample code:
	 * 
	 * <code>
	 * // Create a line from { x: 0, y: 50 } to { x: 50, y: 50 };
	 * var line = new Path.Line(new Point(0, 50), new Point(50, 50));
	 * 
	 * line.strokeWidth = 3;
	 * 
	 * // Set the dashed stroke to [10pt dash, 5pt gap, 8pt dash, 10pt gap]:
	 * line.dashArray = [10, 5, 8, 10];
	 * </code>
	 */
	public float[] getDashArray() {
		return stroke != null ? stroke.getDashArray() : null;
	}

	public void setDashArray(float[] array) {
		getStroke(true).setDashArray(array);
	}
	
	/**
	 * The miter limit controls when the program switches from a mitered
	 * (pointed) join to a beveled (squared-off) join. The default miter limit
	 * is 4, which means that when the length of the point reaches four times
	 * the stroke weight, the program switches from a miter join to a bevel
	 * join. A miter limit of 0 results in a bevel join.
	 * 
	 * @return the miter limit as a value between 0 and 500
	 */
	public Float getMiterLimit() {
		return stroke != null ? stroke.getMiterLimit() : null;
	}

	public void setMiterLimit(Float limit) {
		getStroke(true).setMiterLimit(limit);
	}

	/**
	 * Specifies whether to overprint the stroke. By default, when you print
	 * opaque, overlapping colors, the top color knocks out the area underneath.
	 * You can use overprinting to prevent knockout and make the topmost
	 * overlapping printing ink appear transparent in relation to the underlying
	 * ink.
	 * 
	 * @return {@true if the stroke is overprinted}
	 */
	public Boolean getStrokeOverprint() {
		return stroke != null ? stroke.getOverprint() : null;
	}

	public void setStrokeOverprint(Boolean overprint) {
		getStroke(true).setOverprint(overprint);
	}

	/*
	 * Fill Style
	 */
	/**
	 * {@grouptitle Fill Style}
	 * 
	 * The fill color of the path.
	 * 
	 * Sample code:
	 * <code>
	 * // Create a circle shaped path at { x: 50, y: 50 } with a radius of 10:
	 * var circle = new Path.Circle(new Point(50, 50), 10);
	 * 
	 * // Set the fill color of the circle to CMYK red:
	 * circle.fillColor = new CMYKColor(1, 1, 0, 0);
	 * </code>
	 */
	public Color getFillColor() {
		// TODO: Return Color.NONE instead of null?
		return fill != null ? fill.getColor() : null;
	}

	public void setFillColor(Color color) {
		getFill(true).setColor(color);
	}

	public void setFillColor(java.awt.Color color) {
		getFill(true).setColor(color);
	}

	/**
	 * Specifies whether to overprint the fill. By default, when you print
	 * opaque, overlapping colors, the top color knocks out the area underneath.
	 * You can use overprinting to prevent knockout and make the topmost
	 * overlapping printing ink appear transparent in relation to the underlying
	 * ink.
	 * 
	 * @return {@true if the fill is overprinted}
	 */
	public Boolean getFillOverprint() {
		return fill != null ? fill.getOverprint() : null;
	}

	public void setFillOverprint(Boolean overprint) {
		getFill(true).setOverprint(overprint);
	}
}
