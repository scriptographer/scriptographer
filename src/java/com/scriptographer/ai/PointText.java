/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2004-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 23.10.2005.
 * 
 * $RCSfile: PointText.java,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2006/01/03 05:38:03 $
 */

package com.scriptographer.ai;

import java.awt.geom.Point2D;

public class PointText extends TextFrame {

	protected PointText(long handle, Document document) {
		super(handle, document);
	}
	
	/**
	 * Creates a point text object
	 */

	native private static long nativeCreate(int docHandle, int orient, float x, float y);

	public PointText(Document document, Point2D point, int orient) {
		this(nativeCreate(document != null ? document.handle : 0, orient, (float) point.getX(), (float) point.getY()), document);
	}

	public PointText(Document document, Point2D point) {
		this(document, point, ORIENTATION_HORIZONTAL);
	}

	public PointText(Point2D point, int orient) {
		this(null, point, orient);
	}

	public PointText(Point2D point) {
		this(null, point, ORIENTATION_HORIZONTAL);
	}

	// read only. AITransformArt suite can be used to change a kPointTextType's anchor.
	public native Point getAnchor();
}
