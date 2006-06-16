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
 * $RCSfile: stdHeaders.h,v $
 * $Author: lehni $
 * $Revision: 1.10 $
 * $Date: 2006/06/16 16:18:29 $
 */
 
#if !defined(__STDHEADERS_H_INCLUDED__)
#define __STDHEADERS_H_INCLUDED__

// Derective for Codewarrior to use the pre-compiled header file
#if (defined(__PIMWCWMacPPC__) && !(defined(MakingPreCompiledHeader)))
	#if TARGET_BUILD_MACHO
		#if _DEBUG
			#include "MachO_Debug.ch"
		#else
			#include "MachO_Release.ch"
		#endif
	#else
		#if _DEBUG
			#include "CFM_Debug.ch"
		#else
			#include "CFM_Release.ch"
		#endif
	#endif
#else

#ifdef MAC_ENV
	#include <Carbon.h>
#endif
#ifdef WIN_ENV
	#include "windows.h"
	#include <time.h>
#endif

// STD Library 
#include <stdio.h>
#include <string.h>
#include <vector>
#include <sstream>
#include <fstream>
#include <algorithm>
using namespace std;

// Sweet Pea Headers
#include "SPConfig.h"
#include "SPTypes.h"

// Illustrator Headers
#include "AITypes.h"

// Define versions so they can even be used when compiling for 10 or CS1:
#define kAI10	0x10000001	// AI 10.0
#define kAI11	0x11000001	// AI 11.0
#define kAI12	0x12000001	// AI 12.0

#if kPluginInterfaceVersion < kAI11
	// Compatibility for Illustrator version before CS:
	// ASRect, ASPoint, ASRGBColor, etc. have been deprecated in favor of ADM types with the same
	// name, ADMRect, ADMPoint, etc. The switch to ADMxxx types is painless and makes for a more
	// uniform use of standard Adobe types. If for some reason you cannot switch you can uncomment
	// the old ASxxx types in ASTypes.h.
	#define ADMRect ASRect
	#define ADMPoint ASPoint
#endif

#if kPluginInterfaceVersion < kAI12
	// GetWSProfile in AIOverrideColorConversion.h takes AIColorProfile instead of ASUInt32 since AI12
	#define AIColorProfile ASUInt32
	// was renamed in AI12:
	namespace ATE {
		typedef ATE::GlyphID ATEGlyphID;
	}
#endif

#define PI 3.14159265358979323846

#include <jni.h>
#include "Suites.h"

#endif // #if (defined(__PIMWCWMacPPC__) && !(defined(MakingPreCompiledHeader)))
#endif //  !defined(__STDHEADERS_H_INCLUDED__)