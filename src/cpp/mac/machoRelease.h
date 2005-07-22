// ADOBE SYSTEMS INCORPORATED
// Copyright  1993 - 2001 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this 
// file in accordance with the terms of the Adobe license agreement
// accompanying it.  If you have received this file from a source
// other than Adobe, then your use, modification, or distribution
// of it requires the prior written permission of Adobe.
//-------------------------------------------------------------------
//-------------------------------------------------------------------------------
//
//	File:
//		PICarbon.h
//
//	Description:
//		This file contains the prototypes and utilities
//		for Carbon plug-in.
//
//	Use:
//		This particular file will define the flags to
//		build a Carbon version of the plug-in.
//
//-------------------------------------------------------------------------------

#ifndef __machoRelease_h__	// Has this not been defined yet?
#define __machoRelease_h__	// Only include this once by predefining it

#ifndef TARGET_CARBON
#define TARGET_CARBON	1
#endif

#if defined(__MWERKS__)
/* OSMessageNotification.h defines a type membername[0] construct that metrowerks doesn't like */
#define __OS_OSMESSAGENOTIFICATION_H
#endif

#define _MSL_USING_MW_C_HEADERS 1
#define __CF_USE_FRAMEWORK_INCLUDES__	1
#define _MSL_NEEDS_EXTRAS		0

#define TARGET_API_MAC_OSX 		1
#define TARGET_API_MAC_CARBON 	1
#define TARGET_API_MAC_OS8		0
#define TARGET_BUILD_MACHO		1
#define TARGET_BUILD_CFM		0
#define TARGET_RT_MAC_CFM		0
#define BUILDING_FOR_MACH		1

#define _MSL_USING_MW_C_HEADERS 1
#define __NOEXTENSIONS__

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

#endif // __machoRelease_h__
// end machoRelease.h
