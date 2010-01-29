include('Raster.js');

function createDot(x, y, dot, radius) {
	if (radius > 0) {
		var item = dot.clone();
		item.strokeWidth = radius * values.scale;
		item.position += new Point(x, y) * values.size;
	}
	return item;
}

if (initRaster()) {
	var values = Dialog.prompt('Enter Raster Values:', {
		size: { value: 10, description: 'Grid Size'},
		scale: { value: 10, description: 'Stroke Scale'}
	});
	if (values) {
		executeRaster(createDot);
	}
}