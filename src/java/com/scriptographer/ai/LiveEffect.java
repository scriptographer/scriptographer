/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 18.02.2005.
 * 
 * $RCSfile: LiveEffect.java,v $
 * $Author: lehni $
 * $Revision: 1.6 $
 * $Date: 2005/04/04 17:06:16 $
 */

package com.scriptographer.ai;

import com.scriptographer.js.FunctionHelper;
import com.scriptographer.js.Unsealed;
import com.scriptographer.util.Handle;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptRuntime;

/**
 * Wrapper for Illustrator's LiveEffects.
 *
 * Unfortunatelly, Illustrator is not able to remove once created effects again until the next restart.
 * They can be removed from the menu but not from memory. So In order to recycle effects with the
 * same settings, e.g. during development, where the code often changes but the initial settings
 * maybe not, keep track of all existing effects and match against those first before creating a new one.
 *
 * Also, when Scriptographer is (re)loaded, the list of existing effects needs to be walked through and
 * added to the list of unusedEffects. This is done by calling getUnusedEffects.
 */


/*
Re: sdk: Illustrator AILiveEffect questions
Datum: 23. Februar 2005 18:53:13 GMT+01:00

In general, live effects should run on all the art it is given. In the first example of running a post effect on a
path, the input art is split into two objects: one path that contains just the stroke attributes and another path that
contains just the fill color. Input art is typically split up this way when effects are involved.

If the effect is dragged before any of the fill/stroke layers in the appearance palette, then the input path the effect
will see will just be one path, since it has not been split up yet. However, the input path will contain no paint,
since it has not gone through the fill/stroke layers yet.

In the example of running a post effect on a group with two paths, the input art will be split up again in order to go
through the fill/stroke layers and the "Contents" layer. Thus, you will see three copies of the group: one that may
just be filled, one that may just be stroked, and one that contains the original group unchanged.

Because of this redundancy, it is more optimal to register effects as pre effects, if the effect does not care about
the paint on the input path. For example, the Roughen effect in Illustrator is registered as a pre effect since it
merely roughens the geometry, regardless of the paint. On the other hand, drop shadow is registered as a post effect,
since its results depend on the paint applied to the input objects.

While there is redundancy, effects really cannot make any assumptions about the input art they are given and should
thus attempt to operate on all of the input art. At the end of executing an entire appearance, Illustrator will attempt
to "clean up" and remove any unnecessary nested groups and unpainted paths.

When creating output art for the go message, the output art must be a child of the same parent as the input art. It
also must be the only child of this parent, so if you create a copy of the input art, work on it and attempt to return
the copy as the output art, you must make sure to dispose the original input art first. It is not legal to create an
art object in an arbitrary place and return that as the output art.

Effects are limited in the kinds of attributes that they can attach to the output art. Effects must restrict themselves
to using "simple" attributes, such as:
- 1 fill and 1 stroke (AIPathStyle)
- transparency options (AIBlendStyle)
It is actually not necessary to use the AIArtStyle suite when generating output art, and effects should try to avoid
it. Effects also should avoid putting properties on output art that will generate more styled art (ie. nested styled
art is not allowed).

I suggest playing around with the TwirlFilterProject in Illustrator and expanding the appearance to get a better
picture of the live effect architecture.

Hope that helps,
-Frank
*/
public class LiveEffect extends AIObject implements Unsealed {
	// AIStyleFilterPreferredInputArtType
	public final static int
		INPUT_DYNAMIC	 		= 0,
		INPUT_GROUP 			= 1 << (Art.TYPE_GROUP - 1),
		INPUT_PATH 				= 1 << (Art.TYPE_PATH - 1),
		INPUT_COMPOUNDPATH 		= 1 << (Art.TYPE_COMPOUNDPATH - 1),

		INPUT_PLACED 			= 1 << (Art.TYPE_PLACED - 1),
		INPUT_MYSTERYPATH 		= 1 << (Art.TYPE_MYSTERYPATH - 1),
		INPUT_RASTER 			= 1 << (Art.TYPE_RASTER - 1),

		// If INPUT_PLUGIN is not specified, the filter will receive the result group of a plugin
		// group instead of the plugin group itself
		INPUT_PLUGIN			= 1 << (Art.TYPE_PLUGIN - 1),
		INPUT_MESH 				= 1 << (Art.TYPE_MESH - 1),

		INPUT_TEXTFRAME 		= 1 << (Art.TYPE_TEXTFRAME - 1),

		INPUT_SYMBOL 			= 1 << (Art.TYPE_SYMBOL - 1),

		INPUT_FOREIGN			= 1 << (Art.TYPE_FOREIGN - 1),
		INPUT_LEGACYTEXT		= 1 << (Art.TYPE_LEGACYTEXT - 1),

		// Indicates that the effect can operate on any input art. */
		INPUT_ANY 				= 0xfff,
		// Indicates that the effect can operate on any input art other than plugin groups which
		// are replaced by their result art.
		INPUT_ANY_BUT_PLUGIN	= INPUT_ANY & ~INPUT_PLUGIN,

		// Special values that don't correspond to regular art types should be in the high half word

		// Wants strokes to be converted to outlines before being filtered (not currently implemented)
		INPUT_OUTLINED_STROKE	= 0x10000,
		// Doesn't want to take objects that are clipping paths or clipping text (because it destroys them,
		// e.g. by rasterizing, or by splitting a single path into multiple non-intersecting paths,
		// or by turning it into a plugin group, like the brush filter).
		// This flag is on for "Not OK" instead of on for "OK" because destroying clipping paths is
		// an exceptional condition and we don't want to require normal filters to explicitly say they're OK.
		// Also, it is not necessary to turn this flag on if you can't take any paths at all.
		INPUT_NO_CLIPMASKS		= 0x20000;

	//AIStyleFilterFlags
	public final static int
		TYPE_DEFAULT = 0,
		TYPE_PRE_EFFECT = 1,
		TYPE_POST_EFFECT = 2,
		TYPE_STROKE = 3,
		TYPE_FILL = 4;
	
	public final static int
		FLAG_NONE						= 0,
		FLAG_SPECIAL_GROU_PRE_FILTER 	= 0x010000,
		FLAG_HAS_SCALABLE_PARAMS 		= 0x020000,
		FLAG_USE_AUTO_RASTARIZE 		= 0x040000,
		FLAG_CAN_GENERATE_SVG_FILTER	= 0x080000;

	private String name;
	private String title;
	private int preferedInput;
	private int type;
	private int flags;
	private int majorVersion;
	private int minorVersion;
	private MenuItem menuItem = null;

	/**
	 * effects maps effectHandles to their wrappers.
	 */
	private static HashMap effects = new HashMap();
	private static ArrayList unusedEffects = null;

	/**
	 * Called from the native environment.
	 */
	protected LiveEffect(int effectHandle, String name, String title, int preferedInput, int type, int flags, int majorVersion, int minorVersion) {
		super(effectHandle);
		this.name = name;
		this.title = title;
		this.preferedInput = preferedInput;
		this.type = type;
		this.flags = flags;
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
	}

	/**
	 *
	 * @param name
	 * @param title
	 * @param preferedInput a combination of LiveEffect.INPUT_*
	 * @param type one of LiveEffect.TYPE_
	 * @param flags a combination of LiveEffect.FLAG_*
	 * @param majorVersion
	 * @param minorVersion
	 */
	public LiveEffect(String name, String title, int preferedInput, int type, int flags, int majorVersion, int minorVersion) {
		this(0, name, title, preferedInput, type, flags, majorVersion, minorVersion);

		ArrayList unusedEffects = getUnusedEffects();

		// now see first wether there is an unusedEffect already:
		int index = unusedEffects.indexOf(this);
		if (index >= 0) {
			// found one, let's reuse it's handle and remove the old effect from the list:
			LiveEffect effect = (LiveEffect) unusedEffects.get(index);
			handle = effect.handle;
			effect.handle = 0;
			unusedEffects.remove(index);
		} else {
			// no previously existing effect found, create a new one:
			handle = nativeCreate(name, title, preferedInput, type, flags, majorVersion, minorVersion);
		}

		if (handle == 0)
			throw new RuntimeException("Unable to create LifeEffect");

		effects.put(new Handle(handle), this);
	}

	/**
	 * Same constructor, but name is used for title and name.
	 * 
	 * @param name
	 * @param preferedInput a combination of LiveEffect.INPUT_*
	 * @param type one of LiveEffect.TYPE_
	 * @param flags a combination of LiveEffect.FLAG_*
	 * @param majorVersion
	 * @param minorVersion
	 */
	public LiveEffect(String name, int preferedInput, int type, int flags, int majorVersion, int minorVersion) {
		this(name, name, preferedInput, type, flags, majorVersion, minorVersion);
	}

	private native int nativeCreate(String name, String title, int preferedInput, int type, int flags, int majorVersion, int minorVersion);

	/**
	 * "remove's" the effect. there is no real destroy for LiveEffects in Illustrator, so all it really does is remove
	 * the effect's menu item, if there is one. It keeps the effectHandle and puts itself in the list of unused effects
	 */
	public void remove() {
		Handle key = new Handle(handle);
		// see wether we're still linked:
		if (effects.get(key) == this) {
			// if so remove it and put it to the list of unsed effects, for later recycling
			effects.remove(key);
			getUnusedEffects().add(this);
			if (menuItem != null)
				menuItem.remove();
			menuItem = null;
		}
	}

	public static void removeAll() {
		// as remove() modifies the map, using an iterator is not possible here:
		Object[] effects = LiveEffect.effects.values().toArray();
		for (int i = 0; i < effects.length; i++) {
			((LiveEffect) effects[i]).remove();
		}
	}

	private native MenuItem nativeAddMenuItem(String name, String category, String title);

	public MenuItem addMenuItem(String cateogry, String title) {
		if (menuItem == null) {
			menuItem = nativeAddMenuItem(name, cateogry, title);
			return menuItem;
		}
		return null;
	}

	public MenuItem getMenuItem() {
		return menuItem;
	}

	public boolean equals(Object obj) {
		if (obj instanceof LiveEffect) {
			LiveEffect effect = (LiveEffect) obj;
			return name.equals(effect.name) &&
					title.equals(effect.title) &&
					preferedInput == effect.preferedInput &&
					type == effect.type &&
					flags == effect.flags &&
					majorVersion == effect.majorVersion &&
					minorVersion == effect.minorVersion;
		}
		return false;
	}

	private static ArrayList getUnusedEffects() {
		if (unusedEffects == null)
			unusedEffects = nativeGetEffects();
		return unusedEffects;
	}

	private static native ArrayList nativeGetEffects();

	/**
	 * Call only from onEditParameters!
	 */
	public native boolean updateParameters(Map parameters);

	/**
	 * Call only from onEditParameters!
	 */
	// TODO: is this still needed? difference to getMenuItem()?
	public native Object getMenuItem(Map parameters);

	// Callback functions:

	protected void onEditParameters(Map parameters) throws Exception {
		System.out.println("onEditParameters");
		if (wrapper != null)
			FunctionHelper.callFunction(wrapper, "onEditParameters", new Object[] { parameters });
	}

	private Function onCalculate = null;
	
	public void setOnCalculate(Function onCalculate) {
		this.onCalculate = onCalculate;
	}
	
	public Function getOnCalculate() {
		return onCalculate;
	}

	protected Art onCalculate(Map parameters, Art art) throws Exception {
		System.out.println("onCalculate");
		if (wrapper != null && onCalculate != null) {
			Object ret = FunctionHelper.callFunction(wrapper, onCalculate, new Object[] { parameters, art });
			// it is only possible to either return the art itself or set the art to null!
			// everything else semse to cause a illustrator crash

			// TODO: This is not correct handling:
			// Am 23.02.2005 um 18:53 schrieb Frank Stokes-Guinan:
			// When creating output art for the go message, the output art must be a child of the same parent as
			// the input art. It also must be the only child of this parent, so if you create a copy of the input
			// art, work on it and attempt to return the copy as the output art, you must make sure to dispose the
			// original input art first. It is not legal to create an art object in an arbitrary place and return
			// that as the output art.

			return ret == art ? art : null;
		}
		return null;
	}

	protected int onGetInputType(Map parameters, Art art) throws Exception {
		System.out.println("onGetInputType");
		if (wrapper != null) {
			return ScriptRuntime.toInt32(
				FunctionHelper.callFunction(wrapper, "onGetInputType", new Object[] { parameters, art })
			);
		}
		return 0;
	}

	/**
	 * To be called from the native environment:
	 */
	private static void onEditParameters(int handle, Map parameters, int effectContext, boolean allowPreview)
			throws Exception {
		LiveEffect effect = getEffect(handle);
		if (effect != null) {
			// put these special values to the parameters for the duration of the handler
			// the parameter map then needs to be passed to functions like updateParameters
			parameters.put("context", new Handle(effectContext));
			parameters.put("allowPreview", new Boolean(allowPreview));
			effect.onEditParameters(parameters);
			parameters.remove("context");
			parameters.remove("allowPreview");
		}
	}

	/**
	 * To be called from the native environment:
	 */
	private static int onCalculate(int handle, Map parameters, Art art) throws Exception {
		LiveEffect effect = getEffect(handle);
		if (effect != null) {
			Art newArt = effect.onCalculate(parameters, art);
			if (newArt != null)
				art = newArt;
		}
		// already return the handle to the native environment so it doesn't need to access it there...
		return art.handle;
	}

	/**
	 * To be called from the native environment:
	 */
	private static int onGetInputType(int handle, Map parameters, Art art) throws Exception {
		LiveEffect effect = getEffect(handle);
		if (effect != null)
			return effect.onGetInputType(parameters, art);
		return 0;
	}

	private static LiveEffect getEffect(int handle) {
		return (LiveEffect) effects.get(new Handle(handle));
	}
}
