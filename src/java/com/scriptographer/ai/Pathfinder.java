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
 * File created on 21.01.2005.
 *
 * $RCSfile: Pathfinder.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/07/27 22:55:30 $
 */

package com.scriptographer.ai;

import com.scriptographer.util.List;

public final class Pathfinder {
	/**
     * Don't let anyone instantiate this class.
     */
    private Pathfinder() {
	}

	public final static float DEFAULT_PRECISION = 10f;
	public final static boolean DEFAULT_REMOVE_POINTS = false;
	public final static boolean DEFAULT_EXTRACT_UNPAINTED = false;

	// unite

	public static native void unite(Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted);

	public static void unite(Object[] artObjects, float precision, boolean removePoints) {
		unite(artObjects, precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void unite(Object[] artObjects, float precision) {
		unite(artObjects, precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void unite(Object[] artObjects) {
		unite(artObjects, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void unite(List artObjects, float precision, boolean removePoints, boolean extractUnpainted) {
		unite(artObjects.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void unite(List artObjects, float precision, boolean removePoints) {
		unite(artObjects.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void unite(List artObjects, float precision) {
		unite(artObjects.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void unite(List artObjects) {
		unite(artObjects.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// intersect

	public static native void intersect(Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted);

	public static void intersect(Object[] artObjects, float precision, boolean removePoints) {
		intersect(artObjects, precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void intersect(Object[] artObjects, float precision) {
		intersect(artObjects, precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void intersect(Object[] artObjects) {
		intersect(artObjects, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void intersect(List artObjects, float precision, boolean removePoints, boolean extractUnpainted) {
		intersect(artObjects.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void intersect(List artObjects, float precision, boolean removePoints) {
		intersect(artObjects.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void intersect(List artObjects, float precision) {
		intersect(artObjects.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void intersect(List artObjects) {
		intersect(artObjects.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// exclude

	public static native void exclude(Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted);

	public static void exclude(Object[] artObjects, float precision, boolean removePoints) {
		exclude(artObjects, precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void exclude(Object[] artObjects, float precision) {
		exclude(artObjects, precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void exclude(Object[] artObjects) {
		exclude(artObjects, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void exclude(List artObjects, float precision, boolean removePoints, boolean extractUnpainted) {
		exclude(artObjects.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void exclude(List artObjects, float precision, boolean removePoints) {
		exclude(artObjects.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void exclude(List artObjects, float precision) {
		exclude(artObjects.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void exclude(List artObjects) {
		exclude(artObjects.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// backMinusFront

	public static native void backMinusFront(Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted);

	public static void backMinusFront(Object[] artObjects, float precision, boolean removePoints) {
		backMinusFront(artObjects, precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void backMinusFront(Object[] artObjects, float precision) {
		backMinusFront(artObjects, precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void backMinusFront(Object[] artObjects) {
		backMinusFront(artObjects, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void backMinusFront(List artObjects, float precision, boolean removePoints, boolean extractUnpainted) {
		backMinusFront(artObjects.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void backMinusFront(List artObjects, float precision, boolean removePoints) {
		backMinusFront(artObjects.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void backMinusFront(List artObjects, float precision) {
		backMinusFront(artObjects.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void backMinusFront(List artObjects) {
		backMinusFront(artObjects.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// frontMinusBack

	public static native void frontMinusBack(Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted);

	public static void frontMinusBack(Object[] artObjects, float precision, boolean removePoints) {
		frontMinusBack(artObjects, precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void frontMinusBack(Object[] artObjects, float precision) {
		frontMinusBack(artObjects, precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void frontMinusBack(Object[] artObjects) {
		frontMinusBack(artObjects, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void frontMinusBack(List artObjects, float precision, boolean removePoints, boolean extractUnpainted) {
		frontMinusBack(artObjects.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void frontMinusBack(List artObjects, float precision, boolean removePoints) {
		frontMinusBack(artObjects.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void frontMinusBack(List artObjects, float precision) {
		frontMinusBack(artObjects.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void frontMinusBack(List artObjects) {
		frontMinusBack(artObjects.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// divide

	public static native void divide(Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted);

	public static void divide(Object[] artObjects, float precision, boolean removePoints) {
		divide(artObjects, precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void divide(Object[] artObjects, float precision) {
		divide(artObjects, precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void divide(Object[] artObjects) {
		divide(artObjects, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void divide(List artObjects, float precision, boolean removePoints, boolean extractUnpainted) {
		divide(artObjects.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void divide(List artObjects, float precision, boolean removePoints) {
		divide(artObjects.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void divide(List artObjects, float precision) {
		divide(artObjects.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void divide(List artObjects) {
		divide(artObjects.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// outline

	public static native void outline(Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted);

	public static void outline(Object[] artObjects, float precision, boolean removePoints) {
		outline(artObjects, precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void outline(Object[] artObjects, float precision) {
		outline(artObjects, precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void outline(Object[] artObjects) {
		outline(artObjects, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void outline(List artObjects, float precision, boolean removePoints, boolean extractUnpainted) {
		outline(artObjects.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void outline(List artObjects, float precision, boolean removePoints) {
		outline(artObjects.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void outline(List artObjects, float precision) {
		outline(artObjects.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void outline(List artObjects) {
		outline(artObjects.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// trim

	public static native void trim(Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted);

	public static void trim(Object[] artObjects, float precision, boolean removePoints) {
		trim(artObjects, precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void trim(Object[] artObjects, float precision) {
		trim(artObjects, precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void trim(Object[] artObjects) {
		trim(artObjects, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void trim(List artObjects, float precision, boolean removePoints, boolean extractUnpainted) {
		trim(artObjects.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void trim(List artObjects, float precision, boolean removePoints) {
		trim(artObjects.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void trim(List artObjects, float precision) {
		trim(artObjects.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void trim(List artObjects) {
		trim(artObjects.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// merge

	public static native void merge(Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted);

	public static void merge(Object[] artObjects, float precision, boolean removePoints) {
		merge(artObjects, precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void merge(Object[] artObjects, float precision) {
		merge(artObjects, precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void merge(Object[] artObjects) {
		merge(artObjects, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void merge(List artObjects, float precision, boolean removePoints, boolean extractUnpainted) {
		merge(artObjects.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void merge(List artObjects, float precision, boolean removePoints) {
		merge(artObjects.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void merge(List artObjects, float precision) {
		merge(artObjects.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void merge(List artObjects) {
		merge(artObjects.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// crop

	public static native void crop(Object[] artObjects, float precision, boolean removePoints, boolean extractUnpainted);

	public static void crop(Object[] artObjects, float precision, boolean removePoints) {
		crop(artObjects, precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void crop(Object[] artObjects, float precision) {
		crop(artObjects, precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void crop(Object[] artObjects) {
		crop(artObjects, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void crop(List artObjects, float precision, boolean removePoints, boolean extractUnpainted) {
		crop(artObjects.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void crop(List artObjects, float precision, boolean removePoints) {
		crop(artObjects.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void crop(List artObjects, float precision) {
		crop(artObjects.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void crop(List artObjects) {
		crop(artObjects.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}
}
