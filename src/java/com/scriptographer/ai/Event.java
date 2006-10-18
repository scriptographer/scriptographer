/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 21.12.2004.
 *
 * $RCSfile: Event.java,v $
 * $Author: lehni $
 * $Revision: 1.5 $
 * $Date: 2006/10/18 14:17:43 $
 */

package com.scriptographer.ai;

import com.scriptographer.js.WrappableObject;

public class Event extends WrappableObject {
	private Point point;
	private double pressure;
	
	protected Event() {
		point = new Point();
	}
	
	protected void setValues(float x, float y, int pressure) {
		point.setLocation(x, y);
		this.pressure = pressure / 255.0;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(16);
		buf.append("{ point: ").append(point.toString());
		buf.append(", pressure: ").append(pressure);
		buf.append(" }");
		return buf.toString();
	}

	public Point getPoint() {
		return new Point(point);
	}

	public double getPressure() {
		return pressure;
	}
}
