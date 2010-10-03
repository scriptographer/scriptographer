////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/typography/styling-typography/#changing-the-font-of-a-text-range

var position = new Point(100, 100); 
var textItem = new PointText(position); 
textItem.content = 'Helvetica and Times'; 
 
var hRange = textItem.range.words[0]; 
hRange.characterStyle.font = app.fonts['Helvetica']['Regular']; 
 
var tRange = textItem.range.words[2]; 
tRange.characterStyle.font  = app.fonts['Times'];