// raster.js is a base script for all the raster scripts
// that take a selection of a raster and a path object
// as a starting point for some raster processing
// the only thing that has to be defined is the drawDot 
// function
var raster = null, dots = [];

Art.prototype.getCompoundArea = function(area) {
	if (!area) area = 0;
	if (this instanceof Path) return area + this.getArea();
	else if (this instanceof CompoundPath || this instanceof Group) {
		var child = this.firstChild;
		while (child) {
			area = child.getCompoundArea(area);
			child = child.nextSibling;
		}
	}
	return area;
}

function initRaster() {
	var sel = document.selectedItems;
	for (var i = 0; i < sel.length; i++) {
		var obj = sel[i];
		if (!raster) {
			if (obj instanceof Raster) {
				raster = obj;
			} else if (obj instanceof PlacedItem && !obj.eps) {
				// Embed placed images so the raster script can access pixels
				raster = obj.embed(false);
			}
			if (raster) continue;
		}
		dots.push(obj);
	}
	if (!raster || !dots.length)
		Dialog.alert("Please select both a raster item and a graphic item.");
	else
		return true;
}

function executeRaster(createDot, multiple) {
	document.deselectAll();
	var group = new Group();
	for (var i = 0; i < dots.length; i++) {
		// Create a copy of each dot that is moved to the origin so 
		// rasters that scale the dot are simple to realize:
		var dot = dots[i] = dots[i].clone();
		var origin = dot.bounds.center;
		dot.translate(origin.multiply(-1));
		// Scale multiple dots to the same blackness as the first one
		if (multiple && i > 0)
			dot.scale(Math.sqrt(Math.abs(dots[0].getCompoundArea()) / Math.abs(dot.getCompoundArea())));
	}
//	var img = raster.getImage();
	for (var y = 0; y < raster.height; y++) {
		for (var x = 0; x < raster.width; x++) {
//			var c = new java.awt.Color(img.getRGB(x, y));
//			var col = 1 - (0.3 * c.red + 0.59  * c.green + 0.11 * c.blue) / 255;
			var radius = raster.getPixel(x, y).convert(Color.TYPE_GRAY).gray;
			var obj = createDot(x, raster.height - y, multiple ? dots : dots[0], radius);
			if (obj) {
				obj.translate(origin);
				group.appendChild(obj);
			}
		}
		document.redraw();
	}
	for (var i = 0; i < dots.length; i++)
		dots[i].remove();
	return group;
}