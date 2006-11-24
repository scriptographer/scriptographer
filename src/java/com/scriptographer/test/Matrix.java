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
 * File created on 20.12.2004.
 *
 * $RCSfile: Matrix.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2006/11/24 23:39:40 $
 */

package com.scriptographer.test;

import java.awt.geom.*;

import org.mozilla.javascript.Scriptable;

import com.scriptographer.js.Wrappable;
import com.scriptographer.ai.Point;

/**
 * This class represents an affine transformation between two coordinate
 * spaces in 2 dimensions. Such a transform preserves the "straightness"
 * and "parallelness" of lines. The transform is built from a sequence of
 * translations, scales, flips, rotations, and shears.
 *
 * <p>The transformation can be represented using matrix math on a 3x3 array.
 * Given (x,y), the transformation (x',y') can be found by:
 * <pre>
 * [ x']   [ m00 m01 m02 ] [ x ]   [ m00*x + m01*y + m02 ]
 * [ y'] = [ m10 m11 m12 ] [ y ] = [ m10*x + m11*y + m12 ]
 * [ 1 ]   [  0   0   1  ] [ 1 ]   [          1          ]
 * </pre>
 * The bottom row of the matrix is constant, so a transform can be uniquely
 * represented (as in {@link #toString()}) by
 * "[[m00, m01, m02], [m10, m11, m12]]".
 */
public class Matrix extends AffineTransform implements Wrappable {

	public Matrix() {
	}

	/**
	 * Create a new transform which copies the given one.
	 *
	 * @param at the transform to copy
	 * @throws NullPointerException if tx is null
	 */
	public Matrix(AffineTransform at) {
		super(at);
	}

	/**
	 * Construct a transform with the given matrix entries:
	 *
	 * <pre>
	 *  [ m00 m01 m02 ]
	 *  [ m10 m11 m12 ]
	 *  [  0   0   1  ]
	 * </pre>
	 *
	 * @param scaleX the x scaling component
	 * @param shearY the y shearing component
	 * @param shearX the x shearing component
	 * @param scaleY the y scaling component
	 * @param translateX the x translation component
	 * @param translateY the y translation component
	 */
	public Matrix(double scaleX, double shearY, double shearX, double scaleY, double translateX, double translateY) {
		super(scaleX, shearY, shearX, scaleY, translateX, translateY);
	}

	/**
	 * Construct a transform from a sequence of float entries. The array must
	 * have at least 4 entries, which has a translation factor of 0; or 6
	 * entries, for specifying all parameters:
	 * <pre>
	 * [ f[0] f[2] (f[4]) ]
	 * [ f[1] f[3] (f[5]) ]
	 * [  0     0    1    ]
	 * </pre>
	 *
	 * @param values the matrix to copy from, with at least 4 (6) entries
	 * @throws NullPointerException if f is null
	 * @throws ArrayIndexOutOfBoundsException if f is too small
	 */
	public Matrix(double[] values) {
		super(values);
	}

	public Object clone() {
		return new Matrix((AffineTransform) this);
	}

	public AffineTransform createInverse() throws NoninvertibleTransformException {
		return new Matrix(super.createInverse());
	}

	  /**
	   * Returns a rotation transform. A positive angle (in radians) rotates
	   * the positive x-axis to the positive y-axis:
	   * <pre>
	   * [ cos(theta) -sin(theta) 0 ]
	   * [ sin(theta)  cos(theta) 0 ]
	   * [     0           0      1 ]
	   * </pre>
	   *
	   * @param theta the rotation angle
	   * @return the rotating transform
	   */
	public static AffineTransform getRotateInstance(double theta) {
		Matrix m = new Matrix();
		m.setToRotation(theta);
		return m;
	}

	  /**
	   * Returns a rotation transform about a point. A positive angle (in radians)
	   * rotates the positive x-axis to the positive y-axis. This is the same
	   * as calling:
	   * <pre>
	   * AffineTransform tx = new AffineTransform();
	   * tx.setToTranslation(x, y);
	   * tx.rotate(theta);
	   * tx.translate(-x, -y);
	   * </pre>
	   *
	   * <p>The resulting matrix is:
	   * <pre>
	   * [ cos(theta) -sin(theta) x-x*cos+y*sin ]
	   * [ sin(theta)  cos(theta) y-x*sin-y*cos ]
	   * [     0           0            1       ]
	   * </pre>
	   *
	   * @param theta the rotation angle
	   * @param x the x coordinate of the pivot point
	   * @param y the y coordinate of the pivot point
	   * @return the rotating transform
	   */
	public static AffineTransform getRotateInstance(double theta, double x, double y) {
		Matrix m = new Matrix();
		m.setToRotation(theta, x, y);
		return m;
	}

	public static AffineTransform getRotateInstance(double theta, Point2D center) {
		Matrix m = new Matrix();
		m.setToRotation(theta, center.getX(), center.getY());
		return m;
	}

	  /**
	   * Returns a scaling transform:
	   * <pre>
	   * [ sx 0  0 ]
	   * [ 0  sy 0 ]
	   * [ 0  0  1 ]
	   * </pre>
	   *
	   * @param sx the x scaling factor
	   * @param sy the y scaling factor
	   * @return the scaling transform
	   */
	public static AffineTransform getScaleInstance(double sx, double sy) {
		Matrix m = new Matrix();
		m.setToScale(sx, sy);
		return m;
	}

	public static AffineTransform getScaleInstance(double scale) {
		Matrix m = new Matrix();
		m.setToScale(scale, scale);
		return m;
	}

	  /**
	   * Returns a shearing transform (points are shifted in the x direction based
	   * on a factor of their y coordinate, and in the y direction as a factor of
	   * their x coordinate):
	   * <pre>
	   * [  1  shx 0 ]
	   * [ shy  1  0 ]
	   * [  0   0  1 ]
	   * </pre>
	   *
	   * @param shx the x shearing factor
	   * @param shy the y shearing factor
	   * @return the shearing transform
	   */
	public static AffineTransform getShearInstance(double shx, double shy) {
		Matrix m = new Matrix();
		m.setToShear(shx, shx);
		return m;
	}

	  /**
	   * Returns a translation transform:
	   * <pre>
	   * [ 1 0 tx ]
	   * [ 0 1 ty ]
	   * [ 0 0 1  ]
	   * </pre>
	   *
	   * @param tx the x translation distance
	   * @param ty the y translation distance
	   * @return the translating transform
	   */
	public static AffineTransform getTranslateInstance(double tx, double ty) {
		Matrix m = new Matrix();
		m.setToTranslation(tx, tx);
		return m;
	}

	public static AffineTransform getTranslateInstance(Point2D pt) {
		Matrix m = new Matrix();
		m.setToTranslation(pt.getX(), pt.getY());
		return m;
	}

	public void setScaleX(double scaleX) {
		setTransform(scaleX, getShearY(), getShearX(), getScaleY(), getTranslateX(), getTranslateY());
	}

	public void setScaleY(double scaleY) {
		setTransform(getScaleX(), getShearY(), getShearX(), scaleY, getTranslateX(), getTranslateY());
	}

	public void setShearX(double shearX) {
		setTransform(getScaleX(), getShearY(), shearX, getScaleY(), getTranslateX(), getTranslateY());
	}

	public void setShearY(double shearY) {
		setTransform(getScaleX(), shearY, getShearX(), getScaleY(), getTranslateX(), getTranslateY());
	}

	public void setTranslateX(double translateX) {
		setTransform(getScaleX(), getShearY(), getShearX(), getScaleY(), translateX, getTranslateY());
	}

	public void setTranslateY(double translateY) {
		setTransform(getScaleX(), getShearY(), getShearX(), getScaleY(), getTranslateX(), translateY);
	}

	public Point deltaTransform(Point2D src) {
		return (Point) deltaTransform(src, new Point());
	}

	public Point inverseTransform(Point2D src) throws NoninvertibleTransformException {
		return (Point) inverseTransform(src, new Point());
	}

	public Point transform(Point2D src) {
		return (Point) transform(src, new Point());
	}

	public void rotate(double theta, Point2D center) {
		super.rotate(theta, center.getX(), center.getY());
	}

	public void translate(Point2D pt) {
		super.translate(pt.getX(), pt.getY());
	}

	public void scale(double scale) {
		super.scale(scale, scale);
	}

	// wrappable interface

	 protected Scriptable wrapper;

	public void setWrapper(Scriptable wrapper) {
		this.wrapper = wrapper;
	}

	public Scriptable getWrapper() {
		return wrapper;
	}
}