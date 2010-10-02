var paths = document.getItems({
	type: Path,
	selected: true
});
if (paths.length > 0) {
	var components = {
		distance: { value: 1, label: 'Distance' },
		size: { value: 10, label: 'Size' }
	};
	var values = Dialog.prompt('Stitch:', components);
	if (values) {
		for (var j = 0; j < paths.length; j++) {
			var item = paths[j];
			item = item.clone();
			item.curvesToPoints(values.distance, 10000);
			var mul = 1;
			var res = new Path();
			for (var i = 0, j = item.curves.length; i < j; i++) {
				var curve = item.curves[i];
				var pt = curve.getPoint(0);
				var n = curve.getNormal(0);
				if (n.x != 0 || n.y != 0) {
					n = n.normalize(values.size);
					res.add(pt + (n * mul));
					mul *= -1;
				}
			}
			item.remove();
		}
	}
} else {
	Dialog.alert('Please select a path first.');
}