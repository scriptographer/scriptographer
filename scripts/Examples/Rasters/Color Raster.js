var raster, dot;

var rasters = document.getItems({
	type: [Raster, PlacedFile],
	selected: true
});

if(rasters.length) {
	for (var i = 0, l = rasters.length; i < l; i++) {
		rasters[i].selected = false;
	}

	raster = rasters.first;
	if(raster instanceof PlacedFile && !raster.eps) {
		// Embed placed images so the raster script can access pixels
		raster = raster.embed(false);
	}

	var sel = document.selectedItems;

	for (var i = 0; i < sel.length; i++) {
		var obj = sel[i];
		if(!obj.isAncestor(raster)) {
			dot = obj;
			i = sel.length;
		}
	}
}

function setColor(item, color) {
	if (item instanceof Path) {
		if (item.strokeColor) item.strokeColor = color;
		if (item.fillColor) item.fillColor = color;
	}
	var child = item.firstChild;
	while (child) {
		setColor(child, color);
		child = child.nextSibling;
	}
}

function createDot(x, y, dot, color) {
	var item = dot.clone();
	setColor(item, color);
	item.position += new Point(x, y) * values.size;
	return item;
}

if (!raster || !dot) {
	Dialog.alert('Please select both a raster item\nand a graphic item.');
} else {
	var pixelCount = raster.height * raster.width;
	var sure = true;
	if(pixelCount > 20000) {
		script.showProgress = false;
		sure = Dialog.confirm('The image you\'re about to rasterize contains ' + pixelCount + ' pixels.\nRasterizing could take a long time.\nAre you sure you want to proceed?');
		script.showProgress = true;
	}
	
	if (sure) {
		var components = {
			size: { value: 10, label: 'Grid Size', width: 100 }
		};
		var values = Dialog.prompt('Enter Raster Values:', components);

		if (values) {
			document.deselectAll();

			var group = new Group();
			var white = new GrayColor(0);
			
			for (var y = 0; y < raster.height; y++) {
				for (var x = 0; x < raster.width; x++) {
					app.updateProgress(y * raster.width + x + 1, pixelCount);
					var col = raster.getPixel(x, y);
					if (white != col) {
						group.appendTop(createDot(x, raster.height - y, dot, col));
					}
				}
				document.redraw();
			}
		}
	}
}