////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/interaction/creating-mouse-tools/#fixed-distance-drag-events

tool.fixedDistance = 5; 
 
function onMouseDrag(event) { 
	var radius = event.delta.length / 2; 
	new Path.Circle(event.middlePoint, radius); 
}