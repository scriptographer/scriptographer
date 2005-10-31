values = prompt("Enter Spirograph Values:", 
{type: "number", value: 220, title: "Length:", width: 50},
{type: "number", value: 130, title: "Radius 1:", width: 50},
{type: "number", value: 32, title: "Radius 2:", width: 50},
{type: "number", value: 101, title: "Position:", width: 50},
{type: "number", value: 8, title: "Number of passes:", width: 50}
);

if (values != null) {
    var length = values[0];
    var radius1 = values[1];
    var radius2 = values[2];
    var position = values[3];
    var num = values[4];

    var step = 1 * Math.PI / 180.0;
    var theta = 0;

    var art = new Art("path");
    var i = 0;
    while(i <= 360 * num) {
        art.segments.push(
            length * Math.cos(theta) - position * Math.cos(length * theta / radius2),
            length * Math.sin(theta) - position * Math.sin(length * theta / radius2)
        );
        theta += step;
        i++;
        if (i % 10 == 0) redraw();
    }
    art.pointsToCurves();
}