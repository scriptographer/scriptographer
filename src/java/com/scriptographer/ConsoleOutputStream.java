/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 07.12.2004.
 *
 * $Id$
 */

package com.scriptographer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author lehni
 */
public class ConsoleOutputStream extends OutputStream {
	/**
	 * the singleton object
	 */
	private static ConsoleOutputStream console = new ConsoleOutputStream();
	
	/**
	 * some constants
	 */
	private static final String lineSeparator = 
		System.getProperty("line.separator");
    private static final char newLine =
    	lineSeparator.charAt(lineSeparator.length() - 1);

    private boolean enabled;

	private StringBuffer buffer;
	private PrintStream stream;
	private PrintStream stdOut;
	private PrintStream stdErr;
	private ConsoleOutputWriter writer;

	private ConsoleOutputStream() {
		buffer = new StringBuffer();
		stream = new PrintStream(this);
		stdOut = System.out;
		stdErr = System.err;
		enabled = false;
	}

	/**
	 * Adds chars to the internal StringBuffer until a new line char is
	 * detected, in which case the collected line is written to the
	 * native console window through writeLine.
	 * 
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int b) throws IOException {
		char c = (char) b;
		if (c == newLine) {
			if (enabled) {
				// if there is already a newline at the end of this line, remove
				// it as writer.println adds it again...
				int pos = buffer.lastIndexOf(lineSeparator);
				int sepLength = lineSeparator.length();
				if (pos > 0 && pos == buffer.length() - sepLength)
					buffer.delete(pos, pos + sepLength);
				writer.println(buffer.toString());
				buffer.setLength(0);
			} else {
				buffer.append(lineSeparator);
			}
		} else {
			buffer.append(c);
		}
	}
	
	public static void enableOutput(boolean enabled) {
		console.enabled = enabled && console.writer != null;
		if (console.enabled && console.buffer.length() > 0) {
			try {
				// write a newline character so the buffer is flushed to the
				// console
				console.write(newLine);
			} catch (IOException e) {
				// never happens!
			}
		}
	}
	
	public static void enableRedirection(boolean enable) {
		if (enable) {
			System.setOut(console.stream);
			System.setErr(console.stream);
		} else {
			System.setOut(console.stdOut);
			System.setErr(console.stdErr);
		}
	}
	

	public static void setWriter(ConsoleOutputWriter writer) {
		console.writer = writer;
		enableOutput(true);
	}
}