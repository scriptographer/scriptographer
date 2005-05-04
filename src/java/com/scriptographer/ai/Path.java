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
 * $Revision: 1.9 $
 * $Date: 2005/05/04 23:40:55 $
 */

package com.scriptographer.ai;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptRuntime;

import com.scriptographer.CommitManager;
import com.scriptographer.js.FunctionHelper;
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
    
	public Object clone() {
		// TODO: only commit the objects that concenr this art object, not everything!
		CommitManager.commit();
		return super.clone();
	}

	public SegmentList getSegments() {
		if (segments == null)
			segments = new SegmentList(this);
		else
			segments.checkUpdate();
		return segments;
	}

	public void setSegments(Collection list) {
		SegmentList segments = getSegments();
		// TODO: implement SegmentList.setAll so clear is not necesssary and nativeCommit is used instead of nativeInsert
		// removeRange would still be needed in cases the new list is smaller than the old one...
		segments.clear();
		segments.addAll(list);
	}

	public void setSegments(Object[] segments) {
		setSegments(Arrays.asList(segments));
	}

	public CurveList getCurves() {
		if (curves == null)
			curves = new CurveList(this, getSegments());
		return curves;
	}

	public native boolean isClosed();
	public native void setClosed(boolean closed);
	public native boolean isGuide();
	public native void setGuide(boolean guide);
	public native TabletValue[] getTabletData();
	public native void setTabletData(TabletValue[] data);
	
	public void setTabletData(NativeArray data) {
		Object[] array = FunctionHelper.convertToArray(data);
		ArrayList values = new ArrayList();
		for (int i = 0; i < array.length; i++) {
			Object obj = array[i];
			TabletValue value = null;
			if (obj instanceof Object[]) {
				Object[] objArray = (Object[]) obj;
				value = new TabletValue(
					(float) ScriptRuntime.toNumber(objArray[0]),
					(float) ScriptRuntime.toNumber(objArray[1])
				);
			} else if (obj instanceof TabletValue) {
				value = (TabletValue) obj;
			}
			if (value != null)
				values.add(value);
		}
		TabletValue[] tabletData = new TabletValue[values.size()];
		values.toArray(tabletData);
		setTabletData(tabletData);
	}

	public native float getLength(float flatness);

	public float getLength() {
		return getLength(Curve.FLATNESS);
	}

	public native float getArea();

	private static native int nativePointsToCurves(int handle, float tolerance, float threshold, int cornerRadius, float scale);

	public int pointsToCurves(float tolerance, float threshold, int cornerRadius, float scale) {
		int length = nativePointsToCurves(handle, tolerance, threshold, cornerRadius, scale);
		if (segments != null)
			segments.updateSize(length);
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
			segments.updateSize(length);
		return length;
	}

	public int curvesToPoints(float maxPointDistance) {
		return curvesToPoints(maxPointDistance, Curve.FLATNESS);
	}

	public int curvesToPoints() {
		return curvesToPoints(1000f, Curve.FLATNESS);
	}

	private static native void nativeReduceSegments(int handle, float flatness);

	public void reduceSegments(float flatness) {
		nativeReduceSegments(handle, flatness);
		if (segments != null)
			segments.updateSize(-1);
	}

	public void reduceSegments() {
		reduceSegments(Curve.FLATNESS);
	}

	public Path split(int index, float parameter) {
		SegmentList segments = getSegments();
		Object[] newSegments = null;

		if (parameter < 0.0f) parameter = 0.0f;
		else if (parameter >= 1.0f) {
			// t = 1 is the same as t = 0 and index ++
			index++;
			parameter = 0.0f;
		}
		if (index >= 0 && index < segments.size - 1) {
			if (parameter == 0.0) { // spezial case
				if (index > 0) {
					// split at index
					newSegments = segments.toArray(index, segments.size);
					segments.remove(index + 1, segments.size);
				}
			}
			// divide the segment at index at parameter
			Segment segment = (Segment) segments.get(index);
			if (segment != null) {
				segment.divide(parameter);
				// create the new path with the segments to the right of t
				newSegments = segments.toArray(index + 1, segments.size);
				// and delete these segments from the current path, not including the divided point
				segments.remove(index + 2, segments.size);
			}
		}
		if (newSegments != null)
			return new Path(newSegments);
		else
			return null;
	}

	public Path split(int index) {
		return split(index, 0f);
	}

	public CurveParameter hitTest(Point point, float epsilon) {
		CurveList curves = getCurves();
		int length = curves.size();
		
		for (int i = 0; i < length; i++) {
			Curve curve = (Curve) curves.get(i);
			float t = curve.hitTest(point, epsilon);
			if (t >= 0)
				return new CurveParameter(curve, t);
		}
		return null;
	}

	public CurveParameter hitTest(Point point) {
		return hitTest(point, Curve.EPSILON);
	}

	public CurveParameter getParameterWithLength(float length, float flatness) {
		CurveList curves = getCurves();
		float currentLength = 0;
		for (int i = 0; i < curves.size; i++) {
			float startLength = currentLength;
			Curve curve = (Curve) curves.get(i);
			currentLength += curve.getLength(flatness);
			if (currentLength >= length) { // found the segment within which the length lies
				float t = curve.getParameterWithLength(length - startLength, flatness);
				return new CurveParameter(curve, t);
			}
		}
		return null;
	}

	public CurveParameter getParameterWithLength(float length) {
		return getParameterWithLength(length, Curve.FLATNESS);
	}
	
	/*
	 *  postscript-like interface: moveTo, lineTo, curveTo, arcTo
	 */	
	public void moveTo(float x, float y) {
		getSegments().moveTo(x, y);
	}
	
	public void moveTo(Point pt) {
		getSegments().moveTo(pt);
	}
	
	public void moveTo(Point2D pt) {
		getSegments().moveTo(pt);
	}
	
	public void lineTo(float x, float y) {
		getSegments().lineTo(x, y);
	}
	
	public void lineTo(Point pt) {
		getSegments().lineTo(pt);
	}
	
	public void lineTo(Point2D pt) {
		getSegments().lineTo(pt);
	}
	
	public void curveTo(float c1x, float c1y, float c2x, float c2y, float x, float y) {
		getSegments().curveTo(c1x, c1y, c2x, c2y, x, y);
	}
	
	public void curveTo(Point c1, Point c2, Point pt) {
		getSegments().curveTo(c1, c2, pt);
	}
	
	public void curveTo(Point2D c1, Point2D c2, Point2D pt) {
		getSegments().curveTo(c1, c2, pt);
	}

	public void arcTo(float centerX, float centerY, float endX, float endY, int ccw) {
		getSegments().arcTo(centerX, centerY, endX, endY, ccw);
	}

	public void arcTo(Point center, Point endPoint, int ccw) {
		getSegments().arcTo(center, endPoint, ccw);
	}

	public void arcTo(Point2D center, Point2D endPoint, int ccw) {
		getSegments().arcTo(center, endPoint, ccw);
	}
	
	public Segment getFirstSegment() {
		return getSegments().getFirstSegment();
	}
	
	public Segment getLastSegment() {
		return getSegments().getLastSegment();
	}
}