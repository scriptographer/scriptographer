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
 * File created on 03.11.2005.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.script.ArgumentReader;
import com.scratchdisk.util.IntegerEnumUtils;
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

	public CharacterStyle(ArgumentReader reader) {
		// Handler fill & stroke through PathStyle argument reader constructor
		super(nativeCreate(), reader);
		range = null;
		commitKey = this;
		// See reading of color in StrokeStyle:
		Object weight = reader.readObject("font");
		if (weight == null && (!reader.isHash() || reader.has("font")))
			weight = FontWeight.NONE;
		if (weight instanceof String) {
			setFont((String) weight);
		} else {
			if (weight instanceof FontWeight || weight == null)
				setFont((FontWeight) weight);
			else
				setFont(FontWeight.NONE);
		}
		setFontSize(reader.readFloat("fontSize"));
		setHorizontalScale(reader.readFloat("horizontalScale"));
		setVerticalScale(reader.readFloat("verticalScale"));
		setAutoLeading(reader.readBoolean("autoLeading"));
		setLeading(reader.readFloat("leading"));
		setTracking(reader.readInteger("tracking"));
		setBaselineShift(reader.readFloat("baselineShift"));
		setRotation(reader.readFloat("Rotation"));
		setKerningType(reader.readEnum("kerningType", KerningType.class));
		setCapitalization(reader.readEnum("capitalization", TextCapitalization.class));
		setBaselineOption(reader.readEnum("baselineOption", BaselineOption.class));
		setOpenTypePosition(reader.readEnum("openTypePosition", OpenTypePosition.class));
		setStrikethroughPosition(reader.readEnum("strikethroughPosition", StrikethroughPosition.class));
		setUnderlinePosition(reader.readEnum("underlinePosition", UnderlinePosition.class));
		setUnderlineOffset(reader.readFloat("underlineOffset"));
		setLigature(reader.readBoolean("ligature"));
		setDiscretionaryLigature(reader.readBoolean("discretionaryLigature"));
		setContextualLigature(reader.readBoolean("contextLigature"));
		setAlternateLigatures(reader.readBoolean("alternateLigature"));
		setOldStyle(reader.readBoolean("oldStyle"));
		setFractions(reader.readBoolean("fractions"));
		setOrdinals(reader.readBoolean("ordinals"));
		setSwash(reader.readBoolean("swash"));
		setTitling(reader.readBoolean("titling"));
		setConnectionForms(reader.readBoolean("forms"));
		setStylisticAlternates(reader.readBoolean("stylisticAlternates"));
		setOrnaments(reader.readBoolean("ornaments"));
		setFigureStyle(reader.readEnum("figureStyle", FigureStyle.class));
		setNoBreak(reader.readBoolean("noBreak"));
	}

	protected CharacterStyle(int handle, TextRange range) {
		this(handle);
		this.range = range;
		this.commitKey = range != null ?
				(Object) range.getStory() : (Object) this;
	}
	
	protected void changeHandle(int newHandle) {
		nativeRelease(handle); // release old handle
		handle = newHandle;
		pathStyleChanged = false;
		fetched = false; // force refetch of PathStyle
		version = CommitManager.version;
	}
	
	private native int nativeClone();
	
	public Object clone() {
		if (dirty) // make sure it's not dirty 
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
			int cap, int join, float miterLimit,
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
		markSetStyle();
		// markDirty is only called if PathStyle changes are made and they
		// need to be
		pathStyleChanged = true;
	}
	
	/**
	 * markSetStyle is called from the native environment. it marks dirty but
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
	 * @jsbean text.characterStyle.font = app.fonts["Verdana"];
	 * @jsbean
	 * @jsbean //sets the second word to Verdana Bold
	 * @jsbean text.range.words[1].characterStyle.font = app.fonts["Verdana"]["Bold"];
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

	public void setFont(String font) {
		setFont(FontList.getInstance().getWeight(font));
	}
	
	public void setFont(FontFamily font) {
		setFont(font != null && font.size() > 0 ?
				(FontWeight) font.get(0) : null);
	}
	
	/**
	 * @jsbean The font size in points.
	 * @jsbean Sample code:
	 * @jsbean <pre>
	 * @jsbean var text = new PointText(new Point(0,0));
	 * @jsbean text.content = "The content of the text field.";
	 * @jsbean 
	 * @jsbean // sets the font size to 10pt
	 * @jsbean text.characterStyle.fontSize = 10
	 * @jsbean </pre>
	 */
	public native Float getFontSize();
	public native void setFontSize(Float size);
	
	/**
	 * @jsbean The horizontal scale of the character style.
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
	 * @jsbean The vertical scale of the character style.
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
	 * @jsbean Specifies whether to use auto leading in the character style.
	 */
	public native Boolean getAutoLeading();
	public native void setAutoLeading(Boolean leading);

	/**
	 * @jsbean The leading (vertical spacing) of the character style in points.
	 */
	public native Float getLeading();
	public native void setLeading(Float leading);

	public native Integer getTracking();
	public native void setTracking(Integer tracking);

	/**
	 * @jsbean The baseline shift of the character style in points.
	 * @jsbean Baseline shift moves text up or down relative to it's baseline.
	 */
	public native Float getBaselineShift();
	public native void setBaselineShift(Float shift);

	public native Float getRotation();
	public native void setRotation(Float rotation);

	private native Integer nativeGetKerningType();
	private native void nativeSetKerningType(Integer method);

	/**
	 * @jsbean The character style's kerning method.
	 * @jsbean Sample code:
	 * @jsbean <pre>
	 * @jsbean var text = new PointText(new Point(0,0));
	 * @jsbean text.content = "The content of the text field.";
	 * @jsbean 
	 * @jsbean // sets the kerning method to optical
	 * @jsbean text.characterStyle.kerningType = "optical";
	 * @jsbean </pre>
	 */
	public KerningType getKerningType() {
		return (KerningType) IntegerEnumUtils.get(KerningType.class,
				nativeGetKerningType());
	}

	public void setKerningType(KerningType type) {
		nativeSetKerningType(type != null ? type.value : null);
	}
	
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
	private native Integer nativeGetCapitalization();
	private native void nativeSetCapitalization(Integer caps);

	public TextCapitalization getCapitalization() {
		return IntegerEnumUtils.get(TextCapitalization.class, nativeGetCapitalization());
	}

	public void setCapitalization(TextCapitalization type) {
		nativeSetCapitalization(type != null ? type.value : null);
	}

	private native Integer nativeGetBaselineOption();
	private native void nativeSetBaselineOption(Integer option);
	
	public BaselineOption getBaselineOption() {
		return IntegerEnumUtils.get(BaselineOption.class, nativeGetBaselineOption());
	}

	public void setBaselineOption(BaselineOption type) {
		nativeSetBaselineOption(type != null ? type.value : null);
	}

	private native Integer nativeGetOpenTypePosition();
	private native void nativeSetOpenTypePosition(Integer position);

	public OpenTypePosition getOpenTypePosition() {
		return IntegerEnumUtils.get(OpenTypePosition.class, nativeGetOpenTypePosition());
	}

	public void setOpenTypePosition(OpenTypePosition type) {
		nativeSetOpenTypePosition(type != null ? type.value : null);
	}

	private native Integer nativeGetStrikethroughPosition();
	private native void nativeSetStrikethroughPosition(Integer position);
	
	public StrikethroughPosition getStrikethroughPosition() {
		return (StrikethroughPosition) IntegerEnumUtils.get(StrikethroughPosition.class,
				nativeGetStrikethroughPosition());
	}

	public void setStrikethroughPosition(StrikethroughPosition type) {
		nativeSetStrikethroughPosition(type != null ? type.value : null);
	}

	private native Integer nativeGetUnderlinePosition();
	private native void nativeSetUnderlinePosition(Integer position);

	public UnderlinePosition getUnderlinePosition() {
		return (UnderlinePosition) IntegerEnumUtils.get(UnderlinePosition.class,
				nativeGetUnderlinePosition());
	}

	public void setUnderlinePosition(UnderlinePosition type) {
		nativeSetUnderlinePosition(type != null ? type.value : null);
	}
	
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

	public native Boolean getSwash();
	public native void setSwash(Boolean swash);

	public native Boolean getTitling();
	public native void setTitling(Boolean titling);

	public native Boolean getConnectionForms();
	public native void setConnectionForms(Boolean forms);

	public native Boolean getStylisticAlternates();
	public native void setStylisticAlternates(Boolean alternates);

	public native Boolean getOrnaments();
	public native void setOrnaments(Boolean ornaments);

	private native Integer nativeGetFigureStyle();
	private native void nativeSetFigureStyle(Integer caps);

	public FigureStyle getFigureStyle() {
		return (FigureStyle) IntegerEnumUtils.get(FigureStyle.class,
				nativeGetFigureStyle());
	}

	public void setFigureStyle(FigureStyle type) {
		nativeSetFigureStyle(type != null ? type.value : null);
	}

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
	
	private native void nativeRelease(int handle);
	
	protected void finalize() {
		nativeRelease(handle);
		handle = 0;
	}
}
