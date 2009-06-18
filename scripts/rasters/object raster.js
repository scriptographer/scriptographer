include('raster.js');

function createDot(x, y, dot, radius) {
	if (radius > 0.1) {
		var item = dot.clone();
		item.position += new Point(x, y) * size;
		item.scale(radius * scale);
		return item;
	}
}

if (initRaster()) {
	var values = Dialog.prompt('Enter Raster Values:', [
		{ value: 10, description: 'Grid Size:'},
		{ value: 100, description: 'Object Scale (%):'}
	]);
	if (values) {
		var size = values[0], scale = values[1] / 100;
		executeRaster(createDot);
	}
}