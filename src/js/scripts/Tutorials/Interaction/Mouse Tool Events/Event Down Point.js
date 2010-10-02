// http://scriptographer.org/tutorials/interaction/mouse-tool-events/#mouse-position

function onMouseUp(event) { 
	// Create a path: 
	var path = new Path(); 
 
	// Add the mouse down position: 
	path.add(event.downPoint); 
 
	// Add the mouse up position: 
	path.add(event.point); 
}