var size = 10;
var raster = null, dot = null;
var sel = activeDocument.selectedItems;

for (var i = 0; i < sel.length; i++) {
	obj = sel[i];
	if (raster == null && obj instanceof Raster) raster = obj;
	else if (dot == null && !(obj instanceof Raster)) dot = obj;
	if (raster != null && dot != null) break;
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
	var m = new Matrix();
	m.translate(x * size, y * size);
	item.transform(m); 
	return item;
}

if (raster != null && dot != null) {
	var pixelCount = raster.height * raster.width;
	var sure = true;
	if(pixelCount > 20000) {
		script.showProgress = false;
		sure = Dialog.confirm('The image you\'re about to rasterize contains ' + pixelCount + ' pixels.\nRasterizing could take a long time.\nAre you sure you want to proceed?');
		script.showProgress = true;
	}
	
	if (sure) {
		var values = Dialog.prompt('Enter Raster Values:', [
			{ value: size, description: 'Grid Size:', width: 100 }
		]);

		if (values) {
			activeDocument.deselectAll();
			size = values[0];

			var group = new Group();
			var white = new GrayColor(0);

			for (var y = 0; y < raster.height; y++) {
				for (var x = 0; x < raster.width; x++) {
					app.updateProgress(y * raster.width + x + 1, pixelCount);
					var col = raster.getPixel(x, y);
					if (!white == col) {
						group.appendChild(createDot(x, raster.height - y, dot, col));
					}
				}
				activeDocument.redraw();
			}
		}
	}
}