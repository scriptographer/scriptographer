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
 * File created on 26.07.2005.
 */

package com.scriptographer.ai;

import java.awt.geom.GeneralPath;

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
			double toX, double toY, double parameter) {
		getCurrentPath().curveTo(throughX, throughY, toX, toY, parameter);
	}

	@Override
	public void arcTo(double x, double y, boolean clockwise) {
		getCurrentPath().arcTo(x, y, clockwise);
	}

	@Override
	public void arcTo(double throughX, double throughY,
			double toX, double toY) {
		getCurrentPath().arcTo(throughX, throughY, toX, toY);
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
			double toX, double toY, double parameter) {
		getCurrentPath().curveBy(throughX, throughY, toX, toY, parameter);
	}

	@Override
	public void arcBy(double x, double y, boolean clockwise) {
		getCurrentPath().arcBy(x, y, clockwise);
	}

	@Override
	public void arcBy(double throughX, double throughY, double toX, double toY) {
		getCurrentPath().arcBy(throughX, throughY, toX, toY);
	}

	@Override
	public void closePath() {
		Path prevPath = getCurrentPath();
		prevPath.setClosed(true);
	}

	/**
	 * Converts to a Java2D shape.
	 * 
	 * @jshide
	 */
	@Override
	public GeneralPath toShape() {
		Path path = (Path) getFirstChild();
		GeneralPath shape = path.toShape();
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
