var image = new Image(scriptDir + "/folder.png");
var rollover = new Image(scriptDir + "/script.png");


var dlg = new FloatingDialog(FloatingDialog.OPTION_TABBED | FloatingDialog.OPTION_RESIZING);
dlg.init = function() {
	this.title = "Test";
	/*
	this.setBounds(new Rectangle(200, 200, 400, 300));
	var s = new TextEdit(this, TextEdit.OPTION_MULTILINE);
	s.value = 10;
	s.justify = s.JUSTIFY_CENTER;
	s.units = s.UNITS_CENTIMETER;
	*/
	/*
	var s = new SpinEdit(this, SpinEdit.OPTION_POPUP);
	s.setRange(0, 10);
	s.value = 5;
	s.style = SpinEdit.STYLE_VERTICAL;
	var popup = s.getPopupList();
	popup.createEntry("7.5 pt");
	*/
	var l = new HierarchyList(this);
	var lineHeight = 15;
	l.setEntrySize(2000, lineHeight);
	l.setEntryTextRect(0, 0, 2000, lineHeight);
	t = l.add("test 1");
	t = l.add("test 2");
	for (var i in l) {
		print(l[i]);
	}
	
//	s.rolloverPicture = image;
//	s.setBounds(new Rectangle(10, 10, 200, 15));
	this.visible = true;
}

dlg.init();

/*
var sel = activeDocument.getSelectedArt();
print(sel.length);

var timer = new Timer(120, true);
timer.onExecute = function() {
	print(sel[0] + " " + sel[0].curves[0].point1);
}
timer.start();
*/
/*
var obj = activeDocument.createOval(new Rectangle(0, 0, 10, 10));

var size = 15;
for (var i = 0; i < 40; i++) {
	for (var j = 0; j < 40; j++) {
//		activeDocument.createOval(new Rectangle(i * size, j * size, 10, 10));
		var dot = obj.clone();
		var m = new Matrix();
		m.translate(i * size, j * size);
//		var m = Matrix.getTranslateInstance(i * size, j * size);
		dot.transform(m); 
		if (dot.bounds.x != i * size || dot.bounds.y != j * size) {
			print(m, i * size, j * size, dot.bounds);
			break;
		}
	}
}

obj.remove();
*/