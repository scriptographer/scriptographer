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
 * File created on 20.12.2004.
 *
 * $RCSfile: Matrix.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:01:00 $
 */

package com.scriptographer.ai;

import java.awt.geom.*;

public class Matrix extends AffineTransform {
	
	public Matrix() {
	}
	
	public Matrix(AffineTransform at) {
		super(at);
	}
	
	public Matrix(double scaleX, double shearY, double shearX, double scaleY,
		double translateX, double translateY) {
		super(scaleX, shearY, shearX, scaleY, translateX, translateY);
	}

	public Matrix(double[] values) {
		super(values);
	}

	public Object clone() {
		return new Matrix((AffineTransform) this);
	}

	public AffineTransform createInverse()
		throws NoninvertibleTransformException {
		return new Matrix(super.createInverse());
	}

	public static AffineTransform getRotateInstance(double theta) {
		return new Matrix(AffineTransform.getRotateInstance(theta));
	}

	public static AffineTransform getRotateInstance(double theta, double x, double y) {
		return new Matrix(AffineTransform.getRotateInstance(theta, x, y));
	}

	public static AffineTransform getRotateInstance(double theta, Point2D center) {
		return new Matrix(AffineTransform.getRotateInstance(theta, center.getX(), center.getY()));
	}

	public static AffineTransform getScaleInstance(double sx, double sy) {
		return new Matrix(AffineTransform.getScaleInstance(sx, sy));
	}

	public static AffineTransform getScaleInstance(double scale) {
		return new Matrix(AffineTransform.getScaleInstance(scale, scale));
	}

	public static AffineTransform getShearInstance(double shx, double shy) {
		return new Matrix(AffineTransform.getShearInstance(shx, shy));
	}

	public static AffineTransform getTranslateInstance(double tx, double ty) {
		return new Matrix(AffineTransform.getTranslateInstance(tx, ty));
	}

	public static AffineTransform getTranslateInstance(Point2D pt) {
		return new Matrix(AffineTransform.getTranslateInstance(pt.getX(), pt.getY()));
	}

	public void setScaleX(double scaleX) {
		setTransform(scaleX, getShearY(), getShearX(), getScaleY(),
			getTranslateX(), getTranslateY());
	}

	public void setScaleY(double scaleY) {
		setTransform(getScaleX(), getShearY(), getShearX(), scaleY,
			getTranslateX(), getTranslateY());
	}

	public void setShearX(double shearX) {
		setTransform(getScaleX(), getShearY(), shearX, getScaleY(),
			getTranslateX(), getTranslateY());
	}

	public void setShearY(double shearY) {
		setTransform(getScaleX(), shearY, getShearX(), getScaleY(),
			getTranslateX(), getTranslateY());
	}

	public void setTranslateX(double translateX) {
		setTransform(getScaleX(), getShearY(), getShearX(), getScaleY(),
			translateX, getTranslateY());
	}

	public void setTranslateY(double translateY) {
		setTransform(getScaleX(), getShearY(), getShearX(), getScaleY(),
			getTranslateX(), translateY);
	}

	public Point deltaTransform(Point2D pt) {
		return (Point) deltaTransform(pt, new Point());
	}

	public Point inverseTransform(Point2D pt)
		throws NoninvertibleTransformException {
		return (Point) inverseTransform(pt, new Point());
	}

	public Point transform(Point2D pt) {
		return (Point) transform(pt, new Point());
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
}