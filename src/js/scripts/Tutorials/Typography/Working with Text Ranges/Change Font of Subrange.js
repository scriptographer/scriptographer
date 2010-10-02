// http://scriptographer.org/tutorials/typography/working-with-text-ranges/

// Create the text item 
var position = new Point(50, 50); 
var textItem = new PointText(position); 
textItem.content = 'This is a textrange';

// Get a TextRange of the first 5 characters 
var subRange = textItem.range.getSubRange(0, 5); 

// Change the font of the range's character style 
subRange.characterStyle.font = app.fonts['Helvetica'];