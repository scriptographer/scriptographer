// http://scriptographer.org/tutorials/interaction/creating-mouse-tools/#event-interval

// Call the onMouseDrag function every 30 ms: 
tool.eventInterval = 30; 
 
var path; 
function onMouseDown(event) { 
	// Create a new path and add the first segment where 
	// the user clicked: 
	path = new Path(); 
	path.add(event.point); 
} 
 
function onMouseDrag(event) { 
	var lastPoint = path.segments.last.point; 
 
	// the difference between the current position of the mouse 
	// and the last segment point of the path: 
	var vector = event.point - lastPoint; 
	
	// the position of the new point that we will add to the path: 
	var point = lastPoint + vector / 30; 
	path.add(point); 
}