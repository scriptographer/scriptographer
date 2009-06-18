include('raster.js');

var size = 10;
var scale = 100;

function createDot(x, y, dot, radius) {
	if (radius > 0) {
		var item = dot.clone();
		item.position += new Point(x, y) * size;
		item.scale(radius + scale);
		item.rotate(radius * Math.PI * 2);
		return item;
	}
	return null;
}

if (initRaster()) {
	var values = Dialog.prompt('Enter Raster Values:', [
		{ value: size, description: 'Grid Size:'},
		{ value: scale, description: 'Object Scale (%):'}
	]);
	if (values) {
		size = values[0];
		scale = values[1] / 100.0;
		executeRaster(createDot);
	}
}
