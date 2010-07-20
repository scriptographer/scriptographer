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
 * File created on 21.01.2005.
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

	private static final float DEFAULT_PRECISION = 10f;
	private static final boolean DEFAULT_REMOVE_POINTS = false;
	private static final boolean DEFAULT_EXTRACT_UNPAINTED = false;

	// Unite

	public static native Item unite(Object[] items, float precision,
			boolean removePoints, boolean extractUnpainted);

	public static Item unite(Object[] items, float precision,
			boolean removePoints) {
		return unite(items, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item unite(Object[] items, float precision) {
		return unite(items, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item unite(Object[] items) {
		return unite(items, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item unite(ExtendedList items, float precision,
			boolean removePoints, boolean extractUnpainted) {
		return unite(items.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item unite(ExtendedList items, float precision,
			boolean removePoints) {
		return unite(items.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item unite(ExtendedList items, float precision) {
		return unite(items.toArray(), precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item unite(ExtendedList items) {
		return unite(items.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// intersect

	public static native Item intersect(Object[] items, float precision,
			boolean removePoints, boolean extractUnpainted);

	public static Item intersect(Object[] items, float precision,
			boolean removePoints) {
		return intersect(items, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item intersect(Object[] items, float precision) {
		return intersect(items, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item intersect(Object[] items) {
		return intersect(items, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item intersect(ExtendedList items, float precision,
			boolean removePoints, boolean extractUnpainted) {
		return intersect(items.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item intersect(ExtendedList items, float precision,
			boolean removePoints) {
		return intersect(items.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item intersect(ExtendedList items, float precision) {
		return intersect(items.toArray(), precision,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item intersect(ExtendedList items) {
		return intersect(items.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// exclude

	public static native Item exclude(Object[] items, float precision,
			boolean removePoints, boolean extractUnpainted);

	public static Item exclude(Object[] items, float precision,
			boolean removePoints) {
		return exclude(items, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item exclude(Object[] items, float precision) {
		return exclude(items, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item exclude(Object[] items) {
		return exclude(items, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item exclude(ExtendedList items, float precision,
			boolean removePoints, boolean extractUnpainted) {
		return exclude(items.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item exclude(ExtendedList items, float precision,
			boolean removePoints) {
		return exclude(items.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item exclude(ExtendedList items, float precision) {
		return exclude(items.toArray(), precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item exclude(ExtendedList items) {
		return exclude(items.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// backMinusFront

	public static native Item backMinusFront(Object[] items,
			float precision, boolean removePoints, boolean extractUnpainted);

	public static Item backMinusFront(Object[] items, float precision,
			boolean removePoints) {
		return backMinusFront(items, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item backMinusFront(Object[] items, float precision) {
		return backMinusFront(items, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item backMinusFront(Object[] items) {
		return backMinusFront(items, DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item backMinusFront(ExtendedList items,
			float precision, boolean removePoints, boolean extractUnpainted) {
		return backMinusFront(items.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item backMinusFront(ExtendedList items,
			float precision, boolean removePoints) {
		return backMinusFront(items.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item backMinusFront(ExtendedList items, float precision) {
		return backMinusFront(items.toArray(), precision,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item backMinusFront(ExtendedList items) {
		return backMinusFront(items.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// frontMinusBack

	public static native Item frontMinusBack(Object[] items,
			float precision, boolean removePoints, boolean extractUnpainted);

	public static Item frontMinusBack(Object[] items, float precision,
			boolean removePoints) {
		return frontMinusBack(items, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item frontMinusBack(Object[] items, float precision) {
		return frontMinusBack(items, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item frontMinusBack(Object[] items) {
		return frontMinusBack(items, DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item frontMinusBack(ExtendedList items,
			float precision, boolean removePoints, boolean extractUnpainted) {
		return frontMinusBack(items.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item frontMinusBack(ExtendedList items,
			float precision, boolean removePoints) {
		return frontMinusBack(items.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item frontMinusBack(ExtendedList items, float precision) {
		return frontMinusBack(items.toArray(), precision,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item frontMinusBack(ExtendedList items) {
		return frontMinusBack(items.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// divide

	public static native Item divide(Object[] items, float precision,
			boolean removePoints, boolean extractUnpainted);

	public static Item divide(Object[] items, float precision,
			boolean removePoints) {
		return divide(items, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item divide(Object[] items, float precision) {
		return divide(items, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item divide(Object[] items) {
		return divide(items, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item divide(ExtendedList items, float precision,
			boolean removePoints, boolean extractUnpainted) {
		return divide(items.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item divide(ExtendedList items, float precision,
			boolean removePoints) {
		return divide(items.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item divide(ExtendedList items, float precision) {
		return divide(items.toArray(), precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item divide(ExtendedList items) {
		return divide(items.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// outline

	public static native Item outline(Object[] items, float precision,
			boolean removePoints, boolean extractUnpainted);

	public static Item outline(Object[] items, float precision,
			boolean removePoints) {
		return outline(items, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item outline(Object[] items, float precision) {
		return outline(items, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item outline(Object[] items) {
		return outline(items, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item outline(ExtendedList items, float precision,
			boolean removePoints, boolean extractUnpainted) {
		return outline(items.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item outline(ExtendedList items, float precision,
			boolean removePoints) {
		return outline(items.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item outline(ExtendedList items, float precision) {
		return outline(items.toArray(), precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item outline(ExtendedList items) {
		return outline(items.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// trim

	public static native Item trim(Object[] items, float precision,
			boolean removePoints, boolean extractUnpainted);

	public static Item trim(Object[] items, float precision,
			boolean removePoints) {
		return trim(items, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item trim(Object[] items, float precision) {
		return trim(items, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item trim(Object[] items) {
		return trim(items, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item trim(ExtendedList items, float precision,
			boolean removePoints, boolean extractUnpainted) {
		return trim(items.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item trim(ExtendedList items, float precision,
			boolean removePoints) {
		return trim(items.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item trim(ExtendedList items, float precision) {
		return trim(items.toArray(), precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item trim(ExtendedList items) {
		return trim(items.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// merge

	public static native Item merge(Object[] items, float precision,
			boolean removePoints, boolean extractUnpainted);

	public static Item merge(Object[] items, float precision,
			boolean removePoints) {
		return merge(items, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item merge(Object[] items, float precision) {
		return merge(items, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item merge(Object[] items) {
		return merge(items, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item merge(ExtendedList items, float precision,
			boolean removePoints, boolean extractUnpainted) {
		return merge(items.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item merge(ExtendedList items, float precision,
			boolean removePoints) {
		return merge(items.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item merge(ExtendedList items, float precision) {
		return merge(items.toArray(), precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item merge(ExtendedList items) {
		return merge(items.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}

	// crop

	public static native Item crop(Object[] items, float precision,
			boolean removePoints, boolean extractUnpainted);

	public static Item crop(Object[] items, float precision,
			boolean removePoints) {
		return crop(items, precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item crop(Object[] items, float precision) {
		return crop(items, precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item crop(Object[] items) {
		return crop(items, DEFAULT_PRECISION, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item crop(ExtendedList items, float precision,
			boolean removePoints, boolean extractUnpainted) {
		return crop(items.toArray(), precision, removePoints,
				extractUnpainted);
	}

	public static Item crop(ExtendedList items, float precision,
			boolean removePoints) {
		return crop(items.toArray(), precision, removePoints,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item crop(ExtendedList items, float precision) {
		return crop(items.toArray(), precision, DEFAULT_REMOVE_POINTS,
				DEFAULT_EXTRACT_UNPAINTED);
	}

	public static Item crop(ExtendedList items) {
		return crop(items.toArray(), DEFAULT_PRECISION,
				DEFAULT_REMOVE_POINTS, DEFAULT_EXTRACT_UNPAINTED);
	}
}
