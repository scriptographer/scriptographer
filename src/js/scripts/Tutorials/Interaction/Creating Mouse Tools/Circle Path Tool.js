////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/interaction/creating-mouse-tools/#using-the-distance-that-the-mouse-has-moved
function onMouseUp(event) { 
	var myRadius = event.delta.length; 
	var myCircle = new Path.Circle(event.downPoint, myRadius); 
}