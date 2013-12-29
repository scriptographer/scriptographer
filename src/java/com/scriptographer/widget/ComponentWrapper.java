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
 * File created on Feb 9, 2008.
 */

package com.scriptographer.widget;

import com.scriptographer.widget.Component;

/**
 * An interface to allow the various component wrappers to return
 * their component in a standardized way.
 * 
 * @author lehni
 */
interface ComponentWrapper {
	Component getComponent();
}
