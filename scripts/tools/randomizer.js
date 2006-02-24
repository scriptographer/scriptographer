function onInit() {
    size = 50;
}

function onOptions() {
    size = Dialog.prompt("Randomizer:", [ { description: "Radius", type: "Range", value: size, width: 200, min: 0, max: 1000, step: 0.5 } ])[0];
}

function onMouseDown(event) {
    path = new Path();
    path.moveTo(event.point);
}

function onMouseUp(event) {
//    path.pointsToCurves(25, 10, 10.0, 10.0);
}

function onMouseDrag(event) {
    var point = event.point;
    path.curveTo(
        point.add(size * (Math.random() - 0.5), size * (Math.random() - 0.5)),
        point.add(size * (Math.random() - 0.5), size * (Math.random() - 0.5)),
        point.add(size * (Math.random() - 0.5), size * (Math.random() - 0.5))
    );
}