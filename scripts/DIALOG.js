/*
var file = Dialog.chooseColor(null, new java.awt.Color(0.5, 0.2, 0.9));
var file = Dialog.fileOpen("Testing", ["*.ai"], ScriptographerEngine.baseDir);
print(file);
var file = Dialog.fileSave("Testing", ["*.ai"], ScriptographerEngine.baseDir);
print(file);
var file = Dialog.chooseDirectory("Testing", ScriptographerEngine.baseDir);
print(file);
*/

var values = Dialog.prompt("Enter some values", [
	new Dialog.PromptItem(Dialog.PromptItem.TYPE_NUMBER, "one", 10),
	new Dialog.PromptItem(Dialog.PromptItem.TYPE_STRING, "two", "Bla bla bla"),
	new Dialog.PromptItem(Dialog.PromptItem.TYPE_RANGE, "three", 10, 0, 100, 5)
]);

for (var i in values) {
	print(values[i]);
}