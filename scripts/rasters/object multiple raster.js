include("raster.js");

function createDot(x, y, dots, radius) {
	if (radius > 0) {
		// Pick a dot at random:
		var art = dots[Math.floor(dots.length * Math.random())].clone();
		art.transform(new Matrix()
			.translate(x * size, y * size)
			.rotate(radius * rotation * Math.PI / 180.0)
			.scale((radius * radius * gradiation + radius * (1.0 - gradiation)) * scale)
		);
		return art;
	}
}

if (initRaster()) {
	values = Dialog.prompt("Enter Raster Values:", [
		{ value: 10, description: "Grid Size:", width: 50} ,
		{ value: 100, description: "Object Scale (%):", width: 50 },
		{ value: 0.5, description: "Gradiation:", width: 50 },
		{ value: 360.0, description: "Rotation:", width: 50 }
	]);
	if (values) {
		var size = values[0], scale = values[1] / 100.0, gradiation = values[2], rotation = values[3];
		executeRaster(createDot, true);
	}
}