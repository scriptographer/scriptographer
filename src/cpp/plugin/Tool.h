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
 * $RCSfile: Tool.h,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:59 $
 */

class Tool {
public: // TODO: make private
	int fIndex;
	char fTitle[32];
	AIToolHandle fHandle;
	char fFilename[300];
	ADMListEntryRef fListEntry;
	int fPictureID;
	int fCursorID;
	
	jobject fTool;
public:
	Tool(int index, SPPluginRef pluginRef, int iconID, int pictureID, int cursorID, long options, Tool *sameGroupTool = NULL, Tool *sameToolsetTool = NULL);
	~Tool();

	ASBoolean initScript();
	void reset();
	jobject getTool();
	
	int getFolderPictureID() {
		return fPictureID;
	}
		
	void setListEntry(ADMListEntryRef entry) {
		fListEntry = entry;
	}

	void onEditOptions();
	void onSelect();
	void onDeselect();
	void onReselect();
	void onMouseDown(float x, float y, int pressure);
	void onMouseDrag(float x, float y, int pressure);
	void onMouseUp(float x, float y, int pressure);
};
