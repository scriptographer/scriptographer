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
 * File created on 26.07.2005.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

import com.scratchdisk.list.Lists;
import com.scratchdisk.list.ReadOnlyList;
import com.scriptographer.ScriptographerException;

/**
 * A compound path contains two or more paths, holes are drawn where the paths
 * overlap. All the paths in a compound path take on the style of the backmost
 * path.
 * 
 * @author lehni
 */
public class CompoundPath extends PathItem {
	/**
	 * Wraps an AIArtHandle in a Path object
	 */
	protected CompoundPath(int handle, int docHandle, boolean created) {
		super(handle, docHandle, created);
	}

	/**
	 * Creates a compound path item.
	 */
	public CompoundPath() {
		super(TYPE_COMPOUNDPATH);
	}

	public CompoundPath(ReadOnlyList<? extends Item> children) {
		this();
		for (Item item : children)
			this.appendBottom(item);
	}
	
	public CompoundPath(Item[] children) {
		this(Lists.asList(children));
	}
	
	/**
	 * @jshide
	 */
	public CompoundPath(Shape shape) {
		this();
		append(shape);
	}

	/**
	 * Specifies whether the compound path is used as a guide.
	 * @return {@true if the compound path is used as a guide}
	 */
	public boolean isGuide() {
		Item child = getFirstChild();
		return child != null && ((Path) child).isGuide();
	}
	
	public void setGuide(boolean guide) {
		for (Item item : getChildren())
			((Path) item).setGuide(guide);
	}

	private Path getPreviousPath() {
		Path prevPath = (Path) getFirstChild();
		if (prevPath == null)
			throw new ScriptographerException("Use a moveTo command first");
		return prevPath;
	}

	/*
	 *  PostScript-like interface: moveTo, lineTo, curveTo, arcTo
	 */
	public void moveTo(double x, double y) {
		// moveTo always creates a new path:
		Path path = new Path();
		appendTop(path);
		path.moveTo(x, y);
	}
	
	public void lineTo(double x, double y) {
		getPreviousPath().lineTo(x, y);
	}
	
	public void curveTo(double c1x, double c1y, double c2x, double c2y,
			double x, double y) {
		getPreviousPath().curveTo(c1x, c1y, c2x, c2y, x, y);
	}
	
	public void curveTo(double cx, double cy, double x, double y) {
		getPreviousPath().curveTo(cx, cy, x, y);
	}

	public void arcTo(double endX, double endY) {
		getPreviousPath().arcTo(endX, endY);
	}

	public void curveThrough(double centerX, double centerY, double endX, double endY, double t) {
		getPreviousPath().curveThrough(centerX, centerY, endX, endY, t);
	}

	public void arcThrough(double centerX, double centerY, double endX, double endY) {
		getPreviousPath().arcThrough(centerX, centerY, endX, endY);
	}
	
	public void closePath() {
		Path prevPath = getPreviousPath();
		prevPath.setClosed(true);
	}
	
	/*
	 * Convert to and from Java2D (java.awt.geom)
	 */

	/**
	 * Appends the segments of a PathIterator to this CompoundPath. Optionally,
	 * the initial {@link PathIterator#SEG_MOVETO}segment of the appended path
	 * is changed into a {@link PathIterator#SEG_LINETO}segment.
	 * 
	 * @param iter the PathIterator specifying which segments shall be appended
	 * @param connect {@code true} for substituting the initial
	 *        {@link PathIterator#SEG_MOVETO}segment by a
	 *        {@link PathIterator#SEG_LINETO}, or {@code false} for not
	 *        performing any substitution. If this GeneralPath is currently
	 *        empty, {@code connect} is assumed to be {@code false}, thus
	 *        leaving the initial {@link PathIterator#SEG_MOVETO}unchanged.
	 * @jshide
	 */
	public void append(PathIterator iter, boolean connect) {
		float[] f = new float[6];
		while (!iter.isDone()) {
			switch (iter.currentSegment(f)) {
				case PathIterator.SEG_MOVETO: {
					Path prevPath = (Path) getFirstChild();
					int size = prevPath != null ?
							prevPath.getSegments().size() : -1;
					if (!connect || size <= 0) {
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
					curveTo(f[0], f[1], f[2], f[3]);
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

	/**
	 * @jshide
	 */
	public GeneralPath toShape() {
		Path path = (Path) getFirstChild();
		GeneralPath shape = (GeneralPath) path.toShape();
		while ((path = (Path) path.getNextSibling()) != null) {
			shape.append(path.toShape(), false);
		}
		return shape;
	}

	/**
	 * If this is a compound path with only one path inside,
	 * the path is moved outside and the compound path is erased.
	 * Otherwise, the compound path is returned unmodified.
	 *
	 * @return the simplified compound path.
	 */
	public PathItem simplify() {
		Path path = (Path) getFirstChild();
		if (path.getNextSibling() == null) {
			path.moveAbove(this);
			this.remove();
			return path;
		}
		return this;
	}
}
