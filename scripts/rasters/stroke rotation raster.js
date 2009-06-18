include('raster.js');

var size = 10;
var scale = 10;

function createDot(x, y, dot, radius) {
	var item = dot.clone();
	item.strokeWidth = radius * scale;
	item.position += new Point(x, y) * size;
	item.rotate(radius * Math.PI * 2);
	return item;
}

if (initRaster()) {
	var values = Dialog.prompt('Enter Raster Values:', [
		{ value: size, description: 'Grid Size:' },
		{ value: scale, description: 'Stroke Scale:' }
	]);
	if (values) {
		size = values[0];
		scale = values[1];
		executeRaster(createDot);
	}
}