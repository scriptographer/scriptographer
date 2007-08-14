var sel = document.getMatchingItems(Path, { selected: true });
if (sel.length > 0) {
	values = Dialog.prompt("Stich:", [
		{ value: 1, description: "Distance", width: 50 },
		{ value: 10, description: "Size", width: 50 }
	]);
	if (values) {
		var dist = values[0];
		var size = values[1];
		for (var j = 0; j < sel.length; j++) {
			var art = sel[j];
			art = art.clone();
			art.curvesToPoints(dist, 10000);
			var mul = 1;
			var res = new Path();
	        for (var i = 0, j = art.curves.length; i < j; i++) {
				var curve = art.curves[i];
				var pt = curve.getPoint(0);
				var n = curve.getNormal(0);
	            if (n.x != 0 || n.y != 0) {
	                n = n.normalize(size);
	                res.segments.add(pt.add(n.multiply(mul)));
	                mul *= -1;
	            }
			}
			art.remove();
		}
	}
} else {
	Dialog.alert("Please select a path.");
}