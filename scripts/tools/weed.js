function onInit() {
	size = 10;
	max = 10;
	setIdleEventInterval(1000 / 100); // 100 times a second
}

function onOptions() {
	values = Dialog.prompt("Random Radius:", [
		{ value: size, description: "Size", width: 50},
		{ value: max, description: "Max", width: 50}
	]);
	if (values != null) {
		size = values[0];
		max = values[1];
	}
}

function onMouseDown(event) {
	branches = [];
	var count = Math.round(Math.random() * (max - 1.0) + 1.0);
	var group = new Group();
	for (var i = 0; i < count; i++)
		branches.push(new Branch(event.point, group));
}

function onMouseUp(event) {
	for (var i in branches)
		branches[i].finish();
}

function onMouseDrag(event) {
	for (var i in branches)
		branches[i].grow();
}

// Branch:

function Branch(point, group) {
	this.point = point;
	this.vector = new Point(Math.random() * 20, 0).rotate(Math.random() * 2 * Math.PI);
	this.path = new Path();
	this.path.moveTo(point);
	group.appendChild(this.path);
}

Branch.prototype.grow = function() {
	this.vector = this.vector.transform(new Matrix().rotate(Math.random() - 0.5));
	this.point = this.point.add(this.vector);
	this.path.lineTo(this.point);
}

Branch.prototype.finish = function() {
	this.path.pointsToCurves();
}
