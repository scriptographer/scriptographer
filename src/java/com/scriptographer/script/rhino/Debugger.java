/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id: ExtendedJavaClass.java 230 2007-01-16 20:36:33Z lehni $
 */

package com.scriptographer.script.rhino;

import org.mozilla.javascript.tools.debugger.SwingGui;
import org.mozilla.javascript.tools.debugger.Dim;

import com.scriptographer.ScriptographerEngine;
import com.scriptographer.ai.Application;

import javax.swing.tree.*;
import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import java.util.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.KeyAdapter;
import java.awt.*;
import java.io.File;

/**
 * @author lehni
 */
public class Debugger extends Dim implements
		TreeSelectionListener {

	DebugGui gui;
	JTree tree;
	JList list;
	DebuggerTreeNode treeRoot;
	DefaultTreeModel treeModel;
	HashMap treeNodes = new HashMap();
	HashMap scriptNames = new HashMap();

	public Debugger() {
		gui = new DebugGui(this, "Debugger");
	}

	void createTreeNode(String sourceName, Dim.SourceInfo sourceInfo) {
		File file = new File(sourceName);
		File baseDir = ScriptographerEngine.getScriptDirectory();
		ArrayList path = new ArrayList();
		do {
			path.add(file.getName());
			file = file.getParentFile();
		} while (file != null && !file.equals(baseDir));
		DebuggerTreeNode node = treeRoot;
		DebuggerTreeNode newNode = null;
		for (int i = path.size() - 1; i >= 0; i--) {
			String name = (String) path.get(i);
			DebuggerTreeNode n = node.get(name);
			if (n == null) {
				n = new DebuggerTreeNode(name);
				node.add(n);
				if (newNode == null)
					newNode = n;
			}
			node = n;
		}
		treeNodes.put(sourceName, node);
		scriptNames.put(node, sourceName);
		MutableTreeNode parent = (MutableTreeNode) node.getParent();
		if (parent == treeRoot && treeRoot.getChildCount() == 1) {
			tree.makeVisible(new TreePath(new Object[] { parent, node }));
		}
		treeModel.insertNodeInto(node, parent, parent.getIndex(node));
	}

	void openScript(TreePath path) {
		if (path == null)
			return;
		Object node = path.getLastPathComponent();
		if (node == null)
			return;
		String sourceName = (String) scriptNames.get(node);
		if (sourceName == null)
			return;
		gui.showFileWindow(sourceName, -1);
	}

	void openFunction(FunctionItem function) {
		if (function == null)
			return;
		FunctionSource src = function.src;
		if (src != null) {
			SourceInfo si = src.sourceInfo();
			String url = si.url();
			int lineNumber = src.firstLine();
			gui.showFileWindow(url, lineNumber);
		}
	}

	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
			.getLastSelectedPathComponent();

		if (node == null)
			return;

		Object script = scriptNames.get(node);
		if (script != null) {
			// openScript(script);
		}
	}

	public void setVisible(boolean visible) {
		if (!gui.isVisible()) {
			gui.pack();
			gui.setLocation(100, 100);
		}
		gui.setVisible(visible);
	}

	class DebuggerTreeNode extends DefaultMutableTreeNode {

		public DebuggerTreeNode(Object obj) {
			super(obj);
		}

		public DebuggerTreeNode get(String name) {
			Enumeration children = this.children();
			while (children.hasMoreElements()) {
				DebuggerTreeNode node = (DebuggerTreeNode) children
					.nextElement();
				if (node != null && name.equals(node.getUserObject()))
					return node;
			}
			return null;
		}
	}

	class NodeInserter implements Runnable {
		MutableTreeNode node;

		NodeInserter(MutableTreeNode node) {
			this.node = node;
		}

		public void run() {
		}
	}

	class DebugGui extends SwingGui {

		String currentSourceUrl;
		
		public boolean isGuiEventThread() {
			return true;
		}

		public void dispatchNextGuiEvent() throws InterruptedException {
			Application.dispatchNextEvent();
		}

		public DebugGui(Dim dim, String title) {
			super(dim, title);
			Container contentPane = getContentPane();
			Component main = contentPane.getComponent(1);
			contentPane.remove(main);

			treeRoot = new DebuggerTreeNode("Files");
			tree = new JTree(treeRoot);
			treeModel = new DefaultTreeModel(treeRoot);
			tree.setModel(treeModel);
			tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.addTreeSelectionListener(Debugger.this);
			// tree.setRootVisible(false);
			// track double clicks
			tree.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent evt) {
					openScript(tree.getSelectionPath());
				}
			});
			// track enter key
			tree.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent evt) {
					if (evt.getKeyCode() == KeyEvent.VK_ENTER)
						openScript(tree.getSelectionPath());
				}
			});
			JScrollPane treeScroller = new JScrollPane(tree);
			treeScroller.setPreferredSize(new Dimension(180, 300));

			list = new JList();
			// no bold font lists for me, thanks
			list.setFont(list.getFont().deriveFont(Font.PLAIN));
			list.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent evt) {
					openFunction((FunctionItem) list.getSelectedValue());
				}
			});
			list.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent evt) {
					if (evt.getKeyCode() == KeyEvent.VK_ENTER)
						openFunction((FunctionItem) list.getSelectedValue());
				}
			});
			JScrollPane listScroller = new JScrollPane(list);
			listScroller.setPreferredSize(new Dimension(180, 200));

			JSplitPane split1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			split1.setTopComponent(treeScroller);
			split1.setBottomComponent(listScroller);
			split1.setOneTouchExpandable(true);
			split1.setResizeWeight(0.66);

			JSplitPane split2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			split2.setLeftComponent(split1);
			split2.setRightComponent(main);
			split2.setOneTouchExpandable(true);
			contentPane.add(split2, BorderLayout.CENTER);
		}

		public void updateSourceText(final Dim.SourceInfo sourceInfo) {
			// super.updateSourceText(sourceInfo);
			String filename = sourceInfo.url();
			if (!treeNodes.containsKey(filename)) {
				createTreeNode(filename, sourceInfo);
			}
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateFileWindow(sourceInfo);
				}
			});
		}

		protected void showFileWindow(String sourceName, int lineNumber) {
			if (!isVisible())
				setVisible(true);
			if (!sourceName.equals(currentSourceUrl)) {
				updateFunctionList(sourceName);
				DebuggerTreeNode node = (DebuggerTreeNode) treeNodes
					.get(sourceName);
				if (node != null) {
					TreePath path = new TreePath(node.getPath());
					tree.setSelectionPath(path);
					tree.scrollPathToVisible(path);
				}
			}
			super.showFileWindow(sourceName, lineNumber);
		}

		private void updateFunctionList(String sourceName) {
			// display functions for opened script file
			currentSourceUrl = sourceName;
			Vector functions = new Vector();
			SourceInfo si = sourceInfo(sourceName);
			String[] lines = si.source().split("\\r\\n|\\r|\\n");
			int length = si.functionSourcesTop();
			for (int i = 0; i < length; i++) {
				FunctionSource src = si.functionSource(i);
				if (sourceName.equals(src.sourceInfo().url())) {
					functions.add(new FunctionItem(src, lines));
				}
			}
			// Collections.sort(functions);
			list.setListData(functions);
		}
	}

	class FunctionItem implements Comparable {

		FunctionSource src;

		String name;

		String line = "";

		FunctionItem(FunctionSource src, String[] lines) {
			this.src = src;
			name = src.name();
			if ("".equals(name)) {
				try {
					line = lines[src.firstLine() - 1];
					int f = line.indexOf("function") - 1;
					StringBuffer b = new StringBuffer();
					boolean assignment = false;
					while (f-- > 0) {
						char c = line.charAt(f);
						if (c == ':' || c == '=')
							assignment = true;
						else if (assignment
							&& Character.isJavaIdentifierPart(c) || c == '$'
							|| c == '.')
							b.append(c);
						else if (!Character.isWhitespace(c) || b.length() > 0)
							break;
					}
					name = b.length() > 0 ? b.reverse().toString()
						: "<anonymous>";
				} catch (Exception x) {
					// fall back to default name
					name = "<anonymous>";
				}
			}
		}

		public int compareTo(Object o) {
			FunctionItem other = (FunctionItem) o;
			return name.compareTo(other.name);
		}

		public String toString() {
			return name;
		}

	}
}
