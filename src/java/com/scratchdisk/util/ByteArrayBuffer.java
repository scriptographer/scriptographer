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
 * File created on Apr 8, 2008.
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
	protected int length;
	protected boolean bigEndian;

	public ByteArrayBuffer(int length, boolean bigEndian) {
		// Create byte array of given length:
		this.length = length;
		this.buffer = new byte[length];
		this.bigEndian = bigEndian;
		this.offset = 0;
	}

	public ByteArrayBuffer(int length) {
		this(length, true);
	}

	public ByteArrayBuffer(boolean bigEndian) {
		this(0, bigEndian);
	}

	public ByteArrayBuffer() {
		this(0, true);
	}

	public byte readByte() {
		return buffer[offset++];
	}

	public int readUnsignedByte() {
		return readByte() & 0xff;
	}

	public short readShort() {
		if (bigEndian) {
			return (short) (((buffer[offset++] & 0xff) << 8) |
				((buffer[offset++] & 0xff) << 0));
		} else {
			return (short) (((buffer[offset++] & 0xff) << 0) |
				((buffer[offset++] & 0xff) << 8));
		}
	}

	public int readUnsignedShort() {
		return readShort() & 0xffff;
	}

	public int readInt() {
		if (bigEndian) {
			return ((buffer[offset++] & 0xff) << 24) |
				((buffer[offset++] & 0xff) << 16) |
				((buffer[offset++] & 0xff) << 8) |
				((buffer[offset++] & 0xff) << 0);
		} else {
			return ((buffer[offset++] & 0xff) << 0) |
				((buffer[offset++] & 0xff) << 8) |
				((buffer[offset++] & 0xff) << 16) |
				((buffer[offset++] & 0xff) << 24);
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

	public void fill(int length, byte value) {
		int next = offset + length;
		for (int i = offset; i < next; i++)
			buffer[i] = value;
		offset = next;
	}

	public int readFrom(InputStream in) throws IOException {
		return in.read(buffer, offset, length - offset);
	}

	public void writeTo(OutputStream out) throws IOException {
		out.write(buffer, 0, length);
	}

	public int readFrom(ByteArrayBuffer in, int offset, int length) {
		if (this.offset + length > this.length)
			length = this.length - this.offset;
		if (length > 0)
			System.arraycopy(in.buffer, offset, this.buffer, this.offset, length);
		return length < 0 ? -1 : length;
	}

	public int readFrom(ByteArrayBuffer in) {
		return readFrom(in, 0, in.length);
	}

	public int writeTo(ByteArrayBuffer out, int offset, int length) {
		return out.readFrom(this, offset, length);
	}

	public int writeTo(ByteArrayBuffer out) {
		return writeTo(out, 0, length);
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		if (length > buffer.length) {
			byte[] newBuffer = new byte[length];
			System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
			buffer = newBuffer;
		}
		this.length = length;
	}

	public String toString() {
		String str = "[";
		for (int i = 0; i < length; i++) {
			if (i > 0)
				str += ' ';
			String part = Integer.toHexString(buffer[i] & 0xff);
			if (part.length() == 1)
				str += '0';
			str += part;
		}
		str += ']';
		return str;
	}
}