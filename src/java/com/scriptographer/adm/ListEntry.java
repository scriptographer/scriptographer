package com.scriptographer.adm;

import java.io.IOException;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.mozilla.javascript.ScriptRuntime;

import com.scriptographer.js.FunctionHelper;

public class ListEntry extends NotificationHandler {
	private String text;

	private Image picture;
	private Image selectedPicture;
	private Image disabledPicture;

	protected ListItem list;
	
	public ListEntry(ListItem list, int index) {
		if (this instanceof HierarchyListEntry) {
			if (!(list instanceof HierarchyList))
				throw new RuntimeException("HierarchListEntry is required for HierarchyList");
		} else {
			if (list instanceof HierarchyList)
				throw new RuntimeException("HierarchListEntry is only allowed for HierarchyList");
		}
		handle = nativeCreate(list, index, list.getUniqueId());
		if (handle == 0)
			throw new RuntimeException("Cannot create list entry");
		this.list = list;
	}
	
	public ListEntry(ListItem list) {
		this(list, -1); // -1 means insert at the end
	}
	
	public boolean remove() {
		if (handle > 0) {
			nativeDestroy();
			handle = 0;
			return true;
		}
		return false;
	}
	
	/*
	 * Callback functions
	 */

	protected void onDraw(Drawer drawer) throws Exception {
		list.callFunction(list.onDrawEntry, drawer, this);
	}

	protected boolean onTrack(Tracker tracker) throws Exception {
		Object result = list.callFunction(list.onTrackEntry, tracker, this);
		if (result != null)
			return ScriptRuntime.toBoolean(result);
		return true;
	}

	protected void onDestroy() throws Exception {
		if (wrapper != null)
			FunctionHelper.callFunction(wrapper, "onDestroy");
		list.callFunction("onDestroyEntry", this);
	}

	protected void onClick() throws Exception {
		if (wrapper != null)
			FunctionHelper.callFunction(wrapper, "onClick");
		list.callFunction("onClickEntry", this);
	}
	
	protected void onChangeText() throws Exception {
		if (wrapper != null)
			FunctionHelper.callFunction(wrapper, "onChangeText");
		list.callFunction("onChangeEntryText", this);
	}
	
	protected void onNotify(int notifier) throws Exception {
		switch (notifier) {
			case Notifier.NOTIFIER_DESTROY:
				onDestroy();
			break;
			case Notifier.NOTIFIER_USER_CHANGED:
				onClick();
				break;
			case Notifier.NOTIFIER_ENTRY_TEXT_CHANGED:
				onChangeText();
				break;
		}
	}

	/*
	 * entry creation/destruction
	 * 
	 */

	private native int nativeCreate(ListItem list, int index, int id);
	private native void nativeDestroy();


	/* 
	 * contaer accessors
	 * 
	 */
	
	public native int getIndex();
	
	public ListItem getList() {
		return list;
	}

	/* 
	 * entry ID
	 * 
	 */
	/*
	public native void setID(int entryID);
	public native int getID();
	*/
	
	/* 
	 * entry selection segmentFlags
	 * 
	 */

	public native void setSelected(boolean select);
	public native boolean isSelected();

	/* 
	 * entry visibility
	 * 
	 */
	
	public native void makeInBounds();
	public native boolean isInBounds();

	/* 
	 * entry state accessors
	 * 
	 */

	public native void setEnabled(boolean enable);
	public native boolean isEnabled();
	
	public native void setActive(boolean activate);
	public native boolean isActive();
	
	public native void setChecked(boolean check);
	public native boolean isChecked();
	
	public native void setSeparator(boolean separator);
	public native boolean isSeparator();
/* 
	 * entry bounds accessors
	 * 
	 */

	public native Rectangle getLocalRect();
	public native Rectangle getBounds();
/* 
	 * coordate transformations
	 * 
	 */

	public native Point localToScreen(int x, int y);
	public native Point screenToLocal(int x, int y);

	public native Rectangle localToScreen(int x, int y, int width, int height);
	public native Rectangle screenToLocal(int x, int y, int width, int height);

	public Point localToScreen(Point2D pt) {
		return localToScreen((int) pt.getX(), (int) pt.getY());
	}

	public Point screenToLocal(Point2D pt) {
		return screenToLocal((int) pt.getX(), (int) pt.getY());
	}

	public Rectangle localToScreen(Rectangle2D rt) {
		return localToScreen((int) rt.getX(), (int) rt.getY(), (int) rt.getWidth(), (int) rt.getHeight());
	}

	public Rectangle screenToLocal(Rectangle2D rt) {
		return screenToLocal((int) rt.getX(), (int) rt.getY(), (int) rt.getWidth(), (int) rt.getHeight());
	}

	/* 
	 * redraw requests
	 * 
	 */
		
	public native void invalidate();
	public native void invalidate(int x, int y, int width, int height);
	public native void update();

	public void invalidate(Rectangle2D rt) {
		invalidate((int) rt.getX(), (int) rt.getY(), (int) rt.getWidth(), (int) rt.getHeight());
	}

	/* 
	 * entry picture accessors
	 * 
	 */

	private native void nativeSetPicture(int iconHandle);
	private native void nativeSetSelectedPicture(int iconHandle);
	private native void nativeSetDisabledPicture(int iconHandle);

	public Image getPicture() {
		return picture;
	}
		
	public void setPicture(Object obj) throws IOException {
		picture = Image.getImage(obj);
		nativeSetPicture(picture != null ? picture.createIconHandle() : 0);
	}
	
	public Image getSelectedPicture() {
		return selectedPicture;
	}
	
	public void setSelectedPicture(Object obj) throws IOException {
		selectedPicture = Image.getImage(obj);
		nativeSetSelectedPicture(selectedPicture != null ? selectedPicture.createIconHandle() : 0);
	}

	public Image getDisabledPicture() {
		return disabledPicture;
	}

	public void setDisabledPicture(Object obj) throws IOException {
		disabledPicture = Image.getImage(obj);
		nativeSetDisabledPicture(disabledPicture != null ? disabledPicture.createIconHandle() : 0);
	}

	/* 
	 * entry text accessors
	 * 
	 */

	private native void nativeSetText(String text);
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
		nativeSetText(text);
	}
	
	/*
	 *  entry timer accessors
	 *
	 */

	/*
	public native ADMTimerRef createTimer(ADMUt32 milliseconds,
				ADMActionMask abortMask, ADMEntryTimerProc timerProc,
				ADMEntryTimerAbortProc timerAbortProc, ADMt32 options);

	public native void abortTimer(ADMTimerRef timer);
	*/

	/* 
	 * entry checkmark accessors
	 * 
	 */

	/*
	public native void setCheckGlyph(ADMStandardCheckGlyphID checkGlyph);
	public native ADMStandardCheckGlyphID getCheckGlyph();
	*/

}
