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
 * File created on 23.05.2006.
 *
 * $RCSfile: CFM_Stub.cpp,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2006/06/12 22:16:07 $
 */

#ifndef ACCESSOR_CALLS_ARE_FUNCTIONS
#define ACCESSOR_CALLS_ARE_FUNCTIONS 1
#endif

#ifndef OPAQUE_TOOLBOX_STRUCTS
#define OPAQUE_TOOLBOX_STRUCTS 1
#endif

#ifndef TARGET_API_MAC_CARBON
#define TARGET_API_MAC_CARBON 1
#endif

#ifndef TARGET_CARBON
#define TARGET_CARBON 1
#endif

#ifndef __PIMWCWMacPPC__
#define __PIMWCWMacPPC__ 1
#endif

#include <Carbon.h>
#include <string.h>
#include <stdio.h>
#include "SPInterf.h"
#include "AITypes.h"
#include "AIUser.h"

#define DllExport extern "C" __declspec(dllexport)

typedef int (* MainProc)(char *caller, char *selector, void *message);

DllExport SPAPI SPErr main(char *caller, char *selector, void *message) {
	static MainProc pMainProc = NULL;
	if (!pMainProc) {
		((SPMessageData *) message)->globals = NULL;
		SPBasicSuite *sSPBasic = ((SPMessageData *) message)->basic;

		// Determine the plugin's filesSpecifications.
		SPPlatformFileSpecification fileSpec;
		SPPluginsSuite *sSPPlugins = NULL;
		if (sSPBasic->AcquireSuite(kSPPluginsSuite, kSPPluginsSuiteVersion, (const void **) &sSPPlugins))
			return kCantHappenErr;
		if (sSPPlugins->GetPluginFileSpecification(((SPMessageData *) message)->self, &fileSpec))
			return kCantHappenErr;
		sSPBasic->ReleaseSuite(kSPPluginsSuite, kSPPluginsSuiteVersion);
		
		// Now convert the specs to a platform path.
		char path[kMaxPathLength];
		AIUserSuite *sAIUser = NULL;
		if (sSPBasic->AcquireSuite(kAIUserSuite, kAIUserSuiteVersion, (const void **) &sAIUser))
			return kCantHappenErr;
		if (sAIUser->SPPlatformFileSpecification2Path(&fileSpec, path))
			return kCantHappenErr;
		sSPBasic->ReleaseSuite(kAIUserSuite, kAIUserSuiteVersion);

		// Add the relative path to the MachO bundle.
		// TODO: this should probably be somewhere else than MacOSClassic.
		strcat(path, "Contents:MacOSClassic:Scriptographer.aip");
		
		// Load the bundle from the hfs path.
		CFStringRef pathStr = CFStringCreateWithCString(NULL, path, NULL);
		CFURLRef bundleURL = CFURLCreateWithFileSystemPath(NULL, pathStr, kCFURLHFSPathStyle, true);
		CFBundleRef bundle = CFBundleCreate(NULL, bundleURL);
		CFRelease(bundleURL);
		CFRelease(pathStr);

		// Extract the function pointer for main.
		if(bundle && CFBundleLoadExecutable(bundle)) {
			pMainProc = (MainProc) CFBundleGetFunctionPointerForName(bundle, CFSTR("main"));
		}
		if (!pMainProc) {
			return kCantHappenErr;
		}
	}
	return pMainProc(caller, selector, message);
}

