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
 * $RCSfile: ScriptographerEngine.h,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/03/25 00:27:58 $
 */

#include "jniMacros.h"
#include "exceptions.h"

class Tool;

class ScriptographerEngine {
private:
    JavaVM* fJavaVM;
	jobject fJavaEngine; // returned by ScriptographerEngine.getInstance
	char *fHomeDir;
	bool fInitialized;

#ifdef MAC_ENV
	// used for the javaThread workaround:
	MPQueueID fRequestQueue;
	MPQueueID fResponseQueue;
#endif

public:
	/*
	 * Opaque JVM handles to Java classes, methods and objects required for
	 * Java reflection.  These are computed and cached during initialization.
	 */
// JSE:
	jclass cls_System;
	jfieldID fid_System_out;
	
	jclass cls_PrintStream;
	jmethodID mid_PrintStream_println;

	jclass cls_Class;
	jmethodID mid_Class_getName;

	jclass cls_String;
	jmethodID cid_String;
	jmethodID mid_String_getBytes;
	
	jclass cls_Number;
	jmethodID mid_Number_intValue;
	
	jclass cls_Integer;
	jmethodID cid_Integer;
	
	jclass cls_Float;
	jmethodID cid_Float;
	
	jclass cls_Boolean;
	jmethodID cid_Boolean;
	jmethodID mid_Boolean_booleanValue;

	jclass cls_ObjectArray;
	
	jclass cls_File;
	jmethodID cid_File;
	jmethodID mid_File_getPath;
	
	jclass cls_Map;
	jmethodID mid_Map_entrySet;
	jmethodID mid_Map_put;
	jmethodID mid_Map_get;
	
	jclass cls_Map_Entry;
	jmethodID mid_Map_Entry_getKey;
	jmethodID mid_Map_Entry_getValue;
	
	jclass cls_HashMap;
	jmethodID cid_HashMap;
	
	jclass cls_Set;
	jmethodID mid_Set_iterator;
	
	jclass cls_Iterator;
	jmethodID mid_Iterator_hasNext;
	jmethodID mid_Iterator_next;

	jclass cls_OutOfMemoryError;
	
	jclass cls_awt_Color;
	jmethodID cid_awt_Color;
	jmethodID mid_awt_Color_getColorComponents;

	jclass cls_awt_Rectangle;
	jmethodID cid_awt_Rectangle;
	jfieldID fid_awt_Rectangle_x;
	jfieldID fid_awt_Rectangle_y;
	jfieldID fid_awt_Rectangle_width;
	jfieldID fid_awt_Rectangle_height;
	jmethodID mid_awt_Rectangle_setBounds;

	jclass cls_awt_Point;
	jmethodID cid_awt_Point;
	jfieldID fid_awt_Point_x;
	jfieldID fid_awt_Point_y;
	jmethodID mid_awt_Point_setLocation;
	
	jclass cls_awt_Dimension;
	jmethodID cid_awt_Dimension;
	jfieldID fid_awt_Dimension_width;
	jfieldID fid_awt_Dimension_height;
	jmethodID mid_awt_Dimension_setSize;
	
	jclass cls_awt_AffineTransform;
	jmethodID cid_awt_AffineTransform;
	jmethodID mid_awt_AffineTransform_getScaleX;
	jmethodID mid_awt_AffineTransform_getShearY;
	jmethodID mid_awt_AffineTransform_getShearX;
	jmethodID mid_awt_AffineTransform_getScaleY;
	jmethodID mid_awt_AffineTransform_getTranslateX;
	jmethodID mid_awt_AffineTransform_getTranslateY;
	
	jclass cls_awt_ICC_Profile;
	jmethodID mid_awt_ICC_Profile_getInstance;
	
// Loader:
	jclass cls_Loader;
	jmethodID mid_Loader_init;
	jmethodID mid_Loader_reload;
	jmethodID mid_Loader_loadClass;
	
// Scriptographer:
	jclass cls_ScriptographerEngine;
	jmethodID mid_ScriptographerEngine_getInstance;
	jmethodID mid_ScriptographerEngine_executeString;
	jmethodID mid_ScriptographerEngine_executeFile;
	jmethodID mid_ScriptographerEngine_init;
	jmethodID mid_ScriptographerEngine_destroy;
	jmethodID mid_ScriptographerEngine_onAbout;

	jclass cls_ScriptographerException;

	jclass cls_Handle;
	jmethodID cid_Handle;
	jfieldID fid_Handle_handle;

// AI:
	jclass cls_AIObject;
	jfieldID fid_AIObject_handle;

	jclass cls_Tool;
	jmethodID cid_Tool;
	jmethodID mid_Tool_onEditOptions;
	jmethodID mid_Tool_onSelect;
	jmethodID mid_Tool_onDeselect;
	jmethodID mid_Tool_onReselect;
	jmethodID mid_Tool_onMouseDrag;
	jmethodID mid_Tool_onMouseDown;
	jmethodID mid_Tool_onMouseUp;

	jclass cls_Point;
	jmethodID cid_Point;
	jfieldID fid_Point_x;
	jfieldID fid_Point_y;
	jmethodID mid_Point_setPoint;

	jclass cls_Rectangle;
	jmethodID cid_Rectangle;
	jfieldID fid_Rectangle_x;
	jfieldID fid_Rectangle_y;
	jfieldID fid_Rectangle_width;
	jfieldID fid_Rectangle_height;
	jmethodID mid_Rectangle_setRect;
	
	jclass cls_Color;
	jmethodID mid_Color_getComponents;

	jclass cls_Grayscale;
	jmethodID cid_Grayscale;
	
	jclass cls_RGBColor;
	jmethodID cid_RGBColor;
	
	jclass cls_CMYKColor;
	jmethodID cid_CMYKColor;

	jclass cls_Art;
	jmethodID mid_Art_wrapArtHandle;
	jmethodID mid_Art_onSelectionChanged;

	jclass cls_Path;
	
	jclass cls_PathStyle;
	jmethodID mid_PathStyle_init;
	
	jclass cls_FillStyle;
	jmethodID cid_FillStyle;
	
	jclass cls_StrokeStyle;
	jmethodID cid_StrokeStyle;
	
	jclass cls_Group;
	
	jclass cls_Raster;
	jfieldID fid_Raster_rasterData;
	
	jclass cls_Layer;

	jclass cls_Segment;
	jclass cls_Curve;
	
	jclass cls_SegmentPosition;
	jmethodID cid_SegmentPosition;
	
	jclass cls_TabletValue;
	jmethodID cid_TabletValue;
	jfieldID fid_TabletValue_offset;
	jfieldID fid_TabletValue_value;
	
	jclass cls_Document;
	
	jclass cls_LiveEffect;
	jmethodID cid_LiveEffect;
	jmethodID mid_LiveEffect_onEditParameters;
	jmethodID mid_LiveEffect_onCalculate;
	jmethodID mid_LiveEffect_onInterpolate;
	jmethodID mid_LiveEffect_onGetInputType;
	
	jclass cls_MenuItem;
	jmethodID mid_MenuItem_wrapItemHandle;
	jmethodID mid_MenuItem_onClick;
	jmethodID mid_MenuItem_onUpdate;

	jclass cls_MenuGroup;
	
// ADM:
	jclass cls_ADMObject;
	jfieldID fid_ADMObject_handle;

	jclass cls_Dialog;
	jfieldID fid_Dialog_size;
	jfieldID fid_Dialog_bounds;

	jclass cls_ModalDialog;
	jfieldID fid_ModalDialog_doesModal;

	jclass cls_PopupDialog;

	jclass cls_DialogGroupInfo;
	jmethodID cid_DialogGroupInfo;
	
	jclass cls_Drawer;
	
	jclass cls_Image;
	jfieldID fid_Image_byteWidth;
	jfieldID fid_Image_bitsPerPixel;
	jmethodID mid_Image_getIconHandle;

	jclass cls_Item;
	jfieldID fid_Item_size;
	jfieldID fid_Item_bounds;

	jclass cls_ListItem;
	jfieldID fid_ListItem_listHandle;

	jclass cls_List;	
	jclass cls_HierarchyList;
	
	jclass cls_ListEntry;
	
	jclass cls_HierarchyListEntry;

	jclass cls_NotificationHandler;
	jfieldID fid_NotificationHandler_tracker;
	jfieldID fid_NotificationHandler_drawer;
	jmethodID mid_NotificationHandler_onNotify_String;
	jmethodID mid_NotificationHandler_onNotify_int;
	jmethodID mid_NotificationHandler_onDraw;

	jclass cls_CallbackHandler;
	jmethodID mid_CallbackHandler_onResize;
		
	jclass cls_Tracker;
	jmethodID mid_Tracker_onTrack;

public:
	ScriptographerEngine(const char *homeDir);
	~ScriptographerEngine();
	
	void init();
	JavaVM *exit();

#ifdef MAC_ENV
	JavaVM *javaThread();
#endif

	void initReflection(JNIEnv *env);
	// registerNatives code is automatically generated by jni.js / build.xm in registerNatives.cpp:
	void registerNatives(JNIEnv *env);
	void registerClassNatives(JNIEnv *env, const char *className, const JNINativeMethod *methods, int count);

	void initEngine();
	jstring reloadEngine();
	jobject getJavaEngine();
	
	bool isInitialized() {
		return fInitialized;
	}
		
	void executeString(const char *filename);
	void executeFile(const char *script);
	void println(JNIEnv *env, const char *str, ...);
	
	// com.scriptographer.awt.Point <-> AIRealPoint
	jobject convertPoint(JNIEnv *env, AIRealPoint *pt, jobject res = NULL);	
	AIRealPoint *convertPoint(JNIEnv *env, jobject pt, AIRealPoint *res = NULL);

	// java.awt.Point <-> ADMPoint
	jobject convertPoint(JNIEnv *env, ADMPoint *pt, jobject res = NULL);
	ADMPoint *convertPoint(JNIEnv *env, jobject pt, ADMPoint *res = NULL);

	// com.scriptographer.awt.Rectangle <-> AIRealRect
	jobject convertRectangle(JNIEnv *env, AIRealRect *rt, jobject res = NULL);	
	AIRealRect *convertRectangle(JNIEnv *env, jobject rt, AIRealRect *res = NULL);	

	// java.awt.Rectangle <-> ADMRect
	jobject convertRectangle(JNIEnv *env, ADMRect *rt, jobject res = NULL);
	ADMRect *convertRectangle(JNIEnv *env, jobject rt, ADMRect *res = NULL);	

	// java.awt.Dimension <-> ADMPoint
	jobject convertDimension(JNIEnv *env, ADMPoint *dim, jobject res = NULL);
	ADMPoint *convertDimension(JNIEnv *env, jobject dim, ADMPoint *res = NULL);

	// java.awt.Color <-> ADMRGBColor
	jobject convertColor(JNIEnv *env, ADMRGBColor *srcCol);	
	ADMRGBColor *convertColor(JNIEnv *env, jobject srcCol, ADMRGBColor *dstCol = NULL);	

	// com.scriptoggrapher.ai.Color <-> AIColor
	jobject convertColor(JNIEnv *env, AIColor *srcCol, AIReal alpha = 1.0f);
	AIColor *convertColor(JNIEnv *env, jobject srcCol, AIColor *dstCol = NULL, AIReal *alpha = NULL);	
	AIColor *convertColor(JNIEnv *env, jfloatArray srcCol, AIColor *dstCol = NULL, AIReal *alpha = NULL);

	// AIColor <-> ADMRGBColor
	AIColor *convertColor(ADMRGBColor *srcCol, AIColor *dstCol = NULL);	
	ADMRGBColor *convertColor(AIColor *srcCol, ADMRGBColor *dstCol = NULL);	

	// AIColor <-> AIColor
	AIColor *convertColor(AIColor *srcCol, AIColorConversionSpaceValue dstSpace, AIColor *dstCol = NULL, AIReal srcAlpha = 1.0f, AIReal *dstAlpha = NULL);

	// java.awt.AffineTransform <-> AIRealMatrix
	jobject convertMatrix(JNIEnv *env, AIRealMatrix *mt, jobject res = NULL);
	AIRealMatrix *convertMatrix(JNIEnv *env, jobject mt, AIRealMatrix *res = NULL);
	
	// java.util.Map <-> AIDictionary
	jobject convertDictionary(JNIEnv *env, AIDictionaryRef dictionary, jobject map = NULL, bool onlyNew = false);
	AIDictionaryRef convertDictionary(JNIEnv *env, jobject map, AIDictionaryRef dictionary = NULL);

	// AI Handles
	AIArtHandle getArtHandle(JNIEnv *env, jobject obj);
	AILayerHandle getLayerHandle(JNIEnv *env, jobject obj);
	AIDocumentHandle getDocumentHandle(JNIEnv *env, jobject obj);
	AILiveEffectHandle getLiveEffectHandle(JNIEnv *env, jobject obj);
	AIMenuItemHandle getMenuItemHandle(JNIEnv *env, jobject obj);
	AIMenuGroup getMenuGroupHandle(JNIEnv *env, jobject obj);
	
	// AI Wrap Handles
	jobject wrapArtHandle(JNIEnv *env, AIArtHandle handle);
	jobject wrapLayerHandle(JNIEnv *env, AILayerHandle layer);
	jobject wrapMenuItemHandle(JNIEnv *env, AIMenuItemHandle item);

	ASErr selectionChanged();
	
	// AI Tool
	ASErr toolEditOptions(AIToolMessage *message);
	ASErr toolTrackCursor(AIToolMessage *message);
	ASErr toolSelect(AIToolMessage *message);
	ASErr toolDeselect(AIToolMessage *message);
	ASErr toolReselect(AIToolMessage *message);
	ASErr toolMouseDrag(AIToolMessage *message);
	ASErr toolMouseDown(AIToolMessage *message);
	ASErr toolMouseUp(AIToolMessage *message);

	// AI LiveEffect
	jobject getLiveEffectParameters(JNIEnv *env, AILiveEffectParameters parameters);
	AILiveEffectParamContext getLiveEffectContext(JNIEnv *env, jobject parameters);
	ASErr liveEffectEditParameters(AILiveEffectEditParamMessage *message);
	ASErr liveEffectCalculate(AILiveEffectGoMessage *message);
	ASErr liveEffectInterpolate(AILiveEffectInterpParamMessage *message);
	ASErr liveEffectGetInputType(AILiveEffectInputTypeMessage *message);
	
	// AI MenuItem
	ASErr menuItemExecute(AIMenuMessage *message);
	ASErr menuItemUpdate(AIMenuMessage *message, long inArtwork, long isSelected, long isTrue);
	
	// ADM CallbackListener
	void callOnNotify(jobject handler, ADMNotifierRef notifier); 
	void callOnDestroy(jobject handler); 
	bool callOnTrack(jobject handler, ADMTrackerRef tracker);
	void callOnDraw(jobject handler, ADMDrawerRef drawer);

	ASErr about();

	// ADM Refs
	ADMDialogRef getDialogRef(JNIEnv *env, jobject obj);
	ADMDrawerRef getDrawerRef(JNIEnv *env, jobject obj);
	ADMTrackerRef getTrackerRef(JNIEnv *env, jobject obj);
	ADMIconRef getIconRef(JNIEnv *env, jobject obj);
	ADMImageRef getImageRef(JNIEnv *env, jobject obj);
	ADMItemRef getItemRef(JNIEnv *env, jobject obj);
	ADMListRef getListRef(JNIEnv *env, jobject obj);
	ADMHierarchyListRef getHierarchyListRef(JNIEnv *env, jobject obj);
	ADMEntryRef getListEntryRef(JNIEnv *env, jobject obj);
	ADMListEntryRef getHierarchyListEntryRef(JNIEnv *env, jobject obj);
	
	// ADM Objects
	jobject getDialogObject(ADMDialogRef dlg);
	jobject getItemObject(ADMItemRef item);
	jobject getListObject(ADMListRef list);
	jobject getListObject(ADMHierarchyListRef list);
	jobject getListEntryObject(ADMEntryRef list);
	jobject getListEntryObject(ADMListEntryRef list);
		
	// JNI stuff:
	JNIEnv *getEnv();
	
	jstring createJString(JNIEnv *env, const char *str);
	char *createCString(JNIEnv *env, jstring jstr);
	void throwException(JNIEnv *env, const char* name, const char* msg);
	void throwException(JNIEnv *env, const char* msg);

	// for reflection:
	jclass findClass(JNIEnv *env, const char *name);
	jclass loadClass(JNIEnv *env, const char *name);
	
	jmethodID getMethodID(JNIEnv *env, jclass cls, const char *name, const char *signature);
	jmethodID getStaticMethodID(JNIEnv *env, jclass cls, const char *name, const char *signature);
	jmethodID getConstructorID(JNIEnv *env, jclass cls, const char *signature);
	
	jfieldID getFieldID(JNIEnv *env, jclass cls, const char *name, const char *signature);
	jfieldID getStaticFieldID(JNIEnv *env, jclass cls, const char *name, const char *signature);
	
	// wrappers for JNI function with added exception handling
	jobject newObject(JNIEnv *env, jclass cls, jmethodID ctr, ...);

	// get<type>Field(JNIEnv * env, jobject obj, jfieldID fid);
	JNI_DECLARE_GETFIELD_FUNCTIONS
	// set<type>Field(JNIEnv * env, jobject obj, jfieldID fid, <type> val);
	JNI_DECLARE_SETFIELD_FUNCTIONS
	// getStatic<type>Field(JNIEnv * env, jclass cls, const char *name, const char *signature);
	JNI_DECLARE_GETSTATICFIELD_FUNCTIONS
	// call<type>Method(JNIEnv * env, jobject obj, jmethodID mid, ...);
	// callStatic<type>Method(JNIEnv * env, jclass cls, jmethodID mid, ...);
	// call<type>MethodReport(JNIEnv * env, jobject obj, jmethodID mid, ...);
	// callStatic<type>MethodReport(JNIEnv * env, jclass cls, jmethodID mid, ...);
	JNI_DECLARE_CALLMETHOD_FUNCTIONS
};

extern ScriptographerEngine *gEngine;

// for the JVM invocation:
typedef jint (JNICALL *CreateJavaVMProc)(JavaVM **jvm, void **env, void *args);
typedef jint (JNICALL *GetDefaultJavaVMInitArgsProc)(void *args);

#ifdef WIN_ENV
#define PATH_SEP_CHR '\\'
#define PATH_SEP_STR "\\"
#else
#define PATH_SEP_CHR '/'
#define PATH_SEP_STR "/"
#endif

#define MIN(a, b) ((a) < (b) ? (a) : (b))
#define MAX(a, b) ((a) > (b) ? (a) : (b))
#define ABS(a) ((a) >= 0 ? (a) : -(a))
