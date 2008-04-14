/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 02.01.2005.
 *
 * $Id:CallbackHandler.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer.adm;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.Map;
import java.util.regex.Pattern;

import com.scriptographer.ScriptographerEngine; 
import com.scriptographer.ScriptographerException;
import com.scratchdisk.list.List;
import com.scratchdisk.script.Callable;
import com.scratchdisk.util.ConversionUtils;

/**
 * Subclasses the NotificationHandler and adds functionality for
 * defining track and draw callbacks (NotificationHandler can handle
 * these but not set them).
 * It also adds the onResize handler as both Item and Dialog need it,
 * and these are the only subclasses of CallbackHandler 
 *
 * @author lehni
 */
abstract class Component extends NotificationHandler {
	/*
	 * Use an activation mechanism for the expensive callback routines (the ones
	 * that get called often). These are only activated if the user actually
	 * sets a callback functions.
	 * 
	 * Abstract declarations for native functions that enable and disable the
	 * native track and draw proc procedures:
	 */
	private boolean trackCallback = false;
	private boolean drawCallback = false;

	abstract protected void nativeSetTrackCallback(boolean enabled);
	abstract protected void nativeSetDrawCallback(boolean enabled);

	public void setTrackCallback(boolean enabled) {
		nativeSetTrackCallback(enabled);
		trackCallback = enabled;
	}

	public boolean getTrackCallback() {
		return trackCallback;
	}

	public void setDrawCallback(boolean enabled) {
		nativeSetDrawCallback(enabled);
		drawCallback = enabled;
	}

	public boolean getDrawCallback() {
		return drawCallback;
	}
	
	/**
	 * @param mask Tracker.MASK_*
	 */
	abstract public void setTrackMask(int mask);
	abstract public int getTrackMask();

 	protected Callable onTrack = null;
	protected Callable onDraw = null;

	public void setOnTrack(Callable func) {
		setTrackCallback(func != null);
		onTrack = func;
	}

	public Callable getOnTrack() {
		return onTrack;
	}

	protected boolean onTrack(Tracker tracker) throws Exception {
		// Retrieve through getter so it can be overriden by subclasses,
		// e.g. HierarchyList
		Callable onTrack = this.getOnTrack();
		if (onTrack != null) {
			Object result = ScriptographerEngine.invoke(onTrack, this,
					new Object[] { tracker });
			if (result != null)
				return ConversionUtils.toBoolean(result);
		}
		return true;
	}

	public void setOnDraw(Callable func) {
		setDrawCallback(func != null);
		onDraw = func;
	}

	public Callable getOnDraw() {
		return onDraw;
	}

	protected void onDraw(Drawer drawer) throws Exception {
		// Retrieve through getter so it can be overriden by subclasses,
		// e.g. HierarchyList
		Callable onDraw = this.getOnDraw();
		if (onDraw != null)
			ScriptographerEngine.invoke(onDraw, this,
					new Object[] { drawer });
	}
	
	protected Callable onResize = null;

	public void setOnResize(Callable func) {
		onResize = func;
	}

	public Callable getOnResize() {
		return onResize;
	}

	protected void onResize(int dx, int dy) throws Exception {
		// Retrieve through getter so it can be overridden by subclasses,
		// e.g. HierarchyList
		Callable onResize = this.getOnResize();
		if (onResize != null)
			ScriptographerEngine.invoke(onResize, this,
					new Object[] { new Integer(dx), new Integer(dy) });
	}

	/*
	 * AWT / Layout Bridge
	 */

	protected abstract java.awt.Component getAWTComponent();

	protected AWTContainer getAWTContainer() {
		java.awt.Component component = getAWTComponent();
		if (!(component instanceof AWTContainer))
			throw new ScriptographerException("Component does not support sub components.");
		return (AWTContainer) component;
	}

	public void setLayout(LayoutManager mgr) {
		this.getAWTContainer().setLayout(mgr);
	}

	public LayoutManager getLayout() {
		return this.getAWTContainer().getLayout();
	}

	public void setMargin(int top, int right, int bottom, int left) {
		this.getAWTContainer().setInsets(top, left, bottom, right);
	}

	public Border getMargin() {
		return new Border(this.getAWTContainer().getInsets());
	}

	public void setMargin(Border margin) {
		setMargin(margin.top, margin.right, margin.bottom, margin.left);
	}

	public void setMargin(int margin) {
		setMargin(margin, margin, margin, margin);
	}

	public void setMargin(int ver, int hor) {
		setMargin(ver, hor, ver, hor);
	}

	public int getLeftMargin() {
		return getMargin().left;
	}

	public void setLeftMargin(int left) {
		Border margin = getMargin();
		margin.left = left;
		setMargin(margin);
	}

	public int getTopMargin() {
		return getMargin().top;
	}

	public void setTopMargin(int top) {
		Border margin = getMargin();
		margin.top = top;
		setMargin(margin);
	}

	public int getRightMargin() {
		return getMargin().right;
	}

	public void setRightMargin(int right) {
		Border margin = getMargin();
		margin.right = right;
		setMargin(margin);
	}

	public int getBottomMargin() {
		return getMargin().bottom;
	}

	public void setBottomMargin(int bottom) {
		Border margin = getMargin();
		margin.bottom = bottom;
		setMargin(margin);
	}

	protected void addComponent(Component component) {
		// Do nothing here, only ItemGroup uses it
	}

	protected void removeComponent(Component component) {
		// Do nothing here, only ItemGroup uses it
	}

	private Content content = null;

	public Content getContent() {
		if (content == null)
			content = new Content(this);
		return content;
	}

	public void setContent(Component[] elements) {
		// The default for setting array elements is a flow layout
		if (this.getLayout() == null)
			this.setLayout(new FlowLayout());

		Content content = getContent();
		content.removeAll();
		content.addAll(elements);
	}

	public void setContent(List<? extends Component> elements) {
		// The default for setting array elements is a flow layout
		if (this.getLayout() == null)
			this.setLayout(new FlowLayout());

		Content content = getContent();
		content.removeAll();
		content.addAll(elements);
	}
	
	private static final Pattern borderLayoutPattern = Pattern.compile(
			"^(north|south|east|west|center|first|last|before|after)$",
			Pattern.CASE_INSENSITIVE);

	public void setContent(Map<String,? extends Component> elements) {
		// Find out what kind of layout we have by checking the keys
		// in the map:
		if (this.getLayout() == null) {
			boolean borderLayout = true;
			for (String key : elements.keySet())
				borderLayout = borderLayoutPattern.matcher(key).matches();
			if (borderLayout) {
				this.setLayout(new BorderLayout());
			} else {
				// TODO: Figure out amount of rows and columns by analyzing the keys.
				// for now we don't do that...
				this.setLayout(new TableLayout("preferred", "preferred"));
			}
		}

		Content content = getContent();
		content.removeAll();
		content.addAll(elements);
	}

	public void addToContent(Component component) {
		getContent().add(component);
	}

	public void addToContent(Component component, String constraints) {
		getContent().set(constraints, component);
	}

	public void addToContent(Component component, int index) {
		getContent().add(index, component);
	}

	public void removeFromContent(Component component) {
		getContent().remove(component);
	}
	
	public void removeFromContent(int index) {
		getContent().remove(index);
	}

	public void removeFromContent(String constraints) {
		getContent().remove(constraints);
	}

	public void removeContent() {
		getContent().removeAll();
	}
/**
	 * An abstract class that adds some commonly used things like
	 * setInsets to java.awt.Container.
	 * 
	 * @author lehni
	 *
	 */
	abstract class AWTContainer extends java.awt.Container implements ComponentWrapper {
		Insets insets;

		public void setInsets(int top, int left, int bottom, int right) {
			insets = new Insets(top, left, bottom, right);
		}

		public Insets getInsets() {
			return insets;
		}

		public void doLayout() {
			super.doLayout();
			// now walk through all the items do their layout as well:
			java.awt.Component[] components = getComponents();
			for (int i = 0; i < components.length; i++)
				components[i].doLayout();
		}
	}
}
