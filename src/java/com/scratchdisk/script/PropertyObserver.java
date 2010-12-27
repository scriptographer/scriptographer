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
 * File created on May 4, 2010.
 */

package com.scratchdisk.script;

import java.util.Map;

/**
 * @author lehni
 *
 */
public interface PropertyObserver {
	public void onChangeProperty(Map object, Object key, Object value);
}
