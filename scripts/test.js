var file = Dialog.chooseColor(null, new java.awt.Color(0.5, 0.2, 0.9));
var file = Dialog.fileOpen("Testing", ["*.ai"], ScriptographerEngine.baseDir);
print(file);
var file = Dialog.fileSave("Testing", ["*.ai"], ScriptographerEngine.baseDir);
print(file);
var file = Dialog.chooseDirectory("Testing", ScriptographerEngine.baseDir);
print(file);
