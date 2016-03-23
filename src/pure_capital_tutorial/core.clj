(ns pure-capital-tutorial.core
  (:require [clj-http.client :as client]
            [clojure.instant :as instant]
            [util.date :refer :all])
  )


(require '[clojure.pprint :as p])

(defonce auth {})
;;(def oanda "https://api-fxpractice.oanda.com/v1/")
(def accountId 9116683)

(def local "http://localhost:8080/v1/")

(rcf3339-url
 (instant/read-instant-timestamp "2015-12-16T00:00:00"))

(prn
 (:prices
  (:body
   (client/get (str local "prices?instruments=GBP_USD,EUR_USD")
               {:headers auth :as :json}))))




(map :time (:candles (:body  (client/get (reduce str [local "/candles?count=3"]) {:as :json}))))

(p/pprint
 (client/get (reduce str [local "accounts"]) {:as :json}))


(map :displayName
     (:instruments
      (:body
       (client/get (reduce str [local "instruments?accountId=9116683"])
                                       {:headers auth :as :json}))))

;; character of financial data
(require '[oanda.core :as o])
(require '[incanter [stats :as s]
           [charts :as ch]
           [core :as i]])
(require '[clojure.repl :as r])


(def h1 (o/retrieve-history :gbp-usd :m1 5000))
(p/pprint (keys (first h1)))

(->> (take 50 h1)
    (p/print-table
     [:time
    :openMid
    :lowMid
    :closeMid
    :highMid
    :volume]))

(do (def h1-highs (map :highMid h1))

    (def h1-stat  (o/make-stat h1-highs))

    (def h1-pdf (s/pdf-normal
                 (range (:min h1-stat) (:max h1-stat) 0.0001)
                 :mean (:mean h1-stat)
                 :sd (:sd h1-stat)))
    (i/view (ch/histogram h1-highs :nbins 100 :title "highs histogram"))
    (i/view (ch/xy-plot
             (range 0 (count h1-highs))
             (map #(-> %
                       (- (:mean h1-stat))
                       (/ (:sd h1-stat))) h1-highs))))

(do
    (def h2 (o/retrieve-history :gbp-usd :m1 1000))
    (def h2-highs (map :highMid h2))

    (def h2-stat  (o/make-stat h2-highs))

    (def h2-pdf (s/pdf-normal
        (range (:min h2-stat) (:max h2-stat) 0.0001)
        :mean (:mean h2-stat)
        :sd (:sd h2-stat)))
    (i/view (ch/histogram h2-highs :nbins 100 :title "highs2 histogram"))
    (i/view (ch/xy-plot
            (range 0 (count h2-highs))
            (map #(-> %
                    (- (:mean h2-stat))
                    (/ (:sd h2-stat))) h2-highs)))

)

;; (def h2-hist (i/view (ch/histogram h2-highs :nbins 100 :title "highs2 histogram")))
;; (p/pprint (class h2-hist))
;; (require '[clojure.inspector :as ins])
;; (ins/inspect-table h2)
;; (def a-hist (atom (repeatedly 10 #(rand-int 100))))
;; (reset! a-hist (repeatedly 10 #(rand-int 100)))
;; (i/view (ch/histogram @a-hist ))
;;
;; (def hist-ch (ch/histogram (repeatedly 10 #(rand-int 100))))
;; (def v (i/view hist-ch))
;; (.setChart (.getChartPanel v ) (ch/histogram (repeatedly 10 #(rand-int 100))))
;; (p/pprint (bean v))
;; (.getDataset (.getXYPlot hist-ch))
;; (def scheduler (java.util.concurrent.Executors/newScheduledThreadPool 1))
;; (.scheduleAtFixedRate scheduler
;;                       (fn [] (println "hell"))
;;                       1 1
;;                       java.util.concurrent.TimeUnit/SECONDS
;;                       )
;; (.shutdown scheduler)

;;;;;;;;;;;;;;;;;;;;;;;;;; Moving histogram
;; narrow information to midpoints
(defn ->mid-candles
  "[Candle] -> [Candle]"
  [candles]
  (for [candle candles]
    (select-keys
     candle
    [:time :openMid :highMid :lowMid :closeMid :volume])))

(def candles (->mid-candles h1))
(def candles-stat (o/candles->stat candles))
;; 2 options - 1.center around 0 or map [0..1]
(def candles-norm
  (for [candle candles]
    (o/normalize-0-1-candle candle candles-stat)))

(i/view (ch/line-chart (range 0 5000) (map :volume candles-norm)))
(p/pprint (first candles-norm))
;; it seems like a lot of processing but is very fast
;; last thing:
;; truncate precision

;;(def h (first (map :highMid candles-norm)))
;;(prn (-> h (* 1e6) Math/floor (* 1e-6)))
;;(prn (float h))

(def highs (for [high (map :highMid candles-norm)]
             (-> high (* 1e6) Math/floor (* 1e-6) float)))

(print (first highs))
(i/view (ch/line-chart (range 5000) highs))

(apply min highs)

(def r1 (range 0 1 1e-6))
(count r1)


;; histogram
;; I didn't know that clojure has function for histogram
(defn histogram
  "[Double] -> Int -> [Int]"
  [xs res]
  (let [
        bins (range (apply min xs) (+ 1e-6 (apply max xs)) 1e-6)
        hist (zipmap bins (repeat (count bins) 0))
        freq (frequencies xs)
        hist (persistent!
              (reduce
                (fn [res [k v]] (assoc! res k v))
                (transient hist)
                freq))
        ]
    (map #(apply + %)
         (->> hist sort vals (partition-all (/ (count hist) res))))))

(defn find-first [x xs]
  (apply min (filter #(<= x %) xs)))
(find-first 4.1 (range 10))



(defn histogram
  [xs res]
  (let [step (float (/ 1 res))
        bins (range 0 (+ step 1) step)
        hist (zipmap bins (repeat (count bins) 0))
        freq (frequencies xs)
        hist (reduce (fn [acc [k v]]
                       (let [nk (apply min (filter #(<= k %) bins))
                             ov (get acc nk 0)]
                         (assoc acc nk
                                (+ ov v))))
                     {} freq)
       ]
    (vec (vals (sort hist)))
))

(print  (histogram highs 128))

(do
  (time (def highs-histogram (histogram highs 512)))
    (count highs-histogram)
    (i/view (ch/histogram highs :nbins 256))
    (i/view (ch/bar-chart (range (count highs-histogram)) highs-histogram)))
(print (take 10 highs-histogram))
(count highs-histogram)

(print (first candles))
(i/view (ch/candle-stick-plot :data (i/dataset
                                     [:time :highMid :lowMid :openMid :closeMid :volume]
                  (take 512 candles ))
                              :high :highMid
                              :low :lowMid
                              :open :openMid
                              :close :closeMid
                              :volume :volume
                              :date :time))
