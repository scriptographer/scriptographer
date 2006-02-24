function onInit() {
	black = new Grayscale(1);
}

function onMouseDown(event) {
    art = new Path();
    activeDocument.createStar(5, event.point, 5, 15).style.fill.color = black;
    art.moveTo(event.point);
}

function onMouseUp(event) {
    activeDocument.createStar(5, event.point, 5, 15).style.fill.color = black;
    art.style.fill.color = null;
    art.style.stroke.color = black;
    art.style.stroke.width = 0.5;
}

function onMouseDrag(event) {
    art.lineTo(event.point.add(event.point));
    art.lineTo(event.point);
}
