include("raster multiple.js");

function createDot(x, y, dot, radius) {
    if (radius > 0) {
        var art = dot.clone();
        var m = new Matrix();
        m.translate(x * size, y * size);
        m.rotate(radius * rotation * Math.PI / 180.0);
        m.scale((radius * radius * gradiation + radius * (1.0 - gradiation)) * scale);
        art.transform(m); 
        return art;
    }
    return null;
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
/*
        var rect = new Art("rect", new Rect(0, 0, size, size));
        var s = Math.abs(rect.getArea() / getCompoundArea(dots[0]));
        rect.remove();
        var m = new Matrix().scale(s);
        for (var i = 0; i < dots.length; i++) dots[i].transform(m);
*/
        executeRaster();
    }
}