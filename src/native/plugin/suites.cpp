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
 * $Id$
 */
 
#include "stdHeaders.h"

#if kPluginInterfaceVersion >= kAI11
	#include "ATETextSuitesImportHelper.h"
#endif

extern "C" {
	// the basic suite doesn't need to be loaded:
	SPBasicSuite 					*sSPBasic;

	// suite globals
	SPBlocksSuite					*sSPBlocks;
	AIMdMemorySuite					*sAIMDMemory;
	SPAccessSuite					*sSPAccess;
	SPFilesSuite					*sSPFiles;
	SPPluginsSuite 					*sSPPlugins;

	AIAnnotatorSuite				*sAIAnnotator;
	AIArraySuite					*sAIArray;
	AIArtStyleSuite					*sAIArtStyle;
	AIArtSuite						*sAIArt;
	AIArtSetSuite					*sAIArtSet;
	AIATEPaintSuite					*sAIATEPaint;
	AIAppContextSuite				*sAIAppContext;
	AIBlendStyleSuite				*sAIBlendStyle;
	AIColorConversionSuite			*sAIColorConversion;
	AIOverrideColorConversionSuite	*sAIOverrideColorConversion;
	AICursorSnapSuite				*sAICursorSnap;
	AIDictionarySuite				*sAIDictionary;
	AIDictionaryIteratorSuite		*sAIDictionaryIterator;
	AIDocumentSuite					*sAIDocument;
	AIDocumentListSuite				*sAIDocumentList;
	AIDocumentViewSuite				*sAIDocumentView;
	AIEntrySuite					*sAIEntry;
	AIEnvelopeSuite					*sAIEnvelope;
	AIExpandSuite					*sAIExpand;
	AIFileFormatSuite				*sAIFileFormat;
	AIFilterSuite					*sAIFilter;
	AIFontSuite						*sAIFont;
	AIGradientSuite					*sAIGradient;
	AIGroupSuite					*sAIGroup;
	AIHitTestSuite					*sAIHitTest;
	AIHardSoftSuite					*sAIHardSoft;
	AILayerSuite					*sAILayer;
	AILiveEffectSuite				*sAILiveEffect;
	AIMatchingArtSuite				*sAIMatchingArt;
	AIMeshSuite						*sAIMesh;
	AIMenuSuite						*sAIMenu;
	AINotifierSuite					*sAINotifier;
	AIPaintStyleSuite				*sAIPaintStyle;
	AIPathConstructionSuite			*sAIPathConstruction;
	AIPathfinderSuite				*sAIPathfinder;
	AIPathInterpolateSuite			*sAIPathInterpolate;
	AIPathStyleSuite				*sAIPathStyle;
	AIPathSuite						*sAIPath;
	AIPluginGroupSuite				*sAIPluginGroup;
	AIPatternSuite					*sAIPattern;
	AIPlacedSuite					*sAIPlaced;
	AIPreferenceSuite				*sAIPreference;
	AIRasterSuite					*sAIRaster;
	AIRasterizeSuite				*sAIRasterize;
	AIRealBezierSuite				*sAIRealBezier;
	AIRealMathSuite					*sAIRealMath;
	AIRuntimeSuite					*sAIRuntime;
	AIShapeConstructionSuite		*sAIShapeConstruction;
	AISwatchListSuite				*sAISwatchList;
	AISymbolSuite					*sAISymbol;
	AISymbolPaletteSuite			*sAISymbolPalette;
	AITabletDataSuite				*sAITabletData;
	AITagSuite						*sAITag;
	AITextFrameHitSuite				*sAITextFrameHit;
	AIToolSuite						*sAITool;
	AITimerSuite					*sAITimer;
	AITransformArtSuite				*sAITransformArt;
	AIUserSuite						*sAIUser;
	AIUndoSuite						*sAIUndo;
	AIURLSuite						*sAIURL;

	_ADMBasicSuite					*sADMBasic;
	_ADMDialogSuite 				*sADMDialog;
	_ADMItemSuite					*sADMItem;
	_ADMIconSuite					*sADMIcon;
	_ADMImageSuite					*sADMImage;
	_ADMListSuite					*sADMList;
	_ADMHierarchyListSuite			*sADMHierarchyList;
	_ADMDialogGroupSuite			*sADMDialogGroup;
	_ADMNotifierSuite				*sADMNotifier;
	_ADMEntrySuite					*sADMEntry;
	_ADMListEntrySuite				*sADMListEntry;
	_ADMTrackerSuite				*sADMTracker;
	_ADMDrawerSuite					*sADMDrawer;
	
#ifdef MAC_ENV
	ADMMacHostSuite					*sADMMacHost;
#endif
#ifdef WIN_ENV
	ADMWinHostSuite					*sADMWinHost;
#endif

#if kPluginInterfaceVersion >= kAI11
	AITextFrameSuite				*sAITextFrame;
	AIATECurrentTextFeaturesSuite	*sAIATECurrentTextFeatures;
	EXTERN_TEXT_SUITES
#else
	AITextSuite						*sAIText;
	AITextFaceStyleSuite			*sAITextFaceStyle;
	AITextLineSuite					*sAITextLine;
	AITextPathSuite					*sAITextPath;
	AITextRunSuite					*sAITextRun;
	AITextStreamSuite				*sAITextStream;
#endif

#if kPluginInterfaceVersion >= kAI12
	AIFilePathSuite					*sAIFilePath;
	AIUnicodeStringSuite			*sAIUnicodeString;
	AITracingSuite					*sAITracing;
	AITracingIPSuite				*sAITracingIP;
#endif
}

// The startup and postStartup array contains all the suites which
// are aquired by the AcquireSuites() routine

// In order to be able to add CFM glue for CS1 on Mac, we need the 
// sizeof information for each suite as well.
// Use this regular expression to convert the list:
// ^((?:\s*)k([a-zA-Z0-9]*)(.*)),(?:\s*)$ -> \1, sizeof(\2),

// startup: all suites that are needed immediatelly (onStartupPlugin)
ImportSuite startup[] = {
	kADMBasicSuite, _kADMBasicSuiteVersion, &sADMBasic, sizeof(_ADMBasicSuite),

	kSPBlocksSuite, kSPBlocksSuiteVersion, &sSPBlocks, sizeof(SPBlocksSuite),
	kAIMdMemorySuite, kAIMdMemorySuiteVersion, &sAIMDMemory, sizeof(AIMdMemorySuite),
	kSPAccessSuite, kSPAccessSuiteVersion, &sSPAccess, sizeof(SPAccessSuite),
	kSPFilesSuite, kSPFilesSuiteVersion, &sSPFiles, sizeof(SPFilesSuite),
	kSPPluginsSuite, kSPPluginsSuiteVersion, &sSPPlugins, sizeof(SPPluginsSuite),
	kAIAppContextSuite, kAIAppContextSuiteVersion, &sAIAppContext, sizeof(AIAppContextSuite),
	kAIBlendStyleSuite, kAIBlendStyleSuiteVersion, &sAIBlendStyle, sizeof(AIBlendStyleSuite),
	kAINotifierSuite, kAINotifierVersion, &sAINotifier, sizeof(AINotifierSuite),
	kAIToolSuite, kAIToolVersion, &sAITool, sizeof(AIToolSuite),
	kAIUserSuite, kAIUserSuiteVersion, &sAIUser, sizeof(AIUserSuite),

	// We need these previously post-startup suites now already, since the scripting engine is used	
	// in pre startup to add toolbar buttons.
	// Needed for fetching current document
	kAIDocumentSuite, kAIDocumentSuiteVersion, &sAIDocument, sizeof(AIDocumentSuite),
	// Needed to create global dictionary symbols
	kAIDictionarySuite, kAIDictionaryVersion, &sAIDictionary, sizeof(AIDictionarySuite),
	// Needed to get version numbers
	kAIRuntimeSuite, kAIRuntimeVersion, &sAIRuntime, sizeof(AIRuntimeSuite),
	// Needed to install menus
	kAIMenuSuite, kAIMenuVersion, &sAIMenu, sizeof(AIMenuSuite),
#if kPluginInterfaceVersion >= kAI12
	// Needed for string conversion by various underlying method calls
	kAIUnicodeStringSuite, kAIUnicodeStringSuiteVersion, &sAIUnicodeString, sizeof(AIUnicodeStringSuite),
#endif

	// ADM
	kADMBasicSuite, _kADMBasicSuiteVersion, &sADMBasic, sizeof(_ADMBasicSuite),
	kADMDialogSuite, _kADMDialogSuiteVersion, &sADMDialog, sizeof(_ADMDialogSuite),
	kADMItemSuite, _kADMItemSuiteVersion, &sADMItem, sizeof(_ADMItemSuite),
	kADMIconSuite, _kADMIconSuiteVersion, &sADMIcon, sizeof(_ADMIconSuite),
	kADMImageSuite, _kADMImageSuiteVersion, &sADMImage, sizeof(_ADMImageSuite),
	kADMListSuite, _kADMListSuiteVersion, &sADMList, sizeof(_ADMListSuite),
	kADMHierarchyListSuite, _kADMHierarchyListSuiteVersion, &sADMHierarchyList, sizeof(_ADMHierarchyListSuite),
	kADMDialogGroupSuite, _kADMDialogGroupSuiteVersion, &sADMDialogGroup, sizeof(_ADMDialogGroupSuite),
	kADMNotifierSuite, _kADMNotifierSuiteVersion, &sADMNotifier, sizeof(_ADMNotifierSuite),
	kADMEntrySuite, _kADMEntrySuiteVersion, &sADMEntry, sizeof(_ADMEntrySuite),
	kADMListEntrySuite, _kADMListEntrySuiteVersion, &sADMListEntry, sizeof(_ADMListEntrySuite),
	kADMTrackerSuite, _kADMTrackerSuiteVersion, &sADMTracker, sizeof(_ADMTrackerSuite),
	kADMDrawerSuite, _kADMDrawerSuiteVersion, &sADMDrawer, sizeof(_ADMDrawerSuite),
	#ifdef MAC_ENV
	kADMMacHostSuite, kADMMacHostSuiteVersion, &sADMMacHost, sizeof(ADMMacHostSuite),
	#endif
	#ifdef WIN_ENV
	kADMWinHostSuite, kADMWinHostSuiteVersion, &sADMWinHost, sizeof(ADMWinHostSuite),
	#endif

	NULL, 0, NULL, 0
};

// postStartup: all suites that are needed after startup (onPostStartupPlugin)
ImportSuite postStartup[] = {
	kAIAnnotatorSuite, kAIAnnotatorVersion,	&sAIAnnotator, sizeof(AIAnnotatorSuite),
	kAIArraySuite, kAIArraySuiteVersion, &sAIArray, sizeof(AIArraySuite),
	kAIArtStyleSuite, kAIArtStyleVersion, &sAIArtStyle, sizeof(AIArtStyleSuite),
	kAIArtSuite, kAIArtVersion, &sAIArt, sizeof(AIArtSuite),
	kAIArtSetSuite, kAIArtSetVersion, &sAIArtSet, sizeof(AIArtSetSuite),
	kAIATEPaintSuite, kAIATEPaintSuiteVersion, &sAIATEPaint, sizeof(AIATEPaintSuite),
	kAIColorConversionSuite, kAIColorConversionVersion, &sAIColorConversion, sizeof(AIColorConversionSuite),
	kAIOverrideColorConversionSuite, kAIOverrideColorConversionVersion, &sAIOverrideColorConversion, sizeof(AIOverrideColorConversionSuite),
	kAICursorSnapSuite, kAICursorSnapSuiteVersion, &sAICursorSnap, sizeof(AICursorSnapSuite),
	kAIDictionaryIteratorSuite, kAIDictionaryIteratorVersion, &sAIDictionaryIterator, sizeof(AIDictionaryIteratorSuite),
	kAICursorSnapSuite, kAICursorSnapSuiteVersion, &sAICursorSnap, sizeof(AICursorSnapSuite),
	kAIDocumentListSuite, kAIDocumentListSuiteVersion, &sAIDocumentList, sizeof(AIDocumentListSuite),
	kAIDocumentViewSuite, kAIDocumentViewSuiteVersion, &sAIDocumentView, sizeof(AIDocumentViewSuite),
	kAIEntrySuite, kAIEntryVersion, &sAIEntry, sizeof(AIEntrySuite),
	kAIEnvelopeSuite, kAIEnvelopeSuiteVersion, &sAIEnvelope, sizeof(AIEnvelopeSuite),
	kAIExpandSuite, kAIExpandSuiteVersion, &sAIExpand, sizeof(AIExpandSuite),
	kAIFileFormatSuite, kAIFileFormatSuiteVersion, &sAIFileFormat, sizeof(AIFileFormatSuite),
	kAIFilterSuite, kAIFilterVersion, &sAIFilter, sizeof(AIFilterSuite),
	kAIFontSuite, kAIFontVersion, &sAIFont, sizeof(AIFontSuite),
	kAIGradientSuite, kAIGradientVersion, &sAIGradient, sizeof(AIGradientSuite),
	kAIGroupSuite, kAIGroupVersion, &sAIGroup, sizeof(AIGroupSuite),
	kAIHitTestSuite, kAIHitTestSuiteVersion, &sAIHitTest, sizeof(AIHitTestSuite),
	kAIHardSoftSuite, kAIHardSoftSuiteVersion, &sAIHardSoft, sizeof(AIHardSoftSuite),
	kAILayerSuite, kAILayerVersion, &sAILayer, sizeof(AILayerSuite),
	kAILiveEffectSuite, kAILiveEffectVersion, &sAILiveEffect, sizeof(AILiveEffectSuite),
	kAIMatchingArtSuite, kAIMatchingArtVersion, &sAIMatchingArt, sizeof(AIMatchingArtSuite),
	kAIMeshSuite, kAIMeshVersion, &sAIMesh, sizeof(AIMeshSuite),
	kAIPaintStyleSuite, kAIPaintStyleSuiteVersion, &sAIPaintStyle, sizeof(AIPaintStyleSuite),
	kAIPathConstructionSuite, kAIPathConstructionSuiteVersion, &sAIPathConstruction, sizeof(AIPathConstructionSuite),
	kAIPathfinderSuite, kAIPathfinderSuiteVersion, &sAIPathfinder, sizeof(AIPathfinderSuite),
	kAIPathInterpolateSuite, kAIPathInterpolateSuiteVersion, &sAIPathInterpolate, sizeof(AIPathInterpolateSuite),
	kAIPathStyleSuite, kAIPathStyleVersion, &sAIPathStyle, sizeof(AIPathStyleSuite),
	kAIPathSuite, kAIPathSuiteVersion, &sAIPath, sizeof(AIPathSuite),
	kAIPatternSuite, kAIPatternVersion, &sAIPattern, sizeof(AIPatternSuite),
	kAIPluginGroupSuite, kAIPluginGroupVersion, &sAIPluginGroup, sizeof(AIPluginGroupSuite),
	kAIPlacedSuite, kAIPlacedVersion, &sAIPlaced, sizeof(AIPlacedSuite),
	kAIPreferenceSuite, kAIPreferenceSuiteVersion, &sAIPreference, sizeof(AIPreferenceSuite),
	kAIRasterSuite, kAIRasterSuiteVersion, &sAIRaster, sizeof(AIRasterSuite),
	kAIRasterizeSuite, kAIRasterizeSuiteVersion, &sAIRasterize, sizeof(AIRasterizeSuite),
	kAIRealBezierSuite, kAIRealBezierSuiteVersion, &sAIRealBezier, sizeof(AIRealBezierSuite),
	kAIRealMathSuite, kAIRealMathVersion, &sAIRealMath, sizeof(AIRealMathSuite),
	kAIShapeConstructionSuite, kAIShapeConstructionSuiteVersion, &sAIShapeConstruction, sizeof(AIShapeConstructionSuite),
	kAISwatchListSuite, kAISwatchListSuiteVersion, &sAISwatchList, sizeof(AISwatchListSuite),
	kAISymbolSuite, kAISymbolSuiteVersion, &sAISymbol, sizeof(AISymbolSuite),
	kAISymbolPaletteSuite, kAISymbolPaletteSuiteVersion, &sAISymbolPalette, sizeof(AISymbolPaletteSuite),
	kAITabletDataSuite, kAITabletDataVersion, &sAITabletData, sizeof(AITabletDataSuite),
	kAITagSuite, kAITagVersion, &sAITag, sizeof(AITagSuite),
	kAITextFrameHitSuite, kAITextFrameHitVersion, &sAITextFrameHit, sizeof(AITextFrameHitSuite),
	kAITimerSuite, kAITimerVersion, &sAITimer, sizeof(AITimerSuite),
	kAITransformArtSuite, kAITransformArtSuiteVersion, &sAITransformArt, sizeof(AITransformArtSuite),
	kAIUndoSuite, kAIUndoSuiteVersion, &sAIUndo, sizeof(AIUndoSuite),
	kAIURLSuite, kAIURLSuiteVersion, &sAIURL, sizeof(AIURLSuite),

#if kPluginInterfaceVersion >= kAI11
	kAITextFrameSuite, kAITextFrameSuiteVersion, &sAITextFrame, sizeof(AITextFrameSuite),
	kAIATECurrentTextFeaturesSuite, kAIATECurrentTextFeaturesSuiteVersion, &sAIATECurrentTextFeatures, sizeof(AIATECurrentTextFeaturesSuite),
	// Content of IMPORT_TEXT_SUITES, with added sizeof fields:
	kApplicationPaintSuite, kApplicationPaintSuiteVersion, &sApplicationPaint, sizeof(ATE::ApplicationPaintSuite),
	kCompFontSuite, kCompFontSuiteVersion, &sCompFont, sizeof(ATE::CompFontSuite),
	kCompFontClassSuite, kCompFontClassSuiteVersion, &sCompFontClass, sizeof(ATE::CompFontClassSuite),
	kCompFontClassSetSuite, kCompFontClassSetSuiteVersion, &sCompFontClassSet, sizeof(ATE::CompFontClassSetSuite),
	kCompFontComponentSuite, kCompFontComponentSuiteVersion, &sCompFontComponent, sizeof(ATE::CompFontComponentSuite),
	kCompFontSetSuite, kCompFontSetSuiteVersion, &sCompFontSet, sizeof(ATE::CompFontSetSuite),
	kGlyphRunSuite, kGlyphRunSuiteVersion, &sGlyphRun, sizeof(ATE::GlyphRunSuite),
	kGlyphRunsIteratorSuite, kGlyphRunsIteratorSuiteVersion, &sGlyphRunsIterator, sizeof(ATE::GlyphRunsIteratorSuite),
	kMojiKumiSuite, kMojiKumiSuiteVersion, &sMojiKumi, sizeof(ATE::MojiKumiSuite),
	kMojiKumiSetSuite, kMojiKumiSetSuiteVersion, &sMojiKumiSet, sizeof(ATE::MojiKumiSetSuite),
	kTextFrameSuite, kTextFrameSuiteVersion, &sTextFrame, sizeof(ATE::TextFrameSuite),
	kTextFramesIteratorSuite, kTextFramesIteratorSuiteVersion, &sTextFramesIterator, sizeof(ATE::TextFramesIteratorSuite),
	kTextLineSuite, kTextLineSuiteVersion, &sTextLine, sizeof(ATE::TextLineSuite),
	kTextLinesIteratorSuite, kTextLinesIteratorSuiteVersion, &sTextLinesIterator, sizeof(ATE::TextLinesIteratorSuite),
	kTextResourcesSuite, kTextResourcesSuiteVersion, &sTextResources, sizeof(ATE::TextResourcesSuite),
	kApplicationTextResourcesSuite, kApplicationTextResourcesSuiteVersion, &sApplicationTextResources, sizeof(ATE::ApplicationTextResourcesSuite),
	kDocumentTextResourcesSuite, kDocumentTextResourcesSuiteVersion, &sDocumentTextResources, sizeof(ATE::DocumentTextResourcesSuite),
	kVersionInfoSuite, kVersionInfoSuiteVersion, &sVersionInfo, sizeof(ATE::VersionInfoSuite),
	kArrayApplicationPaintRefSuite, kArrayApplicationPaintRefSuiteVersion, &sArrayApplicationPaintRef, sizeof(ATE::ArrayApplicationPaintRefSuite),
	kArrayRealSuite, kArrayRealSuiteVersion, &sArrayReal, sizeof(ATE::ArrayRealSuite),
	kArrayBoolSuite, kArrayBoolSuiteVersion, &sArrayBool, sizeof(ATE::ArrayBoolSuite),
	kArrayIntegerSuite, kArrayIntegerSuiteVersion, &sArrayInteger, sizeof(ATE::ArrayIntegerSuite),
	kArrayLineCapTypeSuite, kArrayLineCapTypeSuiteVersion, &sArrayLineCapType, sizeof(ATE::ArrayLineCapTypeSuite),
	kArrayFigureStyleSuite, kArrayFigureStyleSuiteVersion, &sArrayFigureStyle, sizeof(ATE::ArrayFigureStyleSuite),
	kArrayLineJoinTypeSuite, kArrayLineJoinTypeSuiteVersion, &sArrayLineJoinType, sizeof(ATE::ArrayLineJoinTypeSuite),
	kArrayWariChuJustificationSuite, kArrayWariChuJustificationSuiteVersion, &sArrayWariChuJustification, sizeof(ATE::ArrayWariChuJustificationSuite),
	kArrayStyleRunAlignmentSuite, kArrayStyleRunAlignmentSuiteVersion, &sArrayStyleRunAlignment, sizeof(ATE::ArrayStyleRunAlignmentSuite),
	kArrayAutoKernTypeSuite, kArrayAutoKernTypeSuiteVersion, &sArrayAutoKernType, sizeof(ATE::ArrayAutoKernTypeSuite),
	kArrayBaselineDirectionSuite, kArrayBaselineDirectionSuiteVersion, &sArrayBaselineDirection, sizeof(ATE::ArrayBaselineDirectionSuite),
	kArrayLanguageSuite, kArrayLanguageSuiteVersion, &sArrayLanguage, sizeof(ATE::ArrayLanguageSuite),
	kArrayFontCapsOptionSuite, kArrayFontCapsOptionSuiteVersion, &sArrayFontCapsOption, sizeof(ATE::ArrayFontCapsOptionSuite),
	kArrayFontBaselineOptionSuite, kArrayFontBaselineOptionSuiteVersion, &sArrayFontBaselineOption, sizeof(ATE::ArrayFontBaselineOptionSuite),
	kArrayFontOpenTypePositionOptionSuite, kArrayFontOpenTypePositionOptionSuiteVersion, &sArrayFontOpenTypePositionOption, sizeof(ATE::ArrayFontOpenTypePositionOptionSuite),
	kArrayUnderlinePositionSuite, kArrayUnderlinePositionSuiteVersion, &sArrayUnderlinePosition, sizeof(ATE::ArrayUnderlinePositionSuite),
	kArrayStrikethroughPositionSuite, kArrayStrikethroughPositionSuiteVersion, &sArrayStrikethroughPosition, sizeof(ATE::ArrayStrikethroughPositionSuite),
	kArrayParagraphJustificationSuite, kArrayParagraphJustificationSuiteVersion, &sArrayParagraphJustification, sizeof(ATE::ArrayParagraphJustificationSuite),
	kArrayArrayRealSuite, kArrayArrayRealSuiteVersion, &sArrayArrayReal, sizeof(ATE::ArrayArrayRealSuite),
	kArrayBurasagariTypeSuite, kArrayBurasagariTypeSuiteVersion, &sArrayBurasagariType, sizeof(ATE::ArrayBurasagariTypeSuite),
	kArrayPreferredKinsokuOrderSuite, kArrayPreferredKinsokuOrderSuiteVersion, &sArrayPreferredKinsokuOrder, sizeof(ATE::ArrayPreferredKinsokuOrderSuite),
	kArrayKinsokuRefSuite, kArrayKinsokuRefSuiteVersion, &sArrayKinsokuRef, sizeof(ATE::ArrayKinsokuRefSuite),
	kArrayMojiKumiRefSuite, kArrayMojiKumiRefSuiteVersion, &sArrayMojiKumiRef, sizeof(ATE::ArrayMojiKumiRefSuite),
	kArrayMojiKumiSetRefSuite, kArrayMojiKumiSetRefSuiteVersion, &sArrayMojiKumiSetRef, sizeof(ATE::ArrayMojiKumiSetRefSuite),
	kArrayTabStopsRefSuite, kArrayTabStopsRefSuiteVersion, &sArrayTabStopsRef, sizeof(ATE::ArrayTabStopsRefSuite),
	kArrayLeadingTypeSuite, kArrayLeadingTypeSuiteVersion, &sArrayLeadingType, sizeof(ATE::ArrayLeadingTypeSuite),
	kArrayFontRefSuite, kArrayFontRefSuiteVersion, &sArrayFontRef, sizeof(ATE::ArrayFontRefSuite),
	kArrayGlyphIDSuite, kArrayGlyphIDSuiteVersion, &sArrayGlyphID, sizeof(ATE::ArrayGlyphIDSuite),
	kArrayRealPointSuite, kArrayRealPointSuiteVersion, &sArrayRealPoint, sizeof(ATE::ArrayRealPointSuite),
	kArrayRealMatrixSuite, kArrayRealMatrixSuiteVersion, &sArrayRealMatrix, sizeof(ATE::ArrayRealMatrixSuite),
	kCharFeaturesSuite, kCharFeaturesSuiteVersion, &sCharFeatures, sizeof(ATE::CharFeaturesSuite),
	kCharInspectorSuite, kCharInspectorSuiteVersion, &sCharInspector, sizeof(ATE::CharInspectorSuite),
	kCharStyleSuite, kCharStyleSuiteVersion, &sCharStyle, sizeof(ATE::CharStyleSuite),
	kCharStylesSuite, kCharStylesSuiteVersion, &sCharStyles, sizeof(ATE::CharStylesSuite),
	kCharStylesIteratorSuite, kCharStylesIteratorSuiteVersion, &sCharStylesIterator, sizeof(ATE::CharStylesIteratorSuite),
	kFindSuite, kFindSuiteVersion, &sFind, sizeof(ATE::FindSuite),
	kFontSuite, kFontSuiteVersion, &sFont, sizeof(ATE::FontSuite),
	kGlyphSuite, kGlyphSuiteVersion, &sGlyph, sizeof(ATE::GlyphSuite),
	kGlyphsSuite, kGlyphsSuiteVersion, &sGlyphs, sizeof(ATE::GlyphsSuite),
	kGlyphsIteratorSuite, kGlyphsIteratorSuiteVersion, &sGlyphsIterator, sizeof(ATE::GlyphsIteratorSuite),
	kKinsokuSuite, kKinsokuSuiteVersion, &sKinsoku, sizeof(ATE::KinsokuSuite),
	kKinsokuSetSuite, kKinsokuSetSuiteVersion, &sKinsokuSet, sizeof(ATE::KinsokuSetSuite),
	kParaFeaturesSuite, kParaFeaturesSuiteVersion, &sParaFeatures, sizeof(ATE::ParaFeaturesSuite),
	kParagraphSuite, kParagraphSuiteVersion, &sParagraph, sizeof(ATE::ParagraphSuite),
	kParagraphsIteratorSuite, kParagraphsIteratorSuiteVersion, &sParagraphsIterator, sizeof(ATE::ParagraphsIteratorSuite),
	kParaInspectorSuite, kParaInspectorSuiteVersion, &sParaInspector, sizeof(ATE::ParaInspectorSuite),
	kParaStyleSuite, kParaStyleSuiteVersion, &sParaStyle, sizeof(ATE::ParaStyleSuite),
	kParaStylesSuite, kParaStylesSuiteVersion, &sParaStyles, sizeof(ATE::ParaStylesSuite),
	kParaStylesIteratorSuite, kParaStylesIteratorSuiteVersion, &sParaStylesIterator, sizeof(ATE::ParaStylesIteratorSuite),
	kSpellSuite, kSpellSuiteVersion, &sSpell, sizeof(ATE::SpellSuite),
	kStoriesSuite, kStoriesSuiteVersion, &sStories, sizeof(ATE::StoriesSuite),
	kStorySuite, kStorySuiteVersion, &sStory, sizeof(ATE::StorySuite),
	kTabStopSuite, kTabStopSuiteVersion, &sTabStop, sizeof(ATE::TabStopSuite),
	kTabStopsSuite, kTabStopsSuiteVersion, &sTabStops, sizeof(ATE::TabStopsSuite),
	kTabStopsIteratorSuite, kTabStopsIteratorSuiteVersion, &sTabStopsIterator, sizeof(ATE::TabStopsIteratorSuite),
	kTextRangeSuite, kTextRangeSuiteVersion, &sTextRange, sizeof(ATE::TextRangeSuite),
	kTextRangesSuite, kTextRangesSuiteVersion, &sTextRanges, sizeof(ATE::TextRangesSuite),
	kTextRangesIteratorSuite, kTextRangesIteratorSuiteVersion, &sTextRangesIterator, sizeof(ATE::TextRangesIteratorSuite),
	kTextRunsIteratorSuite, kTextRunsIteratorSuiteVersion, &sTextRunsIterator, sizeof(ATE::TextRunsIteratorSuite),
	kWordsIteratorSuite, kWordsIteratorSuiteVersion, &sWordsIterator, sizeof(ATE::WordsIteratorSuite),
#else
	kAITextSuite, kAITextSuiteVersion, &sAIText, sizeof(AITextSuite),
	kAITextFaceStyleSuite, kAITextFaceStyleSuiteVersion, &sAITextFaceStyle, sizeof(AITextFaceStyleSuite),
	kAITextLineSuite, kAITextLineSuiteVersion, &sAITextLine, sizeof(AITextLineSuite),
	kAITextPathSuite, kAITextPathSuiteVersion, &sAITextPath, sizeof(AITextPathSuite),
	kAITextRunSuite, kAITextRunSuiteVersion, &sAITextRun, sizeof(AITextRunSuite),
	kAITextStreamSuite, kAITextStreamSuiteVersion, &sAITextStream, sizeof(AITextStreamSuite),
#endif
	
#if kPluginInterfaceVersion >= kAI12
	kAIFilePathSuite, kAIFilePathSuiteVersion, &sAIFilePath, sizeof(AIFilePathSuite),
	kAITracingSuite, kAITracingSuiteVersion, &sAITracing, sizeof(AITracingSuite),
	kAITracingIPSuite, kAITracingIPSuiteVersion, &sAITracingIP, sizeof(AITracingIPSuite),
#endif
	
	NULL, 0, NULL, 0
};

ImportSuites gStartupSuites = {
	startup,
	false,
};

ImportSuites gPostStartupSuites = {
	postStartup,
	false,
};
