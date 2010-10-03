////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/interaction/mouse-tool-events/#graphic-tablet-pressure

function onMouseDrag(event) { 
	var radius = 5 * event.pressure; 
	new Path.Circle(event.point, radius); 
}