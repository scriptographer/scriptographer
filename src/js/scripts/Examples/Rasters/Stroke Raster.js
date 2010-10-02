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
	var components = {
		size: { value: 10, label: 'Grid Size'},
		scale: { value: 10, label: 'Stroke Scale'}
	};
	var values = Dialog.prompt('Enter Raster Values:', components);
	if (values) {
		executeRaster(createDot);
	}
}