 function onInit() {
         size = 50;
         radius = 100;
         dichte = 10;
}

function onOptions() {
    values = Dialog.prompt("graffmaster:", [
        { value: size, description: "size", width: 20 },
        { value: radius, description: "radius", width: 30 },
        { value: dichte, description: "dichte", width: 20 }
	]);
    if (values != null) {
        size = values[0];
        radius = values[1];
        dichte = values[2];
    }
}


function onMouseDown(event) {
}

function onMouseUp(event) {
}

function onMouseDrag(event) {
    var pt = event.point;
    for (var i=1; i < dichte; i++) {
        var degree = Math.random() * 360;
        var rad = radius * Math.random();
        var xPt = pt.x + rad * Math.sin(degree * Math.PI / 180);
        var yPt = pt.y + rad * Math.cos(degree * Math.PI / 180);
        var newSize = size * Math.random() ;
        rect = new Rectangle(0, 0, newSize, newSize);
        rect.center = new Point(xPt, yPt);
        activeDocument.createOval(rect);
    }
}