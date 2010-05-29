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
#include "ScriptographerEngine.h"
#include "loadJava.h"
#include <sys/stat.h>

#define JRE_KEY "Software\\JavaSoft\\Java Runtime Environment"

void getJVMPath(const char *jrePath, const char *jvmType, char *jvmPath) {
	struct stat s;
	sprintf(jvmPath, "%s\\bin\\%s\\jvm.dll" , jrePath, jvmType);
	if (stat(jvmPath, &s) != 0)
		throw new StringException("No JVM of type `%s' found at `%s'", jvmType, jvmPath);
}

bool getStringFromRegistry(HKEY key, const char *name, char *buf, jint bufsize) {
	DWORD type, size;
	return (RegQueryValueEx(key, name, 0, &type, 0, &size) == 0
		&& type == REG_SZ
		&& (size < (unsigned int)bufsize)
		&& RegQueryValueEx(key, name, 0, 0, (unsigned char*) buf, &size) == 0);
}

void getJREPath(char *jrePath) {
	HKEY key, subkey;

	if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, JRE_KEY, 0, KEY_READ, &key) != 0)
		throw new StringException("Error opening registry key '" JRE_KEY);

	char version[64];
	if (!getStringFromRegistry(key, "CurrentVersion", version, sizeof(version))) {
		RegCloseKey(key);
		throw new StringException("Failed to read registry key '" JRE_KEY "\\CurrentVersion'");
	}

	if (RegOpenKeyEx(key, version, 0, KEY_READ, &subkey) != 0) {
		RegCloseKey(key);
		throw new StringException("Error opening registry key '" JRE_KEY "\\%s'", version);
	}

	if (!getStringFromRegistry(subkey, "JavaHome", jrePath, MAX_PATH)) {
		RegCloseKey(key);
		RegCloseKey(subkey);
		throw new StringException("Failed to read registry key '" JRE_KEY "\\%s\\JavaHome'", version);
	}

	RegCloseKey(key);
	RegCloseKey(subkey);
}

void loadJavaVM(const char *jvmType, CreateJavaVMProc *createJavaVM, GetDefaultJavaVMInitArgsProc *getDefaultJavaVMInitArgs) {
	char jrePath[MAX_PATH];
	getJREPath(jrePath);

	char jvmPath[MAX_PATH];
	getJVMPath(jrePath, jvmType, jvmPath);

	// load the Java VM DLL
	HINSTANCE handle = LoadLibrary(jvmPath);

	if (handle == NULL)
		throw new StringException("Cannot load JVM at %s", jvmPath);

	// now get the function addresses
	*createJavaVM = (CreateJavaVMProc) GetProcAddress(handle, "JNI_CreateJavaVM");
	*getDefaultJavaVMInitArgs = (GetDefaultJavaVMInitArgsProc) GetProcAddress(handle, "JNI_GetDefaultJavaVMInitArgs");

	if (createJavaVM == NULL || getDefaultJavaVMInitArgs == NULL)
		throw new StringException("Cannot find JNI interfaces in: %s", jvmPath);
}

// Code below from http://forums.sun.com/thread.jspa?threadID=5403763

bool canAllocate(DWORD bytes) {
	LPVOID base = VirtualAlloc(NULL, bytes, MEM_RESERVE, PAGE_READWRITE);
	if (base == NULL)
			return false;
	VirtualFree(base, 0, MEM_RELEASE);
	return true;
}

int getMaxHeapAvailable(int maxPermSize, int maxHeapSize, int extraSize) {
	SYSTEM_INFO sysInfo;
	GetSystemInfo(&sysInfo);

	// JVM aligns as follows: 
	// Quoted from size_t GenCollectorPolicy::compute_max_alignment() of JDK 7 hotspot code:
	//	  The card marking array and the offset arrays for old generations are
	//	  committed in os pages as well. Make sure they are entirely full (to
	//	  avoid partial page problems), e.g. if 512 bytes heap corresponds to 1
	//	  byte entry and the os page size is 4096, the maximum heap size should
	//	  be 512*4096 = 2MB aligned.

	// card_size computation from CardTableModRefBS::SomePublicConstants of JDK 7 hotspot code
	DWORD alignmentBytes = sysInfo.dwPageSize * (1 << 9);

	// Make it fit in the alignment structure
	DWORD maxHeapBytes = maxHeapSize + (maxHeapSize % alignmentBytes);
	DWORD originalMaxHeapBytes = maxHeapBytes;

	// Loop and decrement requested amount by one chunk
	// until the available amount is found
	int numMemChunks = maxHeapBytes / alignmentBytes;
	while (!canAllocate(maxHeapBytes + maxPermSize + extraSize) && numMemChunks > 0) {
		numMemChunks--;
		maxHeapBytes = numMemChunks * alignmentBytes;
	}

	if (numMemChunks == 0)
		return 0;

	// If we can allocate the requested size, return it instead of the
	// calculated size.
	return maxHeapBytes == originalMaxHeapBytes ? maxHeapSize : maxHeapBytes;
}

