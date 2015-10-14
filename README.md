# vdom

A Clojure library for constructing virtual DOMs using [virtual-dom](https://github.com/Matt-Esch/virtual-dom).

## Installation

```clojure
[vdom "0.1.1-SNAPSHOT"]
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

To update the HTML, call the render function again with new data.

Rendering works well with core.async channels and goroutines. For instance, assuming you have a channel called `ui` on which UI data trees are passed, the following updates the HTML every time there's a new UI state.

```clojure
(let [render (vdom.core/renderer js/document.body)]
  (go-loop []
    (render (<! ui))
    (recur))
```

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

### vdom.elm

The `vdom.elm` namespace provides some simple FRP-style functions, namely `foldp` and `render!`. They use core.async channels and goroutines instead of true FRP signals.

`foldp` takes a function, an initial value, and a channel of actions, and returns a new channel of values. The values are produced by applying the given function to the current value and the latest value from the channel of actions.

`render!` takes a channel of UI trees and a root element, and updates the DOM whenever a new UI tree comes in.

These two functions, along with `core.async/map`, provide the basis for a simple, [Elm-style architecture](https://github.com/evancz/elm-architecture-tutorial#the-elm-architecture).

```clojure
(defn step [x action]
  (condp = action
    :inc (inc x)
    :dec (dec x)
    x))

(let [actions (core.async/chan)
      models (vdom.elm/foldp step 0 actions)]
  (vdom.elm/render! (core.async/map (fn [x] [:div {} x]) [models]) js/document.body))
```
