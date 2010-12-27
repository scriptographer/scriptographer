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
 * File created on 04.11.2005.
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnumUtils;
import com.scriptographer.CommitManager;
import com.scriptographer.Committable;

/**
 * The ParagraphStyle object represents the paragraph style of a text item ({@link TextRange#getParagraphStyle()})
 * or a text range ({@link TextRange#getParagraphStyle()}).
 * 
 * Sample code:
 * <code>
 * var text = new PointText(new Point(0,0));
 * text.content = 'Hello world.';
 * text.paragraphStyle.justification = 'center';
 * </code>
 * @author lehni
 */
public class ParagraphStyle extends NativeObject implements Style, Committable {

	private TextRange range;
	private Object commitKey;
	protected boolean dirty = false;
	protected int version = -1;
	
	private static native int nativeCreate();
	
	private ParagraphStyle(int handle) {
		super(handle);
		version = CommitManager.version;
	}
	
	public ParagraphStyle() {
		this(nativeCreate());
		range = null;
		commitKey = this;
	}

	protected ParagraphStyle(int handle, TextRange range) {
		this(handle);
		this.range = range;
		this.commitKey = range != null ?
				(Object) range.getStory() : (Object) this;
	}
	
	protected void changeHandle(int newHandle) {
		nativeRelease(handle); // release old handle
		handle = newHandle;
	}
	
	private native int nativeClone();
	
	public Object clone() {
		if (dirty) // make sur it's not dirty 
			commit(false);
		return new ParagraphStyle(nativeClone());
	}
	
	protected native void nativeSetStyle(int handle, int rangeHandle);

	public void commit(boolean endExecution) {
		if (dirty) {
			if (range != null)
				nativeSetStyle(handle, range.handle);
			dirty = false;
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
	
	// ------------------------------------------------------------------
	// Justification
	// ------------------------------------------------------------------

	private native Integer nativeGetJustification();
	private native void nativeSetJustification(Integer justification);
	
	/**
	 * {@grouptitle Justification}
	 * 
	 * The justification of the paragraph.
	 */
	public ParagraphJustification getJustification() {
		return IntegerEnumUtils.get(ParagraphJustification.class, nativeGetJustification());
	}

	public void setJustification(ParagraphJustification type) {
		nativeSetJustification(type != null ? type.value : null);
	}
	
	private native Integer nativeGetSingleWordJustification();
	private native void nativeSetSingleWordJustification(Integer justification);

	/**
	 * The single word justification of the paragraph.
	 */
	public ParagraphJustification getSingleWordJustification() {
		return IntegerEnumUtils.get(ParagraphJustification.class, nativeGetSingleWordJustification());
	}

	public void setSingleWordJustification(ParagraphJustification type) {
		nativeSetSingleWordJustification(type != null ? type.value : null);
	}

	/**
	 * {@grouptitle Indentation}
	 * 
	 * The indentation of the first line in the paragraph.
	 */
	public native Float getFirstLineIndent();
	public native void setFirstLineIndent(Float indent);
	
	/**
	 * The indentation at the left of the paragraph.
	 */
	public native Float getStartIndent();
	public native void setStartIndent(Float indent);
	
	/**
	 * The indentation at the right of the paragraph.
	 */
	public native Float getEndIndent();
	public native void setEndIndent(Float indent);
	
	/**
	 * {@grouptitle Spacing}
	 * 
	 * The space before the paragraph.
	 */
	public native Float getSpaceBefore();
	public native void setSpaceBefore(Float space);

	/**
	 * The space after the paragraph.
	 */
	public native Float getSpaceAfter();
	public native void setSpaceAfter(Float space);

	// ------------------------------------------------------------------
	// Hyphenation Features
	// ------------------------------------------------------------------
	
	/**
	 * {@grouptitle Hyphenation}
	 * 
	 * Specifies whether to use hyphenation within the paragraph
	 * @return {@true if the paragraph uses hyphenation}
	 */
	public native Boolean getHyphenation();
	public native void setHyphenation(Boolean hyphenate);
	
	/**
	 * The minimum number of characters that a word needs to have to be able to
	 * be hyphenated.
	 */
	public native Integer getHyphenatedWordSize();
	public native void setHyphenatedWordSize(Integer size);
	
	/**
	 * The minimum number of characters at the beginning of a word
	 * that can be broken by a hyphen.
	 */
	public native Integer getPreHyphenSize();
	public native void setPreHyphenSize(Integer size);
	
	/**
	 * The minimum number of characters at the end of a word
	 * that can be broken by a hyphen.
	 */
	public native Integer getPostHyphenSize();
	public native void setPostHyphenSize(Integer size);
	
	/**
	 * The maximum number of consecutive lines on which hyphenation may occur. A
	 * value of 0 means unlimited consecutive hyphens are allowed at ends of
	 * lines.
	 */
	public native Integer getConsecutiveHyphenLimit();
	public native void setConsecutiveHyphenLimit(Integer limit);
	
	/**
	 * Specifies a distance from the right edge of the paragraph, where
	 * hyphenation is not allowed. A value of 0 allows all hyphenation.
	 */
	public native Float getHyphenationZone();
	public native void setHyphenationZone(Float zone);
	
	/**
	 * Specifies whether capitalized words should be hyphenated.
	 * @return {@true if capitalized words should be hyphenated}
	 */
	public native Boolean getHyphenateCapitalized();
	public native void setHyphenateCapitalized(Boolean hyphenate);
	
	public native Float getHyphenationPreference();
	public native void setHyphenationPreference(Float preference);

	// ------------------------------------------------------------------
	// Justification Features
	// ------------------------------------------------------------------
	
	/**
	 * {@grouptitle Word Spacing}
	 * 
	 * The desired word spacing of the paragraph.
	 */
	public native Float getDesiredWordSpacing();
	public native void setDesiredWordSpacing(Float spacing);
	
	/**
	 * The maximum word spacing of the paragraph.
	 */
	public native Float getMaxWordSpacing();
	public native void setMaxWordSpacing(Float spacing);
	
	/**
	 * The minimum word spacing of the paragraph.
	 */
	public native Float getMinWordSpacing();
	public native void setMinWordSpacing(Float spacing);
	
	/**
	 * {@grouptitle Letter Spacing}
	 * 
	 * The desired letter spacing of the paragraph.
	 */
	public native Float getDesiredLetterSpacing();
	public native void setDesiredLetterSpacing(Float spacing);
	
	/**
	 * The maximum letter spacing of the paragraph.
	 */
	public native Float getMaxLetterSpacing();
	public native void setMaxLetterSpacing(Float spacing);

	/**
	 * The minimum letter spacing of the paragraph.
	 */
	public native Float getMinLetterSpacing();
	public native void setMinLetterSpacing(Float spacing);
	
	/**
	 * {@grouptitle Glyph Scaling}
	 * 
	 * The desired glyph scaling of the paragraph as a value between 0 and 1.
	 */
	public native Float getDesiredGlyphScaling();
	public native void setDesiredGlyphScaling(Float scaling);
	
	/**
	 * The maximum glyph scaling of the paragraph as a value between 0 and 1.
	 */
	public native Float getMaxGlyphScaling();
	public native void setMaxGlyphScaling(Float scaling);
	
	/**
	 * The minimum glyph scaling of the paragraph as a value between 0 and 1.
	 */
	public native Float getMinGlyphScaling();
	public native void setMinGlyphScaling(Float scaling);
	
	/**
	 * {@grouptitle Leading}
	 * 
	 * The auto leading percentage of the paragraph as a value between 0 and 1.
	 */
	public native Float getAutoLeadingPercentage();
	public native void setAutoLeadingPercentage(Float percentage);
	
	private native Integer nativeGetLeading();
	private native void nativeSetLeading(Integer type);
	
	/**
	 * The leading mode of the paragraph.
	 */
	public LeadingType getLeading() {
		return IntegerEnumUtils.get(LeadingType.class, nativeGetLeading());
	}

	public void setLeading(LeadingType type) {
		nativeSetLeading(type != null ? type.value : null);
	}
	
	/* TODO: implement
	public native TabStopList getTabStops();
	public native void setTabStops(TabStopList tabStops);
	*/
	
	/**
	 * {@grouptitle Tabs}
	 * 
	 * The default tab width of the paragraph.
	 */
	public native Float getDefaultTabWidth();
	public native void setDefaultTabWidth(Float width);

	// ------------------------------------------------------------------
	// Japanese Features
	// ------------------------------------------------------------------
	/* TODO:
	ATEErr (*GetHangingRoman) ( ParaFeaturesRef parafeatures, bool* isAssigned, bool* ret);
	ATEErr (*GetAutoTCY) ( ParaFeaturesRef parafeatures, bool* isAssigned, ASInt32* ret);
	ATEErr (*GetBunriKinshi) ( ParaFeaturesRef parafeatures, bool* isAssigned, bool* ret);
	ATEErr (*GetBurasagariType) ( ParaFeaturesRef parafeatures, bool* isAssigned, BurasagariType* ret);
	ATEErr (*GetPreferredKinsokuOrder) ( ParaFeaturesRef parafeatures, bool* isAssigned, PreferredKinsokuOrder* ret);
	ATEErr (*GetKurikaeshiMojiShori) ( ParaFeaturesRef parafeatures, bool* isAssigned, bool* ret);
	/// This will return a null object if Kinsoku is not used (ie None set)
	ATEErr (*GetKinsoku) ( ParaFeaturesRef parafeatures, bool* isAssigned, KinsokuRef* ret);
	/// This will return a null object if Mojikumi is not used (ie None set)
	ATEErr (*GetMojiKumi) ( ParaFeaturesRef parafeatures, bool* isAssigned, MojiKumiRef* ret);
	// Other
	ATEErr (*GetEveryLineComposer) ( ParaFeaturesRef parafeatures, bool* isAssigned, bool* ret);
	ATEErr (*GetDefaultCharFeatures) ( ParaFeaturesRef parafeatures, bool* isAssigned, CharFeaturesRef* ret);
	*/
	
	private native void nativeRelease(int handle);
	
	protected void finalize() {
		nativeRelease(handle);
		handle = 0;
	}
}
