////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/document-items/hit-tests/#hit-results

function onMouseDown(event) { 
	var hitResult = document.hitTest(event.point); 
	// If there is a hitResult and the hitResult 
	// contains a segment: 
	if(hitResult && hitResult.segment) { 
		// Create a circle shaped path at the position 
		// of the segment: 
		new Path.Circle(hitResult.segment.point, 5); 
	} 
}