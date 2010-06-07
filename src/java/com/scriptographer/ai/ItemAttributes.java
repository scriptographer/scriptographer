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
 * File created on Jun 7, 2010.
 */

package com.scriptographer.ai;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.scratchdisk.script.ArgumentReader;
import com.scratchdisk.util.ArrayList;

/**
 * @author lehni
 *
 */
public class ItemAttributes {

	private Class[] types;
	private HashMap<ItemAttribute, Boolean> attributes =
			new HashMap<ItemAttribute, Boolean>();
	
	public ItemAttributes() {
	}

	public ItemAttributes(ArgumentReader reader) {
		reader.setProperties(this);
	}

	protected ItemList getItems(Document document) {
		// Convert the attributes list to a new HashMap containing only
		// integer -> boolean pairs.
		int whichAttrs = 0, attrs = 0;
		for (Map.Entry<ItemAttribute, Boolean> entry : attributes.entrySet()) {
			ItemAttribute attribute = entry.getKey();
			Boolean value = entry.getValue();
			if (value != null) {
				whichAttrs |= attribute.value;
				if (value)
					attrs |= attribute.value;
			}
		}
		ItemList items = new ItemList();
		
		// If no class was specified, match them all through Item.
		Class[] types = this.types != null
				? this.types : new Class[] { Item.class };
		for (int i = 0; i < types.length; i++) {
			Class type = types[i];
			// Expand PathItem -> Path / CompoundPath
			if (PathItem.class.equals(type)) {
				items.addAll(document.getMatchingItems(Path.class,
						whichAttrs, attrs));
				items.addAll(document.getMatchingItems(CompoundPath.class,
						whichAttrs, attrs));
			} else {
				ItemList list = document.getMatchingItems(type,
						whichAttrs, attrs);
				// Filter out TextItems that do not match the given type.
				// This is needed since nativeGetMatchingItems returns all
				// TextItems...
				// TODO: Move this to the native side maybe?
				if (TextItem.class.isAssignableFrom(type))
					for (Item item : list)
						if (!type.isInstance(item))
							list.remove(item);
				items.addAll(list);
			}
		}
		// Filter out matched children when the parent matches too
		for (int i = items.size() - 1; i >= 0; i--) {
			Item item = items.get(i);
			if (items.contains(item.getParent()))
				items.remove(i);
		}
		return items;
	}

	public Class[] getType() {
		return types;
	}

	public void setType(Class[] types) {
		ArrayList<Class> classes = new ArrayList<Class>(Arrays.asList(types));
		// Filter out classes again that are not inheriting Item.
		for (int i = classes.size() - 1; i >= 0; i--)
			if (!Item.class.isAssignableFrom((classes.get(i))))
				classes.remove(i);
		this.types = classes.toArray(new Class[classes.size()]);
	}

	public void setType(Class type) {
		setType(new Class[] { type });
	}

	public void setType(String[] types) {
		ArrayList<Class> classes = new ArrayList<Class>();
		for (String type : types) {
			// Try loading class from String name.
			try {
				classes.add(Class.forName(Item.class.getPackage().getName()
						+ "." + type));
			} catch (ClassNotFoundException e) {
			}
		}
		setType(classes.toArray(new Class[classes.size()]));
	}

	public void setType(String types) {
		setType(types.split("\\s*,\\s*"));
	}

	private Boolean get(ItemAttribute attribute) {
		return attributes.get(attribute);
	}

	private void set(ItemAttribute attribute, Boolean value) {
		attributes.put(attribute, value);
	}

	public Boolean getSelected() {
		return get(ItemAttribute.SELECTED);
	}

	public void setSelected(Boolean selected) {
		set(ItemAttribute.SELECTED, selected);
	}

	public Boolean getLocked() {
		return get(ItemAttribute.LOCKED);
	}

	public void setLocked(Boolean locked) {
		set(ItemAttribute.LOCKED, locked);
	}

	public Boolean getHidden() {
		return get(ItemAttribute.HIDDEN);
	}

	public void setHidden(Boolean hidden) {
		set(ItemAttribute.HIDDEN, hidden);
	}

	public Boolean getFullySelected() {
		return get(ItemAttribute.FULLY_SELECTED);
	}

	public void setFullySelected(Boolean fullySelected) {
		set(ItemAttribute.FULLY_SELECTED, fullySelected);
	}

	public Boolean getClipMask() {
		return get(ItemAttribute.CLIP_MASK);
	}

	public void setClipMask(Boolean clipMask) {
		set(ItemAttribute.CLIP_MASK, clipMask);
	}

	public Boolean getTargeted() {
		return get(ItemAttribute.TARGETED);
	}

	public void setTargeted(Boolean targeted) {
		set(ItemAttribute.TARGETED, targeted);
	}
}
