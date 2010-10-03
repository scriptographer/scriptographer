////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/typography/working-with-text-ranges/#helvarial

var textItems = document.getItems({ 
	type: 'TextItem', 
	selected: true 
}); 
 
var arial = app.fonts['Arial']; 
var helvetica = app.fonts['Helvetica']['Regular']; 
 
if (textItems.length > 0) { 
	var textItem = textItems[0]; 
	var characters = textItem.range.characters; 
	for(var i = 0; i < characters.length; i++) { 
		var character = characters[i]; 
		if (i.isOdd()) { 
			character.characterStyle.font = arial; 
		} else { 
			character.characterStyle.font = helvetica; 
		} 
	} 
} else {
	Dialog.alert('Please select a Text Item first.');
}