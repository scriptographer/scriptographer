////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/document-items/finding-items-in-the-document/#selected-items-shortcut

var selectedItems = document.selectedItems; 
for(var i = 0; i < selectedItems.length; i++) { 
	var selectedItem = selectedItems[i]; 
	selectedItem.remove(); 
}