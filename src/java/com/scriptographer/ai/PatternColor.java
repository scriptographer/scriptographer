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
 * File created on Oct 19, 2006.
 */

package com.scriptographer.ai;

/**
 * @author lehni
 */
public class PatternColor extends Color {
	Pattern pattern;
	Matrix matrix;

	/**
	 * Called from the native environment
	 */
	protected PatternColor(int patternHandle, Matrix matrix) {
		// do not use constructor bellow as we do not need to copy origin and matrix here
		this.pattern = Pattern.wrapHandle(patternHandle, null);
		this.matrix = matrix;
	}
	
	public PatternColor(Pattern pattern, Matrix matrix) {
		this.pattern = pattern;
		this.matrix = new Matrix(matrix);
	}
	
	/**
	 * called from the native environment, to fill a native struct
	 * @param struct
	 */
	protected void set(int pointer) {
		nativeSetPattern(pointer, pattern.handle, matrix);
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof PatternColor) {
			PatternColor col = (PatternColor) obj;
			return (pattern.equals(col.pattern) && matrix.equals(col.matrix));
		}
		return false;
	}

	public float[] getComponents() {
		throw new UnsupportedOperationException("Cannot convert pattern to components");
	}

	public java.awt.Color toAWTColor() {
		throw new UnsupportedOperationException("Cannot convert pattern to AWT color");
	}
	
	public void setAlpha(float alhpa) {
		throw new UnsupportedOperationException("Cannot set alpha on pattern");
	}

	public Pattern getPattern() {
		return pattern;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
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
