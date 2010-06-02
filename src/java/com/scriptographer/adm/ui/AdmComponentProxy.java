/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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
 * File created on Jun 2, 2010.
 */

package com.scriptographer.adm.ui;

import java.util.LinkedHashMap;

import com.scratchdisk.script.ScriptEngine;
import com.scratchdisk.util.ConversionUtils;
import com.scriptographer.adm.Border;
import com.scriptographer.adm.Button;
import com.scriptographer.adm.CheckBox;
import com.scriptographer.adm.ColorButton;
import com.scriptographer.adm.Dialog;
import com.scriptographer.adm.FontPopupList;
import com.scriptographer.adm.FontPopupListOption;
import com.scriptographer.adm.Frame;
import com.scriptographer.adm.FrameStyle;
import com.scriptographer.adm.Item;
import com.scriptographer.adm.ItemGroup;
import com.scriptographer.adm.ListEntry;
import com.scriptographer.adm.PopupList;
import com.scriptographer.adm.PopupMenu;
import com.scriptographer.adm.Size;
import com.scriptographer.adm.Slider;
import com.scriptographer.adm.SpinEdit;
import com.scriptographer.adm.TextEdit;
import com.scriptographer.adm.TextEditItem;
import com.scriptographer.adm.TextOption;
import com.scriptographer.adm.TextPane;
import com.scriptographer.adm.TextValueItem;
import com.scriptographer.adm.ToggleItem;
import com.scriptographer.adm.ValueItem;
import com.scriptographer.adm.layout.TableLayout;
import com.scriptographer.ai.Color;
import com.scriptographer.ai.FontWeight;
import com.scriptographer.ai.RGBColor;
import com.scriptographer.ui.Component;
import com.scriptographer.ui.ComponentProxy;
import com.scriptographer.ui.ComponentType;
import com.scriptographer.ui.TextUnits;

/**
 * @author lehni
 *
 */
public class AdmComponentProxy extends ComponentProxy {
	// Used for scaling slider values
	private double factor = 1;

	private Integer selectedIndex;

	// Native items / entries
	private Item item;
	private ListEntry entry;

	public AdmComponentProxy(Component component) {
		super(component);
		// Set scaling factor for Slider to allow fractional digits
		if (component.getType() == ComponentType.SLIDER)
			factor = 1000;
	}

	protected Item createItem(Dialog dialog) {
		// Item:
		item = null;
		ComponentType type = component.getType();
		switch (type) {
		case NUMBER: {
			Boolean steppers = component.getSteppers();
			if (steppers != null && steppers) {
				item = new SpinEdit(dialog) {
					protected void onChange() {
						AdmComponentProxy.this.onChange(true);
					}
				};
			} else {
				TextEditItem textItem = new TextEdit(dialog) {
					protected void onChange() {
						AdmComponentProxy.this.onChange(true);
					}
				};
				textItem.setAllowMath(true);
				textItem.setAllowUnits(true);
				item = textItem;
			}
		}
		break;
		case STRING: {
			Boolean multiline = component.getMultiline();
			TextOption[] options = multiline != null && multiline
					? new TextOption[] { TextOption.MULTILINE }
					: null;
			item = new TextEdit(dialog, options) {
				protected void onChange() {
					AdmComponentProxy.this.onChange(true);
				}
			};
		}
		break;
		case TEXT: {
			item = new TextPane(dialog);
			// Space a bit more to the top, to compensate the descender space.
			item.setMargin(new Border(1, 0, 0, 0));
		}
		break;
		case RULER: {
			// If the ruler has a label, add it to the left of it, using an
			// ItemGroup and a TableLayout. The ItemGroup needs to be created
			// before the frame, otherwise layouting issues arise...
			// Ideally this should be resolved, but as ADM is on its way out,
			// just work around it for now.
			ItemGroup group;
			String label = component.getLabel();
			if (label != null && !label.equals("")) {
				group = new ItemGroup(dialog);
				TextPane labelItem = new TextPane(dialog);
				labelItem.setText(label);
				// Use 3 rows, so the center one with the ruler gets centered,
				// then span the label across all three.
				double[][] sizes = {
					new double[] { TableLayout.PREFERRED, TableLayout.FILL },
					new double[] { TableLayout.FILL, TableLayout.PREFERRED,
							TableLayout.FILL }
				};
				group.setLayout(new TableLayout(sizes));
				group.add(labelItem, "0, 0, 0, 2");
				group.setMarginTop(2);
			} else {
				group = null;
			}
			Frame frame = new Frame(dialog);
			frame.setStyle(FrameStyle.SUNKEN);
			// Margin needs to be set before changing size...
			// TODO: Fix this in UI package?
			int top = label != null ? 2 : 4, bottom = 4;
			frame.setMargin(top, 0, bottom, 0);
			// Margin is included inside size, not added. This is different
			// to how things works with CSS...
			// TODO: Fix this in UI package?
			frame.setHeight(2 + top + bottom);
			// Now finish setting up the layout group and label
			if (group != null) {
				group.add(frame, "1, 1, full, center");
				item = group;
			} else {
				item = frame;
			}
		}
		break;
		case SLIDER: {
			item = new Slider(dialog) {
				protected void onChange() {
					AdmComponentProxy.this.onChange(true);
				}
			};
		}
		break;
		case CHECKBOX: {
			item = new CheckBox(dialog) {
				protected void onClick() {
					AdmComponentProxy.this.onChange(true);
				}
			};
		}
		break;
		case LIST: {
			item = new PopupList(dialog, true) {
				protected void onChange() {
					selectedIndex = this.getSelectedEntry().getIndex();
					AdmComponentProxy.this.onChange(true);
				}
			};
		}
		break;
		case BUTTON: {
			item = new Button(dialog) {
				protected void onClick() {
					AdmComponentProxy.this.onClick();
				}
			};
		}
		break;
		case COLOR: {
			item = new ColorButton(dialog) {
				protected void onClick() {
					AdmComponentProxy.this.onChange(true);
				}
			};
		}
		break;
		case FONT: {
			item = new FontPopupList(dialog, new FontPopupListOption[] {
					FontPopupListOption.EDITABLE,
					FontPopupListOption.VERTICAL
			}) {
				protected void onChange() {
					AdmComponentProxy.this.onChange(true);
				}
			};
		}
		break;
		case MENU_ENTRY: 
		case MENU_SEPARATOR: {
			PopupMenu menu = dialog.getPopupMenu();
			if (menu != null) {
				entry = new ListEntry(menu) {
					protected void onSelect() {
						AdmComponentProxy.this.onSelect();
					}
				};
				if (type == ComponentType.MENU_SEPARATOR)
					entry.setSeparator(true);
			}
		}
		break;
		}
		initialize();
		return item;
	}

	protected int addToContent(Dialog dialog,
			LinkedHashMap<String, com.scriptographer.adm.Component> content,
			int column, int row) {
		Item valueItem = createItem(dialog);
		String label = component.getLabel();
		boolean isRuler = component.getType() == ComponentType.RULER;
		boolean hasLabel = !isRuler && label != null && !"".equals(label);
		if (hasLabel) {
			TextPane labelItem = new TextPane(dialog);
			labelItem.setText(label + ":");
			// Adjust top margin of label to reflect the native margin
			// in the value item.
			Item marginItem = valueItem;
			// If this is an item group, use the first item in it instead
			// This is only needed for FontPopupList so far.
			if (marginItem instanceof ItemGroup)
				marginItem = (Item) ((ItemGroup) marginItem).getContent().get(0);
			Border margin = marginItem.getVisualMargin();
			// Also take into account any margins the component might have set
			if (valueItem != marginItem)
				margin = margin.add(valueItem.getMargin());
			labelItem.setMargin(margin.top + 3, 4, 0, 0);
			content.put(column + ", " + row + ", right, top", labelItem);
		}
		boolean fullSize = component.getFullSize();
		String justification = isRuler || component.getFullSize() 
				? "full, center" : "left, center";
		content.put(isRuler || !hasLabel && fullSize
				? column + ", " + row  + ", " + (column + 1) + ", " + row + ", "
						+ justification
				: (column + 1) + ", " + row + ", " + justification,
				valueItem);
		return row + 1;
	}

	protected Size getSize() {
		if (item != null) {
			Size size;
			Boolean multiline = component.getMultiline();
			Integer columns = component.getColumns();
			Integer rows = component.getRows();
			if (multiline != null && multiline && columns != null
					&& rows != null) {
				// Base width on an average wide character, such as H
				size = item.getTextSize("H");
				size = new Size(
						size.width * columns + 8,
						size.height * rows + 8
				);
			} else {
				// Use preferred size instead of best size for ruler, as we want
				// to take into account items of which the size was already set.
				size = component.getType() == ComponentType.RULER
						? item.getPreferredSize()
						: item.getBestSize();
				Integer length = component.getLength();
				if (length != null)
					size.width = item.getTextSize("H").width * length;
			}
			Integer width = component.getWidth();
			if (width != null)
				size.width = width;
			Integer height = component.getHeight();
			if (height != null)
				size.height = height;
			return size;
		}
		return null;
	}

	public void updateSize() {
		if (item != null) {
			Size size = getSize();
			if (size != null && !size.equals(item.getSize())) {
				item.setSize(size);
				onSizeChanged();
			}
		}
	}

	public Object getValue() {
		if (item == null) {
			return component.getDefaultValue();
		} else {
			switch (component.getType()) {
			case STRING:
			case TEXT:
				return ((TextValueItem) item).getText();
			case BUTTON:
				return ((Button) item).getText();
			case NUMBER:
				return ((ValueItem) item).getValue();
			case SLIDER:
				double value = (double) ((ValueItem) item).getValue() / factor;
				Double inc = component.getIncrement();
				if (inc != null) {
					double pre = value;
					value = Math.round(value / inc) * inc;
					if (pre != value)
						((ValueItem) item).setValue((float) (value * factor));
				}
				return value;
			case CHECKBOX:
				return ((ToggleItem) item).isChecked();
			case LIST:
				ListEntry selected = ((PopupList) item).getSelectedEntry();
				if (selected != null)
					return component.getOption(selected.getIndex());
				break;
			case COLOR:
				return ((ColorButton) item).getColor();
			case FONT:
				return ((FontPopupList) item).getFontWeight();
			}
			return null;
		}
	}

	public boolean setValue(Object value) {
		if (item == null && entry == null)
			return false;
		boolean callOnChange = true;
		switch (component.getType()) {
		case STRING:
		case TEXT: {
			String text = ConversionUtils.toString(value);
			Integer maxLength = component.getMaxLength();
			if (maxLength != null && text != null
					&& text.length() > maxLength)
				text = text.substring(0, maxLength);
			((TextValueItem) item).setText(text);
			updateSize();
		}
		break;
		case BUTTON: {
			((Button) item).setText(ConversionUtils.toString(value));
			updateSize();
		}
		break;
		case NUMBER:
		case SLIDER: {
			((ValueItem) item).setValue(
					(float) (ConversionUtils.toDouble(value) * factor));
		}
		break;
		case CHECKBOX: {
			((CheckBox) item).setChecked(ConversionUtils.toBoolean(value));
		}
		break;
		case LIST: {
			PopupList list = (PopupList) item;
			ListEntry selected = null;
			int index = selectedIndex != null ? selectedIndex : 0;
			for (int i = 0, l = list.size(); i < l && selected == null; i++) {
				Object option = component.getOption(index);
				if (ConversionUtils.equals(value, option))
					index = i;
			}
			setSelectedIndex(index, false);
			// No need to call onChange, as setSelectionIndex already does:
			callOnChange = false;
		}
		break;
		case COLOR: {
			Color color = ScriptEngine.convertToJava(value, Color.class);
			if (color == null)
				color = new RGBColor(0, 0, 0);
			((ColorButton) item).setColor(color);
		}
		break;
		case FONT: {
			FontWeight weight = ScriptEngine.convertToJava(value,
					FontWeight.class);
			if (weight != null)
				((FontPopupList) item).setFontWeight(weight);
		}
		break;
		case MENU_ENTRY: {
			entry.setText(ConversionUtils.toString(value));
		}
		break;
		}
		// Update palette's value object too
		if (callOnChange)
			onChange(false);
		return true;
	}

	public Integer getSelectedIndex() {
		return selectedIndex;
	}

	public boolean setSelectedIndex(Integer index, boolean callback) {
		selectedIndex = index;
		if (item == null)
			return false;
		// TODO: Handle index == null ?
		PopupList list = (PopupList) item;
		list.setSelectedEntry(list.get(index));
		return true;
	}

	public boolean setRange(Double min, Double max) {
		if (item == null)
			return false;
		((ValueItem) item).setRange(
				(float) (min != null ? min * factor : Integer.MIN_VALUE), 
				(float) (max != null ? max * factor : Integer.MAX_VALUE));
		return true;
	}

	public void setIncrement(double increment) {
		if (item != null)
			((ValueItem) item).setIncrements((float) (increment * factor));
	}

	public void setFractionDigits(Integer fractionDigits) {
		if (item != null)
			((TextEditItem) item).setFractionDigits(fractionDigits);
	}

	public void setUnits(TextUnits units) {
		if (item != null) {
			TextEditItem textItem = (TextEditItem) item;
			textItem.setUnits(units);
			textItem.setShowUnits(units != TextUnits.NONE);
		}
	}

	public void setSteppers(Boolean steppers) {
		// No support to change this at runtime for now
	}

	public void setVisible(boolean visible) {
		if (item != null)
			item.setVisible(visible);
	}

	public void setEnabled(boolean enabled) {
		if (item != null)
			item.setEnabled(enabled);
	}

	public void setMaxLength(Integer maxLength) {
		if (item != null)
			((TextEditItem) item).setMaxLength(maxLength != null
					? maxLength : -1);
	}

	public void setOptions(Object[] options, Object current) {
		if (item == null)
			return;
		PopupList list = (PopupList) item;
		list.removeAll();
		if (options != null && options.length > 0) {
			int index = selectedIndex != null ? selectedIndex : 0;
			for (int i = 0; i < options.length; i++) {
				Object option = options[i];
				if (option.equals(current))
					index = i;
				ListEntry entry = null;
				if (option instanceof ListEntry) {
					entry = (ListEntry) option;
					entry = list.add(entry);
				} else {
					entry = new ListEntry(list);
					// Simplify names for art items
					String name = option instanceof com.scriptographer.ai.Item
							? ((com.scriptographer.ai.Item) option).getName()
							: null;
					entry.setText(name != null ? name : option.toString());
				}
			}
			if (index < 0)
				index = 0;
			else if (index >= options.length)
				index = options.length - 1;
			// We're changing options, not value, so cause onChange
			// callback for value
			setSelectedIndex(index, true);
			updateSize();
		}
	}
}
