(ns util.helpers)


(defn ->0-1
  [x low high]
  (double (/ (- x low) (- high low))))


(defn minmaxidx [xs & {:keys [f] :or {f min}}]
  (let [m (apply f xs)]
    (first (filter #(== m (nth xs %)) (range (count xs))))))
