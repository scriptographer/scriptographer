/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 */

#include "jniMacros.h"
#include "exceptions.h"

enum CoordinateSystem {
	/**
	 * Use document (page) coordinate system.
	 */
	kDocumentCoordinates,

	/**
	 * Use artboard coordinate system.
	 */
	kArtboardCoordinates,

	/**
	 * Use the coordinate system.
	 */
	kCurrentCoordinates
};

class ScriptographerEngine {
private:
    JavaVM* m_javaVM;
	char *m_pluginPath;
	bool m_initialized;
	Array<AIDocumentHandle> m_suspendedDocuments;

#ifdef MAC_ENV
	// used for the javaThread workaround:
	MPQueueID m_requestQueue;
	MPQueueID m_responseQueue;
#endif

	// Coordinate System Stuff
	AIRealPoint m_rulerOrigin;
	AIRealPoint m_artboardOrigin;
	bool m_topDownCoordinates;

public:
	AIDictKey m_artHandleKey;
	AIDictKey m_docReflowKey;

	/*
	 * Opaque JVM handles to Java classes, methods and objects required for
	 * Java reflection.  These are computed and cached during initialization.
	 */
// JSE:
	jclass cls_Object;
	jmethodID mid_Object_toString;
	jmethodID mid_Object_equals;

	jclass cls_System;
	jfieldID fid_System_out;

	jclass cls_PrintStream;
	jmethodID mid_PrintStream_println;

	jclass cls_Class;
	jmethodID mid_Class_getName;
	jmethodID mid_Class_getConstructors;

	jclass cls_String;
	jmethodID cid_String;
	jmethodID mid_String_getBytes;

	jclass cls_Number;
	jmethodID mid_Number_intValue;
	jmethodID mid_Number_floatValue;
	jmethodID mid_Number_doubleValue;

	jclass cls_Integer;
	jmethodID cid_Integer;

	jclass cls_Float;
	jmethodID cid_Float;

	jclass cls_Double;
	jmethodID cid_Double;

	jclass cls_Boolean;
	jmethodID cid_Boolean;
	jmethodID mid_Boolean_booleanValue;

	jclass cls_File;
	jmethodID cid_File;
	jmethodID mid_File_getPath;

	jclass cls_Collection;
	jmethodID mid_Collection_add;

	jclass cls_Map;

	jclass cls_ArrayList;
	jmethodID cid_ArrayList;

	jclass cls_OutOfMemoryError;
	
	jclass cls_awt_Color;
	jmethodID cid_awt_Color;
	jmethodID mid_awt_Color_getColorComponents;
	
	jclass cls_awt_ICC_Profile;
	jmethodID mid_awt_ICC_Profile_getInstance;
	
// Loader:
	jclass cls_Loader;
	jmethodID mid_Loader_init;
	jmethodID mid_Loader_reload;
	jmethodID mid_Loader_loadClass;

// Scratchdisk:
	jclass cls_List;
	jmethodID mid_List_size;
	jmethodID mid_List_get;
	
// Scriptographer:
	jclass cls_ScriptographerEngine;
	jmethodID mid_ScriptographerEngine_init;
	jmethodID mid_ScriptographerEngine_destroy;
	jmethodID mid_ScriptographerEngine_reportError;
	jmethodID mid_ScriptographerEngine_onHandleEvent;
	jmethodID mid_ScriptographerEngine_onHandleKeyEvent;

	jclass cls_ScriptographerException;

	jclass cls_CommitManager;
	jmethodID mid_CommitManager_commit;

// AI:
	jclass cls_ai_NativeObject;
	jfieldID fid_ai_NativeObject_handle;

	jclass cls_ai_DocumentObject;
	jfieldID fid_ai_DocumentObject_document;

	jclass cls_ai_Document;
	jmethodID mid_ai_Document_wrapHandle;
	jmethodID mid_ai_Document_onClosed;
	jmethodID mid_ai_Document_onSelectionChanged;
	jmethodID mid_ai_Document_onUndo;
	jmethodID mid_ai_Document_onRedo;
	jmethodID mid_ai_Document_onClear;
	jmethodID mid_ai_Document_onRevert;
	
	jclass cls_ai_Dictionary;
	jmethodID cid_ai_Dictionary;
	jfieldID fid_ai_Dictionary_handle;
	jmethodID mid_ai_Dictionary_wrapHandle;
	jmethodID mid_ai_Dictionary_setValidation;
	
	jclass cls_ai_Tool;
	jmethodID cid_ai_Tool;
	jmethodID mid_ai_Tool_onHandleEvent;

	jclass cls_ai_Point;
	jmethodID cid_ai_Point;
	jfieldID fid_ai_Point_x;
	jfieldID fid_ai_Point_y;
	jmethodID mid_ai_Point_set;

	jclass cls_ai_Rectangle;
	jmethodID cid_ai_Rectangle;
	jfieldID fid_ai_Rectangle_x;
	jfieldID fid_ai_Rectangle_y;
	jfieldID fid_ai_Rectangle_width;
	jfieldID fid_ai_Rectangle_height;
	jmethodID mid_ai_Rectangle_set;

	jclass cls_ai_Size;
	jmethodID cid_ai_Size;
	jfieldID fid_ai_Size_width;
	jfieldID fid_ai_Size_height;
	jmethodID mid_ai_Size_set;
	
	jclass cls_ai_Matrix;
	jmethodID cid_ai_Matrix;
	jmethodID mid_ai_Matrix_getScaleX;
	jmethodID mid_ai_Matrix_getShearY;
	jmethodID mid_ai_Matrix_getShearX;
	jmethodID mid_ai_Matrix_getScaleY;
	jmethodID mid_ai_Matrix_getTranslateX;
	jmethodID mid_ai_Matrix_getTranslateY;
	
	jclass cls_ai_Color;
	jmethodID mid_ai_Color_getComponents;
	jobject obj_ai_Color_NONE;

	jclass cls_ai_GrayColor;
	jmethodID cid_ai_GrayColor;
	
	jclass cls_ai_RGBColor;
	jmethodID cid_ai_RGBColor;
	
	jclass cls_ai_CMYKColor;
	jmethodID cid_ai_CMYKColor;
	
	jclass cls_ai_GradientColor;
	jmethodID cid_ai_GradientColor;
	jmethodID mid_ai_GradientColor_set;
	
	jclass cls_ai_PatternColor;
	jmethodID cid_ai_PatternColor;
	jmethodID mid_ai_PatternColor_set;
	
	jclass cls_ai_Item;
	jfieldID fid_ai_Item_version;
	jfieldID fid_ai_Item_dictionaryHandle;
	jfieldID fid_ai_Item_dictionaryKey;
	jmethodID mid_ai_Item_wrapHandle;
	jmethodID mid_ai_Item_getIfWrapped;
	jmethodID mid_ai_Item_changeHandle;
	jmethodID mid_ai_Item_isValid;
	jmethodID mid_ai_Item_commitIfWrapped;
	
	jclass cls_ai_ItemList;
	jmethodID cid_ItemList;
	jmethodID mid_ai_ItemList_add;

	jclass cls_ai_Path;
	jclass cls_ai_CompoundPath;
	jclass cls_ai_TextItem;
	
	jclass cls_ai_TextRange;
	jmethodID cid_ai_TextRange;
	jfieldID fid_ai_TextRange_glyphRuns;

	jclass cls_ai_TextStory;
	
	jclass cls_ai_PathStyle;
	jmethodID mid_PathStyle_init;
	
	jclass cls_ai_FillStyle;
	jmethodID cid_ai_FillStyle;
	jmethodID mid_ai_FillStyle_init;
	jmethodID mid_ai_FillStyle_initNative;
	
	jclass cls_ai_StrokeStyle;
	jmethodID cid_ai_StrokeStyle;
	jmethodID mid_ai_StrokeStyle_init;
	jmethodID mid_ai_StrokeStyle_initNative;
	
	jclass cls_ai_CharacterStyle;
	jmethodID mid_ai_CharacterStyle_markSetStyle;

	jclass cls_ai_ParagraphStyle;
	jmethodID mid_ai_ParagraphStyle_markSetStyle;
	
	jclass cls_ai_Group;
	
	jclass cls_ai_Raster;
	jfieldID fid_ai_Raster_data;
	
	jclass cls_ai_PlacedFile;

	jclass cls_ai_PlacedSymbol;

	jclass cls_ai_Tracing;
	jmethodID mid_ai_Tracing_markDirty;
	
	jclass cls_ai_Layer;
	
	jclass cls_ai_Segment;
	jclass cls_ai_Curve;
	
	jclass cls_ai_GradientStop;
	jmethodID mid_ai_GradientStop_set;

	jclass cls_ai_Artboard;
	jmethodID mid_ai_Artboard_set;
	
	jclass cls_ai_LiveEffect;
	jmethodID cid_ai_LiveEffect;
	jmethodID mid_ai_LiveEffect_onEditParameters;
	jmethodID mid_ai_LiveEffect_onCalculate;
	jmethodID mid_ai_LiveEffect_onInterpolate;
	jmethodID mid_ai_LiveEffect_onGetInputType;

	jclass cls_ai_LiveEffectParameters;
	jmethodID mid_ai_LiveEffectParameters_wrapHandle;
	
	jclass cls_ai_Annotator;
	jmethodID cid_ai_Annotator;
	jmethodID mid_ai_Annotator_onDraw;
	jmethodID mid_ai_Annotator_onInvalidate;
	
	jclass cls_ai_HitResult;
	jmethodID cid_ai_HitResult;
	
	jclass cls_ai_FileFormat;
	jmethodID cid_ai_FileFormat;

	jclass cls_ai_Timer;
	jmethodID mid_ai_Timer_onExecute;

// UI:
	jclass cls_ui_NativeObject;
	jfieldID fid_ui_NativeObject_handle;
	jclass cls_ui_MenuGroup;
	jmethodID mid_ui_MenuGroup_wrapHandle;

	jclass cls_ui_MenuItem;
	jmethodID mid_ui_MenuItem_wrapHandle;
	jmethodID mid_ui_MenuItem_onSelect;
	jmethodID mid_ui_MenuItem_onUpdate;

//moved out of ADM
	jclass cls_ui_Rectangle;
	jmethodID cid_ui_Rectangle;
	jfieldID fid_ui_Rectangle_x;
	jfieldID fid_ui_Rectangle_y;
	jfieldID fid_ui_Rectangle_width;
	jfieldID fid_ui_Rectangle_height;
	jmethodID mid_ui_Rectangle_set;
	
	jclass cls_ui_Point;
	jmethodID cid_ui_Point;
	jfieldID fid_ui_Point_x;
	jfieldID fid_ui_Point_y;
	jmethodID mid_ui_Point_set;
	
	jclass cls_ui_Size;
	jmethodID cid_ui_Size;
	jfieldID fid_ui_Size_width;
	jfieldID fid_ui_Size_height;
	jmethodID mid_ui_Size_set;
#ifndef ADM_FREE
// ADM:
	

	jclass cls_adm_Dialog;
	jmethodID mid_adm_Dialog_onSizeChanged;
	jmethodID mid_adm_Dialog_deactivateActiveDialog;

	jclass cls_adm_PopupDialog;

	jclass cls_adm_DialogGroupInfo;
	jmethodID cid_adm_DialogGroupInfo;
	
	jclass cls_adm_Drawer;
	jmethodID cid_adm_Drawer;

	jclass cls_adm_FontInfo;
	jmethodID cid_adm_FontInfo;
	
	jclass cls_adm_Image;
	jfieldID fid_adm_Image_byteWidth;
	jfieldID fid_adm_Image_bitsPerPixel;
	jmethodID mid_adm_Image_getIconHandle;

	jclass cls_adm_ListItem;
	jfieldID fid_adm_ListItem_listHandle;

	jclass cls_adm_HierarchyListBox;
	
	jclass cls_adm_ListEntry;
	
	jclass cls_adm_HierarchyListEntry;

	jclass cls_adm_NotificationHandler;
	jfieldID fid_adm_NotificationHandler_tracker;
	jfieldID fid_adm_NotificationHandler_drawer;
	jmethodID mid_adm_NotificationHandler_onNotify;
	jmethodID mid_adm_NotificationHandler_onDraw;

	jclass cls_adm_Tracker;
	jmethodID mid_adm_Tracker_onTrack;

#if defined(MAC_ENV) && kPluginInterfaceVersion >= kAI14
	jclass cls_adm_TextEditItem;
	jfieldID fid_adm_TextEditItem_setSelectionTimer;
#endif
#endif //	#ifndef ADM_FREE
public:
	ScriptographerEngine(const char *pluginPath);
	~ScriptographerEngine();
	
	void init();
	void exit();

#ifdef MAC_ENV
	void javaThread();
#endif

	void initReflection(JNIEnv *env);
	// registerNatives code is automatically generated by jni.js / build.xm in registerNatives.cpp:
	void registerNatives(JNIEnv *env);
	void registerClassNatives(JNIEnv *env, const char *className, const JNINativeMethod *methods, int count);

	void initEngine();
	jstring reloadEngine();

	ASErr onStartup();
	ASErr onShutdown();
	
	bool isInitialized() {
		return m_initialized;
	}
	
	void println(JNIEnv *env, const char *str, ...);
	void reportError(JNIEnv *env);
	
	jstring convertString(JNIEnv *env, const char *str);
	jstring convertString(JNIEnv *env, unsigned char *str);
	char *convertString(JNIEnv *env, jstring jstr, int minLength = 0);
	jstring convertString(JNIEnv *env, const ASUnicode *str, int length = -1);
	ASUnicode *convertString_ASUnicode(JNIEnv *env, jstring jstr);
#if kPluginInterfaceVersion < kAI12
	unsigned char *convertString_Pascal(JNIEnv *env, jstring jstr, int minLength = 0);
#else
	jstring convertString(JNIEnv *env, ai::UnicodeString &str);
	ai::UnicodeString convertString_UnicodeString(JNIEnv *env, jstring jstr);
#endif
#ifdef MAC_ENV
	CFStringRef convertString_CFString(JNIEnv *env, jstring jstr);
#endif

	void updateCoordinateSystem();

	void setTopDownCoordinates(bool topDownCoordinates) {
		m_topDownCoordinates = topDownCoordinates;
	}

	// java.lang.Boolean <-> jboolean
	jobject convertBoolean(JNIEnv *env, jboolean value);	
	jboolean convertBoolean(JNIEnv *env, jobject value);

	// java.lang.Integer <-> jint
	jobject convertInteger(JNIEnv *env, jint value);
	jint convertInteger(JNIEnv *env, jobject value);

	// java.lang.Float <-> jfloat
	jobject convertFloat(JNIEnv *env, jfloat value);
	jfloat convertFloat(JNIEnv *env, jobject value);

	// java.lang.Double <-> jdouble
	jobject convertDouble(JNIEnv *env, jdouble value);
	jdouble convertDouble(JNIEnv *env, jobject value);
	
	// com.scriptographer.ai.Point <-> AIRealPoint
	jobject convertPoint(JNIEnv *env, CoordinateSystem system, AIReal x, AIReal y, jobject res = NULL);	
	jobject convertPoint(JNIEnv *env, CoordinateSystem system, AIRealPoint *point, jobject res = NULL) {
		return convertPoint(env, system, point->h, point->v, res);
	}
	void convertPoint(JNIEnv *env, CoordinateSystem system, jdouble x, jdouble y, AIRealPoint *res);
	void convertPoint(JNIEnv *env, CoordinateSystem system, jobject point, AIRealPoint *res);

	// Segment array point conversion
	void convertSegments(JNIEnv *env, AIReal *data, int count, CoordinateSystem system, bool from);

	// com.scriptographer.ai.Rectangle <-> AIRealRect
	jobject convertRectangle(JNIEnv *env, CoordinateSystem system, AIReal left, AIReal top, AIReal right, AIReal bottom, jobject res = NULL);
	jobject convertRectangle(JNIEnv *env, CoordinateSystem system, AIRealRect *rect, jobject res = NULL) {
		return convertRectangle(env, system, rect->left, rect->top, rect->right, rect->bottom, res);
	}
	void convertRectangle(JNIEnv *env, CoordinateSystem system, jobject rect, AIRealRect *res);	

	// com.scriptographer.ai.Size <-> AIRealPoint
	jobject convertSize(JNIEnv *env, float width, float height, jobject res = NULL);
	jobject convertSize(JNIEnv *env, AIRealPoint *size, jobject res = NULL) {
		return convertSize(env, size->h, size->v, res);
	}
	
	jobject convertSize(JNIEnv *env, AISize *size, jobject res = NULL) {
		return convertSize(env, size->width, size->height, res);
	}
	void convertSize(JNIEnv *env, jobject size, AIRealPoint *res);
	void convertSize(JNIEnv *env, jobject size, AISize *res);


	// com.scriptoggrapher.ai.Matrix <-> AIRealMatrix
	jobject convertMatrix(JNIEnv *env, CoordinateSystem from, CoordinateSystem to, AIRealMatrix *mt, jobject res = NULL);
	AIRealMatrix *convertMatrix(JNIEnv *env, CoordinateSystem from, CoordinateSystem to, jobject mt, AIRealMatrix *res);

	// com.scriptographer.adm.Point <-> ADMPoint
	jobject convertPoint(JNIEnv *env, ADMPoint *point, jobject res = NULL);
	void convertPoint(JNIEnv *env, jobject point, ADMPoint *res);

	// com.scriptographer.adm.Rectangle <-> ADMRect
	jobject convertRectangle(JNIEnv *env, ADMRect *rect, jobject res = NULL);
	void convertRectangle(JNIEnv *env, jobject rect, ADMRect *res);	

	// com.scriptographer.adm.Rectangle <-> Rect //todo: mac?
	jobject ScriptographerEngine::convertRectangle(JNIEnv *env, RECT *rect, jobject res = NULL);
	void convertRectangle(JNIEnv *env, jobject rect, RECT *res);


	// com.scriptographer.adm.Size <-> ADMPoint
	jobject convertSize(JNIEnv *env, ADMPoint *size, jobject res = NULL);
	void convertSize(JNIEnv *env, jobject size, ADMPoint *res);

	// java.awt.Color <-> ADMRGBColor
	jobject convertColor(JNIEnv *env, ADMRGBColor *srcCol);	
	void convertColor(JNIEnv *env, jobject srcCol, ADMRGBColor *dstCol);	

	// com.scriptoggrapher.ai.Color <-> AIColor
	jobject convertColor(JNIEnv *env, AIColor *srcCol, AIReal alpha = -1.0f);
	bool convertColor(JNIEnv *env, jobject srcCol, AIColor *dstCol, AIReal *alpha = NULL);	
	bool convertColor(JNIEnv *env, jfloatArray srcCol, AIColor *dstCol, AIReal *alpha = NULL);

	// AIColor <-> ADMRGBColor
	bool convertColor(ADMRGBColor *srcCol, AIColor *dstCol);	
	bool convertColor(AIColor *srcCol, ADMRGBColor *dstCol);	

	// AIColor <-> AIColor
	bool convertColor(AIColor *srcCol, AIColorConversionSpaceValue dstSpace, AIColor *dstCol, AIReal srcAlpha = -1.0f, AIReal *dstAlpha = NULL);

	// com.scriptoggrapher.ai.FillStyle <-> AIFillStyle
	jobject convertFillStyle(JNIEnv *env, AIFillStyle *style, jobject res = NULL);
	void convertFillStyle(JNIEnv *env, jobject style, AIFillStyle *res);

	// com.scriptoggrapher.ai.StrokeStyle <-> AIStrokeStyle
	jobject convertStrokeStyle(JNIEnv *env, AIStrokeStyle *style, jobject res = NULL);
	void convertStrokeStyle(JNIEnv *env, jobject style, AIStrokeStyle *res);

	// com.scriptoggrapher.ai.ItemList <-> AIArtSet
	jobject convertItemSet(JNIEnv *env, AIArtSet set, bool layerOnly = false);
	AIArtSet convertItemSet(JNIEnv *env, jobject itemSet, bool activate = false);
	AIArtSet convertItemSet(JNIEnv *env, jobjectArray items, bool activate = false);

	// java.io.File <-> SPPlatformFileSpecification
	char *getFilePath(JNIEnv *env, jobject file);
	jobject convertFile(JNIEnv *env, const char *path);
	jobject convertFile(JNIEnv *env, SPPlatformFileSpecification *fileSpec);
	SPPlatformFileSpecification *convertFile(JNIEnv *env, jobject file, SPPlatformFileSpecification *res = NULL);
#ifdef MAC_ENV
	jobject convertFile(JNIEnv *env, CFStringRef path);
#endif
	
	// AI Handles
	int getAIObjectHandle(JNIEnv *env, jobject obj, const char *name);
	int getDocumentObjectHandle(JNIEnv *env, jobject obj, bool activateDoc, const char *name);
	AIDictionaryRef getDictionaryHandle(JNIEnv *env, jobject obj);
	
	AIArtHandle getArtHandle(JNIEnv *env, jobject obj, bool activateDoc = false, AIDocumentHandle *doc = NULL);
	AILayerHandle getLayerHandle(JNIEnv *env, jobject obj, bool activateDoc = false);
	AIDocumentHandle getDocumentHandle(JNIEnv *env, jobject obj, bool activateDoc = false);
	AIDictionaryRef getArtDictionaryHandle(JNIEnv *env, jobject obj);
	AIDictKey getArtDictionaryKey(JNIEnv *env, jobject obj);
	
	inline AIPatternHandle getPatternHandle(JNIEnv *env, jobject obj, bool activateDoc = false) {
		return (AIPatternHandle) getDocumentObjectHandle(env, obj, activateDoc, "pattern");
	}
	
	inline AISwatchRef getSwatchHandle(JNIEnv *env, jobject obj, bool activateDoc = false) {
		return (AISwatchRef) getDocumentObjectHandle(env, obj, activateDoc, "swatch");
	}
	
	inline AIGradientHandle getGradientHandle(JNIEnv *env, jobject obj, bool activateDoc = false) {
		return (AIGradientHandle) getDocumentObjectHandle(env, obj, activateDoc, "gradient");
	}

	inline AIDocumentViewHandle getDocumentViewHandle(JNIEnv *env, jobject obj, bool activateDoc = false) {
		return (AIDocumentViewHandle) getDocumentObjectHandle(env, obj, activateDoc, "view");
	}
	
	inline AIFontKey getFontHandle(JNIEnv *env, jobject obj) {
		return (AIFontKey) getAIObjectHandle(env, obj, "font");
	}

	inline AIToolHandle getToolHandle(JNIEnv *env, jobject obj) {
		return (AIToolHandle) getAIObjectHandle(env, obj, "tool");
	}

	inline AILiveEffectHandle getLiveEffectHandle(JNIEnv *env, jobject obj) {
		return (AILiveEffectHandle) getAIObjectHandle(env, obj, "effect");
	}

	// ATE Handles
	ATE::TextFrameRef getTextFrameHandle(JNIEnv *env, jobject obj, bool activateDoc = false);

	inline ATE::TextRangeRef getTextRangeHandle(JNIEnv *env, jobject obj, bool activateDoc = false) {
		return (ATE::TextRangeRef) getDocumentObjectHandle(env, obj, activateDoc, "text range");
	}
	
	inline ATE::StoryRef getStoryHandle(JNIEnv *env, jobject obj, bool activateDoc = false) {
		return (ATE::StoryRef) getDocumentObjectHandle(env, obj, activateDoc, "text story");
	}
	
	inline ATE::CharFeaturesRef getCharFeaturesHandle(JNIEnv *env, jobject obj) {
		return (ATE::CharFeaturesRef) getAIObjectHandle(env, obj, "character style");
	}
	
	inline ATE::ParaFeaturesRef getParaFeaturesHandle(JNIEnv *env, jobject obj) {
		return (ATE::ParaFeaturesRef) getAIObjectHandle(env, obj, "paragraph style");
	}
	
	jobject wrapTextRangeRef(JNIEnv *env, ATE::TextRangeRef range);
	
	// AI Wrap Handles
	jobject wrapArtHandle(JNIEnv *env, AIArtHandle art, AIDocumentHandle doc = NULL, bool created = false, short type = -1, bool checkWrapped = true);
	bool updateArtIfWrapped(JNIEnv *env, AIArtHandle art);
	void changeArtHandle(JNIEnv *env, jobject itemObject, AIArtHandle art, AIDocumentHandle doc = NULL, bool clearDictionary = false);
	void setItemDictionary(JNIEnv *env, jobject obj, AIDictionaryRef dictionary, AIDictKey key);

	jobject wrapLayerHandle(JNIEnv *env, AILayerHandle layer, AIDocumentHandle doc = NULL);
	jobject wrapMenuGroupHandle(JNIEnv *env, AIMenuGroup item);
	jobject wrapMenuItemHandle(JNIEnv *env, AIMenuItemHandle item);

	jobject wrapDocumentHandle(JNIEnv *env, AIDocumentHandle doc);
	jobject wrapDictionaryHandle(JNIEnv *env, AIDictionaryRef dictionary, AIDocumentHandle doc = NULL, jobject validation = NULL);
	jobject wrapLiveEffectParameters(JNIEnv *env, AILiveEffectParameters parameters, AIDocumentHandle doc = NULL);

	void commit(JNIEnv *env);
	void resumeSuspendedDocuments();
	
	ASErr onSelectionChanged();
	ASErr onDocumentClosed(AIDocumentHandle handle);
	ASErr onUndo();
	ASErr onRedo();
	ASErr onClear();
	ASErr onRevert();

#ifdef WIN_ENV
	ASErr deactivateActiveDialog();
#endif // WIN_ENV

	// AI Tool
	ASErr Tool_onHandleEvent(const char * selector, AIToolMessage *message);

	// AI LiveEffect
	ASErr LiveEffect_onEditParameters(AILiveEffectEditParamMessage *message);
	ASErr LiveEffect_onCalculate(AILiveEffectGoMessage *message);
	ASErr LiveEffect_onInterpolate(AILiveEffectInterpParamMessage *message);
	ASErr LiveEffect_onGetInputType(AILiveEffectInputTypeMessage *message);
	
	// AI MenuItem
	ASErr MenuItem_onSelect(AIMenuMessage *message);
	ASErr MenuItem_onUpdate(AIMenuMessage *message, long inArtwork, long isSelected, long isTrue);

	// AI Annotator
	ASErr Annotator_onDraw(AIAnnotatorMessage *message);
	ASErr Annotator_onInvalidate(AIAnnotatorMessage *message);
	
	bool callOnHandleKeyEvent(int type, ASUInt32 keyCode, ASUnicode character, ASUInt32 modifiers);
	ASErr callOnHandleEvent(int type);

#ifndef ADM_FREE
	// ADM CallbackListener
	void callOnNotify(jobject handler, ADMNotifierRef notifier); 
	void callOnNotify(jobject handler, char *notifier); 
	void callOnDestroy(jobject handler); 
	bool callOnTrack(jobject handler, ADMTrackerRef tracker);
	bool callOnDraw(jobject handler, ADMDrawerRef drawer);

	

	// ADM Handles
	int getADMObjectHandle(JNIEnv *env, jobject obj, const char *name);
	int getADMListHandle(JNIEnv *env, jobject obj, const char *name);

	

	inline ADMDialogRef getDialogHandle(JNIEnv *env, jobject obj) {
		return (ADMDialogRef) getADMObjectHandle(env, obj, "dialog");
	}

	inline ADMDrawerRef getDrawerHandle(JNIEnv *env, jobject obj) {
		return (ADMDrawerRef) getADMObjectHandle(env, obj, "drawer");
	}

	inline ADMTrackerRef getTrackerHandle(JNIEnv *env, jobject obj) {
		return (ADMTrackerRef) getADMObjectHandle(env, obj, "tracker");
	}

	inline ADMIconRef getIconHandle(JNIEnv *env, jobject obj) {
		return (ADMIconRef) getADMObjectHandle(env, obj, "icon");
	}

	inline ADMImageRef getImageHandle(JNIEnv *env, jobject obj) {
		return (ADMImageRef) getADMObjectHandle(env, obj, "image");
	}

	inline ADMItemRef getItemHandle(JNIEnv *env, jobject obj) {
		return (ADMItemRef) getADMObjectHandle(env, obj, "item");
	}

	inline ADMListRef getListBoxHandle(JNIEnv *env, jobject obj) {
		return (ADMListRef) getADMListHandle(env, obj, "list");
	}

	inline ADMHierarchyListRef getHierarchyListBoxHandle(JNIEnv *env, jobject obj) {
		return (ADMHierarchyListRef) getADMListHandle(env, obj, "hierarchy list");
	}

	inline ADMEntryRef getListEntryHandle(JNIEnv *env, jobject obj) {
		return (ADMEntryRef) getADMObjectHandle(env, obj, "list entry");
	}

	inline ADMListEntryRef getHierarchyListEntryHandle(JNIEnv *env, jobject obj) {
		return (ADMListEntryRef) getADMObjectHandle(env, obj, "hierarchy list entry");
	}
	
	// ADM Objects
	jobject getDialogObject(ADMDialogRef dlg);
	jobject getItemObject(ADMItemRef item);
	jobject getListObject(ADMListRef list);
	jobject getListObject(ADMHierarchyListRef list);
	jobject getListEntryObject(ADMEntryRef list);
	jobject getListEntryObject(ADMListEntryRef list);
#else //non ADM

	int getControlObjectHandle(JNIEnv *env, jobject obj, const char *name);
	
	inline AIPanelRef  getAIPanelRef (JNIEnv *env, jobject obj) {
		return (AIPanelRef ) getControlObjectHandle(env, obj, "AiPanelRef");
	}

#endif //#ifndef ADM_FREE

	//former getADMObjectHandle but for Menu
	int getMenuObjectHandle(JNIEnv *env, jobject obj, const char *name);

	// Menu items are in the ADM package in Scriptographer, although natively they belong to AI
	inline AIMenuItemHandle getMenuItemHandle(JNIEnv *env, jobject obj) {
		return (AIMenuItemHandle) getMenuObjectHandle(env, obj, "menu item");
	}
	
	inline AIMenuGroup getMenuGroupHandle(JNIEnv *env, jobject obj) {
		return (AIMenuGroup) getMenuObjectHandle(env, obj, "menu group");
	}

	// JNI stuff:
	JNIEnv *getEnv();
	
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
	jboolean isEqual(JNIEnv *env, jobject obj1, jobject obj2);

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

// Callback function pointers for dynamically loading JNI invocation functions on Windows
typedef jint (JNICALL *CreateJavaVMProc)(JavaVM **jvm, void **env, void *args);
typedef jint (JNICALL *GetDefaultJavaVMInitArgsProc)(void *args);

#ifdef WIN_ENV
#define PATH_SEP_CHR '\\'
#define PATH_SEP_STR "\\"
#define NATIVE_NEWLINE "\r\n"
#else
#define PATH_SEP_CHR '/'
#define PATH_SEP_STR "/"
#define NATIVE_NEWLINE "\r"
#endif

#define MIN(a, b) ((a) < (b) ? (a) : (b))
#define MAX(a, b) ((a) > (b) ? (a) : (b))
#define ABS(a) ((a) >= 0 ? (a) : -(a))
