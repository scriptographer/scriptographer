include('raster.js');

function createDot(x, y, dots, radius) {
	if (radius > 0) {
		// Pick a dot at random:
		var item = dots[Math.floor(dots.length * Math.random())].clone();
		item.position += new Point(x, y) * size;
		item.rotate(radius * rotation * Math.PI / 180.0, item.position);
		item.scale((radius * radius * gradiation + radius * (1.0 - gradiation)) * scale);
		return item;
	}
}

if (initRaster()) {
	var values = Dialog.prompt('Enter Raster Values:', [
		{ value: 10, description: 'Grid Size:'} ,
		{ value: 100, description: 'Object Scale (%):'},
		{ value: 0.5, description: 'Gradiation:'},
		{ value: 360.0, description: 'Rotation:'}
	]);
	if (values) {
		var size = values[0], scale = values[1] / 100.0, gradiation = values[2], rotation = values[3];
		executeRaster(createDot, true);
	}
}