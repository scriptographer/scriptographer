// http://scriptographer.org/tutorials/document-items/finding-items-in-the-document/

var selectedItems = document.getItems({ 
	selected: true 
}); 
 
if (selectedItems.length > 0) { 
	var item = selectedItems[0]; 
	item.remove(); 
} else { 
	Dialog.alert('Please select something first!'); 
}