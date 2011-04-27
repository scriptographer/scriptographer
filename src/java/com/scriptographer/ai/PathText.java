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
 * File created on 23.10.2005.
 */

package com.scriptographer.ai;

/**
 * A PathText item represents a path in an Illustrator document which has text
 * running along it.
 * 
 * @author lehni
 */
public class PathText extends TextItem {

	protected PathText(int handle, int docHandle, boolean created) {
		super(handle, docHandle, created);
	}

	native private static int nativeCreate(int orientation, int artHandle);

	/**
	 * Creates a path text item.
	 * 
	 * Sample code:
	 * <code>
	 * var path = new Path.Circle(new Point(150, 150), 40);
	 * var text = new PathText(path);
	 * text.content = 'Some text running along a circle';
	 * </code>
	 * 
	 * @param path the path that the text will run along
	 * @param orient the text orientation {@default 'horizontal'}
	 */
	public PathText(Path path, TextOrientation orientation) {
		super(nativeCreate(orientation != null
				? orientation.value : TextOrientation.HORIZONTAL.value,
				path != null ? path.handle : 0));
		// TODO: check what exactly do startT endT vs start anchor!
	}

	public PathText(Path path) {
		this(path, TextOrientation.HORIZONTAL);
	}

	public Path getTextPath() {
		return (Path) getFirstChild();
	}

	private Double getOffset(int index) {
		double[] offsets = nativeGetPathOffsets();
		double param = offsets[index];
		int segment = (int) param;
		param -= segment;
		Path path = getTextPath();
		CurveList curves = path.getCurves();
		if (segment == curves.size() && param == 0.0) {
			segment--;
			param = 1;
		}
		return path.getOffset(new CurveLocation(path, segment, param));
	}

	private void setOffset(int index, double offset) {
		double[] offsets = nativeGetPathOffsets();
		// Convert offset length to index.parameter value, as required by
		// native path offset code.
		Path path = getTextPath();
		CurveLocation loc = path.getLocation(offset);
		double param;
		if (loc != null) {
			param = loc.getIndex() + loc.getParameter();
		} else {
			param = index == 0 ? 0 : path.getLength();
		}
		offsets[index] = param;
		nativeSetPathOffsets(offsets[0], offsets[1]);
	}

	public Double getStartOffset() {
		return getOffset(0);
	}

	public void setStartOffset(double offset) {
		setOffset(0, offset);
	}

	public Double getEndOffset() {
		return getOffset(1);
	}

	public void setEndOffset(double offset) {
		setOffset(1, offset);
	}

	
	private native double[] nativeGetPathOffsets();
	
	private native void nativeSetPathOffsets(double start, double end);
}
