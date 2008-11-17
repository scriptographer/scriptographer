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
 * File created on Oct 19, 2006.
 * 
 * $Id$
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

	public Matrix getMatrix() {
		return matrix;
	}

	public void setMatrix(Matrix matrix) {
		this.matrix = new Matrix(matrix);
	}
}
