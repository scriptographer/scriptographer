/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 16.03.2005.
 *
 * $Id$
 */

package com.scriptographer.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.WrapFactory;

import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 */
public class ScriptographerContextFactory extends ContextFactory {
	
	protected boolean hasFeature(Context cx, int featureIndex) {
		switch (featureIndex) {
			case Context.FEATURE_E4X:
				return false;
			case Context.FEATURE_MEMBER_EXPR_AS_FUNCTION_NAME:
				return true;
			case Context.FEATURE_DYNAMIC_SCOPE:
				return true;
		}
		return super.hasFeature(cx, featureIndex);
	}

    protected Context makeContext() {
        Context context = new Context();

		WrapFactory wrapper = new ScriptographerWrapFactory();
		wrapper.setJavaPrimitiveWrap(false);
		context.setApplicationClassLoader(getClass().getClassLoader());
		context.setWrapFactory(wrapper);

//		context.setOptimizationLevel(9);
        // Use pure interpreter mode to allow for
        // observeInstructionCount(Context, int) to work
        context.setOptimizationLevel(-1);
        // Make Rhino runtime to call observeInstructionCount
        // each 10000 bytecode instructions
        context.setInstructionObserverThreshold(20000);
        
        return context;
    }
    
    public static class ScriptCanceledException extends RuntimeException {
    }

    protected void observeInstructionCount(Context cx, int instructionCount) {
		if (!ScriptographerEngine.updateProgress())
			throw new ScriptCanceledException();
    }
}
