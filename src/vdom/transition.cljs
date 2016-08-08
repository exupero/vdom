(ns vdom.transition
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! timeout]]
            [vdom.hooks :refer [hook]]))

(def ^:dynamic *fps* 60)

(defn animate! [f]
  (let [dt (/ 1000 *fps*)]
    (go
      (loop [t 0]
        (let [result (f (/ t 1000))]
          (when-not (= :done result)
            (<! (timeout dt))
            (recur (+ t dt))))))))

(defn transition [f {:keys [duration wait easing]
                     :or {duration 1 wait 0 easing identity}}]
  (hook (fn [el]
          (let [max-time (+ duration wait)]
            (animate! (fn [t]
                        (cond
                          (<= t wait)     (f el 0)
                          (<= t max-time) (f el (easing (/ (- t wait) duration)))
                          :else           (do (f el 1) :done))))))))

(defn sigmoid [a x]
  (let [xa (js/Math.pow x a)]
    (/ xa (+ xa (js/Math.pow (- 1 x) a)))))

(def ease-in #(* % %))
(def ease-out #(- (* % (- % 2))))

(def ease-in-out #(sigmoid 2 %))
