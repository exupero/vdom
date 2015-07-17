vdom.min.js: vdom.js
	node_modules/.bin/uglify -s vdom.js -o vdom.min.js

vdom.js: vdom-main.js
	node_modules/.bin/browserify vdom-main.js -o vdom.js
