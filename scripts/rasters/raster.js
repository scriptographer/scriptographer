// raster.js is a base script for all the raster scripts
// that take a selection of a raster and a path object
// as a starting point for some raster processing
// the only thing that has to be defined is the drawDot 
// function
var raster = null;
var dot = null;
var sel = null;

function initRaster() {
	sel = activeDocument.getSelectedItems();
	for (var i = 0; i < sel.length; i++) {
		obj = sel[i];
		if (raster == null && obj instanceof Raster) raster = obj;
		else if (dot == null) dot = obj;
		if (raster != null && dot != null) break;
	}
	return (raster != null && dot != null);
}

function executeRaster(createDot) {
	activeDocument.deselectAll();
	var group = new Group();
	// create a copy of the dot that is moved to the origin so 
	// rasters that scale the dot are simple to realize:
	dot = dot.clone();
	var move = dot.bounds.center;
	dot.translate(move.multiply(-1));
//	var img = raster.getImage();
	for (var y = 0; y < raster.height; y++) {
		for (var x = 0; x < raster.width; x++) {
//			var c = new java.awt.Color(img.getRGB(x, y));
//			var col = 1 - (0.3 * c.red + 0.59  * c.green + 0.11 * c.blue) / 255;
			var radius = raster.getPixel(x, y).convert(Color.TYPE_GRAY).gray;
			var obj = createDot(x, raster.height - y, dot, radius);
			if (obj) {
				obj.translate(move);
				group.append(obj);
			}
		}
		activeDocument.redraw();
	}
	dot.remove();
	return group;
}