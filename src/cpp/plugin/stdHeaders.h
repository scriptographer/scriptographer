/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Revision: 1.12 $
 * $Date: 2006/10/18 14:18:09 $
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

#include "Suites.h"

#include "Array.h"

#define PI 3.14159265358979323846

#endif // #if (defined(__PIMWCWMacPPC__) && !(defined(MakingPreCompiledHeader)))
#endif //  !defined(__STDHEADERS_H_INCLUDED__)