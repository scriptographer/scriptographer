/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile: TableLayout.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/03/10 22:52:30 $
 */

package com.scriptographer.adm;

public class TableLayout extends info.clearthought.layout.TableLayout {
	public TableLayout(double[] col, double[] row, int hGap, int vGap) {
		super(col, row);
	}

	public TableLayout(double[][] size, int hGap, int vGap) {
		this(size[0], size[1], hGap, vGap);
	}
	
	public TableLayout(double[] col, double[] row) {
		this(col, row, 0, 0);
	}
	
	public TableLayout(double[][] size) {
		this(size, 0, 0);
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
