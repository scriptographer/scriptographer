include('raster.js');

function createDot(x, y, dot, radius) {
	var item = dot.clone();
	item.strokeWidth = radius * values.scale;
	item.position += new Point(x, y) * values.size;
	item.rotate(radius * Math.PI * 2);
	return item;
}

if (initRaster()) {
	var values = Dialog.prompt('Enter Raster Values:', {
		size: { value: 10, description: 'Grid Size' },
		scale: { value: 10, description: 'Stroke Scale' }
	});
	if (values) {
		executeRaster(createDot);
	}
}