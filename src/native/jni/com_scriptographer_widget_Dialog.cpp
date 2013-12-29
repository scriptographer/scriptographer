#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "ScriptographerPlugin.h"
#include "com_scriptographer_widget_Dialog.h"
#include <Uxtheme.h>

/*
 * com.scriptographer.widget.Dialog
 */

/*
 * int getParentWindow(long arg1)
 */


/*
 * int nativeCreate(java.lang.String arg1, int arg2, int arg3)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_widget_Dialog_nativeCreate(
  JNIEnv *env, jobject obj, jstring name, jint style, jint options)
{

	/*try {
		char *str = gEngine->convertString(env, name);
		DEFINE_CALLBACK_PROC(Dialog_onInit);
		ADMDialogRef dialog = sADMDialog->Create(gPlugin->getPluginRef(), str,
				kEmptyDialogID, (ADMDialogStyle) style,
				(ADMDialogInitProc) CALLBACK_PROC(Dialog_onInit),
				env->NewGlobalRef(obj), options);
		delete str;
		if (dialog == NULL)
			throw new StringException("Unable to create dialog.");
		return (jint) dialog;
	} EXCEPTION_CONVERT(env);
	return 0;*/

	ASErr error ; // sAIMenu->AddMenuItemZString(gPlugin->getPluginRef(), "123Third Party Panel", kOtherPalettesMenuGroup, ZREF("123Third Party Panel"),
					//					kMenuItemNoOptions, &fEmptyPanelPanelMenuItemHandle);

	try {

		AIPanelRef fPanel;

		//AIPanelFlyoutMenuRef fPanelFlyoutMenu;

		AIMenuItemHandle fEmptyPanelPanelMenuItemHandle;
		//todo:
		AISize pnSize = {240, 320};
	
			char *str = gEngine->convertString(env, name);
				/** Creates a new panel.
	@param inPluginRef		The plug-in that is creating the panel.
	@param inID				A unique identifier for the new panel.
	@param inTitle			The title of the panel.
	@param inStateCount 	The number of host layouts for the panel; must be at least 1.
	@param inMinSize		Minimum size of the panel.
	@param isResizable		True to make the panel resizable.
	@param inFlyoutMenu		The flyout menu for the panel, or NULL to hide the flyout icon.
	@param inUserData		Developer-defined data to be associated with the panel.
	@param outPanel			[out] A buffer in which to return the new panel object.
 	*/

			error = sAIPanel->Create(gPlugin->getPluginRef(), 
				ai::UnicodeString(str), 
				ai::UnicodeString(str), 3,
				pnSize,
				true, 
				NULL, //fPanelFlyoutMenu, 
				
				env->NewGlobalRef(obj), //user data
				
				fPanel);
	
		delete str;

	if (error)
			throw new StringException("Unable to create dialog.");
	/*AISize minSize = {50, 50};
	AISize maxSize = {800, 800};
	AISize prefConstSize = {100, 100};
	AISize prefUnconstSize = {600, 600};

	error = sAIPanel->SetSizes(fPanel, minSize, prefUnconstSize, prefConstSize, maxSize);
	*/
	error = sAIPanel->Show(fPanel, false);

	//AIPanelPlatformWindow hDlg = NULL;
	//error = sAIPanel->GetPlatformWindow(fPanel, hDlg);

	//if (error)
	//	return error;

	return (jint) fPanel;
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void nativeDestroy(int arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_widget_Dialog_nativeDestroy(
		JNIEnv *env, jobject obj, jint arg1) {
	try {
		// TODO: define nativeDestroy
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean nativeIsVisible()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_widget_Dialog_nativeIsVisible(
  JNIEnv * env, jobject obj)
{
try {
		AIPanelRef fPanel = gEngine->getAIPanelRef(env, obj);
		
		AIPanelPlatformWindow hDlg = NULL;
		AIBoolean bVis;
		AIErr error = sAIPanel->IsShown (fPanel, bVis);
		if (error)
			return false;
		return bVis;

	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void nativeSetVisible(boolean arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_widget_Dialog_nativeSetVisible(
  JNIEnv * env, jobject obj, jboolean isVisible)
{
	try {
		AIPanelRef fPanel = gEngine->getAIPanelRef(env, obj);
		
		AIPanelPlatformWindow hDlg = NULL;
		AIBoolean bVis;
		AIErr error = sAIPanel->Show(fPanel, isVisible);
	} EXCEPTION_CONVERT(env);
	
}

/*
 * void nativeSetActive(boolean arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_widget_Dialog_nativeSetActive(
		JNIEnv *env, jobject obj, jboolean arg1) {
	try {
		// TODO: define nativeSetActive
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ui.Size nativeGetSize()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_widget_Dialog_nativeGetSize(
  JNIEnv *env, jobject obj) {
	
	  try {
		AIPanelRef fPanel = gEngine->getAIPanelRef(env, obj);
		
		AIPanelPlatformWindow hDlg = NULL;
		AISize size;
		AIErr error = sAIPanel->GetSize(fPanel, size);
		return gEngine->convertSize(env, &size);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void nativeSetSize(int arg1, int arg2)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_widget_Dialog_nativeSetSize(
		JNIEnv *env, jobject obj, jint arg1, jint arg2) {
	try {
		// TODO: define nativeSetSize
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ui.Rectangle nativeGetBounds()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_widget_Dialog_nativeGetBounds(
  JNIEnv * env, jobject obj)
{
	try {
		AIPanelRef fPanel = gEngine->getAIPanelRef(env, obj);
		
		AIPanelPlatformWindow hDlg = NULL;
		AIErr error = sAIPanel->GetPlatformWindow(fPanel, hDlg);
		//win:
#ifdef WIN_ENV
			RECT rc;
			if (!GetWindowRect(hDlg, &rc))
				throw new StringException("Error calling GetWindowRect.");
			return gEngine->convertRectangle(env, &rc);
#endif
		//todo MAC

	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void nativeSetBounds(int arg1, int arg2, int arg3, int arg4)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_widget_Dialog_nativeSetBounds(
		JNIEnv *env, jobject obj, jint arg1, jint arg2, jint arg3, jint arg4) {
	try {
		// TODO: define nativeSetBounds
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ui.Point getPosition()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_widget_Dialog_getPosition(
		JNIEnv *env, jobject obj) {
	try {
		// TODO: define getPosition
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setPosition(int arg1, int arg2)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_widget_Dialog_setPosition(
		JNIEnv *env, jobject obj, jint arg1, jint arg2) {
	try {
		// TODO: define setPosition
	} EXCEPTION_CONVERT(env);
}

/*
 * void invalidate()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_widget_Dialog_invalidate__(
		JNIEnv *env, jobject obj) {
	try {
		// TODO: define invalidate
	} EXCEPTION_CONVERT(env);
}

/*
 * void invalidate(int arg1, int arg2, int arg3, int arg4)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_widget_Dialog_invalidate__IIII(
		JNIEnv *env, jobject obj, jint arg1, jint arg2, jint arg3, jint arg4) {
	try {
		// TODO: define invalidate
	} EXCEPTION_CONVERT(env);
}

/*
 * void update()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_widget_Dialog_update(
		JNIEnv *env, jobject obj) {
	try {
		// TODO: define update
	} EXCEPTION_CONVERT(env);
}

/*
 * int nativeGetFont()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_widget_Dialog_nativeGetFont(
		JNIEnv *env, jobject obj) {
	try {
		// TODO: define nativeGetFont
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void nativeSetFont(int arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_widget_Dialog_nativeSetFont(
		JNIEnv *env, jobject obj, jint arg1) {
	try {
		// TODO: define nativeSetFont
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetName(java.lang.String arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_widget_Dialog_nativeSetName(
		JNIEnv *env, jobject obj, jstring arg1) {
	try {
		// TODO: define nativeSetName
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetMinimumSize(int arg1, int arg2)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_widget_Dialog_nativeSetMinimumSize(
		JNIEnv *env, jobject obj, jint arg1, jint arg2) {
	try {
		// TODO: define nativeSetMinimumSize
	} EXCEPTION_CONVERT(env);
}

/*
 * void nativeSetMaximumSize(int arg1, int arg2)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_widget_Dialog_nativeSetMaximumSize(
		JNIEnv *env, jobject obj, jint arg1, jint arg2) {
	try {
		// TODO: define nativeSetMaximumSize
	} EXCEPTION_CONVERT(env);
}

/*
 * com.scriptographer.ui.Size getIncrement()
 */
JNIEXPORT jobject JNICALL Java_com_scriptographer_widget_Dialog_getIncrement(
		JNIEnv *env, jobject obj) {
	try {
		// TODO: define getIncrement
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * void setIncrement(int arg1, int arg2)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_widget_Dialog_setIncrement(
		JNIEnv *env, jobject obj, jint arg1, jint arg2) {
	try {
		// TODO: define setIncrement
	} EXCEPTION_CONVERT(env);
}

/*
 * boolean isEnabled()
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_widget_Dialog_isEnabled(
		JNIEnv *env, jobject obj) {
	try {
		// TODO: define isEnabled
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void setEnabled(boolean arg1)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_widget_Dialog_setEnabled(
		JNIEnv *env, jobject obj, jboolean arg1) {
	try {
		// TODO: define setEnabled
	} EXCEPTION_CONVERT(env);
}
