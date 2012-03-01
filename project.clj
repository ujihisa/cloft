(defproject cloft "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[clj-http "0.3.1"]
                 [org.clojure/core.match "0.2.0-alpha9"]]
  :dev-dependencies [[org.bukkit/bukkit "1.1-R6"]
                     [clj-minecraft "1.0.0-SNAPSHOT"]
                     [org.clojure/clojure "1.3.0"]
                     [org.clojure/tools.logging "0.2.3"]]
  :repl-options [:init nil :caught clj-stacktrace.repl/pst+]
  :repositories {"spout-repo-snap" "http://repo.getspout.org/content/repositories/snapshots/"
                 "spout-repo-rel" "http://repo.getspout.org/content/repositories/releases/"})
