(defproject cloft "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[clj-http "0.5.6"]
                 [swank-clojure/swank-clojure "1.5.0-SNAPSHOT"]
                 [org.clojure/clojure "1.4.0"]
                 [org.bukkit/bukkit "1.4.7-R0.2-SNAPSHOT"]]
  :dev-dependencies [[org.bukkit/bukkit "1.4.7-R0.2-SNAPSHOT"]
                     [org.zmq/jzmq "1.0.0"]]
  :repositories {"org.bukkit"
                 "http://repo.bukkit.org/service/local/repositories/snapshots/content/"}
  :javac-options {:destdir "classes/"}
  :java-source-paths ["javasrc"])
