////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/interaction/keyboard-interaction/#having-fun-with-the-arrow-keys

// The following example creates a path on execution and then adds
// segment points to it when you press one of the arrow keys in the
// direction of the key.

// The starting position of the line 
var position = new Point(100, 100);

// The amount we will move when an arrow key is pressed: 
var step = 5; 
 
var path = new Path(); 
path.add(position); 
 
function onKeyDown(event) { 
	if(event.keyCode == 'left') { 
		position.x -= step; 
	} 
 
	if(event.keyCode == 'right') { 
		position.x += step; 
	} 
	
	if(event.keyCode == 'up') { 
		position.y -= step; 
	} 
	
	if(event.keyCode == 'down') { 
		position.y += step; 
	} 
	path.add(position); 
}