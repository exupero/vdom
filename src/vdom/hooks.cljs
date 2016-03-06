(ns vdom.hooks)

(deftype Hook [f]
  Object
  (hook [_ node prop prev]
    (f node prop prev)))

(defn hook [f]
  (Hook. f))

(goog/exportSymbol "Hook" Hook)
(goog/exportSymbol "Hook.prototype.hook" Hook.prototype.hook)
