////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/interaction/keyboard-interaction/#receiving-key-events

function onKeyDown(event) { 
	if(event.keyCode == 'space') { 
		print('The spacebar was pressed!'); 
	} 
} 
 
function onKeyUp(event) { 
	if(event.keyCode == 'space') { 
		print('The spacebar was released!'); 
	} 
}