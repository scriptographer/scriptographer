/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile: mainDialog.cpp,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/03/05 21:33:03 $
 */
 
#include "stdHeaders.h"
#include "Plugin.h"
#include "Tool.h"
#include "resourceIds.h"
#include "admHandler.h"
#include "mainDialog.h"
#include "editorDialog.h"
#include "consoleDialog.h"
#include "ScriptographerEngine.h"
#ifdef MAC_ENV 
#include "macUtils.h"
#endif

// addFiles recursively walks through the folders in gPlugin->baseDir and adds the listEntries to
// the script list.
// this is highly platform dependant...

void ASAPI mainPictureHandler(ADMListEntryRef entry, ADMDrawerRef drawer) {
	SPPluginRef plugin = sADMItem->GetPluginRef(sADMListEntry->GetItem(entry));
	sADMListEntry->DefaultDraw(entry, drawer);
	// only draw leaves:
	if (sADMListEntry->GetChildList(entry) == NULL) {
		ADMRect rect;
		sADMListEntry->GetExpandArrowLocalRect(entry, &rect);
		int width = rect.right - rect.left - 1;
		rect.left += width;
		rect.right += width;
		int id = kScriptPictureID;
		Tool *tool = gPlugin->getTool(entry);
		if (tool != NULL)
			id = tool->getFolderPictureID();
		sADMDrawer->DrawResPictureCentered(drawer, plugin, id, &rect);
	}
}

void addFiles(ADMHierarchyListRef listRef, char *path) {
	if (strlen(path) == 0) return;
	bool more;
	char strPath[kMaxPathLength];
#ifdef WIN_ENV
	strcpy(strPath, path);
	strcat(strPath, "\\*.*"); 
	WIN32_FIND_DATA finddata;
	HANDLE handle = FindFirstFile(strPath, &finddata);
	more = (handle != (HANDLE) -1);
#elif MAC_ENV
	SPPlatformFileSpecification fileSpec;
	sAIUser->Path2SPPlatformFileSpecification(path, &fileSpec);
	CInfoPBRec info;
	memset(&info, 0, sizeof(CInfoPBRec));
	info.hFileInfo.ioDirID = fileSpec.parID;
	info.hFileInfo.ioVRefNum = fileSpec.vRefNum;
	info.hFileInfo.ioNamePtr = fileSpec.name;
	more = !PBGetCatInfo(&info, false);
	unsigned char fname[kMaxPathLength];
	if (more) {
		info.hFileInfo.ioFlParID = info.dirInfo.ioDrDirID;
		info.hFileInfo.ioFDirIndex = 0;
	}
#endif
	int i = 0;
	while (more) {
		bool isDir;
		char *name;
#ifdef WIN_ENV
		isDir = (finddata.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY);
		name = (char *) &finddata.cFileName;
#elif MAC_ENV
		info.hFileInfo.ioFDirIndex = i + 1;
		info.hFileInfo.ioNamePtr = fname;
		info.hFileInfo.ioDirID = info.hFileInfo.ioFlParID;
		more = !PBGetCatInfo(&info, false);
		if (!more) break;
		isDir = info.hFileInfo.ioFlAttrib & 0x10; // check for directory flag
		name = gPlugin->fromPascal(fname, (char*) fname); // abuse fname as the contianer for the converted cstring
#endif
		if (!isDir) {
			if (isJavaScript(name)) {
				ADMListEntryRef entryRef = sADMHierarchyList->InsertEntry(listRef, i);
				sADMListEntry->SetText(entryRef, name);
//				sADMListEntry->SetPictureID(entryRef, kScriptPictureID, NULL);
				char *strData = (char *)malloc(strlen(path) + strlen(name) + 2);
				strcpy(strData, path);
				strcat(strData, PATH_SEPARATOR_STR); 
				strcat(strData, name);
				sADMListEntry->SetUserData(entryRef, strData);
				// check if this script is marked as a tool:
				// if so, initialize it:
				Tool *tool = gPlugin->getTool(strData);
				if (tool != NULL) tool->setListEntry(entryRef);
			}
		} else if (strcmp(name, ".") != 0 && strcmp(name, "..") != 0) {
			ADMListEntryRef dirRef = sADMHierarchyList->InsertEntry(listRef, i);
			sADMListEntry->SetText(dirRef, name);
			sADMListEntry->SetPictureID(dirRef, kFolderPictureID, NULL);
			sADMListEntry->SetUserData(dirRef, NULL);
			ADMHierarchyListRef dirListRef = sADMListEntry->CreateChildList(dirRef);
			strcpy(strPath, path);
			strcat(strPath, PATH_SEPARATOR_STR); 
			strcat(strPath, name); 
			// it's a folder, so call addFiles again
			addFiles(dirListRef, strPath);
			sADMListEntry->ExpandHierarchy(dirRef, false);
		}
		i++;
#ifdef WIN_ENV
		more = FindNextFile(handle, &finddata);
#endif
	}
#ifdef WIN_ENV
	FindClose(handle);
#endif
}

void mainRemoveFiles(ADMHierarchyListRef listRef) {
	int count = sADMHierarchyList->NumberOfEntries(listRef);
	for (int i = count - 1; i >= 0; i--) {
		ADMListEntryRef entryRef = sADMHierarchyList->IndexEntry(listRef, i);
		void *data = sADMListEntry->GetUserData(entryRef);
		if (data != NULL) free(data);
		sADMHierarchyList->RemoveEntry(listRef, i);
	}
}

void mainRefreshFiles(ADMDialogRef dlg) {
	if (dlg == NULL) dlg = gPlugin->main.dlg;
	if (dlg != NULL) {
		ADMItemRef listItemRef = sADMDialog->GetItem(dlg, kMainScriptListItem);
		ADMHierarchyListRef listRef = sADMItem->GetHierarchyList(listItemRef);
		mainRemoveFiles(listRef);
		sADMHierarchyList->SetEntryWidth(listRef, 10000);
		sADMHierarchyList->SetEntryHeight(listRef, layerLineHeight);
		addFiles(listRef, gPlugin->baseDir);
	}
}

// Initialize dialog items and notification procedures for the ADM dialog
ASErr ASAPI mainDlgInit(ADMDialogRef dlg) {
	ASErr fxErr = kNoErr;
	
	// get the base dir
	char baseDir[300];
	strcpy(baseDir, "");
	AIErr err = sAIPreference->GetStringPreference( gPlugin->main.name, "baseDir", baseDir );
	strcpy(gPlugin->baseDir, baseDir);

	// ask the user when it's not set yet:
	while (strlen(gPlugin->baseDir) == 0) mainChooseBaseDir();

	/*******************************************************************************
 	 **	Dialog level stuff
	 **/
	 
	// Attach the dialog-level callbacks
	sADMDialog->SetDestroyProc(dlg, mainDlgDestroy );
	sADMDialog->SetNotifyProc(dlg, mainDlgNotify);

	// Do this only if you have a resizable dialog of type kADMTabbedResizingFloatingDialogStyle or kADMResizingFloatingDialogStyle
	// Set the increments and min/max sizes to limit grow behavior.
	sADMDialog->SetMinWidth( dlg, layerMinWidth );
	sADMDialog->SetMinHeight( dlg, layerMinHeight );
	sADMDialog->SetMaxWidth( dlg, layerMaxWidth );
	sADMDialog->SetVerticalIncrement( dlg, layerLineHeight );
	
	// Setup popup menu on dialog
	ADMItemRef menuItemRef = sADMDialog->GetItem(dlg, kADMMenuItemID);
	if (menuItemRef) {
		ADMListRef menuListRef = sADMItem->GetList(menuItemRef);
		if (menuListRef)
			sADMList->SetMenuID(menuListRef, gPlugin->getPluginRef(), kMainPulldownMenuID, NULL);
		sADMItem->SetNotifyProc(menuItemRef, mainPopupMenuNotify);
		sADMItem->SetTrackProc(menuItemRef, mainPopupMenuTrack);
	}

	// setup script list
	ADMItemRef scriptListItemRef = sADMDialog->GetItem(dlg, kMainScriptListItem);
	if (scriptListItemRef) {
		sADMItem->SetItemStyle(scriptListItemRef, kADMBlackRectListBoxStyle);
		ADMRect rect;
		sADMItem->GetBoundsRect(scriptListItemRef, &rect);
		rect.left--;
		rect.top--;
		sADMItem->SetBoundsRect(scriptListItemRef, &rect);
		ADMHierarchyListRef scriptListRef = sADMItem->GetHierarchyList(scriptListItemRef);
		if (scriptListRef) {
			sADMHierarchyList->SetNotifyProc(scriptListRef, mainScriptListNotify);
			sADMHierarchyList->SetTrackProc(scriptListRef, mainScriptListTrack);
			sADMHierarchyList->SetDrawProc(scriptListRef, mainPictureHandler);
		}
	}
	mainRefreshFiles(dlg);

	// resize handler

	ADMItemRef resizeItemRef = sADMDialog->GetItem(dlg, kADMResizeItemID);
	if (resizeItemRef) sADMItem->SetNotifyProc(resizeItemRef, mainResize);

	ADMItemRef *buttonItems = gPlugin->main.buttonItems;

	buttonItems[0] = initPictureButton(dlg, kMainPlayItem, mainPlayNotify, kPlayPictureID, false);
	buttonItems[1] = initPictureButton(dlg, kMainStopItem, mainStopNotify, kStopPictureID);
	buttonItems[2] = initPictureButton(dlg, kMainRefreshItem, mainRefreshNotify, kRefreshPictureID);
	buttonItems[3] = initPictureButton(dlg, kMainEditorItem, mainEditorNotify, kScriptPictureID);
	buttonItems[4] = initPictureButton(dlg, kMainConsoleItem, mainConsoleNotify, kConsolePictureID);
	buttonItems[5] = initPictureButton(dlg, kMainTool1Item, mainTool1Notify, kTool1IconID);
	buttonItems[6] = initPictureButton(dlg, kMainTool2Item, mainTool2Notify, kTool2IconID);
	dialogLoadPreference(dlg, gPlugin->main.name);

#ifdef WIN_ENV
	// create the font object:
	// default size and facename, but allow customization
	LOGFONT logFont; memset(&logFont, 0, sizeof(LOGFONT));
	logFont.lfHeight = 10;
	logFont.lfWeight = FW_NORMAL;
	logFont.lfCharSet = DEFAULT_CHARSET;
	strcpy(logFont.lfFaceName, "Courier");
	gPlugin->font = CreateFontIndirect(&logFont);
#endif

	return fxErr;
}

static void ASAPI mainDlgDestroy(ADMDialogRef dlg) {
	sAIPreference->PutBooleanPreference( gPlugin->main.name, "visible", mainVisible() );

	dialogSavePreference(dlg, gPlugin->main.name);
	
	gPlugin->main.dlg = NULL;
	// remove the font
#ifdef WIN_ENV
	DeleteObject(gPlugin->font);
#elif MAC_ENV
//	ADD MAC FONT SUPPORT HERE
#endif
}

static void ASAPI mainDlgNotify(ADMDialogRef dlg, ADMNotifierRef notifier) {
	sADMDialog->DefaultNotify(dlg, notifier);
}

void mainShow(bool show) {
	sADMDialog->Show(gPlugin->main.dlg, show);
}

bool mainVisible() {
	return sADMDialog->IsVisible(gPlugin->main.dlg);
}

static void ASAPI mainResize(ADMItemRef item, ADMNotifierRef notifier) {
	dialogResize(item, notifier, kMainScriptListItem, gPlugin->main.buttonItems, sizeof(gPlugin->main.buttonItems) / sizeof(ADMItemRef));
}

static ASBoolean ASAPI mainPopupMenuTrack(ADMItemRef item, ADMTrackerRef tracker) {
	if (sADMTracker->GetAction(tracker) == kADMButtonDownAction) { 	
		SPPluginRef pluginRef = sADMItem->GetPluginRef(item);
		ADMListRef menuListRef = sADMItem->GetList(item);
		ADMDialogRef dlg = sADMItem->GetDialog(item);
		if (menuListRef) enableButtonPopupItem(dlg, menuListRef, 1, kMainPlayItem);
	} 	

	return sADMItem->DefaultTrack(item, tracker);
}

static void ASAPI mainPopupMenuNotify( ADMItemRef item, ADMNotifierRef notifier) {
	if ( sADMNotifier->IsNotifierType( notifier, kADMUserChangedNotifier ) ) {
		int id = getPopupID(item);
		switch(id) {
			case 1: mainPlayNotify(item, notifier); break;
			case 3: mainStopNotify(item, notifier); break;
			case 4: mainRefreshNotify(item, notifier); break;
			case 6: mainEditorNotify(item, notifier); break;
			case 7: mainConsoleNotify(item, notifier); break;
			case 9: mainBaseDirNotify(item, notifier); break;
			case 11: gPlugin->about(NULL);
		}
	}
}

#ifdef MAC_ENV

// SEE http://developer.apple.com/technotes/tn/tn1002.html
bool openFile(char *filename) {
	FSSpec spec;
	carbonPathToFSSpec(filename, &spec);

	bool ok = true;
	AppleEvent theAEvent, theReply;
	AEAddressDesc fndrAddress;
	AEDescList targetListDesc;
	OSType fndrCreator;
	AliasHandle targetAlias;
	
		/* set up locals  */
	AECreateDesc(typeNull, NULL, 0, &theAEvent);
	AECreateDesc(typeNull, NULL, 0, &fndrAddress);
	AECreateDesc(typeNull, NULL, 0, &theReply);
	AECreateDesc(typeNull, NULL, 0, &targetListDesc);
	targetAlias = NULL;
	fndrCreator = 'MACS';
	
		/* create an open documents event targeting the finder */
	ok = AECreateDesc(typeApplSignature, (Ptr) &fndrCreator,
		sizeof(fndrCreator), &fndrAddress) == noErr;
	if (!ok) goto bail;
	ok = AECreateAppleEvent(kCoreEventClass, kAEOpenDocuments,
		&fndrAddress, kAutoGenerateReturnID,
		kAnyTransactionID, &theAEvent) == noErr;
	if (!ok) goto bail;
	
		/* create the list of files to open */
	ok = AECreateList(NULL, 0, false, &targetListDesc) == noErr;
	if (!ok) goto bail;
	ok = NewAlias(NULL, &spec, &targetAlias) == noErr;
	if (!ok) goto bail;
	HLock((Handle) targetAlias);
	ok = AEPutPtr(&targetListDesc, 1, typeAlias, *targetAlias, GetHandleSize((Handle) targetAlias)) == noErr;
	HUnlock((Handle) targetAlias);
	if (!ok) goto bail;
	
		/* add the file list to the apple event */
	ok = AEPutParamDesc(&theAEvent, keyDirectObject, &targetListDesc) == noErr;
	if (!ok) goto bail;

		/* send the event to the Finder */
	ok = AESend(&theAEvent, &theReply, kAENoReply,
		kAENormalPriority, kAEDefaultTimeout, NULL, NULL) == noErr;

		/* clean up and leave */
bail:
	if (targetAlias != NULL) DisposeHandle((Handle) targetAlias);
	AEDisposeDesc(&targetListDesc);
	AEDisposeDesc(&theAEvent);
	AEDisposeDesc(&fndrAddress);
	AEDisposeDesc(&theReply);
	return ok;
}

#endif

static ASBoolean ASAPI mainScriptListTrack( ADMListEntryRef entry, ADMTrackerRef tracker) {
	if (sADMTracker->GetAction(tracker) == kADMButtonUpAction && (sADMTracker->GetModifiers(tracker) & kADMDoubleClickModifier)) {
		if (sADMListEntry->GetUserData(entry) != NULL) {
			editorOpen(entry);
			/*
			char *filename;
			if (entry != NULL) filename = (char *)sADMListEntry->GetUserData(entry);
			else filename = NULL;
			if (filename != NULL)
				openFile(filename);
			*/
		}
	}

	ASBoolean doNotify = sADMListEntry->DefaultTrack(entry, tracker);
	return doNotify;
}

static void ASAPI mainScriptListNotify( ADMListEntryRef entry, ADMNotifierRef notifier) {
	if ( sADMNotifier->IsNotifierType( notifier, kADMUserChangedNotifier ) ) {
		ADMDialogRef dlg = sADMItem->GetDialog(sADMListEntry->GetItem(entry));
		ADMItemRef playItemRef = sADMDialog->GetItem(dlg, kMainPlayItem);
		sADMItem->Enable(playItemRef, sADMListEntry->GetUserData(entry) != NULL);
	}
}

static ADMListEntryRef mainGetCurrentListEntry() {
	ADMDialogRef dlg = gPlugin->main.dlg;
	ADMItemRef scriptListItemRef = sADMDialog->GetItem(dlg, kMainScriptListItem);
	if (scriptListItemRef != NULL) {
		ADMHierarchyListRef scriptListRef = sADMItem->GetHierarchyList(scriptListItemRef);
		if (scriptListRef != NULL) {
			return sADMHierarchyList->GetActiveLeafEntry(scriptListRef);
		}
	}
	return NULL;
}

static void ASAPI mainPlayNotify( ADMItemRef item, ADMNotifierRef notifier) {
	if ( sADMNotifier->IsNotifierType( notifier, kADMUserChangedNotifier ) ) {
		ADMListEntryRef entryRef = mainGetCurrentListEntry();
		if (entryRef != NULL) {
			char *filename = (char *)sADMListEntry->GetUserData(entryRef);
			if (filename != NULL) gEngine->evaluateFile(filename);
		}
	}
}

static void ASAPI mainStopNotify( ADMItemRef item, ADMNotifierRef notifier) {
	if ( sADMNotifier->IsNotifierType( notifier, kADMUserChangedNotifier ) ) {
	/* TODO: Stop Timers
		jsStopTimers();
	*/
	}
}

static void ASAPI mainRefreshNotify( ADMItemRef item, ADMNotifierRef notifier) {
	if ( sADMNotifier->IsNotifierType( notifier, kADMUserChangedNotifier ) ) {
		mainRefreshFiles();
		gPlugin->reloadEngine();
	}
}

static void ASAPI mainEditorNotify( ADMItemRef item, ADMNotifierRef notifier) {
	if ( sADMNotifier->IsNotifierType( notifier, kADMUserChangedNotifier ) ) {
		editorShow(!sADMDialog->IsVisible(gPlugin->editor.dlg));
	}
}

static void ASAPI mainConsoleNotify( ADMItemRef item, ADMNotifierRef notifier) {
	if ( sADMNotifier->IsNotifierType( notifier, kADMUserChangedNotifier ) ) {
		consoleShow(!sADMDialog->IsVisible(gPlugin->console.dlg));
	}
}

void mainChooseBaseDir(ADMDialogRef dlg) {
	SPPlatformFileSpecification startingDir, result;
	sAIUser->Path2SPPlatformFileSpecification(gPlugin->baseDir, &startingDir);
	char path[300];
	if (sADMBasic->StandardGetDirectoryDialog("Please choose the Scriptographer base directory:", &startingDir, &result)) {
		sAIUser->SPPlatformFileSpecification2Path(&result, path);
		// remove the path seperators at the end
		int pos = strlen(path);
		while(path[--pos] == PATH_SEPARATOR_CHR) path[pos] = '\0';
		strcpy(gPlugin->baseDir, path);
		mainRefreshFiles(dlg);
	}
}

static void ASAPI mainBaseDirNotify( ADMItemRef item, ADMNotifierRef notifier) {
	if ( sADMNotifier->IsNotifierType( notifier, kADMUserChangedNotifier ) ) {
		mainChooseBaseDir();
	}
}

static void mainSetCurrentToolScript(int index) {
	Tool *tool = gPlugin->getTool(index);
	// reset the old entry
	if (tool->fListEntry != NULL) {
		strcpy(tool->fFilename, "");
	}
	// and set the new one if it's a script:
	ADMListEntryRef entryRef = mainGetCurrentListEntry();
	if (entryRef != NULL) {
		char *filename = (char *)sADMListEntry->GetUserData(entryRef);
		if (filename != NULL) {
			ADMListEntryRef oldEntry = tool->fListEntry;
			tool->fListEntry = entryRef;
			sADMListEntry->Invalidate(tool->fListEntry);
			sADMListEntry->Invalidate(oldEntry);
			strcpy(tool->fFilename, filename);
			tool->initScript();
		}
	}
}

static void ASAPI mainTool1Notify( ADMItemRef item, ADMNotifierRef notifier) {
	if ( sADMNotifier->IsNotifierType( notifier, kADMUserChangedNotifier ) ) {
		mainSetCurrentToolScript(0);
	}
}

static void ASAPI mainTool2Notify( ADMItemRef item, ADMNotifierRef notifier) {
	if ( sADMNotifier->IsNotifierType( notifier, kADMUserChangedNotifier ) ) {
		mainSetCurrentToolScript(1);
	}
}