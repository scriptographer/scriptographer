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
 * File created on Mar 7, 2010.
 */

package com.scriptographer.adm;

import com.scriptographer.ai.Color;
import com.scriptographer.ui.Rectangle;
import com.scriptographer.ui.Point;

/**
 * @author lehni
 *
 */
public class ColorButton extends ImageButton {

	private Color color;

	public ColorButton(Dialog dialog) {
		super(dialog);
		// TODO: Fix this in item instead! Size defaults to 100 / 100...
		setSize(getBestSize());
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;

		if(color != null) {
			Rectangle rect = new Rectangle(new Point(),
					getSize().subtract(getMargin()).subtract(6));
			Image image = new Image(rect.width, rect.height, ImageType.RGB);
			Drawer drawer = image.getDrawer();
			drawer.setColor(color.toAWTColor());
			drawer.fillRect(rect);
			drawer.dispose();
			setImage(image);
		}
	}

	protected void updateBounds(int x, int y, int width, int height,
			boolean sizeChanged) {
		super.updateBounds(x, y, width, height, sizeChanged);
		setColor(color);
	}

	protected void onNotify(Notifier notifier) {
		if (notifier == Notifier.USER_CHANGED) {
			Color color = Dialog.chooseColor(this.color);
			if(color == null)
				return;
			setColor(color);
		}
		super.onNotify(notifier);
	}
}
