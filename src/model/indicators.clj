(ns model.indicators
  ;; (:gen-class)
  )



(defn with-ichimoku
  "[Candle] -> [Candle]"
  [window & {:keys [tenkansen-p kijunsen-p senkou-p]
                         :or {tenkansen-p 9
                              kijunsen-p 26
                              senkou-p 52}}]
  (let [candles window
        lows (vec (map :lowMid candles))
        highs (vec (map :highMid candles))
        len (count candles)
        median (fn [i period]
                 (-> (apply max (subvec highs (- i period) (inc i)))
                     (+ (apply min (subvec lows (- i period) (inc i))))
                     (/ 2)))
        ichimoku (for [i (range len)]
                   (let [tenkansen-p (if (< i tenkansen-p)
                                       i
                                       tenkansen-p)
                         tenkansen (median i tenkansen-p)
                         kijunsen-p (if (< i kijunsen-p)
                                      i
                                      kijunsen-p)
                         kijunsen (median i kijunsen-p)
                         senkou-a (/ (+ tenkansen kijunsen) 2)
                         senkou-p (if (< i senkou-p)
                                    i
                                    senkou-p)
                         senkou-b (median i senkou-p)
                         kumo (if (> senkou-b senkou-a) 722 227)]
                     {:tenkanSen tenkansen
                      :kijunSen kijunsen
                      :senkouSpanA senkou-a
                      :senkouSpanB senkou-b
                      :kumo kumo}))]
    (->> ichimoku (map merge candles) vec)))



(defrecord Price
    [price time volume])


(defn candle->price
  "Candle -> Price"
  [candle]
  (let [price (-> (:highMid candle)
                  (+ (:lowMid candle))
                  (+ (:closeMid candle))
                  (/ 3.0))
        time (:time candle)
        volume (:volume candle)]
    (->Price price time volume)))



;; (defn with-aaron
;;   [prices])



(defn with-ema
  "[Price] -> [Price]"
  [prices & {:keys [n prev]
             :or {n 20 prev nil}}]
  (let [alpha (/ 2.0 (+ n 1))]
    (loop [prices0 prices
           acc (transient [])
           ]
      (if (empty? prices0)
        (persistent! acc)
        (let [price (first prices0)
              prev-ema (if (zero? (count acc))
                         (:price price)
                         (:ema (get acc (dec (count acc)))))
              ema (-> price
                      :price
                      (- prev-ema)
                      (* alpha)
                      (+ prev-ema))
              price-with-ema (assoc price :ema ema)]
          (recur (rest prices0) (conj! acc price-with-ema)))))))



(defn with-bbands
  "[Price] -> [Price] "
  [prices & {:keys [n k prev]
             :or {n 20 k 2 prev nil}}]
  (loop [prices0 (with-ema prices)
         acc (transient [])]
    (if (empty? prices0)
      (persistent! acc)
      (let [price (first prices0)
            i (count acc)
            i-low (if (neg? (- i n)) 0 (- i n))
            window (subvec prices i-low (inc i))
            only-prices (map :price window)
            mean (/ (reduce + only-prices) (count only-prices))
            variance (/ (reduce
                         + (map #(Math/pow (- % mean) 2) only-prices))
                        (count only-prices))
            sd (Math/sqrt variance)
            upper-band (+ (:ema price) (* k sd))
            lower-band (- (:ema price) (* k sd))
            price (-> price
                      (assoc :lowerBand lower-band)
                      (assoc :upperBand upper-band))]
        (recur (rest prices0) (conj! acc price))
        ))
    )
  )
