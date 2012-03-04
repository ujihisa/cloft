(defproject cloft "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[clj-http "0.3.1"]
                 [swank-clojure/swank-clojure "1.3.3"]
                 [org.clojure/clojure "1.3.0"]]
  :dev-dependencies [[org.bukkit/bukkit "1.2.3-R0"]
                     [clj-minecraft "1.0.0-SNAPSHOT"]]
  ;:repl-options [:init nil :caught clj-stacktrace.repl/pst+]
  ;:repositories {"spout-repo-snap" "http://repo.getspout.org/content/repositories/snapshots/"
  ;               "spout-repo-rel" "http://repo.getspout.org/content/repositories/releases/"})
  :javac-options {:destdir "classes/"}
  :java-source-path "javasrc")
