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
 * File created on 14.02.2005.
 *
 * $RCSfile: FillStyle.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:01:01 $
 */

package com.scriptographer.ai;

public class FillStyle {
	protected Color color; 					/* Fill color */
	protected boolean overprint;			/* Overprint */

	private PathStyle style = null;

	protected FillStyle(PathStyle style) {
		this.style = style;
	}

	protected FillStyle(FillStyle fill, PathStyle style) {
		this(fill);
		this.style = style;
	}

	public FillStyle(FillStyle fill) {
		init(fill.color, fill.overprint);
	}

	public FillStyle(Color color, boolean overprint) {
		init(color, overprint);
	}

	protected void setStyle(PathStyle style) {
		this.style = style;
	}

	protected void init(Color color, boolean overprint) {
		this.color = color;
		this.overprint = overprint;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
		if (style != null)
			style.markDirty();
	}

	// TODO: convert through getColorComponents instead!
	public void setColor(java.awt.Color color) {
		setColor(new RGBColor(color));
	}

	public boolean getOverprint() {
		return overprint;
	}

	public void setOverprint(boolean overprint) {
		this.overprint = overprint;
		if (style != null)
			style.markDirty();
	}
}
