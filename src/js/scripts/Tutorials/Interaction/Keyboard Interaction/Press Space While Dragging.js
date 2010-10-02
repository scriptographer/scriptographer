// http://scriptographer.org/tutorials/interaction/keyboard-interaction/#checking-whether-a-key-is-pressed

var path; 
function onMouseDown(event) { 
	path = new Path(); 
	path.add(event.point); 
} 
 
function onMouseDrag(event) { 
	if(Key.isDown('space')) { 
		// If the space key is down, change the point of 
		// the last segment to the position of the mouse: 
		path.segments.last.point = event.point; 
	} else { 
		// If the space key is not down, add a segment 
		// to the path at the position of the mouse: 
		path.add(event.point); 
	} 
}