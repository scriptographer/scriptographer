function onInit() {
black = new Grayscale(1);
degree = 52;
length = 80;
difference = 30;
amount = 4;
baseline = 1;
}

function onOptions() {
	values = Dialog.prompt("fizzlemaster3000", [
		{description: "1 - direction: *", value: degree, width: 50},
		{description: "2 - growth:  (0 = flat / 100 = long strokes)", value: length, width: 50},
		{description: "3 - disturbance:  (0 = boring / 50 = wild)", value: difference, width: 50},
		{description: "4 - bunches:  (1 = one bunch / 50 = fifty bunches)", value: amount, width: 20},
		{description: "5 - baseline: **  (1 = yes / 0 = no)", value: baseline, width: 20},
		{description: ""},
		{description: "*  enter value in degree. (0 = up / 90 = right / 180 = down...)"},
		{description: "**  creates the shape you're actually drawing. (no fill, no stroke)"},
		{description: ""}
	]);
	if (values != null) {
		degree = values[0];
		length = values[1];
		difference = values[2];
		amount = values[3];
		baseline = values[4];
	}
}

function onMouseDown(event) {
    arts = new Array(amount);
    for (i = 0; i < amount; i++) {
        arts[i] = new Path();
        arts[i].style.fill.color = null;
        arts[i].style.stroke.color = black;
        arts[i].style.stroke.width = 0.2;
    }
 	if (baseline == 1) {
		artbase = new Path();
		artbase.style.fill.color = null;
		artbase.style.stroke.color = null;
		artbase.style.stroke.width = 0;
	}
}

function onMouseUp(event) {
//oh no it's empty :(
}

function onMouseDrag(event) {
    for (var i = 0; i < amount; i++) {
        arts[i].segments.add(event.point);
    
        var rand_degree = degree + ( ((Math.random() - 0.5) * 2.0) * difference );
        var rand_length = length * Math.random();
        var rad = -(rand_degree-90) * Math.PI / 180;
        var x = Math.cos(rad) * rand_length;
        var y = Math.sin(rad) * rand_length;

        arts[i].segments.add(event.point.add(x, y));
    }
   if (baseline == 1) artbase.segments.add(event.point.add(0,0));
}