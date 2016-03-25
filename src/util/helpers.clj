(ns util.helpers)


(defn ->0-1
  [x low high]
  (double (/ (- x low) (- high low))))

(defn ->0-1-float
  [x low high]
  (-> (/ (- x low) (- high low)) (* 1e6) Math/floor (* 1e-6) float))


(defn minmaxidx [xs & {:keys [f] :or {f min}}]
  (let [m (apply f xs)]
    (first (filter #(== m (nth xs %)) (range (count xs))))))
