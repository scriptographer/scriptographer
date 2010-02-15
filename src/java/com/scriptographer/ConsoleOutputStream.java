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
 * File created on 07.12.2004.
 *
 * $Id:ConsoleOutputStream.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer;

import java.io.ByteArrayOutputStream;
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

    // Use a ByteArrayOutputStream instead of a StringBuffer,
    // since we receive print(int) with bytes in the platform
    // encoding, not chars.
    private ByteArrayOutputStream buffer;
	private PrintStream stream;
	private PrintStream stdOut;
	private PrintStream stdErr;
	private ScriptographerCallback callback = null;

	private ConsoleOutputStream() {
		buffer = new ByteArrayOutputStream();
		stream = new PrintStream(this);
		stdOut = System.out;
		stdErr = System.err;
		enabled = false;
	}

	private boolean receivedHeadlessError = false;
	/**
	 * Adds chars to the internal StringBuffer until a new line char is
	 * detected, in which case the collected line is written to the native
	 * console window through writeLine.
	 * 
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int b) throws IOException {
		// Detect the end of a received headless error. The only way to tell is 
		// once we're receiving a new text from the main thread again.
		if (receivedHeadlessError && ScriptographerEngine.isMainThreadActive()) {
			ScriptographerEngine.logError(buffer.toString());
			buffer.reset();
			receivedHeadlessError = false;
		}
		char c = (char) b;
		if (c != 0) // Do not write out 0 chars, since they mess up the client side
			buffer.write(c);
		if (c == newLine) {
			// Only print to the console if we're in the right thread to prevent UI crashes.
			if (ScriptographerEngine.isMainThreadActive()) {
				if (enabled) {
					String str = buffer.toString();
					buffer.reset();
					// If there already is a newline at the end of this line,
					// remove it as callback.println adds it again...
					int pos = str.lastIndexOf(lineSeparator);
					if (pos > 0 && pos == str.length() - lineSeparator.length())
						str = str.substring(0, pos);
					// Make sure we have the right line separators:
					str = str.replaceAll("\\n|\\r\\n|\\r", lineSeparator);
					// And convert tabs to 4 spaces
					str = str.replaceAll("\\t", "    ");
					ScriptographerEngine.logConsole(str);
					callback.println(str);
				}
			} else if (ScriptographerEngine.isMacintosh() && !receivedHeadlessError) {
				// Filter out weird java.lang.ClassCastException: sun.java2d.HeadlessGraphicsEnvironment on OSX 10.5
				receivedHeadlessError = buffer.toString().indexOf(
						"java.lang.ClassCastException: sun.java2d.HeadlessGraphicsEnvironment") != -1;
			}
		}
	}

	public static void enableOutput(boolean enabled) {
		console.enabled = enabled && console.callback != null;
		if (console.enabled && console.buffer.size() > 0) {
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

	protected static void setCallback(ScriptographerCallback callback) {
		console.callback = callback;
		enableOutput(callback != null);
	}
}