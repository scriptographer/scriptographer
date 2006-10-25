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
 * File created on Oct 18, 2006.
 * 
 * $RCSfile: GradientColor.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2006/10/25 02:12:50 $
 */

package com.scriptographer.ai;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class GradientColor extends Color {
	Gradient gradient;
	Point origin;
	float angle;
	float length;
	Matrix matrix;
	float hiliteAngle;
	float hiliteLength;
	
	/**
	 * Called from the native environment
	 */
	protected GradientColor(int gradientHandle, Point origin, float angle,
		float length, Matrix matrix, float hiliteAngle, float hiliteLength) {
		this.gradient = Gradient.wrapHandle(gradientHandle, null);
		this.origin = origin;
		this.angle = angle;
		this.length = length;
		this.matrix = matrix;
		this.hiliteAngle = hiliteAngle;
		this.hiliteLength = hiliteLength;
	}
	
	public GradientColor(Gradient gradient, Point2D origin, float angle,
		float length, AffineTransform matrix, float hiliteAngle, float hiliteLength) {
		// use the above constructor, but copy origin and matrix
		// let's not care about the call to wrapHandle above,
		// as this is not used often
		this(gradient.handle, new Point(origin), angle, length,
			new Matrix(matrix), hiliteAngle, hiliteLength);
	}
	
	/**
	 * called from the native environment, to fill a native struct
	 * @param struct
	 */
	protected void set(int pointer) {
		nativeSetGradient(pointer, gradient.handle, origin, angle, length, matrix,
			hiliteAngle, hiliteLength);
	}

	public boolean equals(Object obj) {
		// TODO: Implement!
		return obj == this;
	}

	public float[] getComponents() {
		throw new UnsupportedOperationException();
	}

	public java.awt.Color toAWTColor() {
		throw new UnsupportedOperationException();
	}
	
	public void setAlpha(float alhpa) {
		throw new UnsupportedOperationException();
	}

	public float getAngle() {
		return angle;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public Gradient getGradient() {
		return gradient;
	}

	public void setGradient(Gradient gradient) {
		this.gradient = gradient;
	}

	public float getHiliteAngle() {
		return hiliteAngle;
	}

	public void setHiliteAngle(float hiliteAngle) {
		this.hiliteAngle = hiliteAngle;
	}

	public float getHiliteLength() {
		return hiliteLength;
	}

	public void setHiliteLength(float hiliteLength) {
		this.hiliteLength = hiliteLength;
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}

	public Matrix getMatrix() {
		return matrix;
	}

	public void setMatrix(AffineTransform matrix) {
		this.matrix = new Matrix(matrix);
	}

	public Point getOrigin() {
		return origin;
	}

	public void setOrigin(Point2D origin) {
		this.origin = new Point(origin);
	}
}