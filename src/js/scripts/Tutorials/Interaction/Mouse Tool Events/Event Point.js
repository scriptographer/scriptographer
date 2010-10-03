////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/interaction/mouse-tool-events/#mouse-position

var path; 
function onMouseDown(event) { 
	// Create a path: 
	path = new Path(); 
	// Add the mouse down position: 
	path.add(event.point); 
} 
 
function onMouseUp(event) { 
	// Add the mouse up position: 
	path.add(event.point); 
}