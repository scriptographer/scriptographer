// http://scriptographer.org/tutorials/document-items/hit-tests/#hitting-items-with-the-mouse

function onMouseDown(event) { 
	// Check whether an item was clicked on: 
	if(event.item) { 
		// If so, remove it: 
		event.item.remove(); 
	} 
}