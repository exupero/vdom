vdom.min.js: src/vdom/vdom.js
	node_modules/.bin/uglify -s vdom.js -o src/vdom/vdom.min.js

src/vdom/vdom.js: src/vdom/vdom-main.js
	node_modules/.bin/browserify src/vdom/vdom-main.js -o src/vdom/vdom.js
