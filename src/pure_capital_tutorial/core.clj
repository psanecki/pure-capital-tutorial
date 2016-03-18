(ns pure-capital-tutorial.core
  (:require [clj-http.client :as client]
            [clojure.instant :as instant]
            [util.date :refer :all])
  )


(defonce auth {})
(def oanda "https://api-fxpractice.oanda.com/v1/")
(def accountId 9116683)

(def local "http://localhost:8080/v1/")

(rcf3339-url
 (instant/read-instant-timestamp "2015-12-16T00:00:00"))

(:prices (:body (client/get (str local "prices?instruments=GBP_USD,EUR_USD") {:headers auth :as :json})))




(map :time (:candles (:body  (client/get (reduce str [local "/candles?count=3"]) {:as :json}))))

(client/get (reduce str [local "accounts"]) {:as :json})


(map :displayName (:instruments (:body (client/get (reduce str [local "instruments?accountId=9116683"])
                                       {:headers auth :as :json}))))
