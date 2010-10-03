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

import com.scratchdisk.list.ExtendedArrayList;
import com.scratchdisk.list.List;
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

	/*
	 * Setting selected attribute on Groups and CompoundPaths only works when
	 * also explicitly applying the same attributes to all the children, even
	 * when using setFully selected. So override both here.
	 */
	public void setSelected(boolean selected) {
		Item child = getFirstChild();
		while (child != null) {
			child.setSelected(selected);
			child = child.getNextSibling();
		}
		super.setSelected(selected);
	}

	public void setFullySelected(boolean selected) {
		Item child = getFirstChild();
		while (child != null) {
			child.setFullySelected(selected);
			child = child.getNextSibling();
		}
		super.setFullySelected(selected);
	}

	private Path getCurrentPath() {
		Path current = (Path) getFirstChild();
		if (current == null)
			throw new ScriptographerException("Use a moveTo() command first");
		return current;
	}

	@Override
	public void moveTo(double x, double y) {
		// moveTo always creates a new path:
		Path path = new Path();
		appendTop(path);
		path.moveTo(x, y);
	}

	@Override
		public void lineTo(double x, double y) {
		getCurrentPath().lineTo(x, y);
	}
	
	@Override
	public void cubicCurveTo(double c1x, double c1y, double c2x, double c2y,
			double x, double y) {
		getCurrentPath().cubicCurveTo(c1x, c1y, c2x, c2y, x, y);
	}
	
	@Override
	public void quadraticCurveTo(double cx, double cy, double x, double y) {
		getCurrentPath().quadraticCurveTo(cx, cy, x, y);
	}

	@Override
	public void curveTo(double throughX, double throughY,
			double endX, double endY, double t) {
		getCurrentPath().curveTo(throughX, throughY, endX, endY, t);
	}

	@Override
	public void arcTo(double endX, double endY, boolean clockwise) {
		getCurrentPath().arcTo(endX, endY, clockwise);
	}

	@Override
	public void arcTo(double throughX, double throughY,
			double endX, double endY) {
		getCurrentPath().arcTo(throughX, throughY, endX, endY);
	}

	public void moveBy(double x, double y) {
		Point current = getCurrentPath().getSegments().getCurrentSegment().point;
		moveTo(current.add(x, y));
	}

	public void moveBy(Point pt) {
		if (pt == null) moveTo(0, 0);
		else moveBy(pt.x, pt.y);
	}

	@Override
	public void lineBy(double x, double y) {
		getCurrentPath().lineBy(x, y);
	}

	@Override
	public void curveBy(double throughX, double throughY,
			double endX, double endY, double t) {
		getCurrentPath().curveBy(throughX, throughY, endX, endY, t);
	}

	@Override
	public void arcBy(double endX, double endY, boolean clockwise) {
		getCurrentPath().arcBy(endX, endY, clockwise);
	}

	@Override
	public void arcBy(double throughX, double throughY, double endX, double endY) {
		getCurrentPath().arcBy(throughX, throughY, endX, endY);
	}

	@Override
	public void closePath() {
		Path prevPath = getCurrentPath();
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
	@Override
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
					quadraticCurveTo(f[0], f[1], f[2], f[3]);
					break;
				case PathIterator.SEG_CUBICTO:
					cubicCurveTo(f[0], f[1], f[2], f[3], f[4], f[5]);
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
	@Override
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

	protected List<Curve> getAllCurves() {
		Item child = getFirstChild();
		ExtendedArrayList<Curve> curves = new ExtendedArrayList<Curve>();
		while (child != null) {
			curves.addAll(((Path )child).getCurves());
			child = child.getNextSibling();
		}
		return curves;
	}
}
