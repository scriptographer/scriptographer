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
 * File created on 03.11.2005.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import com.scriptographer.CommitManager;

/**
 * CharacterStyle is built on top of PathStyle and adds the text related fields
 * 
 * @author lehni
 **/

/*
 * This is pretty hackish due to the way PathStyle was implemented:
 * PathStyle is completely mirrored in java through fetch and commit
 * The same is done here for CharacterStyle, by using sAIATEPaint in order
 * to convert the CharFeaturesRef into AIPathStyle and back
 * 
 * All the additional type related fields are not mirrored but directly
 * changed on the underlying CharfeaturesRef
 * markDirty needs to be called for any of the changes
 */
public class CharacterStyle extends PathStyle {

	// AutoKernType
	public final static Integer KERNING_MANUAL = new Integer(0);
	public final static Integer KERNING_METRIC = new Integer(1);
	public final static Integer KERNING_OPTICAL = new Integer(2);

	// FontCapsOption
	public final static Integer CAPS_NORMAL = new Integer(0);
	public final static Integer CAPS_SMALL = new Integer(1);
	public final static Integer CAPS_ALL = new Integer(2);
	public final static Integer CAPS_ALL_SMALL = new Integer(3);

	// FontBaselineOption
	public final static Integer BASELINE_NORMAL = new Integer(0);
	public final static Integer BASELINE_SUPERSCRIPT = new Integer(1);
	public final static Integer BASELINE_SUBSCRIPT = new Integer(2);

	// FontOpenTypePositionOption
	public final static Integer POSITION_NORMAL = new Integer(0);
	public final static Integer POSITION_SUPERSCRIPT = new Integer(1);
	public final static Integer POSITION_SUBSCRIPT = new Integer(2);
	public final static Integer POSITION_NUMERATOR = new Integer(3);
	public final static Integer POSITION_DENOMINATOR = new Integer(4);

	// StrikethroughPosition
	public final static Integer STRIKETHROUGH_OFF = new Integer(0);
	public final static Integer STRIKETHROUGH_XHEIGHT = new Integer(1);
	public final static Integer STRIKETHROUGH_EMBOX = new Integer(2);

	// UnderlinePosition
	public final static Integer UNDERLINE_OFF = new Integer(0);
	public final static Integer UNDERLINE_RIGHT_IN_VERTICAL = new Integer(1);
	public final static Integer UNDERLINE_LEFT_IN_VERTICAL = new Integer(2);

	// FigureStyle
	public final static Integer FIGURE_DEFAULT = new Integer(0);
	public final static Integer FIGURE_TABULAR = new Integer(1);
	public final static Integer FIGURE_PROPORTIONAL_OLDSTYLE = new Integer(2);
	public final static Integer FIGURE_PROPORTIONAL = new Integer(3);
	public final static Integer FIGURE_TABULAR_OLDSTYPE = new Integer(4);

	private TextRange range;
	private Object commitKey;
	private boolean pathStyleChanged;
	
	private static native int nativeCreate();
	
	private CharacterStyle(int handle) {
		super(handle);
		version = CommitManager.version;
		pathStyleChanged = false;
	}
	
	public CharacterStyle() {
		this(nativeCreate());
		range = null;
		commitKey = this;
	}

	protected CharacterStyle(int handle, TextRange range) {
		this(handle);
		this.range = range;
		this.commitKey = range != null ?
				(Object) range.getStory() : (Object) this;
	}
	
	protected void changeHandle(int newHandle) {
		release(); // release old handle
		handle = newHandle;
		pathStyleChanged = false;
		fetched = false; // force refetch of PathStyle
		version = CommitManager.version;
	}
	
	private native int nativeClone();
	
	public Object clone() {
		if (dirty) // make sur it's not dirty 
			commit();
		return new CharacterStyle(nativeClone());
	}
	
	protected void update() {
		// only update if it didn't change in the meantime:
		if (!fetched || !dirty && range != null &&
				version != CommitManager.version)
			fetch();
	}
	
	protected native void nativeGet(int handle);
	
	protected native void nativeSet(int handle, int docHandle,
		Color fillColor, boolean hasFillColor,
		short fillOverprint,
		Color strokeColor, boolean hasStrokeColor,
		short strokeOverprint, float strokeWidth,
		float dashOffset, float[] dashArray,
		short cap, short join, float miterLimit,
		short clip, short lockClip, short evenOdd, float resolution);
	
	protected native void nativeSetStyle(int handle, int docHandle,
			int rangeHandle);

	protected void fetch() {
		nativeGet(handle);
		fetched = true;
	}

	public void commit() {
		if (dirty) {
			if (pathStyleChanged)
				commit(handle, 0);
			if (range != null) {
				nativeSetStyle(handle, range.document.handle, range.handle);
			}
			dirty = false;
		}
	}

	/**
	 * markDirty is called when a pathStyle field is changed, see PathStyle
	 */
	protected void markDirty() {
		if (!dirty) {
			CommitManager.markDirty(commitKey, this);
			dirty = true;
			// markDirty is only called if PathStyle changes are made and they
			// need to be
			pathStyleChanged = true;
		}
	}
	
	/**
	 * markSetStyle is called from the native environemnt. it marks dirty but
	 * doesn't set pathStyleChanged, as it's only used for character style
	 * features
	 */
	protected void markSetStyle() {
		if (!dirty) {
			CommitManager.markDirty(commitKey, this);
			dirty = true;
		}
	}
	
	private native int nativeGetFont();
	private native void nativeSetFont(int handle);
	
	/**
	 * @jsbean Specifies which font to use in the character style.
	 * @jsbean If you pass a font family, it will automatically pick the first
	 * @jsbean of the family's weights.
	 * @jsbean Sample code:
	 * @jsbean <pre>
	 * @jsbean var text = new PointText(new Point(0,0));
	 * @jsbean text.content = "The content of the text field.";
	 * @jsbean
	 * @jsbean // Sets all the text to Verdana Regular.
	 * @jsbean text.characterStyle.font = fonts["Verdana"];
	 * @jsbean
	 * @jsbean //sets the second word to Verdana Bold
	 * @jsbean text.range.words[1].characterStyle.font = fonts["Verdana"]["Bold"];
	 * @jsbean </pre>
	 */
	public FontWeight getFont() {
		int handle = nativeGetFont();
		if (handle == -1)
			return null;
		else if (handle == 0)
			return FontWeight.NONE;
		else
			return FontWeight.wrapHandle(handle);
	}
	
	public void setFont(FontWeight weight) {
		int font;
		if (weight == null)
			font = -1;
		else if (weight == FontWeight.NONE)
			font = 0;
		else
			font = weight.handle;
		nativeSetFont(font);
	}
	
	public void setFont(FontFamily font) {
		setFont(font != null && font.size() > 0 ?
				(FontWeight) font.get(0) : null);
	}
	
	/**
	 * @jsbean Specifies the font size in pixels.
	 * @jsbean Sample code:
	 * @jsbean <pre>
	 * @jsbean var text = new PointText(new Point(0,0));
	 * @jsbean text.content = "The content of the text field.";
	 * @jsbean 
	 * @jsbean // sets the font size to 10px
	 * @jsbean text.characterStyle.fontSize = 10
	 * @jsbean </pre>
	 */
	public native Float getFontSize();
	public native void setFontSize(Float size);
	
	/**
	 * @jsbean Specifies the horizontal scale of the character style.
	 * @jsbean Sample code:
	 * @jsbean <pre>
	 * @jsbean var text = new PointText(new Point(0,0));
	 * @jsbean text.content = "The content of the text field.";
	 * @jsbean 
	 * @jsbean // sets the horizontal scale to 200%
	 * @jsbean text.characterStyle.horizontalScale = 2
	 * @jsbean </pre>
	 */
	public native Float getHorizontalScale();
	public native void setHorizontalScale(Float scale);

	/**
	 * @jsbean Specifies the vertical scale of the character style.
	 * @jsbean Sample code:
	 * @jsbean <pre>
	 * @jsbean var text = new PointText(new Point(0,0));
	 * @jsbean text.content = "The content of the text field.";
	 * @jsbean 
	 * @jsbean // sets the vertical scale to 200%
	 * @jsbean text.characterStyle.verticalScale = 2
	 * @jsbean </pre>
	 */
	public native Float getVerticalScale();
	public native void setVerticalScale(Float scale);

	/**
	 * @jsbean A boolean value that specifies wether to use auto leading in the character style.
	 */
	public native Boolean getAutoLeading();
	public native void setAutoLeading(Boolean leading);

	/**
	 * @jsbean Specifies the leading (vertical spacing) of the character style in pixels.
	 */
	public native Float getLeading();
	public native void setLeading(Float leading);

	public native Integer getTracking();
	public native void setTracking(Integer tracking);

	/**
	 * @jsbean Specifies the baseline shift of the character style in pixels.
	 * @jsbean Baseline shift moves text up or down relative to it's baseline.
	 */
	public native Float getBaselineShift();
	public native void setBaselineShift(Float shift);

	public native Float getRotation();
	public native void setRotation(Float rotation);

	/**
	 * @jsbean The character style's kerning method as specified by the CharacterStyle.KERNING_*
	 * @jsbean static properties.
	 * @jsbean Sample code:
	 * @jsbean <pre>
	 * @jsbean var text = new PointText(new Point(0,0));
	 * @jsbean text.content = "The content of the text field.";
	 * @jsbean 
	 * @jsbean // sets the kerning method to optical
	 * @jsbean text.characterStyle.kerningMethod = CharacterStyle.KERNING_OPTICAL;
	 * @jsbean </pre>
	 */
	public native Integer getKerningMethod();
	public native void setKerningMethod(Integer method);
	
	/**
	 * @jsbean The character style's capitalization as specified by the CharacterStyle.CAPS_*
	 * @jsbean static properties.
	 * @jsbean Sample code:
	 * @jsbean <pre>
	 * @jsbean var text = new PointText(new Point(0,0));
	 * @jsbean text.content = "The content of the text field.";
	 * @jsbean 
	 * @jsbean // sets the capitalization to use only caps
	 * @jsbean text.characterStyle.capitalization = CharacterStyle.CAPS_ALL;
	 * @jsbean </pre>
	 */
	public native Integer getCapitalization();
	public native void setCapitalization(Integer caps);

	public native Integer getBaselineOption();
	public native void setBaselineOption(Integer option);
	
	public native Integer getOpenTypePosition();
	public native void setOpenTypePosition(Integer position);
	
	public native Integer getStrikethroughPosition();
	public native void setStrikethroughPosition(Integer position);
	
	public native Integer getUnderlinePosition();
	public native void setUnderlinePosition(Integer position);
	
	public native Float getUnderlineOffset();
	public native void setUnderlineOffset(Float offset);

	// ------------------------------------------------------------------
	// OpenType features
	// ------------------------------------------------------------------

	public native Boolean getLigature();
	public native void setLigature(Boolean ligature);

	public native Boolean getDiscretionaryLigature();
	public native void setDiscretionaryLigature(Boolean ligature);

	public native Boolean getContextualLigature();
	public native void setContextualLigature(Boolean ligature);

	public native Boolean getAlternateLigatures();
	public native void setAlternateLigatures(Boolean ligature);

	public native Boolean getOldStyle();
	public native void setOldStyle(Boolean oldStyle);

	public native Boolean getFractions();
	public native void setFractions(Boolean fractions);

	public native Boolean getOrdinals();
	public native void setOrdinals(Boolean ordinals);

	public native Boolean GetSwash();
	public native void setSwash(Boolean swash);

	public native Boolean getTitling();
	public native void setTitling(Boolean titling);

	public native Boolean getConnectionForms();
	public native void setConnectionForms(Boolean forms);

	public native Boolean getStylisticAlternates();
	public native void setStylisticAlternates(Boolean alternates);

	public native Boolean getOrnaments();
	public native void setOrnaments(Boolean ornaments);

	public native Integer getFigureStyle();
	public native void setFigureStyle(Integer figureStyle);

	public native Boolean getNoBreak();
	public native void setNoBreak(Boolean noBreak);
/*
	// ------------------------------------------------------------------
	// Japanese OpenType feature support
	// ------------------------------------------------------------------
	ATEErr (*GetProportionalMetrics) ( CharFeaturesRef charfeatures, bool* isAssigned, bool* ret);
	ATEErr (*GetKana) ( CharFeaturesRef charfeatures, bool* isAssigned, bool* ret);
	ATEErr (*GetRuby) ( CharFeaturesRef charfeatures, bool* isAssigned, bool* ret);
	ATEErr (*GetItalics) ( CharFeaturesRef charfeatures, bool* isAssigned, bool* ret);
	ATEErr (*GetBaselineDirection) ( CharFeaturesRef charfeatures, bool* isAssigned, BaselineDirection* ret);
	ATEErr (*GetLanguage) ( CharFeaturesRef charfeatures, bool* isAssigned, Language* ret);
	ATEErr (*GetJapaneseAlternateFeature) ( CharFeaturesRef charfeatures, bool* isAssigned, JapaneseAlternateFeature* ret);
	ATEErr (*GetTsume) ( CharFeaturesRef charfeatures, bool* isAssigned, ASReal* ret);
	ATEErr (*GetStyleRunAlignment) ( CharFeaturesRef charfeatures, bool* isAssigned, StyleRunAlignment* ret);
	// ------------------------------------------------------------------
	// WariChu Setings
	// ------------------------------------------------------------------
	ATEErr (*GetWariChuEnabled) ( CharFeaturesRef charfeatures, bool* isAssigned, bool* ret);
	ATEErr (*GetWariChuLineCount) ( CharFeaturesRef charfeatures, bool* isAssigned, ASInt32* ret);
	ATEErr (*GetWariChuLineGap) ( CharFeaturesRef charfeatures, bool* isAssigned, ASInt32* ret);
	ATEErr (*GetWariChuScale) ( CharFeaturesRef charfeatures, bool* isAssigned, ASReal* ret);
	ATEErr (*GetWariChuSize) ( CharFeaturesRef charfeatures, bool* isAssigned, ASReal* ret);
	ATEErr (*GetWariChuWidowAmount) ( CharFeaturesRef charfeatures, bool* isAssigned, ASInt32* ret);
	ATEErr (*GetWariChuOrphanAmount) ( CharFeaturesRef charfeatures, bool* isAssigned, ASInt32* ret);
	ATEErr (*GetWariChuJustification) ( CharFeaturesRef charfeatures, bool* isAssigned, WariChuJustification* ret);
	
	
TateChuYokoVerticalAdjustment
	ATEErr (*GetTCYUpDownAdjustment) ( CharFeaturesRef charfeatures, bool* isAssigned, ASInt32* ret);
TateChuYokoHorizontalAdjustment
	ATEErr (*GetTCYLeftRightAdjustment) ( CharFeaturesRef charfeatures, bool* isAssigned, ASInt32* ret);

japanese:

akiLeft
	ATEErr (*GetLeftAki) ( CharFeaturesRef charfeatures, bool* isAssigned, ASReal* ret);
akiRight
	ATEErr (*GetRightAki) ( CharFeaturesRef charfeatures, bool* isAssigned, ASReal* ret);
*/
	
	protected native void release();
	
	protected void finalize() {
		release();
	}
}
