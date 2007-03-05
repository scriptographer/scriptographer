/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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