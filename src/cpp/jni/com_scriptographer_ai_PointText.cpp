#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_PointText.h"

/*
 * com.scriptographer.ai.PointText
 */

/*
 * int nativeCreate(int docHandle, int orient, float x, float y)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ai_PointText_nativeCreate(JNIEnv *env, jclass cls, jint docHandle, jint orient, jfloat x, jfloat y) {
	AIArtHandle art = NULL;

	CREATEART_BEGIN
	
	DEFINE_POINT(pt, x, y);

	sAITextFrame->NewPointText(kPlaceAboveAll, NULL, (AITextOrientation) orient, pt, &art);
	if (art == NULL)
		throw new StringException("Cannot create text object. Please make sure there is an open document.");

	CREATEART_END

	return (jint) art;
}

/*
 * com.scriptographer.ai.Point getAnchor()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_ai_PointText_getAnchor(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle text = gEngine->getArtHandle(env, obj);
		AIRealPoint anchor;
		if (!sAITextFrame->GetPointTextAnchor(text, &anchor))
			return gEngine->convertPoint(env, &anchor);		
	} EXCEPTION_CONVERT(env)
	return NULL;
}
