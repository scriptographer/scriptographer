// http://scriptographer.org/tutorials/interaction/mouse-tool-events/#hitting-items-with-the-mouse

function onMouseDown(event) { 
	// Check whether an item was clicked on: 
	if(event.item) { 
		// If so, scale it: 
		event.item.scale(1.1); 
	} 
}