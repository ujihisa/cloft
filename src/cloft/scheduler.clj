(ns cloft.scheduler)

(def schedule (atom {}))
(def current-sec (atom 0))
(defn settimer [after f]
  (dosync
    (let [wake-up (+ @current-sec after)]
      (swap! schedule assoc wake-up
             (cons f (@schedule wake-up []))))))

(defn on-beat []
  (dosync
    (let [s @schedule
          now @current-sec
          r (s now false)]
      (when r
        (doseq [f r] (f)))
      (swap! schedule dissoc now))
    (swap! current-sec inc)))
