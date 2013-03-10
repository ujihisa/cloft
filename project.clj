(defproject cloft "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[clj-http "0.6.4"]
                 [swank-clojure/swank-clojure "1.5.0-SNAPSHOT"]
                 [org.clojure/clojure "1.5.0"]
                 [org.bukkit/bukkit "1.4.7-R1.1-SNAPSHOT"]
                 [org.dynmap/dynmap-api "1.6"]]
  :dev-dependencies [[org.bukkit/bukkit "1.4.7-R1.1-SNAPSHOT"]
                     [org.zeromq/jeromq "0.2.0"]]
  :repositories {"org.bukkit"
                 "http://repo.bukkit.org/service/local/repositories/snapshots/content/"}
  :javac-options {:destdir "classes/"}
  :java-source-paths ["javasrc"])
