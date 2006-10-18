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
 * $RCSfile: MachO_Release.h,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2006/10/18 14:18:41 $
 */

#ifndef __MachO_Release_h__	// Has this not been defined yet?
#define __MachO_Release_h__	// Only include this once by predefining it

#ifndef TARGET_CARBON
#define TARGET_CARBON	1
#endif

#ifdef __MWERKS__

/* OSMessageNotification.h defines a type membername[0] construct that metrowerks doesn't like */
#define __OS_OSMESSAGENOTIFICATION_H

#define _MSL_USING_MW_C_HEADERS 1
#define __CF_USE_FRAMEWORK_INCLUDES__	1
#define _MSL_NEEDS_EXTRAS		0
#define _MSL_USING_MW_C_HEADERS 1
#define __NOEXTENSIONS__

#endif

#define TARGET_API_MAC_OSX 		1
#define TARGET_API_MAC_CARBON 	1
#define TARGET_API_MAC_OS8		0
#define TARGET_BUILD_MACHO		1
#define TARGET_BUILD_CFM		0
#define TARGET_RT_MAC_CFM		0
#define BUILDING_FOR_MACH		1

#define qDebug	  0
#define qRelease  1

//This let's us use p2cstr, c2pstr, C2PStr, and P2CStr.
#define OLDP2C 1

#define MAC_ENV 1

#define Platform_Carbon 1

//We don't want console IO. Turn this on, and we get __nInit. __nInit is a static define in niostream.h,
//and it adds code to every one of our source files in _sint. dmaclach

#define _MSL_NO_CONSOLE_IO

//We don't want debugging, because some of it's macros interfere with our code. dmaclach
#define __DEBUGGING__

#endif // __MachO_Release_h__
// end MachO_Release.h
