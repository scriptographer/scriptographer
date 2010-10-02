// http://scriptographer.org/tutorials/typography/styling-typography/#character-style-inherits-path-style

var position = new Point(100, 100); 
var textItem = new PointText(position); 
textItem.content = 'Roses are red.\rViolets are blue.';

var redWord = textItem.range.words[2]; 
redWord.characterStyle.fillColor = '#ff0000'; 
 
var blueWord = textItem.range.words[5]; 
blueWord.characterStyle.fillColor = '#8F00FF';