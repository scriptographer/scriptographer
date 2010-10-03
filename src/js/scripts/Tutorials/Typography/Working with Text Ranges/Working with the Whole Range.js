////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/typography/working-with-text-ranges/#working-with-the-whole-range-of-a-text-item

// Create the text item 
var position = new Point(50, 50); 
var textItem = new PointText(position); 
textItem.content = 'This is a textrange'; 
textItem.range.characterStyle.font = app.fonts['Helvetica'];