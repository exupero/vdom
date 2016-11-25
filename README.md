# vdom

A Clojure library for constructing virtual DOMs using [virtual-dom](https://github.com/Matt-Esch/virtual-dom).

## Installation

```clojure
[vdom "0.2.1-SNAPSHOT"]
```

Then require `vdom.core` or, if you'd like a more reactive experience, `vdom.elm`.

## Building

After installing Node modules with `npm install`, build the Javascript files by running `make`.

## Usage

### vdom.core

The primary interface for vdom is the `renderer` function in `vdom.core`. Call it with a DOM element to get a function that renders a tree of Clojure data into HTML within that element.

For example,

```clojure
(let [render (vdom.core/renderer js/document.body)]
  (render [:div {} "Hello, world"]))
```

To update the HTML, call the render function again with a different argument.

### UI data trees

Vdom is based on [virtual-dom](https://github.com/Matt-Esch/virtual-dom), with a transparent mapping between Clojure data structures and the virtual-dom functions `VNode`, `VText`, and `svg`.

For example, the Clojure tree

```clojure
[:div {:id "root" :className "test"}
 [:span {} "Hello, "]
 [:span {} "world"]]
```

is equivalent to the JavaScript

```javascript
new VNode('div', {id: 'root', className: 'test'}, [
  VNode('span', {}, VText('Hello, ')),
  VNode('span', {}, VText('world'))
])
new VText("Hello, world"));
```

Any children that are seqs are flattened, allowing, for instance,

```clojure
[:ul {}
 (for [i (range 1 6)]
   [:li {} i])]
```

which produces the HTML

```html
<ul>
  <li>1</li>
  <li>2</li>
  <li>3</li>
  <li>4</li>
  <li>5</li>
</ul>
```

Vdom handles SVG nodes transparently, so long as an `svg` node is part of the tree. Descendant nodes of `svg` are constructed with the `virtual-hyperscript/svg` function rather than `VNode`. Descendants of a `foreignObject` tag are constructed with `VNode`.

## Hooks

To get the actual DOM element in your UI tree, include in a node's attributes a virtual-dom hook. Provide a value by calling `vdom.hooks/hook` with a function that takes a DOM node. For instance,

```
[:input {:hookFocus (vdom.hooks/hook (fn [node] (.focus node)))}]
```
