////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/interaction/creating-mouse-tools/#click-drag-and-release-example

var myPath; 
 
function onMouseDown(event) { 
	myPath = new Path(); 
} 
 
function onMouseDrag(event) { 
	myPath.add(event.point); 
} 
 
function onMouseUp(event) { 
	var myCircle = new Path.Circle(event.point, 5); 
}