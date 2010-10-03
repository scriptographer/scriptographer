////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/typography/working-with-text-ranges/#changing-the-content-of-a-text-range

// Create the text item 
var position = new Point(50, 50); 
var textItem = new PointText(position); 
textItem.content = 'This is a textrange'; 
 
var firstWord = textItem.range.words[0]; 
firstWord.characterStyle.fillColor = '#ff0000'; 
firstWord.content = 'That ';