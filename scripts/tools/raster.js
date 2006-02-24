function onInit() {
    size = 10;
}

function getPos(pt) {
    return new Point(Math.round(pt.x / size) * size, Math.round(pt.y / size) * size);
}

function onMouseDown(event) {
    point = getPos(event.point);
    path = new Path();
    path.moveTo(point);
}

function onMouseDrag(event) {
    var p = getPos(event.point);
    if (!point.equals(p)) {
        path.lineTo(p);
        point = p;
    }
}