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
 * File created on 18.02.2005.
 * 
 * $Id:LiveEffect.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer.ai;

import java.util.ArrayList;

import com.scratchdisk.script.Callable;
import com.scratchdisk.util.IntMap;
import com.scratchdisk.util.IntegerEnumUtils;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.ScriptographerException;
import com.scriptographer.ui.MenuItem;

/**
 * Wrapper for Illustrator's LiveEffects. Unfortunately, Illustrator is not
 * able to remove once created effects again until the next restart. They can be
 * removed from the menu but not from memory. So In order to recycle effects
 * with the same settings, e.g. during development, where the code often changes
 * but the initial settings maybe not, keep track of all existing effects and
 * match against those first before creating a new one. Also, when
 * Scriptographer is (re)loaded, the list of existing effects needs to be walked
 * through and added to the list of unusedEffects. This is done by calling
 * getUnusedEffects.
 * 
 * @author lehni
 * 
 * @jshide
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
item in an arbitrary place and return that as the output art.

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
public class LiveEffect extends NativeObject {

	// AIStyleFilterPreferredInputArtType
	protected static final int
		INPUT_DYNAMIC	 		= 0,
		INPUT_GROUP 			= 1 << (Item.TYPE_GROUP - 1),
		INPUT_PATH 				= 1 << (Item.TYPE_PATH - 1),
		INPUT_COMPOUNDPATH 		= 1 << (Item.TYPE_COMPOUNDPATH - 1),

		INPUT_PLACED 			= 1 << (Item.TYPE_PLACED - 1),
		INPUT_MYSTERYPATH 		= 1 << (Item.TYPE_MYSTERYPATH - 1),
		INPUT_RASTER 			= 1 << (Item.TYPE_RASTER - 1),

		// If INPUT_PLUGIN is not specified, the filter will receive the result group of a plugin
		// group instead of the plugin group itself
		INPUT_PLUGIN			= 1 << (Item.TYPE_PLUGIN - 1),
		INPUT_MESH 				= 1 << (Item.TYPE_MESH - 1),

		INPUT_TEXTFRAME 		= 1 << (Item.TYPE_TEXTFRAME - 1),

		INPUT_SYMBOL 			= 1 << (Item.TYPE_SYMBOL - 1),

		INPUT_FOREIGN			= 1 << (Item.TYPE_FOREIGN - 1),
		INPUT_LEGACYTEXT		= 1 << (Item.TYPE_LEGACYTEXT - 1),

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
	public static final int
		FLAG_NONE						= 0,
		/* Parameters can be scaled. */
		FLAG_HAS_SCALABLE_PARAMS 		= 1 << 17,
		/* Supports automatic rasterization. */
		FLAG_USE_AUTO_RASTARIZE 		= 1 << 18,
		/* Supports the generation of an SVG filter. */
		FLAG_CAN_GENERATE_SVG_FILTER	= 1 << 19,
		/* Has parameters that can be modified by a \c #kSelectorAILiveEffectAdjustColors message. */
		FLAG_HAS_ADJUST_COLOR_HANDLER	= 1 << 20,
		/* Handles \c #kSelectorAILiveEffectIsCompatible messages. If this flag is not set the message will not be sent. */
		FLAG_HAS_IS_COMPATIBLE_HANDLER	= 1 << 21;

	private String name;
	private String title;
	private LiveEffectPosition position;
	private int preferredInput;
	private int flags;
	private int majorVersion;
	private int minorVersion;
	private MenuItem menuItem = null;

	/**
	 * effects maps effectHandles to their wrappers.
	 */
	private static IntMap<LiveEffect> effects = null;

	/**
	 * Called from the native environment.
	 */
	protected LiveEffect(int handle, String name, String title, int position,
			int preferredInput, int flags, int majorVersion, int minorVersion) {
		super(handle);
		this.name = name;
		this.title = title;
		this.position = IntegerEnumUtils.get(LiveEffectPosition.class, position);
		this.preferredInput = preferredInput;
		this.flags = flags;
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
	}

	/**
	 * @param title preferred 
	 * @param preferred a combination of LiveEffect.INPUT_*
	 * @param flags a combination of LiveEffect.FLAG_*
	 * @param majorVersion
	 * @param minorVersion
	 */
	public LiveEffect(String title, String category, LiveEffectPosition position,
			Class preferredInput, int flags, int majorVersion, int minorVersion) {
		this(0, title, title, position != null ? position.value : 0,
				getInputType(preferredInput), flags, majorVersion, minorVersion);

		IntMap<LiveEffect> effects = getEffects();

		// Now see first whether there is an effect already that fits this
		// description. Reuse it, as we're probably re-executing a script
		// that produces the same effect again.
		Integer key = effects.keyOf(this);
		if (key != null) {
			// Found one, let's reuse it's handle and remove the old effect from
			// the list:
			LiveEffect effect = effects.get(key);
			effect.remove();
			handle = effect.handle;
			effect.handle = 0;
			effects.remove(key);
		} else {
			// No previously existing effect found, create a new one:
			handle = nativeCreate(name, title, this.position.value,
					this.preferredInput, flags, majorVersion, minorVersion);
		}

		if (handle == 0)
			throw new ScriptographerException("Unable to create LifeEffect.");

		if (category != null)
			menuItem = nativeAddMenuItem(name, category, title + "...");

		effects.put(handle, this);
	}

	public LiveEffect(String title, String category, LiveEffectPosition position, Class preferredInput,
			int flags) {
		this(title, category, position, preferredInput, flags, 1, 0);
	}

	public LiveEffect(String title, String category, LiveEffectPosition position, Class preferredInput) {
		this(title, category, position, preferredInput, FLAG_NONE, 1, 0);
	}

	public LiveEffect(String title, String category, LiveEffectPosition position) {
		this(title, category, position, null, FLAG_NONE, 1, 0);
	}

	private native int nativeCreate(String name, String title,
			int position, int flags, int preferredInput, int majorVersion,
			int minorVersion);

	private native MenuItem nativeAddMenuItem(String name, String category,
			String title);

	/**
	 * "Removes" the effect. there is no real destroy for LiveEffects in
	 * Illustrator, so all it really does is remove the effect's menu item, if
	 * there is one. It keeps the effectHandle and puts itself in the list of
	 * unused effects
	 */
	public boolean remove() {
		// see whether we're still linked:
		if (effects.get(handle) == this) {
			// if so remove it and put it to the list of unused effects, for later recycling
//			effects.remove(handle);
	//		getEffects().add(this);
			if (menuItem != null)
				menuItem.remove();
			menuItem = null;
			return true;
		}
		return false;
	}

	public static void removeAll() {
		// As remove() modifies the map, using an iterator is not possible here:
		if (effects != null)
			for (Object effect : effects.values().toArray())
				((LiveEffect) effect).remove();
	}

	public MenuItem getMenuItem() {
		return menuItem;
	}

	/*
	 * used for unusedEffects.indexOf in the constructor above
	 */
	public boolean equals(Object obj) {
		if (obj instanceof LiveEffect) {
			LiveEffect effect = (LiveEffect) obj;
			return name.equals(effect.name) &&
					title.equals(effect.title) &&
					preferredInput == effect.preferredInput &&
					position == effect.position &&
					flags == effect.flags &&
					majorVersion == effect.majorVersion &&
					minorVersion == effect.minorVersion;
		}
		return false;
	}

	private static IntMap<LiveEffect> getEffects() {
		if (effects == null) {
			effects = new IntMap<LiveEffect>();
			for (LiveEffect effect : nativeGetEffects())
				effects.put(effect.handle, effect);
		}
		return effects;
	}

	private static native ArrayList<LiveEffect> nativeGetEffects();

	// Getters:

	public LiveEffectPosition getPosition() {
		return position;
	}

	/**
	 * @jshide
	 */
	public String getName() {
		return name;
	}

	/**
	 * @jshide
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @jshide
	 */
	public int getFlags() {
		return flags;
	}

	/**
	 * @jshide
	 */
	public int getMajorVersion() {
		return majorVersion;
	}

	/**
	 * @jshide
	 */
	public int getMinorVersion() {
		return minorVersion;
	}

	// Callback functions:

	private Callable onEditParameters = null;
	
	public Callable getOnEditParameters() {
		return onEditParameters;
	}

	public void setOnEditParameters(Callable onEditParameters) {
		this.onEditParameters = onEditParameters;
	}

	protected void onEditParameters(LiveEffectEvent event) throws Exception {
		if (onEditParameters != null)
			ScriptographerEngine.invoke(
					onEditParameters, this, event);
	}

	private Callable onCalculate = null;
	
	public Callable getOnCalculate() {
		return onCalculate;
	}
	
	public void setOnCalculate(Callable onCalculate) {
		this.onCalculate = onCalculate;
	}

	protected void onCalculate(LiveEffectEvent event) throws Exception {
		if (onCalculate != null)
			ScriptographerEngine.invoke(onCalculate, this, event);
	}

	private Callable onGetInputType = null;

	public Callable getOnGetInputType() {
		return onGetInputType;
	}

	public void setOnGetInputType(Callable onGetInputType) {
		this.onGetInputType = onGetInputType;
	}

	protected int onGetInputType(LiveEffectEvent event) throws Exception {
		if (onGetInputType != null) {
			Object ret = ScriptographerEngine.invoke(
					onGetInputType, this, event);
			// Determine type from returned class
			if (ret instanceof Class)
				return getInputType((Class) ret);
			
		}
		// Default is INPUT_ANY_BUT_PLUGIN
		return INPUT_ANY_BUT_PLUGIN;
	}

	protected static int getInputType(Class cls) {
		// Default setting for effects that provide no input type is INPUT_DYNAMIC,
		// so the getInputType handler is asked instead.
		int type =  INPUT_DYNAMIC;
		// Determine type from Item class
		if (cls != null && Item.class.isAssignableFrom(cls)) {
			type = Item.getItemType(cls);
			if (type == Item.TYPE_ANY)
				type = INPUT_ANY_BUT_PLUGIN;
			else if (type == Item.TYPE_UNKNOWN)
				type = INPUT_DYNAMIC;
			else {
				type = 1 << (type - 1);
				if (type == INPUT_ANY)
					type = INPUT_ANY_BUT_PLUGIN;
			}
		}
		return type;
	}

	/**
	 * To be called from the native environment:
	 */
	@SuppressWarnings("unused")
	private static void onEditParameters(int handle, int dataHandle) throws Exception {
		LiveEffect effect = getEffect(handle);
		if (effect != null) {
			effect.onEditParameters(new LiveEffectEvent(0, dataHandle));
			/*
			Dictionary data = Dictionary.wrapHandle(dataHandle, null, true);
			data.remove("-position");
			data.remove("-handle");
			effect.onEditParameters(new LiveEffectEvent(null, data));
			data.put("-handle", dataHandle);
			*/
		}
	}

	/**
	 * To be called from the native environment:
	 */
	@SuppressWarnings("unused")
	private static int onCalculate(int handle, Item item, int dataHandle)
			throws Exception {
		LiveEffect effect = getEffect(handle);
		if (effect != null) {
			Document document = item.document;
			Dictionary data = Dictionary.wrapHandle(dataHandle, document, false);
			/*
			Object h = data.get("-handle");
			if (h instanceof Integer)
				data = Dictionary.wrapHandle(((Number) h).intValue(), document, false);
			if (!data.containsKey("-position")) {
				Item selected = document.getSelectedItems().getFirst();
				int position = 0;
				if (selected != null) {
					LiveEffectPosition pos = selected.getEffectPosition(effect, data);
					if (pos != null)
						position = pos.value;
				}
				data.put("-position", position);
			}
			*/
			Item parent = item.getParent();
			// Scriptographer's new item recording feature makes
			// processing effects extremly convenient. All new items
			// are automatically collected, and the right thing is
			// done with them at the end. Since doing the wrong
			// thing leads to endless crashes, this is the best
			// way to handle this anyway.
			Item.collectNewItems();
			ItemList newItems = null;
			try {
				effect.onCalculate(new LiveEffectEvent(item, data));
			} finally {
				newItems = Item.retreiveNewItems();
			}
			if (newItems.size() > 0) {
				Item newItem;
				if (newItems.size() == 1) {
					newItem = newItems.getFirst();
				} else {
					// More than one new item was produced. Group them, as
					// LiveEffects require one item only.
					newItem = new Group(newItems);
				}
				// "When creating output art for the go message, the output art
				// must be a child of the same parent as the input art. It also
				// must be the only child of this parent, so if you create a
				// copy of the input art, work on it and attempt to return the
				// copy as the output art, you must make sure to dispose the
				// original input art first. It is not legal to create an item
				// in an arbitrary place and return that as the output art."
				if (newItem.getParent().equals(parent) || parent.appendTop(newItem)) {
					item.remove();
					item = newItem;
				}
			}
		}
		// already return the handle to the native environment so it doesn't
		// need to access it there...
		return item.handle;
	}

	/**
	 * To be called from the native environment:
	 */
	@SuppressWarnings("unused")
	private static int onGetInputType(int handle, int itemHandle, int dataHandle)
			throws Exception {
		// For improved performance of onGetInputType, we do not wrap the handle
		// on the native side already, as often it is not even used. Instead
		// The LiveEffectEvent takes care of that on demand.
		LiveEffect effect = getEffect(handle);
		if (effect != null)
			return effect.onGetInputType(new LiveEffectEvent(itemHandle, dataHandle));
		return INPUT_ANY_BUT_PLUGIN;
	}

	private static LiveEffect getEffect(int handle) {
		return effects.get(handle);
	}
}
