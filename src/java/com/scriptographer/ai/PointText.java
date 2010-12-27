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
 * File created on 23.10.2005.
 */

package com.scriptographer.ai;

/**
 * A PointText item represents text in an Illustrator document which starts from
 * a certain point and expands by the amount of characters contained in it.
 * 
 * @author lehni
 */
public class PointText extends TextItem {

	protected PointText(int handle, int docHandle, boolean created) {
		super(handle, docHandle, created);
	}

	native private static int nativeCreate(int orientation, double x, double y);
	
	/**
	 * Creates a point text item
	 * 
	 * Sample code:
	 * <code>
	 * var text = new PointText(new Point(50, 100));
	 * text.content = 'The contents of the point text';
	 * </code>
	 * 
	 * @param point the point where the text will begin
	 * @param orient the text orientation {@default 'horizontal'}
	 */
	public PointText(Point point, TextOrientation orientation) {
		super(nativeCreate(orientation != null
				? orientation.value : TextOrientation.HORIZONTAL.value,
				(float) point.x, (float) point.y));
	}

	public PointText(Point point) {
		this(point, TextOrientation.HORIZONTAL);
	}

	public PointText() {
		this(new Point());
	}

	/**
	 * The PointText's anchor point
	 */
	public native Point getPoint();

	public void setPoint(Point point) {
		if (point != null)
			translate(point.subtract(getPoint()));
	}
}
