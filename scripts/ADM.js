// var engine = Packages.com.scriptographer.ScriptographerEngine.getEngine();

var dlg = new Dialog(Dialog.STYLE_TABBED_RESIZING_FLOATING, "This is just a test", new Rectangle(100, 100, 400, 600), Dialog.OPTION_TABBED_DIALOG_SHOWS_CYCLE, function() {
	/*
	var hor = new Item(this, Item.TYPE_SCROLLBAR, new Rectangle(21, 10, 200, 10));

	var ver = new Item(this, Item.TYPE_SCROLLBAR, new Rectangle(10, 10, 10, 100));
	ver.increments = [5, 10];
	ver.range = [0, 100];
	ver.value = 10;
	ver.onChange = function() {
		print(this.value);
	}
		
	var pic = new Item(this, Item.TYPE_PICTURE_PUSHBUTTON, new Rectangle(100, 100, 300, 300));
	pic.picture = "/test.gif";
	pic.onChange = function() {
		print('hi');
	}
	*/
	
//	var img = new Item(this, Item.TYPE_PICTURE_STATIC, new Rectangle(400, 100, 300, 300));
//	img.picture = "/test.jpg";
	
	var progress = new ProgressBar(this, new Rectangle(0, 0, 300, 20));
	var scroller = new ScrollBar(this, new Rectangle(0, 0, 300, 20));
	scroller.valueRange = progress.valueRange = [0, 100];
	scroller.value = progress.value = 0;
	scroller.increments = [5, 10];
	scroller.onChange = function() {
		progress.value = scroller.value;
	}

	var arrows = null;
	/*
	// 	ChasingArrows only work on Mac, an exception is thrown on Windows
	try {
		arrows = new ChasingArrows(this, new Rectangle(0, 0, 32, 32));
	} catch (e) {
	}
	*/
	
	var btn = new PushButton(this, new Rectangle(0, 0, 100, 15), "destroy");
	btn.onChange = function() {
		this.dialog.destroy();
	}
	
	var frame = new Frame(this, new Rectangle(0, 0, 10, 10));

	var lst = new HierarchyListBox(this, new Rectangle(0, 0, 300, 300));
	var fileImage = new Image("/small.gif");
	var dirImage = new Image("/small.gif");
//	lst.style = 16;
	var list = this.fileList = lst.list;
	list.bgColor = Drawer.COLOR_BLACK;
	list.entrySize = [300, 16];
	list.entryTextRect = [0, 0, 300, 16];
	
	list.onTrack = function(tracker, entry) {
		if (tracker.action == Tracker.ACTION_BUTTON_UP && (tracker.modifiers & Tracker.MODIFIER_DOUBLE_CLICK)) {
			if (entry.isDirectory) {
				entry.expanded = !entry.expanded;
				entry.list.invalidate();
			}
			execute(entry.file);
		}
	}
	
	function addFiles(list, dir, filter) {
		var files = dir.list(filter);
		for (var i in files) {
			var entry = list.createEntry();
			var file = new java.io.File(dir, files[i]);
			entry.text = files[i];
			entry.file = file;
			entry.isDirectory = file.isDirectory();
			if (entry.isDirectory) {
				var newList = addFiles(entry.createChildList(), file, filter);
				entry.expanded = false;
				entry.picture = fileImage;
			} else {
				entry.picture = dirImage;
			}
		}
		return list;
	}
	
	var dir = new java.io.File("/Users/Lehni/Development/C & C++/Scriptographer/scripts");
	
	// filter for hiding files:
	var filter = new java.io.FilenameFilter() {
		accept: function(dir, name) {
			return !new java.lang.String(name).startsWith(".");
		}
	};

	addFiles(list, dir, filter);
	
	// layout:

	this.setLayout(new java.awt.BorderLayout());

	var test = new ItemContainer(new java.awt.BorderLayout());
	test.add(btn, java.awt.BorderLayout.CENTER);
	if (arrows)
		test.add(arrows, java.awt.BorderLayout.EAST);
	this.addToLayout(test, java.awt.BorderLayout.SOUTH);
	
	this.addToLayout(lst, java.awt.BorderLayout.CENTER);

	var test = new ItemContainer(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
	test.frame = frame;
	test.setInsets(2, 2);
	frame.style = Frame.STYLE_SUNKEN;
	test.add(progress);
	test.add(scroller);
	this.addToLayout(test, java.awt.BorderLayout.NORTH);
});

dlg.onDraw = function(drawer) {
	drawer.drawLine(0, 0, 100, 100);
}

// dlg.invalidate();

dlg.onCollapse = function() {
	print('collapse');
}

dlg.onTrack = function(tracker) {
//	print(tracker.point + " " + tracker.time);
}

dlg.onClose = function() {
	this.destroy();
}

dlg.onResize = function(dx, dy) {
//	print(dx, dy);
}

print(dlg.size);