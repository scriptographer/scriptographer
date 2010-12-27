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
 * File created on Feb 17, 2007.
 */

package com.scriptographer.ai;

/**
 * The only purpose for this interface is to give a hint to the scripting layer
 * as to how to deal with instances of this interface. Basically it means that
 * they need to convert null to and from Color.NONE / FontWeight.NONE, when
 * settings colors / fonts.
 * 
 * @author lehni
 * 
 * @jshide
 */
public interface Style {
	
}
