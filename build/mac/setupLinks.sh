JAVA=../../../../java/build/
DOCS=../../../../../docs/js/

mkdir build/CS3
mkdir build/CS3/Debug
ln -s $JAVA build/CS3/Debug/java
ln -s $DOCS build/CS3/Debug/doc
mkdir build/CS3/Release
ln -s $JAVA build/CS3/Release/java
ln -s $DOCS build/CS3/Release/doc

mkdir build/CS2
mkdir build/CS2/Debug
ln -s $JAVA build/CS2/Debug/java
ln -s $DOCS build/CS2/Debug/doc
mkdir build/CS2/Release
ln -s $JAVA build/CS2/Release/java
ln -s $DOCS build/CS2/Release/doc

mkdir build/CS
mkdir build/CS/Debug
ln -s $JAVA build/CS/Debug/java
ln -s $DOCS build/CS/Debug/doc
mkdir build/CS/Release
ln -s $JAVA build/CS/Release/java
ln -s $DOCS build/CS/Release/doc
