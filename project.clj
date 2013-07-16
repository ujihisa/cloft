(defproject cloft "1.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :license {:name "GPL-3 or later"}
  :dependencies [[clj-http "0.7.5"]
                 [org.clojure/clojure "1.5.1"]
                 [org.bukkit/bukkit "1.5-R0.1-SNAPSHOT"]
                 [org.dynmap/dynmap-api "1.8"]]
  :dev-dependencies [[org.bukkit/bukkit "1.6.2-R0.1-SNAPSHOT"]
                     [org.zeromq/jeromq "0.2.0"]]
  :repositories {"org.bukkit"
                 "http://repo.bukkit.org/service/local/repositories/snapshots/content/"
                 "for dynmap"
                 {:url "http://repo.mikeprimm.com/"
                  :checksum :warn}}
  :javac-options {:destdir "classes/"}
  :java-source-paths ["javasrc"])
