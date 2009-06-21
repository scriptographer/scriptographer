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
 * File created on 04.11.2005.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.script.ArgumentReader;
import com.scratchdisk.util.IntegerEnumUtils;
import com.scriptographer.CommitManager;
import com.scriptographer.Commitable;

/**
 * @author lehni
 */
public class ParagraphStyle extends NativeObject implements Style, Commitable {

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

	/**
	 * @jshide
	 */
	public ParagraphStyle(ArgumentReader reader) {
		this();
		setJustification(reader.readEnum("justification", ParagraphJustification.class));
		setSingleWordJustification(reader.readEnum("singleWordJustification", ParagraphJustification.class));
		setFirstLineIndent(reader.readFloat("firstLineIndent"));
		setStartIndent(reader.readFloat("startIndent"));
		setEndIndent(reader.readFloat("endIndent"));
		setSpaceBefore(reader.readFloat("spaceBefore"));
		setSpaceAfter(reader.readFloat("spaceAfter"));
		setHyphenation(reader.readBoolean("hyphenation"));
		setHyphenatedWordSize(reader.readInteger("hyphenatedWordSize"));
		setPreHyphenSize(reader.readInteger("preHyphenSize"));
		setPostHyphenSize(reader.readInteger("postHyphenSize"));
		setConsecutiveHyphenLimit(reader.readInteger("consecutiveHyphenLimit"));
		setHyphenationZone(reader.readFloat("hyphenationZone"));
		setHyphenateCapitalized(reader.readBoolean("hyphenateCapitalized"));
		setHyphenationPreference(reader.readFloat("hyphenationPreference"));
		setDesiredWordSpacing(reader.readFloat("desiredWordSpacing"));
		setMaxWordSpacing(reader.readFloat("maxWordSpacing"));
		setMinWordSpacing(reader.readFloat("minWordSpacing"));
		setDesiredLetterSpacing(reader.readFloat("desiredLetterSpacing"));
		setMaxLetterSpacing(reader.readFloat("maxLetterSpacing"));
		setMinLetterSpacing(reader.readFloat("minLetterSpacing"));
		setDesiredGlyphScaling(reader.readFloat("desiredGlyphScaling"));
		setMaxGlyphScaling(reader.readFloat("maxGlyphScaling"));
		setMinGlyphScaling(reader.readFloat("minGlyphSpacing"));
		setAutoLeadingPercentage(reader.readFloat("autoLeadingPercentage"));
		setLeading(reader.readEnum("leading", LeadingType.class));
		// TODO: setTabStops((TabStopList) reader.readObject("tabStop", TabStopList.class));
		setDefaultTabWidth(reader.readFloat("defaultTabWidth"));
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
			commit();
		return new ParagraphStyle(nativeClone());
	}
	
	protected native void nativeSetStyle(int handle, int rangeHandle);

	/**
	 * @jshide
	 */
	public void commit() {
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
	 * The justification of the paragraph.
	 * {@grouptitle Justification}
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
	 * The indentation of the first line in the paragraph.
	 * {@grouptitle Indentation}
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
	 * The space after the paragraph.
	 * {@grouptitle Spacing}
	 */
	public native Float getSpaceBefore();
	public native void setSpaceBefore(Float space);

	/**
	 * The space before the paragraph.
	 */
	public native Float getSpaceAfter();
	public native void setSpaceAfter(Float space);

	// ------------------------------------------------------------------
	// Hyphenation Features
	// ------------------------------------------------------------------
	
	/**
	 * Specifies whether to use hyphenation within the paragraph
	 * @return {@true if the paragraph uses hyphenation}
	 * {@grouptitle Hyphenation}
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
	 * The desired word spacing of the paragraph.
	 * {@grouptitle Word Spacing}
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
	 * The desired letter spacing of the paragraph.
	 * {@grouptitle Letter Spacing}
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
	 * The desired glyph scaling of the paragraph as a value between 0 and 1.
	 * {@grouptitle Glyph Scaling}
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
	 * The auto leading percentage of the paragraph as a value between 0 and 1.
	 * {@grouptitle Leading}
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
	 * The default tab width of the paragraph.
	 * {@grouptitle Tabs}
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
