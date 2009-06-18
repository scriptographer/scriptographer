var point, path;
var values = { size: 10 };

function onOptions() {
	values = Dialog.prompt('Raster:', {
		size: { description: 'Size'}
	}, values);
}

function getPos(pt) {
	return (pt / values.size).round() * values.size;
}

function onMouseDown(event) {
    point = getPos(event.point);
    path = new Path();
    path.moveTo(point);
}

function onMouseDrag(event) {
    var p = getPos(event.point);
    if (point != p) {
        path.lineTo(p);
        point = p;
    }
}