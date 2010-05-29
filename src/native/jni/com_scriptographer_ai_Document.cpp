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
 */

#include "stdHeaders.h"
#include "ScriptographerPlugin.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Document.h"
#include "com_scriptographer_ai_Item.h"

/*
 * com.scriptographer.ai.Document
 */

// gWorkingDoc allways points to the document ai is current working in. 
// whenever getArtHandle is called, it's document is retrieved too, and checked
// against this. Documents are switched if necessary. At the end of the code
// execution, the previous active document is restored (see endExecution)
AIDocumentHandle gWorkingDoc = NULL;
// gActiveDoc points to the document that the user has chosen to be working in.
// this can differ from gWorkingDoc, because if code works on more than one document
// at a time, documents are dynamically switched whenever needed, and gWorkingDoc
// keeps track of the one we are currently working in. gActiveDoc keeps track of the
// one activated by the user (at the start the same as gWorkingDoc, then depending
// on calls of Document.activate)
AIDocumentHandle gActiveDoc = NULL;
// gCreationDoc is only set when an object needs to be created in another document
// than the currently active one. It's set in Document.activate and erased after
// the first usage.
AIDocumentHandle gCreationDoc = NULL;

void Document_activate(AIDocumentHandle doc) {
	if (doc == NULL) {
		// If Document_activate(NULL) is called,
		// we switch to gCreationDoc if set, gActiveDoc otherwise
		// This should only be happening during the creation of new items.
		doc = gCreationDoc != NULL ? gCreationDoc : gActiveDoc;
	}
	if (gWorkingDoc != doc) {
		sAIDocumentList->Activate(doc, false);
		gWorkingDoc = doc;
	}
	// Erase it again...
	gCreationDoc = NULL;
}

/*
 * void nativeBeginExecution(int[] values)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_nativeBeginExecution(JNIEnv *env, jclass cls, jintArray values) {
	try {
		// Fetch the current working document, so it can
		// be set again if it was changed by document
		// handling code in the native environment in the end.
		// This is needed as any code that relies on the right document
		// to be set may switch any time (in fact, the native getArtHandle
		// function switches all the time).
		gWorkingDoc = NULL;
		sAIDocument->GetDocument(&gWorkingDoc);
		gActiveDoc = gWorkingDoc;
		gCreationDoc = NULL;
		long undoLevel = 0, redoLevel = 0;
		sAIUndo->CountTransactions(&undoLevel, &redoLevel);
		// Return values through int array, for performance
		jint data[] = {
			(jint) gWorkingDoc,
			undoLevel,
			redoLevel
		};
		env->SetIntArrayRegion(values, 0, 3, data);
	} EXCEPTION_CONVERT(env);
}

/*
 * void endExecution()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_endExecution(JNIEnv *env, jclass cls) {
	try {
		gEngine->resumeSuspendedDocuments();
		if (gActiveDoc != gWorkingDoc)
			sAIDocumentList->Activate(gActiveDoc, false);
	} EXCEPTION_CONVERT(env);
}

/*
 * int nativeGetActiveDocumentHandle()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Document_nativeGetActiveDocumentHandle(JNIEnv *env, jclass cls) {
	return (jint) gActiveDoc;
}

/*
 * int nativeGetWorkingDocumentHandle()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Document_nativeGetWorkingDocumentHandle(JNIEnv *env, jclass cls) {
	return (jint) gWorkingDoc;
}

/*
 * int nativeCreate(java.io.File file, int colorModel, int dialogStatus)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Document_nativeCreate__Ljava_io_File_2II(JNIEnv *env, jclass cls, jobject file, jint colorModel, jint dialogStatus) {
	AIDocumentHandle doc = NULL;
	try {
		SPPlatformFileSpecification fileSpec;
		if (gEngine->convertFile(env, file, &fileSpec) != NULL) {
#if kPluginInterfaceVersion < kAI12
			sAIDocumentList->Open(&fileSpec, (AIColorModel) colorModel, (ActionDialogStatus) dialogStatus, &doc);
#else
			ai::FilePath filePath(fileSpec);
	#if kPluginInterfaceVersion < kAI13
			sAIDocumentList->Open(filePath, (AIColorModel) colorModel, (ActionDialogStatus) dialogStatus, &doc);
	#else
			sAIDocumentList->Open(filePath, (AIColorModel) colorModel, (ActionDialogStatus) dialogStatus, true, &doc);
	#endif
#endif
		}
		if (doc != NULL)
			gWorkingDoc = doc;
	} EXCEPTION_CONVERT(env);
	return (jint) doc;
}

/*
 * int nativeCreate(java.lang.String title, float width, float height, int colorModel, int dialogStatus)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Document_nativeCreate__Ljava_lang_String_2FFII(JNIEnv *env, jclass cls, jstring title, jfloat width, jfloat height, jint colorModel, jint dialogStatus) {
	AIDocumentHandle doc = NULL;
	AIColorModel model = (AIColorModel) colorModel;
	char *str = NULL;
	try {
#if kPluginInterfaceVersion < kAI12
		str = gEngine->convertString(env, title);
		sAIDocumentList->New(str, &model, &width, &height, (ActionDialogStatus) dialogStatus, &doc);
#else
		ai::UnicodeString str = gEngine->convertString_UnicodeString(env, title);
#if kPluginInterfaceVersion < kAI13
		sAIDocumentList->New(str, &model, &width, &height, (ActionDialogStatus) dialogStatus, &doc);
#else // kPluginInterfaceVersion >= kAI13
		AINewDocumentPreset params;
		params.docTitle = str;
		params.docWidth = width;
		params.docHeight = height;
		params.docColorMode = model;
		sAIDocument->GetDocumentRulerUnits((short *) &params.docUnits);
		params.docPreviewMode = kAIPreviewModeDefault;
		// TODO: What to do with these two?
		params.docTransparencyGrid = kAITransparencyGridNone;
		params.docRasterResolution = kAIRasterResolutionScreen;
		ai::UnicodeString preset("");
#if kPluginInterfaceVersion >= kAI14
		// TODO: Add additional version that allow configuration of art boards for CS4?
		params.docNumArtboards = 1;
		params.docArtboardLayout = kAIArtboardLayoutGridByRow;
		params.docArtboardSpacing = 0;
		params.docArtboardRowsOrCols = 1;
#endif // kPluginInterfaceVersion >= kAI14
		sAIDocumentList->New(preset, &params, (ActionDialogStatus) dialogStatus, &doc);
#endif // kPluginInterfaceVersion >= kAI13 
		if (doc != NULL)
			gWorkingDoc = doc;
#endif
	} EXCEPTION_CONVERT(env);
	if (str != NULL)
		delete str;
	return (jint) doc;
}

/*
 * void nativeActivate(boolean focus, boolean forCreation)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_nativeActivate(JNIEnv *env, jobject obj, jboolean focus, jboolean forCreation) {
	try {
		// Do not switch yet as we may want to focus the document too:
		AIDocumentHandle doc = gEngine->getDocumentHandle(env, obj);
		if (doc != gWorkingDoc) {
			sAIDocumentList->Activate(doc, focus);
			gWorkingDoc = doc;
			// If forCreation is set, set gCreationDoc instead of gActiveDoc
			if (forCreation) gCreationDoc = doc;
			else gActiveDoc = doc;
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.Layer getActiveLayer()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_getActiveLayer(JNIEnv *env, jobject obj) {
	jobject layerObj = NULL;
	try {
		// Cause the doc switch if necessary
		AIDocumentHandle doc = gEngine->getDocumentHandle(env, obj, true);
		
		AILayerHandle layer = NULL;
		sAILayer->GetCurrentLayer(&layer); 
		if (layer != NULL)
			layerObj = gEngine->wrapLayerHandle(env, layer, doc);
	} EXCEPTION_CONVERT(env);
	return layerObj;
}

/*
 * int getActiveViewHandle()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Document_getActiveViewHandle(JNIEnv *env, jobject obj) {
	AIDocumentViewHandle view = NULL;
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		
		// The active view is at index 0:
		sAIDocumentView->GetNthDocumentView(0, &view);
	} EXCEPTION_CONVERT(env);
	return (jint) view;
}

/*
 * int getActiveSymbolHandle()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Document_getActiveSymbolHandle(JNIEnv *env, jobject obj) {
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		AIPatternHandle symbol = NULL;
		sAISymbolPalette->GetCurrentSymbol(&symbol);
		return (jint) symbol;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int getActiveArtboardIndex()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Document_getActiveArtboardIndex(JNIEnv *env, jobject obj) {
#if kPluginInterfaceVersion >= kAI13
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		ASInt32 index;
		if (!sAICropArea->GetActive(&index))
			return index;
	} EXCEPTION_CONVERT(env);
	return -1;
#else // kPluginInterfaceVersion < kAI13
	return 0;
#endif // kPluginInterfaceVersion < kAI13 
}

/*
 * void setActiveArtboardIndex(int index)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_setActiveArtboardIndex(JNIEnv *env, jobject obj, jint index) {
#if kPluginInterfaceVersion >= kAI13
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		sAICropArea->SetActive(index);
	} EXCEPTION_CONVERT(env);
#endif // kPluginInterfaceVersion < kAI13 
}

/*
 * com.scriptographer.ai.Point getPageOrigin()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_getPageOrigin(JNIEnv *env, jobject obj) {
	jobject origin = NULL;
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		
		AIRealPoint pt;
		sAIDocument->GetDocumentPageOrigin(&pt);
		origin = gEngine->convertPoint(env, &pt);
	} EXCEPTION_CONVERT(env);
	return origin;
}

/*
 * void setPageOrigin(com.scriptographer.ai.Point origin)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_setPageOrigin(JNIEnv *env, jobject obj, jobject origin) {
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		
		AIRealPoint pt;
		gEngine->convertPoint(env, origin, &pt);
		sAIDocument->SetDocumentPageOrigin(&pt);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.Point getRulerOrigin()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_getRulerOrigin(JNIEnv *env, jobject obj) {
	jobject origin = NULL;
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		
		AIRealPoint pt;
		sAIDocument->GetDocumentRulerOrigin(&pt);
		origin = gEngine->convertPoint(env, &pt);
	} EXCEPTION_CONVERT(env);
	return origin;
}

/*
 * void setRulerOrigin(com.scriptographer.ai.Point origin)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_setRulerOrigin(JNIEnv *env, jobject obj, jobject origin) {
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		
		AIRealPoint pt;
		gEngine->convertPoint(env, origin, &pt);
		sAIDocument->SetDocumentRulerOrigin(&pt);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.Size getSize()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_getSize(JNIEnv *env, jobject obj) {
	jobject size = NULL;
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		
		AIDocumentSetup setup;
		sAIDocument->GetDocumentSetup(&setup);
		DEFINE_POINT(pt, setup.width, setup.height);
		size = gEngine->convertSize(env, &pt);
	} EXCEPTION_CONVERT(env);
	return size;
}

/*
 * void setSize(double width, double height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_setSize(JNIEnv *env, jobject obj, jdouble width, jdouble height) {
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		
		AIDocumentSetup setup;
		sAIDocument->GetDocumentSetup(&setup);
		setup.width = width;
		setup.height = height;
		sAIDocument->SetDocumentSetup(&setup);
	} EXCEPTION_CONVERT(env);
}

/*
 * int nativeGetColormodel()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Document_nativeGetColormodel(JNIEnv *env, jobject obj) {
	short model = kDocUnknownColor;
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		sAIDocument->GetDocumentColorModel(&model);
	} EXCEPTION_CONVERT(env);
	return model;
}

/*
 * void nativeSetColormodel(int model)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_nativeSetColormodel(JNIEnv *env, jobject obj, jint model) {
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		sAIDocument->SetDocumentColorModel((short) model);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isModified()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Document_isModified(JNIEnv *env, jobject obj) {
	ASBoolean modified = false;
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		
		sAIDocument->GetDocumentModified(&modified);
	} EXCEPTION_CONVERT(env);
	return modified;
}

/*
 * void setModified(boolean modified)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_setModified(JNIEnv *env, jobject obj, jboolean modified) {
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		
		sAIDocument->SetDocumentModified(modified);
	} EXCEPTION_CONVERT(env);
}

/*
 * java.io.File getFile()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_getFile(JNIEnv *env, jobject obj) {
	jobject file = NULL;
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		
		SPPlatformFileSpecification fileSpec;
#if kPluginInterfaceVersion < kAI12
		if (!sAIDocument->GetDocumentFileSpecification(&fileSpec)) {
#else
		ai::FilePath filePath;
		if (!sAIDocument->GetDocumentFileSpecification(filePath)) {
			filePath.GetAsSPPlatformFileSpec(fileSpec);
#endif
			file = gEngine->convertFile(env, &fileSpec);
		}
	} EXCEPTION_CONVERT(env);
	return file;
}

/*
 * void nativePrint(int status)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_nativePrint(JNIEnv *env, jobject obj, jint status) {
	try {
		AIDocumentHandle doc = gEngine->getDocumentHandle(env, obj);
		sAIDocumentList->Print(doc, (ActionDialogStatus) status);
	} EXCEPTION_CONVERT(env);
}

/*
 * void save()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_save(JNIEnv *env, jobject obj) {
	try {
		AIDocumentHandle doc = gEngine->getDocumentHandle(env, obj);
		sAIDocumentList->Save(doc);
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean nativeWrite(java.io.File file, int formatHandle, boolean ask)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Document_nativeWrite(JNIEnv *env, jobject obj, jobject file, jint formatHandle, jboolean ask) {
	jboolean ret = false;
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		gEngine->commit(env);
		
		char *formatName = NULL;

		long formatOptions;

		if (formatHandle != 0) {
			sAIFileFormat->GetFileFormatName((AIFileFormatHandle) formatHandle, &formatName);
			sAIFileFormat->GetFileFormatOptions((AIFileFormatHandle) formatHandle, &formatOptions);
		}

		if (formatName == NULL) {
			formatName = "Adobe Illustrator Any Format Writer";
			formatOptions = kFileFormatWrite;
		}

		if (ask)
			formatOptions &= ~kFileFormatSuppressUI;
		else
			formatOptions |= kFileFormatSuppressUI;

		SPPlatformFileSpecification fileSpec;
		if (gEngine->convertFile(env, file, &fileSpec) != NULL) {
#if kPluginInterfaceVersion < kAI12
			ret = !sAIDocument->WriteDocumentWithOptions(&fileSpec, formatName, formatOptions, ask);
#else
			ai::FilePath filePath(fileSpec);
			ret = !sAIDocument->WriteDocumentWithOptions(filePath, formatName, formatOptions, ask);
#endif
		}
	} EXCEPTION_CONVERT(env);
	
	return ret;
}

/*
 * void close()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_close(JNIEnv *env, jobject obj) {
	try {
		AIDocumentHandle doc = gEngine->getDocumentHandle(env, obj);
		sAIDocumentList->Close(doc);
	} EXCEPTION_CONVERT(env);
}

/*
 * void redraw()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_redraw(JNIEnv *env, jobject obj) {
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		// TODO: Add a commmit command that only commits all items of the document
		gEngine->commit(env);
		sAIDocument->RedrawDocument();
	} EXCEPTION_CONVERT(env);
}

/*
 * void undo()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_undo(JNIEnv *env, jobject obj) {
	try {
#if kPluginInterfaceVersion >= kAI13
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		gEngine->commit(env);
		sAIDocument->Undo();
#endif // kPluginInterfaceVersion >= kAI13
	} EXCEPTION_CONVERT(env);
}

/*
 * void redo()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_redo(JNIEnv *env, jobject obj) {
	try {
#if kPluginInterfaceVersion >= kAI13
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		gEngine->commit(env);
		sAIDocument->Redo();
#endif // kPluginInterfaceVersion >= kAI13
	} EXCEPTION_CONVERT(env);
}

/*
 * void invalidate(float x, float y, float width, float height)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_invalidate(JNIEnv *env, jobject obj, jfloat x, jfloat y, jfloat width, jfloat height) {
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		
		// use the DocumentView's function SetDocumentViewInvalidDocumentRect that fits
		// much better here.
		// Acording to DocumentView.h, we don't need to pass a view handle, as this
		// document is now the current one anyhow and its view is on top of the others:	
		DEFINE_RECT(rect, x, y, width, height);
		sAIDocumentView->SetDocumentViewInvalidDocumentRect(NULL, &rect);
	} EXCEPTION_CONVERT(env);
}

/*
 * void copy()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_copy(JNIEnv *env, jobject obj) {
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		gEngine->commit(env);
		sAIDocument->Copy();
	} EXCEPTION_CONVERT(env);
}

/*
 * void cut()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_cut(JNIEnv *env, jobject obj) {
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		gEngine->commit(env);
		sAIDocument->Cut();
	} EXCEPTION_CONVERT(env);
}

/*
 * void paste()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_paste(JNIEnv *env, jobject obj) {
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		gEngine->commit(env);
		sAIDocument->Paste();
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.Item place(java.io.File file, boolean linked)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_place(JNIEnv *env, jobject obj, jobject file, jboolean linked) {
	try {
		AIDocumentHandle doc = gEngine->getDocumentHandle(env, obj, true);
		AIArtHandle art = PlacedFile_place(env, doc, file, linked);
		return gEngine->wrapArtHandle(env, art, doc, true);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

// ItemList stuff:

/*
 * boolean hasSelectedItems()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Document_hasSelectedItems(JNIEnv *env, jobject obj) {
	jboolean selected = false;
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		selected = sAIMatchingArt->IsSomeArtSelected();
	} EXCEPTION_CONVERT(env);
	return selected;
}

/*
 * com.scriptographer.ai.ItemList getSelectedItems()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_getSelectedItems(JNIEnv *env, jobject obj) {
	jobject itemSet = NULL;
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		AIArtSet selected = Item_getSelected(true);
		if (selected != NULL) {
			itemSet = gEngine->convertItemSet(env, selected);
			sAIArtSet->DisposeArtSet(&selected);
		}
	} EXCEPTION_CONVERT(env);
	return itemSet;
}

/*
 * void nativeSelectAll()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_nativeSelectAll(JNIEnv *env, jobject obj) {
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		// Get all unselected items
		AIMatchingArtSpec spec;
		spec.type = kAnyArt;
		spec.whichAttr = kArtFullySelected;
		spec.attr = 0;
		AIArtHandle **matches;
		long numMatches;
		if (!sAIMatchingArt->GetMatchingArt(&spec, 1, &matches, &numMatches)) {
			for (long i = 0; i < numMatches; i++)
				sAIArt->SetArtUserAttr((*matches)[i], kArtSelected, kArtSelected);
			sAIMDMemory->MdMemoryDisposeHandle((void **) matches);
		}
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.TextRange getSelectedTextRange()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_getSelectedTextRange(JNIEnv *env, jobject obj) {
	try {
		using namespace ATE;
		AIBoolean value;
		TextRangesRef rangesRef;
		if (!sAIDocument->HasTextFocus(&value) && value
				&& !sAIDocument->HasTextCaret(&value) && !value
				&& !sAIDocument->GetTextSelection(&rangesRef) && rangesRef != NULL) {
			ITextRanges ranges(rangesRef);
			if (ranges.GetSize() > 0) {
				ITextRange first = ranges.GetFirst();
				TextRangeRef rangeRef = first.GetRef();
				// Add one ref since ITextRange will release at the end.
				sTextRange->AddRef(rangeRef);
				return gEngine->wrapTextRangeRef(env, rangeRef);
			}
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void nativeDeselectAll()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_nativeDeselectAll(JNIEnv *env, jobject obj) {
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		Item_deselectAll();
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.ItemList getMatchingItems(java.lang.Class typeClass, java.util.Map attributes)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_getMatchingItems(JNIEnv *env, jobject obj, jclass typeClass, jobject attributes) {
	jobject itemSet = NULL;
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		gEngine->commit(env);
		AIArtSet set;
		if (!sAIArtSet->NewArtSet(&set)) {
			bool layerOnly = false;
			short type = Item_getType(env, typeClass);
			if (type == com_scriptographer_ai_Item_TYPE_LAYER) {
				type = kGroupArt;
				layerOnly = true;
			}
			AIArtSpec spec;
			spec.type = type;
			spec.whichAttr = 0;
			spec.attr = 0;
			// use the env's version of the callers for speed reasons. check for exceptions only once for every entry:
			jobject keySet = env->CallObjectMethod(attributes, gEngine->mid_Map_keySet);
			jobject iterator = env->CallObjectMethod(keySet, gEngine->mid_Set_iterator);
			while (env->CallBooleanMethod(iterator, gEngine->mid_Iterator_hasNext)) {
				jobject key = env->CallObjectMethod(iterator, gEngine->mid_Iterator_next);
				jobject value = env->CallObjectMethod(attributes, gEngine->mid_Map_get, key);
				jint flag = env->CallIntMethod(key, gEngine->mid_Number_intValue);
				jint set = env->CallBooleanMethod(value, gEngine->mid_Boolean_booleanValue);
				spec.whichAttr |= flag;
				if (set)
					spec.attr |= flag;
				EXCEPTION_CHECK(env);
			}
			if (!sAIArtSet->MatchingArtSet(&spec, 1, set)) {
				itemSet = gEngine->convertItemSet(env, set, layerOnly);
				sAIArtSet->DisposeArtSet(&set);
			}
		}
	} EXCEPTION_CONVERT(env);
	return itemSet;
}

/*
 * com.scriptographer.ai.Path nativeCreateRectangle(com.scriptographer.ai.Rect rect)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_nativeCreateRectangle(JNIEnv *env, jobject obj, jobject rect) {
	try {
		// Activate document
		AIDocumentHandle doc = gEngine->getDocumentHandle(env, obj, true);
		short paintOrder;
		// simply call for the error message and the doc activation
		Item_getInsertionPoint(&paintOrder);
		AIRealRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		AIArtHandle art = NULL;
		sAIShapeConstruction->NewRect(rt.top, rt.left, rt.bottom, rt.right, false, &art);
		return gEngine->wrapArtHandle(env, art, doc, true);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Path nativeCreateRoundRectangle(com.scriptographer.ai.Rectangle rect, com.scriptographer.ai.Size size)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_nativeCreateRoundRectangle(JNIEnv *env, jobject obj, jobject rect, jobject size) {
	try {
		// Activate document
		AIDocumentHandle doc = gEngine->getDocumentHandle(env, obj, true);
		short paintOrder;
		// simply call for the error message and the doc activation
		Item_getInsertionPoint(&paintOrder);
		AIRealRect rt;
		AIRealPoint pt;
		gEngine->convertRectangle(env, rect, &rt);
		gEngine->convertSize(env, size, &pt);
		AIArtHandle art = NULL;
		sAIShapeConstruction->NewRoundedRect(rt.top, rt.left, rt.bottom, rt.right, pt.h, pt.v, false, &art);
		return gEngine->wrapArtHandle(env, art, doc, true);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Path nativeCreateOval(com.scriptographer.ai.Rectangle rect, boolean circumscribed)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_nativeCreateOval(JNIEnv *env, jobject obj, jobject rect, jboolean circumscribed) {
	try {
		// Activate document
		AIDocumentHandle doc = gEngine->getDocumentHandle(env, obj, true);
		short paintOrder;
		// simply call for the error message and the doc activation
		Item_getInsertionPoint(&paintOrder);
		AIRealRect rt;
		gEngine->convertRectangle(env, rect, &rt);
		AIArtHandle art = NULL;
		if (circumscribed)
			sAIShapeConstruction->NewCircumscribedOval(rt.top, rt.left, rt.bottom, rt.right, false, &art);
		else
			sAIShapeConstruction->NewInscribedOval(rt.top, rt.left, rt.bottom, rt.right, false, &art);
		return gEngine->wrapArtHandle(env, art, doc, true);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Path nativeCreateRegularPolygon(com.scriptographer.ai.Point center, int numSides, float radius)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_nativeCreateRegularPolygon(JNIEnv *env, jobject obj, jobject center, jint numSides, jfloat radius) {
	try {
		// Activate document
		AIDocumentHandle doc = gEngine->getDocumentHandle(env, obj, true);
		short paintOrder;
		// simply call for the error message and the doc activation
		Item_getInsertionPoint(&paintOrder);
		AIRealPoint pt;
		gEngine->convertPoint(env, center, &pt);
		AIArtHandle art = NULL;
		sAIShapeConstruction->NewRegularPolygon(numSides, pt.h, pt.v, radius, false, &art);
		return gEngine->wrapArtHandle(env, art, doc, true);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Path nativeCreateStar(com.scriptographer.ai.Point center, int numPoints, float radius1, float radius2)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_nativeCreateStar(JNIEnv *env, jobject obj, jobject center, jint numPoints, jfloat radius1, jfloat radius2) {
	try {
		// Activate document
		AIDocumentHandle doc = gEngine->getDocumentHandle(env, obj, true);
		short paintOrder;
		// simply call for the error message and the doc activation
		Item_getInsertionPoint(&paintOrder);
		AIRealPoint pt;
		gEngine->convertPoint(env, center, &pt);
		AIArtHandle art = NULL;
		sAIShapeConstruction->NewStar(numPoints, pt.h, pt.v, radius1, radius2, false, &art);
		return gEngine->wrapArtHandle(env, art, doc, true);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.Path nativeCreateSpiral(com.scriptographer.ai.Point firstArcCenter, com.scriptographer.ai.Point start, float decayPercent, int numQuarterTurns, boolean clockwiseFromOutside)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_nativeCreateSpiral(JNIEnv *env, jobject obj, jobject firstArcCenter, jobject start, jfloat decayPercent, jint numQuarterTurns, jboolean clockwiseFromOutside) {
	try {
		// Activate document
		AIDocumentHandle doc = gEngine->getDocumentHandle(env, obj, true);
		short paintOrder;
		// simply call for the error message and the doc activation
		Item_getInsertionPoint(&paintOrder);
		AIRealPoint ptCenter, ptStart;
		gEngine->convertPoint(env, firstArcCenter, &ptCenter);
		gEngine->convertPoint(env, start, &ptStart);
		AIArtHandle art = NULL;
		sAIShapeConstruction->NewSpiral(ptCenter, ptStart, decayPercent, numQuarterTurns, clockwiseFromOutside, &art);
		return gEngine->wrapArtHandle(env, art, doc, true);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * com.scriptographer.ai.HitResult nativeHitTest(com.scriptographer.ai.Point point, int type, float tolerance, com.scriptographer.ai.Item art)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Document_nativeHitTest(JNIEnv *env, jobject obj, jobject point, jint type, jfloat tolerance, jobject item) {
	jobject hitTest = NULL;
	try {
		// Cause the doc switch if necessary
		AIDocumentHandle doc = gEngine->getDocumentHandle(env, obj, true);

		AIRealPoint pt;
		gEngine->convertPoint(env, point, &pt);
		
		AIArtHandle handle = gEngine->getArtHandle(env, item);
		if (handle != NULL) {
			Item_commit(env, handle);
		} else {
			gEngine->commit(env);
		}
		
		AIHitRef hit;
		AIHitRequest request = (AIHitRequest) type;
		// bugfix for illustrator: does not seem to support kNearestPointOnPathHitRequest
		if (type == kNearestPointOnPathHitRequest)
			request = kAllNoFillHitRequest;
		if (!sAIHitTest->HitTestEx(handle, &pt, tolerance, request, &hit)) {
			AIToolHitData toolHit;
			if (sAIHitTest->IsHit(hit) && !sAIHitTest->GetHitData(hit, &toolHit)) {
				int hitType = toolHit.type;
				TextRangeRef textRange = NULL;
				// Support for hittest on text frames:
				if (Item_getType(toolHit.object) == kTextFrameArt) {
					int textPart = sAITextFrameHit->GetPart(hit);
					// fake HIT values for Text, added from AITextPart + 10
					if (textPart != kAITextNowhere)
						hitType = textPart + 10;
					if (hitType >= 0)
						sAITextFrame->DoTextFrameHit(hit, &textRange);
				} else if (type == kNearestPointOnPathHitRequest) {
					// filter out to simulate kNearestPointOnPathHitRequest that does not work properly
					if (hitType > kSegmentHitType)
						hitType = -1;
				} else if (hitType == kFillHitType) {
					// bugfix for illustrator: returns kFillHitType instead of kCenterHitType when hitting center!
					AIBoolean visible = false;
					sAIArt->GetArtCenterPointVisible(toolHit.object, &visible);
					if (visible) {
						// find zoom factor
						AIDocumentViewHandle view = NULL;
						// the active view is at index 0:
						sAIDocumentView->GetNthDocumentView(0, &view);
						AIReal zoom = 1.0f;
						sAIDocumentView->GetDocumentViewZoom(view, &zoom);
						// messure distance from center
						AIRealRect bounds;
						sAIArt->GetArtBounds(toolHit.object, &bounds);
						DEFINE_POINT(center, (bounds.left + bounds.right) / 2.0, (bounds.top + bounds.bottom) / 2.0);
						if (sAIRealMath->AIRealPointClose(&center, &pt, tolerance / zoom))
							hitType = kCenterHitType;
					}
				}
				if (hitType >= 0) {
					jobject item = gEngine->wrapArtHandle(env, toolHit.object, doc);
					jobject point = gEngine->convertPoint(env, &toolHit.point);
					hitTest = gEngine->newObject(env, gEngine->cls_ai_HitResult, gEngine->cid_ai_HitResult, (jint) doc,
							hitType, item, (jint) toolHit.segment, (jdouble) toolHit.t, point, (jint) textRange);
				}
			}
			sAIHitTest->Release(hit);
		}
	} EXCEPTION_CONVERT(env);
	return hitTest;
}

/*
 * int nativeGetStories(int storyHandle, boolean release)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Document_nativeGetStories(JNIEnv *env, jobject obj, jint storyHandle, jboolean release) {
	using namespace ATE;
	jint ret = 0;
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		StoriesRef stories;
		if (!sStory->GetStories((StoryRef) storyHandle, &stories)) {
			ret = (jint) stories;
		}
		if (release)
			sStory->Release((StoryRef) storyHandle);
	} EXCEPTION_CONVERT(env);
	return ret;
}

/*
 * void reflowText()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_reflowText(JNIEnv *env, jobject obj) {
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		sAIDocument->ResumeTextReflow();
		sAIUser->AppIdle();
		sAIDocument->SuspendTextReflow();
	} EXCEPTION_CONVERT(env);
}

/*
 * int nativeGetFileFormat()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Document_nativeGetFileFormat(JNIEnv *env, jobject obj) {
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		AIFileFormatHandle handle;
		if (!sAIDocument->GetDocumentFileFormat(&handle))
			return (jint) handle;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void nativeSetFileFormat(int handle)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Document_nativeSetFileFormat(JNIEnv *env, jobject obj, jint handle) {
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		if (sAIDocument->SetDocumentFileFormat((AIFileFormatHandle) handle))
			throw new StringException("Cannot set file format on document");
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean nativeIsValid()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Document_nativeIsValid(JNIEnv *env, jobject obj) {
	try {
		AIDocumentHandle doc = gEngine->getDocumentHandle(env, obj);
		AIBoolean exists = false;
		sAIDocument->DocumentExists(doc, &exists);
		return exists;
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * int nativeGetData()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Document_nativeGetData(JNIEnv *env, jobject obj) {
	AIDictionaryRef dictionary = NULL;
	try {
		// Cause the doc switch if necessary
		gEngine->getDocumentHandle(env, obj, true);
		sAIDocument->GetDictionary(&dictionary);
	} EXCEPTION_CONVERT(env);
	return (jint) dictionary;
}
