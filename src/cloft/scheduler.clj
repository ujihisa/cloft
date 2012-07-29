(ns cloft.scheduler
  (:require [cloft.cloft :as c]))

(def schedule (atom {}))
(def current-tick (atom 0))
(defn settimer [after f]
  (dosync
    (let [wake-up (+ @current-tick after)]
      (swap! schedule assoc wake-up
             (cons f (@schedule wake-up []))))))

(defn on-beat []
  (dosync
    (let [s @schedule
          now @current-tick
          r (s now false)]
      (when r
        (doseq [f r] (f)))
      (swap! schedule dissoc now))
    (swap! current-tick inc)))

(defn msg [s]
  (prn "scheduler.clj is included from" s))
