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
 * File created on 07.03.2005.
 * 
 * $RCSfile: Test.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/03/07 13:36:38 $
 */

package com.scriptographer;

public class Test {
	public static int testInternal(int a, int b, int c, int d, int e, int f) {
		return a + b + c + d + e + f;
	}

	public native static int testExternal(int a, int b, int c, int d, int e, int f);

	public static void speedTest() {
		int count = 10000000;
		long t1 = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			testInternal(i, i, i, i, i, i);
		}
		long t2 = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			testExternal(i, i, i, i, i, i);
		}
		long t3 = System.currentTimeMillis();
		System.out.println((t2 - t1) + " VS " + (t3 - t2) + " " + ((double)(t3 - t2) /( double)(t2 - t1)));
	}
}
