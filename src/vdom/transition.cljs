(ns vdom.transition
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! timeout]]
            [vdom.hooks :refer [hook]]))

(def ^:dynamic *fps* 60)

(defprotocol ITransitionQueue
  (transition! [_ speed])
  (enqueue! [_ f])
  (dequeue! [_ id])
  (clear! [_]))

(defrecord TransitionQueue [queue]
  ITransitionQueue
  (transition! [_ speed]
    (let [dt (/ 1000 *fps*)]
      (go
        (<! (timeout 0))
        (loop [t 0]
          (when (seq @queue)
            (js/window.requestAnimationFrame
              (fn []
                (doseq [[id f] @queue]
                  (when-not (f (* (/ t 1000) speed))
                    (swap! queue dissoc id)))))
            (<! (timeout dt))
            (recur (+ t dt)))))))
  (enqueue! [_ f]
    (let [id (gensym "transition")]
      (swap! queue assoc id f)
      id))
  (dequeue! [_ id]
    (swap! queue dissoc id))
  (clear! [_]
    (reset! queue {})))

(defn transition-queue []
  (->TransitionQueue (atom {})))

(defn transition [f {:keys [duration wait easing]
                     :or {duration 1 wait 0 easing identity}}]
  (let [max-time (+ duration wait)]
    (fn [t]
      (cond
        (<= t wait)     (do (f 0) true)
        (<= t max-time) (do (f (easing (/ (- t wait) duration))) true)
        :else           (do (f 1) false)))))

(defn sigmoid [a x]
  (let [xa (js/Math.pow x a)]
    (/ xa (+ xa (js/Math.pow (- 1 x) a)))))

(def ease-in #(* % %))
(def ease-out #(- (* % (- % 2))))
(def ease-in-out #(sigmoid 2 %))
