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
 * File created on 03.12.2004.
 *
 * $RCSfile: Path.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:01:01 $
 */

package com.scriptographer.ai;

import org.mozilla.javascript.NativeArray;

import java.util.Collection;
import java.util.Arrays;

public class Path extends Art {

	private SegmentList segments = null;
	private CurveList curves = null;

	/**
	 * Wraps an AIArtHandle in a Path object
	 */
	protected Path(Integer handle) {
		super(handle);
	}

	/**
	 * Creates a path object
	 */
	public Path() {
		super(TYPE_PATH);
	}

	public Path(Collection segments) {
		this();
		setSegments(segments);
	}

	public Path(Object[] segments) {
		this(Arrays.asList(segments));
	}

	public static native Path createRectangle(Rectangle rect);
	public static native Path createRoundRectangle(Rectangle rect, float hor, float ver);
	public static native Path createOval(Rectangle rect, boolean circumscribed);
	public static native Path createRegularPolygon(int numSides, Point center, float radius);
	public static native Path createStar(int numPoints, Point center, float radius1, float radius2);
	public static native Path createSpiral(Point firstArcCenter, Point start, float decayPercent, int numQuarterTurns, boolean clockwiseFromOutside);

	public static Path createOval(Rectangle rect) {
		return createOval(rect, false);
	}

	public int hashCode() {
		return artHandle;
	}

	public SegmentList getSegments() {
		if (segments == null)
			segments = new SegmentList(this);
		else
			segments.checkUpdate();
		return segments;
	}

	public void setSegments(Collection segments) {
		SegmentList list = getSegments();
		// TODO: implement SegmentList.setAll so clear is not necesssary and nativeCommit is used instead of nativeInsert
		// removeRange would still be needed in cases the new list is smaller than the old one...
		list.clear();
		list.addAll(segments);
	}

	public void setSegments(Object[] segments) {
		setSegments(Arrays.asList(segments));
	}

	public CurveList getCurves() {
		if (curves == null)
			curves = new CurveList(this, getSegments());
		return curves;
	}

	public boolean remove() {
		boolean ret = super.remove();
		// dereference from path if they're used somewhere else!
		if (segments != null)
			segments.path = null;
		return ret;
	}

	public native boolean getClosed();
	public native void setClosed(boolean closed);
	public native boolean getGuide();
	public native void setGuide(boolean guide);
	public native TabletValue[] getTabletData();
	public native void setTabletData(TabletValue[] data);

	public native float getLength(float flatness);

	public float getLength() {
		return getLength(0.1f);
	}

	public native float getArea();

	private static native int nativePointsToCurves(int handle, float tolerance, float threshold, int cornerRadius, float scale);

	public int pointsToCurves(float tolerance, float threshold, int cornerRadius, float scale) {
		int length = nativePointsToCurves(artHandle, tolerance, threshold, cornerRadius, scale);
		if (segments != null)
			segments.updateLength(length);
		return length;
	}

	public int pointsToCurves(float tolerance, float threshold, int cornerRadius) {
		return pointsToCurves(tolerance, threshold, cornerRadius, 1f);
	}

	public int pointsToCurves(float tolerance, float threshold) {
		return pointsToCurves(tolerance, threshold, 1, 1f);
	}

	public int pointsToCurves(float tolerance) {
		return pointsToCurves(tolerance, 1f, 1, 1f);
	}

	public int pointsToCurves() {
		return pointsToCurves(2.5f, 1f, 1, 1f);
	}

	private static native int nativeCurvesToPoints(int handle, float maxPointDistance, float flatness);

	public int curvesToPoints(float maxPointDistance, float flatness) {
		int length = nativeCurvesToPoints(artHandle, maxPointDistance, flatness);
		if (segments != null)
			segments.updateLength(length);
		return length;
	}

	public int curvesToPoints(float maxPointDistance) {
		return curvesToPoints(maxPointDistance, 0.1f);
	}

	public int curvesToPoints() {
		return curvesToPoints(1000f, 0.1f);
	}

	private static native void nativeReduceSegments(int handle, float flatness);

	public void reduceSegments(float flatness) {
		nativeReduceSegments(artHandle, flatness);
		if (segments != null)
			segments.updateLength(-1);
	}

	public void reduceSegments() {
		reduceSegments(0.1f);
	}

	private static native int nativeSplit(int handle, int index, float position);

	public Path split(int index, float position) {
		int handle = nativeSplit(artHandle, index, position);
		if (handle != 0) {
			Path path = new Path(new Integer(handle));
			if (segments != null)
				segments.updateLength(-1);
			return path;
		}
		return null;
	}

	public Path split(int index) {
		return split(index, 0f);
	}

	public native SegmentPosition hitTest(Point point, float epsilon);

	public SegmentPosition hitTest(Point point) {
		return hitTest(point, 0.001f);
	}

	public native SegmentPosition getPositionWithLength(float length, float flatness);

	public SegmentPosition getPosWithLength(float length) {
		return getPositionWithLength(length, 0.1f);
	}
}