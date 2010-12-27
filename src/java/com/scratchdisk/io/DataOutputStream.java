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
