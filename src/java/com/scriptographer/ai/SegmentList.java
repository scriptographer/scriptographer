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
 * File created on 14.12.2004.
 *
 * $RCSfile: SegmentList.java,v $
 * $Author: lehni $
 * $Revision: 1.4 $
 * $Date: 2005/03/25 17:09:15 $
 */

package com.scriptographer.ai;

import java.util.*;
import java.awt.geom.Point2D;

import com.scriptographer.util.ExtendedJavaList;
import com.scriptographer.util.AbstractFetchList;

public class SegmentList extends AbstractFetchList {
	protected Path path;
	protected int length;
	protected CurveList curves = null;

	private ExtendedJavaList list;

	private int maxVersion = -1; // the latest version of segments, even if there's only one of this version
	private int lengthVersion = -1;

	// how many float values are stored in a segment:
	// use this ugly but fast hack: the AIPathSegment represents roughly an array
	// of 6 floats (for the 3 AIRealPoints p, in, out) + a char (boolean corner)
	// due to the data alignment, there are 3 empty bytes after the char.
	// when using a float array with seven elements, the first 6 are set correctly.
	// the first byte of the 7th represents the boolean value. if this float is set
	// to 0 before fetching, it will be == 0 if false, and != 0 if true, so that's
	// all we want in the java environment:
	protected static final int VALUES_PER_SEGMENT = 7;

	public SegmentList() {
		list = new ExtendedJavaList();
		length = 0;
	}

	protected SegmentList(Path path) {
		this();
		this.path = path;
		updateLength(-1);
	}
	
	/*
	 *  postscript-like interface: moveTo, lineTo, curveTo, arcTo
	 */	
	public void moveTo(float x, float y) {
		if (length > 0)
			throw new UnsupportedOperationException("moveTo can only be called at the beginning of a SegmentList");
		add(new Segment(x, y));
	}
	
	public void moveTo(Point pt) {
		moveTo(pt.x, pt.y);
	}
	
	public void moveTo(Point2D pt) {
		moveTo((float) pt.getX(), (float) pt.getY());
	}
	
	public void lineTo(float x, float y) {
		if (length == 0)
			throw new UnsupportedOperationException("Use a moveTo command first");
		add(new Segment(x, y));
	}
	
	public void lineTo(Point pt) {
		lineTo(pt.x, pt.y);
	}
	
	public void lineTo(Point2D pt) {
		lineTo((float) pt.getX(), (float) pt.getY());
	}
	
	public void curveTo(float c1x, float c1y, float c2x, float c2y, float x, float y) {
		if (length == 0)
			throw new UnsupportedOperationException("Use a moveTo command first");
		// first modify the current segment:
		Segment lastSegment = getSegment(length - 1);
		lastSegment.handleOut.setLocation(c1x, c1y);
		lastSegment.setCorner(false);
		// and add the new segment, with handleIn set to c2
		add(new Segment(x, y, c2x, c2y, x, y, false));
	}
	
	public void curveTo(Point c1, Point c2, Point pt) {
		curveTo(c1.x, c1.y, c2.x, c2.y, pt.x, pt.y);
	}
	
	public void curveTo(Point2D c1, Point2D c2, Point2D pt) {
		curveTo((float) c1.getX(), (float) c1.getY(), (float) c2.getX(), (float) c2.getY(), (float) pt.getX(), (float) pt.getY());
	}

	public void arcTo(float centerX, float centerY, float endX, float endY, int ccw) {
		if (length == 0)
			throw new UnsupportedOperationException("Use a moveTo command first");
		
		// get the startPoint:
		Segment startSegment = getSegment(length - 1);
		double startX = startSegment.point.x;
		double startY = startSegment.point.y;
		
		// determine the width and height of the ellipse by the 3 given points
		// center, startPoint and endPoint:
		// find the scaleFactor that scales this system horicontally so a circle
		// would fit. the resulting radius is the ellipse's height.
		// Then apply the opposite factor to the radius in order to get the width.
		
		double x1 = startX - centerX;
		double y1 = startY - centerY;
		double x2 = endX - centerX;
		double y2 = endY - centerY;
		
		double s = Math.sqrt(
			(y2 * y2 - y1 * y1) /
			(x1 * x1 - x2 * x2)
		);
		
		double h = Math.sqrt(x1 * x1 + y1 * y1);
		double w = h / s;
		if (s == 0 || h == 0)
			throw new UnsupportedOperationException("Cannot create an arc with the given center end starting point");
		
		// Note: reversing the Y equations negates the angle to adjust
		// for the upside down coordinate system.
		double angle = Math.atan2(centerY - startY, startX - centerX);
		double extent = Math.atan2(centerY - endY, endX - centerX);
		extent -= angle;
		if (extent <= 0.0) {
			extent += Math.PI * 2.0;
		}
		if (ccw < 0) extent = Math.PI * 2.0 - extent;
		else extent = -extent;
		angle = -angle;
			
		double ext = Math.abs(extent);
		int arcSegs;
		if (ext >= 2 * Math.PI) arcSegs = 4;
		else arcSegs = (int) Math.ceil(ext * 2 / Math.PI);

		double inc = extent;
		if (inc > 2 * Math.PI) inc = 2 * Math.PI;
		else if (inc < -2 * Math.PI) inc = -2 * Math.PI;
		inc /= arcSegs;
		
		double halfInc = inc / 2.0;
		double z = 4.0 / 3.0 * Math.sin(halfInc) / (1.0 + Math.cos(halfInc));
		
		for (int i = 0; i <= arcSegs; i++) {
			double relx = Math.cos(angle);
			double rely = Math.sin(angle);
			Point pt = new Point(centerX + relx * w, centerY + rely * h);
			Point out;
			if (i == arcSegs) out = null;
			else out = new Point(centerX + (relx - z * rely) * w, centerY + (rely + z * relx) * h);
			if (i == 0) {
				// modify startSegment
				startSegment.handleOut.setLocation(out);
			} else {
				// add new Segment
				Point in = new Point(centerX + (relx + z * rely) * w, centerY + (rely - z * relx) * h);
				add(new Segment(pt, in, out, false));
			}
			angle += inc;
		}
	}

	public void arcTo(Point center, Point endPoint, int ccw) {
		arcTo(center.x, center.y, endPoint.x, endPoint.y, ccw);
	}

	public void arcTo(Point2D center, Point2D endPoint, int ccw) {
		arcTo((float) center.getX(), (float) center.getY(), (float) endPoint.getX(), (float) endPoint.getY(), ccw);
	}

	protected Path getPath() {
		return path;
	}

	public int hashCode() {
		return path != null ? path.handle : super.hashCode();
	}

	private static native int nativeGetLength(int handle);

	/**
	 * Fetches the length from the underlying AI structure and puts the internal reflection
	 * in the right state and length.
	 *
	 * @param newLength If specified, nativeGetLength doesn't need to be called in order to speed things up.
	 */
	protected void updateLength(int newLength) {
		if (path != null) {
			// updateLength is called in the beginning of a SegmentList and whenever the list completely changes,
			// e.g. when Path.reduceSegments is called. In these cases, the existing segments are not valid anymore:
			for (int i = 0; i < length; i++) {
				Segment segment = (Segment) list.get(i);
				if (segment != null) {
					// detach from SegmentList and set null
					segment.segments = null;
					list.set(i, null);
				}
			}
			if (newLength == -1)
				newLength = nativeGetLength(path.handle);
			list.setSize(newLength);
			length = newLength;
			if (curves != null)
				curves.updateLength();
			// decrease maxVersion so elements gets refetched, see fetch:
			maxVersion--;
			lengthVersion = path.version;
		}
	}

	protected void checkUpdate() {
		if (path != null && lengthVersion != path.version) {
			updateLength(-1);
		}
	}

	protected static native void nativeFetch(int handle, int index, int count, float[] values);
	protected static native void nativeCommit(int handle, int index, float pointX, float pointY, float inX, float inY, float outX, float outY, boolean corner);
	protected static native void nativeCommit(int handle,int index, int count, float[] values);
	protected static native void nativeInsert(int handle, int index, float pointX, float pointY, float inX, float inY, float outX, float outY, boolean corner);
	protected static native void nativeInsert(int handle,int index, int count, float[] values);

	/**
	 * Fetches a series of segments from the underlying Adobe Illustrator Path.
	 *
	 * @param fromIndex
	 * @param toIndex
	 */
	protected void fetch(int fromIndex, int toIndex) {
		if (path != null) {
			int pathVersion = path.version;
			// if all are out of maxVersion or only one segment is fetched, no scanning for valid segments is needed:
			int fetchCount = toIndex - fromIndex;
			boolean fetchAll = maxVersion != pathVersion || fetchCount <= 1;

			// even if not everything is fetched, update the maxVersion as
			// this field just signifies that some elements are up to date.
			maxVersion = pathVersion;

			int start = fromIndex, end;

			float []values = null;
			while (true) {
				if (fetchAll) {
					end = toIndex;
				} else {
					// skip the ones that are alreay fetched:
					Segment segment;
					while (start < toIndex &&
							((segment = (Segment) list.get(start)) != null) &&
							segment.version == pathVersion) {
						start++;
					}

					if (start == toIndex) // all fetched, jump out
						break;

					// now determine the length of the block that needs to be fetched:
					end = start + 1;

					/*
					// if keepCount is set, try to fetch fetchCount items:
					if (keepCount && toIndex - start < fetchCount) {
						toIndex = start + fetchCount;
						if (toIndex > length)
							toIndex = length;
					}
					*/

					while (end < toIndex && (
								((segment = (Segment) list.get(start)) == null) ||
								(segment != null && segment.version != pathVersion)
							)
						)
						end++;
				}

				// fetch these segmentValues and set the segments:
				int count = end - start;
				fetchCount -= count;
				if (values == null || values.length < count)
					values = new float[count * VALUES_PER_SEGMENT];
//				System.out.println("nativeFetch " + start + " " + count);
				nativeFetch(path.handle, start, count, values);
				int valueIndex = 0;
				for (int i = start; i < end; i++) {
					Segment segment = (Segment) list.get(i);
					if (segment == null) {
						segment = new Segment(this, i);
						list.set(i, segment);
					}
					segment.setValues(values, valueIndex);
					segment.version = pathVersion;
					valueIndex += VALUES_PER_SEGMENT;
				}

				// are we at the end? if so, jump out
				if (end == toIndex)
					break;

				// otherwise set start to end and continue
				start = end;
			}
		}
	}

	protected void fetch() {
		if (length > 0)
			fetch(0, length);
	}

	public Object get(int index) {
		// as fetching doesn't cost so much but calling JNI functions does, fetch a few elements in the neighborhood
		// at a time:
		int fromIndex = index - 2;
		if (fromIndex < 0)
			fromIndex = 0;

		int toIndex = fromIndex + 4;
		if (toIndex > length)
			toIndex = length;
		fetch(fromIndex, toIndex);
		return list.get(index);
	}

	public Segment getSegment(int index) {
		return (Segment) get(index);
	}

	public boolean add(int index, Object obj) {
		Segment segment;
		if (obj instanceof Segment) {
			segment = (Segment) obj;
			// copy it if it comes from another list:
			if (segment.segments != null)
				segment = new Segment(segment);
		} else if (obj instanceof Point2D) {
			// convert single points to a segment
			segment = new Segment((Point2D) obj);
		} else return false;
		// add to internal structure
		list.add(index, segment);
		length++;
		// now update the segment and set the values in the value array:
		segment.segments = this;
		segment.index = index;
		// and add to illustrator as well
		segment.insert();
		// updatePoint indices
		for (int i = index + 1; i < length; i++) {
			segment = (Segment) list.get(i);
			if (segment != null)
				segment.index = i;
		}
		return true;
	}

	public boolean addAll(int index, Collection c) {
		if (index < 0 || index > length)
			throw new IndexOutOfBoundsException("Index: "+index+", Size: "+length);

		int count = c.size();
		if (count == 0)
			return false;

		float[] values;
		int commitVersion;
		if (path != null) {
			values = new float[count * VALUES_PER_SEGMENT];
			commitVersion = path.version;
		} else {
			values = null;
			commitVersion = 0;
		}

		int valueIndex = 0;
		int addCount = 0;
		int addIndex = index;

		Iterator e = c.iterator();
		while (e.hasNext()) {
			Object obj = e.next();
			Segment segment = null;
			if (obj instanceof Segment) {
				segment = (Segment) obj;
				// copy it if it comes from another list:
				if (segment.segments != null)
					segment = new Segment(segment);
			} else if (obj instanceof Point2D) {
				// convert single points to a segment
				segment = new Segment((Point2D) obj);
			}
			if (segment != null) {
				// add to internal structure
				list.add(addIndex++, segment);
				// update verion:
				segment.version = commitVersion;
				// set values in array
				if (values != null) {
					segment.getValues(values, valueIndex);
					valueIndex += VALUES_PER_SEGMENT;
				}
				addCount++;
			}
		}

		// and add the segments to illustrator as well
		if (values != null && addCount > 0) {
			SegmentList.nativeInsert(path.handle, index, addCount, values);
			return true;
		}

		return false;
	}

	public Object set(int index, Object obj) {
		if (obj instanceof Segment) {
			Segment segment = (Segment) obj;
			Segment ret = (Segment) list.set(index, segment);
			segment.segments = this;
			segment.index = index;
			segment.markDirty();
			if (ret != null)
				ret.segments = null;
			return ret;

		}
		return null;
	}

	public int getLength() {
		return length;
	}

	public boolean isEmpty() {
		return length == 0;
	}

	private static native int nativeRemove(int handle, int index, int count);

	public void remove(int fromIndex, int toIndex) {
		if (fromIndex < toIndex) {
			int newSize = length + fromIndex - toIndex;

			for (int i = fromIndex; i < toIndex; i++) {
				Segment obj = (Segment) list.get(i);
				if (obj != null)
					obj.segments = null;
			}

			if (path != null) {
				length = nativeRemove(path.handle, fromIndex, toIndex - fromIndex);
			}

			list.removeRange(fromIndex, toIndex);

			length = newSize;
		}

	}

	public Object remove(int index) {
		Object obj = get(index);
		remove(index, index + 1);
		return obj;
	}

	private static native void nativeReverse(int handle);

	public void reverse() {
		if (path != null) {
			// reverse underlying ai structures:
			nativeReverse(path.handle);
		}
		// reverse internal arrays:
		Object[] objs = list.toArray();

		for (int i = 0; i < length; i++) {
			int ri = length - i - 1;
			Segment obj = (Segment)objs[ri];
			if (obj != null)
				obj.index = i;
			list.set(i, obj);
		}
	}

	public String toString() {
		fetch();
		StringBuffer buf = new StringBuffer(256);
		buf.append("[ ");
		for (int i = 0; i < length; i++) {
			Segment obj = (Segment) get(i);
			if (i > 0) buf.append(", ");
			buf.append(obj.toString());
		}
		buf.append(" ]");
		return buf.toString();
	}
}
