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
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:01:00 $
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

	public static void unite(List list, float precision, boolean removePoints, boolean extractUnpainted) {
		unite(list.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void unite(List list, float precision, boolean removePoints) {
		unite(list.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void unite(List list, float precision) {
		unite(list.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void unite(List list) {
		unite(list.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
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

	public static void intersect(List list, float precision, boolean removePoints, boolean extractUnpainted) {
		intersect(list.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void intersect(List list, float precision, boolean removePoints) {
		intersect(list.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void intersect(List list, float precision) {
		intersect(list.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void intersect(List list) {
		intersect(list.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
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

	public static void exclude(List list, float precision, boolean removePoints, boolean extractUnpainted) {
		exclude(list.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void exclude(List list, float precision, boolean removePoints) {
		exclude(list.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void exclude(List list, float precision) {
		exclude(list.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void exclude(List list) {
		exclude(list.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
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

	public static void backMinusFront(List list, float precision, boolean removePoints, boolean extractUnpainted) {
		backMinusFront(list.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void backMinusFront(List list, float precision, boolean removePoints) {
		backMinusFront(list.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void backMinusFront(List list, float precision) {
		backMinusFront(list.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void backMinusFront(List list) {
		backMinusFront(list.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
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

	public static void frontMinusBack(List list, float precision, boolean removePoints, boolean extractUnpainted) {
		frontMinusBack(list.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void frontMinusBack(List list, float precision, boolean removePoints) {
		frontMinusBack(list.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void frontMinusBack(List list, float precision) {
		frontMinusBack(list.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void frontMinusBack(List list) {
		frontMinusBack(list.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
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

	public static void divide(List list, float precision, boolean removePoints, boolean extractUnpainted) {
		divide(list.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void divide(List list, float precision, boolean removePoints) {
		divide(list.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void divide(List list, float precision) {
		divide(list.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void divide(List list) {
		divide(list.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
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

	public static void outline(List list, float precision, boolean removePoints, boolean extractUnpainted) {
		outline(list.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void outline(List list, float precision, boolean removePoints) {
		outline(list.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void outline(List list, float precision) {
		outline(list.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void outline(List list) {
		outline(list.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
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

	public static void trim(List list, float precision, boolean removePoints, boolean extractUnpainted) {
		trim(list.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void trim(List list, float precision, boolean removePoints) {
		trim(list.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void trim(List list, float precision) {
		trim(list.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void trim(List list) {
		trim(list.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
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

	public static void merge(List list, float precision, boolean removePoints, boolean extractUnpainted) {
		merge(list.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void merge(List list, float precision, boolean removePoints) {
		merge(list.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void merge(List list, float precision) {
		merge(list.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void merge(List list) {
		merge(list.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
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

	public static void crop(List list, float precision, boolean removePoints, boolean extractUnpainted) {
		crop(list.toArray(), precision, removePoints, extractUnpainted);
	}

	public static void crop(List list, float precision, boolean removePoints) {
		crop(list.toArray(), precision, removePoints, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void crop(List list, float precision) {
		crop(list.toArray(), precision, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static void crop(List list) {
		crop(list.toArray(), DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}
}
