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
import com.scratchdisk.util.StringUtils;

/**
 * ScriptException for Rhino, preferably called RhinoException, but
 * that's already used by Rhino (org.mozilla.javascript.RhinoException).
 */
public class RhinoScriptException extends ScriptException {
	private RhinoEngine engine;

	private static Throwable getCause(Throwable cause) {
		// Unwrap multiple wrapped exceptions, but make sure we have one
		// WrappedException that contains information about script and line
		// number.
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
				value = ScriptableObject.getProperty((Scriptable) value,
						"exception");
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
		if (cause instanceof RhinoException) {
			RhinoException re = (RhinoException) cause;
			StringWriter buf = new StringWriter();
			PrintWriter writer = new PrintWriter(buf);
			if (re instanceof WrappedException) {
				// Make sure we're not printing the "Wrapped ...Exception:" part
				writer.println(((WrappedException) re).getWrappedException()
						.getMessage());
			} else {
				writer.println(re.details());
			}
			String[] stackTrace = re.getScriptStackTrace().split("\\r\\n|\\n|\\r");
			String sourceName = re.sourceName();
			if (sourceName != null) {
				int lineNumber = re.lineNumber();
				// Report sourceName / lineNumber if it is not in the stack
				// trace already.
				// TODO Why is this needed? Rhino bug?
				if (stackTrace.length == 0 || stackTrace[0].indexOf(
						sourceName + ":" + lineNumber) == -1) {
					String[] path = engine.getScriptPath(new File(sourceName));
					if (path != null)
						writer.println("\tat "
								+ StringUtils.join(path, separator)
								+ ":" + lineNumber);
				}
			}
			// Parse the lines for filename:linenumber
			Pattern pattern = Pattern.compile("\\s+at\\s+(.+):(\\d+)");
			for (int i = 0; i < stackTrace.length; i++) {
				String line = stackTrace[i];
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					String file = matcher.group(1);
					// Filter out hidden scripts. Only report scripts
					// that are located in base:
					if (file.indexOf(separator + "__") == -1) {
						String[] path = engine.getScriptPath(new File(file));
						if (path != null) {
							writer.println("\tat "
									+ StringUtils.join(path, separator) + ":"
									+ matcher.group(2));
						}
					}
				}
			}
			return buf.toString().trim();
		} else {
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
