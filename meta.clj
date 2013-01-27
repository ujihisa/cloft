(ns meta
  (:require [clojure.string :as s]))

(doseq [fullclass (s/split-lines (slurp "/tmp/aaaaa"))
        :let [e (s/replace fullclass #"^.*\." "")]]
  (let [noevent (s/replace e #"Event$" "")
        hyphen (s/lower-case (s/replace e #"(.)(\p{Upper})" "$1-$2"))]
    (println (apply str (interpose "\n"
                                   ["    @EventHandler"
                                    (str "    public void on" noevent "(" fullclass " event) {")
                                    (str "        clojure.lang.Var f = clojure.lang.RT.var(ns, \"" hyphen "\");")
                                    "        if (f.isBound()) f.invoke(event);"
                                    "    }"])))))

