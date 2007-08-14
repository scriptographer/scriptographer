function onInit() {
	scale = 0.9;
}

function onOptions() {
	var values = Dialog.prompt("Grow:", [
		{ value: scale, description: "Scale", width: 50 }
	]);
	if (values != null) {
		scale = values[0];
	}
}

function onMouseDown(event) {
	path = new Path();
	path.moveTo(event.point);
}

function onMouseUp(event) {
	path.pointsToCurves();
	var group = new Group();
	group.appendChild(path);
	var count = 0;
	while (count++ < 100) {
		var lastB = path.curves[path.curves.length - 1];
		var p2 = lastB.getPoint(1);
		var a2 = lastB.getTangent(1).getAngle();
		var obj = path.clone();
		obj.scale(scale);
		group.appendChild(obj);
		if (obj.bounds.width < 1 && obj.bounds.height < 1)
			break;
		var firstB = obj.curves[0];
		var p1 = firstB.getPoint(0);
		var a1 = firstB.getTangent(0).getAngle();
		obj.transform(new Matrix().translate(p2.subtract(p1)).rotate(a2 - a1, p1));
		path = obj;
	}
}

function onMouseDrag(event) {
	path.lineTo(event.point);
}