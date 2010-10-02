// http://scriptographer.org/tutorials/typography/styling-typography/#font-size

var position = new Point(100, 100); 
var textItem = new PointText(position); 
textItem.content = 'Little\rand\rLarge'; 
 
var littleRange = textItem.range.words[0]; 
littleRange.characterStyle.fontSize = 5; 
 
var largeRange = textItem.range.words[2]; 
largeRange.characterStyle.fontSize = 15;