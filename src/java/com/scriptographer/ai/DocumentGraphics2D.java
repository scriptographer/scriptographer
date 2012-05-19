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
 * File created on May 19, 2012.
 */

package com.scriptographer.ai;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

import com.scratchdisk.util.IntegerEnumUtils;
import com.scriptographer.awt.AbstractGraphics2D;

/**
 * @author lehni
 * 
 */
public class DocumentGraphics2D extends AbstractGraphics2D {

	Document document;
	boolean textAsShapes;

	public DocumentGraphics2D(Document document, boolean textAsShapes) {
		super(textAsShapes);
		this.document = document;
	}

    public DocumentGraphics2D(DocumentGraphics2D g) {
        super(g);
		this.document = g.document;
	    this.textAsShapes = g.textAsShapes;
    }

	private PathItem createPathItem(Shape s) {
		return document.createPathItem(s.getPathIterator(gc.getTransform()));
	}

	public void draw(Shape s) {
		// Only BasicStroke can be converted.
		// If the GraphicContext's Stroke is not an instance of BasicStroke,
		// then the stroked outline is filled.
		Stroke stroke = gc.getStroke();
		if (stroke instanceof BasicStroke) {
			BasicStroke basicStroke = (BasicStroke) stroke;
			PathItem item = createPathItem(s);
			java.awt.Color color = gc.getColor();
			item.setStrokeColor(color);
			item.setDashArray(basicStroke.getDashArray());
			item.setDashOffset(basicStroke.getDashPhase());
			item.setStrokeCap(IntegerEnumUtils.get(StrokeCap.class, basicStroke.getEndCap()));
			item.setStrokeJoin(IntegerEnumUtils.get(StrokeJoin.class,basicStroke.getLineJoin()));
			item.setStrokeWidth(basicStroke.getLineWidth());
			item.setMiterLimit(basicStroke.getMiterLimit());
			item.setFillColor(Color.NONE);
			
		}
	}

	public void fill(Shape s) {
		PathItem item = createPathItem(s);
		java.awt.Color color = gc.getColor();
		item.setFillColor(color);
		item.setStrokeColor(Color.NONE);
	}

	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		// No-op
	}

	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		return false;
	}

	public boolean drawImage(Image img, int x, int y, int width, int height,
			ImageObserver observer) {
		return false;
	}

	public void dispose() {
	}

	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		// TODO: Implement
	}

	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		// TODO: Implement
	}

	public void drawString(String s, float x, float y) {
		if (textAsShapes) {
			super.drawString(s, x, y);
		} else {
			// TODO: Implement
		}
	}

	public void drawString(AttributedCharacterIterator iterator, float x,
			float y) {
		if (!textAsShapes) {
			super.drawString(iterator, x, y);
		} else {
			// TODO: Implement
		}
	}

	public GraphicsConfiguration getDeviceConfiguration() {
		return null;
	}

	public void setRenderingHints(Map<?,?> hints) {
	}

	public void addRenderingHints(Map<?,?> hints) {
	}

	public Graphics create() {
		return new DocumentGraphics2D(this);
	}

	public FontMetrics getFontMetrics(Font f) {
		return null;
	}

	public void setXORMode(java.awt.Color col) {
	}
}
