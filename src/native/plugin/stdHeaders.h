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

#ifndef __stdHeaders__
#define __stdHeaders__

#ifdef MAC_ENV
#include <Carbon.h>
#endif // MAC_ENV

#ifdef WIN_ENV
#define _CRT_NONSTDC_NO_DEPRECATE 1
#define _CRT_SECURE_NO_DEPRECATE 1
#define _SECURE_SCL_DEPRECATE 0
// C++ exception specification ignored except to indicate a function is not __declspec(nothrow)
#pragma warning (disable: 4290)	

#define strcasecmp stricmp
#define vsnprintf _vsnprintf

#include "windows.h"
#include <time.h>

// Define constants missing in VC 2005's winuser.h
#ifndef MAPVK_VK_TO_VSC
#define MAPVK_VK_TO_VSC 0
#endif
#ifndef MAPVK_VSC_TO_VK
#define MAPVK_VSC_TO_VK 1
#endif
#ifndef MAPVK_VK_TO_CHAR
#define MAPVK_VK_TO_CHAR 2
#endif
#ifndef MAPVK_VSC_TO_VK_EX
#define MAPVK_VSC_TO_VK_EX 3
#endif
#ifndef MAPVK_VK_TO_VSC_EX
#define MAPVK_VK_TO_VSC_EX 4
#endif

// Define missing isnan and isinf
#define isnan(x) _isnan(x)
#define isinf(x) (!_finite(x))

#endif // WIN_ENV

// JNI
#include <jni.h>

// STD Library 
#include <stdio.h>
#include <string.h>
#include <vector>
#include <sstream>
#include <fstream>
#include <algorithm>
using namespace std;

#include "suites.h"

#include "Array.h"

#define PI 3.14159265358979323846

#endif // __stdHeaders__