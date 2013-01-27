(ns helper
  (:refer-clojure :exclude [replace])
  (:use [clojure.string :only [split-lines lower-case replace]]))

(defn main [fname]
  (doseq [line (split-lines (slurp fname))
          :let [x (replace (lower-case line) #"_" "-")]]
    (println (format "(def %s Sound/%s)" x line))))

(if-let [[arg & _] *command-line-args*]
  (main arg)
  (prn "give me a file name"))
