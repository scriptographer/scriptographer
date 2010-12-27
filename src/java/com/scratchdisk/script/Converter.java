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
 * File created on Feb 12, 2008.
 */

package com.scratchdisk.script;

/**
 * @author lehni
 *
 */
public interface Converter {
	public <T> T convert(Object from, Class<T> to);

	public Object unwrap(Object obj);

	public void setProperties(Object object, ArgumentReader properties);
}
