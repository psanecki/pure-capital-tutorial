(ns pure-capital-tutorial.core
  (:require
   [clj-http.client :as client]
   [clojure.repl :as r]
   [clojure.pprint :as p]
   [clojure.instant :as instant]
   [incanter
    [stats :as s]
    [charts :as ch]
    [core :as i]]
   [oanda.core :as o]
   [model.indicators :as ind]
   [util.date :refer :all]
   [util.scheduler :as sch]
 ;; :reload-all
  )
  )


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



(def h1 (o/retrieve-history :gbp-usd :m1 5000))
(p/pprint (keys (first h1)))
(def u-h1 (o/move-window h1))

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
;; ;;;;;;;;;;;;;;;;;;;;;;;;;; Moving histogram
;; narrow information to midpoints

(def h1 (o/retrieve-history :eur-usd :m1 5000))
(count h1)
(do

  (def candles (o/->mid-candles h1))
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
  (i/view (ch/line-chart (range 5000) highs)))




;; histogram
;; I didn't know that clojure has function for histogram

(require '[clj-fudi.core :as fudi])
(require '[model.indicators :as ind] :reload-all)

(p/pprint (o/get-instrument-list))

(defn histogram->pd [])
(defn histogram->pd
  []
  (let [history (o/retrieve-history :chf-jpy :m1 5000)
        candles (o/->mid-candles history)
        stat (o/candles->stat candles)
        ;; choice: to normalise or not
        candles-norm (o/normalize-0-1-candles candles stat)
        highs (map :highMid candles-norm)
        histogram (ind/histogram highs :nbins 512)
        ]
    (fudi/send-udp {:reset true :histogram (vals  histogram)} :localhost 3001)
    histogram ))

(def hist (histogram->pd))
(count hist)

(i/view (ch/bar-chart (keys hist) (vals hist)))

(defonce a (sch/every-nmsec #'histogram->pd 1000))
;;(print  (ind/histogram highs 128))
(sch/shutdown a)


(do
  (def highs-histogram (ind/histogram (take 5000  highs) 256))
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
(o/get-current-prices :eur-usd)
