////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/document-items/finding-items-in-the-document/

var hiddenItems = document.getItems({ 
	hidden: true 
}); 
// loop trough the hiddenItems array 
for(var i = 0, l = hiddenItems.length; i < l; i++) { 
	var item = hiddenItems[i]; 
	item.remove(); 
}