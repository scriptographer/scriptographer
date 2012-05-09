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

#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ai_Dictionary.h"

/*
 * com.scriptographer.ai.Dictionary
 */

#define KEY_VISIBLE(KEY) KEY && KEY != gEngine->m_artHandleKey \
		&& KEY != gEngine->m_docReflowKey

int Dictionary_size(AIDictionaryRef dictionary) {
	int size = sAIDictionary->Size(dictionary);
	// Filter out all hidden keys
	if (sAIDictionary->IsKnown(dictionary, gEngine->m_artHandleKey) ||
		sAIDictionary->IsKnown(dictionary, gEngine->m_docReflowKey))
		size--;
	return size;
}

/*
 * int nativeCreate()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Dictionary_nativeCreate(
		JNIEnv *env, jclass obj) {
	try {
		AIDictionaryRef dictionary = NULL;
		sAIDictionary->CreateDictionary(&dictionary);
		return (jint) dictionary;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeCreateLiveEffectParameters()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Dictionary_nativeCreateLiveEffectParameters(
		JNIEnv *env, jclass cls) {
	try {
		AILiveEffectParameters parameters = NULL;
		// AI SDK: "The dictionary created by this function supports Undo operations,
		// which the generic one does not."
		sAILiveEffect->CreateLiveEffectParameters(&parameters);
		return (jint) parameters;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * java.lang.Object nativeGet(int handle, int docHandle, java.lang.Object key)
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_Dictionary_nativeGet(
		JNIEnv *env, jobject obj, jint handle, jint docHandle, jobject key) {
	jobject res = NULL;
	try {
		AIDictionaryRef dictionary = (AIDictionaryRef) handle;
		AIDocumentHandle document = (AIDocumentHandle) docHandle;
		char *str = gEngine->convertString(env, (jstring) env->CallObjectMethod(
				key, gEngine->mid_Object_toString));
		AIDictKey dictKey = sAIDictionary->Key(str);
		delete str;
		if (KEY_VISIBLE(dictKey)) {
			AIEntryRef entry = sAIDictionary->Get(dictionary, dictKey);
			if (entry != NULL) {
				// Keep track of wether the entry was converted to a value or
				// not, so we know if it was auto-released or not. See below.
				bool converted = false;
				AIEntryType type = sAIEntry->GetType(entry);
				switch (type) {
						/*
						 TODO: implement these:
						 UnknownType,
						 // array
						 ArrayType,
						 // Binary data. if the data is stored to file it is the
						 // clients responsibility to
						 deal with the endianess of the data.
						 BinaryType,
						 // a reference to a pattern
						 PatternRefType,
						 // a reference to a brush pattern
						 BrushPatternRefType,
						 // a reference to a custom color (either spot or global
						 // process)
						 CustomColorRefType,
						 // a reference to a gradient
						 GradientRefType,
						 // a reference to a plugin global object
						 PluginObjectRefType,
						 // an unique id
						 UIDType,
						 // an unique id reference
						 UIDREFType,
						 // an XML node
						 XMLNodeType,
						 // a SVG filter
						 SVGFilterType,
						 // an art style
						 ArtStyleType,
						 // a symbol definition reference
						 SymbolPatternRefType,
						 // a graph design reference
						 GraphDesignRefType,
						 // a blend style (transpareny attributes)
						 BlendStyleType,
						 // a graphical object
						 GraphicObjectType
						 */
					case BinaryType: {
						ASInt32 size;
						void *value;
						// So far, only NULL values are supported for binary
						// types. See nativePut()
						if (!sAIEntry->ToBinary(entry, NULL, &size)
								&& size == sizeof(void *)
								&& !sAIEntry->ToBinary(entry, &value, &size)
								&& value == NULL) {
							converted = true;
							res = NULL;
						}
					} break;
					case IntegerType: {
						ASInt32 value;
						if (converted = !sAIEntry->ToInteger(entry, &value))
							res = gEngine->convertInteger(env, value);
					} break;
					case BooleanType: {
						ASBoolean value;
						if (converted = !sAIEntry->ToBoolean(entry, &value))
							res = gEngine->convertBoolean(env, value);
					} break;
					case RealType: {
						ASReal value;
						if (converted = !sAIEntry->ToReal(entry, &value))
							res = gEngine->convertFloat(env, value);
					} break;
					case StringType: {
						const char *value;
						if (converted = !sAIEntry->ToString(entry, &value))
							res = gEngine->convertString(env, value);
					} break;
					case DictType: {
						// This can be either an art object or a dictionary:
						AIDictionaryRef dict;
						AIArtHandle art;
						if (converted = !sAIEntry->ToArt(entry, &art)) {
							res = gEngine->wrapArtHandle(env, art, document);
							// And set its dictionary
							gEngine->setItemDictionary(env, res, dictionary,
									dictKey);
						} else if (converted = !sAIEntry->ToDict(entry, &dict)) {
							res = gEngine->wrapDictionaryHandle(env, dict,
									document, obj);
						}
					} break;
					case PointType: {
						AIRealPoint point;
						if (converted = !sAIEntry->ToRealPoint(entry, &point))
							res = gEngine->convertPoint(env,
									kArtboardCoordinates, &point);
					} break;
					case MatrixType: {
						AIRealMatrix matrix;
						// TODO: Test if conersion is correct
						if (converted = !sAIEntry->ToRealMatrix(entry, &matrix))
							res = gEngine->convertMatrix(env,
									kCurrentCoordinates, kArtboardCoordinates,
									&matrix);
					} break;
					case FillStyleType: {
						AIFillStyle fill;
						if (converted = !sAIEntry->ToFillStyle(entry, &fill))
							res = gEngine->convertFillStyle(env, &fill);
					}
						break;
					case StrokeStyleType: {
						AIStrokeStyle stroke;
						if (converted = !sAIEntry->ToStrokeStyle(entry, &stroke))
							res = gEngine->convertStrokeStyle(env,&stroke);
					}
				}
				// If the entry was not converted through any of the
				// sAIEntry->To* methods, which all auto-release the entry, we
				// need to release it manually.
				if (!converted)
					sAIEntry->Release(entry);
			}
		}
	} EXCEPTION_CONVERT(env);
	return res;
}

/*
 * boolean nativePut(int handle, java.lang.String key, java.lang.Object value)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Dictionary_nativePut(
		JNIEnv *env, jobject obj, jint handle, jstring key, jobject value) {
	jboolean res = false;
	try {
		AIDictionaryRef dictionary = (AIDictionaryRef) handle;
		char *str = gEngine->convertString(env, (jstring) env->CallObjectMethod(
				key, gEngine->mid_Object_toString));
		AIDictKey dictKey = sAIDictionary->Key(str);
		delete str;
		if (KEY_VISIBLE(dictKey)) {
			AIEntryRef entry = NULL;
			std::exception *exc = NULL;
			try {
				/*
				 TODO: implement these:
				 UnknownType,
				 // array
				 ArrayType,
				 // Binary data. if the data is stored to file it is the clients
				 // responsibility to
				 deal with the endianess of the data.
				 BinaryType,
				 // a reference to a pattern
				 PatternRefType,
				 // a reference to a brush pattern
				 BrushPatternRefType,
				 // a reference to a custom color (either spot or global process)
				 CustomColorRefType,
				 // a reference to a gradient
				 GradientRefType,
				 // a reference to a plugin global object
				 PluginObjectRefType,
				 // an unique id
				 UIDType,
				 // an unique id reference
				 UIDREFType,
				 // an XML node
				 XMLNodeType,
				 // a SVG filter
				 SVGFilterType,
				 // an art style
				 ArtStyleType,
				 // a symbol definition reference
				 SymbolPatternRefType,
				 // a graph design reference
				 GraphDesignRefType,
				 // a blend style (transpareny attributes)
				 BlendStyleType,
				 // a graphical object
				 GraphicObjectType
				 */
				if (value == NULL) {
					res = !sAIDictionary->SetBinaryEntry(dictionary, dictKey,
							&value, sizeof(void *)); 
				} else {
					bool isDict = false;
					if (env->IsInstanceOf(value, gEngine->cls_Integer)) {
						entry = sAIEntry->FromInteger(gEngine->convertInteger(
								env, value));
					} else if (env->IsInstanceOf(value, gEngine->cls_Boolean)) {
						entry = sAIEntry->FromBoolean(gEngine->convertBoolean(
								env, value));
					} else if (env->IsInstanceOf(value, gEngine->cls_Float)) {
						entry = sAIEntry->FromReal(gEngine->convertFloat(env,
								value));
					} else if (env->IsInstanceOf(value, gEngine->cls_Double)) {
						entry = sAIEntry->FromReal(gEngine->convertDouble(env,
								value));
					} else if (env->IsInstanceOf(value, gEngine->cls_String)) {
						char *strValue = gEngine->convertString(env,
								(jstring) value);
						entry = sAIEntry->FromString(strValue);
						delete strValue;
					} else if (env->IsInstanceOf(value, gEngine->cls_ai_Item)) {
						AIArtHandle art = gEngine->getArtHandle(env, value);
						res = !sAIDictionary->MoveArtToEntry(dictionary,
								dictKey, art);
						if (res)
							gEngine->setItemDictionary(env, value, dictionary,
									dictKey);
					} else if (env->IsInstanceOf(value,
							gEngine->cls_ai_Dictionary)) {
						entry = sAIEntry->FromDict(gEngine->getDictionaryHandle(
								env, value));
						isDict = true;
					} else if (env->IsInstanceOf(value, gEngine->cls_Map)) {
						// Convert Map to Dictionary through Dictionary
						// constructor, then  use its handle:
						value = gEngine->newObject(env,
								gEngine->cls_ai_Dictionary,
								gEngine->cid_ai_Dictionary, value);
						entry = sAIEntry->FromDict(gEngine->getDictionaryHandle(
								env, value));
						isDict = true;
					} else if (env->IsInstanceOf(value, gEngine->cls_ai_Point)
							|| env->IsInstanceOf(value, gEngine->cls_adm_Point)) {
						AIRealPoint point;
						gEngine->convertPoint(env, kArtboardCoordinates, value,
								&point);
						entry = sAIEntry->FromRealPoint(&point);
					} else if (env->IsInstanceOf(value, gEngine->cls_ai_Matrix)) {
						AIRealMatrix matrix;
						// TODO: Test if conersion is correct
						gEngine->convertMatrix(env, kCurrentCoordinates,
								kArtboardCoordinates, value, &matrix);
						entry = sAIEntry->FromRealMatrix(&matrix);
					} else if (env->IsInstanceOf(value, gEngine->cls_ai_FillStyle)) {
						AIFillStyle style;
						gEngine->convertFillStyle(env, value, &style);
						entry = sAIEntry->FromFillStyle(&style);
					} else if (env->IsInstanceOf(value, gEngine->cls_ai_StrokeStyle)) {
						AIStrokeStyle style;
						gEngine->convertStrokeStyle(env, value, &style);
						entry = sAIEntry->FromStrokeStyle(&style);
					}
					if (entry != NULL) {
						res = !sAIDictionary->Set(dictionary, dictKey, entry);
						if (isDict)
							gEngine->callVoidMethod(env, value,
									gEngine->mid_ai_Dictionary_setValidation,
									obj);
					}
				}
			} catch(std::exception *e) {
				exc = e;
			}
			if (entry)
				sAIEntry->Release(entry);
			if (exc != NULL)
				throw exc;
		}
	} EXCEPTION_CONVERT(env);
	return res;
}

/*
 * boolean nativeRemove(int handle, java.lang.Object key)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Dictionary_nativeRemove(
		JNIEnv *env, jobject obj, jint handle, jobject key) {
	try {
		AIDictionaryRef dictionary = (AIDictionaryRef) handle;
		char *str = gEngine->convertString(env, (jstring) env->CallObjectMethod(
				key, gEngine->mid_Object_toString));
		AIDictKey dictKey = sAIDictionary->Key(str);
		delete str;
		if (KEY_VISIBLE(dictKey))
			return !sAIDictionary->DeleteEntry(dictionary, dictKey);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * boolean containsKey(java.lang.Object key)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ai_Dictionary_containsKey(
		JNIEnv *env, jobject obj, jobject key) {
	try {
		AIDictionaryRef dictionary = gEngine->getDictionaryHandle(env, obj);
		char *str = gEngine->convertString(env, (jstring) env->CallObjectMethod(
				key, gEngine->mid_Object_toString));
		AIDictKey dictKey = sAIDictionary->Key(str);
		delete str;
		return KEY_VISIBLE(dictKey) && sAIDictionary->IsKnown(dictionary,
				dictKey);
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * int size()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_Dictionary_size(JNIEnv *env,
		jobject obj) {
	try {
		AIDictionaryRef dictionary = gEngine->getDictionaryHandle(env, obj);
		return Dictionary_size(dictionary);
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * java.lang.String[] keys()
 */
JNIEXPORT jobjectArray JNICALL Java_com_scriptographer_ai_Dictionary_keys(
		JNIEnv *env, jobject obj) {
	try {
		AIDictionaryRef dictionary = gEngine->getDictionaryHandle(env, obj);
		AIDictionaryIterator iterator;
		if (!sAIDictionary->Begin(dictionary, &iterator)) {
			int index = 0;
			jobjectArray array = env->NewObjectArray(Dictionary_size(dictionary),
					gEngine->cls_String, NULL);
			while (!sAIDictionaryIterator->AtEnd(iterator)) {
				AIDictKey key = sAIDictionaryIterator->GetKey(iterator);
				if (KEY_VISIBLE(key)) {
					const char *str = sAIDictionary->GetKeyString(key);
					env->SetObjectArrayElement(array, index++,
							gEngine->convertString(env, str));
				}
				sAIDictionaryIterator->Next(iterator);
			}
			sAIDictionaryIterator->Release(iterator);
			return array;
		}
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void nativeRelease(int handle)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Dictionary_nativeRelease(
		JNIEnv *env, jobject obj, jint handle) {
	try {
		sAIDictionary->Release((AIDictionaryRef) handle);
	} EXCEPTION_CONVERT(env);
}
