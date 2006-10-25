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
 * File created on Oct 19, 2006.
 * 
 * $RCSfile: PatternColor.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2006/10/25 02:12:50 $
 */

package com.scriptographer.ai;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class PatternColor extends Color {
	/** Reference to the AIPattern which describes instance independent
	parameters for the paint (the prototype artwork). */
	Pattern pattern;
	/** Distance to translate the [unscaled] prototype before filling */
	float shiftDistance;
	/** Angle to translate the [unscaled] prototype before filling */
	float shiftAngle;
	/** Fraction to scale the prototype before filling */
	Point scaleFactor;
	/** Angle to rotate the prototype before filling */
	float rotationAngle;
	/** Whether or not the prototype is reflected before filling */
	boolean reflect;
	/** Axis around which to reflect */
	float reflectAngle;
	/** Angle to slant the shear by */
	float shearAngle;
	/** Axis to shear with respect to */
	float shearAxis;
	/** Additional transformation arising from manipulating the path */
	Matrix matrix;
	/**
	 * Called from the native environment
	 */
	protected PatternColor(int patternHandle, float shiftDistance, float shiftAngle,
		Point scaleFactor, float rotationAngle, boolean reflect, float reflectAngle,
		float shearAngle, float shearAxis, Matrix matrix) {
		// do not use constructor bellow as we do not need to copy origin and matrix here
		this.pattern = Pattern.wrapHandle(patternHandle, null);
		this.shiftDistance = shiftDistance;
		this.shiftAngle = shiftAngle;
		this.scaleFactor = scaleFactor;
		this.rotationAngle = rotationAngle;
		this.reflect = reflect;
		this.reflectAngle = reflectAngle;
		this.shearAngle = shearAngle;
		this.shearAxis = shearAxis;
		this.matrix = matrix;
	}
	
	public PatternColor(Pattern pattern, float shiftDistance, float shiftAngle,
		Point2D scaleFactor, float rotationAngle, boolean reflect, float reflectAngle,
		float shearAngle, float shearAxis, AffineTransform matrix) {
		// use the above constructor, but copy origin and matrix
		// let's not care about the call to wrapHandle above,
		// as this is not used often
		this(pattern.handle, shiftDistance, shiftAngle, new Point(scaleFactor),
			rotationAngle, reflect, reflectAngle, shearAngle, shearAxis,
			new Matrix(matrix));
	}
	
	/**
	 * called from the native environment, to fill a native struct
	 * @param struct
	 */
	protected void set(int pointer) {
		nativeSetPattern(pointer, pattern.handle, shiftDistance, shiftAngle,
			scaleFactor, rotationAngle, reflect, reflectAngle, shearAngle,
			shearAxis, matrix);
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

	public Matrix getMatrix() {
		return matrix;
	}

	public void setMatrix(AffineTransform matrix) {
		this.matrix = new Matrix(matrix);
	}

	public Pattern getPattern() {
		return pattern;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	public boolean isReflect() {
		return reflect;
	}

	public void setReflect(boolean reflect) {
		this.reflect = reflect;
	}

	public float getReflectAngle() {
		return reflectAngle;
	}

	public void setReflectAngle(float reflectAngle) {
		this.reflectAngle = reflectAngle;
	}

	public float getRotationAngle() {
		return rotationAngle;
	}

	public void setRotationAngle(float rotationAngle) {
		this.rotationAngle = rotationAngle;
	}

	public Point getScaleFactor() {
		return scaleFactor;
	}

	public void setScaleFactor(Point2D scaleFactor) {
		this.scaleFactor = new Point(scaleFactor);
	}

	public float getShearAngle() {
		return shearAngle;
	}

	public void setShearAngle(float shearAngle) {
		this.shearAngle = shearAngle;
	}

	public float getShearAxis() {
		return shearAxis;
	}

	public void setShearAxis(float shearAxis) {
		this.shearAxis = shearAxis;
	}

	public float getShiftAngle() {
		return shiftAngle;
	}

	public void setShiftAngle(float shiftAngle) {
		this.shiftAngle = shiftAngle;
	}

	public float getShiftDistance() {
		return shiftDistance;
	}

	public void setShiftDistance(float shiftDistance) {
		this.shiftDistance = shiftDistance;
	}

}
