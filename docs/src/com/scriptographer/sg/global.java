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
 * File created on 10.06.2006.
 */

package com.scriptographer.sg;

import com.scriptographer.ai.Tool;
import com.scriptographer.ai.DocumentList;
import com.scriptographer.ai.Document;

/*
 * This class is just a dummy to get Javadoc to generate documentation for
 * the Global Scope
 */

/**
 * Objects and functions present in the Global Scope. These can be used anywhere
 * in scripts.
 * 
 * @author lehni
 */
public class global {

	private global() {
	}

	/**
	 * The global scriptographer object.
	 */
	public Scriptographer scriptographer;

	/**
	 * The global illustrator object.
	 */
	public Illustrator illustrator;
	
	/**
	 * The global documents array.
	 */
	public DocumentList documents;

	/**
	 * The global active document object.
	 */
	public Document document;

	/**
	 * The global script object.
	 */
	public Script script;

	/**
	 * The reference to the tool object. This is only set when a tool script is
	 * executed and assigned with the Scriptographer drawing tool.
	 */
	public Tool tool;

	/**
	 * Prints one or more passed parameters to the console, seperated by white
	 * space.
	 */
	public void print() {
	}

	/**
	 * Loads and evaluates one or more javascript files.
	 * <p>e.g:
	 * <pre>include("raster.js", "mysql.js");</pre>
	 */
	public void include() {
	}
}
