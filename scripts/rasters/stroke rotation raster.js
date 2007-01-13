include("raster.js");

var size = 10;
var scale = 10;

function createDot(x, y, dot, radius) {
	var art = dot.clone();
	art.style.stroke.width = radius * scale;
	var m = new Matrix();
	m.translate(x * size, y * size);
	m.rotate(radius * Math.PI * 2);
/*
	if ((y % 2) == 0) {
		m.translate(size / 2 , 0);
	}
*/
	art.transform(m); 
	return art;
}

if (initRaster()) {
	values = Dialog.prompt("Enter Raster Values:", [
		{ value: size, description: "Grid Size:", width: 50 },
		{ value: scale, description: "Stroke Scale:", width: 50 }
	]);
	if (values) {
		size = values[0];
		scale = values[1];
		executeRaster(createDot);
	}
}