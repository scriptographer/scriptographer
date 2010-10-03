// ////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/interaction/creating-mouse-tools/#my-first-mouse-tool

// Create a new path once, when the script is executed: 
var myPath = new Path(); 

// This function is called whenever the user
// clicks the mouse in the document: 
function onMouseDown(event) { 
	// Add a segment to the path at the position of the mouse: 
	myPath.add(event.point); 
}