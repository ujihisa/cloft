(defproject cloft "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[clj-http "0.5.5"]
                 [swank-clojure/swank-clojure "1.3.3"]
                 [org.zmq/jzmq "1.0.0"]
                 [org.clojure/clojure "1.4.0"]]
  :dev-dependencies [[org.bukkit/bukkit "1.3.2-R0.3-SNAPSHOT"]]
  :repositories {"org.bukkit"
                 "http://repo.bukkit.org/service/local/repositories/snapshots/content/"}
  :javac-options {:destdir "classes/"}
  :java-source-path "javasrc")
