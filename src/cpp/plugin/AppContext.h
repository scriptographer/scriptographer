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
 * $RCSfile: AppContext.h,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:59 $
 */

/*
 *        Name:	AppContext.hpp
 *      Author:	Dave Lazarony 
 *        Date:	6/11/96					  
 *     Purpose:	Creates a Application Context for setting up the globals in the applications suite.
 *
 * Copyright (c) 1986-1996 Adobe Systems Incorporated, All Rights Reserved.
 *
 */

#ifndef __SPAccess__
#include "SPAccess.h"
#endif

class AppContext
{
protected:
	void *fAppContext;

public:
	AppContext(SPPluginRef plugin);
	~AppContext();
};
