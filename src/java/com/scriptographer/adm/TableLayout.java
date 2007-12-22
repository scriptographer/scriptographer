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
 * File created on 08.03.2005.
 *
 * $Id:TableLayout.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer.adm;

import com.scratchdisk.util.ConversionUtils;

/**
 * @author lehni
 */
public class TableLayout extends info.clearthought.layout.TableLayout {
	
	public TableLayout(double[][] size, int hGap, int vGap) {
		super(size[0], size[1]);
		this.hGap = hGap;
		this.vGap = vGap;
	}
	
	public TableLayout(double[][] size) {
		this(size, 0, 0);
	}

	public TableLayout(Object[][] size) {
		super(getSize(size[0]), getSize(size[1]));
	}

	public TableLayout(Object[][] size, int hGap, int vGap) {
		this(size);
		this.hGap = hGap;
		this.vGap = vGap;
	}

	private static double[] getSize(Object[] objects) {
		double[] size = new double[objects.length];
		for (int i = 0; i < objects.length; i++) {
			double value;
			Object obj = objects[i];
			if (obj instanceof String) {
				if ("fill".equalsIgnoreCase((String) obj)) value = FILL;
				else if ("preferred".equalsIgnoreCase((String) obj)) value = PREFERRED; 
				else if ("minimum".equalsIgnoreCase((String) obj)) value = MINIMUM; 
				else value = Double.NaN;
			} else {
				value = ConversionUtils.toDouble(obj);
			}
			size[i] = value;
		}
		return size;
	}

	/* overrides setHGap to allow negative gaps
	 * @see info.clearthought.layout.TableLayout#setHGap(int)
	 */
	public void setHGap(int hGap) {
		this.hGap = hGap;
	}
	/* overrides setVGap to allow negative gaps
	 * @see info.clearthought.layout.TableLayout#setVGap(int)
	 */
	public void setVGap(int vGap) {
		this.vGap = vGap;
	}
	
	public void setGaps(int hGap, int vGap) {
		this.hGap = hGap;
		this.vGap = vGap;
	}
}
