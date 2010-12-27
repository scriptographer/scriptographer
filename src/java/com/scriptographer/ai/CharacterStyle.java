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
 * File created on 03.11.2005.
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnumUtils;
import com.scriptographer.CommitManager;
import com.scriptographer.ScriptographerEngine;

/**
 * The CharacterStyle object represents the character style of a text item (
 * {@link TextItem#getCharacterStyle()}) or a text range (
 * {@link TextRange#getCharacterStyle()}).
 * 
 * Sample code:
 * <code>
 * var text = new PointText(new Point(50, 100));
 * text.content = 'Hello world.';
 * text.characterStyle.font = app.fonts['helvetica'];
 * text.characterStyle.fontSize = 10;
 * text.characterStyle.tracking = 100;
 * </code>
 * 
 * @author lehni
 */

// CharacterStyle is built on top of PathStyle and adds the text related fields

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

	protected CharacterStyle(int handle, TextRange range) {
		this(handle);
		this.range = range;
		this.commitKey = range != null ? range.getStory() : this;
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
			commit(false);
		return new CharacterStyle(nativeClone());
	}
	
	protected void update() {
		// only update if it didn't change in the meantime:
		if (!fetched || !dirty && range != null &&
				version != CommitManager.version)
			fetch();
	}
	
	@Override
	protected native void nativeGet(int handle, int docHandle);

	@Override
	protected native void nativeSet(int handle, int docHandle, 
			Color fillColor, boolean hasFillColor,
			short fillOverprint,
			Color strokeColor, boolean hasStrokeColor,
			short strokeOverprint, float strokeWidth,
			int cap, int join, float miterLimit,
			float dashOffset, float[] dashArray,
			short clip, short lockClip, int windingRule, float resolution);

	/* This does not need to override the one in ParagraphStyle */
	protected native void nativeSetStyle(int handle, int docHandle,
			int rangeHandle);

	protected void fetch() {
		// TODO: Find out if ATE CharacterStyle also needs activation of document, and
		// pass it if so: range != null ? range.document.handle : 0
		// This also needs code on the native side:
		// if (docHandle != NULL)
		//     Document_activate((AIDocumentHandle) docHandle);
		nativeGet(handle, 0);
		version = CommitManager.version;
		fetched = true;
	}

	public void commit(boolean endExecution) {
		if (dirty) {
			if (pathStyleChanged)
				commit(handle, 0);
			if (range != null) {
				nativeSetStyle(handle, range.document.handle, range.handle);
			}
			dirty = false;
			version = CommitManager.version;
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
	 * Specifies which font to use in the character style.
	 * If you pass a font family, it will automatically pick the first
	 * of the family's weights.
	 * 
	 * Sample code:
	 * <code>
	 * var text = new PointText(new Point(0,0));
	 * text.content = 'The content of the text field.';
	 *
	 * // Sets all the text to Verdana Regular.
	 * text.characterStyle.font = app.fonts['Verdana'];
	 *
	 * //sets the second word to Verdana Bold
	 * text.range.words[1].characterStyle.font = app.fonts['Verdana']['Bold'];
	 * </code>
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
	 * The font size in points.
	 * 
	 * Sample code:
	 * <code>
	 * var text = new PointText(new Point(0,0));
	 * text.content = 'The content of the text field.';
	 * 
	 * // sets the font size to 10pt
	 * text.characterStyle.fontSize = 10
	 * </code>
	 */
	public native Float getFontSize();
	public native void setFontSize(Float size);
	
	/**
	 * The horizontal scale of the character style.
	 * 
	 * Sample code:
	 * <code>
	 * var text = new PointText(new Point(0,0));
	 * text.content = 'The content of the text field.';
	 * 
	 * // sets the horizontal scale to 200%
	 * text.characterStyle.horizontalScale = 2;
	 * </code>
	 */
	public native Float getHorizontalScale();
	public native void setHorizontalScale(Float scale);

	/**
	 * The vertical scale of the character style.
	 * 
	 * Sample code:
	 * <code>
	 * var text = new PointText(new Point(0,0));
	 * text.content = 'The content of the text field.';
	 * 
	 * // sets the vertical scale to 200%
	 * text.characterStyle.verticalScale = 2;
	 * </code>
	 */
	public native Float getVerticalScale();
	public native void setVerticalScale(Float scale);

	/**
	 * Specifies whether to use auto leading in the character style.
	 * @return {@true if the character style uses auto leading}
	 */
	public native Boolean getAutoLeading();

	public void setAutoLeading(Boolean leading) {
		Boolean previous = getAutoLeading();
		if (previous == null && leading != null
				|| previous != null && !previous.equals(leading)) {
			nativeSetAutoLeading(leading);
			if (range != null)
				range.updateStyle();
		}
	}

	public native void nativeSetAutoLeading(Boolean leading);

	/**
	 * The leading (vertical spacing) of the character style.
	 */
	public native Float getLeading();

	public void setLeading(Float leading) {
		Float previous = getLeading();
		if (previous == null && leading != null
				|| previous != null && !previous.equals(leading)) {
			if (leading != null)
				setAutoLeading(false);
			nativeSetLeading(leading);
			if (range != null)
				range.updateStyle();
		}
	}

	private native void nativeSetLeading(Float leading);


	/**
	 * The tracking (horizontal character spacing) of the character style.
	 */
	public native Integer getTracking();
	public native void setTracking(Integer tracking);

	/**
	 * The baseline shift of the character style in points.
	 * Baseline shift moves text up or down relative to it's baseline.
	 */
	public native Float getBaselineShift();
	public native void setBaselineShift(Float shift);

	/**
	 * The rotation of the characters in the text item that the character style
	 * is applied to.
	 * 
	 * Angles are oriented clockwise and measured in degrees by default. Read
	 * more about angle units and orientation in the description of the
	 * {@link com.scriptographer.ai.Point#getAngle()} property.
	 * 
	 * Sample code:
	 * <code>
	 * var text = new PointText(new Point(0,0));
	 * text.content = 'The content of the text field.';
	 * 
	 * // sets the character rotation to 45 degrees:
	 * text.characterStyle.rotation = 45;
	 * </code>
	 * 
	 * @return the rotation angle
	 */
	public Float getRotation() {
		Float rotation = nativeGetRotation();
		// Rotation is internall in degrees
		if (rotation != null && !ScriptographerEngine.anglesInDegrees)
			return (float) (rotation * Math.PI / 180.0);
		return rotation;
	}

	public void setRotation(Float rotation) {
		// Rotation is internall in degrees
		if (rotation != null && !ScriptographerEngine.anglesInDegrees)
			rotation =(float) (rotation * 180.0 / Math.PI);
		nativeSetRotation(rotation);
	}

	private native Float nativeGetRotation();
	private native void nativeSetRotation(Float rotation);

	/**
	 * The kerning between two characters in thousands of em.
	 * 
	 * Sample code:
	 * <code>
	 * var text = new PointText(new Point(100, 100));
	 * text.content = 'abc';
	 * var range = text.range.characters[1];
	 * print(range.content); // 'b'
	 * range.characterStyle.kerning = 400;
	 * </code>
	 */
	public Integer getKerning() {
		return range != null ? nativeGetKerning(range.handle) : null;
	}

	public void setKerning(Integer kerning) {
		if (kerning != null && range != null)
			nativeSetKerning(range.handle, kerning);
	}
	
	private native int nativeGetKerning(int rangeHandle);
	private native void nativeSetKerning(int rangeHandle, int kerning);

	/**
	 * The character style's kerning method.
	 * 
	 * Sample code:
	 * <code>
	 * var text = new PointText(new Point(0,0));
	 * text.content = 'The content of the text field.';
	 * 
	 * // sets the kerning method to optical
	 * text.characterStyle.kerningType = 'optical';
	 * </code>
	 */
	public KerningType getKerningType() {
		return IntegerEnumUtils.get(KerningType.class, nativeGetKerningType());
	}

	public void setKerningType(KerningType type) {
		nativeSetKerningType(type != null ? type.value : null);
	}

	private native Integer nativeGetKerningType();
	private native void nativeSetKerningType(Integer method);

	/**
	 * The character style's capitalization.
	 * 
	 * Sample code:
	 * <code>
	 * var text = new PointText(new Point(0,0));
	 * text.content = 'The content of the text field.';
	 * 
	 * // sets the capitalization to all caps
	 * text.characterStyle.capitalization = 'all';
	 * </code>
	 */
	public TextCapitalization getCapitalization() {
		return IntegerEnumUtils.get(TextCapitalization.class, nativeGetCapitalization());
	}

	public void setCapitalization(TextCapitalization type) {
		nativeSetCapitalization(type != null ? type.value : null);
	}

	private native Integer nativeGetCapitalization();
	private native void nativeSetCapitalization(Integer caps);

	/**
	 * The character style's baseline option which is used to set the text to
	 * either subscript or superscript. Superscript and subscript text is
	 * reduced-size text that is raised or lowered in relation to a font's
	 * baseline.
	 * 
	 * Sample code:
	 * <code>
	 * var text = new PointText(new Point(0,0));
	 * text.content = 'The content of the text field.';
	 * 
	 * // sets the baseline option to superscript:
	 * text.characterStyle.capitalization = 'superscript';
	 * </code>
	 */
	public BaselineOption getBaselineOption() {
		return IntegerEnumUtils.get(BaselineOption.class, nativeGetBaselineOption());
	}

	public void setBaselineOption(BaselineOption type) {
		nativeSetBaselineOption(type != null ? type.value : null);
	}

	private native Integer nativeGetBaselineOption();
	private native void nativeSetBaselineOption(Integer option);
	
	public OpenTypePosition getOpenTypePosition() {
		return IntegerEnumUtils.get(OpenTypePosition.class, nativeGetOpenTypePosition());
	}

	public void setOpenTypePosition(OpenTypePosition type) {
		nativeSetOpenTypePosition(type != null ? type.value : null);
	}

	private native Integer nativeGetOpenTypePosition();
	private native void nativeSetOpenTypePosition(Integer position);

	public StrikethroughPosition getStrikethroughPosition() {
		return IntegerEnumUtils.get(StrikethroughPosition.class,
				nativeGetStrikethroughPosition());
	}

	public void setStrikethroughPosition(StrikethroughPosition type) {
		nativeSetStrikethroughPosition(type != null ? type.value : null);
	}

	private native Integer nativeGetStrikethroughPosition();
	private native void nativeSetStrikethroughPosition(Integer position);
	
	public UnderlinePosition getUnderlinePosition() {
		return IntegerEnumUtils.get(UnderlinePosition.class,
				nativeGetUnderlinePosition());
	}

	public void setUnderlinePosition(UnderlinePosition type) {
		nativeSetUnderlinePosition(type != null ? type.value : null);
	}
	
	private native Integer nativeGetUnderlinePosition();
	private native void nativeSetUnderlinePosition(Integer position);

	/**
	 * Sets the offset of the underline relative to the baseline.
	 */
	public native Float getUnderlineOffset();
	public native void setUnderlineOffset(Float offset);

	// ------------------------------------------------------------------
	// OpenType features
	// ------------------------------------------------------------------

	/**
	 * {@grouptitle OpenType Features}
	 */
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
		return IntegerEnumUtils.get(FigureStyle.class, nativeGetFigureStyle());
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
