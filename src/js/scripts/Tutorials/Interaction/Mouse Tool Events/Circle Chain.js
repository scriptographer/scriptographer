////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/document-items/finding-items-in-the-document/#selected-items-shortcut

function onMouseDrag(event) { 
	var radius = event.delta.length / 2; 
	new Path.Circle(event.middlePoint, radius); 
}