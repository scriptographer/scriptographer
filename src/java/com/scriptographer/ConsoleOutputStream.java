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
 * File created on 07.12.2004.
 *
 * $RCSfile: ConsoleOutputStream.java,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/03/07 13:36:51 $
 */

package com.scriptographer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ConsoleOutputStream extends OutputStream {
	private static ConsoleOutputStream console;
	private boolean enabled = false;
	private static final String lineSeparator = System.getProperty("line.separator");
    private static final char newLine = lineSeparator.charAt(lineSeparator.length() - 1);

	private StringBuffer buffer;
	private PrintStream stream;
	private PrintStream stdOut;
	private PrintStream stdErr;
	private ConsoleOutputWriter writer;

	public ConsoleOutputStream() {
		buffer = new StringBuffer();
		stream = new PrintStream(this);
		stdOut = System.out;
		stdErr = System.err;
	}
	
	public static ConsoleOutputStream getInstance() {
		if (console == null)
			console = new ConsoleOutputStream();
		
		return console;
	}
	
	public void enableRedirection(boolean enable) {
		if (enable) {
			System.setOut(stream);
			System.setErr(stream);
		} else {
			System.setOut(stdOut);
			System.setErr(stdErr);
		}
	}

	public void setWriter(ConsoleOutputWriter writer) {
		this.writer = writer;
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
				int pos = buffer.lastIndexOf(lineSeparator);
				int sepLength = lineSeparator.length();
				// if there is already a newline at the end of this line, remove it
				// as writeLine adds it again...
				if (pos > 0 && pos == buffer.length() - sepLength)
					buffer.delete(pos, pos + sepLength);
				// writeLine(buffer.toString());
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
		ConsoleOutputStream console = getInstance();
		console.enabled = enabled && console.writer != null;
		if (console.enabled && console.buffer.length() > 0) {
			try {
				// write a newline character so the buffer is flushed to the console
				console.write(newLine);
			} catch (IOException e) {
				// never happens!
			}
		}
	}
}