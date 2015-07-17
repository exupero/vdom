(ns vdom.hooks)

(deftype Hook [f]
  Object
  (hook [_ x]
    (f x)))

(defn hook [f]
  (Hook. f))

(goog/exportSymbol "Hook" Hook)
(goog/exportSymbol "Hook.prototype.hook" Hook.prototype.hook)
