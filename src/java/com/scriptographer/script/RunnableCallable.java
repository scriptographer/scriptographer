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
 * File created on Jul 23, 2009.
 */

package com.scriptographer.script;

import com.scratchdisk.script.Callable;
import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 *
 */
public class RunnableCallable implements Runnable {

	private Callable callable;
	private Object bind;

	public RunnableCallable(Callable callable, Object bind) {
		this.callable = callable;
		this.bind = bind;
	}

	public void run() {
		ScriptographerEngine.invoke(callable, bind);
	}
}
