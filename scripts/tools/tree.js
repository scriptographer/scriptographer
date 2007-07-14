function onInit() {
    minScale = 0.2;
    maxScale = 0.8;
    rotationValue = 0.5;
    minBranch = 4;
    maxBranch = 6;
}

function onOptions() {
	var values = Dialog.prompt("Tree:", [
        { value: minScale, description: "Minimal Scale", width: 50 },
        { value: maxScale, description: "Maximal Scale", width: 50 },
        { value: rotationValue, description: "Rotation", width: 50 },
        { value: minBranch, description: "Minimal Branch Number", width: 50 },
        { value: maxBranch, description: "Maximal Branch Number", width: 50 }
	]);

    if (values != null) {
        minScale = values[0];
        maxScale = values[1];
        rotationValue = values[2];
        minBranch = values[3];
        maxBranch = values[4];
        if (minScale > maxScale) maxScale = minScale;
        if (minBranch > maxBranch) maxBranch = minBranch;
    }
}

function onMouseDown(event) {
    path = new Path();
    path.moveTo(event.point);
}

function onMouseUp(event) {
	if (path.segments.length > 0) {
		path.pointsToCurves();
		var group = new Group();
		group.appendChild(path);
		var branches = [ { path: path, scale: 1.0, rotation: 0 } ];
		var count = 0;
		while (branches.length != 0) {
			var newBranches = [];
			for (var i in branches) {
				var branch = branches[i];
				if (branch.scale > 0.2) {
					var curPath = branch.path;
					var prevEndPoint = curPath.segments[curPath.segments.length - 1].point;
					var newCount = Math.round(Math.random() * (maxBranch - minBranch) + minBranch);
					for (var j = 0; j < newCount; j++) {
						var newPath = path.clone();
						var scale = branch.scale * (Math.random() * (maxScale - minScale) + minScale);
						var rotation = branch.rotation + (Math.random() - 0.5) * Math.PI * rotationValue;
						
						newPath.transform(new Matrix().scale(scale));
						var curStartPoint = newPath.segments[0].point;
						var matrix = new Matrix().translate(prevEndPoint.subtract(curStartPoint));
						matrix.rotate(rotation, curStartPoint);
						newPath.transform(matrix);

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