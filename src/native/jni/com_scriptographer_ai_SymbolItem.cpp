#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_SymbolItem.h"

/*
 * com.scriptographer.ai.SymbolItem
 */

/*
 * int nativeCreate(int symbolHandle, java.awt.geom.AffineTransform at)
 */

JNIEXPORT jint JNICALL Java_com_scriptographer_ai_SymbolItem_nativeCreate(JNIEnv *env, jclass cls, jint symbolHandle, jobject at) {
	try {
		short paintOrder;
		AIArtHandle artInsert = Art_getInsertionPoint(&paintOrder);
		AIRealMatrix m;
		gEngine->convertMatrix(env, at, &m);
		// harden the matrix as symbols use hard matrixes internaly
		sAIHardSoft->AIRealMatrixHarden(&m);
		AIArtHandle res = NULL;
		sAISymbol->NewInstanceWithTransform((AIPatternHandle) symbolHandle, &m, paintOrder, artInsert, &res);
		return (jint) res;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * int nativeGetSymbol()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_SymbolItem_nativeGetSymbol(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIPatternHandle symbol = NULL;
		sAISymbol->GetSymbolPatternOfSymbolArt(art, &symbol);
		return (jint) symbol;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void setSymbol(com.scriptographer.ai.Symbol symbol)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_SymbolItem_setSymbol(JNIEnv *env, jobject obj, jobject symbol) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		gEngine->getPatternHandle(env, symbol);
		sAISymbol->SetSymbolPatternOfSymbolArt(art, symbol);
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ai.Matrix getMatrix()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_SymbolItem_getMatrix(JNIEnv *env, jobject obj) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIRealMatrix m;
		sAISymbol->GetSoftTransformOfSymbolArt(art, &m);
		return gEngine->convertMatrix(env, &m);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setMatrix(java.awt.geom.AffineTransform at)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_SymbolItem_setMatrix(JNIEnv *env, jobject obj, jobject at) {
	try {
		AIArtHandle art = gEngine->getArtHandle(env, obj, true);
		AIRealMatrix m;
		gEngine->convertMatrix(env, at, &m);
		sAISymbol->SetSoftTransformOfSymbolArt(art, &m);
	} EXCEPTION_CONVERT(env);
}
