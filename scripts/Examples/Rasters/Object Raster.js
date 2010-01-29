include('raster.js');

function createDot(x, y, dot, radius) {
	if (radius > 0.1) {
		var item = dot.clone();
		item.position += new Point(x, y) * values.size;
		item.scale(radius * values.scale);
		return item;
	}
}

if (initRaster()) {
	var values = Dialog.prompt('Enter Raster Values:', {
		size: { value: 10, description: 'Grid Size'},
		scale: { value: 100, description: 'Object Scale (%)'}
	});
	if (values) {
		values.scale /= 100;
		executeRaster(createDot);
	}
}