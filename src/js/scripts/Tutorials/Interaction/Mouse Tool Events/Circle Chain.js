function onMouseDrag(event) { 
	var radius = event.delta.length / 2; 
	new Path.Circle(event.middlePoint, radius); 
}