////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/interaction/creating-mouse-tools/#line-tool

function onMouseUp(event) { 
	var myPath = new Path(); 
	myPath.add(event.downPoint); 
	myPath.add(event.point); 
}