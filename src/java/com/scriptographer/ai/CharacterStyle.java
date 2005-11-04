/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2004-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile: CharacterStyle.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/11/04 01:34:14 $
 */

package com.scriptographer.ai;

import com.scriptographer.CommitManager;

/**
 * CharacterStyle is built on top of PathStyle and adds the type related fields
 **/

/*
 * This pretty hackish due to the way PathStyle was implemented:
 * PathStyle is completely mirrored in java through fetch and commit
 * The same is done here for CharacterStyle, by using sAIATEPaint in order
 * to convert the CharFeaturesRef into AIPathStyle and back
 * 
 * All the additional type related fields are not mirrored but directly
 * changed on the underlying CharfeaturesRef
 * markDirty needs to be called for any of the changes
 * TODO: nativeCommit doesn't need to commit the path style if there weren't
 * any changes on it 
 */
public class CharacterStyle extends PathStyle {
	
	// AutoKernType
	public final int KERNING_MANUAL = 0;
	public final int KERNING_METRIC = 1;
	public final int KERNING_OPTICAL = 2;

	private TextRange range = null;
	private Object commitKey;
	private boolean pathStyleChanged = false;

	protected CharacterStyle(int handle, TextRange range) {
		super(handle);
		this.range = range;
		this.commitKey = range != null ? (Object) range.getStory() : (Object) this;
	}
	
	public native Object clone();
	
	protected void update() {
		// only update if it didn't change in the meantime:
		if (!fetched /* TODO: add version control here? || !dirty && art != null && version != art.version)*/)
			fetch();
	}
	
	protected native void nativeFetch(int handle);
	
	protected native void nativeCommit(int handle1, int handle2,
		float[] fillColor, boolean hasFillColor, short fillOverprint,
		float[] strokeColor, boolean hasStrokeColor, short strokeOverprint, float strokeWidth,
		float dashOffset, float[] dashArray,
		short cap, short join, float miterLimit,
		short clip, short lockClip, short evenOdd, float resolution);
	
	protected native void nativeSetStyle(int handle1, int handle2);

	protected void fetch() {
		nativeFetch(handle);
		fetched = true;
	}

	public void commit() {
		if (pathStyleChanged)
			nativeCommit(handle, range != null ? range.handle : 0);
		else if (range != null)
			nativeSetStyle(handle, range.handle);
		dirty = false;
	}

	/**
	 * markDirty is called when a pathStyle field is changed, see PathStyle
	 */
	protected void markDirty() {
		if (!dirty) {
			CommitManager.markDirty(commitKey, this);
			dirty = true;
			// markDirty is only called if PathStyle changes are made and they need to be
			pathStyleChanged = true;
		}
	}
	
	/**
	 * markSetStyle is called from the native environemnt. it marks dirty but doesn't set 
	 * pathStyleChanged, as it's only used for character style features 
	 */
	protected void markSetStyle() {
		if (!dirty) {
			CommitManager.markDirty(commitKey, this);
			dirty = true;
		}
	}
	
//	ATEErr (*GetFont) ( CharFeaturesRef charfeatures, bool* isAssigned, FontRef* ret);

	
	public native Float getFontSize();
	public native void setFontSize(Float size);
	
	public native Float getHorizontalScale();
	public native void setHorizontalScale(Float scale);

	public native Float getVerticalScale();
	public native void setVerticalScale(Float scale);

	public native Boolean getAutoLeading();
	public native void setAutoLeading(Boolean leading);

	public native Float getLeading();
	public native void setLeading(Float leading);

	public native Integer getTracking();
	public native void setTracking(Integer tracking);

	public native Float getBaselineShift();
	public native void setBaselineShift(Float shift);

	public native Float getRotation();
	public native void setRotation(Float rotation);

	public native Integer getKerningMethod();
	public native void setKerningMethod(Integer method);
	
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
	
	protected native void finalize();
}
