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
 */

// Define versions so they can even be used when compiling for 10 or CS1:
#define kAI11	0x11000001	// AI 11.0 (CS)
#define kAI12	0x12000001	// AI 12.0 (CS2)
#define kAI13	0x13000001	// AI 13.0 (CS3)
#define kAI14	0x14000001	// AI 14.0 (CS4)
#define kAI15	0x15000001	// AI 15.0 (CS5)
#define kAI16	0x16000001	// AI 16.0 (CS6)
#define kAI17	0x17000001	// AI 17.0 (CC)

// Sweet Pea Headers
#include "SPConfig.h"
#include "SPTypes.h"


// Illustrator Headers
#include "AITypes.h"

#if kPluginInterfaceVersion >= kAI16 
	#define ADM_FREE


#else
	#undef ADM_FREE


#endif // kPluginInterfaceVersion >= kAI16  

// Sweet Pea Suites
#include "SPBlocks.h"
#include "SPAccess.h"
#include "SPInterf.h"
#include "SPRuntme.h" 
#include "SPSuites.h"
#include "SPFiles.h"

#ifndef ADM_FREE
// ADM Suites
#include "ADMBasic.h"
#include "ADMDialog.h"
#include "ADMHost.h"
#include "ADMItem.h"
#include "ADMIcon.h"
#include "ADMImage.h"
#include "ADMList.h"
#include "ADMHierarchyList.h"
#include "ADMDialogGroup.h"
#include "ADMNotifier.h"
#include "ADMEntry.h"
#include "ADMListEntry.h"
#include "ADMTracker.h"
#include "ADMDrawer.h"
#include "ADMResource.h"

#endif //#ifndef ADM_FREE

// System Suites
#include "AIPlugin.h"
#include "AIMDMemory.h"

// General Suites
#include "AIAnnotator.h"
#include "AIArray.h"
#include "AIArt.h"
#include "AIArtSet.h"
#include "AIArtStyle.h"
#include "AIArtStyleParser.h"
#include "AIBlock.h"
#include "AIOverrideColorConversion.h"
#include "AIColorConversion.h"
#include "AIContext.h"
#include "AICursorSnap.h"
#include "AICustomColor.h"
#include "AIDocument.h"
#include "AIDocumentList.h"
#include "AIDocumentView.h"
#include "AIDrawArt.h"
#include "AIEnvelope.h"
#include "AIExpand.h"
#include "AIFileFormat.h"
#include "AIFilter.h"
#include "AIGradient.h"
#include "AIGroup.h"
#include "AIHardSoft.h"
#include "AIHitTest.h"
#include "AILayer.h"
#include "AILiveEffect.h"
#include "AIMask.h"
#include "AIMatchingArt.h"
#include "AIMenu.h"
#include "AIMesh.h"
#include "AIMenuGroups.h"
#include "AINotifier.h"
#include "AIPaintStyle.h"
#include "AIPaintStyle.h"
#include "AIPath.h"
#include "AIPathStyle.h"
#include "AIPathConstruction.h"
#include "AIPathfinder.h"
#include "AIPathInterpolate.h"
#include "AIPattern.h"
#include "AIPlaced.h"
#include "AIPluginGroup.h"
#include "AIRandom.h"
#include "AIRaster.h"
#include "AIRasterize.h"
#include "AIRealMath.h"
#include "AIRuntime.h"
#include "AIShapeConstruction.h"
#include "AISwatchList.h"
#include "AISymbol.h"
#include "AITabletData.h"
#include "AITag.h"
#include "AITimer.h"
#include "AITool.h"
#include "AITransformArt.h"
#include "AIUser.h"
#include "AIUndo.h"
#include "AIURL.h"

#include "AIContext.h"
#include "AIPreference.h"

#include "AITextFrame.h"
#include "IText.h"
#include "AIATEPaint.h"
#include "AIATECurrTextFeatures.h"
#include "ATETextSuitesExtern.h"

#if kPluginInterfaceVersion >= kAI12 && kPluginInterfaceVersion <= kAI15
#include "AITracing.h"
#endif // kPluginInterfaceVersion >= kAI12 && kPluginInterfaceVersion <= kAI15

#if kPluginInterfaceVersion >= kAI13 && kPluginInterfaceVersion <= kAI15
#include "AICropArea.h"
#endif // kPluginInterfaceVersion >= kAI13 && kPluginInterfaceVersion <= kAI15

#if kPluginInterfaceVersion >= kAI15
#include "AIArtboard.h"
#include "IAIArtboards.hpp"
#endif // kPluginInterfaceVersion >= kAI15

#if kPluginInterfaceVersion >= kAI16
#include "AIPanel.h"

#endif // kPluginInterfaceVersion >= kAI15


#if kPluginInterfaceVersion <= kAI11
#define kTabletTypeCount 1
#endif // kPluginInterfaceVersion <= kAI11

typedef struct {
	char *name;
	int version;
	void *suite;
	int size;
} ImportSuite;

typedef struct {
	ImportSuite* suites;
	ASBoolean acquired;
} ImportSuites;

extern ImportSuites gStartupSuites;
extern ImportSuites gPostStartupSuites;

#if kPluginInterfaceVersion < kAI12
// GetWSProfile in AIOverrideColorConversion.h takes AIColorProfile instead of ASUInt32 since AI12
#define AIColorProfile ASUInt32
// was renamed in AI12:
namespace ATE {
	typedef ATE::GlyphID ATEGlyphID;
}
#endif // kPluginInterfaceVersion < kAI12

#if kPluginInterfaceVersion < kAI13
// AI13 Introduced ATEBool8 for ATE, before it simply was bool:
namespace ATE {
	typedef bool ATEBool8;
}
#endif // kPluginInterfaceVersion < kAI13

#if kPluginInterfaceVersion < kAI16
namespace ai {
	typedef long int32;
	typedef short int16;
}
	typedef ai::int32 sizet; //klio todo: types.h namespace sc: types from kAI
#else
	typedef  size_t sizet; //klio todo: types.h
#endif // kPluginInterfaceVersion < kAI16

#if kPluginInterfaceVersion <= kAI16
	typedef ASRealMatrix RealMatrix;
	typedef ASRealPoint  RealPoint;
#else
	typedef ATETextDOM::RealMatrix RealMatrix;
	typedef ATETextDOM::RealPoint  RealPoint;
#endif


#ifndef ADM_FREE
// ADM Suite versions for different versions of Illustrator
// ADM Suites default to the oldest versions.
// Define symbols that point to the newest here und use these bellow:

#define _kADMIconSuiteVersion 2
#define _kADMImageSuiteVersion 2
#define _kADMListSuiteVersion 4
#define _kADMHierarchyListSuiteVersion 5
#define _kADMListEntrySuiteVersion 4

#if kPluginInterfaceVersion >= kAI15

#define _kADMDialogSuiteVersion 10
#define _kADMBasicSuiteVersion 11
#define _kADMItemSuiteVersion 9
#define _kADMEntrySuiteVersion 6
#define _kADMNotifierSuiteVersion 2
#define _kADMDialogGroupSuiteVersion 7
#define _kADMDrawerSuiteVersion 7
#define _kADMTrackerSuiteVersion 2
#define _kADMHostSuiteVersion 7

#elif kPluginInterfaceVersion >= kAI14

#define _kADMDialogSuiteVersion 10
#define _kADMBasicSuiteVersion 11
#define _kADMItemSuiteVersion 9
#define _kADMEntrySuiteVersion 6
#define _kADMNotifierSuiteVersion 2
#define _kADMDialogGroupSuiteVersion 7
#define _kADMDrawerSuiteVersion 7
#define _kADMTrackerSuiteVersion 2
#define _kADMHostSuiteVersion 7

#elif kPluginInterfaceVersion >= kAI13

#define _kADMDialogSuiteVersion 10
#define _kADMBasicSuiteVersion 10
#define _kADMItemSuiteVersion 9
#define _kADMEntrySuiteVersion 6
#define _kADMNotifierSuiteVersion 2
#define _kADMDialogGroupSuiteVersion 6
#define _kADMDrawerSuiteVersion 6
#define _kADMTrackerSuiteVersion 2
#define _kADMHostSuiteVersion 7

#elif kPluginInterfaceVersion >= kAI12

#define _kADMDialogSuiteVersion 9
#define _kADMBasicSuiteVersion 9
#define _kADMItemSuiteVersion 9
#define _kADMEntrySuiteVersion 6
#define _kADMNotifierSuiteVersion 2
#define _kADMDialogGroupSuiteVersion 4
#define _kADMDrawerSuiteVersion 5
#define _kADMTrackerSuiteVersion 1
#define _kADMHostSuiteVersion 6

#elif kPluginInterfaceVersion >= kAI11

#define _kADMDialogSuiteVersion 9
#define _kADMBasicSuiteVersion 8
#define _kADMItemSuiteVersion 9
#define _kADMEntrySuiteVersion 6
#define _kADMNotifierSuiteVersion 2
#define _kADMDialogGroupSuiteVersion 3
#define _kADMDrawerSuiteVersion 5
#define _kADMTrackerSuiteVersion 1
#define _kADMHostSuiteVersion 6

#endif

// Macros to concatonate the suite name with the version number, to point
// to the right structs

#define CONCAT(A,B) PASTE(A,B)
#define INSERT(A) A
#define PASTE(A,B) INSERT(A##B)

// References to the structs to use, based on their versions

#define _ADMBasicSuite CONCAT(ADMBasicSuite, _kADMBasicSuiteVersion)
#define _ADMDialogSuite CONCAT(ADMDialogSuite, _kADMDialogSuiteVersion)
#define _ADMItemSuite CONCAT(ADMItemSuite, _kADMItemSuiteVersion)
#define _ADMIconSuite CONCAT(ADMIconSuite, _kADMIconSuiteVersion)
#define _ADMImageSuite CONCAT(ADMImageSuite, _kADMImageSuiteVersion)
#define _ADMListSuite CONCAT(ADMListSuite, _kADMListSuiteVersion)
#define _ADMHierarchyListSuite CONCAT(ADMHierarchyListSuite, _kADMHierarchyListSuiteVersion)
#define _ADMDialogGroupSuite CONCAT(ADMDialogGroupSuite, _kADMDialogGroupSuiteVersion)
#define _ADMNotifierSuite CONCAT(ADMNotifierSuite, _kADMNotifierSuiteVersion)
#define _ADMEntrySuite CONCAT(ADMEntrySuite, _kADMEntrySuiteVersion)
#define _ADMListEntrySuite CONCAT(ADMListEntrySuite, _kADMListEntrySuiteVersion)
#define _ADMTrackerSuite CONCAT(ADMTrackerSuite, _kADMTrackerSuiteVersion)
#define _ADMDrawerSuite CONCAT(ADMDrawerSuite, _kADMDrawerSuiteVersion)
#define _ADMHostSuite CONCAT(ADMHostSuite, _kADMHostSuiteVersion)


#endif //#ifndef ADM_FREE

// The basic suite doesn't need to be loaded:
extern "C" SPBasicSuite 					*sSPBasic;

// Suite globals
extern "C" SPBlocksSuite					*sSPBlocks;
extern "C" AIMdMemorySuite					*sAIMDMemory;
extern "C" SPAccessSuite					*sSPAccess;
extern "C" SPFilesSuite						*sSPFiles;
extern "C" SPPluginsSuite 					*sSPPlugins;

extern "C" AIAnnotatorSuite					*sAIAnnotator;
extern "C" AIArraySuite						*sAIArray;
extern "C" AIArtStyleSuite					*sAIArtStyle;
extern "C" AIArtStyleParserSuite			*sAIArtStyleParser;
extern "C" AIArtSuite						*sAIArt;
extern "C" AIArtSetSuite					*sAIArtSet;
extern "C" AIATEPaintSuite					*sAIATEPaint;
extern "C" AIAppContextSuite				*sAIAppContext;
extern "C" AIBlendStyleSuite				*sAIBlendStyle;
extern "C" AIColorConversionSuite			*sAIColorConversion;
extern "C" AIOverrideColorConversionSuite	*sAIOverrideColorConversion;
extern "C" AICursorSnapSuite				*sAICursorSnap;
extern "C" AIDictionarySuite				*sAIDictionary;
extern "C" AIDictionaryIteratorSuite		*sAIDictionaryIterator;
extern "C" AIDocumentSuite					*sAIDocument;
extern "C" AIDocumentListSuite				*sAIDocumentList;
extern "C" AIDocumentViewSuite				*sAIDocumentView;
extern "C" AIDrawArtSuite					*sAIDrawArt;
extern "C" AIEntrySuite						*sAIEntry;
extern "C" AIEnvelopeSuite					*sAIEnvelope;
extern "C" AIExpandSuite					*sAIExpand;
extern "C" AIFileFormatSuite				*sAIFileFormat;
extern "C" AIFilterSuite					*sAIFilter;
extern "C" AIFontSuite						*sAIFont;
extern "C" AIGradientSuite					*sAIGradient;
extern "C" AIGroupSuite						*sAIGroup;
extern "C" AIHitTestSuite					*sAIHitTest;
extern "C" AIHardSoftSuite					*sAIHardSoft;
extern "C" AILayerSuite						*sAILayer;
extern "C" AILiveEffectSuite				*sAILiveEffect;
extern "C" AIMatchingArtSuite				*sAIMatchingArt;
extern "C" AIMeshSuite						*sAIMesh;
extern "C" AIMenuSuite						*sAIMenu;
extern "C" AINotifierSuite					*sAINotifier;
extern "C" AIPaintStyleSuite				*sAIPaintStyle;
extern "C" AIPathConstructionSuite			*sAIPathConstruction;
extern "C" AIPathfinderSuite				*sAIPathfinder;
extern "C" AIPathInterpolateSuite			*sAIPathInterpolate;
extern "C" AIPathStyleSuite					*sAIPathStyle;
extern "C" AIPathSuite						*sAIPath;
extern "C" AIPatternSuite					*sAIPattern;
extern "C" AIPlacedSuite					*sAIPlaced;
extern "C" AIPluginGroupSuite				*sAIPluginGroup;
extern "C" AIPreferenceSuite				*sAIPreference;
extern "C" AIRasterSuite					*sAIRaster;
extern "C" AIRasterizeSuite					*sAIRasterize;
extern "C" AIRealBezierSuite				*sAIRealBezier;
extern "C" AIRealMathSuite					*sAIRealMath;
extern "C" AIRuntimeSuite					*sAIRuntime;
extern "C" AIShapeConstructionSuite			*sAIShapeConstruction;
extern "C" AISwatchListSuite				*sAISwatchList;
extern "C" AISymbolSuite					*sAISymbol;
extern "C" AISymbolPaletteSuite				*sAISymbolPalette;
extern "C" AITabletDataSuite				*sAITabletData;
extern "C" AITagSuite						*sAITag;
extern "C" AITextFrameHitSuite				*sAITextFrameHit;
extern "C" AIToolSuite						*sAITool;
extern "C" AITimerSuite						*sAITimer;
extern "C" AITransformArtSuite				*sAITransformArt;
extern "C" AIUserSuite						*sAIUser;
extern "C" AIUndoSuite						*sAIUndo;
extern "C" AIURLSuite						*sAIURL;

#ifndef ADM_FREE

extern "C" _ADMBasicSuite					*sADMBasic;
extern "C" _ADMDialogSuite 					*sADMDialog;
extern "C" _ADMItemSuite					*sADMItem;
extern "C" _ADMIconSuite					*sADMIcon;
extern "C" _ADMImageSuite					*sADMImage;
extern "C" _ADMListSuite					*sADMList;
extern "C" _ADMHierarchyListSuite			*sADMHierarchyList;
extern "C" _ADMDialogGroupSuite				*sADMDialogGroup;
extern "C" _ADMNotifierSuite				*sADMNotifier;
extern "C" _ADMEntrySuite					*sADMEntry;
extern "C" _ADMListEntrySuite				*sADMListEntry;
extern "C" _ADMTrackerSuite					*sADMTracker;
extern "C" _ADMDrawerSuite					*sADMDrawer;
extern "C" _ADMHostSuite					*sADMHost;

#ifdef MAC_ENV
extern "C" ADMMacHostSuite					*sADMMacHost;
#endif // MAC_ENV
#ifdef WIN_ENV
extern "C" ADMWinHostSuite					*sADMWinHost;
#endif // WIN_ENV

#endif //#ifndef ADM_FREE

#if kPluginInterfaceVersion >= kAI11
extern "C" AITextFrameSuite					*sAITextFrame;
extern "C" AIATECurrentTextFeaturesSuite	*sAIATECurrentTextFeatures;
#else
extern "C" AITextSuite						*sAIText;
extern "C" AITextFaceStyleSuite				*sAITextFaceStyle;
extern "C" AITextLineSuite					*sAITextLine;
extern "C" AITextPathSuite					*sAITextPath;
extern "C" AITextRunSuite					*sAITextRun;
extern "C" AITextStreamSuite				*sAITextStream;
#endif // kPluginInterfaceVersion >= kAI11

#if kPluginInterfaceVersion >= kAI12
extern "C" AIFilePathSuite					*sAIFilePath;
extern "C" AIUnicodeStringSuite				*sAIUnicodeString;
#endif // kPluginInterfaceVersion >= kAI12

#if kPluginInterfaceVersion >= kAI12 && kPluginInterfaceVersion <= kAI15
extern "C" AITracingSuite					*sAITracing;
extern "C" AITracingIPSuite					*sAITracingIP;
#endif // kPluginInterfaceVersion >= kAI12 && kPluginInterfaceVersion <= kAI15

#if kPluginInterfaceVersion >= kAI13 && kPluginInterfaceVersion <= kAI15
extern "C" AICropAreaSuite					*sAICropArea;
#endif // kPluginInterfaceVersion >= kAI13 && kPluginInterfaceVersion <= kAI15

#if kPluginInterfaceVersion >= kAI15
extern "C" AIArtboardSuite					* sAIArtboard;
#endif // kPluginInterfaceVersion >= kAI15

#if kPluginInterfaceVersion >= kAI16
extern "C" AIPanelSuite				*sAIPanel;
extern "C" AIPanelFlyoutMenuSuite	*sAIPanelFlyoutMenu;
#endif // kPluginInterfaceVersion >= kAI16
