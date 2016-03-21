(defproject clj-fudi "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :jvm-opts [
             "-Dauth=/home/przemek/Documents/hacking_capital/local.edn"
            ;; "-Dauth=/home/przemek/Documents/hacking_capital/auth.edn"
             ]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [org.clojure/data.csv "0.1.3"]
                 [incanter "1.5.7"]
                 [clj-http "2.0.0"]
                 [cheshire "5.5.0"]
                 [clj-http-lite "0.3.0"]
                 ]

)
