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
 * $Revision: 1.5 $
 * $Date: 2005/03/30 08:21:32 $
 */

package com.scriptographer.ai;

import java.util.Collection;
import java.util.Arrays;

import com.scriptographer.util.Handle;

public class Path extends Art {

	private SegmentList segments = null;
	private CurveList curves = null;

	/**
	 * Wraps an AIArtHandle in a Path object
	 */
	protected Path(Handle handle) {
		super(handle);
	}

	/**
	 * Creates a path object
	 */
	public Path(Document document) {
		super(document, TYPE_PATH);
	}

	public Path(Document document, Collection segments) {
		this(document);
		setSegments(segments);
	}

	public Path(Document document, Object[] segments) {
		this(document, Arrays.asList(segments));
	}
	
	public Path() {
		super(null, TYPE_PATH);
	}

	public Path(Collection segments) {
		this();
		setSegments(segments);
	}

	public Path(Object[] segments) {
		this(Arrays.asList(segments));
	}

    public boolean remove() {
        boolean ret = super.remove();
        // dereference from path if they're used somewhere else!
        if (segments != null)
            segments.path = null;
        return ret;
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
		int length = nativePointsToCurves(handle, tolerance, threshold, cornerRadius, scale);
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
		int length = nativeCurvesToPoints(handle, maxPointDistance, flatness);
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
		nativeReduceSegments(handle, flatness);
		if (segments != null)
			segments.updateLength(-1);
	}

	public void reduceSegments() {
		reduceSegments(0.1f);
	}

	private static native int nativeSplit(int handle, int index, float position);

	public Path split(int index, float position) {
		int newHandle = nativeSplit(handle, index, position);
		if (newHandle != 0) {
			Path path = new Path(new Handle(newHandle));
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