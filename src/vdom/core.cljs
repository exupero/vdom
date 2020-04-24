(ns vdom.core
  (:require vdom.js))

(def diff js/VDOM.diff)
(def patch js/VDOM.patch)
(def create js/VDOM.create)

(defn flatten-children [children]
  (->> children
    (tree-seq seq? seq)
    (remove seq?)
    (remove nil?)))

(defn html-node [tag attrs children]
  (js/VDOM.VHtml.
    (name tag)
    (clj->js (dissoc attrs :key))
    (clj->js children)
    (:key attrs)))

(defn svg-node [tag attrs children]
  (js/VDOM.VSvg. (name tag) (clj->js attrs) (clj->js children)))

(defn text-node [s]
  (js/VDOM.VText. s))

(declare svg-tree)

(defn virtual-node? [arg]
  ;; use javascript notion of true/false,
  ;; otherwise, this will return true for "" and cause issues
  (js/Boolean (js/VDOM.isVirtualNode arg)))

(defn html-tree [arg]
  (cond
    (nil? arg)
    (text-node "")

    (virtual-node? arg)
    arg

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

    (virtual-node? arg)
    arg

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
        update (if-let [frame js/window.requestAnimationFrame]
                 (fn [f] (frame f))
                 (fn [f] (f)))]
    (.appendChild elem @root)
    (fn [view]
      (let [new-tree (html-tree view)
            patches (diff @tree new-tree)]
        (reset! tree new-tree)
        (update #(swap! root patch patches))))))
