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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -- GPL LICENSE NOTICE --
 * 
 * File created on 10.06.2006.
 * 
 * $Id$
 */

package com.scriptographer.sg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;

/**
 * @author lehni
 * 
 * @jshide
 */

public class File extends java.io.File {
	private BufferedReader reader = null;
	private PrintWriter writer = null;
    private boolean eof = false;
    private String lastLine = null;

	public File(String pathname) {
		super(pathname);
	}

	public File(String parent, String child) {
		super(parent, child);
	}

	public File(java.io.File parent, String child) {
		super(parent, child);
	}

	public File(URI uri) {
		super(uri);
	}

	public boolean remove() throws IOException {
		this.close();
		return delete();
	}

	/**
	 * Returns the length of the file denoted by this abstract pathname. The
	 * return value is unspecified if this pathname denotes a directory.
	 * 
	 * @return The length, in bytes, of the file denoted by this abstract
	 *pathname, or {@code 0L} if the file does not exist
	 */
	public long getLength() {
		return length();
	}

	public void open() throws IOException {
		if (this.isOpened())
			throw new IllegalStateException("File already open");

		// We assume that the BufferedReader and PrintWriter creation
		// cannot fail except if the FileReader/FileWriter fails.
		// Otherwise we have an open file until the reader/writer
		// get garbage collected.
		if (this.exists()) {
			reader = new BufferedReader(new FileReader(this));
		} else {
			writer = new PrintWriter(new FileWriter(this));
		}
		lastLine = null;
	}

	public boolean isOpened() {
		return reader != null || writer != null;
	}

	public void close() throws IOException {
		if (reader != null)
			reader.close();
		if (writer != null)
			writer.close();
		reader = null;
		writer = null;
		lastLine = null;
	}

	public void write(Object what) {
		if (writer == null)
			throw new IllegalStateException("File not opened for writing");
		if (what != null)
			writer.print(what.toString());
	}

	public void writeln(Object what) {
		this.write(what);
		// If we're still here, writer is set
		writer.println();
	}

	public void writeln() {
		if (writer == null)
			throw new IllegalStateException("File not opened for writing");
		writer.println();
	}

	public void flush() {
		if (writer == null)
			throw new IllegalStateException("File not opened for writing");
		writer.flush();
    }

	public String readln() throws IOException {
		if (reader == null)
			throw new IllegalStateException("File not opened for reading");
		if (eof)
			return null;
		if (lastLine != null) {
			String line = lastLine;
			lastLine = null;
			return line;
		}
		// Here lastLine is null, return a new line
		String line = reader.readLine();
		if (line == null)
			eof = true;
		return line;
	}

	public boolean isEof() throws IOException {
		if (reader == null)
			throw new IllegalStateException("File not opened for reading");
		if (eof) return true;
		if (lastLine != null)
			return false;
		lastLine = reader.readLine();
		eof = lastLine == null;
		return eof;
	}

	public String readAll() throws IOException {
		BufferedReader reader;
		if (this.exists()) {
			reader = new BufferedReader(new FileReader(this));
		} else {
			throw new IllegalStateException("File does not exist");
		}
		if(!this.isFile())
			throw new IllegalStateException("File is not a regular file");

		// Read content line by line to setup proper eol
		StringBuffer buffer = new StringBuffer((int) (this.length() * 1.10));
		while (true) {
		String line = reader.readLine();
		if (line == null)
			break;
		buffer.append(line);
		buffer.append("\n");// EcmaScript EOL
		}
		// Close the file
		reader.close();
		return buffer.toString();
	}
}