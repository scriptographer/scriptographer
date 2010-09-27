// Raster.js is a base script for all the raster scripts
// that take a selection of a raster and a path object
// as a starting point for some raster processing
// the only thing that has to be defined is the drawDot 
// function

var raster = null, dots = [], pixelCount;

Item.prototype.getCompoundArea = function(area) {
	if (!area) area = 0;
	if (this instanceof Path) return area + this.area;
	else if (this instanceof CompoundPath || this instanceof Group) {
		var child = this.firstChild;
		while (child) {
			area = child.getCompoundArea(area);
			child = child.nextSibling;
		}
	}
	return area;
};

function initRaster() {
	var error = false;
	if (!document) {
		Dialog.alert('Please open a document first.');
		return false;
	}
	
	var rasters = document.getItems({
		type: [Raster, PlacedFile],
		selected: true
	});
	for (var i = 0, l = rasters.length; i < l; i++) {
		rasters[i].selected = false;
	}
	
	var sel = document.selectedItems;
	
	if (rasters.length) {
		raster = rasters.first;
		if (raster instanceof PlacedFile && !raster.eps) {
			// Embed placed images so the raster script can access pixels
			raster = raster.embed(false);
		}
		
		for (var i = 0; i < sel.length; i++) {
			var obj = sel[i];
			if (!obj.isAncestor(raster))
				dots.push(obj);
		}
	}
	
	if (!raster || !dots.length) {
		Dialog.alert('Please select both a raster item\nand a graphic item.');
		return false;
	} else {
		pixelCount = raster.height * raster.width;
		var sure = true;
		if (pixelCount > 20000) {
			script.showProgress = false;
			sure = Dialog.confirm('The image you\'re about to rasterize contains ' + pixelCount + ' pixels.\nRasterizing could take a long time.\nAre you sure you want to proceed?');
			script.showProgress = true;
		}
		return sure;
	}
}

function executeRaster(createDot, multiple) {
	document.deselectAll();
	var group = new Group();
	for (var i = 0; i < dots.length; i++) {
		// Create a copy of each dot that is moved to the origin so 
		// rasters that scale the dot are simple to realize:
		var dot = dots[i] = dots[i].clone();
		var origin = dot.bounds.center;
		dot.position -= origin;
		// Scale multiple dots to the same blackness as the first one
		if (multiple && i > 0)
			dot.scale(Math.sqrt(Math.abs(dots[0].getCompoundArea()) / Math.abs(dot.getCompoundArea())));
	}

	for (var y = 0; y < raster.height; y++) {
		for (var x = 0; x < raster.width; x++) {
			app.updateProgress(y * raster.width + x + 1, pixelCount);
			var radius = raster.getPixel(x, y).gray;
			var obj = createDot(x, y, multiple ? dots : dots[0], radius);
			if (obj) {
				obj.position += origin;
				group.appendTop(obj);
			}
		}
		document.redraw();
	}
	for (var i = 0; i < dots.length; i++)
		dots[i].remove();
	return group;
}