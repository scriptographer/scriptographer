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
 * File created on Oct 18, 2006.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import com.scriptographer.list.AbstractStructList;


/**
 * @author lehni
 */
public class GradientStop extends AbstractStructList.Entry<Gradient> {
	protected Color color;
	protected double rampPoint;
	protected double midPoint;
	
	/**
	 * Creates a new GradientStop object.
	 * 
	 * @param color the color of the stop {@default new GrayColor(0)}
	 * @param rampPoint the position of the stop on the gradient ramp {@default 0}
	 * @param midPoint the position where the color of the stop blends equally
	 *        with the color of the next stop. {@default 0.5}
	 */
	public GradientStop(Color color, double rampPoint, double midPoint) {
		if (color == null)
			throw new IllegalArgumentException("Gradient color cannot be null");
		this.midPoint = midPoint;
		this.rampPoint = rampPoint;
		this.color = color;
	}

	public GradientStop(Color color, double rampPoint) {
		this(color, rampPoint, 0.5);
	}

	public GradientStop(Color color) {
		this(color, 0, 0.5);
	}

	public GradientStop() {
		this(new GrayColor(0), 0, 0.5);
	}
	
	public GradientStop(GradientStop stop) {
		this(stop.color, stop.midPoint, stop.rampPoint);
	}

	protected GradientStop(Gradient gradient, int index) {
		super(gradient, index);
	}
	
	/**
	 * Called from the native side
	 */
	protected void set(double midPoint, double rampPoint, Color color) {
		// Scale native values from 0 .. 100 to 0 .. 1
		this.midPoint = midPoint / 100;
		this.rampPoint = rampPoint / 100;
		this.color = color;
	}
	
	protected boolean nativeGet() {
		return GradientStopList.nativeGet(reference.handle, index, this);
	}

	protected boolean nativeInsert() {
		return GradientStopList.nativeInsert(
				reference.handle, reference.document.handle, index,
				midPoint * 100, rampPoint * 100, color.getComponents());
	}
	
	protected boolean nativeSet() {
		// Scale values back from 0 .. 1 to 0 .. 100
		return GradientStopList.nativeSet(
				reference.handle, reference.document.handle, index,
				midPoint * 100, rampPoint * 100, color.getComponents());
	}

	/**
	 * The color of the gradient stop.
	 */
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

	/**
	 * The midpoint of the gradient stop describes the position where the color
	 * of the stop blends equally with the color of the next gradient stop.
	 * @return the midpoint of the gradient as a value between 0 and 1
	 */
	public double getMidPoint() {
		this.update();
		return midPoint;
	}

	public void setMidPoint(double midPoint) {
		this.update();
		this.midPoint = midPoint;
		this.markDirty();
	}

	/**
	 * The position of the gradient stop on the gradient ramp.
	 * @return a value between 0 and 1
	 */
	public double getRampPoint() {
		this.update();
		return rampPoint;
	}

	public void setRampPoint(double rampPoint) {
		this.update();
		// Ask Adobe why they choose funny limits like these.
		// We don't know, but they did:
		if (rampPoint < 0.13f)
			rampPoint = 0.13f;
		else if (rampPoint > 0.87f)
			rampPoint = 0.87f;
		this.rampPoint = rampPoint;
		this.markDirty();
	}
	
	/**
	 * The {@link Gradient} that this gradient stop belongs to.
	 */
	public Gradient getGradient() {
		return reference;
	}
	
	/**
	 * The index of the gradient stop in the {@link Gradient} it belongs to.
	 */
	public int getIndex() {
		return index;
	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof GradientStop) {
			GradientStop stop = (GradientStop) obj;
			return color.equals(stop.color)
					&& rampPoint == stop.rampPoint
					&& midPoint == stop.midPoint;
		}
		return false;
	}
}
