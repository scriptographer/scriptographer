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
 * File created on 14.12.2004.
 */

package com.scriptographer.ai;

import com.scriptographer.list.AbstractFetchList;

/**
 * @author lehni
 * 
 * @jshide
 */
public class CurveList extends AbstractFetchList<Curve> {
	protected Path path;
	protected SegmentList segments;

	protected CurveList(Path path, SegmentList segments) {
		this.path = path;
		this.segments = segments;
		segments.curves = this;
		updateSize();
	}

	public Class<Curve> getComponentType() {
		return Curve.class;
	}

	public Path getPath() {
		return path;
	}

	/**
	 * updateSize is called from the linked SegmentList on size changes.
	 */
	protected void updateSize() {
		int newSize = segments.size();
		// Reduce length by one if it's an open path:
		if (!path.isClosed() && newSize > 0)
			newSize--;
		
		if (size != newSize) {
			list.setSize(newSize);
			size = newSize;
		}
	}

	/**
	 * updateIndex is called from the linked SegmentList on index changes.
	 */
	protected void updateIndex(int index) {
		// Only update curve index if curve is created already
		if (index < size) {
			Curve curve = list.get(index);
			if (curve != null)
				curve.setIndex(index);
		}
	}

	protected void fetch(int fromIndex, int toIndex) {
		// Prefetch all the needed segments now:
		segments.fetch(fromIndex, Math.min(segments.size(), toIndex + 1));
	}

	// This list is read only for now.
	// TODO: Implement?
	/**
	 * @jshide
	 */
	public Curve add(int index, Curve element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @jshide
	 */
	public Curve set(int index, Curve element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @jshide
	 */
	public void remove(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	public Curve get(int index) {
		Curve curve = list.get(index);
		if (curve == null) {
			curve = new Curve(segments, index);
			list.set(index, curve);
		} else {
			curve.updateSegments();
		}
		return curve;
	}
}
