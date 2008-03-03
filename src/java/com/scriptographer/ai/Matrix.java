/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id$
 */

package com.scriptographer.ai;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

/**
 * Matrix represents an affine transformation between two coordinate spaces in 2
 * dimensions. Such a transform preserves the "straightness" and "parallelness"
 * of lines. The transform is built from a sequence of translations, scales,
 * flips, rotations, and shears.
 * 
 * The transformation can be represented using matrix math on a 3x3 array. Given
 * <code>(x, y)</code>, the transformation <code>(x', y')</code> can be
 * found by:
 * 
 * <pre>
 * [ x']   [ scaleX shearX translateX ] [ x ]   [ scaleX * x + shearX * y + translateX ]
 * [ y'] = [ shearY scaleY translateY ] [ y ] = [ shearY * x + scaleY * y + translateY ]
 * [ 1 ]   [ 0      0      1          ] [ 1 ]   [ 1                                    ]
 * </pre>
 * 
 * The bottom row of the matrix is constant, so a transform can be uniquely
 * represented (as in {@link #toString()}) by
 * <code>"[[scaleX, shearX, translateX], [shearY, scaleY, translateY]]"</code>.
 * 
 * @author lehni
 */
public class Matrix {

	private AffineTransform at;
	
	public Matrix() {
		at = new AffineTransform();
	}

	/**
	 * Create a new matrix which copies the given one.
	 *
	 * @param m the matrix to copy
	 * @throws NullPointerException if m is null
	 */
	public Matrix(Matrix m) {
		at = m.toAffineTransform();
	}

	public AffineTransform toAffineTransform() {
		return new AffineTransform(at);
	}

	/**
	 * Create a new matrix from the given AWT AffineTransform.
	 *
	 * @param at the transform to copy
	 * @throws NullPointerException if at is null
	 */
	public Matrix(AffineTransform at) {
		this.at = new AffineTransform(at);
	}

	/**
	 * Construct a transform with the given matrix entries:
	 * 
	 * <pre>
	 *  [ scaleX shearX translateX ]
	 *  [ shearY scaleY translateY ]
	 *  [ 0      0      1          ]
	 * </pre>
	 * 
	 * @param scaleX the x scaling component
	 * @param shearY the y shearing component
	 * @param shearX the x shearing component
	 * @param scaleY the y scaling component
	 * @param translateX the x translation component
	 * @param translateY the y translation component
	 */
	public Matrix(double scaleX, double shearY, double shearX, double scaleY,
			double translateX, double translateY) {
		at = new AffineTransform(scaleX, shearY, shearX, scaleY,
				translateX, translateY);
	}

	/**
	 * Construct a matrix from a sequence of numbers. The array must
	 * have at least 4 entries, which has a translation factor of 0; or 6
	 * entries, for specifying all parameters:
	 * <pre>
	 * [ values[0] values[2] (values[4]) ]
	 * [ values[1] values[3] (values[5]) ]
	 * [ 0         0         1           ]
	 * </pre>
	 *
	 * @param values the matrix to copy from, with at least 4 (6) entries
	 * @throws NullPointerException if values is null
	 * @throws ArrayIndexOutOfBoundsException if values is too small
	 */
	public Matrix(double[] values) {
		at = new AffineTransform(values);
	}

	/**
	 * Construct a matrix from a two dimensional array:
	 * <pre>
	 * [ values[0][0] values[0][1] values[0][2] ]
	 * [ values[1][0] values[1][1] values[1][2] ]
	 * [ 0            0            1            ]
	 * </pre>
	 *
	 * @param values the matrix to copy from
	 * @throws NullPointerException if values is null
	 * @throws ArrayIndexOutOfBoundsException if values is too small
	 */
	public Matrix(double[][] values) {
		at = new AffineTransform(
				values[0][0], values[0][1], values[0][2],
				values[1][0], values[1][1], values[1][2]);
	}

	/**
	 * Returns a copy of this <code>Matrix</code> object.
	 * 
	 * @return an copy of this <code>Matrix</code> object.
	 */
	public Object clone() {
		return new Matrix(this);
	}

	/**
	 * Creates the inverse transformation of the object. If the object is not
	 * invertible (in which case {@link #isSingular()} returns true), invert() returns
	 * null, otherwise the object itself is modified and a reference to it is
	 * returned.
	 * 
	 * @return the inversed matrix, or null, if the matrix is singular
	 */
	public Matrix invert() {
		try {
			at = at.createInverse();
			return this;
		} catch (NoninvertibleTransformException e) {
			return null;
		}
	}

	public boolean equals(Object obj) {
		return at.equals(((Matrix) obj).at);
	}
	
	/**
	 * Checks wether the matrix is an identity. Identity matrices are equal to
	 * their inversion.
	 * 
	 * @return true if the matrix is an identity, false otherwise
	 */
	public boolean isIdentity() {
		return at.isIdentity();
	}

	/**
	 * Checks wether the matrix is singular or not. Singular matrices cannot be
	 * inverted.
	 * 
	 * @return true if the matrix is singular, false otherwise
	 */
	public boolean isSingular() {
		// There seems to be no other way to find out if we can 
		// invert than actually trying:
		return invert() == null;
	}

	public double getScaleX() {
		return at.getScaleX();
	}

	public void setScaleX(double scaleX) {
		at.setTransform(scaleX, at.getShearY(), at.getShearX(), at.getScaleY(),
				at.getTranslateX(), at.getTranslateY());
	}
	
	public double getScaleY() {
		return at.getScaleY();
	}

	public void setScaleY(double scaleY) {
		at.setTransform(at.getScaleX(), at.getShearY(), at.getShearX(), scaleY,
				at.getTranslateX(), at.getTranslateY());
	}
	
	public double getShearX() {
		return at.getShearX();
	}

	public void setShearX(double shearX) {
		at.setTransform(at.getScaleX(), at.getShearY(), shearX, at.getScaleY(),
				at.getTranslateX(), at.getTranslateY());
	}

	public double getShearY() {
		return at.getShearY();
	}

	public void setShearY(double shearY) {
		at.setTransform(at.getScaleX(), at.getShearY(), at.getShearX(), shearY,
				at.getTranslateX(), at.getTranslateY());
	}

	public double getTranslateX() {
		return at.getTranslateX();
	}

	public void setTranslateX(double translateX) {
		at.setTransform(at.getScaleX(), at.getShearY(), at.getShearX(),
				at.getScaleY(), translateX, at.getTranslateY());
	}

	public double getTranslateY() {
		return at.getTranslateY();
	}

	public void setTranslateY(double translateY) {
		at.setTransform(at.getScaleX(), at.getShearY(), at.getShearX(),
				at.getScaleY(), at.getTranslateX(), translateY);
	}

	/**
	 * Concatenates the matrix with a translation matrix that translates by
	 * <code>(x, y)</code>. The object itself is modified and a reference to
	 * it is returned.
	 * 
	 * @param x,&nbsp;y the coordinates of the translation
	 * @return the translated matrix
	 */
	public Matrix translate(double x, double y) {
		at.translate(x, y);
		return this;
	}

	public Matrix translate(Point pt) {
		at.translate(pt.getX(), pt.getY());
		return this;
	}

	public Matrix scale(double scale) {
		at.scale(scale, scale);
		return this;
	}

	public Matrix scale(double scaleX, double scaleY) {
		at.scale(scaleX, scaleY);
		return this;
	}

	public Matrix scale(Point scale) {
		at.scale(scale.getX(), scale.getY());
		return this;
	}

	public Matrix rotate(double theta) {
		at.rotate(theta);
		return this;
	}

	public Matrix rotate(double theta, float centerX, float centerY) {
		at.rotate(theta, centerX, centerY);
		return this;
	}

	public Matrix rotate(double theta, Point center) {
		at.rotate(theta, center.getX(), center.getY());
		return this;
	}

	public Matrix shear(double shearX, double shearY) {
		at.shear(shearX, shearY);
		return this;
	}

	public Matrix concatenate(Matrix mx) {
		at.concatenate(mx.toAffineTransform());
		return this;
	}

	public Matrix preConcatenate(Matrix mx) {
		at.preConcatenate(mx.toAffineTransform());
		return this;
	}

	public Point transform(float x, float y) {
		// A bit of converting from Point2D <-> Point
		return new Point(at.transform(new Point2D.Float(x, y), new Point2D.Float()));
	}

	public Point transform(Point src) {
		return transform(src.x, src.y);
	}

	// Round values to sane precision for printing
    // Note that Math.sin(Math.PI) has an error of about 10^-16
    private static double round(double matval) {
    	return Math.rint(matval * 1E15) / 1E15;
    }

    public String toString() {
		return "[["
			+ round(at.getScaleX()) + ", "
			+ round(at.getShearX()) + ", "
			+ round(at.getTranslateX()) + "], ["
			+ round(at.getShearY()) + ", "
			+ round(at.getScaleY()) + ", "
			+ round(at.getTranslateY()) + "]]";
    }
}