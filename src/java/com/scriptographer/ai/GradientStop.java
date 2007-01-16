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
 * File created on Oct 18, 2006.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import com.scriptographer.CommitManager;
import com.scriptographer.Commitable;
import com.scriptographer.ScriptographerException;

/**
 * @author lehni
 */
public class GradientStop implements Commitable {
	protected float midPoint;
	protected float rampPoint;
	protected Color color;
	protected int version = -1;
	protected int index = -1;
	protected GradientStopList list;
	protected boolean dirty = false;
	
	public GradientStop(GradientStop stop)
	throws ScriptographerException {
		this.init(stop.midPoint, stop.rampPoint, stop.color);
	}
	
	public GradientStop(float midPoint, float rampPoint, Color color)
	throws ScriptographerException {
		this.init(midPoint, rampPoint, color);
	}
	
	public GradientStop() {
		try {
			this.init(0, 0, new GrayColor(0));
		} catch (ScriptographerException e) {
			// never happens as color is not null
		}
	}

	protected GradientStop(GradientStopList stops, int index) {
		this.list = stops;
		this.index = index;
	}
	
	protected void init(float midPoint, float rampPoint, Color color)
	throws ScriptographerException {
		if (color == null)
			throw new ScriptographerException("Gradient color cannot be null");
		this.midPoint = midPoint;
		this.rampPoint = rampPoint;
		this.color = color;
	}

	/**
	 * inserts this gradient stop in the underlying AI gradient at position
	 * index Only call once, when adding this stop to the GradientStopList!
	 */
	protected void insert() {
		if (list != null && list.gradient != null) {
			Gradient gradient = list.gradient;
			GradientStopList.nativeInsert(
				gradient.handle, gradient.document.handle, index,
				midPoint, rampPoint, color.getComponents());
			version = CommitManager.version;
			dirty = false;
		}
	}

	protected void markDirty() {
		// only mark it as dirty if it's attached to a path already and
		// if the given dirty flag is not already set
		if (!dirty && list != null && list.gradient != null) {
			CommitManager.markDirty(list.gradient, this);
			dirty = true;
		}
	}
	
	public void commit() {
		if (dirty && list != null && list.gradient != null) {
			Gradient gradient = list.gradient;
			GradientStopList.nativeSet(
				gradient.handle, gradient.document.handle, index,
				midPoint, rampPoint, color.getComponents());
			dirty = false;
			version = CommitManager.version;
		}
	}
	
	protected void update() {
		if (!dirty && list != null && list.gradient != null &&
			version != CommitManager.version) {
			GradientStopList.nativeGet(list.gradient.handle, index, this);
			version = CommitManager.version;
		}
	}

	public Color getColor() {
		this.update();
		return color;
	}

	public void setColor(Color color) {
		if (color != null) {
			this.update();
			this.color = color;
			this.markDirty();
		}
	}

	public float getMidPoint() {
		this.update();
		return midPoint;
	}

	public void setMidPoint(float midPoint) {
		this.update();
		this.midPoint = midPoint;
		this.markDirty();
	}

	public float getRampPoint() {
		this.update();
		return rampPoint;
	}

	public void setRampPoint(float rampPoint) {
		this.update();
		this.rampPoint = rampPoint;
		this.markDirty();
	}
	
	public Gradient getGradient() {
		return this.list != null ? this.list.gradient : null;
	}
	
	public int getIndex() {
		return index;
	}
}
