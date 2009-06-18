var values = {
	minScale: 0.2,
	maxScale: 0.8,
	rotation: 0.5,
	minBranch: 4,
	maxBranch: 6
};

function onOptions() {
	values = Dialog.prompt('Tree:', {
		minScale: { description: 'Minimal Scale' },
		maxScale: { description: 'Maximal Scale' },
		rotation: { description: 'Rotation' },
		minBranch: { description: 'Minimal Branch Number' },
		maxBranch: { description: 'Maximal Branch Number' }
	}, values);

	if (values.minScale > values.maxScale)
		values.maxScale = values.minScale;
	if (values.minBranch > values.maxBranch)
		values.maxBranch = values.minBranch;
}

var path;
function onMouseDown(event) {
	path = new Path();
	path.moveTo(event.point);
}

function onMouseUp(event) {
	if (path.segments.length > 0) {
		path.pointsToCurves();
		var group = new Group([path]);
		var branches = [ { path: path, scale: 1.0, rotation: 0 } ];
		var count = 0;
		while (branches.length != 0) {
			var newBranches = [];
			for (var i in branches) {
				var branch = branches[i];
				if (branch.scale > 0.2) {
					var curPath = branch.path;
					var prevEndPoint = curPath.segments.last.point;
					var newCount = Math.rand(values.minBranch, values.maxBranch);
					for (var j = 0; j < newCount; j++) {
						var newPath = path.clone();
						var scale = branch.scale * (Math.random() * (values.maxScale - values.minScale) + values.minScale);
						var rotation = branch.rotation + (Math.random() - 0.5) * Math.PI * values.rotation;
						newPath.scale(scale);
						var curStartPoint = newPath.segments[0].point;
						newPath.translate(prevEndPoint - curStartPoint);
						newPath.rotate(rotation, curStartPoint);

						group.appendChild(newPath);
						newBranches.push( { path: newPath, scale: scale, rotation: rotation } );

						count++;
						if (count > 1000) {
							newBranches = [];
							break;
						}
					}
				}
			}
			branches  = newBranches;
		}
	}
}

function onMouseDrag(event) {
	path.lineTo(event.point);
}