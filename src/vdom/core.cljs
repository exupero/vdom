(ns vdom.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [chan <! put!]]
            vdom.js))

(def diff js/VDOM.diff)
(def patch js/VDOM.patch)
(def create js/VDOM.create)

(defn flatten-children [children]
  (->> children
    (tree-seq seq? seq)
    (remove seq?)
    (remove nil?)))

(defn html-node [tag attrs children]
  (js/VDOM.VHtml. (name tag) (clj->js attrs) (clj->js children)))

(defn svg-node [tag attrs children]
  (js/VDOM.VSvg. (name tag) (clj->js attrs) (clj->js children)))

(defn text-node [s]
  (js/VDOM.VText. s))

(declare svg-tree)

(defn html-tree [arg]
  (cond
    (nil? arg)
    (text-node "")

    (seq? arg)
    (html-node :div {} (map html-tree (flatten-children arg)))

    (string? arg)
    (text-node arg)

    (number? arg)
    (text-node arg)

    (= :svg (first arg))
    (svg-tree arg)

    :else
    (let [[tag attrs & children] arg]
      (html-node tag attrs (map html-tree (flatten-children children))))))

(defn svg-tree [arg]
  (cond
    (nil? arg)
    (text-node "")

    (string? arg)
    (text-node arg)

    (number? arg)
    (text-node arg)

    (= :foreignObject (first arg))
    (let [[tag attrs & children] arg]
      (svg-node tag attrs (map html-tree (flatten-children children))))

    :else
    (let [[tag attrs & children] arg]
      (svg-node tag attrs (map svg-tree (flatten-children children))))))

(defn renderer [elem]
  (let [tree (atom (text-node ""))
        root (atom (create @tree))
        update (if (nil? (.-requestAnimationFrame js/window))
                 (fn [f] (f))
                 (fn [f] (.requestAnimationFrame js/window f)))]
    (.appendChild elem @root)
    (fn [view]
      (let [new-tree (html-tree view)
            patches (diff @tree new-tree)]
        (reset! tree new-tree)
        (update #(swap! root patch patches))))))
