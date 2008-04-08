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
 * File created on Apr 8, 2008.
 *
 * $Id$
 */

package com.scratchdisk.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author lehni
 *
 */
public class ByteArrayBuffer {

	protected byte[] buffer;
	protected int offset;
	protected boolean bigEndian;

	public ByteArrayBuffer(int length, boolean bigEndian) {
		// Create byte array of given length:
		this.bigEndian = bigEndian;
		buffer = new byte[length];
		offset = 0;
	}

	public ByteArrayBuffer(int length) {
		this(length, true);
	}

	public byte readByte() {
		return buffer[offset++];
	}

	public int readUnsignedByte() {
		return readByte() & 0xff;
	}

	public short readShort() {
		if (bigEndian) {
			return (short) (((int)(buffer[offset++] & 0xff) << 8) |
				((int)(buffer[offset++] & 0xff) << 0));
		} else {
			return (short) (((int)(buffer[offset++] & 0xff) << 0) |
				((int)(buffer[offset++] & 0xff) << 8));
		}
	}

	public int readUnsignedShort() {
		return readShort() & 0xffff;
	}

	public int readInt() {
		if (bigEndian) {
			return ((int)(buffer[offset++] & 0xff) << 24) |
				((int)(buffer[offset++] & 0xff) << 16) |
				((int)(buffer[offset++] & 0xff) << 8) |
				((int)(buffer[offset++] & 0xff) << 0);
		} else {
			return ((int)(buffer[offset++] & 0xff) << 0) |
				((int)(buffer[offset++] & 0xff) << 8) |
				((int)(buffer[offset++] & 0xff) << 16) |
				((int)(buffer[offset++] & 0xff) << 24);
		}
	}

	public long readUnsignedInt() {
		return readInt() & 0xffffffffl;
	}

	public void writeByte(byte value) {
		buffer[offset++] = value;
	}

	public void writeUnsignedByte(int value) {
		writeByte((byte) value);
	}

	public void writeShort(short value) {
		if (bigEndian) {
			buffer[offset++] = (byte) ((value >> 8) & 0xff);
			buffer[offset++] = (byte) ((value >> 0) & 0xff);
		} else {
			buffer[offset++] = (byte) ((value >> 0) & 0xff);
			buffer[offset++] = (byte) ((value >> 8) & 0xff);
		}
	}

	public void writeUnsignedShort(int value) {
		writeShort((short) value);
	}

	public void writeInt(int value) {
		if (bigEndian) {
			buffer[offset++] = (byte) ((value >> 24) & 0xff);
			buffer[offset++] = (byte) ((value >> 16) & 0xff);
			buffer[offset++] = (byte) ((value >> 8) & 0xff);
			buffer[offset++] = (byte) ((value >> 0) & 0xff);
		} else {
			buffer[offset++] = (byte) ((value >> 0) & 0xff);
			buffer[offset++] = (byte) ((value >> 8) & 0xff);
			buffer[offset++] = (byte) ((value >> 16) & 0xff);
			buffer[offset++] = (byte) ((value >> 24) & 0xff);
		}
	}

	public void writeUnsignedInt(long value) {
		writeInt((int) value);
	}

	public int readFrom(InputStream in) throws IOException {
		return in.read(buffer);
	}

	public void writeTo(OutputStream out) throws IOException {
		out.write(buffer);
	}

	public int getOffset() {
		return offset;
	}
}