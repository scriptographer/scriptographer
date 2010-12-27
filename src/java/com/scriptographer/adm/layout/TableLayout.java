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
 * File created on 08.03.2005.
 */

package com.scriptographer.adm.layout;

import com.scratchdisk.util.ConversionUtils;

/**
 * @author lehni
 */
public class TableLayout extends info.clearthought.layout.TableLayout {

	public TableLayout(double[][] sizes, int hgap, int vgap) {
		super(sizes[0], sizes[1]);
		this.hGap = hgap;
		this.vGap = vgap;
	}

	public TableLayout(double[][] sizes) {
		this(sizes, 0, 0);
	}

	public TableLayout(Object[] columns, Object[] rows, int hgap, int vgap) {
		super(getSize(columns), getSize(rows));
		this.hGap = hgap;
		this.vGap = vgap;
	}

	public TableLayout(Object[] col, Object[] row) {
		this(col, row, 0, 0);
	}

	public TableLayout(String columns, String rows, int hgap, int vgap) {
		super(getSize(columns), getSize(rows));
		this.hGap = hgap;
		this.vGap = vgap;
	}

	public TableLayout(String columns, String rows) {
		this(columns, rows, 0, 0);
	}

	private static double[] getSize(Object[] array) {
		double[] size = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			double value;
			Object obj = array[i];
			if (obj instanceof String) {
				String str = (String) obj;
				if ("fill".equalsIgnoreCase(str)) value = FILL;
				else if ("preferred".equalsIgnoreCase(str)) value = PREFERRED; 
				else if ("minimum".equalsIgnoreCase(str)) value = MINIMUM; 
				else value = ConversionUtils.toDouble(obj);
			} else {
				value = ConversionUtils.toDouble(obj);
			}
			size[i] = value;
		}
		return size;
	}

	private static double[] getSize(String str) {
		return getSize(str.split("\\s"));
	}

	/* overrides setHGap to allow negative gaps
	 * @see info.clearthought.layout.TableLayout#setHGap(int)
	 */
	public void setHgap(int hgap) {
		this.hGap = hgap;
	}
	/* overrides setVGap to allow negative gaps
	 * @see info.clearthought.layout.TableLayout#setVGap(int)
	 */
	public void setVgap(int vgap) {
		this.vGap = vgap;
	}
	
	public void setGaps(int hgap, int vgap) {
		this.hGap = hgap;
		this.vGap = vgap;
	}
}
