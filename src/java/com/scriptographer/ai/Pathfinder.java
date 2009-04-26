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
 * File created on 21.01.2005.
 *
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.list.ExtendedList;

/**
 * @author lehni
 */
public final class Pathfinder {
	/**
	 * Don't let anyone instantiate this class.
	 */
	private Pathfinder() {
	}

	public static final float DEFAULT_PRECISION = 10f;
	public static final boolean DEFAULT_REMOVE_POINTS = false;
	public static final boolean DEFAULT_EXTRACT_UNPAINTED = false;

	// Unite

	public static native Item unite(Object[] artObjects, float precision,
			boolean removePoints, boolean extractUnpainted);

	public static Item unite(Object[] artObjects, float precision,
			boolean removePoints) {
		return unite(artObjects, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item unite(Object[] artObjects, float precision) {
		return unite(artObjects, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item unite(Object[] artObjects) {
		return unite(artObjects, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item unite(ExtendedList artObjects, float precision,
			boolean removePoints, boolean extractUnpainted) {
		return unite(artObjects.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item unite(ExtendedList artObjects, float precision,
			boolean removePoints) {
		return unite(artObjects.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item unite(ExtendedList artObjects, float precision) {
		return unite(artObjects.toArray(), precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item unite(ExtendedList artObjects) {
		return unite(artObjects.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// intersect

	public static native Item intersect(Object[] artObjects, float precision,
			boolean removePoints, boolean extractUnpainted);

	public static Item intersect(Object[] artObjects, float precision,
			boolean removePoints) {
		return intersect(artObjects, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item intersect(Object[] artObjects, float precision) {
		return intersect(artObjects, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item intersect(Object[] artObjects) {
		return intersect(artObjects, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item intersect(ExtendedList artObjects, float precision,
			boolean removePoints, boolean extractUnpainted) {
		return intersect(artObjects.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item intersect(ExtendedList artObjects, float precision,
			boolean removePoints) {
		return intersect(artObjects.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item intersect(ExtendedList artObjects, float precision) {
		return intersect(artObjects.toArray(), precision,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item intersect(ExtendedList artObjects) {
		return intersect(artObjects.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// exclude

	public static native Item exclude(Object[] artObjects, float precision,
			boolean removePoints, boolean extractUnpainted);

	public static Item exclude(Object[] artObjects, float precision,
			boolean removePoints) {
		return exclude(artObjects, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item exclude(Object[] artObjects, float precision) {
		return exclude(artObjects, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item exclude(Object[] artObjects) {
		return exclude(artObjects, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item exclude(ExtendedList artObjects, float precision,
			boolean removePoints, boolean extractUnpainted) {
		return exclude(artObjects.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item exclude(ExtendedList artObjects, float precision,
			boolean removePoints) {
		return exclude(artObjects.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item exclude(ExtendedList artObjects, float precision) {
		return exclude(artObjects.toArray(), precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item exclude(ExtendedList artObjects) {
		return exclude(artObjects.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// backMinusFront

	public static native Item backMinusFront(Object[] artObjects,
			float precision, boolean removePoints, boolean extractUnpainted);

	public static Item backMinusFront(Object[] artObjects, float precision,
			boolean removePoints) {
		return backMinusFront(artObjects, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item backMinusFront(Object[] artObjects, float precision) {
		return backMinusFront(artObjects, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item backMinusFront(Object[] artObjects) {
		return backMinusFront(artObjects, DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item backMinusFront(ExtendedList artObjects,
			float precision, boolean removePoints, boolean extractUnpainted) {
		return backMinusFront(artObjects.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item backMinusFront(ExtendedList artObjects,
			float precision, boolean removePoints) {
		return backMinusFront(artObjects.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item backMinusFront(ExtendedList artObjects, float precision) {
		return backMinusFront(artObjects.toArray(), precision,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item backMinusFront(ExtendedList artObjects) {
		return backMinusFront(artObjects.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// frontMinusBack

	public static native Item frontMinusBack(Object[] artObjects,
			float precision, boolean removePoints, boolean extractUnpainted);

	public static Item frontMinusBack(Object[] artObjects, float precision,
			boolean removePoints) {
		return frontMinusBack(artObjects, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item frontMinusBack(Object[] artObjects, float precision) {
		return frontMinusBack(artObjects, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item frontMinusBack(Object[] artObjects) {
		return frontMinusBack(artObjects, DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item frontMinusBack(ExtendedList artObjects,
			float precision, boolean removePoints, boolean extractUnpainted) {
		return frontMinusBack(artObjects.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item frontMinusBack(ExtendedList artObjects,
			float precision, boolean removePoints) {
		return frontMinusBack(artObjects.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item frontMinusBack(ExtendedList artObjects, float precision) {
		return frontMinusBack(artObjects.toArray(), precision,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item frontMinusBack(ExtendedList artObjects) {
		return frontMinusBack(artObjects.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// divide

	public static native Item divide(Object[] artObjects, float precision,
			boolean removePoints, boolean extractUnpainted);

	public static Item divide(Object[] artObjects, float precision,
			boolean removePoints) {
		return divide(artObjects, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item divide(Object[] artObjects, float precision) {
		return divide(artObjects, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item divide(Object[] artObjects) {
		return divide(artObjects, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item divide(ExtendedList artObjects, float precision,
			boolean removePoints, boolean extractUnpainted) {
		return divide(artObjects.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item divide(ExtendedList artObjects, float precision,
			boolean removePoints) {
		return divide(artObjects.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item divide(ExtendedList artObjects, float precision) {
		return divide(artObjects.toArray(), precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item divide(ExtendedList artObjects) {
		return divide(artObjects.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// outline

	public static native Item outline(Object[] artObjects, float precision,
			boolean removePoints, boolean extractUnpainted);

	public static Item outline(Object[] artObjects, float precision,
			boolean removePoints) {
		return outline(artObjects, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item outline(Object[] artObjects, float precision) {
		return outline(artObjects, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item outline(Object[] artObjects) {
		return outline(artObjects, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item outline(ExtendedList artObjects, float precision,
			boolean removePoints, boolean extractUnpainted) {
		return outline(artObjects.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item outline(ExtendedList artObjects, float precision,
			boolean removePoints) {
		return outline(artObjects.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item outline(ExtendedList artObjects, float precision) {
		return outline(artObjects.toArray(), precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item outline(ExtendedList artObjects) {
		return outline(artObjects.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// trim

	public static native Item trim(Object[] artObjects, float precision,
			boolean removePoints, boolean extractUnpainted);

	public static Item trim(Object[] artObjects, float precision,
			boolean removePoints) {
		return trim(artObjects, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item trim(Object[] artObjects, float precision) {
		return trim(artObjects, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item trim(Object[] artObjects) {
		return trim(artObjects, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item trim(ExtendedList artObjects, float precision,
			boolean removePoints, boolean extractUnpainted) {
		return trim(artObjects.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item trim(ExtendedList artObjects, float precision,
			boolean removePoints) {
		return trim(artObjects.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item trim(ExtendedList artObjects, float precision) {
		return trim(artObjects.toArray(), precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item trim(ExtendedList artObjects) {
		return trim(artObjects.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// merge

	public static native Item merge(Object[] artObjects, float precision,
			boolean removePoints, boolean extractUnpainted);

	public static Item merge(Object[] artObjects, float precision,
			boolean removePoints) {
		return merge(artObjects, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item merge(Object[] artObjects, float precision) {
		return merge(artObjects, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item merge(Object[] artObjects) {
		return merge(artObjects, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item merge(ExtendedList artObjects, float precision,
			boolean removePoints, boolean extractUnpainted) {
		return merge(artObjects.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item merge(ExtendedList artObjects, float precision,
			boolean removePoints) {
		return merge(artObjects.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item merge(ExtendedList artObjects, float precision) {
		return merge(artObjects.toArray(), precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item merge(ExtendedList artObjects) {
		return merge(artObjects.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// crop

	public static native Item crop(Object[] artObjects, float precision,
			boolean removePoints, boolean extractUnpainted);

	public static Item crop(Object[] artObjects, float precision,
			boolean removePoints) {
		return crop(artObjects, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item crop(Object[] artObjects, float precision) {
		return crop(artObjects, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item crop(Object[] artObjects) {
		return crop(artObjects, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item crop(ExtendedList artObjects, float precision,
			boolean removePoints, boolean extractUnpainted) {
		return crop(artObjects.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item crop(ExtendedList artObjects, float precision,
			boolean removePoints) {
		return crop(artObjects.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item crop(ExtendedList artObjects, float precision) {
		return crop(artObjects.toArray(), precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item crop(ExtendedList artObjects) {
		return crop(artObjects.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}
}
