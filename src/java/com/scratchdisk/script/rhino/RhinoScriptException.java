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
 * File created on Apr 10, 2007.
 */

package com.scratchdisk.script.rhino;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;
import org.mozilla.javascript.Wrapper;

import com.scratchdisk.script.ScriptException;

/**
 * ScriptException for Rhino, preferably called RhinoException, but
 * that's already used by Rhino (org.mozilla.javascript.RhinoException).
 */
public class RhinoScriptException extends ScriptException {
	private RhinoEngine engine;

	private static Throwable getCause(Throwable cause) {
		// Unwrap multiple wrapped exceptions, but make sure we have one
		// WrappedException that contains information about script and line number.
		if (cause instanceof WrappedException) {
			Throwable wrapped = ((WrappedException) cause).getWrappedException();
			// Unwrap wrapped RhinoScriptExceptions if wrapped more than once
			if (wrapped instanceof RhinoScriptException)
				cause = ((RhinoScriptException) wrapped).getCause();
			// Unwrapped multiply wrapped Rhino Exceptions
			if (wrapped instanceof RhinoException)
				cause = wrapped;
		} else if (cause instanceof JavaScriptException) {
			JavaScriptException jse = (JavaScriptException) cause;
			Object value = jse.getValue();
			if (value instanceof Wrapper) {
				value = ((Wrapper) value).unwrap();
			} else if (value instanceof Scriptable) {
				value = ScriptableObject.getProperty((Scriptable) value, "exception");
			}
			if (value instanceof Throwable)
				return (Throwable) value;
		}
		return cause;
	}

	private static String getMessage(Throwable cause) {
		// Do not use RhinoException#getMessage for short messages
		// since it adds line number information. Use #details instead.
		if (cause instanceof RhinoException) {
			return ((RhinoException) cause).details();
		} else {
			return cause.getMessage();
		}
	}

	public String getFullMessage() {
		Throwable cause = getCause();
		String separator = System.getProperty("file.separator");
		File baseDir = engine.getBaseDirectory();
		String base = baseDir != null ?
				baseDir.getAbsolutePath() + separator : null;
		if (cause instanceof RhinoException) {
			RhinoException re = (RhinoException) cause;
			StringWriter buf = new StringWriter();
			PrintWriter writer = new PrintWriter(buf);
			if (re instanceof WrappedException) {
				// Make sure we're not printing the "Wrapped ...Exception:" part
				writer.println(((WrappedException) re).getWrappedException().getMessage());
			} else {
				writer.println(re.details());
			}
			String[] stackTrace = re.getScriptStackTrace().split("[\\n\\r]");
			String sourceName = re.sourceName();
			if (sourceName != null) {
				int lineNumber = re.lineNumber();
				// Report sourceName / lineNumber if it is not in the stack trace already.
				// Why is this needed? Rhino bug?
				if (stackTrace.length == 0 || stackTrace[0].indexOf(sourceName + ":" + lineNumber) == -1) {
					if (base != null && sourceName.startsWith(base))
						sourceName = sourceName.substring(base.length());
					writer.println("\tat " + sourceName + ":" + lineNumber);
				}
			}
			// Parse the lines for filename:linenumber
			Pattern pattern = Pattern.compile("\\s+at\\s+([^:]+):(\\d+)");
			for (int i = 0; i < stackTrace.length; i++) {
				String line = stackTrace[i];
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					String file = matcher.group(1);
					// Filter out hidden scripts. Only report scripts
					// that are located in base:
					if ((base == null || file.startsWith(base)) &&
							file.indexOf(separator + "__") == -1) {
						String number = matcher.group(2);
						writer.println("\tat " + file + ":" + number);
					}
				}
			}
			return buf.toString().trim();
		} else {
			/*
			if (cause instanceof EvaluatorException) {
				return cause.getMessage();
			} else {
			}
			*/
			String message = cause.getMessage();
			String error = cause.getClass().getSimpleName();
			if (message != null && message.length() != 0)
				error += ": " + message;
			return error;
		}
	}

	public Throwable getWrappedException() {
		Throwable cause = getCause();
		if (cause instanceof WrappedException)
			cause = ((WrappedException) cause).getWrappedException();
		return cause;
	}
	
	public RhinoScriptException(RhinoEngine engine, Throwable cause) {
		super(getMessage(getCause(cause)), getCause(cause));
		this.engine = engine;
	}
}
