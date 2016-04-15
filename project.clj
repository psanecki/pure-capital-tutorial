(defproject pure-capital-tutorial "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :jvm-opts [
            ;; "-Dauth=~/Documents/hacking_capital/local.edn"
             ]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 ;;[incanter "1.5.7"]
                 [clj-http "2.0.0"]
                 ]
  :profiles {:uberjar {:aot :all}}
  :plugins [[lein-gorilla "0.3.6"]]
)
