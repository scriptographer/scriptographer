////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/typography/working-with-text-ranges/#working-with-selected-text

var range = document.selectedTextRange; 
if (range) { 
	range.characterStyle.fontSize += 5; 
} else {
	Dialog.alert('Please select a text range first.');
}