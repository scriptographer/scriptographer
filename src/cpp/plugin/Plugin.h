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
 * $RCSfile: Plugin.h,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:59 $
 */

#define kMaxStringLength 256

#define kUnhandledMsgErr '!MSG'		// This isn't really an error

#define DLLExport extern "C" __declspec(dllexport)

DLLExport SPAPI SPErr PluginMain( char *caller, char *selector, void *message );

/* common.h */

struct Timer {
#ifdef WIN_ENV
	DWORD handle;
	int period;
#else
	AITimerHandle handle;
	ASBoolean free;
#endif
/*
	JSObject *scope;
	JSFunction *function;
	JSObject *timer;
*/	
	char name[32];
};

class ScriptographerEngine;
class Tool;

/* common.h end */

class Plugin {
protected:
	SPPluginRef fPluginRef;
	char *fPluginName;
	int fLockCount;
	SPAccessRef fPluginAccess;
	ASErr fLastError;
	long fErrorTimeout;
	ASBoolean fSupressDuplicateErrors;
	unsigned long fLastErrorTime;

	AINotifierHandle fAppStartedNotifier;

	ASBoolean fLoaded;
	
	// common.h
public:
	AIMenuItemHandle showMain;
	AIMenuItemHandle showEditor;
	AIMenuItemHandle showConsole;
#ifdef WIN_ENV
	HCURSOR	cursor;
	HFONT font;
#elif MAC_ENV
	CursHandle cursor;
#endif

	struct {
		ADMDialogRef dlg;
		char *name;
		ADMItemRef buttonItems[7];
	} main;

	struct {
		ADMDialogRef dlg;
		char *name;
		ADMItemRef buttonItems[5];
		ASBoolean modified;
	} editor;
	
	struct {
		ADMDialogRef dlg;
		char *name;
	} console;


	char baseDir[300];

	ASBoolean showProgress;
	
	ScriptographerEngine *fEngine;
	Tool *fTools[2];
	// for timers
//	Array <Timer *, Timer *> timers;

	// common.h end

public:
	Plugin(SPPluginRef pluginRef);
	~Plugin();
	
	void reportError(const char* str, ...);
	void reportError(ASErr error);
	static ASBoolean filterError(ASErr error);

	SPPluginRef getPluginRef() { return fPluginRef; }
	
	unsigned char *toPascal(const char *src, unsigned char *dst = NULL);
	char *fromPascal(const unsigned char *src, char *dst = NULL);

	bool fileSpecToPath(SPPlatformFileSpecification *fileSpec, char *path);
	bool pathToFileSpec(char *path, SPPlatformFileSpecification *fileSpec);

	Tool *getTool(char *filename);
	Tool *getTool(AIToolMessage *message);
	Tool *getTool(ADMListEntryRef entry);
	Tool *getTool(int index) {
		return fTools[index];
	}
	
	void reloadEngine();

	ASErr lockPlugin(ASBoolean lock);
	ASBoolean isReloadMsg(char *caller, char *selector);
	ASBoolean isUnloadMsg(char *caller, char *selector);

	ASErr startupPlugin(SPInterfaceMessage *message); 
	ASErr shutdownPlugin(SPInterfaceMessage *message); 
	ASErr unloadPlugin(SPInterfaceMessage *message);
	ASErr reloadPlugin(SPInterfaceMessage *message);
	ASErr acquireProperty(SPPropertiesMessage *message)  {
		return kUnhandledMsgErr;
	}

	ASErr releaseProperty(SPPropertiesMessage *message) {
		return kUnhandledMsgErr;
	}

	const char *getPluginName() {
		return fPluginName;	
	}
	
	ASErr about(SPInterfaceMessage *message);

	ASErr handleMessage(char *caller, char *selector, void *message);

	ASBoolean purge() {
		return false;
	}

	ASErr postStartupPlugin();

	void setGlobal(ASBoolean set);

	ASErr notify(AINotifierMessage *message) {
		return kNoErr;
	}

	ASErr goMenuItem(AIMenuMessage *message);

	ASErr updateMenuItem(AIMenuMessage *message);

	ASErr getFilterParameters(AIFilterMessage *message) {
		return kUnhandledMsgErr;
	}
	
	ASErr goFilter(AIFilterMessage *message) {
		return kUnhandledMsgErr;
	}

	ASErr pluginGroupNotify(AIPluginGroupMessage *message) {
		return kUnhandledMsgErr;
	}

	ASErr pluginGroupUpdate(AIPluginGroupMessage *message) {
		return kUnhandledMsgErr;
	}

	ASErr getFileFormatParameters(AIFileFormatMessage *message) {
		return kUnhandledMsgErr;
	}

	ASErr goFileFormat(AIFileFormatMessage *message) {
		return kUnhandledMsgErr;
	}

	ASErr checkFileFormat(AIFileFormatMessage *message) {
		return kUnhandledMsgErr;
	}

	ASErr editToolOptions(AIToolMessage *message);
	ASErr trackToolCursor(AIToolMessage *message);
	ASErr toolMouseDown(AIToolMessage *message);
	ASErr toolMouseDrag(AIToolMessage *message);
	ASErr toolMouseUp(AIToolMessage *message);
	ASErr selectTool(AIToolMessage *message);
	ASErr deselectTool(AIToolMessage *message);
	ASErr reselectTool(AIToolMessage *message);

	ASErr editLiveEffectParameters(AILiveEffectEditParamMessage *message);
	ASErr goLiveEffect(AILiveEffectGoMessage *message);
	ASErr liveEffectInterpolate(AILiveEffectInterpParamMessage *message);
	ASErr liveEffectGetInputType(AILiveEffectInputTypeMessage *message);
	
	ASErr timer(AITimerMessage *message) {
		return kNoErr;
	}

private:
	ASErr acquireSuites(ImportSuites *suites);
	ASErr releaseSuites(ImportSuites *suites);
	ASErr acquireSuite(ImportSuite *suite);
	ASErr releaseSuite(ImportSuite *suite);
	char *findMsg(ASErr error, char *buf, int len);
	char *getMsgString(int n, char *buf, int len);
};

extern Plugin *gPlugin;
