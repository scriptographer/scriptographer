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
 * File created on 26.07.2005.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import java.awt.Shape;
import java.awt.geom.PathIterator;

import com.scratchdisk.util.ExtendedList;
import com.scratchdisk.util.Lists;

/**
 * A compound path contains two or more paths, holes are drawn where the paths
 * overlap. All the paths in a compound path take on the style of the backmost
 * path.
 * 
 * @author lehni
 */
public class CompoundPath extends Art {
	/**
	 * Wraps an AIArtHandle in a Path object
	 */
	protected CompoundPath(int handle) {
		super(handle);
	}

	/**
	 * Creates a compound path item
	 */
	public CompoundPath() {
		super(TYPE_COMPOUNDPATH);
	}
	
	public CompoundPath(ExtendedList children) {
		this();
		for (int i = 0; i < children.size(); i++) {
			Object obj = children.get(i);
			if (obj instanceof Art)
				this.appendChild((Art) obj);
		}
	}
	
	public CompoundPath(Art[] children) {
		this(Lists.asList(children));
	}
	
	public CompoundPath(Shape shape) {
		this();
		append(shape);
	}

	/*
	 *  postscript-like interface: moveTo, lineTo, curveTo, arcTo
	 */	
	
	public void moveTo(float x, float y) {
		// moveTo allways creates a new path:
		Path path = new Path();
		appendChild(path);
		path.moveTo(x, y);
	}
	
	public void moveTo(Point pt) {
		moveTo(pt);
	}
	
	private Path getPreviousPath() {
		Path prevPath = (Path) getFirstChild();
		if (prevPath == null)
			throw new UnsupportedOperationException("Use a moveTo command first");
		return prevPath;
	}
	
	public void lineTo(float x, float y) {
		getPreviousPath().lineTo(x, y);
	}
	
	public void lineTo(Point pt) {
		getPreviousPath().lineTo(pt);
	}
	
	public void curveTo(float c1x, float c1y, float c2x, float c2y,
			float x, float y) {
		getPreviousPath().curveTo(c1x, c1y, c2x, c2y, x, y);
	}
	
	public void curveTo(Point c1, Point c2, Point pt) {
		getPreviousPath().curveTo(c1, c2, pt);
	}
	
	public void quadTo(float cx, float cy, float x, float y) {
		getPreviousPath().quadTo(cx, cy, x, y);
	}
	
	public void quadTo(Point c, Point pt) {
		getPreviousPath().quadTo(c, pt);
	}

	public void arcTo(float centerX, float centerY, float endX, float endY,
			int ccw) {
		getPreviousPath().arcTo(centerX, centerY, endX, endY, ccw);
	}

	public void arcTo(Point center, Point endPoint, int ccw) {
		getPreviousPath().arcTo(center, endPoint, ccw);
	}
	
	public void closePath() {
		Path prevPath = getPreviousPath();
		prevPath.setClosed(true);
	}
	
	/**
	 * Appends the segments of a PathIterator to this CompoundPath. Optionally,
	 * the initial {@link PathIterator#SEG_MOVETO}segment of the appended path
	 * is changed into a {@link PathIterator#SEG_LINETO}segment.
	 * 
	 * @param iter the PathIterator specifying which segments shall be appended.
	 * @param connect <code>true</code> for substituting the initial
	 *        {@link PathIterator#SEG_MOVETO}segment by a {@link
	 *        PathIterator#SEG_LINETO}, or <code>false</code> for not
	 *        performing any substitution. If this GeneralPath is currently
	 *        empty, <code>connect</code> is assumed to be <code>false</code>,
	 *        thus leaving the initial {@link PathIterator#SEG_MOVETO}unchanged.
	 */
	public void append(PathIterator iter, boolean connect) {
		float[] f = new float[6];
		while (!iter.isDone()) {
			switch (iter.currentSegment(f)) {
				case PathIterator.SEG_MOVETO: {
					Path prevPath = (Path) getFirstChild();
					int size = prevPath != null ?
							prevPath.getSegments().size() : -1;
				    if (!connect || size  <= 0) {
						moveTo(f[0], f[1]);
						break;
					} else {
						Point pt = ((Segment) prevPath.getSegments().getLast()).point;
						if (pt.x == f[0] && pt.y == f[1])
							break;
					}
					// Fall through to lineto for connect!
				}
				case PathIterator.SEG_LINETO:
					lineTo(f[0], f[1]);
					break;
				case PathIterator.SEG_QUADTO:
					quadTo(f[0], f[1], f[2], f[3]);
					break;
				case PathIterator.SEG_CUBICTO:
					curveTo(f[0], f[1], f[2], f[3], f[4], f[5]);
					break;
				case PathIterator.SEG_CLOSE:
					closePath();
					break;
			}

			connect = false;
			iter.next();
		}
	}
	
	public void append(PathIterator iter) {
		append(iter, false);
	}

	/**
	 * Appends the segments of a Shape to the path. If <code>connect</code> is
	 * true, the new path segments are connected to the existing one with a
	 * line. The winding rule of the Shape is ignored.
	 */
	public void append(Shape shape, boolean connect) {
		append(shape.getPathIterator(null), connect);
	}

	public void append(Shape shape) {
		append(shape.getPathIterator(null), false);
	}
}
