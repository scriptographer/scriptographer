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
 * File created on 02.01.2005.
 */

package com.scriptographer.widget;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.Map;
import java.util.regex.Pattern;

import com.scratchdisk.list.List;
import com.scratchdisk.script.Callable;
import com.scratchdisk.util.ConversionUtils;
import com.scratchdisk.util.IntegerEnumUtils;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.ScriptographerException;


import com.scriptographer.adm.layout.TableLayout;
import com.scriptographer.ui.Border;
import com.scriptographer.ui.DialogFont;
import com.scriptographer.ui.Size;
import com.scriptographer.widget.ComponentWrapper;


/**
 * Subclasses the NotificationHandler and adds functionality for
 * defining track and draw callbacks (NotificationHandler can handle
 * these but not set them).
 * It also adds the onResize handler as both Item and Dialog need it,
 * and these are the only subclasses of CallbackHandler 
 *
 * @author lehni
 * 
 * @jshide
 */
public abstract class Component extends NotificationHandler {

	protected Component parent;

	/*
	 *  Fonts and Text Size
	 */
	protected abstract int nativeGetFont();
	
	protected abstract void nativeSetFont(int font);

	public DialogFont getFont() {
		// Only call native function if this is actually a native item.
		// This avoids isValid() exceptions when using fake UI items such
		// as spacers.
		if (handle != 0)
				return IntegerEnumUtils.get(DialogFont.class, nativeGetFont());
		return DialogFont.DEFAULT;
	}

	public void setFont(DialogFont font) {
		if (font != null && handle != 0)
			nativeSetFont(font.value);
	}

	public Size getTextSize(String text, int maxWidth, boolean ignoreBreaks) {
		// Create an image to get a drawer to calculate text sizes
		/*Image image = new Image(1, 1, ImageType.RGB);
		Drawer drawer = image.getDrawer();
		drawer.setFont(getFont());
		// Split at new lines chars, and measure each line separately
		String[] lines = ignoreBreaks ? new String[] {
			text.replaceAll("\r\n|\n|\r", " ")
		} : text.split("\r\n|\n|\r");
		Size size = new Size(0, 0);
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line.length() == 0)
				line = " "; // Make sure empty lines are measured too

			// Calculate the size of this part, using the drawer
			int width = drawer.getTextWidth(line);
			if (maxWidth > 0 && width > maxWidth)
				width = maxWidth;
			int height = drawer.getTextHeight(line, width + 1);

			// And add up size
			if (width > size.width)
				size.width = width;
			size.height += height;
		}
		drawer.dispose();
		return size;
		*/
		return  new Size(10,10);
	}

	public Size getTextSize(String text) {
		return getTextSize(text, -1, false);
	}

	public boolean isSmall() {
		DialogFont font = getFont();
		switch (font) {
		case PALETTE:
		case PALETTE_ITALIC:
		case PALETTE_BOLD:
		case PALETTE_BOLD_ITALIC:
			return true;
		default:
			return false;
		}
	}

	/*
	 * Use an activation mechanism for the expensive callback routines (the ones
	 * that get called often). These are only activated if the user actually
	 * sets a callback functions.
	 * 
	 * Abstract declarations for native functions that enable and disable the
	 * native track and draw proc procedures:
	 */
	private boolean trackCallback = false;

	/**
	 * @jshide
	 */
	public boolean getTrackCallback() {
		return trackCallback;
	}

	abstract protected void nativeSetTrackCallback(boolean enabled);

	public void setTrackCallback(boolean enabled) {
		nativeSetTrackCallback(enabled);
		trackCallback = enabled;
	}

	private boolean drawCallback = false;

	/**
	 * @jshide
	 */
	public boolean getDrawCallback() {
		return drawCallback;
	}

	abstract protected void nativeSetDrawCallback(boolean enabled);

	public void setDrawCallback(boolean enabled) {
		nativeSetDrawCallback(enabled);
		drawCallback = enabled;
	}
	
	/**
	 * @param mask Tracker.MASK_*
	 */
	abstract public void setTrackMask(int mask);
	abstract public int getTrackMask();

 	protected Callable onTrack = null;

	public void setOnTrack(Callable func) {
		setTrackCallback(func != null);
		onTrack = func;
	}

	public Callable getOnTrack() {
		return onTrack;
	}
/*
	protected boolean onTrack(Tracker tracker) {
		// Retrieve through getter so it can be overridden by subclasses,
		// e.g. HierarchyListBox
		Callable onTrack = this.getOnTrack();
		if (onTrack != null) {
			Object result = ScriptographerEngine.invoke(onTrack, this, tracker);
			if (result != null)
				return ConversionUtils.toBoolean(result);
		}
		return true;
	}
*/
	protected Callable onDraw = null;

	public void setOnDraw(Callable func) {
		setDrawCallback(func != null);
		onDraw = func;
	}

	public Callable getOnDraw() {
		return onDraw;
	}

	/*
	protected boolean onDraw(Drawer drawer) {
		// Retrieve through getter so it can be overridden by subclasses,
		// e.g. HierarchyListBox
		Callable onDraw = this.getOnDraw();
		if (onDraw != null) {
			Object result = ScriptographerEngine.invoke(onDraw, this, drawer);
			if (result != null)
				return ConversionUtils.toBoolean(result);
		}
		return true;
	}
	*/
	protected Callable onResize = null;

	public void setOnResize(Callable func) {
		onResize = func;
	}

	public Callable getOnResize() {
		return onResize;
	}

	protected void onResize(int dx, int dy) {
		// Retrieve through getter so it can be overridden by subclasses,
		// e.g. HierarchyListBox
		Callable onResize = this.getOnResize();
		if (onResize != null)
			ScriptographerEngine.invoke(onResize, this, dx, dy);
	}

	/*
	 * AWT / Layout Bridge
	 */

	protected abstract java.awt.Component getAWTComponent(boolean create);

	protected AWTContainer getAWTContainer(boolean create) {
		java.awt.Component component = getAWTComponent(create);
		if (component == null && !create)
			return null;
		if (!(component instanceof AWTContainer))
			throw new ScriptographerException("Component does not support sub components.");
		return (AWTContainer) component;
	}

	public void setLayout(LayoutManager mgr) {
		getAWTContainer(true).setLayout(mgr);
	}

	public LayoutManager getLayout() {
		return getAWTContainer(true).getLayout();
	}

	public boolean usesLayout() {
		// See if this item or its parent uses a layout.
		AWTContainer container = getAWTContainer(false);
		return container != null && container.getLayout() != null
				|| parent != null && parent.usesLayout();
	}

	public void doLayout() {
		AWTContainer container = getAWTContainer(false);
		if (container != null)
			container.doLayout();
	}

	/**
	 * @jshide
	 */
	public void setMargin(int top, int right, int bottom, int left) {
		this.getAWTContainer(true).setInsets(top, left, bottom, right);
	}

	public Border getMargin() {
		return new Border(this.getAWTContainer(true).getInsets());
	}

	public void setMargin(Border margin) {
		setMargin(margin.top, margin.right, margin.bottom, margin.left);
	}

	public void setMargin(int margin) {
		setMargin(margin, margin, margin, margin);
	}

	/**
	 * @jshide
	 */
	public void setMargin(int ver, int hor) {
		setMargin(ver, hor, ver, hor);
	}

	public int getMarginLeft() {
		return getMargin().left;
	}

	public void setMarginLeft(int left) {
		Border margin = getMargin();
		margin.left = left;
		setMargin(margin);
	}

	public int getMarginTop() {
		return getMargin().top;
	}

	public void setMarginTop(int top) {
		Border margin = getMargin();
		margin.top = top;
		setMargin(margin);
	}

	public int getMarginRight() {
		return getMargin().right;
	}

	public void setMarginRight(int right) {
		Border margin = getMargin();
		margin.right = right;
		setMargin(margin);
	}

	public int getMarginBottom() {
		return getMargin().bottom;
	}

	public void setMarginBottom(int bottom) {
		Border margin = getMargin();
		margin.bottom = bottom;
		setMargin(margin);
	}

	protected void addComponent(Component component) {
		// Do not really add component here, as only ItemGroup uses it.
		// Do not throw an UnsupportedOperationException though as it is also
		// used by Frame, which just applies its own layout to the added items
		// but does not actually nest them inside.
		component.parent = this;
	}

	protected void removeComponent(Component component) {
		// See above.
		component.parent = null;
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

	/**
	 * @jshide
	 */
	public void addToContent(Component component) {
		getContent().add(component);
	}

	/**
	 * @jshide
	 */
	public void addToContent(Component component, String constraints) {
		getContent().put(constraints, component);
	}

	/**
	 * @jshide
	 */
	public void addToContent(Component component, int index) {
		getContent().add(index, component);
	}

	/**
	 * @jshide
	 */
	public void removeFromContent(Component component) {
		getContent().remove(component);
	}
	
	/**
	 * @jshide
	 */
	public void removeFromContent(int index) {
		getContent().remove(index);
	}

	/**
	 * @jshide
	 */
	public void removeFromContent(String constraints) {
		getContent().remove(constraints);
	}

	/**
	 * @jshide
	 */
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
	abstract class AWTContainer extends java.awt.Container implements
			ComponentWrapper {
		Insets insets;

		public void setInsets(int top, int left, int bottom, int right) {
			insets = new Insets(top, left, bottom, right);
		}

		public Insets getInsets() {
			return insets;
		}

		public void doLayout() {
			super.doLayout();
			// Walk through all the items do their layout as well:
			for (java.awt.Component component : getComponents())
				component.doLayout();
		}
	}
}
