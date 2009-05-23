find . -name *.java -exec svn propset svn:eol-style 'native' \{\} \;
find . -name *.cpp -exec svn propset svn:eol-style 'native' \{\} \;
find . -name *.h -exec svn propset svn:eol-style 'native' \{\} \;
find . -name *.js -exec svn propset svn:eol-style 'native' \{\} \;
find . -name *.py -exec svn propset svn:eol-style 'native' \{\} \;
find . -name *.xml -exec svn propset svn:eol-style 'native' \{\} \;
find . -name *.html -exec svn propset svn:eol-style 'native' \{\} \;
find . -name *.css -exec svn propset svn:eol-style 'native' \{\} \;
find . -name *.properties -exec svn propset svn:eol-style 'native' \{\} \;
find . -name *.sh -exec svn propset svn:eol-style 'native' \{\} \;
find . -name *.bat -exec svn propset svn:eol-style 'native' \{\} \;

find . -name *.java -exec svn propset svn:keywords 'Author Date Id Rev URL' \{\} \;
find . -name *.cpp -exec svn propset svn:keywords 'Author Date Id Rev URL' \{\} \;
find . -name *.h -exec svn propset svn:keywords 'Author Date Id Rev URL' \{\} \;
find . -name *.js -exec svn propset svn:keywords 'Author Date Id Rev URL' \{\} \;
find . -name *.py -exec svn propset svn:keywords 'Author Date Id Rev URL' \{\} \;
find . -name *.xml -exec svn propset svn:keywords 'Author Date Id Rev URL' \{\} \;
find . -name *.html -exec svn propset svn:keywords 'Author Date Id Rev URL' \{\} \;
find . -name *.css -exec svn propset svn:keywords 'Author Date Id Rev URL' \{\} \;
find . -name *.properties -exec svn propset svn:keywords 'Author Date Id Rev URL' \{\} \;
find . -name *.sh -exec svn propset svn:keywords 'Author Date Id Rev URL' \{\} \;
find . -name *.bat -exec svn propset svn:keywords 'Author Date Id Rev URL' \{\} \;

find . -name *.java -exec svn propset svn:mime-type 'text/plain' \{\} \;
find . -name *.cpp -exec svn propset svn:mime-type 'text/plain' \{\} \;
find . -name *.h -exec svn propset svn:mime-type 'text/plain' \{\} \;
find . -name *.properties -exec svn propset svn:mime-type 'text/plain' \{\} \;

find . -name *.bat -exec svn propset svn:mime-type 'application/x-bat' \{\} \;
find . -name *.sh -exec svn propset svn:mime-type 'application/x-sh' \{\} \;

find . -name *.css -exec svn propset svn:mime-type 'text/css' \{\} \;
find . -name *.js -exec svn propset svn:mime-type 'text/javascript' \{\} \;
find . -name *.py -exec svn propset svn:mime-type 'text/x-python' \{\} \;
find . -name *.xml -exec svn propset svn:mime-type 'svn:mime-type=text/xml' \{\} \;
find . -name *.html -exec svn propset svn:mime-type 'text/html' \{\} \;

find . -name *.gif -exec svn propset svn:mime-type 'image/gif' \{\} \;
find . -name *.jpg -exec svn propset svn:mime-type 'image/jpeg' \{\} \;
find . -name *.jpeg -exec svn propset svn:mime-type 'image/jpeg' \{\} \;
find . -name *.png -exec svn propset svn:mime-type 'image/png' \{\} \;
find . -name *.tif -exec svn propset svn:mime-type 'image/tiff' \{\} \;
find . -name *.tiff -exec svn propset svn:mime-type 'image/tiff' \{\} \;

find . -name *.pdf -exec svn propset svn:mime-type 'application/pdf' \{\} \;

find . -name *.sh -exec svn propset svn:executable ON \{\} \;
find . -name *.bat -exec svn propset svn:executable ON \{\} \;
