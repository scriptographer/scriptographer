var paths = document.getItems({
	type: Path,
	selected: true
});

if(paths.length) {
	var components = {
		scale: { label: 'Scale', value: 0.95 }
	};
	var values = Dialog.prompt('Grow:', components);

	if (values) {
		var path = paths[0];
		var group = new Group([path]);
		var count = 0;
		while (count++ < 100) {
			var lastB = path.curves.last;
			var p2 = lastB.getPoint(1);
			var a2 = lastB.getTangent(1).angle;
			var obj = path.clone();
			obj.scale(values.scale);
			group.appendTop(obj);
			if (obj.bounds.width < 1 && obj.bounds.height < 1)
				break;
			var firstB = obj.curves.first;
			var p1 = firstB.getPoint(0);
			var a1 = firstB.getTangent(0).angle;
			obj.rotate(a2 - a1, p1);
			obj.position += p2 - p1;
			path = obj;
		}
	}
} else {
	Dialog.alert('Please select a path first.');
}