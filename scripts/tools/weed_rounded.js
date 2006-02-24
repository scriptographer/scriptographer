function onInit() {
	size = 10;
	max = 10;
	setIdleEventInterval(1000 / 100); // 100 times a second
}

function options() {
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
	this.vector = new Point(1, 0).transform(Matrix.getRotateInstance(Math.random() * 2 * Math.PI));
	this.path = new Path();
	this.path.moveTo(point);
	this.path.style.stroke.width = Math.random() * 8 + 0.1;
	group.append(this.path);
	this.rotate = 0.2;
	this.count = 0;
	this.max = 0;
}

Branch.prototype.grow = function() {
	if (this.count++ < this.max) {
		this.vector = this.vector.transform(Matrix.getRotateInstance(this.rotate));
	} else {
		this.vector = this.vector.normalize().multiply((1.0 - Math.random() * 0.5) * size);
		this.max = Math.round(Math.random() * Math.PI * 2.0 / Math.abs(this.rotate));
		this.rotate *= -1; //(Math.round(Math.random()) * 2 - 1);
		this.count = 0;
	}
	this.point = this.point.add(this.vector);
	this.path.lineTo(this.point);
}

Branch.prototype.finish = function() {
	this.path.pointsToCurves();
}