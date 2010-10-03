var path;

var minSize = 10;

function onMouseDrag(event) {
	// If the user dragged more then minSize:
	if (event.delta.length > minSize) {
		// If there is no path, make one:
		if (!path) {
			path = new Path();
			path.add(event.lastPoint);
		}

		var step = event.delta / 2;
		step.angle += 90;

		// The top point: the middle point + the step rotated by 90 degrees:
		//   -----*
		//   |
		//   ------
		var top = event.middlePoint + step;

		// The bottom point: the middle point - the step rotated by 90 degrees:
		//   ------
		//   |
		//   -----*
		var bottom = event.middlePoint - step;

		path.add(top);
		path.insert(0, bottom);
		path.smooth();
	} else {
		// If the user dragged too slowly:
		
		// If there is currently a path, close it
		if (path) {
			path.add(event.point);
			path.closed = true;
			path.smooth();

			// Set path to null (nothing) so the path check above
			// will force a new path next time the user drags fast enough:
			path = null;
		}
	}
}

function onMouseUp(event) {
	if (path) {
		path.add(event.point);
		path.closed = true;
		path.smooth();
		
		// Set path to null (nothing) so the path check above
		// will force a new path next time the user drags fast enough:
		path = null;
	}
}