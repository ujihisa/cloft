(defproject cloft "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[clj-http "0.3.1"]
                 [swank-clojure/swank-clojure "1.3.3"]
                 [org.zmq/jzmq "1.0.0"]
                 [org.clojure/clojure "1.4.0"]]
  :dev-dependencies [[org.bukkit/bukkit "1.2.5-R4.1-SNAPSHOT"]]
  ;:repl-options [:init nil :caught clj-stacktrace.repl/pst+]
  ;:repositories {"spout-repo-snap" "http://repo.getspout.org/content/repositories/snapshots/"
  ;               "spout-repo-rel" "http://repo.getspout.org/content/repositories/releases/"})
  :repositories {"org.bukkit"
                 "http://repo.bukkit.org/service/local/repositories/snapshots/content/"}
  :javac-options {:destdir "classes/"}
  :java-source-path "javasrc")
