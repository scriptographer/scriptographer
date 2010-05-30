var paths = document.getItems({
	type: Path,
	selected: true
});

if (paths.length) {
	var components = {
		scale: { label: 'Scale', value: 0.95 }
	};
	var values = Dialog.prompt('Grow:', components);

	if (values) {
		var path = paths[0];
		var group = new Group([path]);
		for (var i = 0; i < 100; i++) {
			var lastCurve = path.curves.last;
			var p2 = lastCurve.getPoint(1);
			var a2 = lastCurve.getTangent(1).angle;
			var clone = path.clone();
			group.appendTop(clone);
			clone.scale(values.scale);
			var firstCurve = clone.curves.first;
			var p1 = firstCurve.getPoint(0);
			var a1 = firstCurve.getTangent(0).angle;
			clone.rotate(a2 - a1, p1);
			clone.position += p2 - p1;
			path = clone;
			if (clone.length < 1) {
				clone.remove();
				break;
			}
		}
	}
} else {
	Dialog.alert('Please select a path first.');
}