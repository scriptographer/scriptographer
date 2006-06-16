// raster.js is a base script for all the raster scripts
// that take a selection of a raster and a path object
// as a starting point for some raster processing
// the only thing that has to be defined is the drawDot 
// function
var raster = null;
var dots = [];
var sel = null;

function getCompoundArea(obj, area) {
	if (area == null) area = 0;
	if (obj instanceof Path) return area + obj.getArea();
	else if (obj instanceof CompoundPath || obj instanceof Group) {
		var child = obj.firstChild;
		while (child != null) {
			area = getCompoundArea(child, area);
			child = child.nextSibling;
		}
	}
	return area;
}

function initRaster() {
	sel = activeDocument.getSelectedArt();
	for (var i = 0; i < sel.length; i++) {
		obj = sel[i];
		if (raster == null && obj instanceof Raster) raster = obj;
		else {
			if (!obj.parent.selected) {
				if (dots.length > 0) {
					var center1 = obj.bounds.center;
					var center2 = dots[0].bounds.center;
					var m = new Matrix();
					m.scale(Math.sqrt(Math.abs(getCompoundArea(dots[0])) / Math.abs(getCompoundArea(obj))));
					m.translate(center2.subtract(center1));
					obj.transform(m);
				}
				dots.push(obj);
			}
		}
	}
	var ok = (raster != null && dots.length > 0);
	if (ok) {
		for (var i in dots) {
			var dot = dots[i].clone();
			var m = Matrix.getTranslateInstance(dot.bounds.center.multiply(-1));
			dot.transform(m);
			dots[i] = dot;
		}
	}
	return ok;
}

function executeRaster(createDot) {
	var t = new Date().getTime();
	var group = new Group();
	for (var x = 0; x < raster.width; x++) {
		for (var y = 0; y < raster.height; y++) {
			var radius = raster.getPixel(x, y).convert(Color.TYPE_GRAY).gray;
			var dot = dots[Math.round(Math.random() * (dots.length - 1))];
			var obj = createDot(x, raster.height - y, dot, radius);
			if (obj) group.appendChild(obj);
		}
	}
	for (var i in dots) {
		dots[i].remove();
	}
	for (var i = 0; i < sel.length; i++) {
		sel[i].selected = false;
	}
	print(new Date().getTime() - t);
	return group;
}