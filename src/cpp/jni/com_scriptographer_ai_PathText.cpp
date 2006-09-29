#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_PathText.h"

/*
 * com.scriptographer.ai.PathText
 */

/*
 * long nativeCreate(int docHandle, int orient, int artHandle)
 */
JNIEXPORT jlong JNICALL Java_com_scriptographer_ai_PathText_nativeCreate__III(JNIEnv *env, jclass cls, jint docHandle, jint orient, jint artHandle) {
	AIArtHandle art = NULL;

	CREATEART_BEGIN

	AIArtHandle artLayer = Layer_beginCreateArt();
	sAITextFrame->NewOnPathText(artLayer != NULL ? kPlaceInsideOnTop : kPlaceAboveAll, artLayer, (AITextOrientation) orient, (AIArtHandle) artHandle, 0, -1, NULL, false, &art);
	if (art == NULL)
		throw new StringException("Cannot create text object. Please make sure there is an open document.");

	CREATEART_END

	return (jlong) art;
}

/*
 * jlong nativeCreate(int docHandle, int orient, int artHandle, float x, float y)
 */
JNIEXPORT jlong JNICALL Java_com_scriptographer_ai_PathText_nativeCreate__IIIFF(JNIEnv *env, jclass cls, jint docHandle, jint orient, jint artHandle, jfloat x, jfloat y) {
	AIArtHandle art = NULL;

	CREATEART_BEGIN
	
	AIArtHandle artLayer = Layer_beginCreateArt();
	DEFINE_POINT(pt, x, y);
	sAITextFrame->NewOnPathText2(artLayer != NULL ? kPlaceInsideOnTop : kPlaceAboveAll, artLayer, (AITextOrientation) orient, (AIArtHandle) artHandle, pt, NULL, false, &art);
	if (art == NULL)
		throw new StringException("Cannot create text object. Please make sure there is an open document.");

	CREATEART_END

	return (jlong) art;
}

/*
 * float[] getPathRange()
 */
JNIEXPORT jfloatArray JNICALL Java_com_scriptographer_ai_PathText_getPathRange(JNIEnv *env, jobject obj) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		AIReal start, end;
		if (!sAITextFrame->GetOnPathTextTRange(art, &start, &end)) {
			// create a float array with these values:
			jfloatArray res = env->NewFloatArray(2);
			jfloat range[] = {
				start, end
			};
			env->SetFloatArrayRegion(res, 0, 2, range);
			return res;
		}
	} EXCEPTION_CONVERT(env)
	return NULL;
}

/*
 * void setPathRange(float start, float end)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_PathText_setPathRange(JNIEnv *env, jobject obj, jfloat start, jfloat end) {
	try {
	    AIArtHandle art = gEngine->getArtHandle(env, obj);
		sAITextFrame->SetOnPathTextTRange(art, start, end);
	} EXCEPTION_CONVERT(env)
}
