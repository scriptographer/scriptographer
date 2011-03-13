/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 * 
 * File created on Dec 22, 2007.
 */

package com.scriptographer.ai;

import com.scratchdisk.script.ArgumentReader;
import com.scratchdisk.script.ChangeEmitter;
import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 */
public class Size implements ChangeEmitter {
	protected double width;
	protected double height;

	public Size() {
		width = height = 0;
	}

	/**
	 * Creates a Size object with the given width and height.
	 * 
	 * @param width The width of the Size {@default 0}
	 * @param height The height of the Size {@default 0}
	 */
	public Size(double width, double height) {
		set(width, height);
	}

	public Size(float width, float height) {
		set(width, height);
	}

	/**
	 * Creates a Size object with the given value for both width and height.
	 * 
	 * @param size The width and height of the Size
	 */
	public Size(double size) {
		set(size, size);
	}

	/**
	 * Creates a Size object using the x and y coordinates of the given Point
	 * object.
	 * 
	 * Sample code:
	 * <code>
	 * var point = new Point(50, 50);
	 * var size = new Size(point);
	 * print(size.width); // 50
	 * print(size.height); // 50
	 * </code>
	 * 
	 * @param point
	 */
	public Size(Point point) {
		this(point.x, point.y);
	}

	/**
	 * Creates a Size object using the width and height of the given Size
	 * object.
	 * 
	 * @param size
	 */
	public Size(Size size) {
		this(size.width, size.height);
	}

	/**
	 * @jshide
	 */
	public Size(ArgumentReader reader) {
		this(reader.has("width") ? reader.readDouble("width", 0) : reader.readDouble("x", 0),
				reader.has("height") ? reader.readDouble("height", 0) : reader.readDouble("y", 0));
	}

	/**
	 * @jshide
	 */
	public void set(double width, double height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * The width of the size.
	 */
	public double getWidth() {
		return width;
	}
	
	public void setWidth(double width) {
		this.width = width;
	}

	/**
	 * The height of the size.
	 */
	public double getHeight() {
		return height;
	}
	
	public void setHeight(double height) {
		this.height = height;
	}

	/**
	 * @jshide
	 */
	public Size add(double w, double h) {
		return new Size(width + w, height + h);
	}

	/**
	 * Returns the addition of the width and height of the supplied size to the
	 * size as a new size. The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var firstSize = new Size(8, 10);
	 * var secondSize = new Size(2, 5);
	 * var result = firstSize + secondSize;
	 * print(result); // { width: 10.0, height: 15.0 }
	 * </code>
	 * 
	 * @param size The addition of the two sizes as a new size
	 */
	public Size add(Size size) {
		return add(size.width, size.height);
	}

	/**
	 * Returns the addition of the supplied value to the width and height of the
	 * size as a new size. The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var point = new Size(10, 20);
	 * var result = size + 5;
	 * print(result); // { width: 15.0, height: 25.0 }
	 * </code>
	 * 
	 * @param value
	 * @return the addition of the size and the value as a new size
	 */
	public Size add(double value) {
		return add(value, value);
	}

	/**
	 * @jshide
	 */
	public Size subtract(double w, double h) {
		return new Size(width - w, height - h);
	}

	/**
	 * Returns the subtraction of the width and height of the supplied size from
	 * the size as a new size. The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var firstSize = new Size(8, 10);
	 * var secondSize = new Size(2, 5);
	 * var result = firstSize - secondSize;
	 * print(result); // { width: 6.0, height: 5.0 }
	 * </code>
	 * 
	 * @param size The subtraction of the two sizes as a new size
	 */
	public Size subtract(Size size) {
		return subtract(size.width, size.height);
	}

	/**
	 * Returns the subtraction of the supplied value from the width and height
	 * of the size as a new size. The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var point = new Size(10, 20);
	 * var result = size - 5;
	 * print(result); // { width: 5.0, height: 15.0 }
	 * </code>
	 * 
	 * @param value
	 * @return the subtraction of the value from the size as a new size
	 */
	public Size subtract(double value) {
		return subtract(value, value);
	}

	/**
	 * @jshide
	 */
	public Size multiply(double w, double h) {
		return new Size(width * w, height * h);
	}

	/**
	 * Returns the multiplication of the width and height of the supplied size
	 * with the size as a new size. The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var firstSize = new Size(8, 10);
	 * var secondSize = new Size(2, 5);
	 * var result = firstSize * secondSize;
	 * print(result); // { width: 16.0, height: 50.0 }
	 * </code>
	 * 
	 * @param size The multiplication of the two sizes as a new size
	 */
	public Size multiply(Size size) {
		return multiply(size.width, size.height);
	}

	/**
	 * Returns the multiplication of the supplied value with the width and
	 * height of the size as a new size. The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var point = new Size(10, 20);
	 * var result = size * 2;
	 * print(result); // { width: 20.0, height: 40.0 }
	 * </code>
	 * 
	 * @param value
	 * @return the multiplication of the size by the value as a new size
	 */
	public Size multiply(double value) {
		return multiply(value, value);
	}

	/**
	 * @jshide
	 */
	public Size divide(double w, double h) {
		return new Size(width / w, height / h);
	}

	/**
	 * Returns the division of the width and height of the supplied size by the
	 * size as a new size. The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var firstSize = new Size(8, 10);
	 * var secondSize = new Size(2, 5);
	 * var result = firstSize / secondSize;
	 * print(result); // { width: 4.0, height: 2.0 }
	 * </code>
	 * 
	 * @param size The division of the two sizes as a new size
	 */
	public Size divide(Size size) {
		return divide(size.width, size.height);
	}

	/**
	 * Returns the division of the supplied value by the width and height of the
	 * size as a new size. The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var point = new Size(10, 20);
	 * var result = size / 2;
	 * print(result); // { width: 5.0, height: 10.0 }
	 * </code>
	 * 
	 * @param value
	 * @return the division of the size by the value as a new size
	 */
	public Size divide(double value) {
		return divide(value, value);
	}

	/**
	 * @jshide
	 */
	public Size modulo(double w, double h) {
		return new Size(this.width % w, this.height % h);
	}

	/**
	 * The modulo operator returns the integer remainders of dividing the size
	 * by the supplied size as a new size.
	 * 
	 * Sample code:
	 * <code>
	 * var size = new Size(12, 6);
	 * print(size % new Size(5, 2)); // {width: 2, height: 0}
	 * </code>
	 * 
	 * @param size
	 * @return the integer remainders of dividing the sizes by each other as a
	 *         new size
	 */
	public Size modulo(Size size) {
		return modulo(size.width, size.height);
	}

	/**
	 * The modulo operator returns the integer remainders of dividing the size
	 * by the supplied value as a new size.
	 * 
	 * Sample code:
	 * <code>
	 * var size = new Size(12, 6);
	 * print(size % 5); // {width: 2, height: 1}
	 * </code>
	 * 
	 * @param value
	 * @return the integer remainders of dividing the size by the value as a new size
	 */
	public Size modulo(double value) {
		return modulo(value, value);
	}

	/**
	 * @jshide
	 */
	public Size negate() {
		return new Size(-width, -height);
	}

	/**
	 * Returns a copy of the size.
	 * This is useful as the following code only generates a flat copy:
	 * 
	 * <code>
	 * var size2 = new Size();
	 * var size2 = size1;
	 * size2.x = 1; // also changes size1.x
	 * 
	 * var size2 = size1.clone();
	 * size2.x = 1; // doesn't change size1.x
	 * </code>
	 * 
	 * @return the cloned size object
	 */
	public Object clone() {
		return new Size(this);
	}

	/**
	 * Checks whether the width and height of the size are equal to those of the
	 * supplied size.
	 * 
	 * Sample code:
	 * <code>
	 * var size = new Size(5, 10);
	 * print(size == new Size(5, 10)); // true
	 * print(size == new Size(1, 1)); // false
	 * print(size != new Size(1, 1)); // true
	 * </code>
	 */
	public boolean equals(Object object) {
		if (object instanceof Size) {
			Size size = (Size) object;
			return size.width == width && size.height == height;
		}
		// TODO: support other point types?
		return false;
	}

	/**
	 * {@grouptitle Math Functions}
	 * 
	 * Returns a new size with rounded {@link #width} and {@link #height}
	 * values. The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var size = new Size(10.2, 10.9);
	 * var roundSize = size.round();
	 * print(roundSize); // { width: 10.0, height: 11.0 }
	 * </code>
	 */
	public Size round() {
		return new Size(Math.round(width), Math.round(height));
	}

	/**
	 * Returns a new size with the nearest greater non-fractional values to the
	 * specified {@link #width} and {@link #height} values. The object itself is not
	 * modified!
	 * 
	 * Sample code:
	 * <code>
	 * var size = new Size(10.2, 10.9);
	 * var ceilSize = size.ceil();
	 * print(ceilSize); // { width: 11.0, height: 11.0 }
	 * </code>
	 */
	public Size ceil() {
		return new Size(Math.ceil(width), Math.ceil(height));
	}

	/**
	 * Returns a new size with the nearest smaller non-fractional values to the
	 * specified {@link #width} and {@link #height} values. The object itself is
	 * not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var size = new Size(10.2, 10.9);
	 * var floorSize = size.floor();
	 * print(floorSize); // { width: 10.0, height: 10.0 }
	 * </code>
	 */
	public Size floor() {
		return new Size(Math.floor(width), Math.floor(height));
	}

	/**
	 * Returns a new size with the absolute values of the specified
	 * {@link #width} and {@link #height} values. The object itself is not
	 * modified!
	 * 
	 * Sample code:
	 * <code>
	 * var size = new Size(-5, 10);
	 * var absSize = size.abs();
	 * print(absSize); // { width: 5.0, height: 10.0 }
	 * </code>
	 */
	public Size abs() {
		return new Size(Math.abs(width), Math.abs(height));
	}

	/**
	 * Returns a new size object with the smallest {@link #width} and
	 * {@link #height} of the supplied sizes.
	 * 
	 * Sample code:
	 * <code>
	 * var size1 = new Size(10, 100);
	 * var size2 = new Size(200, 5);
	 * var minSize = Size.min(size1, size2);
	 * print(minSize); // { width: 10.0, height: 5.0 }
	 * </code>
	 * 
	 * @param size1
	 * @param size2
	 * @return The newly created size object
	 */
	public static Size min(Size size1, Size size2) {
		return new Size(
				Math.min(size1.width, size2.width),
				Math.min(size1.height, size2.height));
	}

	/**
	 * Returns a new size object with the largest {@link #width} and
	 * {@link #height} of the supplied sizes.
	 * 
	 * Sample code:
	 * <code>
	 * var size1 = new Size(10, 100);
	 * var size2 = new Size(200, 5);
	 * var maxSize = Size.max(size1, size2);
	 * print(maxSize); // { width: 200.0, height: 100.0 }
	 * </code>
	 * 
	 * @param size1
	 * @param size2
	 * @return The newly created size object
	 */
	public static Size max(Size size1, Size size2) {
		return new Size(
				Math.max(size1.width, size2.width),
				Math.max(size1.height, size2.height));
	}

	/**
	 * Returns a size object with random {@link #width} and {@link #height}
	 * values between {@code 0} and {@code 1}.
	 * 
	 * Sample code:
	 * <code>
	 * var maxSize = new Size(100, 100);
	 * var randomSize = Size.random();
	 * var size = maxSize * randomSize;
	 * </code>
	 */
	public static Size random() {
		return new Size(Math.random(), Math.random());
	}

	public String toString() {
	   	return "{ width: " + ScriptographerEngine.numberFormat.format(width)
	   			+ ", height: " + ScriptographerEngine.numberFormat.format(height)
	   			+ " }";
	}
}
