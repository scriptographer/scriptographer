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
 * File created on May 12, 2008.
 *
 * $Id$
 */

package com.scratchdisk.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author lehni
 *
 */
public class DataOutputStream extends FilterOutputStream {
	private boolean bigEndian;

	public DataOutputStream(OutputStream out, boolean bigEndian) {
		super(out);
		this.bigEndian = bigEndian;
	}

	public DataOutputStream(OutputStream out) {
		this(out, true);
	}

	public void writeUnsignedByte(int value) throws IOException {
		out.write(value);
	}

	public void writeByte(byte value) throws IOException {
		out.write(value);
	}

	public void writeUnsignedShort(int value) throws IOException {
		if (bigEndian) {
			out.write((value >> 8) & 0xff);
			out.write((value >> 0) & 0xff);
		} else {
			out.write((value >> 0) & 0xff);
			out.write((value >> 8) & 0xff);
		}
	}

	public void writeShort(short value) throws IOException {
		writeUnsignedShort(value);
	}

	public void writeUnsignedInt(long value) throws IOException {
		if (bigEndian) {
			out.write(((int) value >> 24) & 0xff);
			out.write(((int) value >> 16) & 0xff);
			out.write(((int) value >> 8) & 0xff);
			out.write(((int) value >> 0) & 0xff);
		} else {
			out.write(((int) value >> 0) & 0xff);
			out.write(((int) value >> 8) & 0xff);
			out.write(((int) value >> 16) & 0xff);
			out.write(((int) value >> 24) & 0xff);
		}
	}

	public void writeInt(int value) throws IOException {
		writeUnsignedInt(value);
	}

	public void writeChar(char value) throws IOException {
		writeUnsignedShort(value);
	}
}
