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
 * $RCSfile: Plugin.cpp,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/03/07 13:40:32 $
 */
 
#include "stdHeaders.h"

#include "Plugin.h"
#include "ScriptographerEngine.h"

#include "resourceIds.h"
#include "AppContext.h"

Plugin *gPlugin = NULL;

Plugin::Plugin(SPPluginRef pluginRef) {
	fPluginRef = pluginRef;
	fPluginName = "Scriptographer";
	fLockCount = 0;
	fPluginAccess = NULL;
	fLastError = kNoErr;
	fSupressDuplicateErrors = true;
	fErrorTimeout = 5;		// seconds
	fLastErrorTime = 0;
	fAppStartedNotifier = NULL;
	fEngine = NULL;	
	fLoaded = false;
}

Plugin::~Plugin() {
}

void Plugin::reloadEngine() {
	char *errors = fEngine->reloadEngine();
	if (errors != NULL) {
		reportError(errors);
		delete errors;
	}
}

// Plugin:

void Plugin::setGlobal(ASBoolean set) {
	if (set) {
		gPlugin = this;
		gEngine = fEngine;
	} else {
		gPlugin = NULL;
		gEngine = NULL;
	}
}

ASErr Plugin::createTool(char *title, int iconID, int cursorID, long options, char *sameGroupTool, char *sameToolsetTool) {
	AIAddToolData data;
	
	data.title = title;
	data.tooltip = title;
	
	data.icon = sADMIcon->GetFromResource(fPluginRef, NULL, iconID, 0);
	if (data.icon == NULL || sADMIcon->GetType(data.icon) == kUnknown) throw new ASErrException('!ico');

	ASErr error;
	
	if (sameGroupTool != NULL) {
		error = sAITool->GetToolNumberFromName(sameGroupTool, &data.sameGroupAs);
		if (error) return error;
	} else {
		data.sameGroupAs = kNoTool;
	}
	
	if (sameToolsetTool != NULL) {
		error = sAITool->GetToolNumberFromName(sameToolsetTool, &data.sameToolsetAs);
		if (error) return error;
	} else {
		data.sameToolsetAs = kNoTool;
	}

	AIToolHandle handle;
	error = sAITool->AddTool(fPluginRef, title, &data, options, &handle);
	if (error) return error;

	error = sAITool->SetToolOptions(handle, options);
	if (error) return error;
	
	return kNoErr;
}


ASErr Plugin::startupPlugin(SPInterfaceMessage *message) {
	setGlobal(false);
	
	// aquire only the basic suites that are needed here. the rest is acquired in postStartup.
	ASErr error = acquireSuites(&gBasicSuites);
	if (error) return error;
	
	error = sSPPlugins->SetPluginName(fPluginRef, fPluginName);
	if (error) return error;
	
	char notifierName[kMaxStringLength];

	sprintf(notifierName, "%s App Started Notifier", fPluginName);
	error = sAINotifier->AddNotifier(fPluginRef, notifierName, kAIApplicationStartedNotifier, &fAppStartedNotifier);
	if (error) return error;

	// set gPlugin:
	setGlobal(true);

	// try to create the Java Engine:
	// determine baseDirectory from plugin location:
	char homeDir[kMaxPathLength];
	SPPlatformFileSpecification fileSpec;
	sSPPlugins->GetPluginFileSpecification(fPluginRef, &fileSpec);
    try {
		if (!fileSpecToPath(&fileSpec, homeDir))
			throw;
		// now find the last occurence of PATH_SEP_CHR and write the java path over it:
		strcpy(strrchr(homeDir, PATH_SEP_CHR) + 1, "java");
		// homeDir now contains the full path to the java stuff
		fEngine = new ScriptographerEngine(homeDir);
	} catch(Exception *e) {
		e->report(NULL);
		delete e;
		return kCantHappenErr;
	} catch(...) {
		return kCantHappenErr;
	}
		
	// for gEngine:
	setGlobal(true);

	// add the menu items
	AIPlatformAddMenuItemData menuData;
	unsigned char pstr[32]; // a buffer for the pstring conversions:
	menuData.groupName = kOtherPalettesMenuGroup;
	menuData.itemText = toPascal("Reload Scriptographer", pstr);
	error = sAIMenu->AddMenuItem(fPluginRef, fPluginName, &menuData, 0, &fReload);
	if (error) return error;
	
	// add the two script tools:
	char *title0 = "Scriptographer Tool 1";
	char *title1 = "Scriptographer Tool 2";
	error = createTool(title0, kTool1IconID, kTool1CursorID, kToolWantsToTrackCursorOption | kToolWantsBufferedDraggingOption);
	if (error) return error;

	error = createTool(title1, kTool2IconID, kTool2CursorID, kToolWantsToTrackCursorOption | kToolWantsBufferedDraggingOption, title0);
	if (error) return error;

	fLoaded = true;

	return error;
}

ASErr Plugin::postStartupPlugin() {
	if (fEngine == NULL)
		return kCantHappenErr;
	
	// now accuire the rest of the suites:
	ASErr error = acquireSuites(&gAdditionalSuites);
	if (error) return error;
	
	fEngine->initEngine();

	return error;
}

ASErr Plugin::shutdownPlugin(SPInterfaceMessage *message) {
	delete fEngine;
	unloadPlugin(NULL);
	return kNoErr;
}

ASErr Plugin::goMenuItem(AIMenuMessage *message) {
	// TODO: as soon as the native dialogs are gone, call gEngine directly from handleMessage
	if (message->menuItem == fReload) gEngine->reloadEngine();
	else return gEngine->menuItemExecute(message);
	return kNoErr;
}

ASErr Plugin::updateMenuItem(AIMenuMessage *message) {
	// TODO: as soon as the native dialogs are gone, call gEngine directly from handleMessage
	if (message->menuItem == fReload) {
	} else {
		long inArtwork, isSelected, isTrue;
		sAIMenu->GetUpdateFlags(&inArtwork, &isSelected, &isTrue);
		return gEngine->menuItemUpdate(message, inArtwork, isSelected, isTrue);
	}
	return kNoErr;
}

ASErr Plugin::unloadPlugin(SPInterfaceMessage *message) {
	// TODO: error handling:
	releaseSuites(&gBasicSuites);
	releaseSuites(&gAdditionalSuites);
	setGlobal(false);

	return kNoErr;
}

inline ASErr Plugin::reloadPlugin(SPInterfaceMessage *message) {
	// only execute the reaload code if the plugin was already loaded once,
	// because the reload message is even sent before the first startup message.
	if (fLoaded) {
		// TODO: error handling:
		setGlobal(true);
		acquireSuites(&gBasicSuites);
		acquireSuites(&gAdditionalSuites);
	}

	return kNoErr;
}

unsigned char *Plugin::toPascal(const char *src, unsigned char *dst) {
	int len = strlen(src);
	
	if (len > 255)
		return NULL;
	
	if (dst == NULL)
		dst = new unsigned char[len + 1];
	
	memmove(dst + 1, src, len);
	dst[0] = (unsigned char) len;
	
	return dst;
}

char *Plugin::fromPascal(const unsigned char *src, char *dst) {
	int len = src[0];

	if (dst == NULL)
		dst = new char[len + 1];
	
	memmove(dst, src + 1, len);
	dst[len] = '\0';
	
	return dst;
}

/*
 * Similar to sAIUser->SPPlatformFileSpecification2Path, but creates a posix path on Mac OS X, because that's what's needed for java
 */
bool Plugin::fileSpecToPath(SPPlatformFileSpecification *fileSpec, char *path) {
#ifdef MAC_ENV
	// java needs a posix path on mac, not a Carbon one, as used by Illustrator:
	// Then transform this into a real FSSpec
	FSSpec fsSpec;
	if (FSMakeFSSpec(fileSpec->vRefNum, fileSpec->parID, fileSpec->name, &fsSpec) != noErr)
		return false;
	// and from there into a Posix path:
	// TODO: in order to be working for non existing files, this would need to be done in a more complicated manner:
	// get the parent dir, convert this to a FSRef and add the filename there, like in pathToFileSpec
	FSRef fsRef;
	if (FSpMakeFSRef(&fsSpec, &fsRef) != noErr)
		return false;
	if (FSRefMakePath(&fsRef, (unsigned char*) path, kMaxPathLength))
		return false;
#else
	// on windows, things are easier because we don't have to convert to a posix path:
	if (sAIUser->SPPlatformFileSpecification2Path(fileSpec, path))
		return false;
#endif
	return true;
}

bool Plugin::pathToFileSpec(char *path, SPPlatformFileSpecification *fileSpec) {
#ifdef MAC_ENV
	// as FSRef can only be created for existing files and directories, this is a bit complicated:
	// create an FSRef for the path's parent dir and from there create a FSSpec for the child: 
	int len = strlen(path);
	int dirPos = len - 1;
	if (path[dirPos] == '/') // skip trailing '/' for folders
		dirPos--;
	// get the path for the parent folder:
	while (dirPos >= 0 && path[dirPos] != '/')
		dirPos--;
		
	// path found?
	if (dirPos < 0)
		return false;
	
	dirPos++;
	
	// now split into directory and file:
	char dirPath[kMaxPathLength];
	char filename[kMaxPathLength];
	memcpy(dirPath, path, dirPos);
	dirPath[dirPos] = '\0';
	int fileLen = len - dirPos;
	memcpy(filename, &path[dirPos], fileLen);
	filename[fileLen] = '\0';
	// now convert the parent directory to a FSRef:
	FSRef fsRef;
	Boolean isDir;
	if (FSPathMakeRef((unsigned char*) dirPath, &fsRef, &isDir) != noErr)
		return false;
	
	// get the information of the parent dir:
	FSCatalogInfo catalogInfo;
	if (FSGetCatalogInfo(&fsRef, kFSCatInfoVolume | kFSCatInfoParentDirID | kFSCatInfoNodeID, &catalogInfo, NULL,  NULL, NULL) != noErr)
		return false;
	
	// and create a FSSpec for the child  with it:
	FSSpec fsSpec;
	OSErr error = FSMakeFSSpec(catalogInfo.volume , catalogInfo.nodeID, toPascal(filename, (unsigned char *) filename), &fsSpec);
	// file not found error is ok:
	if (error != noErr && error != fnfErr)
		return false;

	// copy FSSpec into fileSpec:
	fileSpec->vRefNum = fsSpec.vRefNum;
	fileSpec->parID = fsSpec.parID;
	memcpy(fileSpec->name, fsSpec.name, 64);
#else
	// on windows, things are much easier because we don't have to convert to a posix path:
	if (sAIUser->Path2SPPlatformFileSpecification(path, fileSpec))
		return false;
#endif
	return true;
}

ASErr Plugin::lockPlugin(ASBoolean lock) {
	if (lock) {
		fLockCount++;
		if (fLockCount == 1)
			sSPAccess->AcquirePlugin(fPluginRef, &fPluginAccess);
	} else {
		fLockCount--;
		if (fLockCount == 0) {
			sSPAccess->ReleasePlugin(fPluginAccess);
			fPluginAccess = NULL;
		}
		else if (fLockCount < 0)
			fLockCount = 0;
	}

	return kNoErr;
}

ASErr Plugin::about(SPInterfaceMessage *message) {
/* As we don't want to have adobe credits on our about dialog box, show our own here:
	sADMBasic->AboutBox(fPluginRef,
		"Scriptographer Plugin http://www.scriptographer.com",
		"Juerg Lehni http://www.scratchdisk.com"
	);
*/
	sADMDialog->Modal(fPluginRef, "About Dialog", kAboutDialogID, kADMModalDialogStyle, NULL, NULL, 0);
	return kNoErr;
}

ASBoolean Plugin::isReloadMsg(char *caller, char *selector) {
	return (sSPBasic->IsEqual(caller, kSPAccessCaller ) &&
		sSPBasic->IsEqual( selector, kSPAccessReloadSelector));
}

ASBoolean Plugin::isUnloadMsg(char *caller, char *selector) {
	return (sSPBasic->IsEqual(caller, kSPAccessCaller ) &&
		sSPBasic->IsEqual( selector, kSPAccessUnloadSelector));
}

ASErr Plugin::handleMessage(char *caller, char *selector, void *message) {
	ASErr error = kUnhandledMsgErr;

	/* Sweet Pea messages */

	if (sSPBasic->IsEqual(caller, kSPAccessCaller))  {
		if (sSPBasic->IsEqual( selector, kSPAccessUnloadSelector)) {
			error = unloadPlugin(static_cast<SPInterfaceMessage *>(message));
		} else if (sSPBasic->IsEqual( selector, kSPAccessReloadSelector)) {
			error = reloadPlugin(static_cast<SPInterfaceMessage *>(message));
		}
	} else if (sSPBasic->IsEqual(caller, kSPInterfaceCaller))  {	
		if (sSPBasic->IsEqual(selector, kSPInterfaceAboutSelector)) {
			error = about(static_cast<SPInterfaceMessage *>(message));
		} else if (sSPBasic->IsEqual(selector, kSPInterfaceStartupSelector)) {
			error = startupPlugin(static_cast<SPInterfaceMessage *>(message));
		} else if (sSPBasic->IsEqual(selector, kSPInterfaceShutdownSelector)) {
			error = shutdownPlugin(static_cast<SPInterfaceMessage *>(message));
		}
	} else if (sSPBasic->IsEqual(caller, kSPCacheCaller)) {	
		if (sSPBasic->IsEqual(selector, kSPPluginPurgeCachesSelector)) {
			error = purge() ? kSPPluginCachesFlushResponse : kSPPluginCouldntFlushResponse;
		}
	} else if (sSPBasic->IsEqual( caller, kSPPropertiesCaller )) {
		if (sSPBasic->IsEqual( selector, kSPPropertiesAcquireSelector)) {
			error = acquireProperty((SPPropertiesMessage *) message);
		} else if (sSPBasic->IsEqual( selector, kSPPropertiesReleaseSelector)) {
			error = releaseProperty((SPPropertiesMessage *) message);
		}
	}

	/* Some common AI messages */

	else if (sSPBasic->IsEqual(caller, kCallerAINotify)) {
		AppContext appContext(((SPInterfaceMessage *)message)->d.self);

		// Ideally we would rely upon the caller to envelop our Notify method.
		// But since we won't work right if he doesn't, do this ourselves

		AINotifierMessage *msg = (AINotifierMessage *)message;

		if (sSPBasic->IsEqual(msg->type, kAIApplicationStartedNotifier)) {
			error = postStartupPlugin();
		}
		if (!error || error == kUnhandledMsgErr) {
			if (sSPBasic->IsEqual( selector, kSelectorAINotify )) {
				error = notify(msg);
			}
		}
	} else if (sSPBasic->IsEqual(caller, kCallerAIMenu)) {
		if (sSPBasic->IsEqual( selector, kSelectorAIGoMenuItem )) {
			error = goMenuItem((AIMenuMessage *)message);
		} else if (sSPBasic->IsEqual( selector, kSelectorAIUpdateMenuItem )) {
			error = updateMenuItem((AIMenuMessage *)message);
		}
	} else if (sSPBasic->IsEqual(caller, kCallerAIFilter)) {
		if (sSPBasic->IsEqual( selector, kSelectorAIGetFilterParameters )) {
			error = getFilterParameters((AIFilterMessage *)message);
		} else if (sSPBasic->IsEqual( selector, kSelectorAIGoFilter )) {
			error = goFilter((AIFilterMessage *)message);
		}
	} else if (sSPBasic->IsEqual(caller, kCallerAIPluginGroup)) {
		if (sSPBasic->IsEqual( selector, kSelectorAINotifyEdits )) {
			error = pluginGroupNotify((AIPluginGroupMessage *)message);
		} else if (sSPBasic->IsEqual( selector, kSelectorAIUpdateArt )) {
			error = pluginGroupUpdate((AIPluginGroupMessage *)message);
		}
	} else if (sSPBasic->IsEqual(caller, kCallerAIFileFormat)) {
		if (sSPBasic->IsEqual( selector, kSelectorAIGetFileFormatParameters )) {
			error = getFileFormatParameters((AIFileFormatMessage *)message);
		} else if (sSPBasic->IsEqual( selector, kSelectorAIGoFileFormat )) {
			error = goFileFormat((AIFileFormatMessage *)message);
		} else if (sSPBasic->IsEqual( selector, kSelectorAICheckFileFormat )) {
			error = checkFileFormat((AIFileFormatMessage *)message);
		}
	} else if (sSPBasic->IsEqual(caller, kCallerAITool)) {
		if (sSPBasic->IsEqual( selector, kSelectorAIToolMouseDrag )) {
			error = gEngine->toolMouseDrag((AIToolMessage *)message);
		} else if (sSPBasic->IsEqual( selector, kSelectorAITrackToolCursor )) {
			error = gEngine->toolTrackCursor((AIToolMessage *)message);
		} else if (sSPBasic->IsEqual( selector, kSelectorAIToolMouseDown )) {
			error = gEngine->toolMouseDown((AIToolMessage *)message);
		} else if (sSPBasic->IsEqual( selector, kSelectorAIToolMouseUp )) {
			error = gEngine->toolMouseUp((AIToolMessage *)message);
		} else if (sSPBasic->IsEqual( selector, kSelectorAISelectTool )) {
			error = gEngine->toolSelect((AIToolMessage *)message);
		} else if (sSPBasic->IsEqual( selector, kSelectorAIDeselectTool )) {
			error = gEngine->toolDeselect((AIToolMessage *)message);
		} else if (sSPBasic->IsEqual( selector, kSelectorAIReselectTool )) {
			error = gEngine->toolReselect((AIToolMessage *)message);
		} else if (sSPBasic->IsEqual( selector, kSelectorAIEditToolOptions )) {
			error = gEngine->toolEditOptions((AIToolMessage *)message);
		}
	} else if (sSPBasic->IsEqual(caller, kCallerAILiveEffect)) {
		if (sSPBasic->IsEqual( selector, kSelectorAIEditLiveEffectParameters )) {
			error = gEngine->liveEffectEditParameters((AILiveEffectEditParamMessage *)message);
		} else if (sSPBasic->IsEqual( selector, kSelectorAIGoLiveEffect )) {
			error = gEngine->liveEffectCalculate((AILiveEffectGoMessage *)message);
		} else if (sSPBasic->IsEqual( selector, kSelectorAILiveEffectInterpolate )) {
			error = gEngine->liveEffectInterpolate((AILiveEffectInterpParamMessage *)message);
		} else if (sSPBasic->IsEqual( selector, kSelectorAILiveEffectInputType )) {
			error = gEngine->liveEffectGetInputType((AILiveEffectInputTypeMessage *)message);
		}

	} else if (sSPBasic->IsEqual(caller, kCallerAITimer)) {
		if (sSPBasic->IsEqual( selector, kSelectorAIGoTimer)) {
			error = timer((AITimerMessage *)message);
		}
	} else if (sSPBasic->IsEqual(caller, kCallerAIAnnotation)) {
		if (sSPBasic->IsEqual( selector, kSelectorAIDrawAnnotation)) {
			AIAnnotatorMessage *m = (AIAnnotatorMessage *)message;
			/*
			ADMRect rect;
			ADMFont font;
			OpaqueGrafPtr port;
			sADMDrawer->Create(*m->port, &rect, font, true);
			*/
		}
	}
	// We should probably handle some ADM messages too, but I don't know
	// which ones right now...

	return error;
}

void Plugin::reportError(const char* str, ...) {
	ASBoolean gotBasic = false;
	if (sADMBasic == NULL && sSPBasic != NULL) {
		sSPBasic->AcquireSuite(kADMBasicSuite, kADMBasicSuiteVersion, (const void **) &sADMBasic);
		if (sADMBasic != NULL)
			gotBasic = true;
	}
	if (sADMBasic != NULL) {
		char *text = new char[1024];
		va_list args;
		va_start(args, str);
		vsprintf(text, str, args);
		va_end(args);
		sADMBasic->ErrorAlert(text);
		delete text;
	}
	if (gotBasic) {
		sSPBasic->ReleaseSuite(kADMBasicSuite, kADMBasicSuiteVersion);
		sADMBasic = NULL;
	}
}

void Plugin::reportError(ASErr error) {
	if (!filterError(error)) {
		unsigned long now = time(NULL);
		
		if (error != fLastError || !fSupressDuplicateErrors ||
			now >= fLastErrorTime + fErrorTimeout) {

			fLastError = error;
			fLastErrorTime = now;

			char msg[128];
			char *m;
			m = findMsg(error, msg, sizeof(msg));
			if (m != NULL) {
				char mbuf[128];

				if (strlen(m) < 120) {
					char errString[10];
					if (error < 16385) {  // Then probably a plain ol' number
						sprintf(errString, "%d", error);

					} else {	// Yucky 4-byte string
						int i;
						for (i = 3; i >= 0; i--) {
							errString[i] = (char) ((unsigned long) error) & 0xff;
							error = ((unsigned long) error) >> 8;
						}
						errString[4] = '\0';
					}

					sprintf(mbuf, m, errString);
					m = mbuf;
				}
				reportError(m);
			}
		}
	}
}

char *Plugin::getMsgString(int n, char *buf, int len) {
	ASErr err = sADMBasic->GetIndexString(fPluginRef, 16050, n, buf, len);
	if (err || buf[0] == '\0')
		return NULL;
	else
		return buf;
}

char *Plugin::findMsg(ASErr error, char *buf, int len) {
	int n = 1;
	while (true) {
		char code[10];
		ASErr err = sADMBasic->GetIndexString(fPluginRef, 16050, n, code, sizeof(code));
		// If we got an error, back off and use the last string, which should be
		// the default message
		if (err || code[0] == '\0') {
			if (n == 1)
				return NULL;		// no error strings found
			else
				return getMsgString(n--, buf, len);
		}

		if (code[0] == '-' || (code[0] >= '0' && code[0] <= '9')) {
			// This is a number, so see if it matches
			int c = atoi(code);
			if (c == error)
				return getMsgString(n++, buf, len);

		} else {
			// non numeric 4 byte err code.  (e.g.) '!sel'.
			int	c, i;
			c = 0;

			for (i = 0; i < 4; i++)
				c = (c << 8) + code[i];

			if (c == error)
				return getMsgString(n++, buf, len);
		}
		n += 2;
	}
}

ASBoolean Plugin::filterError(ASErr error) {
	static ASErr errors[] = {
		kRefusePluginGroupReply,
		kWantsAfterMsgPluginGroupReply,
		kMarkValidPluginGroupReply,
		kDontCarePluginGroupReply,
		kToolCantTrackCursorErr,
		kSPPluginCachesFlushResponse,
		kSPSuiteNotFoundError,
		kSPCantAcquirePluginError,
		0
	};

	int i;
	for (i = 0; errors[i] != 0 && errors[i] != error; i++) {}
	return errors[i] != 0;
}

ASErr Plugin::acquireSuites(ImportSuites *suites) {
	if (!suites->acquired) {
		suites->acquired = true;
		ImportSuite *list = suites->suites;
		for (int i = 0; list[i].name != NULL; i++) {
			ASErr error = acquireSuite(&list[i]);
			if (error) return error;	
		}
	}
	return kNoErr;
}

ASErr Plugin::releaseSuites(ImportSuites *suites) {
	if (suites->acquired) {
		suites->acquired = false;
		ImportSuite *list = suites->suites;
		for (int i = 0; list[i].name != NULL; i++) {
			ASErr error = releaseSuite(&list[i]);
			if (error) return error;
		}
	}
	return kNoErr;
}

ASErr Plugin::acquireSuite(ImportSuite *suite) {
	ASErr error = kNoErr;
	char message[256];

	if (suite->suite != NULL) {
		error = sSPBasic->AcquireSuite(suite->name, suite->version, (const void **)suite->suite);
		if (error && sADMBasic != NULL) {
			sprintf(message, "Error: %d, suite: %s, version: %d!", error, suite->name, suite->version);
			sADMBasic->MessageAlert(message);
		}
	}
	return error;
}

ASErr Plugin::releaseSuite(ImportSuite *suite) {
	ASErr error = kNoErr;

	if (suite->suite != NULL) {
		void **s = (void **)suite->suite;
		if (*s != NULL) {
			error = sSPBasic->ReleaseSuite(suite->name, suite->version);
			*s = NULL;
		}
	}
	return error;
}
