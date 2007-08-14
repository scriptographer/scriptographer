function onInit() {
    dist = 3;
    size = 10;
}

function onOptions() {
    var values = Dialog.prompt("Stich:", [
		{ value: dist, description: "Distance", width: 50 },
		{ value: size, description: "Size", width: 50 }
	]);
    dist = values[0];
    size = values[1];
}

function onMouseDown(event) {
    mul = 1;
    art = new Path();
    res = new Path();
}

function onMouseDrag(event) {
    art.segments.add(event.point);
    if (art.getLength() > 10) {
        art.pointsToCurves();
        art.curvesToPoints(dist, 10000);
        for (var i = 0, j = art.curves.length; i < j; i++) {
            var bezier = art.curves[i];
            var pt = bezier.getPoint(0);
            var n = bezier.getNormal(1);
            if (n.x != 0 || n.y != 0) {
                n = n.normalize(size);
                res.segments.add(pt.add(n.multiply(mul)));
                mul *= -1;
            }
        }
        art.remove();
        art = new Path();
        art.segments.add(event.point);
    }
}