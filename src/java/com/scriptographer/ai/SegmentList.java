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
 * File created on 14.12.2004.
 *
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.list.ArrayList;
import com.scratchdisk.list.List;
import com.scriptographer.list.AbstractFetchList;

/**
 * @author lehni
 */
public class SegmentList extends AbstractFetchList<Segment> {
	protected Path path;
	protected int size;
	protected CurveList curves = null;

	private ArrayList.List<Segment> list;

	private int lengthVersion = -1;

	// How many float values are stored in a segment:
	// use this ugly but fast hack: the AIPathSegment represents roughly an
	// array of 6 floats (for the 3 AIRealPoints p, in, out)
	// + a char (boolean corner)
	// Due to the data alignment, there are 3 empty bytes after the char.
	// when using a float array with seven elements, the first 6 are set
	// correctly.
	// The first byte of the 7th represents the boolean value. if this float is
	// set to 0 before fetching, it will be == 0 if false, and != 0 if true, so
	// that's all we want in the java environment:
	protected static final int VALUES_PER_SEGMENT = 7;

	public SegmentList() {
		list = new ArrayList.List<Segment>();
		size = 0;
	}

	protected SegmentList(Path path) {
		this();
		this.path = path;
		updateSize(-1);
	}

	public Path getPath() {
		return path;
	}

	private static native int nativeGetSize(int handle);

	/**
	 * Fetches the length from the underlying AI structure and puts the internal
	 * reflection in the right state and length.
	 * 
	 * @param newSize If specified, nativeSize doesn't need to be called in
	 *        order to speed things up.
	 */
	protected void updateSize(int newSize) {
		if (path != null) {
			if (newSize != size) {
				if (newSize == -1)
					newSize = nativeGetSize(path.handle);
				list.setSize(newSize);
				size = newSize;
				if (curves != null)
					curves.updateSize();
			}
			lengthVersion = path.version;
		}
	}

	/**
	 * updates the synchronization between the cached segments in java
	 * and the underlying Illustrator object.
	 * Only called from Path.getSegmentList()
	 */
	protected void update() {
		if (path != null && lengthVersion != path.version) {
			updateSize(-1);
		}
	}

	protected static native void nativeGet(int handle, int index, int count,
			float[] values);

	// docHandle seems to be only needed for modifying code!
	protected static native void nativeSet(int handle, int docHandle,
			int index, float pointX, float pointY, float inX, float inY,
			float outX, float outY, boolean corner);

	protected static native void nativeSet(int handle, int docHandle,
			int index, int count, float[] values);

	protected static native void nativeInsert(int handle, int docHandle,
			int index, float pointX, float pointY, float inX, float inY,
			float outX, float outY, boolean corner);

	protected static native void nativeInsert(int handle, int docHandle,
			int index, int count, float[] values);

	// for point selections
	protected static native short nativeGetSelectionState(int handle, int index);

	protected static native void nativeSetSelectionState(int handle,
			int docHandle, int index, short state);

	/**
	 * Fetches a series of segments from the underlying Adobe Illustrator Path.
	 * 
	 * @param fromIndex
	 * @param toIndex
	 */
	protected void fetch(int fromIndex, int toIndex) {
		if (path != null) {
			int pathVersion = path.version;
			// if all are out of maxVersion or only one segment is fetched, no
			// scanning for valid segments is needed:
			int fetchCount = toIndex - fromIndex;

			int start = fromIndex, end;

			float []values = null;
			while (true) {
				// skip the ones that are alreay fetched:
				Segment segment;
				while (start < toIndex &&
						((segment = (Segment) list.get(start)) != null) &&
						segment.version == pathVersion) {
					start++;
				}

				if (start == toIndex) // all fetched, jump out
					break;

				// now determine the length of the block that needs to be
				// fetched:
				end = start + 1;

				while (end < toIndex &&
						((segment = (Segment) list.get(start)) == null ||
						segment != null && segment.version != pathVersion)) {
					end++;
				}

				// fetch these segmentValues and set the segments:
				int count = end - start;
				if (count > 0) {
					fetchCount -= count;
					int length =  count * VALUES_PER_SEGMENT;
					if (values == null || values.length < length)
						values = new float[length];
					nativeGet(path.handle, start, count, values);
					int valueIndex = 0;
					for (int i = start; i < end; i++) {
						segment = (Segment) list.get(i);
						if (segment == null) {
							segment = new Segment(this, i);
							list.set(i, segment);
						}
						segment.setValues(values, valueIndex);
						segment.version = pathVersion;
						valueIndex += VALUES_PER_SEGMENT;
					}
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
		if (size > 0)
			fetch(0, size);
	}

	public Segment get(int index) {
		// as fetching doesn't cost so much but calling JNI functions does,
		// fetch a few elements in the neighborhood at a time:
		int fromIndex = index - 2;
		if (fromIndex < 0)
			fromIndex = 0;

		int toIndex = fromIndex + 4;
		if (toIndex > size)
			toIndex = size;
		fetch(fromIndex, toIndex);
		return list.get(index);
	}

	public Segment add(int index, Segment segment) {
		// Copy it if it comes from another list:
		if (segment.segments != null)
			segment = new Segment(segment);
		// Add to internal structure
		list.add(index, segment);
		// Update version:
		if (path != null)
			segment.version = path.version;
		
		// And link segment to this list
		segment.segments = this;
		segment.index = index;
		// Increase size
		size++;
		if (curves != null)
			curves.updateSize();
		// And add to illustrator as well
		segment.insert();
		// Update Segment indices
		for (int i = index + 1; i < size; i++) {
			Segment seg = (Segment) list.get(i);
			if (seg != null)
				seg.index = i;
		}
		return segment;
	}
	
	/**
	 * Adds a segment to the SegmentList.
	 * @param index the index where to add the segment
	 * @param obj either a Segment or a Point
	 * @return the new segment
	 */
	public Segment add(int index, Object obj) {
		if (obj instanceof Segment) {
			return add(index, (Segment) obj);
		} else if (obj instanceof Point) {
			return add(index, new Segment((Point) obj));
		}
		return null;
	}

	public boolean addAll(int index, List elements) {
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);

		int count = elements.size();
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

		for (int i = 0; i < count; i++) {
			Object obj = elements.get(i);
			Segment segment = null;
			if (obj instanceof Segment) {
				segment = (Segment) obj;
				// copy it if it comes from another list:
				if (segment.segments != null)
					segment = new Segment(segment);
			} else if (obj instanceof Point) {
				// convert single points to a segment
				segment = new Segment((Point) obj);
			}
			if (segment != null) {
				// add to internal structure
				list.add(addIndex, segment);
				// update verion:
				segment.version = commitVersion;
				// and link segment to this list
				segment.segments = this;
				segment.index = addIndex++;
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
			SegmentList.nativeInsert(path.handle, path.document.handle, index,
					addCount, values);

			// update size
			size += addCount;
			if (curves != null)
				curves.updateSize();

			// update Segment indices
			for (int i = addIndex; i < size; i++) {
				Segment segment = (Segment) list.get(i);
				if (segment != null)
					segment.index = i;
			}

			return true;
		}

		return false;
	}

	public boolean addAll(List elements) {
		return addAll(size(), elements);
	}

	public Segment set(int index, Segment segment) {
		Segment ret = list.set(index, segment);
		segment.segments = this;
		segment.index = index;
		segment.markDirty(Segment.DIRTY_POINTS);
		if (ret != null) {
			ret.segments = null;
			ret.index = -1;
		}
		return ret;
	}

	public Segment set(int index, Object obj) {
		if (obj instanceof Segment) {
			set(index, (Segment) obj);
		} else if (obj instanceof Point) {
			set(index, new Segment((Point) obj));
		}
		return null;
	}

	public int size() {
		return size;
	}

	/**
	 * Checks whether the SegmentList is empty
	 * 
	 * @return <code>true</code> if it's empty, <code>false</code> otherwise
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	private static native int nativeRemove(int handle, int docHandle,
			int index, int count);

	public void remove(int fromIndex, int toIndex) {
		if (fromIndex < toIndex) {
			int newSize = size + fromIndex - toIndex;
			for (int i = fromIndex; i < toIndex; i++) {
				Segment seg = (Segment) list.get(i);
				if (seg != null) {
					seg.segments = null;
					seg.index = -1;
				}
			}
			if (path != null) {
				size = nativeRemove(path.handle, path.document.handle,
						fromIndex, toIndex - fromIndex);
			}
			list.remove(fromIndex, toIndex);
			size = newSize;
			if (curves != null)
				curves.updateSize();
		}

	}

	public Segment remove(int index) {
		Segment segment = get(index);
		remove(index, index + 1);
		return segment;
	}
	
	/*
	 *  postscript-like interface: moveTo, lineTo, curveTo, arcTo
	 */	
	public void moveTo(float x, float y) {
		if (size > 0)
			throw new UnsupportedOperationException(
					"moveTo can only be called at the beginning of a SegmentList");
		add(new Segment(x, y));
	}
	
	public void moveTo(Point pt) {
		moveTo(pt.x, pt.y);
	}
	
	public void lineTo(float x, float y) {
		if (size == 0)
			throw new UnsupportedOperationException("Use a moveTo command first");
		add(new Segment(x, y));
	}
	
	public void lineTo(Point pt) {
		lineTo(pt.x, pt.y);
	}
	
	public void curveTo(float c1x, float c1y, float c2x, float c2y, float x,
			float y) {
		if (size == 0)
			throw new UnsupportedOperationException("Use a moveTo command first");
		// first modify the current segment:
		Segment lastSegment = get(size - 1);
		// convert to relative values:
		lastSegment.handleOut.set(c1x - lastSegment.point.x, c1y
				- lastSegment.point.y);
		lastSegment.setCorner(false);
		// and add the new segment, with handleIn set to c2
		add(new Segment(x, y, c2x - x, c2y - y, 0, 0, false));
	}
	
	public void curveTo(Point c1, Point c2, Point pt) {
		curveTo(c1.x, c1.y, c2.x, c2.y, pt.x, pt.y);
	}
	
	public void quadTo(float cx, float cy, float x, float y) {
		// This is exact:
		// If we have the three quad poits: A E D,
		// and the cubic is A B C D,
		// B = E + 1/3 (A - E)
		// C = E + 1/3 (D - E)
		Segment segment = get(size - 1);
		float x1 = segment.point.x;
		float y1 = segment.point.y;
		curveTo(cx + (1f/3f) * (x1 - cx), cy + (1f/3f) * (y1 - cy), 
				cx + (1f/3f) * (x - cx), cy + (1f/3f) * (y - cy),
				x, y);
	}
	
	public void quadTo(Point c, Point pt) {
		quadTo(c.x, c.y, pt.x, pt.y);		
	}

	public void arcTo(float centerX, float centerY, float endX, float endY,
			int ccw) {
		if (size == 0)
			throw new UnsupportedOperationException("Use a moveTo command first");
		
		// Get the startPoint:
		Segment startSegment = (Segment) getLast();
		float startX = startSegment.point.x;
		float startY = startSegment.point.y;
		
		// Determine the width and height of the ellipse by the 3 given points
		// center, startPoint and endPoint:
		// Find the scaleFactor that scales this system horizontally so a circle
		// would fit. the resulting radius is the ellipse's height.
		// Then apply the opposite factor to the radius in order to get the width.
		
		float x1 = startX - centerX;
		float y1 = startY - centerY;
		float x2 = endX - centerX;
		float y2 = endY - centerY;
		
		double s = Math.sqrt(
			(y2 * y2 - y1 * y1) /
			(x1 * x1 - x2 * x2)
		);
		
		double h = Math.sqrt(x1 * x1 + y1 * y1);
		if (s == 0 || Double.isNaN(s) || h == 0)
			throw new UnsupportedOperationException(
					"Cannot create an arc with the given center and starting point: "
							+ centerX + ", " + centerY + "; " +
							startX + ", " + startY);
		double w = h / s;
		
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
			else out = new Point(centerX + (relx - z * rely) * w - pt.x,
					centerY + (rely + z * relx) * h - pt.y);
			if (i == 0) {
				// Modify startSegment
				startSegment.handleOut.set(out);
			} else {
				// Add new Segment
				Point in = new Point(centerX + (relx + z * rely) * w - pt.x,
						centerY + (rely - z * relx) * h - pt.y);
				add(new Segment(pt, in, out, false));
			}
			angle += inc;
		}
	}

	public void arcTo(Point center, Point endPoint, int ccw) {
		arcTo(center.x, center.y, endPoint.x, endPoint.y, ccw);
	}
}
