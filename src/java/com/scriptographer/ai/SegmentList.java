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

import com.scratchdisk.list.ReadOnlyList;
import com.scriptographer.list.AbstractFetchList;

/**
 * @author lehni
 * 
 * @jshide
 */
public class SegmentList extends AbstractFetchList<Segment> {
	protected Path path;
	protected CurveList curves = null;

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
	}

	protected SegmentList(Path path) {
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
						((segment = list.get(start)) != null) &&
						segment.version == pathVersion) {
					start++;
				}

				if (start == toIndex) // all fetched, jump out
					break;

				// now determine the length of the block that needs to be
				// fetched:
				end = start + 1;

				while (end < toIndex &&
						((segment = list.get(start)) == null ||
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
						segment = list.get(i);
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
			Segment seg = list.get(i);
			if (seg != null)
				seg.index = i;
		}
		return segment;
	}

	public boolean addAll(int index, ReadOnlyList<? extends Segment> elements) {
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
				// update version:
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
				Segment segment = list.get(i);
				if (segment != null)
					segment.index = i;
			}

			return true;
		}

		return false;
	}

	public boolean addAll(ReadOnlyList<? extends Segment> elements) {
		return addAll(size, elements);
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
	
	/**
	 * Adds a point to the SegmentList.
	 * The point is converted to a segment with no handles.
	 * @param index the index where to add the segment
	 * @param obj either a Segment or a Point
	 * @return the new segment
	 */
	public Segment add(int index, Point point) {
		return add(index, new Segment((Point) point));
	}

	public Segment add(Point point) {
		return add(new Segment((Point) point));
	}

	public Segment set(int index, Point point) {
		return set(index, new Segment(point));
	}

	private static native int nativeRemove(int handle, int docHandle,
			int index, int count);

	public void remove(int fromIndex, int toIndex) {
		if (fromIndex < toIndex) {
			int newSize = size + fromIndex - toIndex;
			for (int i = fromIndex; i < toIndex; i++) {
				Segment seg = list.get(i);
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
	
	/*
	 *  postscript-like interface: moveTo, lineTo, curveTo, arcTo
	 */	
	public void moveTo(double x, double y) {
		if (size > 0)
			throw new UnsupportedOperationException(
					"moveTo can only be called at the beginning of a SegmentList");
		add(new Segment(x, y));
	}
	
	public void moveTo(Point pt) {
		moveTo(pt.x, pt.y);
	}
	
	public void lineTo(double x, double y) {
		/* Let's not be so picky about this for now 
		if (size == 0)
			throw new UnsupportedOperationException("Use a moveTo command first");
		*/
		add(new Segment(x, y));
	}
	
	public void lineTo(Point pt) {
		lineTo(pt.x, pt.y);
	}
	
	public void curveTo(double c1x, double c1y, double c2x, double c2y, double x,
			double y) {
		if (size == 0)
			throw new UnsupportedOperationException("Use a moveTo command first");
		// first modify the current segment:
		Segment lastSegment = get(size - 1);
		// convert to relative values:
		lastSegment.handleOut.set(c1x - lastSegment.point.x, c1y
				- lastSegment.point.y);
		// and add the new segment, with handleIn set to c2
		add(new Segment(x, y, c2x - x, c2y - y, 0, 0));
	}
	
	public void curveTo(Point c1, Point c2, Point pt) {
		curveTo(c1.x, c1.y, c2.x, c2.y, pt.x, pt.y);
	}
	
	public void quadTo(double cx, double cy, double x, double y) {
		// This is exact:
		// If we have the three quad points: A E D,
		// and the cubic is A B C D,
		// B = E + 1/3 (A - E)
		// C = E + 1/3 (D - E)
		Segment segment = get(size - 1);
		double x1 = segment.point.x;
		double y1 = segment.point.y;
		curveTo(cx + (1f/3f) * (x1 - cx), cy + (1f/3f) * (y1 - cy), 
				cx + (1f/3f) * (x - cx), cy + (1f/3f) * (y - cy),
				x, y);
	}
	
	public void quadTo(Point c, Point pt) {
		quadTo(c.x, c.y, pt.x, pt.y);		
	}

	public void arcTo(double middleX, double middleY, double endX, double endY) {
		if (size == 0)
			throw new UnsupportedOperationException("Use a moveTo command first");
		
		// Get the startPoint:
		Segment start = getLast();
		double x1 = start.point.x, x2 = middleX, x3 = endX;
		double y1 = start.point.y, y2 = middleY, y3 = endY;
		
		double f = x3 * x3 - x3 * x2 - x1 * x3 + x1 * x2 + y3 * y3 - y3 * y2 - y1 * y3 + y1 * y2;
		double g = x3 * y1 - x3 * y2 + x1 * y2 - x1 * y3 + x2 * y3 - x2 * y1;
		double m = g == 0 ? 0 : f / g;

		double c = (m * y2) - x2 - x1 - (m * y1);
		double d = (m * x1) - y1 - y2 - (x2 * m);
		double e = (x1 * x2) + (y1 * y2) - (m * x1 * y2) + (m * x2 * y1);

		double centerX = -c / 2;
		double centerY = -d / 2;
		double radius = Math.sqrt(centerX * centerX + centerY * centerY - e);

		// Note: reversing the Y equations negates the angle to adjust
		// for the upside down coordinate system.
		double angle = Math.atan2(centerY - y1, x1 - centerX);
		double middle = Math.atan2(centerY - y2, x2 - centerX);
		double extent = Math.atan2(centerY - y3, x3 - centerX);

		double diff = middle - angle;
		if (diff < -Math.PI)
			diff += Math.PI * 2;
		else if (diff > Math.PI)
			diff -= Math.PI * 2;

		extent -= angle;
		if (extent <= 0.0)
			extent += Math.PI * 2.0;

		if (diff < 0) extent = Math.PI * 2.0 - extent;
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
			Point pt = new Point(centerX + relx * radius, centerY + rely * radius);
			Point out;
			if (i == arcSegs) out = null;
			else out = new Point(centerX + (relx - z * rely) * radius - pt.x,
					centerY + (rely + z * relx) * radius - pt.y);
			if (i == 0) {
				// Modify startSegment
				start.handleOut.set(out);
			} else {
				// Add new Segment
				Point in = new Point(
						centerX + (relx + z * rely) * radius - pt.x,
						centerY + (rely - z * relx) * radius - pt.y);
				add(new Segment(pt, in, out));
			}
			angle += inc;
		}
	}

	public void arcTo(Point middle, Point end) {
		arcTo(middle.x, middle.y, end.x, end.y);
	}

	private Point getLineIntersection(Point p1, Point v1, Point p2, Point v2) {
		// Calculate differences
		double xD1 = v1.x;
		double yD1 = v1.y;
		double xD2 = v2.x;
		double yD2 = v2.y;
		double xD3 = p1.x - p2.x;
		double yD3 = p1.y - p2.y;
		// Calculate the lengths of the two lines
		double len1 = Math.sqrt(xD1 * xD1 + yD1 * yD1);
		double len2 = Math.sqrt(xD2 * xD2 + yD2 * yD2);
		// Calculate angle between the two lines.
		double deg = (xD1 * xD2 + yD1 * yD2) / (len1 * len2);
		// If abs(angle) == 1 then the lines are parallel,
		// so no intersection is possible
		if (Math.abs(deg) == 1)
			return null;
		// Find intersection Pt between two lines
		double ua = (xD2 * yD3 - yD2 * xD3) / (yD2 * xD1 - xD2 * yD1);
		return new Point(
				p1.x + ua * xD1,
				p1.y + ua * yD1
		);
	}  

	public void arcTo(Point end) {
		if (size == 0)
			throw new UnsupportedOperationException("Use a moveTo command first");
		Segment last = getLast();
		Point start = last.point;
		Segment previous = last.getPrevious();
		Point middle = start.add(end).divide(2);
		Point point = null;
		if (previous != null) {
			Point normal1 = previous.getCurve().getNormal(0.99);
			Point normal2 = new Point(start.y - end.y, end.x - start.x);

			// normal2 = normal2.rotate(-Math.PI / 2);
			/*
			Path path = new Path();
			path.moveTo(start);
			path.lineTo(start.add(normal1.normalize(100)));
			path = new Path();
			path.moveTo(middle);
			path.lineTo(middle.add(normal2.normalize(100)));
			*/
			Point center = getLineIntersection(start, normal1, middle, normal2);
			if (center != null) {
				double radius = center.getDistance(start);
				point = center.add(middle.subtract(center).normalize(radius));
				/*
				Document.getActiveDocument().createOval(new Rectangle(point, new Size(5, 5)));
				Document.getActiveDocument().createOval(new Rectangle(end, new Size(5, 5)));
				*/
			}
		} else {
			Point step = middle.subtract(start);
			point = middle.add(-step.y, step.x);
		}
		if (point != null)
			arcTo(point, end);
	}

	public void arcTo(double endX, double endY) {
		arcTo(new Point(endX, endY));
	}

}
