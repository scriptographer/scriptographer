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
 * $RCSfile: CFM_Debug.h,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2006/06/07 16:44:14 $
 */

#ifndef __CFM_Debug_h__	// Has this not been defined yet?
#define __CFM_Debug_h__	// Only include this once by predefining it

#define MAC_ENV 1

#ifndef _DEBUG
#define _DEBUG 1
#endif

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

// The following is necessary befor inclusion of jni.h
// otherwise the JNIEnv has wrong offsets:
//
// See jni.h, Line 194:
//
// #if !TARGET_RT_MAC_CFM
//    void* cfm_vectors[225];
// #endif /* !TARGET_RT_MAC_CFM */

#ifndef TARGET_RT_MAC_CFM
#define TARGET_RT_MAC_CFM 1 
#endif

#endif // __CFM_Debug_h__
// end CFM_Debug.h
