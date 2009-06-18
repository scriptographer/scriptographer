var size = 10;
var point, path;

function getPos(pt) {
	return (pt / size).round() * size;
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