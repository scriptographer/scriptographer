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

#define kMaxStringLength 256

#define kUnhandledMsgErr '!MSG'		// This isn't really an error
#define kUnloadErr '!ULD'			// This isn't really an error either, it's used to tell PluginMain to remove the plugin

#ifdef MAC_ENV
	#define DLLExport extern "C"
#endif
#ifdef WIN_ENV
	#define DLLExport extern "C" __declspec(dllexport)
#endif

DLLExport SPAPI int main(char *caller, char *selector, void *message);

// Callback macros for automatically adding CFM glue to the callback methods on
// CS Mac
#ifdef MACHO_CFM_GLUE

#include "MachO_CFM_Glue.h"

#define DEFINE_CALLBACK_PROC(PROC) \
	static TVector PROC##_Vector; \
	static void *PROC##_Proc = createCFMGlue((void *) PROC, &PROC##_Vector);

#define CALLBACK_PROC(PROC) \
	PROC##_Proc

#else // !MACHO_CFM_GLUE

#define DEFINE_CALLBACK_PROC(PROC)

#define CALLBACK_PROC(PROC) \
	PROC

#endif // !MACHO_CFM_GLUE

#define RETURN_ERROR(CALL) error = CALL; \
	if (error) return error;

class ScriptographerEngine;

class ScriptographerPlugin {

protected:
	SPPluginRef m_pluginRef;
	char *m_pluginName;
	int m_lockCount;
	SPAccessRef m_pluginAccess;
	ASErr m_lastError;
	long m_errorTimeout;
	bool m_supressDuplicateErrors;
	unsigned long m_lastErrorTime;
	AINotifierHandle m_appStartedNotifier;
	AINotifierHandle m_selectionChangedNotifier;
	AINotifierHandle m_documentClosedNotifier;
	AINotifierHandle m_afterUndoNotifier;
	AINotifierHandle m_afterRedoNotifier;
	AINotifierHandle m_beforeRevertNotifier;
	AINotifierHandle m_afterRevertNotifier;
	AINotifierHandle m_beforeClearNotifier;
	bool m_loaded;
	bool m_started;
	bool m_active;
	bool m_reverting; 
	ScriptographerEngine *m_engine;
	
#ifdef LOGFILE
	FILE *m_logFile;
#endif

public:
	ScriptographerPlugin(SPMessageData *messageData);
	~ScriptographerPlugin();
	
	void reportError(const char* str, ...);
	void reportError(ASErr error);
	static ASBoolean filterError(ASErr error);

#ifdef LOGFILE
	void log(const char *str, ...);
#else
	void log(const char *str, ...) {}; // does nothing
#endif

	SPPluginRef getPluginRef() { return m_pluginRef; }
	
	unsigned char *toPascal(const char *src, unsigned char *dst = NULL);
	char *fromPascal(const unsigned char *src, char *dst = NULL);

	bool fileSpecToPath(SPPlatformFileSpecification *fileSpec, char *path);
	bool pathToFileSpec(const char *path, SPPlatformFileSpecification *fileSpec);
	void setCursor(int cursorID);

#ifdef MAC_ENV
	static unsigned char s_keycodeJavaToMac[256];
	static unsigned char s_keycodeMacToJava[256];
	static OSStatus appEventHandler(EventHandlerCallRef handler, EventRef event, void* userData);
	static OSStatus eventHandler(EventHandlerCallRef handler, EventRef event, void *userData);
#endif
#ifdef WIN_ENV
	static WNDPROC s_defaultAppWindowProc;
	static LRESULT CALLBACK appWindowProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam);
	static HHOOK s_defaultGetMessageProc;
	static UINT s_keyChars[256];
	static BYTE s_lastKeyboardState[256];
	static UINT s_lastKeyCode;
	static UINT s_lastScanCode;
	static bool s_lastIsDead;
	static LRESULT CALLBACK getMessageProc(int code, WPARAM wParam, LPARAM lParam);
#endif

	long getNanoTime();
	bool isKeyDown(int keycode);
	bool isActive() {
		return m_active;
	}

	ASErr onStartupPlugin(SPInterfaceMessage *message); 
	ASErr onShutdownPlugin(SPInterfaceMessage *message); 
	ASErr onUnloadPlugin(SPInterfaceMessage *message);
	ASErr acquireProperty(SPPropertiesMessage *message)  {
		return kUnhandledMsgErr;
	}

	ASErr releaseProperty(SPPropertiesMessage *message) {
		return kUnhandledMsgErr;
	}

	const char *getPluginName() {
		return m_pluginName;	
	}

	bool isStarted() {
		return m_started;
	}

	bool isLoaded() {
		return m_loaded;
	}
	
	ASErr handleMessage(char *caller, char *selector, void *message);

	bool purge() {
		return false;
	}

	ASErr onPostStartupPlugin();

	ASErr getFilterParameters(AIFilterMessage *message) {
		return kUnhandledMsgErr;
	}
	
	ASErr onExecuteFilter(AIFilterMessage *message) {
		return kUnhandledMsgErr;
	}

	ASErr onPluginGroupNotify(AIPluginGroupMessage *message) {
		return kUnhandledMsgErr;
	}

	ASErr onPluginGroupUpdate(AIPluginGroupMessage *message) {
		return kUnhandledMsgErr;
	}

	ASErr onGetFileFormatParameters(AIFileFormatMessage *message) {
		return kUnhandledMsgErr;
	}

	ASErr onExecuteFileFormat(AIFileFormatMessage *message) {
		return kUnhandledMsgErr;
	}

	ASErr onCheckFileFormat(AIFileFormatMessage *message) {
		return kUnhandledMsgErr;
	}

#ifdef MACHO_CFM_GLUE
	void createGluedSuite(void **suite, int size);
	void disposeGluedSuite(void *suite, int size);
#endif
private:
	ASErr acquireSuites(ImportSuites *suites);
	ASErr releaseSuites(ImportSuites *suites);
	ASErr acquireSuite(ImportSuite *suite);
	ASErr releaseSuite(ImportSuite *suite);
	char *findMsg(ASErr error, char *buf, int len);
	char *getMsgString(int n, char *buf, int len);
};

extern ScriptographerPlugin *gPlugin;
