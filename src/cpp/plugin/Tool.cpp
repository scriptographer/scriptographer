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
 * $RCSfile: Tool.cpp,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:59 $
 */
 
#include "stdHeaders.h"

#include "Tool.h"
#include "ScriptographerEngine.h"
#ifdef MAC_ENV 
#include "macUtils.h"
#endif

Tool::Tool(int index, SPPluginRef pluginRef, int iconID, int pictureID, int cursorID, long options, Tool *sameGroupTool, Tool *sameToolsetTool) {
	fIndex = index;
	fListEntry = NULL;
	fPictureID = pictureID;
	fCursorID = cursorID;
	fTool = NULL;

	AIAddToolData data;
	
	sprintf(fTitle, "Scriptographer Tool %i", fIndex + 1);
	data.title = fTitle;
	data.tooltip = fTitle;
	
	data.icon = sADMIcon->GetFromResource(pluginRef, NULL, iconID, 0);
	if (data.icon == NULL || sADMIcon->GetType(data.icon) == kUnknown) throw new ASErrException('!ico');

	ASErr error;
	
	if (sameGroupTool != NULL) {
		error = sAITool->GetToolNumberFromName(sameGroupTool->fTitle, &data.sameGroupAs);
		if (error) throw new ASErrException(error);
	} else {
		data.sameGroupAs = kNoTool;
	}
	
	if (sameToolsetTool != NULL) {
		error = sAITool->GetToolNumberFromName(sameToolsetTool->fTitle, &data.sameToolsetAs);
		if (error) throw new ASErrException(error);
	} else {
		data.sameToolsetAs = kNoTool;
	}

	error = sAITool->AddTool(pluginRef, fTitle, &data, options, &fHandle);
	if (error) throw new ASErrException(error);

	error = sAITool->SetToolOptions(fHandle, options);
	if (error) throw new ASErrException(error);
}

Tool::~Tool() {
}

ASBoolean Tool::initScript() {
	jobject tool = getTool();
	if (fFilename[0] != '\0' && tool != NULL) {
#ifdef MAC_ENV
		char path[512];
		carbonPathToPosixPath(fFilename, path);
#else
		const char *path = fFilename;
#endif
		gEngine->callVoidMethodReport(NULL, tool, gEngine->mid_Tool_initScript, gEngine->createJString(NULL, path));
		return true;
	}
	return false;
}

void Tool::reset() {
	fTool = NULL;
	fListEntry = NULL;
	sprintf(fFilename, "");
}

inline jobject Tool::getTool() {
	if (fTool == NULL)
		fTool = gEngine->getTool(fIndex);
	return fTool;
}

void Tool::onEditOptions() {
	gEngine->callVoidMethodReport(NULL, getTool(), gEngine->mid_Tool_onEditOptions);
}

void Tool::onSelect() {
	gEngine->callVoidMethodReport(NULL, getTool(), gEngine->mid_Tool_onSelect);
}

void Tool::onDeselect() {
	gEngine->callVoidMethodReport(NULL, getTool(), gEngine->mid_Tool_onDeselect);
}

void Tool::onReselect() {
	gEngine->callVoidMethodReport(NULL, getTool(), gEngine->mid_Tool_onReselect);
}

void Tool::onMouseDown(float x, float y, int pressure) {
	gEngine->callVoidMethodReport(NULL, getTool(), gEngine->mid_Tool_onMouseDown, x, y, pressure);
}

void Tool::onMouseDrag(float x, float y, int pressure) {
	gEngine->callVoidMethodReport(NULL, getTool(), gEngine->mid_Tool_onMouseDrag, x, y, pressure);
}

void Tool::onMouseUp(float x, float y, int pressure) {
	gEngine->callVoidMethodReport(NULL, getTool(), gEngine->mid_Tool_onMouseUp, x, y, pressure);
}
