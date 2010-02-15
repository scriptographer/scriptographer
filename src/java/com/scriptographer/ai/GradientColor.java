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

/**
 * @author lehni
 */
public class GradientColor extends Color {
	Gradient gradient;
	Point origin;
	Point destination;
	/*
	 * For radial gradients only
	 */
	Point hilite;
	/*
	 * The accumulated transformations of the gradient. It is not necessarily the same
	 * as the transformation matrix of the object containing the gradient.
	 * When a gradient is first applied to an object, the value is set to the
	 * identity matrix. When the user transforms the object, the user
	 * transformation matrix is concatenated to the gradient instance's matrix.
	 */
	Matrix matrix;
	
	/**
	 * Called from the native environment
	 */
	protected GradientColor(int gradientHandle, Point origin, double angle,
		double length, Matrix matrix, double hiliteAngle, double hiliteLength) {
		gradient = Gradient.wrapHandle(gradientHandle, null);
		this.origin = origin;
		angle = angle * Math.PI / 180.0;
		destination = new Point(
				origin.x + Math.cos(angle) * length,
				origin.y + Math.sin(angle) * length
		);
		// Hilite angle is relative to gradient angle
		hiliteAngle = angle + hiliteAngle * Math.PI / 180.0;
		// Hilite length is a factor of the total length.
		// See #set
		hiliteLength *= length;
		hilite = new Point(
				origin.x + Math.cos(hiliteAngle) * hiliteLength,
				origin.y + Math.sin(hiliteAngle) * hiliteLength
		);
		this.matrix = matrix;
	}
	
	/**
	 * Creates a GradientColor object.
	 * 
	 * Sample code:
	 * <code>
	 * // a radial gradient from white to black
	 * var gradient = new Gradient() {
	 * 	type: 'radial',
	 * 	stops: [
	 * 		new GradientStop(new GrayColor(0), 0),
	 * 		new GradientStop(new GrayColor(1), 1)
	 * 	]
	 * };
	 * 
	 * var origin = new Point(0, 0);
	 * var destination = new Point(0, 100);
	 * var gradientColor = new GradientColor(gradient, origin, destination);
	 * 
	 * // create a circle filled with the gradient color
	 * var circle = new Path.Circle(new Point(0, 0), 100) {
	 * 	fillColor: gradientColor
	 * };
	 * </code>
	 * 
	 * @param gradient the gradient
	 * @param origin the origin point
	 * @param destination the destination point
	 * @param hilite the hilite point (only for radial gradients)
	 * @param matrix the tranformation matrix
	 */
	public GradientColor(Gradient gradient, Point origin, Point destination, Point hilite, Matrix matrix) {
		this.gradient = gradient;
		this.origin = new Point(origin);
		this.destination = new Point(destination);
		this.hilite = new Point(hilite != null ? hilite : origin);
		this.matrix = new Matrix(matrix);
	}

	public GradientColor(Gradient gradient, Point origin, Point destination, Point hilite) {
		this(gradient, origin, destination, hilite, null);
	}

	public GradientColor(Gradient gradient, Point origin, Point destination) {
		this(gradient, origin, destination, null);
	}

	/**
	 * Called from the native environment, to fill a native struct. This is the opposite
	 * of the above constructor that 's called from the native side only.
	 * @param struct
	 */
	protected void set(int pointer) {
		// Convert to relative vectors
		Point destination = this.destination.subtract(origin);
		Point hilite = this.hilite.subtract(origin);
		// Calculating hilite is a bit tricky: It's length is a factor
		// of the total length of the gradient, between 0 and 1, and its
		// angle is relative to the gradient's angle. This does the trick:
		double length = destination.getLength();
		double angle = destination.getAngle() * 180.0 / Math.PI;
		// Divide by length to scale to range between 0 and 1:
		double hiliteLength = hilite.getLength() / length;
		// Subtract angle to get absolute angle:
		double hiliteAngle = hilite.getAngle() * 180.0 / Math.PI - angle;
		// Make sure we're not above 1, since that's the maximum allowed value for hilite
		if (hiliteLength > 1)
			hiliteLength = 1;
		nativeSetGradient(pointer, gradient.handle, origin, angle, length,
				matrix, hiliteAngle, hiliteLength);
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof GradientColor) {
			GradientColor color = (GradientColor) obj;
			return gradient.equals(color.gradient)
				&& origin.equals(color.origin)
				&& destination.equals(color.destination)
				&& hilite.equals(color.hilite)
				&& gradient.equals(color.gradient);
		}
		return false;
	}

	/**
	 * @jshide
	 */
	public float[] getComponents() {
		throw new UnsupportedOperationException("Cannot convert gradient to components");
	}

	public java.awt.Color toAWTColor() {
		throw new UnsupportedOperationException("Cannot convert gradient to AWT color");
	}
	
	public void setAlpha(float alhpa) {
		throw new UnsupportedOperationException("Cannot set alpha on gradient");
	}

	/**
	 * The origin point of the gradient.
	 */
	public Point getOrigin() {
		return origin;
	}

	public void setOrigin(Point origin) {
		this.origin = new Point(origin);
	}

	/**
	 * The destination point of the gradient.
	 */
	public Point getDestination() {
		return destination;
	}

	public void setDestination(Point destination) {
		this.destination = destination;
	}

	public Gradient getGradient() {
		return gradient;
	}

	public void setGradient(Gradient gradient) {
		this.gradient = gradient;
	}

	/**
	 * The hilite of the gradient. The hilite is only visible in radial
	 * gradients and allows you to move the center of the gradient while leaving
	 * the boundaries of the gradient alone.
	 */
	public Point getHilite() {
		return hilite;
	}

	public void setHilite(Point hilite) {
		this.hilite = hilite;
	}

	public void set(Color color) {
		throw new UnsupportedOperationException();
	}

	public Matrix getMatrix() {
		return matrix;
	}

	public void setMatrix(Matrix matrix) {
		this.matrix = new Matrix(matrix);
	}
}