function onInit() {
    size = 10;
}

function onOptions() {
    size = Dialog.prompt("Random Scribbler:", [ { description: "Radius", value: size, width: 50} ])[0];
}

function onMouseDown(event) {
    path = new Path();
    path.moveTo(event.point);
    point = event.point;
}

function onMouseUp(event) {
//    path.pointsToCurves(25, 10, 10.0, 10.0);
}

function onMouseDrag(event) {
    point = point.add((Math.random() - 0.5) * size, (Math.random() - 0.5) * size);
    path.lineTo(point);
}