////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/interaction/mouse-tool-events/#mouse-position

// The mouse has to be moved at least 10 pt
// before the next onMouseDrag event is called: 
tool.minDistance = 10; 
 
function onMouseDrag(event) { 
	var path = new Path(); 
	var vector = event.delta; 
 
	// rotate the vector by 90 degrees: 
	vector.angle += 90; 
 
	// change its length to 5 pt: 
	vector.length = 5; 
	
	path.add(event.middlePoint + vector); 
	path.add(event.middlePoint - vector); 
}