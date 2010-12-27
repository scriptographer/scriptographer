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
 * File created on May 12, 2008.
 */

package com.scratchdisk.io;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author lehni
 *
 */
public class DataInputStream extends FilterInputStream {
	protected boolean bigEndian;

	public DataInputStream(InputStream in, boolean bigEndian) {
		super(in);
		this.bigEndian = bigEndian;
	}

	public DataInputStream(InputStream in) {
		this(in, true);
	}

	public int readUnsignedByte() throws IOException {
		int b = in.read();
		if (b < 0)
			throw new EOFException();
		return b;
	}

	public byte readByte() throws IOException {
		return (byte) readUnsignedByte();
	}

	public int readUnsignedShort() throws IOException {
		int b1 = in.read();
		int b2 = in.read();
		if ((b1 | b2) < 0)
			throw new EOFException();
		if (bigEndian) {
			return ((b1 << 8) | 
					(b2 << 0));
		} else {
			return ((b1 << 0) | 
					(b2 << 8));
		}
	}

	public short readShort() throws IOException {
		return (short) readUnsignedShort();
	}

	public long readUnsignedInt() throws IOException {
		int b1 = in.read();
		int b2 = in.read();
		int b3 = in.read();
		int b4 = in.read();
		if ((b1 | b2 | b3 | b4) < 0)
			throw new EOFException();
		if (bigEndian) {
			return (((long) b1 << 24) |
					((long) b2 << 16) |
					((long) b3 << 8) |
					((long) b4 << 0));
		} else {
			return (((long) b1 << 0) |
					((long) b2 << 8) |
					((long) b3 << 16) |
					((long) b4 << 24));
		}
	}

	public int readInt() throws IOException {
		return (int) readUnsignedInt();
	}

	public char readChar() throws IOException {
		return (char) readUnsignedShort();
	}
}
