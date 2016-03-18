(ns util.process-csv
  (:require
   [clojure.java.io :as io]))


(defn mean [xs]
  (/ (reduce + xs) (count xs)))

(defn variance
  ([xs] (variance xs (mean xs))
   ;; (-
   ;;  (/ (reduce + (map #(Math/pow % 2) xs)) (count xs))
   ;;  (Math/pow (mean xs) 2))
   )
  ([xs m]
   (/ (reduce + (map #(Math/pow (- % m) 2) xs)) (count xs)))
  ;; (let [m (mean xs)]
  ;;   (/ (reduce + (map #(Math/pow (- % m) 2) xs)) (count xs)))
  )
