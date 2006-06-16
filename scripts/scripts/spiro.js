var length = 220;
var radius1 = 30;
var radius2 = 10;
var position = 150;
var num = 2;

values = Dialog.prompt("Enter Spirograph Values", [
	{ value: length, description: "Length", width: 50 },
	{ value: radius1, description: "Radius 1", width: 50 },
	{ value: radius2, description: "Radius 2", width: 50 },
	{ value: position, description: "Position", width: 50 },
	{ value: num, description: "Number of passes", width: 50 }
]);

if (values != null) {
    length = values[0];
    radius1 = values[1];
    radius2 = values[2];
    position = values[3];
    num = values[4];

    var step = 1 * Math.PI / 180.0;
    var theta = 0;

    var art = new Path();
    var i = 0;
    while(i <= 360 * num) {
        art.segments.add(new Point(
            length * Math.cos(theta) - position * Math.cos(length * theta / radius2),
            length * Math.sin(theta) - position * Math.sin(length * theta / radius2)
        ));
        theta += step;
        i++;
//        if (i % 10 == 0) activeDocument.redraw();
    }
    art.pointsToCurves();
}